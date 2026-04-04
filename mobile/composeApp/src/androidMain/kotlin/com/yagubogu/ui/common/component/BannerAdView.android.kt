package com.yagubogu.ui.common.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.AdSize as GmsAdSize

actual enum class AdSize {
    BANNER,
    LARGE_BANNER,
    MEDIUM_RECTANGLE,
    ;

    fun toGmsAdSize(): GmsAdSize =
        when (this) {
            BANNER -> GmsAdSize.BANNER
            LARGE_BANNER -> GmsAdSize.LARGE_BANNER
            MEDIUM_RECTANGLE -> GmsAdSize.MEDIUM_RECTANGLE
        }
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
        update = { adView ->
            if (adView.adUnitId != adUnitId) {
                adView.adUnitId = adUnitId
                adView.loadAd(AdRequest.Builder().build())
            }
        },
    )
}
