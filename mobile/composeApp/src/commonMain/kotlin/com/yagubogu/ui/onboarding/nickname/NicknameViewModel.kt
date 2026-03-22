package com.yagubogu.ui.onboarding.nickname

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.yagubogu.data.repository.member.MemberRepository
import com.yagubogu.data.repository.member.NicknameUpdateError
import com.yagubogu.data.repository.member.toNicknameUpdateError
import com.yagubogu.data.repository.member.toUiText
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class NicknameViewModel(
    private val memberRepository: MemberRepository,
) : ViewModel() {
    private val logger = Logger.withTag("NicknameViewModel")

    private val _favoriteTeamUpdateEvent = MutableSharedFlow<Unit>()
    val favoriteTeamUpdateEvent: SharedFlow<Unit> = _favoriteTeamUpdateEvent.asSharedFlow()

    fun updateNickname(newNickname: String) {
        viewModelScope.launch {
            memberRepository
                .updateNickname(newNickname)
                .onSuccess {
                }.onFailure { exception: Throwable ->
                    val nicknameUpdateError: NicknameUpdateError = exception.toNicknameUpdateError()
                    val errorText = nicknameUpdateError.toUiText()
                    logger.w(exception) { "닉네임 변경 API 호출 실패" }
                }
        }
    }
}
