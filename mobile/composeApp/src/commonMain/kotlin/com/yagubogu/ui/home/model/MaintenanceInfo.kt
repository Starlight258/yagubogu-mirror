package com.yagubogu.ui.home.model

data class MaintenanceInfo(
    val id: Int,
    val remoteIsShow: Boolean,
    val shouldShowPopup: Boolean,
    val emoji: String?,
    val title: String?,
    val message: String?,
    val skippableDays: Int?,
    val isLoginBlock: Boolean,
)
