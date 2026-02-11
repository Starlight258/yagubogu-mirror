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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.delay

@Composable
fun OpeningCountdown(
    leftTime: Long,
    modifier: Modifier = Modifier,
) {
    var leftTime by remember { mutableLongStateOf(leftTime) }
    val days: Long by remember { derivedStateOf { leftTime / (60 * 60 * 24) } }
    val hours: Long by remember { derivedStateOf { (leftTime % (60 * 60 * 24)) / (60 * 60) } }
    val minutes: Long by remember { derivedStateOf { (leftTime % (60 * 60)) / 60 } }
    val seconds: Long by remember { derivedStateOf { leftTime % 60 } }

    LaunchedEffect(Unit) {
        while (leftTime > 0L) {
            delay(1000)
            leftTime--
        }
    }

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
            text = "D-$days",
            style =
                EsamanruBold.copy(
                    fontSize = 48.dpToSp,
                    color = Gray700,
                    shadow = cssShadow(color = Dimming025, offsetY = 4.dp, blur = 4.dp),
                ),
        )
        Spacer(modifier = Modifier.height(16.dp))
        TimeCountdowns(hours = hours, minutes = minutes, seconds = seconds)
    }
}

@Composable
fun TimeCountdowns(
    hours: Long,
    minutes: Long,
    seconds: Long,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        TimeCountdown(
            tens = (hours / 10).toString(),
            units = (hours % 10).toString(),
            description = "HOURS",
        )
        ColonDivider()
        TimeCountdown(
            tens = (minutes / 10).toString(),
            units = (minutes % 10).toString(),
            description = "MINUTES",
        )
        ColonDivider()
        TimeCountdown(
            tens = (seconds / 10).toString(),
            units = (seconds % 10).toString(),
            description = "SECONDS",
        )
    }
}

/**
 * @param tens 십의 자리
 * @param units 일의 자리
 * @param description 설명 ex) "HOURS"
 */
@Composable
private fun TimeCountdown(
    tens: String,
    units: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier,
        ) {
            DigitBox(number = tens)
            DigitBox(number = units)
        }
        Text(text = description, style = PretendardRegular, fontSize = 10.dpToSp)
    }
}

@Composable
private fun DigitBox(
    number: String,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .size(width = 40.dp, height = 56.dp)
                .dropShadow(
                    shape = RoundedCornerShape(4.dp),
                    shadow =
                        Shadow(
                            radius = 4.dp,
                            color = Dimming025,
                            offset = DpOffset(x = 0.dp, 4.dp),
                        ),
                ).background(Primary700, RoundedCornerShape(4.dp)),
    ) {
        Text(
            text = number,
            style =
                EsamanruBold.copy(
                    fontSize = 30.dpToSp,
                    color = White,
                ),
        )
    }
}

@Composable
private fun ColonDivider(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .height(56.dp)
                .padding(horizontal = 6.dp),
    ) {
        Text(text = ":", style = PretendardBold.copy(fontSize = 24.dpToSp))
    }
}

@Preview
@Composable
private fun OpeningCountdownPreview() {
    OpeningCountdown(leftTime = 1_000_000L)
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
        TimeCountdowns(hours = 12, minutes = 34, seconds = 56)
    }
}
