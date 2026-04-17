package com.yagubogu.ui.common.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize as SdkAdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError

private fun AdSize.toSdkAdSize(): SdkAdSize =
    when (this) {
        AdSize.BANNER -> SdkAdSize.BANNER
        AdSize.LARGE_BANNER -> SdkAdSize.LARGE_BANNER
        AdSize.MEDIUM_RECTANGLE -> SdkAdSize.MEDIUM_RECTANGLE
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
            AdView(context).also { adView ->
                val adRequest = BannerAdRequest.Builder(adUnitId, adSize.toSdkAdSize()).build()
                adView.loadAd(
                    adRequest,
                    object : AdLoadCallback<BannerAd> {
                        override fun onAdLoaded(ad: BannerAd) = Unit
                        override fun onAdFailedToLoad(adError: LoadAdError) = Unit
                    },
                )
            }
        },
        update = {},
        onRelease = { it.destroy() },
    )
}
