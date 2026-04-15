import type { Env } from '../types';
import { jsonResponse } from '../utils/response';
import { authenticate } from '../middleware/auth';
import { getCorsHeaders } from '../middleware/cors';
import { Logger } from '../utils/logger';
import { latLngToGrid } from '../utils/kmaCoords';

// ─────────────────────────────────────────────────────────────
// 전역 상수
// ─────────────────────────────────────────────────────────────
const CACHE_TTL_SECONDS = 1200; // 20분
const KMA_TIMEOUT_MS    = 3500; // API 타임아웃 3.5초
const KMA_NUM_OF_ROWS   = 60;   // 초단기예보: 9카테고리 × 6시간 = 54행
const CONCURRENT_LIMIT  = 5;    // 동시 KMA API 요청 수 제한

// ─────────────────────────────────────────────────────────────
// 경기장 목록 (모듈 최상단에 고정, 요청마다 재생성 방지)
// ─────────────────────────────────────────────────────────────
const ALL_STADIUMS = [
    { id: 1,  name: '광주 기아 챔피언스필드',  lat: 35.168139, lng: 126.889111 },
    { id: 2,  name: '잠실 야구장',            lat: 37.512150, lng: 127.071976 },
    { id: 3,  name: '고척 스카이돔',           lat: 37.498222, lng: 126.867250 },
    { id: 4,  name: '수원 KT 위즈파크',        lat: 37.299759, lng: 127.009781 },
    { id: 5,  name: '대구 삼성 라이온즈파크',   lat: 35.841111, lng: 128.681667 },
    { id: 6,  name: '사직야구장',             lat: 35.194077, lng: 129.061584 },
    { id: 7,  name: '인천 SSG 랜더스필드',     lat: 37.436778, lng: 126.693306 },
    { id: 8,  name: '창원 NC 파크',           lat: 35.222754, lng: 128.582251 },
    { id: 9,  name: '대전 한화생명 볼파크',     lat: 36.316589, lng: 127.431211 },
    { id: 10, name: '울산 문수 야구장',         lat: 35.532334, lng: 129.265575 },
    { id: 11, name: '월명종합경기장 야구장',     lat: 35.966360, lng: 126.748161 },
    { id: 12, name: '청주 야구장',            lat: 36.638840, lng: 127.470149 },
    { id: 13, name: '포항 야구장',            lat: 36.008273, lng: 129.359410 },
    { id: 14, name: '한화생명 이글스파크',      lat: 36.317178, lng: 127.429167 },
    { id: 15, name: '대구시민운동장 야구장',     lat: 35.881162, lng: 128.586371 },
    { id: 16, name: '무등 야구장',            lat: 35.169165, lng: 126.887245 },
    { id: 17, name: '마산 야구장',            lat: 35.220855, lng: 128.581050 },
    { id: 18, name: '숭의 야구장',            lat: 37.466591, lng: 126.643239 },
    { id: 19, name: '삼성 라이온즈 볼파크',    lat: 35.864844, lng: 128.805667 },
] as const;

type Stadium = typeof ALL_STADIUMS[number];

// ─────────────────────────────────────────────────────────────
// KST 기반 초단기예보 Base Date/Time 계산
// - 매시 30분 생성, 45분 이후 API 제공
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
    let year  = map.get('year')!;
    let month = map.get('month')!;
    let day   = map.get('day')!;
    let hour  = parseInt(map.get('hour')   ?? '0', 10);
    const min = parseInt(map.get('minute') ?? '0', 10);

    if (min < 45) {
        hour -= 1;
        if (hour < 0) {
            // 자정 이전 → 전날 23시로 이동
            hour = 23;
            const prevDay = new Date(now.getTime() - 24 * 60 * 60 * 1000);
            const prevMap = new Map(formatter.formatToParts(prevDay).map(p => [p.type, p.value]));
            year  = prevMap.get('year')!;
            month = prevMap.get('month')!;
            day   = prevMap.get('day')!;
        }
    }

    return {
        baseDate: `${year}${month}${day}`,
        baseTime: `${String(hour).padStart(2, '0')}30`,
    };
}

