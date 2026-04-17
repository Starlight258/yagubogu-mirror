package com.yagubogu.ui.common.component

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize as SdkAdSize

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
    val activity = LocalActivity.current ?: return

    AndroidView(
        modifier = modifier,
        factory = {
            AdView(activity).also { adView ->
                val adRequest = BannerAdRequest.Builder(adUnitId, adSize.toSdkAdSize()).build()
                adView.loadAd(
                    adRequest,
                    object : AdLoadCallback<BannerAd> {
                        override fun onAdLoaded(ad: BannerAd) {
                            ad.adEventCallback =
                                object : BannerAdEventCallback {
                                    override fun onAdImpression() = Unit

                                    override fun onAdClicked() = Unit

                                    override fun onAdShowedFullScreenContent() = Unit

                                    override fun onAdDismissedFullScreenContent() = Unit

                                    override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) = Unit
                                }
                        }

                        override fun onAdFailedToLoad(adError: LoadAdError) = Unit
                    },
                )
            }
        },
        update = {},
        onRelease = { it.destroy() },
    )
}
