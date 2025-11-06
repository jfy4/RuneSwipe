
// ─────────────────────────────────────────────────────────────────────────────
// app/src/main/java/com/example/runeswipe/model/Models.kt
// ─────────────────────────────────────────────────────────────────────────────
package com.example.runeswipe.model

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlin.math.max
import kotlin.math.min

// ───────────────────────────────────────────────
// Player stats and status
// ───────────────────────────────────────────────
enum class SpellType { ATTACK, GUARD, HEAL, STATUS, BUFF, DEBUFF }

enum class BuffEffect(		//flesh out
    val displayName: String,
    val baseDuration: Int,
    val basePotency: Int,
    val description: String = ""
) {
    NONE("None", 0, 0),
}

enum class DebuffEffect(		//flesh out
    val displayName: String,
    val baseDuration: Int,
    val basePotency: Int,
    val description: String = ""
) {
    NONE("None", 0, 0),
}

enum class StatusEffect(
    val displayName: String,
    val baseDuration: Int,
    val basePotency: Int,
    val description: String = ""
) {
    NONE("None", 0, 0),
    POISONED("Poisoned", 5, 3, "Gradually loses HP"),
    // SHIELDED("Shielded", 2, 3),
    // IMMOBILIZED("Immobilized", 3, 4),
    BURNED("Burned", 4, 5, "Takes fire damage."),
}

class Stats(
    initialLife: Int = 30,
    initialMaxLife: Int = 30,
    var strength: Int = 5,
    var defense: Int = 5,
    var constitution: Int = 5,
    var speed: Int = 5,
    var dexterity: Int = 5,
) {
    var maxLife by mutableStateOf(initialMaxLife)
    var life by mutableStateOf(initialLife)

    // fun changeLife(delta: Int) {
    //     life = (life + delta).coerceIn(0, maxLife)
    // }
}

data class StatusState(
    val effect: StatusEffect = StatusEffect.NONE,
    var elapsed: Int = 0
)

data class BuffState(
    val effect: BuffEffect = BuffEffect.NONE,
    var elapsed: Int = 0
)

data class DebuffState(
    val effect: DebuffEffect = DebuffEffect.NONE,
    var elapsed: Int = 0
)

data class Player(
    val name: String,
    val stats: Stats,
    var xp: Int = 0,
    var level: Int = 1,
    // var status: StatusEffect = StatusEffect.NONE,
    var status: StatusState = StatusState(),
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
// data class RuneTemplate(
//     val id: String,
//     val name: String,
//     val strokes: List<List<Point>>  // multiple strokes per rune
// )

/**
 * Game-level spell that references a rune and defines its in-battle effect.
 * @property id   the unique id used for classification
 * @property name the in-game name of the spell
 * @property type the type of spell (ATTACK, STATUS, etc.)
 * @property damage the damage dealt if spell is ATTACK
 * @property status the status effect if spell is STATUS
 * @property buff the buff if spell is BUFF
 * @property debuff the debuff if spelll is DEBUFF
 * @property heal the amount healed if spell is HEAL
 */
data class Spell(
    val id: String,
    val name: String,
    val type: SpellType,
    val damage: Int = 0,
    val status: StatusEffect = StatusEffect.NONE,
    val buff: BuffEffect = BuffEffect.NONE,
    val debuff: DebuffEffect = DebuffEffect.NONE,
    val heal: Int = 0,
) {
    fun apply(caster: Player, target: Player): String {
        var result = "You cast $name!"

        when (type) {
            SpellType.ATTACK -> {
                val dmg = computeDamage(caster, target, damage)
                target.stats.life = max(0, target.stats.life - dmg)
                result += " It dealt $dmg damage."
                if (status != StatusEffect.NONE) {
                    target.status = StatusState(status)
                    result += " ${target.name} is ${status.name.lowercase()}!"
                }
            }

            SpellType.HEAL -> {
                val healed = min(heal, caster.stats.maxLife - caster.stats.life)
                caster.stats.life += healed
                result += " You recovered $healed HP."
            }

            SpellType.STATUS -> {
                if (status != StatusEffect.NONE) {
                    target.status = StatusState(status)
                    result += " ${target.name} is ${status.name.lowercase()}!"
                } else {
                    result += " But nothing happened."
                }
            }

            else -> {
                result += " The spell has no immediate effect."
            }
        }

        return result
    }
}

private fun computeDamage(attacker: Player, defender: Player, basePower: Int): Int {
    val atk = basePower + attacker.stats.strength
    val def = defender.stats.defense
    return max(0, atk - def / 2)
}