// ─────────────────────────────────────────────────────────────
// RN1 강수량 파싱
// 기상청 응답 형태: "강수없음" | "1.0mm 미만" | "1.0~4.9mm" | "30.0mm 이상" | "2.5"
// ─────────────────────────────────────────────────────────────
function parseRainAmount(val: string): number {
    if (val === '강수없음' || val === '0') return 0;
    if (val.includes('미만')) return 0.5; // "1.0mm 미만" → 0~1 사이 근사값

    // "1.0~4.9mm" 범위값 → 중간값 사용
    const rangeMatch = val.match(/(\d+(?:\.\d+)?)\s*~\s*(\d+(?:\.\d+)?)/);
    if (rangeMatch) {
        return (parseFloat(rangeMatch[1]) + parseFloat(rangeMatch[2])) / 2;
    }

    // "30.0mm 이상" 또는 단순 숫자
    const numMatch = val.match(/\d+(?:\.\d+)?/);
    return numMatch ? parseFloat(numMatch[0]) : 0;
}

// ─────────────────────────────────────────────────────────────
// KMA items → 날씨 Enum 변환
//
// ⚠️ 주의: getUltraSrtFcst(초단기예보) 제공 카테고리
//    T1H · RN1 · UUU · VVV · REH · PTY · LGT · VEC · WSD
//    SKY(하늘상태)는 getVilageFcst(단기예보)에만 존재.
//    따라서 CLOUDY / PARTLY_CLOUDY는 이 API로 판별 불가.
//    구름 상태까지 필요하면 getVilageFcst 병행 호출 필요.
// ─────────────────────────────────────────────────────────────
interface WeatherResult {
    condition: string;
    temperature: string;
    precipitation: string;
}

function parseKmaToEnum(items: any[]): WeatherResult {
    let pty = 0, t1h = 0, lgt = 0, rn1 = 0;

    for (const item of items) {
        const val: string = item.fcstValue;
        switch (item.category) {
            case 'PTY': pty = parseInt(val, 10);   break;
            case 'T1H': t1h = parseFloat(val);     break;
            case 'LGT': lgt = parseFloat(val);     break;
            case 'RN1': rn1 = parseRainAmount(val); break;
        }
    }

    let condition: string;

    if (lgt > 0) {
        condition = 'THUNDERSTORM';
    } else if (pty === 1 || pty === 5) {        // 비 | 빗방울
        condition = rn1 >= 5.0 ? 'HEAVY_RAIN'
                  : rn1 >= 1.0 ? 'RAIN'
                  : 'LIGHT_RAIN';
    } else if (pty === 2 || pty === 6) {        // 비/눈 | 진눈깨비
        condition = 'RAIN_SNOW';
    } else if (pty === 3 || pty === 7) {        // 눈 | 눈날림
        condition = 'SNOW';
    } else {
        condition = 'CLEAR';                    // SKY 없으므로 맑음/구름 구분 불가
    }

    return {
        condition,
        temperature:   `${t1h}°C`,
        precipitation: `${rn1}mm`,
    };
}

