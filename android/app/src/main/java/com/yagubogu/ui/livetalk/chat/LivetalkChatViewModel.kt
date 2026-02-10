package com.yagubogu.ui.livetalk.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.data.dto.request.game.LikeBatchRequest
import com.yagubogu.data.dto.request.game.LikeDeltaDto
import com.yagubogu.data.dto.response.game.LikeCountsResponse
import com.yagubogu.data.repository.game.GameRepository
import com.yagubogu.data.repository.member.MemberRepository
import com.yagubogu.data.repository.talk.TalkRepository
import com.yagubogu.data.util.ApiException
import com.yagubogu.ui.common.model.MemberProfile
import com.yagubogu.ui.livetalk.chat.model.LivetalkChatBubbleItem
import com.yagubogu.ui.livetalk.chat.model.LivetalkChatItem
import com.yagubogu.ui.livetalk.chat.model.LivetalkChatUiState
import com.yagubogu.ui.livetalk.chat.model.LivetalkResponseItem
import com.yagubogu.ui.livetalk.chat.model.LivetalkTeams
import com.yagubogu.ui.mapper.toUiModel
import com.yagubogu.ui.util.now
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDateTime
import timber.log.Timber
import kotlin.time.Clock

class LivetalkChatViewModel(
    private val talkRepository: TalkRepository,
    private val gameRepository: GameRepository,
    private val memberRepository: MemberRepository,
) : ViewModel() {
    val messageStateHolder = MessageStateHolder()
    val likeCountStateHolder = LikeCountStateHolder()

    private val _teams = MutableStateFlow<LivetalkTeams?>(null)
    val teams: StateFlow<LivetalkTeams?> = _teams.asStateFlow()

    private val pollingControlLock = Mutex()
    private var pollingJob: Job? = null

    private var likeBatchingJob: Job? = null

    private val _selectedProfile = MutableStateFlow<MemberProfile?>(null)
    val selectedProfile: StateFlow<MemberProfile?> = _selectedProfile.asStateFlow()

    private val _isInitialLoadCompleted: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isInitialLoadCompleted: StateFlow<Boolean> = _isInitialLoadCompleted.asStateFlow()

    val chatUiState: StateFlow<LivetalkChatUiState> =
        combine(
            messageStateHolder.livetalkChatBubbleItems,
            isInitialLoadCompleted,
        ) { items, isInitialLoadCompleted ->
            when {
                !isInitialLoadCompleted -> LivetalkChatUiState.Loading
                items.isEmpty() -> LivetalkChatUiState.Empty
                else -> LivetalkChatUiState.Success(items)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LivetalkChatUiState.Loading,
        )

    fun fetchBeforeTalks(gameId: Long) {
        if (!messageStateHolder.hasNext) return

        viewModelScope.launch {
            val result: Result<LivetalkResponseItem> =
                talkRepository
                    .getBeforeTalks(gameId, messageStateHolder.oldestMessageCursor, CHAT_LOAD_LIMIT)
                    .map { it.toUiModel() }
            result
                .onSuccess { response: LivetalkResponseItem ->
                    _isInitialLoadCompleted.value = true
                    messageStateHolder.addBeforeChats(response)
                }.onFailure { exception ->
                    Timber.w(exception, "과거 메시지 API 호출 실패")
                }
        }
    }

    private suspend fun fetchAfterTalks(gameId: Long) {
        val result: Result<LivetalkResponseItem> =
            talkRepository
                .getAfterTalks(
                    gameId,
                    messageStateHolder.newestMessageCursor,
                    CHAT_LOAD_LIMIT,
                ).map { it.toUiModel() }
        result
            .onSuccess { response: LivetalkResponseItem ->
                _isInitialLoadCompleted.value = true
                messageStateHolder.addAfterChats(response)
            }.onFailure { exception ->
                Timber.w(exception, "최신 메시지 API 호출 실패")
            }
    }

    fun sendMessage(gameId: Long) {
        val message = messageStateHolder.messageText.value
        if (message.isBlank()) return

        viewModelScope.launch {
            messageStateHolder.updateMessageText("")
            messageStateHolder.addPendingWriteChat(
                LivetalkChatBubbleItem.MyPendingBubbleItem(
                    LivetalkChatItem(
                        System.currentTimeMillis(),
                        0L,
                        true,
                        message,
                        null,
                        null,
                        null,
                        LocalDateTime.now(),
                        false,
                    ),
                ),
            )
        }

        viewModelScope.launch {
            val talksResult: Result<LivetalkChatItem> =
                talkRepository.postTalks(gameId, message.trim()).map { it.toUiModel() }
            talksResult
                .onSuccess {
                    stopPolling()
                    startPolling(gameId)
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    fun deleteMessage(
        gameId: Long,
        chatId: Long,
    ) {
        viewModelScope.launch {
            talkRepository
                .deleteTalks(gameId, chatId)
                .onSuccess {
                    messageStateHolder.deleteChat(chatId)
                    Timber.d("현장톡 정상 삭제")
                }.onFailure { exception: Throwable ->
                    when (exception) {
                        is ApiException.BadRequest -> Timber.d("해당 경기에 존재하지 않는 현장톡 삭제 시도")
                        is ApiException.Forbidden -> Timber.d("타인의 현장톡 삭제 시도")
                        is ApiException.NotFound -> Timber.d("존재하지 않는 현장톡 삭제 시도")
                        else -> Timber.d(exception)
                    }
                }
        }
    }

    fun reportMessage(chatId: Long) {
        viewModelScope.launch {
            talkRepository
                .reportTalks(chatId)
                .onSuccess {
                    messageStateHolder.reportChat(chatId)
                    Timber.d("현장톡 정상 신고")
                }.onFailure { exception: Throwable ->
                    when (exception) {
                        is ApiException.BadRequest -> {
                            Timber.d("스스로 신고하거나 중복 신고인 경우")
                        }

                        is ApiException.Forbidden -> {
                            Timber.d("회원이 존재하지 않거나 존재하지 않는 현장톡 신고 시도")
                        }

                        else -> {
                            Timber.d(exception)
                        }
                    }
                }
        }
    }

    fun addLikeToBatch(gameId: Long) {
        viewModelScope.launch {
            likeCountStateHolder.increaseMyTeamShowingCount()
            likeCountStateHolder.increaseLikeCount()
            if (likeBatchingJob?.isActive != true) {
                likeBatchingJob =
                    launch {
                        delay(LIKE_BATCH_INTERVAL_MILLS)
                        sendLikeBatch(gameId)
                    }
            }
        }
    }

    private suspend fun getLikeCount(gameId: Long) {
        val teams: LivetalkTeams? = teams.value
        teams?.myTeamType ?: return

        gameRepository
            .getLikeCounts(gameId)
            .onSuccess { likeCountsResponse: LikeCountsResponse ->
                likeCountStateHolder.updateLikeCount(teams, likeCountsResponse)
            }.onFailure { exception ->
                Timber.w(exception, "응원수 로드 실패")
            }
    }

    private suspend fun sendLikeBatch(gameId: Long) {
        val teams: LivetalkTeams? = teams.value
        teams ?: return

        val countToSend: Int = likeCountStateHolder.getCountToSend()
        val request =
            LikeBatchRequest(
                windowStartEpochSec = Clock.System.now().epochSeconds,
                likeDelta =
                    LikeDeltaDto(
                        teamCode = teams.myTeam.name,
                        delta = countToSend,
                    ),
            )

        if (countToSend > 0 && teams.myTeamType != null) {
            gameRepository
                .addLikeBatches(gameId, request)
                .onSuccess {
                    Timber.d("응원 배치 전송 성공: $countToSend 건")
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "응원 배치 전송 실패")
                }
        }
    }

    fun fetchTeams(gameId: Long) {
        viewModelScope.launch {
            _isInitialLoadCompleted.value = false
            val result: Result<LivetalkTeams> =
                talkRepository.getInitial(gameId).map { it.toUiModel() }
            result
                .onSuccess { livetalkTeams: LivetalkTeams ->
                    _teams.value = livetalkTeams

                    startPolling(gameId)
                }.onFailure { exception ->
                    Timber.w(exception, "최초 팀 정보 가져오기 실패")
                }
        }
    }

    fun fetchMemberProfile(memberId: Long) {
        viewModelScope.launch {
            val memberProfileResult: Result<MemberProfile> =
                memberRepository.getMemberProfile(memberId).map { it.toUiModel() }
            memberProfileResult
                .onSuccess { memberProfile: MemberProfile ->
                    _selectedProfile.emit(memberProfile)
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "사용자 프로필 조회 API 호출 실패")
                }
        }
    }

    fun dismissProfile() {
        viewModelScope.launch {
            _selectedProfile.emit(null)
        }
    }

    fun startPolling(gameId: Long) {
        viewModelScope.launch {
            pollingControlLock.withLock {
                if (pollingJob?.isActive == true) return@launch

                pollingJob =
                    launch {
                        while (true) {
                            launch { fetchAfterTalks(gameId) }
                            launch { getLikeCount(gameId) }
                            delay(POLLING_INTERVAL_MILLS)
                        }
                    }
            }
        }
    }

    fun stopPolling() {
        viewModelScope.launch {
            pollingControlLock.withLock {
                pollingJob?.cancel()
                pollingJob = null
            }
        }
    }

    fun clear(gameId: Long) {
        stopPolling()
        likeBatchingJob?.cancel()
        if (likeCountStateHolder.pendingLikeCount > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                sendLikeBatch(gameId)
            }
        }
        viewModelScope.launch {
            messageStateHolder.clear()
        }
    }

    companion object {
        private const val POLLING_INTERVAL_MILLS = 10_000L

        private const val CHAT_LOAD_LIMIT = 30

        private const val LIKE_BATCH_INTERVAL_MILLS = 5_000L
    }
}
