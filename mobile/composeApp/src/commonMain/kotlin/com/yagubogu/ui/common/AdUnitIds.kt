package com.yagubogu.ui.common

// TODO 실제 배포 전 각 값을 BuildKonfig에서 읽도록 교체
expect object AdUnitIds {
    val homeBanner: String
    val livetalkBanner: String
    val statsBanner: String
    val attendanceBanner: String
    val exitDialogBanner: String
}
