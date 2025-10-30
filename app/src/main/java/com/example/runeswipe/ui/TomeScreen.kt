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

@Composable
fun TomeScreen() {
    val spells = SpellsRepo.All
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

// @Composable
// private fun RunePreview(spell: Spell) {
//     Box(Modifier.height(160.dp).fillMaxWidth()) {
// 	val primaryColor = MaterialTheme.colorScheme.primary
// 	val secondaryColor = MaterialTheme.colorScheme.secondary
	
// 	Canvas(Modifier.fillMaxSize()) {
// 	    val strokes = spell.rune.strokes
// 	    if (strokes.isNotEmpty()) {
// 		for (stroke in strokes) {
// 		    if (stroke.isEmpty()) continue

// 		    val path = Path()
// 		    val first = stroke.first()
// 		    path.moveTo(first.x * size.width, first.y * size.height)
// 		    for (i in 1 until stroke.size) {
// 			val p = stroke[i]
// 			path.lineTo(p.x * size.width, p.y * size.height)
// 		    }

// 		    // Draw the stroke path
// 		    drawPath(path, color = primaryColor, alpha = 0.9f)

// 		    // Draw anchor points
// 		    stroke.forEach { p ->
// 			drawCircle(
// 			    color = secondaryColor,
// 			    radius = 4f,
// 			    center = Offset(p.x * size.width, p.y * size.height)
// 			)
// 		    }
// 		}
// 	    }

	    // val pts = spell.rune.points
	    // if (pts.isNotEmpty()) {
	    // 	val path = Path()
	    // 	val first = pts.first()
	    // 	path.moveTo(first.x * size.width, first.y * size.height)
	    // 	for (i in 1 until pts.size) {
	    // 	    val p = pts[i]
	    // 	    path.lineTo(p.x * size.width, p.y * size.height)
	    // 	}
	    // 	drawPath(path, color = primaryColor, alpha = 0.9f)
	    // 	pts.forEach { p ->
	    // 	    drawCircle(
	    // 		color = secondaryColor,
	    // 		radius = 4f,
	    // 		center = Offset(p.x * size.width, p.y * size.height)
	    // 	    )
	    // 	}
	    // }
	// }
        // Canvas(Modifier.fillMaxSize()) {
        //     val pts = spell.rune.points
        //     if (pts.isNotEmpty()) {
        //         val path = Path()
        //         val first = pts.first()
        //         path.moveTo(first.x * size.width, first.y * size.height)
        //         for (i in 1 until pts.size) {
        //             val p = pts[i]
        //             path.lineTo(p.x * size.width, p.y * size.height)
        //         }
        //         drawPath(path, color = MaterialTheme.colorScheme.primary, alpha = 0.9f)
        //         // Anchor points
        //         pts.forEach { p ->
        //             drawCircle(
        //                 color = MaterialTheme.colorScheme.secondary,
        //                 radius = 4f,
        //                 center = Offset(p.x * size.width, p.y * size.height)
        //             )
        //         }
        //     }
        // }
//     }
// }
