package com.yagubogu.ui.common.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

@Composable
fun BannerAd(
    adUnitId: String,
    modifier: Modifier = Modifier,
    height: Dp = 50.dp,
    backgroundColor: Color = White,
    adSize: AdSize = AdSize.BANNER,
    borderStroke: BorderStroke? = null,
) {
    val cornerRadius = 12.dp

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        val currentWidth = maxWidth

        val minAdWidth =
            when (adSize) {
                AdSize.BANNER, AdSize.LARGE_BANNER -> 320.dp
                AdSize.MEDIUM_RECTANGLE -> 300.dp
                else -> 300.dp
            }

        val finalShape =
            if (currentWidth >= minAdWidth + cornerRadius * 2) {
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
                    .then(
                        if (borderStroke != null) {
                            Modifier.border(borderStroke, finalShape)
                        } else {
                            Modifier
                        },
                    ).clip(finalShape),
            contentAlignment = Alignment.Center,
        ) {
            BannerAdView(
                adUnitId = adUnitId,
                adSize = adSize,
            )
        }
    }
}
