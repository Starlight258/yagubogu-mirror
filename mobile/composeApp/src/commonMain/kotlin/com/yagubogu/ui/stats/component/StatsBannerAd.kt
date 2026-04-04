package com.yagubogu.ui.stats.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.common.component.AdSize
import com.yagubogu.ui.common.component.BannerAdView
import com.yagubogu.ui.theme.White

private val adShape = RoundedCornerShape(12.dp)

@Composable
fun StatsBannerAd(
    adUnitId: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(White, adShape)
                .clip(adShape),
    ) {
        BannerAdView(
            adUnitId = adUnitId,
            adSize = AdSize.BANNER,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
