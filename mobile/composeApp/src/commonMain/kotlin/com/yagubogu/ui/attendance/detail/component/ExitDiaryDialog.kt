package com.yagubogu.ui.attendance.detail.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.yagubogu.ui.common.component.DefaultDialog
import com.yagubogu.ui.common.model.DefaultDialogUiModel

@Composable
fun ExitDiaryDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DefaultDialog(
        dialogUiModel =
            DefaultDialogUiModel(
                title = "저장하지 않고 나갈까요?",
                emoji = "\uD83D\uDEA8",
                message = "변경사항이 저장되지 않아요.",
                negativeText = "취소",
                positiveText = "나가기",
            ),
        onConfirm = onConfirm,
        onCancel = onCancel,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun ExitDiaryDialogPreview() {
    ExitDiaryDialog(
        onConfirm = {},
        onCancel = {},
    )
}
