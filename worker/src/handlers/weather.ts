import type { Env } from '../types';
import { jsonResponse } from '../utils/response';
import { authenticate } from '../middleware/auth';
import { getCorsHeaders } from '../middleware/cors';
import { Logger } from '../utils/logger';

export async function handleWeather(request: Request, env: Env): Promise<Response> {
    const logger = new Logger(env);
    const corsHeaders = getCorsHeaders(request);

    // 1. JWT 인증 절차 수행
    const authResult = await authenticate(request, env);
    if ('error' in authResult) {
        return jsonResponse(
            { error: authResult.error, hint: authResult.hint },
            authResult.status || 401,
            corsHeaders
        );
    }

    const userId = authResult.userId;
    logger.log(`🌤️ 날씨 API 요청 접수 (User: ${userId})`);

    const url = new URL(request.url);
    const namesParam = url.searchParams.get('names'); // ex: ?names=잠실 야구장,고척 스카이돔

    try {
        const stadiums = [
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

        let mockWeatherData = stadiums.map(stadium => {
            const conditions = ["맑음", "흐림", "구름조금", "비", "소나기"];
            const randomCondition = conditions[Math.floor(Math.random() * conditions.length)];
            const randomTemp = Math.floor(Math.random() * 15) + 15; // 15°C ~ 29°C

            return {
                ...stadium,
                weather: {
                    condition: randomCondition,
                    temperature: `${randomTemp}°C`,
                    humidity: `${Math.floor(Math.random() * 40) + 40}%`
                }
            };
        });

        // names 파라미터가 쉼표(,)로 구분되어 들어오면 해당 경기장 이름들만 필터링
        if (namesParam) {
            // 이름들에 앞뒤 공백 제거 후 배열 화
            const requestedNames = namesParam.split(',').map(n => n.trim());
            mockWeatherData = mockWeatherData.filter(s => requestedNames.includes(s.name));
            // 존재하지 않는 경기장은 필터에서 알아서 걸러지므로,
            // 매칭된 응답만 포함되며 요청에 아무것도 매칭 안 되면 빈 배열([]) 반환됨
        }

        return jsonResponse(
            { success: true, count: mockWeatherData.length, data: mockWeatherData },
            200,
            corsHeaders
        );

    } catch (error: any) {
        logger.error('❌ 날씨 요청 처리 중 오류 발생:', error);
        return jsonResponse(
            { error: '서버 내부 오류가 발생했습니다.' },
            500,
            corsHeaders
        );
    }
}
