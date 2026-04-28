package com.example.numberquest

import androidx.lifecycle.ViewModel
import com.example.numberquest.model.Player
import com.example.numberquest.model.TurnResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

enum class Screen { Setup, Game, History }

/**
 * Holds all in-memory game state. No persistence — state lives for the lifetime
 * of the activity (which is appropriate for a quick party game).
 *
 * Random generation is strictly bounded to integers in [1, 5] inclusive.
 */
class GameViewModel : ViewModel() {

    private val _screen = MutableStateFlow(Screen.Setup)
    val screen: StateFlow<Screen> = _screen.asStateFlow()

    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()

    private val _currentTurnIndex = MutableStateFlow(0)
    val currentTurnIndex: StateFlow<Int> = _currentTurnIndex.asStateFlow()

    /** Result of the current turn, or null while waiting for the player to tap. */
    private val _lastResult = MutableStateFlow<TurnResult?>(null)
    val lastResult: StateFlow<TurnResult?> = _lastResult.asStateFlow()

    private val _history = MutableStateFlow<List<TurnResult>>(emptyList())
    val history: StateFlow<List<TurnResult>> = _history.asStateFlow()

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(true)
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    private var turnCounter = 0
    private val random: Random = Random.Default

    /** Begin a new game with the given roster. */
    fun startGame(players: List<Player>) {
        require(players.size >= 2) { "At least 2 players are required." }
        _players.value = players
        _currentTurnIndex.value = 0
        _lastResult.value = null
        _history.value = emptyList()
        turnCounter = 0
        _screen.value = Screen.Game
    }

    /**
     * Generate two random integers in [1, 5] for the current player and record
     * them as the latest result and as a history entry.
     */
    fun drawNumbers(): TurnResult {
        val player = _players.value[_currentTurnIndex.value]
        // Random.nextInt(from, until) — until is EXCLUSIVE, so 1..6 yields 1..5.
        val a = random.nextInt(1, 6)
        val b = random.nextInt(1, 6)
        turnCounter += 1
        val result = TurnResult(
            turnNumber = turnCounter,
            playerId = player.id,
            playerName = player.name,
            playerColor = player.color,
            first = a,
            second = b
        )
        _lastResult.value = result
        _history.value = _history.value + result
        return result
    }

    /** Advance to the next player and clear the current reveal. */
    fun nextTurn() {
        val players = _players.value
        if (players.isEmpty()) return
        _currentTurnIndex.value = (_currentTurnIndex.value + 1) % players.size
        _lastResult.value = null
    }

    fun goTo(target: Screen) {
        _screen.value = target
    }

    fun resetGame() {
        _screen.value = Screen.Setup
        _players.value = emptyList()
        _currentTurnIndex.value = 0
        _lastResult.value = null
        _history.value = emptyList()
        turnCounter = 0
    }

    fun toggleSound() { _soundEnabled.value = !_soundEnabled.value }
    fun toggleHaptics() { _hapticsEnabled.value = !_hapticsEnabled.value }
}
