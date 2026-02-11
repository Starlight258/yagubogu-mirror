package com.yagubogu.ui.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.theme.Dimming025
import com.yagubogu.ui.theme.EsamanruBold
import com.yagubogu.ui.theme.EsamanruMedium
import com.yagubogu.ui.theme.Gray700
import com.yagubogu.ui.theme.PretendardBold
import com.yagubogu.ui.theme.PretendardRegular
import com.yagubogu.ui.theme.Primary100
import com.yagubogu.ui.theme.Primary700
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.theme.dpToSp
import com.yagubogu.ui.util.cssShadow
import kotlinx.datetime.LocalTime

@Composable
fun DateCountdown(
    day: Int,
    time: LocalTime,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = White, shape = RoundedCornerShape(12.dp))
                .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.BottomCenter) {
            Box(
                modifier =
                    Modifier
                        .background(color = Primary100)
                        .size(197.dp, 14.dp),
            )
            Text(text = "개막까지 남은 시간", style = EsamanruMedium, fontSize = 24.dpToSp)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "D-${day}",
            style =
                EsamanruBold.copy(
                    fontSize = 48.dpToSp,
                    color = Gray700,
                    shadow = cssShadow(color = Dimming025, offsetY = 4.dp, blur = 4.dp)
                ),
        )
        Spacer(modifier = Modifier.height(16.dp))
        TimeCountdowns(time = time)
    }
}

@Composable
fun TimeCountdowns(
    time: LocalTime,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        TimeCountdown(
            firstNumber = (time.hour / 10).toString(),
            secondNumber = (time.hour % 10).toString(),
            description = "HOURS"
        )
        ColonDivider()
        TimeCountdown(
            firstNumber = (time.minute / 10).toString(),
            secondNumber = (time.minute % 10).toString(),
            description = "MINUTES"
        )
        ColonDivider()
        TimeCountdown(
            firstNumber = (time.second / 10).toString(),
            secondNumber = (time.second % 10).toString(),
            description = "SECONDS"
        )
    }
}

@Composable
private fun TimeCountdown(
    firstNumber: String,
    secondNumber: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier,
        ) {
            DigitBox(number = firstNumber)
            DigitBox(number = secondNumber)
        }
        Text(text = description, style = PretendardRegular, fontSize = 10.dpToSp)
    }
}

@Composable
private fun DigitBox(
    number: String,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(width = 40.dp, height = 56.dp)
            .dropShadow(
                shape = RoundedCornerShape(4.dp),
                shadow = Shadow(
                    radius = 4.dp,
                    color = Dimming025,
                    offset = DpOffset(x = 0.dp, 4.dp)
                )
            )
            .background(Primary700, RoundedCornerShape(4.dp))
    ) {
        Text(
            text = number, style = EsamanruBold.copy(
                fontSize = 30.dpToSp,
                color = White
            )
        )
    }
}

@Composable
private fun ColonDivider(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(56.dp)
            .padding(horizontal = 6.dp)
    ) {
        Text(text = ":", style = PretendardBold.copy(fontSize = 24.dpToSp))
    }
}

@Preview
@Composable
private fun DateCountdownPreview() {
    DateCountdown(day = 24, time = LocalTime(12, 34, 56))
}

@Preview(showBackground = true)
@Composable
private fun DigitBoxPreview() {
    Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
        DigitBox("7")
    }
}

@Preview(showBackground = true)
@Composable
private fun TimeCountdownPreview() {
    Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
        TimeCountdown("5", "9", description = "MINUTES")
    }
}

@Preview(showBackground = true)
@Composable
private fun TimeCountdownsPreview() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        TimeCountdowns(time = LocalTime(12, 34, 56))
    }
}