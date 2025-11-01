// ─────────────────────────────────────────────────────────────────────────────
// app/src/main/java/com/example/runeswipe/ui/TomeScreen.kt
// ─────────────────────────────────────────────────────────────────────────────
package com.example.runeswipe.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.example.runeswipe.model.Spell
import com.example.runeswipe.model.SpellsRepo
import com.example.runeswipe.model.Player
import com.example.runeswipe.model.SpellTree

// @Composable
// fun TomeScreen() {
//     val spells = SpellsRepo.All
//     LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
//         items(spells) { spell -> SpellCard(spell) }
//     }
// }
@Composable
fun TomeScreen(player: Player) {
    val spells = SpellTree.allSpells.filter { player.knowsSpell(it.id) }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(spells) { spell -> SpellCard(spell) }
    }
}

@Composable
private fun SpellCard(spell: Spell) {
    ElevatedCard { Column(Modifier.padding(16.dp)) {
        Text(spell.name, style = MaterialTheme.typography.titleMedium)
        Text("Type: ${spell.type}")
        Spacer(Modifier.height(8.dp))
        // RunePreview(spell)
    } }
}

