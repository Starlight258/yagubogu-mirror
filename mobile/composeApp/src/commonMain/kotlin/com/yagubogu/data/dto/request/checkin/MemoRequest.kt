package com.yagubogu.data.dto.request.checkin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MemoRequest(
    @SerialName("content")
    val content: String,
)
