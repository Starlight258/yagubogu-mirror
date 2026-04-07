package com.yagubogu.ui.common.component

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.flow.Flow

@Composable
actual fun InterstitialAdEffect(
    triggerFlow: Flow<Unit>,
    adUnitId: String,
    onAdComplete: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? Activity ?: return

    // 로드된 광고 인스턴스 보관 — null이면 아직 로드 전 또는 표시 직후
    var loadedAd by remember { mutableStateOf<InterstitialAd?>(null) }
    // 리컴포지션 시 최신 콜백을 참조하도록 보관
    val currentOnAdComplete by rememberUpdatedState(onAdComplete)

    fun loadAd() {
        InterstitialAd.load(
            context,
            adUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    loadedAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loadedAd = null
                }
            },
        )
    }

    LaunchedEffect(adUnitId) {
        loadAd()
    }

    LaunchedEffect(triggerFlow) {
        triggerFlow.collect {
            val ad = loadedAd
            if (ad == null) {
                currentOnAdComplete()
                return@collect
            }
            ad.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        loadedAd = null
                        loadAd()
                        currentOnAdComplete()
                    }

                    override fun onAdFailedToShowFullScreenContent(error: AdError) {
                        loadedAd = null
                        loadAd()
                        currentOnAdComplete()
                    }
                }
            loadedAd = null
            ad.show(activity)
        }
    }
}
