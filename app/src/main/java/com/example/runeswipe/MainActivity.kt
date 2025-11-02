// ─────────────────────────────────────────────────────────────────────────────
// app/src/main/java/com/example/runeswipe/MainActivity.kt
// ─────────────────────────────────────────────────────────────────────────────
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.runeswipe

import android.app.Activity
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
        RuneModel.load(this)
        super.onCreate(savedInstanceState)
        setContent {
            RuneTheme {
                val nav = rememberNavController()

                // 🔹 The player persists across screens
                val player = remember { Player.default("You") }

                Scaffold { pad ->
                    NavHost(
                        navController = nav,
                        startDestination = "menu",
                        modifier = Modifier.padding(pad)
                    ) {
                        composable("menu") { MainMenuScreen(nav) }

                        composable("battle") {
                            // 🔹 The enemy is created fresh for each battle
                            val enemy = remember { Player.default("Rival") }
                            BattleScreen(player = player, enemy = enemy)
                        }

                        composable("wizard") { WizardScreen() }

                        composable("tome") {
                            TomeScreen(player)
                        }
                    }
                }
            }
        }
    }
}
