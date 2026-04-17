package com.yagubogu.ui.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yagubogu.analytics.AnalyticsLogger
import com.yagubogu.ui.common.component.profile.ProfileImage
import com.yagubogu.ui.home.model.VictoryFairyItem
import com.yagubogu.ui.home.model.VictoryFairyRanking
import com.yagubogu.ui.theme.Bronze
import com.yagubogu.ui.theme.Gold
import com.yagubogu.ui.theme.Gray100
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.theme.PretendardMedium12
import com.yagubogu.ui.theme.PretendardRegular
import com.yagubogu.ui.theme.PretendardRegular12
import com.yagubogu.ui.theme.PretendardRegular16
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.PretendardSemiBold16
import com.yagubogu.ui.theme.Silver
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.theme.dpToSp
import com.yagubogu.ui.util.BalloonTooltip
import com.yagubogu.ui.util.formatOneDecimal
import com.yagubogu.ui.util.noRippleClickable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.all_fan
import yagubogu.composeapp.generated.resources.all_show_more
import yagubogu.composeapp.generated.resources.home_user_profile
import yagubogu.composeapp.generated.resources.home_victory_fairy_my_nickname
import yagubogu.composeapp.generated.resources.home_victory_fairy_ranking
import yagubogu.composeapp.generated.resources.home_victory_fairy_score
import yagubogu.composeapp.generated.resources.home_victory_fairy_score_format
import yagubogu.composeapp.generated.resources.home_victory_fairy_tooltip
import yagubogu.composeapp.generated.resources.ic_arrow_right
import yagubogu.composeapp.generated.resources.ic_info
import yagubogu.composeapp.generated.resources.ic_medal_first
import yagubogu.composeapp.generated.resources.ic_medal_second
import yagubogu.composeapp.generated.resources.ic_medal_third

@Composable
fun VictoryFairyRanking(
    ranking: VictoryFairyRanking,
    onRankingItemClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier =
            modifier
                .fillMaxWidth()
                .background(White, RoundedCornerShape(12.dp))
                .padding(20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.home_victory_fairy_ranking),
                    style = PretendardBold20,
                )
                BalloonTooltip(
                    text = stringResource(Res.string.home_victory_fairy_tooltip),
                ) { showTooltip ->
                    Icon(
                        painter = painterResource(Res.drawable.ic_info),
                        contentDescription = null,
                        tint = Gray300,
                        modifier =
                            Modifier
                                .padding(horizontal = 8.dp)
                                .noRippleClickable {
                                    showTooltip()
                                    AnalyticsLogger.logEvent("tooltip_victory_fairy_ranking")
                                },
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(Res.string.all_show_more),
                    style = PretendardMedium.copy(fontSize = 14.sp, color = Gray400),
                )
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(Res.drawable.ic_arrow_right),
                    contentDescription = null,
                    tint = Gray400,
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(color = Gray100)
                        .padding(horizontal = 8.dp, vertical = 10.dp),
            ) {
                Text(
                    text = stringResource(Res.string.home_user_profile),
                    style = PretendardRegular12,
                )
                Text(
                    text = stringResource(Res.string.home_victory_fairy_score),
                    style = PretendardRegular12,
                )
            }

            VictoryFairyRankingItem(
                item = ranking.myRanking,
                onClick = onRankingItemClick,
                isMyRanking = true,
            )
            HorizontalDivider(color = Gray300, thickness = 0.4.dp)

            ranking.topRankings.forEach { item: VictoryFairyItem ->
                VictoryFairyRankingItem(
                    item = item,
                    onClick = onRankingItemClick,
                )
            }
        }
    }
}

@Composable
private fun VictoryFairyRankingItem(
    item: VictoryFairyItem,
    onClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    isMyRanking: Boolean = false,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(8.dp)
                .noRippleClickable {
                    onClick(item.memberId)
                    AnalyticsLogger.logEvent("member_profile")
                },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.rank.toString(),
            style = PretendardRegular.copy(fontSize = 14.dpToSp, color = Gray500),
            textAlign = TextAlign.Center,
            modifier = Modifier.width(20.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))

        ProfileImage(
            imageUrl = item.profileImageUrl,
            modifier = Modifier.size(36.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1.0f),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text =
                        if (isMyRanking) {
                            stringResource(
                                Res.string.home_victory_fairy_my_nickname,
                                item.nickname,
                            )
                        } else {
                            item.nickname
                        },
                    style = PretendardSemiBold16,
                    maxLines = 1,
                    overflow = TextOverflow.MiddleEllipsis,
                )
                if (item.rank in 1..3) {
                    Spacer(modifier = Modifier.width(6.dp))
                    VictoryFairyMedal(
                        rank = item.rank,
                        modifier = Modifier.height(14.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(Res.string.all_fan, item.teamName),
                style = PretendardMedium12.copy(color = Gray400),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text =
                stringResource(
                    Res.string.home_victory_fairy_score_format,
                    item.score.formatOneDecimal(),
                ),
            style = PretendardRegular16,
        )
    }
}

@Composable
private fun VictoryFairyMedal(
    rank: Int,
    modifier: Modifier = Modifier,
) {
    val (iconRes: DrawableResource, color: Color) =
        when (rank) {
            1 -> Res.drawable.ic_medal_first to Gold
            2 -> Res.drawable.ic_medal_second to Silver
            3 -> Res.drawable.ic_medal_third to Bronze
            else -> null
        } ?: return

    Icon(
        painter = painterResource(iconRes),
        contentDescription = null,
        tint = color,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun VictoryFairyRankingPreview() {
    VictoryFairyRanking(
        ranking = VICTORY_FAIRY_RANKING,
        onRankingItemClick = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun VictoryFairyRankingItemPreview() {
    VictoryFairyRankingItem(item = VICTORY_FAIRY_RANKING_ITEM, onClick = {})
}
