package com.yagubogu.data.dto.request.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    @SerialName("idToken")
    val idToken: String, // Google SDK에서 발급 받은 ID 토큰
    // TODO Provider 상태 분리 필요
    @SerialName("provider")
    val provider: String = "GOOGLE",
)
