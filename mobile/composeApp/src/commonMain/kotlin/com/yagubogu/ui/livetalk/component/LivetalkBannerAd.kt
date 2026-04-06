package com.yagubogu.ui.livetalk.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.common.AdUnitIds
import com.yagubogu.ui.common.component.AdSize
import com.yagubogu.ui.common.component.BannerAd
import com.yagubogu.ui.theme.Gray100

@Composable
fun LivetalkBannerAd(modifier: Modifier = Modifier) {
    BannerAd(
        adUnitId = AdUnitIds.livetalkBanner,
        adSize = AdSize.LARGE_BANNER,
        height = 142.dp,
        borderStroke = BorderStroke(1.dp, Gray100),
        modifier = modifier,
    )
}
