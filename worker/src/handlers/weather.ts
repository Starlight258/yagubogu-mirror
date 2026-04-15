import type { Env } from '../types';
import { jsonResponse } from '../utils/response';
import { authenticate } from '../middleware/auth';
import { getCorsHeaders } from '../middleware/cors';
import { Logger } from '../utils/logger';

// ─────────────────────────────────────────────────────────────
// 전역 상수
// ─────────────────────────────────────────────────────────────
const CACHE_TTL_SECONDS = 3600;
const KMA_TIMEOUT_MS = 3500;
// 카테고리 10개 × 6시간 = 60행 전부 수신 후 최근접 슬롯 필터링
// (numOfRows=10으로 줄이면 카테고리 알파벳 순 정렬로 인해 T1H·WSD 등 누락됨)
const KMA_NUM_OF_ROWS = 60;
const CONCURRENT_LIMIT = 5;
const STRONG_WIND_MS = 10;

const CACHE_KEY_BASE = 'https://kma-grid-cache.internal/ultra-srt-fcst';

// ─────────────────────────────────────────────────────────────
// 경기장 목록
// ─────────────────────────────────────────────────────────────
const ALL_STADIUMS = [
    { id: 1, name: '광주 기아 챔피언스필드', lat: 35.168139, lng: 126.889111, nx: 59, ny: 75 },
    { id: 2, name: '잠실 야구장', lat: 37.512150, lng: 127.071976, nx: 61, ny: 126 },
    { id: 3, name: '고척 스카이돔', lat: 37.498222, lng: 126.867250, nx: 58, ny: 125 },
    { id: 4, name: '수원 KT 위즈파크', lat: 37.299759, lng: 127.009781, nx: 60, ny: 121 },
    { id: 5, name: '대구 삼성 라이온즈파크', lat: 35.841111, lng: 128.681667, nx: 90, ny: 90 },
    { id: 6, name: '사직야구장', lat: 35.194077, lng: 129.061584, nx: 98, ny: 76 },
    { id: 7, name: '인천 SSG 랜더스필드', lat: 37.436778, lng: 126.693306, nx: 55, ny: 124 },
    { id: 8, name: '창원 NC 파크', lat: 35.222754, lng: 128.582251, nx: 89, ny: 76 },
    { id: 9, name: '대전 한화생명 볼파크', lat: 36.316589, lng: 127.431211, nx: 68, ny: 100 },
    { id: 10, name: '울산 문수 야구장', lat: 35.532334, lng: 129.265575, nx: 101, ny: 84 },
    { id: 11, name: '월명종합경기장 야구장', lat: 35.966360, lng: 126.748161, nx: 56, ny: 92 },
    { id: 12, name: '청주 야구장', lat: 36.638840, lng: 127.470149, nx: 69, ny: 107 },
    { id: 13, name: '포항 야구장', lat: 36.008273, lng: 129.359410, nx: 102, ny: 94 },
    { id: 14, name: '한화생명 이글스파크', lat: 36.317178, lng: 127.429167, nx: 68, ny: 100 },
    { id: 15, name: '대구시민운동장 야구장', lat: 35.881162, lng: 128.586371, nx: 89, ny: 91 },
    { id: 16, name: '무등 야구장', lat: 35.169165, lng: 126.887245, nx: 59, ny: 75 },
    { id: 17, name: '마산 야구장', lat: 35.220855, lng: 128.581050, nx: 89, ny: 76 },
    { id: 18, name: '숭의 야구장', lat: 37.466591, lng: 126.643239, nx: 54, ny: 124 },
    { id: 19, name: '삼성 라이온즈 볼파크', lat: 35.864844, lng: 128.805667, nx: 93, ny: 90 },
] as const;

type Stadium = typeof ALL_STADIUMS[number];
const STADIUM_MAP = new Map<number, Stadium>(ALL_STADIUMS.map(s => [s.id, s]));


// ─────────────────────────────────────────────────────────────
// KST 기반 초단기예보 Base Date/Time 계산
// 매시 :30 생성, :45 이후 API 제공
// ─────────────────────────────────────────────────────────────
function getKmaBaseDateTime(): { baseDate: string; baseTime: string } {
    const now = new Date();
    const formatter = new Intl.DateTimeFormat('ko-KR', {
        timeZone: 'Asia/Seoul',
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit',
        hour12: false,
    });

    const map = new Map(formatter.formatToParts(now).map(p => [p.type, p.value]));
    let year = map.get('year')!;
    let month = map.get('month')!;
    let day = map.get('day')!;
    let hour = parseInt(map.get('hour') ?? '0', 10);
    const min = parseInt(map.get('minute') ?? '0', 10);

    if (min < 45) {
        hour -= 1;
        if (hour < 0) {
            hour = 23;
            const prevDay = new Date(now.getTime() - 24 * 60 * 60 * 1000);
            const prevMap = new Map(formatter.formatToParts(prevDay).map(p => [p.type, p.value]));
            year = prevMap.get('year')!;
            month = prevMap.get('month')!;
            day = prevMap.get('day')!;
        }
    }

    return {
        baseDate: `${year}${month}${day}`,
        baseTime: `${String(hour).padStart(2, '0')}30`,
    };
}


