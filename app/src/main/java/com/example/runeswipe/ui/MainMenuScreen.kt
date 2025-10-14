package com.example.runeswipe.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun MainMenuScreen(nav: NavController) {
    val activity = LocalContext.current as? android.app.Activity
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "RuneSwipe",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(24.dp))

            Button(onClick = { nav.navigate("battle") }, Modifier.width(160.dp)) {
                Text("Battle")
            }
            Button(onClick = { nav.navigate("wizard") }, Modifier.width(160.dp)) {
                Text("Wizard")
            }
            Button(onClick = { nav.navigate("tome") }, Modifier.width(160.dp)) {
                Text("Tome")
            }
            Button(
                onClick = { activity?.finish() },
                Modifier.width(160.dp),
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.errorContainer)
            ) {
                Text("Exit")
            }
        }
    }
}
