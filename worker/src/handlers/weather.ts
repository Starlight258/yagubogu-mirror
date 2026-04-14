import type { Env } from '../types';
import { jsonResponse } from '../utils/response';
import { authenticate } from '../middleware/auth';
import { getCorsHeaders } from '../middleware/cors';
import { Logger } from '../utils/logger';
import { latLngToGrid } from '../utils/kmaCoords';

// KST 시간 계산 (Intl.DateTimeFormat 기반)
function getKmaBaseDateTime() {
    const d = new Date();
    // Intl을 사용하여 한국 시간 조각들 추출
    const formatter = new Intl.DateTimeFormat('ko-KR', {
        timeZone: 'Asia/Seoul',
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
    });
    
    const parts = formatter.formatToParts(d);
    const map = new Map(parts.map(p => [p.type, p.value]));
    
    let year = map.get('year');
    let month = map.get('month');
    let day = map.get('day');
    let hour = parseInt(map.get('hour') || '0', 10);
    let minute = parseInt(map.get('minute') || '0', 10);

    // 기상청 초단기실황/예보는 매시 45분 업데이트
    if (minute < 45) {
        hour -= 1;
        if (hour < 0) {
            hour = 23;
            const prev = new Date(d.getTime() - 24 * 60 * 60 * 1000);
            const prevParts = formatter.formatToParts(prev);
            const prevMap = new Map(prevParts.map(p => [p.type, p.value]));
            year = prevMap.get('year');
            month = prevMap.get('month');
            day = prevMap.get('day');
        }
    }

    const baseDate = `${year}${month}${day}`;
    const baseTime = `${String(hour).padStart(2, '0')}30`;
    return { baseDate, baseTime };
}

// 강수량 파싱 (정규식 기반)
function parseRainAmount(val: string): number {
    if (val === '강수없음' || val === '0') return 0;
    const match = val.match(/\d+(\.\d+)?/);
    return match ? parseFloat(match[0]) : 0;
}

function parseKmaToEnum(items: any[]): { condition: string, temperature: string, precipitation: string } {
    let pty = 0, sky = 1, rn1 = 0, t1h = 0, lgt = 0;
    
    for (const item of items) {
        const val = item.fcstValue;
        if (item.category === 'PTY') pty = parseInt(val, 10);
        if (item.category === 'SKY') sky = parseInt(val, 10);
        if (item.category === 'T1H') t1h = parseFloat(val);
        if (item.category === 'LGT') lgt = parseFloat(val);
        if (item.category === 'RN1') rn1 = parseRainAmount(val);
    }

    let condition = "CLEAR";
    
    if (lgt > 0) {
        condition = "THUNDERSTORM";
    } else if (pty === 1 || pty === 5) {
        if (rn1 >= 5.0) condition = "HEAVY_RAIN";
        else if (rn1 >= 1.0) condition = "RAIN";
        else condition = "LIGHT_RAIN";
    } else if (pty === 2 || pty === 6) {
        condition = "RAIN_SNOW";
    } else if (pty === 3 || pty === 7) {
        condition = "SNOW";
    } else if (sky === 4) {
        condition = "CLOUDY";
    } else if (sky === 3) {
        condition = "PARTLY_CLOUDY";
    } else {
        condition = "CLEAR";
    }

    return {
        condition,
        temperature: `${t1h}°C`,
        precipitation: `${rn1}mm`
    };
}

