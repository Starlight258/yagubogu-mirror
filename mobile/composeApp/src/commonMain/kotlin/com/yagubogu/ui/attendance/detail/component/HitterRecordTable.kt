package com.yagubogu.ui.attendance.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.theme.PretendardRegular
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.Primary050
import com.yagubogu.ui.theme.Primary900
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.theme.dpToSp

@Composable
fun HitterRecordTable(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        HorizontalDivider(color = Gray300, thickness = 0.4.dp)
        HitterRecordHeader()
        HorizontalDivider(color = Gray300, thickness = 0.4.dp)
        HitterRecordRow(
            battingOrder = 1,
            playerName = "민우가",
            position = "1루수",
            atBats = 10,
            hits = 3,
            rbi = 1,
            runs = 5,
        )
        HorizontalDivider(color = Gray300, thickness = 0.4.dp)
        HitterRecordRow(
            battingOrder = 2,
            playerName = "김두리",
            position = "2루수",
            atBats = 10,
            hits = 3,
            rbi = 1,
            runs = 5,
        )
        HorizontalDivider(color = Gray300, thickness = 0.4.dp)
    }
}

@Composable
private fun HitterRecordHeader(modifier: Modifier = Modifier) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(Primary050)
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "타자명",
            style = PretendardSemiBold.copy(fontSize = 14.dpToSp, color = Primary900),
            modifier = Modifier.weight(2.8f).padding(start = 8.dp),
        )

        HitterRecordText(
            text = "타수",
            modifier = Modifier.weight(1f),
        )
        HitterRecordText(
            text = "안타",
            modifier = Modifier.weight(1f),
        )
        HitterRecordText(
            text = "타점",
            modifier = Modifier.weight(1f),
        )
        HitterRecordText(
            text = "득점",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HitterRecordRow(
    battingOrder: Int,
    playerName: String,
    position: String,
    atBats: Int,
    hits: Int,
    rbi: Int,
    runs: Int,
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
            modifier = Modifier.weight(2.8f),
        ) {
            Text(
                text = battingOrder.toString(),
                style = PretendardRegular.copy(fontSize = 12.dpToSp, textAlign = TextAlign.Center),
                modifier = Modifier.width(20.dp),
            )
            Text(
                text = playerName,
                style = PretendardMedium.copy(fontSize = 14.dpToSp),
            )
            Text(
                text = position,
                style = PretendardRegular.copy(fontSize = 10.dpToSp, color = Gray500),
                modifier = Modifier.weight(1f),
            )
        }

        HitterRecordText(
            text = atBats.toString(),
            modifier = Modifier.weight(1f),
        )
        HitterRecordText(
            text = hits.toString(),
            modifier = Modifier.weight(1f),
        )
        HitterRecordText(
            text = rbi.toString(),
            modifier = Modifier.weight(1f),
        )
        HitterRecordText(
            text = runs.toString(),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HitterRecordText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = PretendardRegular.copy(fontSize = 12.dpToSp, textAlign = TextAlign.Center),
        modifier = modifier,
    )
}

@Preview
@Composable
private fun HitterRecordTablePreview() {
    HitterRecordTable()
}
