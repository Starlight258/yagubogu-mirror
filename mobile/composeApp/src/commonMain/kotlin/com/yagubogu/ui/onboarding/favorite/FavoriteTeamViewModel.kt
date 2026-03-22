package com.yagubogu.ui.onboarding.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.yagubogu.data.repository.member.MemberRepository
import com.yagubogu.domain.model.Team
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class FavoriteTeamViewModel(
    private val memberRepository: MemberRepository,
) : ViewModel() {
    private val logger = Logger.withTag("FavoriteTeamViewModel")

    private val _favoriteTeamUpdateEvent = MutableSharedFlow<Unit>()
    val favoriteTeamUpdateEvent: SharedFlow<Unit> = _favoriteTeamUpdateEvent.asSharedFlow()

    fun saveFavoriteTeam(team: Team) {
        viewModelScope.launch {
            memberRepository
                .updateFavoriteTeam(team.name)
                .onSuccess {
                    _favoriteTeamUpdateEvent.emit(Unit)
                }.onFailure { exception: Throwable ->
                    logger.w(exception) { "API 호출 실패" }
                }
        }
    }
}