export async function handleWeather(request: Request, env: Env): Promise<Response> {
    const logger = new Logger(env);
    const corsHeaders = getCorsHeaders(request);

    const authResult = await authenticate(request, env);
    if ('error' in authResult) {
        return jsonResponse({ error: authResult.error, hint: authResult.hint }, authResult.status || 401, corsHeaders);
    }

    const url = new URL(request.url);
    const namesParam = url.searchParams.get('names');

    // Cache API 전략: 20분 캐싱
    const cacheKey = new Request(url.toString(), { method: 'GET' });
    const cache = caches.default;
    let response = await cache.match(cacheKey);
    
    if (response) {
        logger.log('🚀 [Cache Hit] 20분 메모리 캐시 사용');
        return response;
    }

    const ALL_STADIUMS = [
        { id: 1, name: "광주 기아 챔피언스필드", lat: 35.168139, lng: 126.889111 },
        { id: 2, name: "잠실 야구장", lat: 37.512150, lng: 127.071976 },
        { id: 3, name: "고척 스카이돔", lat: 37.498222, lng: 126.867250 },
        { id: 4, name: "수원 KT 위즈파크", lat: 37.299759, lng: 127.009781 },
        { id: 5, name: "대구 삼성 라이온즈파크", lat: 35.841111, lng: 128.681667 },
        { id: 6, name: "사직야구장", lat: 35.194077, lng: 129.061584 },
        { id: 7, name: "인천 SSG 랜더스필드", lat: 37.436778, lng: 126.693306 },
        { id: 8, name: "창원 NC 파크", lat: 35.222754, lng: 128.582251 },
        { id: 9, name: "대전 한화생명 볼파크", lat: 36.316589, lng: 127.431211 },
        { id: 10, name: "울산 문수 야구장", lat: 35.532334, lng: 129.265575 },
        { id: 11, name: "월명종합경기장 야구장", lat: 35.966360, lng: 126.748161 },
        { id: 12, name: "청주 야구장", lat: 36.638840, lng: 127.470149 },
        { id: 13, name: "포항 야구장", lat: 36.008273, lng: 129.359410 },
        { id: 14, name: "한화생명 이글스파크", lat: 36.317178, lng: 127.429167 },
        { id: 15, name: "대구시민운동장 야구장", lat: 35.881162, lng: 128.586371 },
        { id: 16, name: "무등 야구장", lat: 35.169165, lng: 126.887245 },
        { id: 17, name: "마산 야구장", lat: 35.220855, lng: 128.581050 },
        { id: 18, name: "숭의 야구장", lat: 37.466591, lng: 126.643239 },
        { id: 19, name: "삼성 라이온즈 볼파크", lat: 35.864844, lng: 128.805667 },
    ];

    let targetStadiums = ALL_STADIUMS;
    if (namesParam) {
        const requestedNames = namesParam.split(',').map(n => n.trim());
        targetStadiums = ALL_STADIUMS.filter(s => requestedNames.includes(s.name));
    }

    try {
        if (!env.KMA_API_KEY) throw new Error("KMA_API_KEY missing");
        
        const serviceKey = encodeURIComponent(env.KMA_API_KEY);
        const { baseDate, baseTime } = getKmaBaseDateTime();

        const results = await Promise.all(targetStadiums.map(async (stadium) => {
            const { nx, ny } = latLngToGrid(stadium.lat, stadium.lng);
            const apiUrl = `https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst?serviceKey=${serviceKey}&pageNo=1&numOfRows=100&dataType=JSON&base_date=${baseDate}&base_time=${baseTime}&nx=${nx}&ny=${ny}`;
            
            try {
                // AbortController로 3.5초 타임아웃 적용
                const controller = new AbortController();
                const timeoutId = setTimeout(() => controller.abort(), 3500);
                
                const kmaRes = await fetch(apiUrl, { signal: controller.signal });
                clearTimeout(timeoutId);

                if (!kmaRes.ok) throw new Error(`HTTP ${kmaRes.status}`);
                
                const json: any = await kmaRes.json();
                const header = json?.response?.header;
                
                if (header?.resultCode !== '00') {
                    throw new Error(`KMA Error: ${header?.resultMsg} (${header?.resultCode})`);
                }

                const items = json?.response?.body?.items?.item;
                if (!items || !Array.isArray(items)) throw new Error("Empty KMA body");

                // ✅ [수정] 모든 시간대 데이터가 섞여 있으므로, 가장 빠른 예보 시각(fcstTime) 데이터만 필터링
                const minFcstTime = items.reduce((min, item) => 
                    parseInt(item.fcstTime, 10) < min ? parseInt(item.fcstTime, 10) : min, 
                    9999
                ).toString().padStart(4, '0');

                const nearestItems = items.filter(item => item.fcstTime === minFcstTime);

                return {
                    id: stadium.id,
                    name: stadium.name,
                    lat: stadium.lat,
                    lng: stadium.lng,
                    weather: parseKmaToEnum(nearestItems)
                };
            } catch (err: any) {
                logger.warn(`⚠️ ${stadium.name} 조회 실패: ${err.message}`);
                return {
                    ...stadium,
                    weather: { condition: "CLEAR", temperature: "N/A", precipitation: "0mm" }
                };
            }
        }));

        const finalResponse = jsonResponse(
            { success: true, count: results.length, data: results },
            200,
            { ...corsHeaders, 'Cache-Control': 's-maxage=1200' } // 20분 캐싱
        );
        
        cache.put(cacheKey, finalResponse.clone());
        logger.log(`📡 [Cache Miss] 기상청 실제 호출 완료 (${results.length}곳). 20분 캐시 생성.`);
        
        return finalResponse;

    } catch (error: any) {
        logger.error('❌ 서버 내부 오류:', error.message);
        return jsonResponse({ error: '날씨 데이터를 가져오지 못했습니다.', message: error.message }, 500, corsHeaders);
    }
}
