package com.yagubogu.ui.util

import com.yagubogu.BuildKonfig
import com.yagubogu.ui.login.model.VersionInfo

fun getAppVersion(): String {
    val appVersion = BuildKonfig.VERSION_CODE
    val isDebug: Boolean = BuildKonfig.IS_DEBUG
    val availableVersionInfo = VersionInfo.of(appVersion)
    val versionName = availableVersionInfo.major.toString() + "." + availableVersionInfo.minor.toString() + "." + availableVersionInfo.patch.toString()

    return when(isDebug){
        true->"$versionName.debug"
        false->versionName
    }
}