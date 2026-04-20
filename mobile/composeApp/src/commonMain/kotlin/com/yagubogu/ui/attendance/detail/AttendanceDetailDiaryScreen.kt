package com.yagubogu.ui.attendance.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yagubogu.analytics.AnalyticsLogger
import com.yagubogu.ui.attendance.detail.component.ImagePickerBoxRow
import com.yagubogu.ui.attendance.detail.component.ImageSlider
import com.yagubogu.ui.attendance.detail.model.AttendanceDetailDiaryUiState
import com.yagubogu.ui.attendance.detail.model.DiaryMode
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardBold16
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.PretendardRegular
import com.yagubogu.ui.theme.PretendardRegular12
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.White
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.attendance_detail_diary_edit
import yagubogu.composeapp.generated.resources.attendance_detail_diary_placeholder
import yagubogu.composeapp.generated.resources.attendance_detail_diary_save
import yagubogu.composeapp.generated.resources.attendance_detail_tab_diary
import yagubogu.composeapp.generated.resources.ic_pencil

@Composable
fun AttendanceDetailDiaryScreen(
    modifier: Modifier = Modifier,
    uiState: AttendanceDetailDiaryUiState = AttendanceDetailDiaryUiState(),
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
    ) {
        AttendanceDetailDiaryTitle(showEditButton = uiState.mode == DiaryMode.WRITE)
        Spacer(modifier = Modifier.height(10.dp))

        when (uiState.mode) {
            DiaryMode.READ -> ReadingDiaryPage(uiState = uiState)
            DiaryMode.WRITE -> WritingDiaryPage(uiState = uiState)
        }
    }
}

@Composable
private fun AttendanceDetailDiaryTitle(
    showEditButton: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painterResource(Res.drawable.ic_pencil),
            contentDescription = stringResource(Res.string.attendance_detail_tab_diary),
            tint = Primary500,
        )
        Text(text = stringResource(Res.string.attendance_detail_tab_diary), style = PretendardBold20)

        if (showEditButton) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(Res.string.attendance_detail_diary_edit),
                style = PretendardRegular12,
                color = Gray500,
                textDecoration = TextDecoration.Underline,
            )
        }
    }
}

@Composable
private fun WritingDiaryPage(
    uiState: AttendanceDetailDiaryUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(White, RoundedCornerShape(12.dp))
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        ImagePickerBoxRow()
        DiaryTextField(
            readOnly = false,
            comment = uiState.comment,
            modifier = Modifier.fillMaxWidth().weight(1f),
        )
        DiarySaveButton(onDiarySave = {})
    }
}

@Composable
private fun ReadingDiaryPage(
    uiState: AttendanceDetailDiaryUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(White, RoundedCornerShape(12.dp))
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        ImageSlider(images = uiState.images)
        DiaryTextField(
            readOnly = true,
            comment = uiState.comment,
            modifier = Modifier.fillMaxWidth().weight(1f),
        )
    }
}

@Composable
private fun DiaryTextField(
    readOnly: Boolean,
    comment: String,
    modifier: Modifier = Modifier,
) {
    val state = rememberTextFieldState(initialText = comment)

    BasicTextField(
        modifier = modifier,
        state = state,
        readOnly = readOnly,
        textStyle = PretendardRegular.copy(fontSize = 14.sp),
        decorator = { innerTextField ->
            if (state.text.isEmpty()) {
                Text(
                    text = stringResource(Res.string.attendance_detail_diary_placeholder),
                    style = PretendardRegular.copy(fontSize = 14.sp, color = Gray400),
                )
            }
            innerTextField()
        },
    )
}

@Composable
private fun DiarySaveButton(
    onDiarySave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        modifier = modifier.fillMaxWidth(),
        onClick = {
            onDiarySave()
            AnalyticsLogger.logEvent("diary_save_button")
        },
        colors =
            ButtonDefaults.buttonColors(
                containerColor = Primary500,
                contentColor = White,
            ),
        contentPadding = PaddingValues(16.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.attendance_detail_diary_save),
            style = PretendardBold16,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AttendanceDetailDiaryScreenWritingPagePreview() {
    AttendanceDetailDiaryScreen(uiState = AttendanceDetailDiaryUiState())
}

@Preview(showBackground = true)
@Composable
private fun AttendanceDetailDiaryScreenReadingPagePreview() {
    AttendanceDetailDiaryScreen(
        uiState =
            AttendanceDetailDiaryUiState(
                mode = DiaryMode.READ,
                images = persistentListOf("", "", ""),
                comment =
                    """
                    어쩌구저쩌구 그런데 직관 기록 최대 몇 자로 해야되지? 제한이 있어야 할 거 같긴 한데.. 몇자로 제한함? 백엔드에서 정했겠지? 크림이 알아서 하겠지? 백엔드에서 정했으면 물어봐야 됨. 어쩌구 저쩌구 직관 기록..

                    와! 오늘 이겼다 어쩌구.. 오늘 졌다 어쩌구 이런 걸 작성하는 건가
                    """.trimIndent(),
            ),
    )
}
