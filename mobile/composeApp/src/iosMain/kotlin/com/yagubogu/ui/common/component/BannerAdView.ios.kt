package com.yagubogu.ui.common.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi

actual enum class AdSize {
    BANNER,
    LARGE_BANNER,
    MEDIUM_RECTANGLE,
    ;

    val heightDp: Int
        get() =
            when (this) {
                BANNER -> 50
                LARGE_BANNER -> 100
                MEDIUM_RECTANGLE -> 250
            }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun BannerAdView(
    adUnitId: String,
    adSize: AdSize,
    modifier: Modifier,
) {
    UIKitView(
        factory = {
            BannerAdProvider.create?.invoke(adUnitId, adSize.heightDp)
                ?: platform.UIKit.UIView()
        },
        modifier =
            modifier
                .fillMaxWidth()
                .height(adSize.heightDp.dp),
        update = {},
        onRelease = {},
        properties =
            UIKitInteropProperties(
                isInteractive = true,
                isNativeAccessibilityEnabled = true,
            ),
    )
}
