package com.yagubogu.ui.common.component

object InterstitialAdProvider {
    var preload: ((adUnitId: String) -> Unit)? = null
    var show: ((adUnitId: String) -> Unit)? = null
}
