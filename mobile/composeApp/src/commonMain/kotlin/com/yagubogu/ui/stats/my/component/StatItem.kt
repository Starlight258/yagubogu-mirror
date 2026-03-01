package com.yagubogu.ui.stats.my.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import yagubogu.composeapp.generated.resources.Res
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardRegular12
import com.yagubogu.ui.theme.PretendardSemiBold20
import yagubogu.composeapp.generated.resources.stats_no_data

@Composable
fun StatItem(
    title: String,
    value: String?,
    modifier: Modifier = Modifier,
    emoji: String? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        if (emoji != null) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }
        Text(
            text = value ?: stringResource(Res.string.stats_no_data),
            style = PretendardSemiBold20,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, style = PretendardRegular12, color = Gray500)
    }
}
