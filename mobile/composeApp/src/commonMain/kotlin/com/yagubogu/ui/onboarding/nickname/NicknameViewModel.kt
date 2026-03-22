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
    private val teamName: String,
) : ViewModel() {
    private val logger = Logger.withTag("NicknameViewModel")

    private val _favoriteTeamUpdateEvent = MutableSharedFlow<Unit>()
    val favoriteTeamUpdateEvent: SharedFlow<Unit> = _favoriteTeamUpdateEvent.asSharedFlow()

    private val _event = MutableSharedFlow<NicknameEvent>()
    val event: SharedFlow<NicknameEvent> = _event.asSharedFlow()

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

    fun updateDefaultNickname() {
        viewModelScope.launch {
            repeat(3) {
                val result = memberRepository.updateNickname(generateNickname(teamName))
                result
                    .onSuccess {
                        _event.emit(NicknameEvent.NavigateToMain)
                        return@launch
                    }.onFailure { exception ->
                        val error = exception.toNicknameUpdateError()
                        if (error != NicknameUpdateError.DuplicateNickname) {
                            logger.w(exception) { "닉네임 변경 API 호출 실패" }
                            return@repeat
                        }
                    }
            }

            repeat(2) {
                val result = memberRepository.updateNickname(generateFallbackNickname(teamName))
                result
                    .onSuccess {
                        _event.emit(NicknameEvent.NavigateToMain)
                        return@launch
                    }.onFailure { exception ->
                        logger.w(exception) { "fallback 닉네임 변경 API 호출 실패" }
                    }
            }

            logger.w { "닉네임 자동 생성 5회 모두 실패, 서버 기본 닉네임으로 진행" }
            _event.emit(NicknameEvent.NavigateToMain)
        }
    }

    private fun generateNickname(teamName: String): String {
        repeat(10) {
            val adj1 = ADJECTIVES.random()
            val adj2 = ADJECTIVES.filter { it != adj1 }.random()
            val candidate = "${adj1.connective} ${adj2.attributive} ${teamName}팬"
            if (candidate.length <= 15) return candidate
        }
        return generateFallbackNickname(teamName)
    }

    private fun generateFallbackNickname(teamName: String): String {
        val randomSuffix =
            (1..4)
                .map { ('a'..'z').random() }
                .joinToString("")
        val adj = ADJECTIVES.random().attributive
        return "$adj ${teamName}팬$randomSuffix"
    }
}
