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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.runeswipe.model.*
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlin.math.max

@Composable
fun BattleScreen(player: Player, enemy: Player) {
    var log by remember { mutableStateOf("Trace a rune to cast a spell…") }
    var playerLife by remember { mutableStateOf(player.stats.life) }
    var enemyLife by remember { mutableStateOf(enemy.stats.life) }

    // Gesture state
    val stroke = remember { mutableStateListOf<Point>() }
    val pendingStrokes = remember { mutableStateListOf<List<Point>>() }
    val handler = remember { Handler(Looper.getMainLooper()) }
    var lastStrokeTime by remember { mutableStateOf(0L) }
    val gestureTimeout = 1000L // ms

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- Opponent HUD (Top) ---
        LifeHud(
            name = enemy.name,
            life = enemyLife,
	    status = enemy.status,
            align = Alignment.CenterHorizontally
        )

        // --- Gesture Drawing Area ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(16.dp)
                )
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            stroke.clear()
                            stroke += offset.toPoint()
                        },
                        onDrag = { change, _ ->
                            stroke += change.position.toPoint()
                            change.consume()
                        },
                        onDragEnd = {
                            pendingStrokes += stroke.toList()
                            stroke.clear()
                            lastStrokeTime = System.currentTimeMillis()
                            handler.removeCallbacksAndMessages(null)

                            handler.postDelayed({
                                val elapsed = System.currentTimeMillis() - lastStrokeTime
                                if (elapsed >= gestureTimeout && pendingStrokes.isNotEmpty()) {
                                    val predicted = RuneModel.predict(pendingStrokes.toList()) // Check if player knowns spell
                                    Log.d("RuneSwipe", "Prediction: $predicted")
                                    if (predicted != null) {
					val spell = if (player.knowsSpell(predicted)) {
					    Log.d("RuneSwipe", "Spell known")
					    SpellTree.allSpells.find { it.id == predicted }
					} else null
					Log.d("RuneSwipe", "Predicted='$predicted' — Match=${spell?.id ?: "null"}")
					Log.d("RuneSwipe", "All spell IDs: ${SpellTree.allSpells.map { it.id }}")
                                        if (spell != null) {
                                            log = "You cast ${spell.name}!"
					    when (spell.type) {
						SpellType.ATTACK -> {
						    val dmg = computeDamage(player, enemy, spell.power)
						    enemyLife = max(0, enemyLife - dmg)
						    if (spell.statusInflict != StatusEffect.NONE) {
							enemy.status = spell.statusInflict
							log += " ${enemy.name} is ${spell.statusInflict.name.lowercase()}!"
						    }
						}

						SpellType.HEAL -> {
						    playerLife = minOf(player.stats.life, playerLife + spell.power)
						    log += " You recovered ${spell.power} HP."
						}

						SpellType.STATUS -> {
						    if (spell.statusInflict != StatusEffect.NONE) {
							enemy.status = spell.statusInflict
							Log.d("RuneSwipe", "here")
							log += " ${enemy.name} is ${spell.statusInflict.name.lowercase()}!"
						    }
						}

						SpellType.DEFENSE -> {
						    log += " The spell has no immediate effect."
						}

						SpellType.BUFF -> {
						    log += " The spell has no immediate effect."
						}

						SpellType.DEBUFF -> {
						    log += " The spell has no immediate effect."
						}   

						else -> {
						    // For now, just log it — covers BUFF, DEBUFF, or unknown future types
						    log += " The spell has no immediate effect."
						}
					    }
                                        } else {
                                            log = "Unknown spell."
                                        }
                                    } else {
                                        log = "No rune recognized."
                                    }
                                    pendingStrokes.clear()
                                }
                            }, gestureTimeout)
                        }
                    )
                }
        ) {
            // Draw current stroke
            Canvas(Modifier.fillMaxSize()) {
                if (stroke.size > 1) {
                    val path = Path().apply {
                        moveTo(stroke.first().x, stroke.first().y)
                        stroke.drop(1).forEach { p ->
                            lineTo(p.x, p.y)
                        }
                    }
                    drawPath(path, color = Color.Cyan, style = Stroke(width = 8f))
                }
            }
        }

        // --- Player HUD (Bottom) ---
        LifeHud(
            name = player.name,
            life = playerLife,
	    status = player.status,
            align = Alignment.CenterHorizontally
        )

        // --- Log and controls ---
        Spacer(Modifier.height(8.dp))
        Text(log, modifier = Modifier.align(Alignment.CenterHorizontally))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Button(onClick = {
                playerLife = player.stats.life
                enemyLife = enemy.stats.life
		enemy.status = StatusEffect.NONE
		player.status = StatusEffect.NONE
                log = "Battle reset."
            }) { Text("Reset") }
        }
    }
}

@Composable
private fun LifeHud(
    name: String,
    life: Int,
    status: StatusEffect,
    align: Alignment.Horizontal
) {
    Column(horizontalAlignment = align) {
        Text(name, fontWeight = FontWeight.Bold)

        LinearProgressIndicator(
            progress = animateFloatAsState(targetValue = life / 30f).value,
            modifier = Modifier
                .width(200.dp)
                .height(10.dp)
        )

        Text("$life / 30")

        // ── Status line ───────────────────────────
        if (status != StatusEffect.NONE) {
            Text(
                text = status.name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Spacer(Modifier.height(16.dp)) // blank space for alignment
        }
    }
}

private fun Offset.toPoint(): Point =
    Point(x = this.x, y = this.y, t = System.currentTimeMillis().toFloat())

private fun computeDamage(attacker: Player, defender: Player, basePower: Int): Int {
    val atk = basePower + attacker.stats.strength
    val def = defender.stats.defense
    return max(0, atk - def / 2)
}
