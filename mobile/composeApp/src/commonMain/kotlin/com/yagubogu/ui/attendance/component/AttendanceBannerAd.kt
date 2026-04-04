package com.yagubogu.ui.attendance.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.common.AdUnitIds
import com.yagubogu.ui.common.component.AdSize
import com.yagubogu.ui.common.component.BannerAdView
import com.yagubogu.ui.theme.White

private val adShape = RoundedCornerShape(12.dp)

@Composable
fun AttendanceBannerAd(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(White, adShape)
                .clip(adShape)
                .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        BannerAdView(
            adUnitId = AdUnitIds.attendanceCalendarBanner,
            adSize = AdSize.BANNER,
        )
    }
}
