package com.example.numberquest.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.numberquest.model.Player
import com.example.numberquest.model.PlayerPalette

private const val MIN_PLAYERS = 2
private const val MAX_PLAYERS = 8

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(onStart: (List<Player>) -> Unit) {
    val palette = remember { PlayerPalette.swatches }

    var playerCount by rememberSaveable { mutableIntStateOf(2) }
    val names = rememberSaveable(
        saver = androidx.compose.runtime.saveable.listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) { mutableStateListOf("Player 1", "Player 2") }
    val colorIndices = rememberSaveable(
        saver = androidx.compose.runtime.saveable.listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) { mutableStateListOf(0, 5) }

    fun resize(newCount: Int) {
        while (names.size < newCount) {
            val n = names.size + 1
            names.add("Player $n")
            // Pick the first unused palette index
            val taken = colorIndices.toSet()
            val nextColor = palette.indices.firstOrNull { it !in taken } ?: ((n - 1) % palette.size)
            colorIndices.add(nextColor)
        }
        while (names.size > newCount) {
            names.removeAt(names.lastIndex)
            colorIndices.removeAt(colorIndices.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Number Quest",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Add players, pick colors, start the game.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Players", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Between $MIN_PLAYERS and $MAX_PLAYERS",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalIconButton(
                            onClick = {
                                if (playerCount > MIN_PLAYERS) {
                                    playerCount -= 1
                                    resize(playerCount)
                                }
                            },
                            enabled = playerCount > MIN_PLAYERS
                        ) { Icon(Icons.Default.Remove, contentDescription = "Decrease") }

                        Text(
                            text = "$playerCount",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        FilledTonalIconButton(
                            onClick = {
                                if (playerCount < MAX_PLAYERS) {
                                    playerCount += 1
                                    resize(playerCount)
                                }
                            },
                            enabled = playerCount < MAX_PLAYERS
                        ) { Icon(Icons.Default.Add, contentDescription = "Increase") }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(playerCount) { idx ->
                    PlayerSetupCard(
                        index = idx,
                        name = names[idx],
                        onNameChange = { names[idx] = it },
                        colorIndex = colorIndices[idx],
                        onColorChange = { newIdx ->
                            // Prevent two players sharing the same color
                            if (newIdx !in colorIndices.filterIndexed { i, _ -> i != idx }) {
                                colorIndices[idx] = newIdx
                            }
                        },
                        palette = palette,
                        usedColorIndices = colorIndices.toList()
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            val canStart = (0 until playerCount).all { names[it].isNotBlank() } &&
                colorIndices.toSet().size == playerCount
            Button(
                onClick = {
                    val players = (0 until playerCount).map { i ->
                        Player(
                            id = i,
                            name = names[i].trim().ifEmpty { "Player ${i + 1}" },
                            color = palette[colorIndices[i]].color
                        )
                    }
                    onStart(players)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = canStart,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Start Game", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerSetupCard(
    index: Int,
    name: String,
    onNameChange: (String) -> Unit,
    colorIndex: Int,
    onColorChange: (Int) -> Unit,
    palette: List<PlayerPalette.Swatch>,
    usedColorIndices: List<Int>
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(palette[colorIndex].color)
                        .border(2.dp, Color.White, CircleShape)
                )
                Spacer(Modifier.width(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Player ${index + 1}") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(palette) { swatch ->
                    val i = palette.indexOf(swatch)
                    val isSelected = i == colorIndex
                    val isTakenByOther = i in usedColorIndices && !isSelected
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(swatch.color)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onSurface
                                else
                                    Color.White.copy(alpha = 0.6f),
                                shape = CircleShape
                            )
                            .alpha(if (isTakenByOther) 0.35f else 1f)
                            .clickable(enabled = !isTakenByOther) { onColorChange(i) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = palette[colorIndex].name,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
