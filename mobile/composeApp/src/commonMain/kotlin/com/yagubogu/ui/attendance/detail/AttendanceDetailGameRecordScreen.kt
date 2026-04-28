package com.yagubogu.ui.attendance.detail

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.yagubogu.ui.attendance.component.ATTENDANCE_HISTORY_ITEM_PLAYED
import com.yagubogu.ui.attendance.component.GameResultSummary
import com.yagubogu.ui.attendance.component.ScoreboardTable
import com.yagubogu.ui.attendance.detail.component.GameRecordTable
import com.yagubogu.ui.attendance.detail.component.HitterRecordTable
import com.yagubogu.ui.attendance.detail.component.PitcherRecordTable
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.attendance.model.TeamType
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.Primary050
import com.yagubogu.ui.theme.Primary100
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.Primary700
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.theme.dpToSp
import com.yagubogu.ui.util.rememberNoRippleInteractionSource
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.attendance_detail_game_result
import yagubogu.composeapp.generated.resources.attendance_detail_tab_game_record
import yagubogu.composeapp.generated.resources.ic_clipboard
import yagubogu.composeapp.generated.resources.ic_users

@Composable
fun AttendanceDetailGameRecordScreen(
    item: AttendanceHistoryItem,
    modifier: Modifier = Modifier,
) {
    val scrollState: ScrollState = rememberScrollState()

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
    ) {
        GameResult(item = item)
        PlayerRecord(item = item)
    }
}

@Composable
private fun GameResult(
    item: AttendanceHistoryItem,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painterResource(Res.drawable.ic_clipboard),
                contentDescription = null,
                tint = Primary500,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stringResource(Res.string.attendance_detail_game_result),
                style = PretendardBold20,
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier =
                Modifier
                    .background(color = White, shape = RoundedCornerShape(12.dp))
                    .padding(20.dp),
        ) {
            GameResultSummary(item = item)

            ScoreboardTable(
                awayTeamName = item.awayTeam.name,
                homeTeamName = item.homeTeam.name,
                awayInningScores = item.awayTeamScoreBoard.inningScores,
                homeInningScores = item.homeTeamScoreBoard.inningScores,
                awayScore = item.awayTeamScoreBoard.runs,
                homeScore = item.homeTeamScoreBoard.runs,
            )

            GameRecordTable(
                awayTeamName = item.awayTeam.name,
                homeTeamName = item.homeTeam.name,
                awayHits = item.awayTeamScoreBoard.hits,
                homeHits = item.homeTeamScoreBoard.hits,
                awayErrors = item.awayTeamScoreBoard.errors,
                homeErrors = item.homeTeamScoreBoard.errors,
                awayBalls = item.awayTeamScoreBoard.basesOnBalls,
                homeBalls = item.homeTeamScoreBoard.basesOnBalls,
            )
        }
    }
}

@Composable
private fun PlayerRecord(
    item: AttendanceHistoryItem,
    modifier: Modifier = Modifier,
) {
    val pagerState: PagerState = rememberPagerState { TeamType.entries.size }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                painterResource(Res.drawable.ic_users),
                contentDescription = null,
                tint = Primary500,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stringResource(Res.string.attendance_detail_tab_game_record),
                style = PretendardBold20,
            )

            Spacer(modifier = Modifier.weight(1f))
            PlayerRecordTeamTabRow(
                pagerState = pagerState,
                awayTeamName = item.awayTeam.name,
                homeTeamName = item.homeTeam.name,
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier =
                Modifier
                    .background(color = White, shape = RoundedCornerShape(12.dp))
                    .padding(20.dp),
        ) {
            HitterRecordTable()

            PitcherRecordTable()
        }
    }
}

@Composable
private fun PlayerRecordTeamTabRow(
    pagerState: PagerState,
    awayTeamName: String,
    homeTeamName: String,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    PrimaryTabRow(
        selectedTabIndex = pagerState.currentPage,
        modifier =
            modifier
                .size(width = 104.dp, height = 32.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Primary100, RoundedCornerShape(8.dp)),
        containerColor = Primary050,
        contentColor = Primary500,
        indicator = {
            Box(
                modifier =
                    Modifier
                        .tabIndicatorOffset(
                            selectedTabIndex = pagerState.currentPage,
                            matchContentSize = false,
                        ).padding(2.dp)
                        .size(width = 50.dp, height = 28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Primary500)
                        .zIndex(-1f),
            )
        },
        divider = {},
        tabs = {
            TeamType.entries.forEachIndexed { index, teamType ->
                val isSelected: Boolean = pagerState.currentPage == index
                Tab(
                    selected = isSelected,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    interactionSource = rememberNoRippleInteractionSource(),
                    content = {
                        Text(
                            text =
                                when (teamType) {
                                    TeamType.AWAY -> awayTeamName
                                    TeamType.HOME -> homeTeamName
                                },
                            style =
                                PretendardSemiBold.copy(
                                    fontSize = 16.dpToSp,
                                    color = if (isSelected) White else Primary700,
                                ),
                            modifier = Modifier.padding(4.dp),
                        )
                    },
                )
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun AttendanceDetailGameRecordScreenPreview() {
    AttendanceDetailGameRecordScreen(
        item = ATTENDANCE_HISTORY_ITEM_PLAYED,
    )
}
