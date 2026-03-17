package com.yagubogu.ui.setting

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import com.yagubogu.ui.common.component.DefaultToolbar
import com.yagubogu.ui.navigation.model.NavigationState
import com.yagubogu.ui.navigation.model.SettingNavKey
import com.yagubogu.ui.navigation.model.toEntries
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.util.slidePopTransition
import com.yagubogu.ui.util.slidePredictivePopTransition
import com.yagubogu.ui.util.slidePushTransition
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.setting_main_title

@Composable
fun SettingScreen(
    navigationState: NavigationState,
    onBackClick: () -> Unit,
    onSettingItemClick: (SettingNavKey) -> Unit,
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
                    onBackClick = onBackClick,
                    title =
                        stringResource(
                            (navigationState.currentRoute as? SettingNavKey)?.label
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
                        onSettingAccountClick = { onSettingItemClick(SettingNavKey.SettingAccount) },
                        onFavoriteTeamEditClick = { onFavoriteTeamEditClick() },
                        onFullScreenMode = { isFull: Boolean -> isTopBarVisible = !isFull },
                        onOssLicenseClick = { onSettingItemClick(SettingNavKey.OssLicense) },
                    )
                }
                entry<SettingNavKey.SettingAccount> {
                    SettingAccountScreen(
                        viewModel = viewModel,
                        onDeleteAccountClick = { onSettingItemClick(SettingNavKey.SettingDeleteAccount) },
                        onLogout = onLogout,
                    )
                }
                entry<SettingNavKey.SettingDeleteAccount> {
                    SettingDeleteAccountScreen(
                        viewModel = viewModel,
                        onDeleteAccountCancel = onDeleteAccountCancel,
                        onLogout = onDeleteAccount,
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
            entries = navigationState.toEntries(entryProvider),
            onBack = onBackClick,
            transitionSpec = { slidePushTransition() },
            popTransitionSpec = { slidePopTransition() },
            predictivePopTransitionSpec = { edge ->
                slidePredictivePopTransition(edge)
            },
        )
    }
}
