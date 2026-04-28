package com.example.numberquest.model

import androidx.compose.ui.graphics.Color

/**
 * Result of a single turn: two random integers in the range [1, 5] revealed
 * to the player who took the turn.
 */
data class TurnResult(
    val turnNumber: Int,
    val playerId: Int,
    val playerName: String,
    val playerColor: Color,
    val first: Int,
    val second: Int
) {
    val total: Int get() = first + second
}
