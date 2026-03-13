package com.yagubogu.ui.util

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import com.yagubogu.R

@Composable
actual fun BackPressHandler() {
    val context: Context = LocalContext.current
    val resources: Resources = LocalResources.current
    val snackbarHostState = LocalSnackbarHostState.current
    val snackbarScope = LocalSnackbarScope.current

    var backPressedTime: Long by remember { mutableLongStateOf(0L) }

    BackHandler {
        val currentTime: Long = System.currentTimeMillis()
        if (currentTime - backPressedTime > BACK_PRESS_INTERVAL_MS) {
            backPressedTime = currentTime
            snackbarHostState.showSingleSnackbar(
                scope = snackbarScope,
                message = resources.getString(R.string.main_back_press_to_exit),
            )
        } else {
            (context as? Activity)?.finish()
        }
    }
}

private const val BACK_PRESS_INTERVAL_MS: Long = 1000L
