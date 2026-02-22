package com.yagubogu.data.util

import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.io.IOException
import kotlin.coroutines.cancellation.CancellationException

suspend inline fun <T> safeApiCall(crossinline apiCall: suspend () -> T): Result<T> =
    runCatching {
        apiCall()
    }.fold(
        onSuccess = {
            Result.success(it)
        },
        onFailure = { e: Throwable ->
            if (e is CancellationException) throw e

            val exception =
                when (e) {
                    // HTTP 응답 에러 처리 (3xx, 4xx, 5xx)
                    is ResponseException -> {
                        val status = e.response.status
                        val errorBody = runCatching { e.response.bodyAsText() }.getOrNull()
                        val message = "HTTP ${status.value}: ${status.description}"

                        when (status) {
                            HttpStatusCode.BadRequest -> ApiException.BadRequest(message, errorBody)
                            HttpStatusCode.Unauthorized -> ApiException.Unauthorized(message, errorBody)
                            HttpStatusCode.Forbidden -> ApiException.Forbidden(message, errorBody)
                            HttpStatusCode.NotFound -> ApiException.NotFound(message, errorBody)
                            HttpStatusCode.Conflict -> ApiException.Conflict(message, errorBody)
                            HttpStatusCode.UnprocessableEntity -> ApiException.UnprocessableEntity(message, errorBody)
                            else ->
                                when {
                                    status.value >= 500 -> ApiException.ServerError(status.value, message, errorBody)
                                    else -> ApiException.Unknown(message, e)
                                }
                        }
                    }
                    // 네트워크 연결 에러 처리 (IOException, Timeout 등)
                    is IOException -> ApiException.NetworkError("네트워크 연결 실패: ${e.message}", e)

                    // 기타 정의되지 않은 에러
                    else -> ApiException.Unknown(e.message, e)
                }

            Result.failure(exception)
        },
    )
