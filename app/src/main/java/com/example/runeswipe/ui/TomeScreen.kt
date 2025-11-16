// ui/TomeScreen.kt
package com.example.runeswipe.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.runeswipe.model.*

@Composable
fun TomeScreen(player: Player) {
    val chapters = SpellTree.chapters
    var selectedChapter by remember { mutableStateOf(chapters.keys.first()) }
    var pageIndex by remember { mutableStateOf(0) }

    val spellsInChapter = remember(selectedChapter, player.knownSpellIds) {
        (chapters[selectedChapter] ?: emptyList()).filter { player.knowsSpell(it.id) }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        // Tabs
        ScrollableTabRow(selectedTabIndex = chapters.keys.indexOf(selectedChapter)) {
            chapters.keys.forEach { chapter ->
                Tab(
                    selected = selectedChapter == chapter,
                    onClick = {
                        selectedChapter = chapter
                        pageIndex = 0
                    },
                    text = { Text(chapter) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        if (spellsInChapter.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No known spells in this chapter.")
            }
            return
        }

        val spell = spellsInChapter[pageIndex]

        // Book spread
        Row(
            Modifier
                .fillMaxSize()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ElevatedCard(
                Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(spell.name, style = MaterialTheme.typography.titleLarge)
                    Text("Type: ${spell.type}")
                    Spacer(Modifier.height(8.dp))
                    when (spell.type) {
                        SpellType.ATTACK -> Text("Damage: ${spell.damage}")
                        SpellType.HEAL   -> Text("Heals: ${spell.heal}")
                        SpellType.STATUS -> Text("Inflicts: ${spell.status}")
                        else -> {}
                    }
                }
            }

            ElevatedCard(
                Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Description", style = MaterialTheme.typography.titleMedium)
                    Text("A study page for ${spell.name}. Add rune art, tips, and lore here.")
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Navigation
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = { if (pageIndex > 0) pageIndex-- }, enabled = pageIndex > 0) {
                Text("◀ Prev")
            }
            Button(
                onClick = { if (pageIndex < spellsInChapter.size - 1) pageIndex++ },
                enabled = pageIndex < spellsInChapter.size - 1
            ) {
                Text("Next ▶")
            }
        }
    }
}
