package com.yagubogu.ui.setting

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEvent
import com.yagubogu.ui.common.component.DefaultToolbar
import com.yagubogu.ui.navigation.model.Navigator
import com.yagubogu.ui.navigation.model.SettingNavKey
import com.yagubogu.ui.navigation.model.toEntries
import com.yagubogu.ui.theme.Gray050
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.setting_main_title

@Composable
fun SettingScreen(
    navigator: Navigator,
    onBackClick: () -> Unit,
    onFavoriteTeamEditClick: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccountCancel: () -> Unit,
    onDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingViewModel = koinViewModel(),
) {
    var isTopBarVisible: Boolean by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = Gray050,
        topBar = {
            AnimatedVisibility(
                visible = isTopBarVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                DefaultToolbar(
                    onBackClick = {
                        when (navigator.canGoBack()) {
                            true -> navigator.goBack()
                            false -> onBackClick()
                        }
                    },
                    title =
                        stringResource(
                            (navigator.currentRoute as? SettingNavKey)?.label
                                ?: Res.string.setting_main_title,
                        ),
                )
            }
        },
        modifier = modifier,
    ) { innerPadding: PaddingValues ->
        val entryProvider: (NavKey) -> NavEntry<NavKey> =
            entryProvider {
                entry<SettingNavKey.SettingMain> {
                    SettingMainScreen(
                        viewModel = viewModel,
                        onSettingAccountClick = { navigator.navigate(SettingNavKey.SettingAccount) },
                        onFavoriteTeamEditClick = { onFavoriteTeamEditClick() },
                        onFullScreenMode = { isFull: Boolean -> isTopBarVisible = !isFull },
                        onOssLicenseClick = { navigator.navigate(SettingNavKey.OssLicense) }
                    )
                }
                entry<SettingNavKey.SettingAccount> {
                    SettingAccountScreen(
                        viewModel = viewModel,
                        onDeleteAccountClick = { navigator.navigate(SettingNavKey.SettingDeleteAccount) },
                        onLogout = onLogout,
                    )
                }
                entry<SettingNavKey.SettingDeleteAccount> {
                    SettingDeleteAccountScreen(
                        viewModel = viewModel,
                        onDeleteAccountCancel = onDeleteAccountCancel,
                        onLogout = {
                            navigator.clearStack()
                            onDeleteAccount()
                        },
                    )
                }
                entry<SettingNavKey.OssLicense> {
                    OssLicenseScreen()
                }
            }

        NavDisplay(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            entries = navigator.state.toEntries(entryProvider),
            onBack = { navigator.goBack() },
            transitionSpec = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(TRANSITION_DURATION_MILLISECOND),
                ) togetherWith
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                            targetOffset = { it / 4 },
                            animationSpec = tween(TRANSITION_DURATION_MILLISECOND),
                        )
            },
            popTransitionSpec = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    initialOffset = { it / 4 },
                    animationSpec = tween(TRANSITION_DURATION_MILLISECOND),
                ) togetherWith
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(TRANSITION_DURATION_MILLISECOND),
                        )
            },
            predictivePopTransitionSpec = { edge ->
                val towards = if (edge == NavigationEvent.EDGE_LEFT) {
                    AnimatedContentTransitionScope.SlideDirection.Right
                } else {
                    AnimatedContentTransitionScope.SlideDirection.Left
                }
                slideIntoContainer(
                    towards = towards,
                    initialOffset = { it / 4 },
                    animationSpec = tween(TRANSITION_DURATION_MILLISECOND),
                ) togetherWith
                        slideOutOfContainer(
                            towards = towards,
                            animationSpec = tween(TRANSITION_DURATION_MILLISECOND),
                        )
            },
        )
    }
}

private const val TRANSITION_DURATION_MILLISECOND = 500
