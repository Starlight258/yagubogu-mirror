package com.yagubogu.ui.attendance.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.theme.PretendardRegular
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.Primary050
import com.yagubogu.ui.theme.Primary900
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.theme.dpToSp

@Composable
fun PitcherRecordTable(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        HorizontalDivider(color = Gray300, thickness = 0.4.dp)
        PitcherRecordHeader()
        HorizontalDivider(color = Gray300, thickness = 0.4.dp)
        PitcherRecordRow(
            playerName = "민우가",
            result = "승",
            innings = "10",
            hitsAllowed = 10,
            runsAllowed = 10,
            earnedRuns = 10,
            strikeouts = 10,
            walksAndHbp = 10,
            pitchCount = 10,
        )
        HorizontalDivider(color = Gray300, thickness = 0.4.dp)
        PitcherRecordRow(
            playerName = "김두리",
            result = "승",
            innings = "10",
            hitsAllowed = 10,
            runsAllowed = 10,
            earnedRuns = 10,
            strikeouts = 10,
            walksAndHbp = 10,
            pitchCount = 10,
        )
        HorizontalDivider(color = Gray300, thickness = 0.4.dp)
    }
}

@Composable
private fun PitcherRecordHeader(modifier: Modifier = Modifier) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(Primary050)
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "투수명",
            style = PretendardSemiBold.copy(fontSize = 14.dpToSp, color = Primary900),
            modifier = Modifier.weight(2.4f).padding(start = 8.dp),
        )

        PitcherRecordText(
            text = "이닝",
            modifier = Modifier.weight(1f),
            isHeader = true,
        )
        PitcherRecordText(
            text = "피안타",
            modifier = Modifier.weight(1f),
            isHeader = true,
        )
        PitcherRecordText(
            text = "실점",
            modifier = Modifier.weight(1f),
            isHeader = true,
        )
        PitcherRecordText(
            text = "자책",
            modifier = Modifier.weight(1f),
            isHeader = true,
        )
        PitcherRecordText(
            text = "삼진",
            modifier = Modifier.weight(1f),
            isHeader = true,
        )
        PitcherRecordText(
            text = "4사구",
            modifier = Modifier.weight(1f),
            isHeader = true,
        )
        PitcherRecordText(
            text = "투구수",
            modifier = Modifier.weight(1f),
            isHeader = true,
        )
    }
}

@Composable
private fun PitcherRecordRow(
    playerName: String,
    result: String,
    innings: String,
    hitsAllowed: Int,
    runsAllowed: Int,
    earnedRuns: Int,
    strikeouts: Int,
    walksAndHbp: Int,
    pitchCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(White)
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(2.4f).padding(start = 6.dp),
        ) {
            Text(
                text = playerName,
                style = PretendardMedium.copy(fontSize = 14.dpToSp),
            )
        }

        PitcherRecordText(
            text = innings,
            modifier = Modifier.weight(1f),
        )
        PitcherRecordText(
            text = hitsAllowed.toString(),
            modifier = Modifier.weight(1f),
        )
        PitcherRecordText(
            text = runsAllowed.toString(),
            modifier = Modifier.weight(1f),
        )
        PitcherRecordText(
            text = earnedRuns.toString(),
            modifier = Modifier.weight(1f),
        )
        PitcherRecordText(
            text = strikeouts.toString(),
            modifier = Modifier.weight(1f),
        )
        PitcherRecordText(
            text = walksAndHbp.toString(),
            modifier = Modifier.weight(1f),
        )
        PitcherRecordText(
            text = pitchCount.toString(),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PitcherRecordText(
    text: String,
    modifier: Modifier = Modifier,
    isHeader: Boolean = false,
) {
    Text(
        text = text,
        style =
            if (isHeader) {
                PretendardRegular.copy(fontSize = 11.dpToSp, textAlign = TextAlign.Center)
            } else {
                PretendardRegular.copy(fontSize = 12.dpToSp, textAlign = TextAlign.Center)
            },
        modifier = modifier,
    )
}

@Preview
@Composable
private fun PitcherRecordTablePreview() {
    PitcherRecordTable()
}
