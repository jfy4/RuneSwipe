// ─────────────────────────────────────────────────────────────────────────────
// app/src/main/java/com/example/runeswipe/ui/BattleScreen.kt
// ─────────────────────────────────────────────────────────────────────────────
package com.example.runeswipe.ui


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.runeswipe.model.*
import kotlin.math.max
import android.util.Log

@Composable
fun BattleScreen(player: Player, enemy: Player) {
    var log by remember { mutableStateOf("Trace a rune to cast a spell…") }
    var playerLife by remember { mutableStateOf(player.stats.life) }
    var enemyLife by remember { mutableStateOf(enemy.stats.life) }

    // Gesture state
    val stroke = remember { mutableStateListOf<Point>() }
    var lastCast by remember { mutableStateOf<Spell?>(null) }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            LifeHud(name = player.name, life = playerLife)
            LifeHud(name = enemy.name, life = enemyLife)
        }

        Box(
            Modifier.weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
		.pointerInput(Unit) {
		    detectDragGestures(
			onDragStart = { offset ->
			    val gestureAreaSize = Size(this.size.width.toFloat(), this.size.height.toFloat())
			    stroke.clear()
			    stroke += offset.toPoint(gestureAreaSize)
			},
			onDrag = { change, _ ->
			    val gestureAreaSize = Size(this.size.width.toFloat(), this.size.height.toFloat())
			    stroke += change.position.toPoint(gestureAreaSize)
			    change.consume()   // ensures continuous updates
			},
			onDragEnd = {
			    // Recognizer code goes here
			    val gestureAreaSize = Size(this.size.width.toFloat(), this.size.height.toFloat())
			    Log.d("RuneSwipe", "Stroke captured: ${stroke.size} points")
			    val (template, distance) = DollarOneRecognizer.recognize(stroke.toList(), Runes.All)
			    Log.d("RuneSwipe", "Recognized: ${template?.name ?: "none"}  distance=$distance")
			    
			    val match = if (distance < 60.0) template else null  // threshold control
			    
			    if (match != null) {
				log = "You cast ${match.name}! (match distance = ${"%.2f".format(distance)})"
			    } else {
				log = "No rune recognized (distance = ${"%.2f".format(distance)})"
			    }
			    // val (template, distance) = DollarOneRecognizer.recognize(stroke.toList(), Runes.All)
			    // Log.d("RuneSwipe", "Best match: ${template?.name ?: "none"}  distance=$distance")


			    // (you don’t actually need to use it again here unless you reference size)
			}
		    )
		}
        ) {
            // Draw current stroke
            Canvas(Modifier.fillMaxSize()) {
                if (stroke.size > 1) {
                    val path = Path()
                    val first = stroke.first().toOffset(size)
                    path.moveTo(first.x, first.y)
                    stroke.drop(1).forEach { p ->
                        val o = p.toOffset(size)
                        path.lineTo(o.x, o.y)
                    }
                    drawPath(
			path = path,
			color = Color.Cyan,
			style = Stroke(width = 8f)
		    )
                }
            }
        }

        lastCast?.let { Text("Last cast: ${it.name}", fontWeight = FontWeight.Medium) }
        Text(log)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { /* TODO local/online turns */ }, enabled = enemyLife > 0 && playerLife > 0) {
                Text("End Turn")
            }
            OutlinedButton(onClick = { /* Reset */
                playerLife = player.stats.life
                enemyLife = enemy.stats.life
                log = "Battle reset."
                lastCast = null
            }) { Text("Reset") }
        }
    }
}

@Composable
private fun LifeHud(name: String, life: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(name, fontWeight = FontWeight.Bold)
        LinearProgressIndicator(
            progress = animateFloatAsState(targetValue = life / 30f).value,
            modifier = Modifier.width(120.dp)
        )
        Text("$life / 30")
    }
}

private fun computeDamage(attacker: Player, defender: Player, basePower: Int): Int {
    // Simple, transparent damage model. Tune later.
    val atk = basePower + attacker.stats.strength
    val def = defender.stats.defense
    return max(1, atk - def / 2)
}

private fun Offset.toPoint(size: androidx.compose.ui.geometry.Size): Point =
    Point(x / size.width, y / size.height)

private fun Point.toOffset(size: androidx.compose.ui.geometry.Size): Offset =
    Offset(x * size.width, y * size.height)
