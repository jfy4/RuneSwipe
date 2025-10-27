// ─────────────────────────────────────────────────────────────────────────────
// app/src/main/java/com/example/runeswipe/MainActivity.kt
// ─────────────────────────────────────────────────────────────────────────────
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.runeswipe

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.os.Bundle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.compose.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
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
                Scaffold { pad ->
                    NavHost(
                        navController = nav,
                        startDestination = "menu",
                        modifier = Modifier.padding(pad)
                    ) {
                        composable("menu") { MainMenuScreen(nav) }
                        composable("battle") {
                            val (player, enemy) = remember {
                                val p = Player.default("You")
                                val e = Player.default("Rival")
                                p to e
                            }
                            BattleScreen(player = player, enemy = enemy)
                        }
                        composable("wizard") { WizardScreen() }
                        composable("tome") { TomeScreen() }
                    }
                }
            }
        }
    }
}


// @file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
// package com.example.runeswipe

// import android.os.Bundle
// import androidx.activity.ComponentActivity
// import androidx.activity.compose.setContent
// import androidx.compose.foundation.layout.*
// import androidx.compose.material3.*
// import androidx.compose.runtime.*
// import androidx.compose.ui.Modifier
// import androidx.compose.ui.unit.dp
// import androidx.navigation.compose.NavHost
// import androidx.navigation.compose.composable
// import androidx.navigation.compose.rememberNavController
// import com.example.runeswipe.model.*
// import com.example.runeswipe.ui.BattleScreen
// import com.example.runeswipe.ui.TomeScreen
// import com.example.runeswipe.ui.theme.RuneTheme

// class MainActivity : ComponentActivity() {
//     override fun onCreate(savedInstanceState: Bundle?) {
//         super.onCreate(savedInstanceState)
//         setContent {
//             RuneTheme {
//                 val nav = rememberNavController()
//                 Scaffold(
//                     topBar = {
//                         TopAppBar(
//                             title = { Text("RuneSwipe") },
//                             actions = {
//                                 TextButton(onClick = { nav.navigate("tome") }) { Text("Tome") }
//                             }
//                         )
//                     }
//                 ) { pad ->
//                     NavHost(
//                         navController = nav,
//                         startDestination = "battle",
//                         modifier = Modifier.padding(pad)
//                     ) {
//                         composable("battle") {
//                             val (player, enemy) = remember {
//                                 val player = Player.default("You")
//                                 val enemy = Player.default("Rival")
//                                 player to enemy
//                             }
//                             BattleScreen(player = player, enemy = enemy)
//                         }
//                         composable("tome") { TomeScreen() }
//                     }
//                 }
//             }
//         }
//     }
// }
