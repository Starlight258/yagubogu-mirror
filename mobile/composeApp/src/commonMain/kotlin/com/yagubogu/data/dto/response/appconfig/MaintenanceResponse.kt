package com.yagubogu.data.dto.response.appconfig

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MaintenanceResponse(
    @SerialName("is_show")
    override val isShow: Boolean, // 점검중 팝업 표시 여부
    @SerialName("id")
    override val id: Int, // 점검 메시지 id
    @SerialName("emoji")
    override val emoji: String?, // 다이얼로그 이모지
    @SerialName("title")
    override val title: String?, // 다이얼로그 제목
    @SerialName("message")
    override val message: String?, // 다이얼로그 메시지
    @SerialName("skippable_days")
    override val skippableDays: Int?, // 스킵 가능한 일 수
    @SerialName("is_login_block")
    val isLoginBlock: Boolean, // 로그인 차단 여부
) : AppConfigPopupDialogResponse
