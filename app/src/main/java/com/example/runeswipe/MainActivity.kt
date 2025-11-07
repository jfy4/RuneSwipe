// ─────────────────────────────────────────────────────────────────────────────
// app/src/main/java/com/example/runeswipe/MainActivity.kt
// ─────────────────────────────────────────────────────────────────────────────
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.runeswipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.example.runeswipe.model.*
import com.example.runeswipe.ui.*
import com.example.runeswipe.ui.theme.RuneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RuneModel.load(this)

        setContent {
            RuneTheme {
                val nav = rememberNavController()

                // 🔹 Try to load the saved player; if none, create a new one
                val context = this
                var player by remember {
                    mutableStateOf(
                        PlayerRepository.load(context)
                            ?: Player.default("You").also { PlayerRepository.save(context, it) }
                    )
                }

                // 🔹 Automatically save whenever player data changes
                LaunchedEffect(player) {
                    PlayerRepository.save(context, player)
                }

                Scaffold { pad ->
                    NavHost(
                        navController = nav,
                        startDestination = "menu",
                        modifier = Modifier.padding(pad)
                    ) {
                        composable("menu") { MainMenuScreen(nav) }

                        composable("battle") {
                            // New enemy each battle
                            val enemy = remember { Player.default("Rival") }
                            BattleScreen(player = player, enemy = enemy)
                        }

                        composable("wizard") {
                            WizardScreen()
                        }

                        composable("tome") {
                            TomeScreen(player)
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // 🔹 Save player progress when app goes to background
        PlayerRepository.save(this, PlayerRepository.load(this) ?: Player.default("You"))
    }
}
