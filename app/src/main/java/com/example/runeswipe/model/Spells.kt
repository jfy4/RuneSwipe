// ─────────────────────────────────────────────────────────────────────────────
// app/src/main/java/com/example/runeswipe/model/Spells.kt
// ─────────────────────────────────────────────────────────────────────────────
package com.example.runeswipe.model

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI


object SpellsRepo {
    val Fireball = Spell(
        id = "Fehu",
        name = "Fireball",
        type = SpellType.ATTACK,
        damage = 6,
	status = StatusEffect.BURNED,
    )

    val Heal = Spell(
        id = "Lefu",
        name = "Healing Light",
        type = SpellType.HEAL,
	heal = 5,
    )

    val Poison = Spell(
        id = "Venhu",
        name = "Poison",
        type = SpellType.STATUS,
	status = StatusEffect.POISONED,
    )

    val Mute = Spell(
        id = "Mute",
        name = "Mute",
        type = SpellType.STATUS,
	status = StatusEffect.SILENCED,
    )


    val All = listOf(Fireball, Heal, Poison, Mute)
}
