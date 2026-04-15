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
const KMA_NCST_NUM_OF_ROWS = 10;   // 실황: 카테고리 ~8개 × 1시점
const KMA_FCST_NUM_OF_ROWS = 60;   // 예보: 카테고리 10개 × 6시간
const CONCURRENT_LIMIT = 5;
const STRONG_WIND_MS = 10;

const CACHE_KEY_BASE = 'https://kma-grid-cache3.internal';
const KMA_BASE_URL = 'https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0';

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
// KST 파트 추출 공통 유틸
// ─────────────────────────────────────────────────────────────
function getKstParts(date: Date) {
    const fmt = new Intl.DateTimeFormat('ko-KR', {
        timeZone: 'Asia/Seoul',
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit', hour12: false,
    });
    const m = new Map(fmt.formatToParts(date).map(p => [p.type, p.value]));
    return {
        year: m.get('year')!,
        month: m.get('month')!,
        day: m.get('day')!,
        hour: parseInt(m.get('hour') ?? '0', 10),
        min: parseInt(m.get('minute') ?? '0', 10),
    };
}

// 초단기실황 base_time: HH00 (정각), 매시 :40 이후 제공
function getNcstBaseDateTime(): { baseDate: string; baseTime: string } {
    const now = new Date();
    let { year, month, day, hour, min } = getKstParts(now);

    if (min < 40) {
        hour -= 1;
        if (hour < 0) {
            const prev = getKstParts(new Date(now.getTime() - 86_400_000));
            return {
                baseDate: `${prev.year}${prev.month}${prev.day}`,
                baseTime: '2300',
            };
        }
    }

    return {
        baseDate: `${year}${month}${day}`,
        baseTime: `${String(hour).padStart(2, '0')}00`,
    };
}

// 초단기예보 base_time: HH30, 매시 :45 이후 제공
function getFcstBaseDateTime(): { baseDate: string; baseTime: string } {
    const now = new Date();
    let { year, month, day, hour, min } = getKstParts(now);

    if (min < 45) {
        hour -= 1;
        if (hour < 0) {
            const prev = getKstParts(new Date(now.getTime() - 86_400_000));
            return {
                baseDate: `${prev.year}${prev.month}${prev.day}`,
                baseTime: '2330',
            };
        }
    }

    return {
        baseDate: `${year}${month}${day}`,
        baseTime: `${String(hour).padStart(2, '0')}30`,
    };
}

// ─────────────────────────────────────────────────────────────
// RN1 강수량 파싱
// ─────────────────────────────────────────────────────────────
function parseRainAmount(val: string): number {
    if (val === '강수없음' || val === '0') return 0;
    if (val.includes('미만')) return 0.5;
    const rangeMatch = val.match(/(\d+(?:\.\d+)?)\s*~\s*(\d+(?:\.\d+)?)/);
    if (rangeMatch) return (parseFloat(rangeMatch[1]) + parseFloat(rangeMatch[2])) / 2;
    const numMatch = val.match(/\d+(?:\.\d+)?/);
    return numMatch ? parseFloat(numMatch[0]) : 0;
}

// ─────────────────────────────────────────────────────────────
// 중간 데이터 타입
// ─────────────────────────────────────────────────────────────
interface NcstData { t1h: number; pty: number; rn1: number; wsd: number; }
interface FcstData { sky: number; lgt: number; }
interface WeatherResult {
    condition: string; sky: string;
    temperature: string; precipitation: string; windSpeed: string;
}

// ─────────────────────────────────────────────────────────────
// 실황 items 파싱 (obsrValue 사용)
// ─────────────────────────────────────────────────────────────
function parseNcst(items: any[]): NcstData {
    let t1h = 0, pty = 0, rn1 = 0, wsd = 0;
    for (const item of items) {
        const val: string = item.obsrValue;   // 실황은 obsrValue
        switch (item.category) {
            case 'T1H': t1h = parseFloat(val); break;
            case 'PTY': pty = parseInt(val, 10); break;
            case 'RN1': rn1 = parseRainAmount(val); break;
            case 'WSD': wsd = parseFloat(val); break;
        }
    }
    return { t1h, pty, rn1, wsd };
}

// ─────────────────────────────────────────────────────────────
// 예보 items → 현재와 가장 가까운 슬롯의 SKY·LGT 추출
// ─────────────────────────────────────────────────────────────
function parseFcst(items: any[]): FcstData {
    const nowMs = Date.now();

    const toUtcMs = (fcstDate: string, fcstTime: string): number => {
        const Y = +fcstDate.slice(0, 4);
        const M = +fcstDate.slice(4, 6) - 1;
        const D = +fcstDate.slice(6, 8);
        const H = +fcstTime.slice(0, 2);
        const m = +fcstTime.slice(2, 4);
        return Date.UTC(Y, M, D, H - 9, m);   // KST → UTC
    };

    const uniqueKeys = [...new Set(items.map(i => `${i.fcstDate}|${i.fcstTime}`))];
    const nearestKey = uniqueKeys.reduce((best, key) => {
        const [d, t] = key.split('|');
        const [bd, bt] = best.split('|');
        return Math.abs(toUtcMs(d, t) - nowMs) < Math.abs(toUtcMs(bd, bt) - nowMs)
            ? key : best;
    }, uniqueKeys[0]);

    const [nearestDate, nearestTime] = nearestKey.split('|');
    const slot = items.filter(i => i.fcstDate === nearestDate && i.fcstTime === nearestTime);

    let sky = 1, lgt = 0;
    for (const item of slot) {
        switch (item.category) {
            case 'SKY': sky = parseInt(item.fcstValue, 10); break;
            case 'LGT': lgt = parseFloat(item.fcstValue); break;
        }
    }
    return { sky, lgt };
}

