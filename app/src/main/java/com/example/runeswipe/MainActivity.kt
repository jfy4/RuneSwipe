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
		var player by remember { mutableStateOf<Player?>(null) }
		var hasLoaded by remember { mutableStateOf(false) }

		LaunchedEffect(Unit) {
		    val saved = PlayerRepository.load(context)
		    if (saved != null) {
			player = saved
			hasLoaded = true
		    } else {
			// No saved player → go to creation screen
			hasLoaded = true
			nav.navigate("createCharacter")
		    }
		}
                // var player by remember {
                //     mutableStateOf(
                //         PlayerRepository.load(context)
                //             ?: Player.default("You").also { PlayerRepository.save(context, it) }
                //     )
                // }

                // 🔹 Automatically save whenever player data changes
		LaunchedEffect(player) {
		    player?.let { PlayerRepository.save(context, it) }
		}
                // LaunchedEffect(player) {
                //     PlayerRepository.save(context, player)
                // }

                Scaffold { pad ->
                    NavHost(
                        navController = nav,
                        startDestination = "menu",
                        modifier = Modifier.padding(pad)
                    ) {
                        // composable("menu") { MainMenuScreen(nav) }
			composable("menu") {
			    // Reload the latest saved player when returning to menu
			    LaunchedEffect(Unit) {
				PlayerRepository.load(context)?.let { player = it }
			    }
			    MainMenuScreen(nav)
			}
			
			composable("createCharacter") {
			    CharacterCreationScreen(
				onDone = { createdPlayer ->
				    player = createdPlayer
				    PlayerRepository.save(context, createdPlayer)
				    nav.navigate("menu") {
					popUpTo("createCharacter") { inclusive = true }
				    }
				}
			    )
			}

			composable("battle") {
			    val p = player ?: return@composable  // do nothing until loaded
			    val enemy = remember { Player.default("Rival") }

			    BattleScreen(
				player = p,
				enemy = enemy,
				navController = nav
			    )
			}
			// composable("battle") {
			//     val enemy = remember { Player.default("Rival") }
			//     BattleScreen(player = player, enemy = enemy, navController = nav)
			// }
			
			composable("alley") {
			    AlleyScreen()
			}

                        composable("wizard") {
                            WizardScreen()
                        }

			composable("tome") {
			    val p = player ?: return@composable
			    TomeScreen(p)
			}
                        // composable("tome") {
                        //     TomeScreen(player)
                        // }
                    }
                }
            }
        }
    }

    // override fun onPause() {
    // 	super.onPause()
    // 	player?.let { PlayerRepository.save(this, it) }
    // }
    // override fun onPause() {
    //     super.onPause()
    //     // 🔹 Save player progress when app goes to background
    //     PlayerRepository.save(this, PlayerRepository.load(this) ?: Player.default("You"))
    // }
}
