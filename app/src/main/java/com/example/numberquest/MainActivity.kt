package com.example.numberquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.numberquest.ui.GameScreen
import com.example.numberquest.ui.HistoryScreen
import com.example.numberquest.ui.SetupScreen
import com.example.numberquest.ui.theme.NumberQuestTheme

class MainActivity : ComponentActivity() {

    private val vm: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NumberQuestTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppRoot(vm)
                }
            }
        }
    }
}

@Composable
private fun AppRoot(vm: GameViewModel) {
    val screen by vm.screen.collectAsState()

    when (screen) {
        Screen.Setup -> {
            SetupScreen(onStart = { players -> vm.startGame(players) })
        }

        Screen.Game -> {
            BackHandler { vm.resetGame() }
            GameScreen(vm = vm)
        }

        Screen.History -> {
            BackHandler { vm.goTo(Screen.Game) }
            HistoryScreen(vm = vm)
        }
    }
}
