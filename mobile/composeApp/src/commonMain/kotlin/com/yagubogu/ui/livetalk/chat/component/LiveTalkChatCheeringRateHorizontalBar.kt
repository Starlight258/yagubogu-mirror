package com.yagubogu.ui.livetalk.chat.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.theme.EsamanruMedium
import com.yagubogu.ui.theme.YaguBoguTheme
import com.yagubogu.ui.theme.dpToSp
import com.yagubogu.ui.util.color
import com.yagubogu.ui.util.formatWithComma
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.livetalk_cheering_count_format
import yagubogu.composeapp.generated.resources.livetalk_cheering_count_label

@Composable
fun LiveTalkChatCheeringRateHorizontalBar(
    myTeam: Team,
    otherTeam: Team,
    myTeamCheeringCount: Long,
    otherTeamCheeringCount: Long,
    modifier: Modifier = Modifier,
) {
    val totalCount = myTeamCheeringCount + otherTeamCheeringCount
    val myTeamChartRange = if (totalCount > 0) myTeamCheeringCount.toFloat() / totalCount else 0.5f
    val otherTeamChartRange = if (totalCount > 0) otherTeamCheeringCount.toFloat() / totalCount else 0.5f

    val barHeight = 8.dp

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text =
                    stringResource(
                        Res.string.livetalk_cheering_count_format,
                        otherTeam.shortname,
                        otherTeamCheeringCount.formatWithComma(),
                    ),
                style = EsamanruMedium.copy(fontSize = 14.dpToSp, color = otherTeam.color),
                modifier = Modifier.align(Alignment.CenterStart),
            )

            Text(
                text = stringResource(Res.string.livetalk_cheering_count_label),
                style = EsamanruMedium.copy(fontSize = 14.dpToSp, color = com.yagubogu.ui.theme.Gray600),
                modifier = Modifier.align(Alignment.Center),
            )

            Text(
                text =
                    stringResource(
                        Res.string.livetalk_cheering_count_format,
                        myTeam.shortname,
                        myTeamCheeringCount.formatWithComma(),
                    ),
                style = EsamanruMedium.copy(fontSize = 14.dpToSp, color = myTeam.color),
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(barHeight)
                    .clip(RoundedCornerShape(12.dp)),
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier =
                        Modifier
                            .weight(otherTeamChartRange)
                            .fillMaxHeight()
                            .background(color = otherTeam.color),
                )
                Box(
                    modifier =
                        Modifier
                            .weight(myTeamChartRange)
                            .fillMaxHeight()
                            .background(color = myTeam.color),
                )
            }
        }
    }
}

@Preview
@Composable
private fun LiveTalkChatCheeringRateHorizontalBarPreview() {
    YaguBoguTheme {
        LiveTalkChatCheeringRateHorizontalBar(
            myTeam = Team.HH,
            otherTeam = Team.NC,
            myTeamCheeringCount = 1000L,
            otherTeamCheeringCount = 3000L,
        )
    }
}
