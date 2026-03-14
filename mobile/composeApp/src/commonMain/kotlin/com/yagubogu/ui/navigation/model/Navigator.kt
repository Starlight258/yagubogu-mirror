package com.yagubogu.ui.navigation.model

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import co.touchlab.kermit.Logger

/**
 * Handles navigation events (forward and back) by updating the navigation state.
 *
 * https://developer.android.com/guide/navigation/navigation-3/migration-guide
 */
class Navigator(
    val state: NavigationState,
) {
    private val logger = Logger.withTag("Navigator")

    private val currentStack: NavBackStack<NavKey>
        get() =
            state.backStacks[state.topLevelRoute]
                ?: error("Stack for ${state.topLevelRoute} not found")
    val currentRoute: NavKey
        get() = currentStack.last()

    fun navigate(route: NavKey) {
        if (route in state.backStacks.keys) {
            // This is a top level route, just switch to it.
            state.topLevelRoute = route
        } else {
            currentStack.add(route)
        }
        showBackStack()
    }

    fun canGoBack(): Boolean = currentRoute != state.topLevelRoute

    fun goBack() {
        if (canGoBack()) {
            currentStack.removeLastOrNull()
        }
        showBackStack()
    }

    fun clearStack() {
        while (canGoBack()) {
            goBack()
        }
    }

    private fun showBackStack() {
        logger.d { "backStacks: ${state.backStacks.keys}" }
        logger.d { "currentStack: ${currentStack.joinToString()}" }
    }
}
