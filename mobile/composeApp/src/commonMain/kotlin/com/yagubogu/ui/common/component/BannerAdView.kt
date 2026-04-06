package com.yagubogu.ui.common.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.theme.White

enum class AdSize(val widthDp: Int, val heightDp: Int) {
    BANNER(320, 50),
    LARGE_BANNER(320, 100),
    MEDIUM_RECTANGLE(300, 250),
}

@Composable
expect fun BannerAdView(
    adUnitId: String,
    adSize: AdSize = AdSize.BANNER,
    modifier: Modifier = Modifier,
)

@Composable
fun BannerAd(
    adUnitId: String,
    modifier: Modifier = Modifier,
    height: Dp = adSize.heightDp.dp,
    backgroundColor: Color = White,
    adSize: AdSize = AdSize.BANNER,
) {
    val cornerRadius = 12.dp

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        val currentWidth = maxWidth

        val finalShape =
            if (currentWidth >= adSize.widthDp.dp + cornerRadius * 2) {
                RoundedCornerShape(cornerRadius)
            } else {
                RectangleShape
            }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(height)
                    .background(backgroundColor, finalShape)
                    .clip(finalShape),
            contentAlignment = Alignment.Center,
        ) {
            BannerAdView(
                adUnitId = adUnitId,
                adSize = adSize,
            )
        }
    }
}
