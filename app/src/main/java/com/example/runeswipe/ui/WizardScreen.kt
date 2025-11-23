@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.runeswipe.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*      // <-- THIS covers remember + mutableStateOf
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

            // ————— INVENTORY TABS —————
            Spacer(Modifier.height(16.dp))
            InventoryTabs(player)
        }
    }
}

@Composable
fun InventoryTabs(player: Player) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Items", "Gear")

    Column {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> ItemInventoryView(player)
            1 -> GearInventoryView(player)
        }
    }
}

@Composable
fun ItemInventoryView(player: Player) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (player.items.isEmpty()) {
            Text("No items.", style = MaterialTheme.typography.bodyMedium)
        } else {
            player.items.forEach { item ->
                Text("${item.name} x${item.quantity}")
            }
        }
    }
}

@Composable
fun GearInventoryView(player: Player) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (player.gear.isEmpty()) {
            Text("No gear equipped.", style = MaterialTheme.typography.bodyMedium)
        } else {
            player.gear.forEach { g ->
                Text("${g.slot}: ${g.name}")
            }
        }
    }
}