// ─────────────────────────────────────────────────────────────
// RN1 강수량 파싱 (버그 수정: 정규식 이중 이스케이프 제거)
// ─────────────────────────────────────────────────────────────
function parseRainAmount(val: string): number {
    if (val === '강수없음' || val === '0') return 0;
    if (val.includes('미만')) return 0.5;

    // ✅ 정규식 리터럴 안에서 \d, \s, \. 사용 (\\d 아님)
    const rangeMatch = val.match(/(\d+(?:\.\d+)?)\s*~\s*(\d+(?:\.\d+)?)/);
    if (rangeMatch) {
        return (parseFloat(rangeMatch[1]) + parseFloat(rangeMatch[2])) / 2;
    }

    const numMatch = val.match(/\d+(?:\.\d+)?/);
    return numMatch ? parseFloat(numMatch[0]) : 0;
}


// ─────────────────────────────────────────────────────────────
// KMA items → 날씨 결과 변환
// ─────────────────────────────────────────────────────────────
interface WeatherResult {
    condition: string;
    sky: string;
    temperature: string;
    precipitation: string;
    windSpeed: string;
}

function parseKmaToWeather(items: any[]): WeatherResult {
    let pty = 0, t1h = 0, lgt = 0, rn1 = 0, wsd = 0, sky = 1;

    for (const item of items) {
        const val: string = item.fcstValue;
        switch (item.category) {
            case 'PTY': pty = parseInt(val, 10); break;
            case 'T1H': t1h = parseFloat(val); break;
            case 'LGT': lgt = parseFloat(val); break;
            case 'RN1': rn1 = parseRainAmount(val); break;
            case 'WSD': wsd = parseFloat(val); break;
            case 'SKY': sky = parseInt(val, 10); break;
        }
    }

    let condition: string;

    if (lgt > 0) {
        condition = 'THUNDERSTORM';
    } else if (pty === 1 || pty === 5) {
        condition = rn1 >= 1.0 ? 'HEAVY_RAIN' : 'LIGHT_RAIN';
    } else if (pty === 2 || pty === 6) {
        condition = 'RAIN_SNOW';
    } else if (pty === 3 || pty === 7) {
        condition = 'SNOW';
    } else if (wsd >= STRONG_WIND_MS) {
        condition = 'STRONG_WIND';
    } else {
        condition = sky === 1 ? 'CLEAR'
            : sky === 3 ? 'PARTLY_CLOUDY'
                : 'CLOUDY';
    }

    return {
        condition,
        sky: sky === 1 ? '맑음' : sky === 3 ? '구름많음' : '흐림',
        temperature: `${t1h}°C`,
        precipitation: `${rn1}mm`,
        windSpeed: `${wsd}m/s`,
    };
}


// ─────────────────────────────────────────────────────────────
// 동시 요청 수 제한 유틸
// ─────────────────────────────────────────────────────────────
async function batchedAll<T>(
    tasks: Array<() => Promise<T>>,
    concurrency: number,
): Promise<T[]> {
    const results: T[] = [];
    for (let i = 0; i < tasks.length; i += concurrency) {
        const batch = tasks.slice(i, i + concurrency).map(fn => fn());
        results.push(...await Promise.all(batch));
    }
    return results;
}


