package com.yagubogu.ui.attendance.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.common.AdUnitIds
import com.yagubogu.ui.common.component.AdSize
import com.yagubogu.ui.common.component.BannerAd

@Composable
fun AttendanceBannerAd(modifier: Modifier = Modifier) {
    BannerAd(
        adUnitId = AdUnitIds.attendanceCalendarBanner,
        adSize = AdSize.BANNER,
        height = 100.dp,
        modifier = modifier,
    )
}
