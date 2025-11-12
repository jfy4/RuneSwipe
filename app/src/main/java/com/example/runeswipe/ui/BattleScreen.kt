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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.runeswipe.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.util.Log

@Composable
fun BattleScreen(
    player: Player,
    enemy: Player,
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var log by remember { mutableStateOf("Trace a rune to cast a spell…") }
    var battleOver by remember { mutableStateOf(false) }

    // ─── Rune Drawing State ──────────────────────
    val currentStroke = remember { mutableStateListOf<Point>() }
    val allStrokes = remember { mutableStateListOf<List<Point>>() }
    var lastStrokeTime by remember { mutableStateOf<Long?>(null) }
    val strokeTimeoutMs = 2000L

    // 1) put endBattle BEFORE the tick loop so we can call it from there
    fun endBattle(victory: Boolean) {
        if (battleOver) return
        battleOver = true

        if (victory) {
            val xpGain = 50 * enemy.level      // tweak formula as you like
            val xpLog = player.gainXp(xpGain)
            log += "\n${enemy.name} was defeated! $xpLog"
        } else {
            log += "\nYou were defeated..."
        }

        // save winner (or current player) to disk
        PlayerRepository.save(context, player)

        // navigate back after a short pause
        scope.launch {
            delay(2000)
            navController.navigate("menu") {
                popUpTo("battle") { inclusive = true }
            }
        }
    }

    // 2) status tick that can actually END the battle
    LaunchedEffect(Unit) {
        while (!battleOver) {
            delay(2000L)
            val enemyLogs = EffectManager.tickPlayer(enemy)
            val playerLogs = EffectManager.tickPlayer(player)

            // if poison (or any effect) killed someone, END HERE
            if (enemy.stats.life <= 0) {
                endBattle(victory = true)
                break
            }
            if (player.stats.life <= 0) {
                endBattle(victory = false)
                break
            }

            (enemyLogs + playerLogs).forEach { log += "\n$it" }
        }
    }

    // 3) stroke-timeout should do nothing once battle is over
    LaunchedEffect(allStrokes.size, lastStrokeTime, battleOver) {
        if (battleOver) return@LaunchedEffect
        lastStrokeTime?.let { start ->
            delay(strokeTimeoutMs)
            val elapsed = System.currentTimeMillis() - start
            if (elapsed >= strokeTimeoutMs && allStrokes.isNotEmpty()) {
                allStrokes.clear()
                currentStroke.clear()
                lastStrokeTime = null
                log = "Your mana fizzles out…"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        LifeHud(enemy, Alignment.CenterHorizontally)

        // ─── Rune Drawing Area ────────────────────
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(16.dp)
                )
                // IMPORTANT: make pointerInput depend on battleOver
                .pointerInput(battleOver) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (battleOver) return@detectTapGestures
                            if (allStrokes.isNotEmpty()) {
                                val predicted = RuneModel.predict(allStrokes.toList())
                                Log.d("RuneSwipe", "Prediction: $predicted")

                                if (predicted != null) {
                                    val spell = if (player.knowsSpell(predicted)) {
                                        SpellTree.allSpells.find { it.id == predicted }
                                    } else null

                                    if (spell != null) {
                                        log = spell.apply(player, enemy)

                                        // immediate death check
                                        when {
                                            enemy.stats.life <= 0 -> endBattle(victory = true)
                                            player.stats.life <= 0 -> endBattle(victory = false)
                                        }
                                    } else {
                                        log = "Unknown spell."
                                    }
                                } else {
                                    log = "No rune recognized."
                                }

                                allStrokes.clear()
                                currentStroke.clear()
                                lastStrokeTime = null
                            }
                        }
                    )
                }
                .pointerInput(battleOver) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (battleOver) return@detectDragGestures
                            currentStroke.clear()
                            currentStroke += offset.toPoint()
                        },
                        onDrag = { change, _ ->
                            if (battleOver) return@detectDragGestures
                            currentStroke += change.position.toPoint()
                            change.consume()
                        },
                        onDragEnd = {
                            if (battleOver) return@detectDragGestures
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
                // Draw current stroke
                if (currentStroke.size > 1) {
                    val path = Path().apply {
                        moveTo(currentStroke.first().x, currentStroke.first().y)
                        currentStroke.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    drawPath(path, color = Color.White, style = Stroke(width = 8f))
                }
            }
        }

        LifeHud(player, Alignment.CenterHorizontally)

        Spacer(Modifier.height(8.dp))
        Text(log, modifier = Modifier.align(Alignment.CenterHorizontally))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Button(
                onClick = {
                    if (battleOver) return@Button  // don't reset dead battle
                    player.stats.life = player.stats.maxLife
                    enemy.stats.life = enemy.stats.maxLife
		    player.statuses.clear()
		    enemy.statuses.clear()
                    // player.status = StatusState()
                    // enemy.status = StatusState()
                    currentStroke.clear()
                    allStrokes.clear()
                    lastStrokeTime = null
                    log = "Battle reset."
                }
            ) { Text("Reset") }
        }
    }
}

@Composable
private fun LifeHud(player: Player, align: Alignment.Horizontal) {
    Column(horizontalAlignment = align) {
        Text(player.name, fontWeight = FontWeight.Bold)
        LinearProgressIndicator(
            progress = animateFloatAsState(
                targetValue = player.stats.life / player.stats.maxLife.toFloat()
            ).value,
            modifier = Modifier
                .width(200.dp)
                .height(10.dp)
        )
        Text("${player.stats.life} / ${player.stats.maxLife}")
	if (player.statuses.isNotEmpty()) {
	    Column {
		player.statuses.forEach { s ->
		    val label = if (s.stacks > 1)
			"${s.effect.displayName} ×${s.stacks}"
		    else
			s.effect.displayName
		    Text(
			text = label,
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.primary
		    )
		}
	    }
	} else {
	    Spacer(Modifier.height(16.dp))
	}
        // if (player.status.effect != StatusEffect.NONE) {
        //     Text(
        //         text = player.status.effect.displayName,
        //         style = MaterialTheme.typography.bodySmall,
        //         color = MaterialTheme.colorScheme.primary
        //     )
        // } else {
        //     Spacer(Modifier.height(16.dp))
        // }
    }
}

private fun Offset.toPoint(): Point =
    Point(x = this.x, y = this.y, t = System.currentTimeMillis().toFloat())