// ─────────────────────────────────────────────────────────────
// 단일 격자 날씨 조회 (격자 단위 캐시 적용)
// ─────────────────────────────────────────────────────────────
async function fetchGridWeather(
    nx: number,
    ny: number,
    serviceKey: string,
    baseDate: string,
    baseTime: string,
    cache: Cache,
    logger: Logger,
): Promise<WeatherResult> {
    const cacheKey = new Request(
        `${CACHE_KEY_BASE}/${baseDate}/${baseTime}/${nx}/${ny}`,
        { method: 'GET' },
    );

    const cached = await cache.match(cacheKey);
    if (cached) {
        logger.log(`🚀 [Grid Cache Hit] nx=${nx} ny=${ny}`);
        return cached.json<WeatherResult>();
    }

    const apiUrl =
        `https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst` +
        `?serviceKey=${serviceKey}` +
        `&pageNo=1&numOfRows=${KMA_NUM_OF_ROWS}&dataType=JSON` +
        `&base_date=${baseDate}&base_time=${baseTime}` +
        `&nx=${nx}&ny=${ny}`;

    const controller = new AbortController();
    const tid = setTimeout(() => controller.abort(), KMA_TIMEOUT_MS);

    try {
        const res = await fetch(apiUrl, { signal: controller.signal });
        clearTimeout(tid);

        if (!res.ok) throw new Error(`HTTP ${res.status}`);

        const json: any = await res.json();
        const header = json?.response?.header;
        if (header?.resultCode !== '00') {
            throw new Error(`KMA Error: ${header?.resultMsg} (${header?.resultCode})`);
        }

        const items: any[] = json?.response?.body?.items?.item;
        if (!Array.isArray(items) || items.length === 0) {
            throw new Error('KMA 응답 데이터 없음');
        }

        // ✅ 버그 수정: 60행 전부 수신 후 fcstDate+fcstTime이 가장 이른 슬롯만 필터링
        // KMA는 카테고리 알파벳순으로 정렬하므로 numOfRows=10으로는 T1H·WSD 등이 잘림
        const minKey = items.reduce((min, item) => {
            const key = `${item.fcstDate}${item.fcstTime}`;
            return key < min ? key : min;
        }, `${items[0].fcstDate}${items[0].fcstTime}`);

        const nearestItems = items.filter(
            item => `${item.fcstDate}${item.fcstTime}` === minKey,
        );

        const weather = parseKmaToWeather(nearestItems);

        await cache.put(
            cacheKey,
            new Response(JSON.stringify(weather), {
                headers: {
                    'Content-Type': 'application/json',
                    'Cache-Control': `s-maxage=${CACHE_TTL_SECONDS}`,
                },
            }),
        );
        logger.log(`📡 [Grid Cache Miss] nx=${nx} ny=${ny} → KMA 호출 완료`);

        return weather;

    } catch (err: any) {
        clearTimeout(tid);
        logger.warn(`⚠️ 격자(${nx},${ny}) 조회 실패: ${err.message}`);
        return { condition: 'UNKNOWN', sky: 'N/A', temperature: 'N/A', precipitation: 'N/A', windSpeed: 'N/A' };
    }
}


// ─────────────────────────────────────────────────────────────
// 메인 핸들러
// GET /api/stadium/weather?ids=1,2,5
// ─────────────────────────────────────────────────────────────
export async function handleWeather(request: Request, env: Env): Promise<Response> {
    const logger = new Logger(env);
    const corsHeaders = getCorsHeaders(request);

    const authResult = await authenticate(request, env);
    if ('error' in authResult) {
        return jsonResponse(
            { error: authResult.error, hint: authResult.hint },
            authResult.status ?? 401,
            corsHeaders,
        );
    }

    const url = new URL(request.url);
    const idsParam = url.searchParams.get('ids');

    let targetStadiums: Stadium[];

    if (idsParam) {
        const ids = idsParam
            .split(',')
            .map(s => parseInt(s.trim(), 10))
            .filter(n => !isNaN(n));

        if (ids.length === 0) {
            return jsonResponse({ error: '유효한 경기장 ID가 없습니다.' }, 400, corsHeaders);
        }

        targetStadiums = ids.reduce<Stadium[]>((acc, id) => {
            const s = STADIUM_MAP.get(id);
            if (s) acc.push(s);
            return acc;
        }, []);

        if (targetStadiums.length === 0) {
            return jsonResponse({ error: '일치하는 경기장이 없습니다.' }, 400, corsHeaders);
        }
    } else {
        targetStadiums = [...ALL_STADIUMS];
    }

    try {
        if (!env.KMA_API_KEY) throw new Error('KMA_API_KEY missing');

        const serviceKey = encodeURIComponent(env.KMA_API_KEY);
        const { baseDate, baseTime } = getKmaBaseDateTime();
        const cache = caches.default;

        const uniqueGrids = [...new Map(
            targetStadiums.map(s => [`${s.nx}:${s.ny}`, { nx: s.nx, ny: s.ny }]),
        ).values()];

        const gridTasks = uniqueGrids.map(
            ({ nx, ny }) => () =>
                fetchGridWeather(nx, ny, serviceKey, baseDate, baseTime, cache, logger)
                    .then(weather => ({ key: `${nx}:${ny}`, weather })),
        );
        const gridResults = await batchedAll(gridTasks, CONCURRENT_LIMIT);

        const gridWeatherMap = new Map(gridResults.map(r => [r.key, r.weather]));

        const data = targetStadiums.map(s => ({
            id: s.id,
            name: s.name,
            lat: s.lat,
            lng: s.lng,
            nx: s.nx,
            ny: s.ny,
            weather: gridWeatherMap.get(`${s.nx}:${s.ny}`)
                ?? { condition: 'UNKNOWN', sky: 'N/A', temperature: 'N/A', precipitation: 'N/A', windSpeed: 'N/A' },
        }));

        return jsonResponse(
            { success: true, count: data.length, baseDate, baseTime, data },
            200,
            corsHeaders,
        );

    } catch (error: any) {
        logger.error('❌ 서버 내부 오류:', error.message);
        return jsonResponse(
            { error: '날씨 데이터를 가져오지 못했습니다.', message: error.message },
            500,
            corsHeaders,
        );
    }
}