// ─────────────────────────────────────────────────────────────
// 실황(실측) + 예보(SKY·LGT) 병합 → WeatherResult
// ─────────────────────────────────────────────────────────────
function mergeToWeather(ncst: NcstData, fcst: FcstData): WeatherResult {
    const { t1h, pty, rn1, wsd } = ncst;
    const { sky, lgt } = fcst;

    let condition: string;

    if (lgt > 0) {
        condition = 'THUNDERSTORM';                               // 낙뢰 최우선
    } else if (pty === 1 || pty === 5) {
        condition = rn1 >= 1.0 ? 'HEAVY_RAIN' : 'LIGHT_RAIN';   // 비·빗방울
    } else if (pty === 2 || pty === 6) {
        condition = 'RAIN_SNOW';                                  // 비/눈
    } else if (pty === 3 || pty === 7) {
        condition = 'SNOW';                                       // 눈
    } else if (wsd >= STRONG_WIND_MS) {
        condition = 'STRONG_WIND';                                // 강풍 (강수 없을 때)
    } else {
        condition = sky === 1 ? 'CLEAR'                           // 하늘 상태
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
// KMA API 공통 호출 유틸 (타임아웃 + 에러 처리 포함)
// ─────────────────────────────────────────────────────────────
async function fetchKmaItems(url: string): Promise<any[]> {
    const controller = new AbortController();
    const tid = setTimeout(() => controller.abort(), KMA_TIMEOUT_MS);
    try {
        const res = await fetch(url, { signal: controller.signal });
        clearTimeout(tid);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const json: any = await res.json();
        const header = json?.response?.header;
        if (header?.resultCode !== '00') {
            throw new Error(`KMA: ${header?.resultMsg} (${header?.resultCode})`);
        }
        const items = json?.response?.body?.items?.item;
        if (!Array.isArray(items) || items.length === 0) throw new Error('KMA 응답 데이터 없음');
        return items;
    } catch (err) {
        clearTimeout(tid);
        throw err;
    }
}

// ─────────────────────────────────────────────────────────────
// 단일 격자 날씨 조회 (실황 + 예보 병렬 호출 후 병합, 격자 단위 캐시)
// 캐시 키: ncstBaseTime + fcstBaseTime 둘 다 포함 → 한쪽만 갱신돼도 캐시 미스
// ─────────────────────────────────────────────────────────────
async function fetchGridWeather(
    nx: number,
    ny: number,
    serviceKey: string,
    ncstBase: { baseDate: string; baseTime: string },
    fcstBase: { baseDate: string; baseTime: string },
    cache: Cache,
    logger: Logger,
): Promise<WeatherResult> {
    const cacheKey = new Request(
        `${CACHE_KEY_BASE}/ncst-${ncstBase.baseDate}-${ncstBase.baseTime}/fcst-${fcstBase.baseTime}/${nx}/${ny}`,
        { method: 'GET' },
    );

    const cached = await cache.match(cacheKey);
    if (cached) {
        logger.log(`🚀 [Cache Hit] nx=${nx} ny=${ny}`);
        return cached.json<WeatherResult>();
    }

    const ncstUrl =
        `${KMA_BASE_URL}/getUltraSrtNcst` +
        `?serviceKey=${serviceKey}&pageNo=1&numOfRows=${KMA_NCST_NUM_OF_ROWS}&dataType=JSON` +
        `&base_date=${ncstBase.baseDate}&base_time=${ncstBase.baseTime}&nx=${nx}&ny=${ny}`;

    const fcstUrl =
        `${KMA_BASE_URL}/getUltraSrtFcst` +
        `?serviceKey=${serviceKey}&pageNo=1&numOfRows=${KMA_FCST_NUM_OF_ROWS}&dataType=JSON` +
        `&base_date=${fcstBase.baseDate}&base_time=${fcstBase.baseTime}&nx=${nx}&ny=${ny}`;

    try {
        // 실황·예보 병렬 호출
        const [ncstItems, fcstItems] = await Promise.all([
            fetchKmaItems(ncstUrl),
            fetchKmaItems(fcstUrl),
        ]);

        const weather = mergeToWeather(parseNcst(ncstItems), parseFcst(fcstItems));

        await cache.put(cacheKey, new Response(JSON.stringify(weather), {
            headers: {
                'Content-Type': 'application/json',
                'Cache-Control': `s-maxage=${CACHE_TTL_SECONDS}`,
            },
        }));
        logger.log(`📡 [Cache Miss] nx=${nx} ny=${ny} → 실황+예보 병합 완료`);

        return weather;

    } catch (err: any) {
        logger.warn(`⚠️ 격자(${nx},${ny}) 조회 실패: ${err.message}`);
        return { condition: 'UNKNOWN', sky: 'N/A', temperature: 'N/A', precipitation: 'N/A', windSpeed: 'N/A' };
    }
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
        const ncstBase = getNcstBaseDateTime();   // 실황 base time
        const fcstBase = getFcstBaseDateTime();   // 예보 base time
        const cache = caches.default;

        const uniqueGrids = [...new Map(
            targetStadiums.map(s => [`${s.nx}:${s.ny}`, { nx: s.nx, ny: s.ny }]),
        ).values()];

        const gridTasks = uniqueGrids.map(
            ({ nx, ny }) => () =>
                fetchGridWeather(nx, ny, serviceKey, ncstBase, fcstBase, cache, logger)
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
            {
                success: true,
                count: data.length,
                ncstBaseDate: ncstBase.baseDate,
                ncstBaseTime: ncstBase.baseTime,
                fcstBaseDate: fcstBase.baseDate,
                fcstBaseTime: fcstBase.baseTime,
                data,
            },
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