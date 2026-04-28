package com.example.numberquest.model

import androidx.compose.ui.graphics.Color

/**
 * Represents a player in the game.
 *
 * @param id Stable identifier (matches index at setup time).
 * @param name Display name.
 * @param color UI color associated with this player throughout the game.
 */
data class Player(
    val id: Int,
    val name: String,
    val color: Color
)
