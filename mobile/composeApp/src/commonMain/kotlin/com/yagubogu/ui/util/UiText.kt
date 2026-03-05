package com.yagubogu.ui.util

import android.content.Context
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

sealed class UiText {
    data class DynamicString(
        val value: String,
    ) : UiText()

    class StringRes(
        val resId: StringResource,
        vararg val args: Any,
    ) : UiText()

    fun asString(context: Context): String =
        when (this) {
            is DynamicString -> value
            is StringRes -> context.getString(resId, *args)
        }
    suspend fun asString(): String =
        when (this) {
            is DynamicString -> value
            is StringRes -> getString(resId, *args)
        }
}
