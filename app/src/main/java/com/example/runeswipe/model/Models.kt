
// ─────────────────────────────────────────────────────────────────────────────
// app/src/main/java/com/example/runeswipe/model/Models.kt
// ─────────────────────────────────────────────────────────────────────────────
package com.example.runeswipe.model

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlin.math.max
import kotlin.math.min
import kotlinx.serialization.Serializable


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

    // ─────────── DoT Effects ───────────
    POISONED("Poisoned", 5, 3, "The target is slowly weakened by venom. Lose HP each turn."),
    BURNED("Burned", 4, 5, "Engulfed in magical flames; loses HP and defense."),
    BLEEDING("Bleeding", 4, 4, "Wounds drain vitality; cannot regenerate HP."),
    CURSED_FLAME("Cursed Flame", 3, 5, "Shadowfire eats at spirit and body."),

    // ─────────── Control Effects ───────────
    FROZEN("Frozen", 2, 0, "Unable to move until thawed; breaks on hit."),
    STUNNED("Stunned", 1, 0, "Cannot act for one turn."),
    PARALYZED("Paralyzed", 3, 0, "50% chance to lose turn each round."),
    ASLEEP("Asleep", 3, 0, "Skip turns until damaged."),
    CONFUSED("Confused", 3, 0, "Attacks may target allies or self."),
    SILENCED("Silenced", 3, 0, "Cannot cast spells."),

    // ─────────── Debuffs ───────────
    WEAKENED("Weakened", 3, 2, "Reduced Strength."),
    CRIPPLED("Crippled", 3, 2, "Reduced Speed."),
    SHATTERED_GUARD("Shattered Guard", 3, 2, "Reduced Defense."),
    CURSED_MIND("Cursed Mind", 4, 2, "Reduced Accuracy or Magic Power."),
    DOOMED("Doomed", 4, 0, "Target dies after timer expires."),

    // ─────────── Buffs ───────────
    ENRAGED("Enraged", 3, 4, "Attack up, Defense down."),
    FORTIFIED("Fortified", 4, 3, "Defense up."),
    HASTE("Haste", 3, 3, "Increased Speed, may act twice per round."),
    BARRIER("Barrier", 3, 6, "Magical shield absorbs harm."),
    REGENERATING("Regeneration", 4, 3, "Recover HP each turn."),
    FOCUSED("Focus", 3, 3, "Accuracy and Magic Power up."),
    REFLECTING("Reflect", 2, 0, "Reflects incoming magic damage."),
    INVISIBLE("Invisible", 3, 0, "Attacks against you may miss."),

    // ─────────── Exotic ───────────
    SOULBOUND("Soulbind", 3, 0, "Links caster and target HP."),
    HEXED("Hexed", 3, 0, "Random debuff each turn."),
    MANA_LEAK("Mana Leak", 3, 3, "Lose MP each turn."),
    SHADOW_VEIL("Shadow Veil", 2, 0, "Immune to one attack type."),
    RADIANT_BLESSING("Radiant Blessing", 3, 0, "Immune to status effects."),
}


enum class BuffEffect(
    val displayName: String,
    val baseDuration: Int,
    val basePotency: Int,
    val description: String = ""
) {
    NONE("None", 0, 0),
    ENRAGED("Enraged", 3, 4, "Attack up, Defense down."),
    FORTIFIED("Fortified", 4, 3, "Increased Defense."),
    HASTE("Haste", 3, 3, "Increased Speed."),
    BARRIER("Barrier", 3, 6, "Absorbs damage."),
    REGENERATION("Regeneration", 4, 3, "Heals each turn."),
    FOCUS("Focus", 3, 3, "Improved accuracy and magic."),
    REFLECT("Reflect", 2, 0, "Reflects spells."),
    INVISIBILITY("Invisibility", 3, 0, "Enemies may miss attacks."),
    RADIANT_BLESSING("Radiant Blessing", 3, 0, "Immune to status effects."),
}

enum class DebuffEffect(
    val displayName: String,
    val baseDuration: Int,
    val basePotency: Int,
    val description: String = ""
) {
    NONE("None", 0, 0),
    WEAKENED("Weakened", 3, 2, "Reduced Strength."),
    CRIPPLED("Crippled", 3, 2, "Reduced Speed."),
    SHATTERED_GUARD("Shattered Guard", 3, 2, "Reduced Defense."),
    CURSED_MIND("Cursed Mind", 4, 2, "Reduced Accuracy or Magic Power."),
    DOOMED("Doomed", 4, 0, "Target dies after timer expires."),
    HEXED("Hexed", 3, 0, "Random debuff each turn."),
    MANA_LEAK("Mana Leak", 3, 3, "Lose MP each turn."),
}

// @Serializable
// class Stats(
//     initialLife: Int = 30,
//     initialMaxLife: Int = 30,
//     var strength: Int = 5,
//     var defense: Int = 5,
//     var constitution: Int = 5,
//     var speed: Int = 5,
//     var dexterity: Int = 5,
// ) {
//     var maxLife by mutableStateOf(initialMaxLife)
//     var life by mutableStateOf(initialLife)

