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

    try {
        // TODO: 실제 날씨 데이터 처리 및 외부 API 호출 로직 구현 위치
        // 현재는 뼈대 생성을 위한 Mock 응답 반환
        
        const mockWeatherData = {
            userId: userId,
            location: "Seoul",
            temperature: "22°C",
            condition: "Sunny",
            message: "날씨 API 엔드포인트가 정상 동작 중입니다."
        };

        return jsonResponse(
            { success: true, data: mockWeatherData },
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
