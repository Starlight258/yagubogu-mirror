package com.yagubogu.data.dto.response.appconfig

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MaintenanceResponse(
    @SerialName("is_show")
    val isShow: Boolean, // 점검중 팝업 표시 여부
    @SerialName("id")
    val id: Int, // 점검 메시지 id
    @SerialName("emoji")
    val emoji: String?, // 다이얼로그 이모지
    @SerialName("title")
    val title: String?, // 다이얼로그 제목
    @SerialName("message")
    val message: String?, // 다이얼로그 메시지
    @SerialName("skippable_days")
    val skippableDays: Int?, // 스킵 가능한 일 수
)
