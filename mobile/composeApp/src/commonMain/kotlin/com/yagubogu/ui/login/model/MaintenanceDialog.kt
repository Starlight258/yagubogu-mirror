package com.yagubogu.ui.login.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.yagubogu.ui.common.component.DefaultDialog
import com.yagubogu.ui.common.model.DefaultDialogUiModel
import com.yagubogu.ui.home.model.MaintenanceInfo

@Composable
fun MaintenanceDialog(
    onConfirm: () -> Unit,
    maintenanceInfo: MaintenanceInfo,
    modifier: Modifier = Modifier,
) {
    val dialogUiModel =
        DefaultDialogUiModel(
            title = maintenanceInfo.title ?: "",
            emoji = maintenanceInfo.emoji,
            message = maintenanceInfo.message,
        )
    DefaultDialog(
        dialogUiModel = dialogUiModel,
        onConfirm = onConfirm,
        onCancel = {},
        modifier = modifier,
    )
}

val MAINTENANCE_INFO =
    MaintenanceInfo(
        id = 1,
        remoteIsShow = true,
        shouldShowPopup = true,
        emoji = "🚧",
        title = "야구보구 점검중 테스트",
        message = "점검중입니다. 잠시만 기다려주세요.",
        skippableDays = 3,
    )

@Preview
@Composable
private fun MaintenanceDialogPreview() {
    MaintenanceDialog(
        onConfirm = {},
        maintenanceInfo = MAINTENANCE_INFO,
        modifier = Modifier,
    )
}
