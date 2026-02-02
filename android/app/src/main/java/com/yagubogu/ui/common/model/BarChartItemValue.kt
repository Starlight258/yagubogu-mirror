package com.yagubogu.ui.common.model

import androidx.compose.ui.graphics.Color
import com.yagubogu.ui.theme.Primary500

data class BarChartItemValue(
    val strokeColor: Color,
    val titleLabel: BarChartLabel?,
    val amount: Int,
    val dataLabel: BarChartLabel?,
)