// ─────────────────────────────────────────────────────────────
// 동시 요청 수 제한 유틸 (p-limit 대신 직접 구현)
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
// 단일 경기장 날씨 조회
// ─────────────────────────────────────────────────────────────
async function fetchStadiumWeather(
    stadium: Stadium,
    serviceKey: string,
    baseDate: string,
    baseTime: string,
    logger: Logger,
) {
    const { nx, ny } = latLngToGrid(stadium.lat, stadium.lng);
    const apiUrl =
        `https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst` +
        `?serviceKey=${serviceKey}` +
        `&pageNo=1&numOfRows=${KMA_NUM_OF_ROWS}&dataType=JSON` +
        `&base_date=${baseDate}&base_time=${baseTime}` +
        `&nx=${nx}&ny=${ny}`;

    try {
        const controller = new AbortController();
        const tid = setTimeout(() => controller.abort(), KMA_TIMEOUT_MS);
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

        // 현재 시각에 가장 근접한 예보 시각의 데이터만 추출
        // fcstDate + fcstTime 복합 문자열로 비교 (자정 경계 오류 방지)
        const minKey = items.reduce((min, item) => {
            const key = `${item.fcstDate}${item.fcstTime}`;
            return key < min ? key : min;
        }, `${items[0].fcstDate}${items[0].fcstTime}`);

        const nearestItems = items.filter(
            item => `${item.fcstDate}${item.fcstTime}` === minKey,
        );

        return {
            id:      stadium.id,
            name:    stadium.name,
            lat:     stadium.lat,
            lng:     stadium.lng,
            weather: parseKmaToEnum(nearestItems),
        };
    } catch (err: any) {
        logger.warn(`⚠️ ${stadium.name} 조회 실패: ${err.message}`);
        // 오류 시 'UNKNOWN' 반환 — CLEAR로 오해하지 않도록
        return {
            id:      stadium.id,
            name:    stadium.name,
            lat:     stadium.lat,
            lng:     stadium.lng,
            weather: { condition: 'UNKNOWN', temperature: 'N/A', precipitation: 'N/A' },
        };
    }
}

// ─────────────────────────────────────────────────────────────
// 메인 핸들러
// ─────────────────────────────────────────────────────────────
export async function handleWeather(request: Request, env: Env): Promise<Response> {
    const logger      = new Logger(env);
    const corsHeaders = getCorsHeaders(request);

    // 인증
    const authResult = await authenticate(request, env);
    if ('error' in authResult) {
        return jsonResponse(
            { error: authResult.error, hint: authResult.hint },
            authResult.status ?? 401,
            corsHeaders,
        );
    }

    const url        = new URL(request.url);
    const namesParam = url.searchParams.get('names');

    // ── Cache API: URL 기반 공유 캐시 (Authorization 무관) ──────
    const cacheKey = new Request(url.toString(), { method: 'GET' });
    const cache    = caches.default;
    const cached   = await cache.match(cacheKey);
    if (cached) {
        logger.log('🚀 [Cache Hit] 20분 캐시 사용');
        return cached;
    }

    // 조회 대상 경기장 필터링
    const targetStadiums: Stadium[] = namesParam
        ? (() => {
            const names = namesParam.split(',').map(n => n.trim());
            return ALL_STADIUMS.filter(s => names.includes(s.name));
        })()
        : [...ALL_STADIUMS];

    if (targetStadiums.length === 0) {
        return jsonResponse({ error: '일치하는 경기장이 없습니다.' }, 400, corsHeaders);
    }

    try {
        if (!env.KMA_API_KEY) throw new Error('KMA_API_KEY missing');

        const serviceKey           = encodeURIComponent(env.KMA_API_KEY);
        const { baseDate, baseTime } = getKmaBaseDateTime();

        // 동시 요청 수 제한 (CONCURRENT_LIMIT = 5)
        const tasks = targetStadiums.map(
            stadium => () => fetchStadiumWeather(stadium, serviceKey, baseDate, baseTime, logger),
        );
        const results = await batchedAll(tasks, CONCURRENT_LIMIT);

        const finalResponse = jsonResponse(
            { success: true, count: results.length, data: results },
            200,
            { ...corsHeaders, 'Cache-Control': `s-maxage=${CACHE_TTL_SECONDS}` },
        );

        // await로 캐시 저장 완료를 보장 (미완료 시 Worker 종료로 소실 방지)
        await cache.put(cacheKey, finalResponse.clone());
        logger.log(`📡 [Cache Miss] 기상청 API 호출 완료 (${results.length}곳). ${CACHE_TTL_SECONDS / 60}분 캐시 생성.`);

        return finalResponse;

    } catch (error: any) {
        logger.error('❌ 서버 내부 오류:', error.message);
        return jsonResponse(
            { error: '날씨 데이터를 가져오지 못했습니다.', message: error.message },
            500,
            corsHeaders,
        );
    }
}