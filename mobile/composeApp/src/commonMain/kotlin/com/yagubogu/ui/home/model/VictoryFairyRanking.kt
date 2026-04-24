package com.yagubogu.ui.home.model

import org.jetbrains.compose.resources.StringResource

data class VictoryFairyRanking(
    val titleRes: StringResource,
    val labelRes: StringResource,
    val topRankings: List<VictoryFairyItem> = emptyList(),
    val myRanking: VictoryFairyItem = VictoryFairyItem(),
)
