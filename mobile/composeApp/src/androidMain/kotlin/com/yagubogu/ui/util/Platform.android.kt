package com.yagubogu.ui.util

import android.content.Intent
import com.yagubogu.YaguBoguApplication
import androidx.core.net.toUri

actual fun openUrl(url: String) {
    val context = YaguBoguApplication.instance.applicationContext
    val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}