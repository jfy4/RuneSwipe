
// ─────────────────────────────────────────────────────────────────────────────
// app/src/main/java/com/example/runeswipe/model/Models.kt
// ─────────────────────────────────────────────────────────────────────────────
package com.example.runeswipe.model

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

// ───────────────────────────────────────────────
// Player stats and status
// ───────────────────────────────────────────────
enum class SpellType { ATTACK, DEFENSE, HEAL, STATUS, BUFF, DEBUFF }

enum class StatusEffect(
    val baseDuration: Int,
    val basePotency: Int
) {
    NONE(0, 0),
    POISONED(5, 3),
    SHIELDED(2, 3),
    IMMOBILIZED(3, 4),
    BURNED(4, 5)
}

data class Stats(
    var life: Int = 30,
    var strength: Int = 5,
    var defense: Int = 5,
    var constitution: Int = 5,
    var speed: Int = 5,
    var dexterity: Int = 5,
)

data class StatusState(
    val effect: StatusEffect = StatusEffect.NONE,
    var elapsed: Int = 0
)

data class Player(
    val name: String,
    val stats: Stats,
    var xp: Int = 0,
    var level: Int = 1,
    // var status: StatusEffect = StatusEffect.NONE,
    var status: StatusEffect = StatusState()
    val knownSpellIds: MutableSet<String> = mutableSetOf("Fehu", "Venhu")
) {
    var cooldownMs by mutableStateOf(0L)

    fun knowsSpell(spellId: String): Boolean = knownSpellIds.contains(spellId)

    fun learnSpell(spell: Spell) {
        if (!knownSpellIds.contains(spell.id) && SpellTree.canUnlock(this, spell.id)) {
            knownSpellIds.add(spell.id)
        }
    }

    companion object {
        fun default(name: String) = Player(name, Stats())
    }
}

// ───────────────────────────────────────────────
// Rune + Spell definitions
// ───────────────────────────────────────────────
/**
 * Each sampled point in a drawn stroke.
 * Matches the structure used by RuneModel + Python training:
 * { x: Float, y: Float, t: Float }
 */
data class Point(
    val x: Float,
    val y: Float,
    val t: Float   // time in milliseconds or relative time
)

/**
 * Represents a drawn rune template or a learned rune pattern.
 */
data class RuneTemplate(
    val id: String,
    val name: String,
    val strokes: List<List<Point>>  // multiple strokes per rune
)

/**
 * Game-level spell that references a rune and defines its in-battle effect.
 */
data class Spell(
    val id: String,
    val name: String,
    val type: SpellType,
    val power: Int,
    val statusInflict: StatusEffect = StatusEffect.NONE,
)

