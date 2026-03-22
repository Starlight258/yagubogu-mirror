package com.yagubogu.ui.onboarding.nickname

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NicknameScreen(
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NicknameViewModel = koinViewModel(),
) {
}
