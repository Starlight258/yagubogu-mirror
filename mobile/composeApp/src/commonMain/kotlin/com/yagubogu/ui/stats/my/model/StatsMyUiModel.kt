package com.yagubogu.ui.stats.my.model

import com.yagubogu.ui.stats.my.component.StatItemValue

data class StatsMyUiModel(
    val winCount: Int = 0,
    val drawCount: Int = 0,
    val loseCount: Int = 0,
    val totalCount: Int = 0,
    val winningPercentage: Float = 0f,
    val myTeam: String? = null,
    val luckyStadium: String? = null,
) {
    val etcPercentage: Float get() = FULL_PERCENTAGE - winningPercentage

    companion object {
        private const val FULL_PERCENTAGE = 100f
    }
}

fun StatsMyUiModel?.toStatItemValue(
    propertySelector: (StatsMyUiModel) -> String?
): StatItemValue {
    val model = this ?: return StatItemValue.Loading
    val value = propertySelector(model)
    return if (value != null) StatItemValue.Data(value) else StatItemValue.NoData
}