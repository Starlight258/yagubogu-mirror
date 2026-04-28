package com.yagubogu.ui.attendance.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yagubogu.analytics.AnalyticsLogger
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.theme.EsamanruBold
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardRegular
import com.yagubogu.ui.theme.PretendardRegular12
import com.yagubogu.ui.theme.PretendardSemiBold16
import com.yagubogu.ui.theme.PretendardSemiBold20
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.theme.dpToSp
import com.yagubogu.ui.util.MMddhhmmFormatter
import com.yagubogu.ui.util.noRippleClickable
import kotlinx.datetime.format

@Composable
fun AttendanceItem(
    item: AttendanceHistoryItem,
    onItemClick: (AttendanceHistoryItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = White, shape = RoundedCornerShape(12.dp))
                .noRippleClickable {
                    onItemClick(item)
                    AnalyticsLogger.logEvent("attendance_history_item_click")
                }.padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        Box(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
        ) {
            Text(
                text = item.awayTeam.score,
                style = EsamanruBold.copy(fontSize = 56.dpToSp),
                color = item.awayTeamColor,
                modifier = Modifier.align(Alignment.CenterStart),
            )

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = item.awayTeam.name,
                        style = PretendardSemiBold20,
                    )
                    Text(
                        text = "vs",
                        style = PretendardSemiBold16,
                    )
                    Text(
                        text = item.homeTeam.name,
                        style = PretendardSemiBold20,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.dateTime.format(MMddhhmmFormatter),
                    style = PretendardRegular12.copy(color = Gray500),
                )
                Text(
                    text = item.stadiumName,
                    style = PretendardRegular.copy(fontSize = 10.sp, color = Gray500),
                )
            }

            Text(
                text = item.homeTeam.score,
                style = EsamanruBold.copy(fontSize = 56.dpToSp),
                color = item.homeTeamColor,
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }

        ScoreboardTable(
            awayTeamName = item.awayTeam.name,
            homeTeamName = item.homeTeam.name,
            awayInningScores = item.awayTeamScoreBoard.inningScores,
            homeInningScores = item.homeTeamScoreBoard.inningScores,
            awayScore = item.awayTeamScoreBoard.runs,
            homeScore = item.homeTeamScoreBoard.runs,
        )
    }
}

@Preview(name = "완료된 경기")
@Composable
private fun AttendanceItemPlayedPreview() {
    AttendanceItem(
        item = ATTENDANCE_HISTORY_ITEM_PLAYED,
        onItemClick = {},
    )
}

@Preview(name = "취소된 경기")
@Composable
private fun AttendanceItemCanceledPreview() {
    AttendanceItem(
        item = ATTENDANCE_HISTORY_ITEM_CANCELED,
        onItemClick = {},
    )
}
