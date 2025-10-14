@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.runeswipe.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WizardScreen() {
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
            Text("Name: You", style = MaterialTheme.typography.titleLarge)
            Text("Level: 1")
            Text("Life: 30")
            Text("Strength: 5")
            Text("Defense: 5")
            Text("Constitution: 5")
            Text("Speed: 5")
            Text("Dexterity: 5")
            Spacer(Modifier.height(16.dp))
            Text("Items:", style = MaterialTheme.typography.titleMedium)
            Text("- Apprentice Robes")
            Text("- Wooden Wand")
            Text("- Pointy Hat")
        }
    }
}
