package com.yagubogu.ui.attendance.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.yagubogu.ui.attendance.detail.component.AttendanceDetailTabRow
import com.yagubogu.ui.attendance.detail.model.AttendanceDetailTab
import com.yagubogu.ui.common.component.DefaultToolbar
import com.yagubogu.ui.theme.Gray050
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.attendance_detail_delete
import yagubogu.composeapp.generated.resources.ic_trash

@Composable
fun AttendanceDetailScreen(
    pagerState: PagerState = rememberPagerState { AttendanceDetailTab.entries.size },
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Gray050),
    ) {
        AttendanceDetailToolbar(onDeleteClick = {})
        AttendanceDetailTabRow(pagerState)
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (page) {
                AttendanceDetailTab.GAME_RECORD.ordinal -> Unit
                AttendanceDetailTab.DIARY.ordinal -> AttendanceDetailDiaryScreen()
            }
        }
    }
}

@Composable
private fun AttendanceDetailToolbar(
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DefaultToolbar(
        onBackClick = {},
        modifier = modifier,
        title = "2025.08.14 (화)",
        actions = {
            IconButton(onClick = onDeleteClick) {
                Icon(
                    painter = painterResource(Res.drawable.ic_trash),
                    contentDescription = stringResource(Res.string.attendance_detail_delete),
                    modifier = Modifier.padding(),
                )
            }
        },
    )
}

@Preview
@Composable
private fun AttendanceDetailScreenDiaryTabPreview(modifier: Modifier = Modifier) {
    AttendanceDetailScreen(
        pagerState = rememberPagerState(initialPage = AttendanceDetailTab.DIARY.ordinal) { AttendanceDetailTab.entries.size },
        modifier = modifier,
    )
}
