package com.yagubogu.ui.stats.my.component

sealed interface StatItemValue {
    data object Loading : StatItemValue
    data object NoData : StatItemValue
    data class Data(val text: String) : StatItemValue
}