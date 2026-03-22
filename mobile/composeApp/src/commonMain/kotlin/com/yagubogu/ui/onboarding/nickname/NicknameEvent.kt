package com.yagubogu.ui.onboarding.nickname

import com.yagubogu.ui.util.UiText

sealed interface NicknameEvent {
    data class NicknameEditSuccess(
        val newNickname: String,
    ) : NicknameEvent

    data class NicknameEditFailure(
        val uiText: UiText,
    ) : NicknameEvent
}
