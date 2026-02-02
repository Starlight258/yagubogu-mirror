package com.yagubogu.ui.util

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

val LocalSnackbarHostState =
    staticCompositionLocalOf<SnackbarHostState> {
        error("SnackbarHostState 없음")
    }

fun SnackbarHostState.showSingleSnackbar(
    scope: CoroutineScope,
    message: String,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Short,
) {
    scope.launch {
        currentSnackbarData?.dismiss()
        showSnackbar(
            message = message,
            actionLabel = actionLabel,
            duration = duration,
        )
    }
}
