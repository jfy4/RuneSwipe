@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.runeswipe.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.runeswipe.model.Player

@Composable
fun WizardScreen(player: Player) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Your Wizard") }) }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ————— BASIC INFO —————
            Text("Name: ${player.name}", style = MaterialTheme.typography.titleLarge)
            Text("Gender: ${player.gender}")
            Text("Eye Color: ${player.eyeColor}")
            Text("Hair Color: ${player.hairColor}")

            Spacer(Modifier.height(12.dp))

            // ————— STATS —————
            Text("Level: ${player.level}")
            Text("XP: ${player.xp} / ${player.level * 100}")
            Spacer(Modifier.height(8.dp))

            Text("Life: ${player.stats.life}")
            Text("Strength: ${player.stats.strength}")
            Text("Defense: ${player.stats.defense}")
            Text("Constitution: ${player.stats.constitution}")
            Text("Speed: ${player.stats.speed}")
            Text("Dexterity: ${player.stats.dexterity}")

            Spacer(Modifier.height(16.dp))

            // TEMP placeholder for future inventory
            Text("Items:", style = MaterialTheme.typography.titleMedium)
            Text("- Apprentice Robes")
            Text("- Wooden Wand")
            Text("- Pointy Hat")
        }
    }
}
