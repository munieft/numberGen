package com.example.numberquest.model

import androidx.compose.ui.graphics.Color

/**
 * Curated palette of distinct, accessible colors for players to choose from.
 * Each color is paired with a friendly display name.
 */
object PlayerPalette {
    data class Swatch(val name: String, val color: Color)

    val swatches: List<Swatch> = listOf(
        Swatch("Crimson", Color(0xFFE53935)),
        Swatch("Tangerine", Color(0xFFFB8C00)),
        Swatch("Sunshine", Color(0xFFF9A825)),
        Swatch("Emerald", Color(0xFF2E7D32)),
        Swatch("Teal", Color(0xFF00897B)),
        Swatch("Sky", Color(0xFF1E88E5)),
        Swatch("Indigo", Color(0xFF3949AB)),
        Swatch("Violet", Color(0xFF8E24AA)),
        Swatch("Magenta", Color(0xFFD81B60)),
        Swatch("Slate", Color(0xFF455A64))
    )

    val colors: List<Color> get() = swatches.map { it.color }
}
