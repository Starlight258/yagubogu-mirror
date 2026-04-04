package com.yagubogu.ui.common.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

expect enum class AdSize {
    BANNER,
    LARGE_BANNER,
    MEDIUM_RECTANGLE,
}

@Composable
expect fun BannerAdView(
    adUnitId: String,
    adSize: AdSize = AdSize.BANNER,
    modifier: Modifier = Modifier,
)
