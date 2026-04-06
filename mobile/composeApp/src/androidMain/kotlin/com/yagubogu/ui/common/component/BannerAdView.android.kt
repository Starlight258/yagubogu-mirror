package com.yagubogu.ui.common.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.AdSize as GmsAdSize

private fun AdSize.toGmsAdSize(): GmsAdSize =
    when (this) {
        AdSize.BANNER -> GmsAdSize.BANNER
        AdSize.LARGE_BANNER -> GmsAdSize.LARGE_BANNER
        AdSize.MEDIUM_RECTANGLE -> GmsAdSize.MEDIUM_RECTANGLE
    }

@Composable
actual fun BannerAdView(
    adUnitId: String,
    adSize: AdSize,
    modifier: Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            AdView(context).apply {
                setAdSize(adSize.toGmsAdSize())
                this.adUnitId = adUnitId
                loadAd(AdRequest.Builder().build())
            }
        },
        update = {},
        onRelease = { it.destroy() },
    )
}
