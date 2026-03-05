package com.yagubogu.ui.util

import platform.Foundation.NSURL
import platform.UIKit.UIApplication


actual fun openUrl(url: String) {
    val nsUrl = NSURL.URLWithString(url)
    if (nsUrl != null) {
        // iOS 시스템 브라우저로 URL 열기
        UIApplication.sharedApplication.openURL(nsUrl)
    }
}