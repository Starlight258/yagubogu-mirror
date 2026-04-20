package com.yagubogu.ui.attendance.detail.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.PretendardRegular12
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.attendance_detail_image_picker
import yagubogu.composeapp.generated.resources.ic_camera

@Composable
fun ImagePickerBox(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .aspectRatio(1f)
                .border(1.dp, Gray400, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(painterResource(Res.drawable.ic_camera), contentDescription = null, tint = Gray400)
            Text(text = stringResource(Res.string.attendance_detail_image_picker), style = PretendardRegular12, color = Gray400)
        }
    }
}

@Composable
fun ImagePickerBoxRow(modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        ImagePickerBox(modifier = Modifier.weight(1f))
        ImagePickerBox(modifier = Modifier.weight(1f))
        ImagePickerBox(modifier = Modifier.weight(1f))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
private fun ImagePickerBoxPreview() {
    Surface(modifier = Modifier.size(200.dp).padding(20.dp)) {
        ImagePickerBox()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
private fun ImagePickerBoxRowPreview() {
    Surface(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
        ImagePickerBoxRow()
    }
}
