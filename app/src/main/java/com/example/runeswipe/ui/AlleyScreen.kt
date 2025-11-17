package com.example.runeswipe.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AlleyScreen() {
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Items", "Clothing", "Artifacts")

    Column(Modifier.fillMaxSize().padding(16.dp)) {

        TabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = tabIndex == index,
                    onClick = { tabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        when (tabIndex) {
            0 -> Text("Item inventory coming soon...")
            1 -> Text("Clothing shop coming soon...")
            2 -> Text("Artifacts and relics coming soon...")
        }
    }
}
