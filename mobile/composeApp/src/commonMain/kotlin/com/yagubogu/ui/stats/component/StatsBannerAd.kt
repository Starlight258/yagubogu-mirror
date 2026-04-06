package com.yagubogu.ui.stats.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.common.component.AdSize
import com.yagubogu.ui.common.component.BannerAd

@Composable
fun StatsBannerAd(
    adUnitId: String,
    modifier: Modifier = Modifier,
) {
    BannerAd(
        adUnitId = adUnitId,
        adSize = AdSize.BANNER,
        height = 50.dp,
        modifier = modifier,
    )
}
