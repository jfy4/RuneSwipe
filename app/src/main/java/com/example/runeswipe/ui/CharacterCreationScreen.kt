package com.example.runeswipe.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.runeswipe.model.Player

@Composable
fun CharacterCreationScreen(onDone: (Player) -> Unit) {
    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("male") }
    var eyeColor by remember { mutableStateOf("") }
    var hairColor by remember { mutableStateOf("") }

    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Create Your Character", style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            // Gender bubble toggle (will later be "body type")
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FilterChip(
                    selected = gender == "male",
                    onClick = { gender = "male" },
                    label = { Text("Male") }
                )
                FilterChip(
                    selected = gender == "female",
                    onClick = { gender = "female" },
                    label = { Text("Female") }
                )
            }

            OutlinedTextField(
                value = eyeColor,
                onValueChange = { eyeColor = it },
                label = { Text("Eye Color") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = hairColor,
                onValueChange = { hairColor = it },
                label = { Text("Hair Color") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val player = Player(
                            name = name,
                            gender = gender,
                            eyeColor = eyeColor,
                            hairColor = hairColor
                        )
                        onDone(player)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Character")
            }
        }
    }
}