//     // fun changeLife(delta: Int) {
//     //     life = (life + delta).coerceIn(0, maxLife)
//     // }
// }
@Serializable
data class StatsData(
    val life: Int = 30,
    val maxLife: Int = 30,
    val strength: Int = 5,	// magic strength
    val defense: Int = 5,	// magic defence
    val constitution: Int = 5,	// resistance to status effects
    val speed: Int = 5,		// rate that mana regenerates???
    val dexterity: Int = 5,	// ability to dodge incoming spell
)

// Non-serializable Compose-friendly wrapper
class Stats(statsData: StatsData = StatsData()) {
    var life by mutableStateOf(statsData.life)
    var maxLife by mutableStateOf(statsData.maxLife)
    var strength by mutableStateOf(statsData.strength)
    var defense by mutableStateOf(statsData.defense)
    var constitution by mutableStateOf(statsData.constitution)
    var speed by mutableStateOf(statsData.speed)
    var dexterity by mutableStateOf(statsData.dexterity)

    fun toData(): StatsData = StatsData(
        life = life,
        maxLife = maxLife,
        strength = strength,
        defense = defense,
        constitution = constitution,
        speed = speed,
        dexterity = dexterity
    )
}

@Serializable
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


// ─────────────────────────────────────────────────────────────
// Serializable data-only version (for saving/loading)
// ─────────────────────────────────────────────────────────────
@Serializable
data class PlayerData(
    val name: String,
    val stats: StatsData = StatsData(),
    val xp: Int = 0,
    val level: Int = 1,
    val knownSpellIds: Set<String> = setOf("Fehu", "Venhu")
)


// ─────────────────────────────────────────────────────────────
// Runtime Player class (Compose-friendly, mutableStateOf, logic)
// ─────────────────────────────────────────────────────────────
class Player(
    val name: String,
) {
    var cooldownMs by mutableStateOf(0L)
    var status: StatusState = StatusState()
    var xp: Int = 0
    var level: Int = 1
    val stats: Stats = Stats()
    val knownSpellIds: MutableSet<String> = mutableSetOf("Fehu", "Venhu")
    val buffs: MutableList<BuffState> = mutableListOf()
    val debuffs: MutableList<DebuffState> = mutableListOf()
    // ─────────────────────────────
    // Spell logic
    // ─────────────────────────────
    fun knowsSpell(spellId: String): Boolean = knownSpellIds.contains(spellId)

    fun learnSpell(spell: Spell) {
        if (!knownSpellIds.contains(spell.id) && SpellTree.canUnlock(this, spell.id)) {
            knownSpellIds.add(spell.id)
        }
    }

    // ─────────────────────────────
    // Converters for persistence
    // ─────────────────────────────
    fun toData(): PlayerData = PlayerData(
        name = name,
        stats = stats.toData(),
        xp = xp,
        level = level,
        knownSpellIds = knownSpellIds
    )

    companion object {
        fun default(name: String) = Player(name)

	fun fromData(data: PlayerData): Player {
            val p = Player(data.name)
            // populate fields
            p.stats.life         = data.stats.life
            p.stats.maxLife      = data.stats.maxLife
            p.stats.strength     = data.stats.strength
            p.stats.defense      = data.stats.defense
            p.stats.constitution = data.stats.constitution
            p.stats.speed        = data.stats.speed
            p.stats.dexterity    = data.stats.dexterity

            p.xp = data.xp
            p.level = data.level
            p.status = StatusState()
            p.knownSpellIds.clear()
            p.knownSpellIds.addAll(data.knownSpellIds)
            return p // ✅ explicitly return the populated player
	}        // fun fromData(data: PlayerData): Player = Player(
        //     name = data.name,
        //     stats = Stats(data.stats),
        //     xp = data.xp,
        //     level = data.level,
        //     status = StatusState(),
        //     knownSpellIds = data.knownSpellIds.toMutableSet()
        // )
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
@Serializable
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
		EffectManager.applyStatus(target, status)
		EffectManager.applyDebuff(target, debuff)
            }

            SpellType.HEAL -> {
		val healed = min(heal, caster.stats.maxLife - caster.stats.life)
		caster.stats.life += healed
		result += " You recovered $healed HP."
            }

            SpellType.STATUS -> {
		EffectManager.applyStatus(target, status)
		result += if (status != StatusEffect.NONE)
                    " ${target.name} is ${status.displayName.lowercase()}!"
		else " But nothing happened."
            }

            SpellType.BUFF -> {
		EffectManager.applyBuff(caster, buff)
		result += if (buff != BuffEffect.NONE)
                    " ${caster.name} is empowered by ${buff.displayName.lowercase()}!"
		else " But nothing happened."
            }

            SpellType.DEBUFF -> {
		EffectManager.applyDebuff(target, debuff)
		result += if (debuff != DebuffEffect.NONE)
                    " ${target.name} is afflicted by ${debuff.displayName.lowercase()}!"
		else " But nothing happened."
            }

            else -> result += " The spell has no immediate effect."
	}

	return result
    }
}

private fun computeDamage(attacker: Player, defender: Player, basePower: Int): Int {
    val atk = basePower + attacker.stats.strength
    val def = defender.stats.defense
    return max(0, atk - def / 2)
}
