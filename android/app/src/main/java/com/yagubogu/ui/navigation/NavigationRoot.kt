package com.yagubogu.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.yagubogu.ui.badge.component.BadgeScreen
import com.yagubogu.ui.favorite.FavoriteTeamScreen
import com.yagubogu.ui.livetalk.chat.LivetalkChatScreen
import com.yagubogu.ui.login.LoginScreen
import com.yagubogu.ui.main.MainScreen
import com.yagubogu.ui.navigation.model.BottomNavKey
import com.yagubogu.ui.navigation.model.Navigator
import com.yagubogu.ui.navigation.model.Route
import com.yagubogu.ui.navigation.model.toEntries
import com.yagubogu.ui.setting.SettingScreen

/**
 * 앱의 최상위 네비게이션 구조를 정의하는 루트 컴포저블.
 *
 * 각 경로([Route])에 따른 화면 컴포저블을 매핑하여 화면 전환을 관리합니다.
 *
 * @param rootNavigator 최상위 라우팅 관리 Navigator
 * @param mainNavigator 하단 탭 네비게이션 Navigator
 * @param settingNavigator 설정 화면 네비게이션 Navigator
 * @param modifier 레이아웃 수정을 위한 [Modifier]
 */
@Composable
fun NavigationRoot(
    rootNavigator: Navigator,
    mainNavigator: Navigator,
    settingNavigator: Navigator,
    modifier: Modifier = Modifier,
) {
    val entryProvider: (NavKey) -> NavEntry<NavKey> =
        entryProvider {
            entry<Route.Login> {
                LoginScreen(
                    onSignIn = { rootNavigator.navigate(Route.Main) },
                    onSignUp = { rootNavigator.navigate(Route.FavoriteTeam) },
                )
            }
            entry<Route.Main> {
                MainScreen(
                    navigator = mainNavigator,
                    onSettingsClick = { rootNavigator.navigate(Route.Setting) },
                    onBadgeClick = { rootNavigator.navigate(Route.Badge) },
                    onLivetalkItemClick = { gameId: Long, isVerified: Boolean ->
                        rootNavigator.navigate(Route.LivetalkChat(gameId, isVerified))
                    },
                )
            }
            entry<Route.Setting> {
                SettingScreen(
                    navigator = settingNavigator,
                    onBackClick = { rootNavigator.goBack() },
                    onFavoriteTeamEditClick = { rootNavigator.navigate(Route.FavoriteTeam) },
                    onLogout = {
                        mainNavigator.navigate(BottomNavKey.Home)
                        rootNavigator.clearStack()
                        rootNavigator.navigate(Route.Login)
                    },
                    onDeleteAccountCancel = {
                        mainNavigator.navigate(BottomNavKey.Home)
                        rootNavigator.clearStack()
                        rootNavigator.navigate(Route.Main)
                    },
                    onDeleteAccount = {
                        mainNavigator.navigate(BottomNavKey.Home)
                        rootNavigator.clearStack()
                        rootNavigator.navigate(Route.Main)
                    },
                )
            }
            entry<Route.FavoriteTeam> {
                FavoriteTeamScreen(
                    onFavoriteTeamUpdate = {
                        mainNavigator.navigate(BottomNavKey.Home)
                        rootNavigator.navigate(Route.Main)
                        rootNavigator.clearStack()
                    },
                )
            }
            entry<Route.Badge> {
                BadgeScreen(
                    onBackClick = { rootNavigator.goBack() },
                )
            }
            entry<Route.LivetalkChat> { key: Route.LivetalkChat ->
                LivetalkChatScreen(
                    gameId = key.gameId,
                    isVerified = key.isVerified,
                    onBackClick = { rootNavigator.goBack() },
                )
            }
        }

    NavDisplay(
        modifier = modifier.fillMaxSize(),
        entries = rootNavigator.state.toEntries(entryProvider),
        onBack = { rootNavigator.goBack() },
    )
}
