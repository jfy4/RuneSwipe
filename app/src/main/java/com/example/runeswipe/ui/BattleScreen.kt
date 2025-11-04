package com.example.runeswipe.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import com.example.runeswipe.util.applyStatusEffects
import android.util.Log
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min

@Composable
fun BattleScreen(player: Player, enemy: Player) {
    var log by remember { mutableStateOf("Trace a rune to cast a spell…") }
    var playerLife by remember { mutableStateOf(player.stats.life) }
    var enemyLife by remember { mutableStateOf(enemy.stats.life) }

    // Periodic status effect ticking
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000L)
            val enemyLog = applyStatusEffects(enemy)
            val playerLog = applyStatusEffects(player)
            if (enemyLog.isNotEmpty()) {
                enemyLife = enemy.stats.life
                log += "\n$enemyLog"
            }
            if (playerLog.isNotEmpty()) {
                playerLife = player.stats.life
                log += "\n$playerLog"
            }
        }
    }

    // ─── Rune drawing state ─────────────────────────────────────────────
    val currentStroke = remember { mutableStateListOf<Point>() }
    val allStrokes = remember { mutableStateListOf<List<Point>>() }

    var lastStrokeTime by remember { mutableStateOf<Long?>(null) }
    val strokeTimeoutMs = 2000L // 2 seconds allowed between strokes

    // Coroutine that checks for stroke timeout (fizzle)
    LaunchedEffect(allStrokes.size, lastStrokeTime) {
        lastStrokeTime?.let { start ->
            delay(strokeTimeoutMs)
            val elapsed = System.currentTimeMillis() - start
            if (elapsed >= strokeTimeoutMs && allStrokes.isNotEmpty()) {
                // Fizzle logic
                allStrokes.clear()
                currentStroke.clear()
                lastStrokeTime = null
                log = "Your mana fizzles out…"
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ─── Opponent HUD (top) ─────────────────────────────
        LifeHud(enemy.name, enemyLife, enemy.status, Alignment.CenterHorizontally)

        // ─── Rune Drawing Area ──────────────────────────────
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                // handle double tap submission
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (allStrokes.isNotEmpty()) {
                                val predicted = RuneModel.predict(allStrokes.toList())
                                Log.d("RuneSwipe", "Prediction: $predicted")

                                if (predicted != null) {
                                    val spell = if (player.knowsSpell(predicted)) {
                                        SpellTree.allSpells.find { it.id == predicted }
                                    } else null

                                    if (spell != null) {
                                        log = "You cast ${spell.name}!"
                                        when (spell.type) {
                                            SpellType.ATTACK -> {
                                                val dmg = computeDamage(player, enemy, spell.power)
                                                enemyLife = max(0, enemyLife - dmg)
                                                if (spell.statusInflict != StatusEffect.NONE) {
                                                    enemy.status = StatusState(spell.statusInflict)
                                                    log += " ${enemy.name} is ${spell.statusInflict.name.lowercase()}!"
                                                }
                                            }
                                            SpellType.HEAL -> {
                                                playerLife = min(player.stats.life, playerLife + spell.power)
                                                log += " You recovered ${spell.power} HP."
                                            }
                                            SpellType.STATUS -> {
                                                if (spell.statusInflict != StatusEffect.NONE) {
                                                    enemy.status = StatusState(spell.statusInflict)
                                                    log += " ${enemy.name} is ${spell.statusInflict.name.lowercase()}!"
                                                }
                                            }
                                            else -> {
                                                log += " The spell has no immediate effect."
                                            }
                                        }
                                    } else {
                                        log = "Unknown spell."
                                    }
                                } else {
                                    log = "No rune recognized."
                                }
                                // Clear after submission
                                allStrokes.clear()
                                currentStroke.clear()
                                lastStrokeTime = null
                            }
                        }
                    )
                }
                // handle drawing
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentStroke.clear()
                            currentStroke += offset.toPoint()
                        },
                        onDrag = { change, _ ->
                            currentStroke += change.position.toPoint()
                            change.consume()
                        },
                        onDragEnd = {
                            if (currentStroke.isNotEmpty()) {
                                allStrokes += currentStroke.toList()
                                currentStroke.clear()
                                lastStrokeTime = System.currentTimeMillis()
                            }
                        }
                    )
                }
        ) {
            Canvas(Modifier.fillMaxSize()) {
                // Draw previous strokes
                allStrokes.forEach { stroke ->
                    if (stroke.size > 1) {
                        val path = Path().apply {
                            moveTo(stroke.first().x, stroke.first().y)
                            stroke.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(path, color = Color.Cyan, style = Stroke(width = 8f))
                    }
                }
                // Draw current stroke in progress
                if (currentStroke.size > 1) {
                    val path = Path().apply {
                        moveTo(currentStroke.first().x, currentStroke.first().y)
                        currentStroke.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    drawPath(path, color = Color.White, style = Stroke(width = 8f))
                }
            }
        }

        // ─── Player HUD (bottom) ────────────────────────────
        LifeHud(player.name, playerLife, player.status, Alignment.CenterHorizontally)

        // ─── Log and controls ───────────────────────────────
        Spacer(Modifier.height(8.dp))
        Text(log, modifier = Modifier.align(Alignment.CenterHorizontally))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Button(onClick = {
                playerLife = player.stats.maxLife
                enemyLife = enemy.stats.maxLife
                player.status = StatusState()
                enemy.status = StatusState()
                currentStroke.clear()
                allStrokes.clear()
                lastStrokeTime = null
                log = "Battle reset."
            }) { Text("Reset") }
        }
    }
}

@Composable
private fun LifeHud(
    name: String,
    life: Int,
    status: StatusState,
    align: Alignment.Horizontal
) {
    Column(horizontalAlignment = align) {
        Text(name, fontWeight = FontWeight.Bold)
        LinearProgressIndicator(
            progress = animateFloatAsState(targetValue = life / 30f).value,
            modifier = Modifier.width(200.dp).height(10.dp)
        )
        Text("$life / 30")
        if (status.effect != StatusEffect.NONE) {
            Text(
                text = status.effect.displayName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Spacer(Modifier.height(16.dp))
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
