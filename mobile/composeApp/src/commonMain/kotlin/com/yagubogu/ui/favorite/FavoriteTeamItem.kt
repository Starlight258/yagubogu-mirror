package com.yagubogu.ui.favorite

import com.yagubogu.domain.model.Team
import com.yagubogu.ui.util.emoji
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteTeamItem(
    val team: Team,
    val emoji: String,
) {
    companion object {
        fun of(team: Team): FavoriteTeamItem {
            val emoji: String = team.emoji
            return FavoriteTeamItem(team, emoji)
        }
    }
}
