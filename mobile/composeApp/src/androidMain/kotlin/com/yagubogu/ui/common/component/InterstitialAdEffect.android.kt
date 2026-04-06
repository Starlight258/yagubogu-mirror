package com.yagubogu.ui.common.component

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
) {
    val context = LocalContext.current
    val activity = context as? Activity ?: return

    // 로드된 광고 인스턴스 보관 — null이면 아직 로드 전 또는 표시 직후
    var loadedAd by remember { mutableStateOf<InterstitialAd?>(null) }

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

    // 컴포저블 진입 시 미리 로드해 표시 딜레이 방지
    LaunchedEffect(adUnitId) {
        loadAd()
    }

    LaunchedEffect(triggerFlow) {
        triggerFlow.collect {
            // 아직 로드되지 않았으면 이번 트리거는 스킵
            val ad = loadedAd ?: return@collect
            ad.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        loadedAd = null
                        loadAd() // 다음 트리거를 위해 즉시 재로드
                    }

                    override fun onAdFailedToShowFullScreenContent(error: AdError) {
                        loadedAd = null
                        loadAd()
                    }
                }
            loadedAd = null
            ad.show(activity)
        }
    }
}
