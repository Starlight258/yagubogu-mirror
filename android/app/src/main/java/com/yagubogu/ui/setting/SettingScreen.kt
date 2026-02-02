package com.yagubogu.ui.setting

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.yagubogu.R
import com.yagubogu.ui.common.component.DefaultToolbar
import com.yagubogu.ui.navigation.model.Navigator
import com.yagubogu.ui.navigation.model.SettingNavKey
import com.yagubogu.ui.navigation.model.toEntries
import com.yagubogu.ui.theme.Gray050

@Composable
fun SettingScreen(
    navigator: Navigator,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onDeleteAccountCancel: () -> Unit,
    onFavoriteTeamEditClick: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        containerColor = Gray050,
        topBar = {
            DefaultToolbar(
                onBackClick = {
                    when (navigator.canGoBack()) {
                        true -> navigator.goBack()
                        false -> onBackClick()
                    }
                },
                title = stringResource((navigator.currentRoute as? SettingNavKey)?.label ?: R.string.setting_main_title),
            )
        },
        modifier = modifier,
    ) { innerPadding: PaddingValues ->
        val entryProvider: (NavKey) -> NavEntry<NavKey> =
            entryProvider {
                entry<SettingNavKey.SettingMain> {
                    SettingMainScreen(
                        snackbarHostState = snackbarHostState,
                        onSettingAccountClick = { navigator.navigate(SettingNavKey.SettingAccount) },
                        onFavoriteTeamEditClick = { onFavoriteTeamEditClick() },
                    )
                }
                entry<SettingNavKey.SettingAccount> {
                    SettingAccountScreen(
                        onDeleteAccountClick = { navigator.navigate(SettingNavKey.SettingDeleteAccount) },
                        onLogout = onLogout,
                    )
                }
                entry<SettingNavKey.SettingDeleteAccount> {
                    SettingDeleteAccountScreen(
                        snackbarHostState = snackbarHostState,
                        onDeleteAccountCancel = {
                            navigator.clearStack()
                            onDeleteAccountCancel()
                        },
                        onDeleteAccount = {
                            navigator.clearStack()
                            onLogout()
                        },
                    )
                }
            }

        NavDisplay(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            entries = navigator.state.toEntries(entryProvider),
            onBack = { navigator.goBack() },
        )
    }
}
