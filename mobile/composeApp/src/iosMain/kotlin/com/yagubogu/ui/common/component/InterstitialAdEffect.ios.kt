package com.yagubogu.ui.common.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow

@Composable
actual fun InterstitialAdEffect(
    triggerFlow: Flow<Unit>,
    adUnitId: String,
) {
    // 컴포저블 진입 시 미리 로드해 표시 딜레이 방지 (Android LaunchedEffect(adUnitId)와 동일)
    LaunchedEffect(adUnitId) {
        InterstitialAdProvider.preload?.invoke(adUnitId)
    }

    LaunchedEffect(triggerFlow) {
        triggerFlow.collect {
            InterstitialAdProvider.show?.invoke(adUnitId)
        }
    }
}
