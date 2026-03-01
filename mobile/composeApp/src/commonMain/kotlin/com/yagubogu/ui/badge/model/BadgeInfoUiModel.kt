package com.yagubogu.ui.badge.model

import android.os.Parcelable
import kotlinx.datetime.LocalDate
import kotlinx.parcelize.Parcelize

@Parcelize
data class BadgeInfoUiModel(
    val badge: BadgeUiModel,
    val description: String,
    val achievedRate: Int,
    val achievedAt: LocalDate?,
    val progressRate: Double,
) : Parcelable
