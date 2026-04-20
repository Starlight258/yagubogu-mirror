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
import com.yagubogu.ui.util.yyyyMMddDayOfWeekFormatter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.attendance_detail_delete
import yagubogu.composeapp.generated.resources.ic_trash

@Composable
fun AttendanceDetailScreen(
    gameId: Long,
    date: LocalDate,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    pagerState: PagerState = rememberPagerState { AttendanceDetailTab.entries.size },
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Gray050),
    ) {
        AttendanceDetailToolbar(
            date = date.format(yyyyMMddDayOfWeekFormatter),
            onBackClick = onBackClick,
            onDeleteClick = {},
        )
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
    date: String,
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DefaultToolbar(
        onBackClick = onBackClick,
        modifier = modifier,
        title = date,
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
        gameId = 0,
        date = LocalDate(2025, 8, 14),
        onBackClick = {},
    )
}
