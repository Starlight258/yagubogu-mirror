package com.yagubogu.data.util

sealed class ApiException(
    val statusCode: Int,
    override val message: String?,
    val errorBody: String? = null,
) : Exception(message) {
    // 400: 잘못된 요청 (파라미터 오류 등)
    class BadRequest(
        message: String?,
        body: String?,
    ) : ApiException(400, message, body)

    // 401: 인증 실패 (토큰 만료 등)
    class Unauthorized(
        message: String?,
        body: String?,
    ) : ApiException(401, message, body)

    // 403: 권한 없음
    class Forbidden(
        message: String?,
        body: String?,
    ) : ApiException(403, message, body)

    // 404: 리소스를 찾을 수 없음
    class NotFound(
        message: String?,
        body: String?,
    ) : ApiException(404, message, body)

    // 409: 리소스 충돌 (중복 닉네임 등)
    class Conflict(
        message: String?,
        body: String?,
    ) : ApiException(409, message, body)

    // 422: 유효성 검사 실패
    class UnprocessableEntity(
        message: String?,
        body: String?,
    ) : ApiException(422, message, body)

    // 5xx: 서버 내부 에러
    class ServerError(
        statusCode: Int,
        message: String?,
        body: String?,
    ) : ApiException(statusCode, message, body)

    // 기타 네트워크 에러 (Timeout, No Internet 등)
    class NetworkError(
        message: String?,
        cause: Throwable,
    ) : ApiException(0, message) {
        init {
            initCause(cause)
        }
    }

    // 정의되지 않은 기타 에러
    class Unknown(
        message: String?,
        cause: Throwable? = null,
    ) : ApiException(-1, message) {
        init {
            cause?.let { initCause(it) }
        }
    }
}
