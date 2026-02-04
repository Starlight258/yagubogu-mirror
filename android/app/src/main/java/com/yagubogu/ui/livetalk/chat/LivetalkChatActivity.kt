package com.yagubogu.ui.livetalk.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.yagubogu.ui.theme.YaguBoguTheme
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class LivetalkChatActivity : ComponentActivity() {
    private val viewModel: LivetalkChatViewModel by viewModel {
        val gameId = intent.getLongExtra(KEY_GAME_ID, 1L)
        val isVerified = intent.getBooleanExtra(KEY_IS_VERIFIED, false)
        parametersOf(gameId, isVerified)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YaguBoguTheme {
                LivetalkChatScreen(
                    viewModel = viewModel,
                    onBackClick = { finish() },
                )
            }
        }
    }

    companion object {
        private const val KEY_GAME_ID = "gameId"
        private const val KEY_IS_VERIFIED = "isVerified"

        fun newIntent(
            context: Context,
            gameId: Long,
            isVerified: Boolean,
        ): Intent =
            Intent(
                context,
                LivetalkChatActivity::class.java,
            ).apply {
                putExtra(KEY_GAME_ID, gameId)
                putExtra(KEY_IS_VERIFIED, isVerified)
            }
    }
}
