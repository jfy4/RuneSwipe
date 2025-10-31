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
        power = 6,
	statusInflict = StatusEffect.BURNED,
    )

    val Heal = Spell(
        id = "Lefu",
        name = "Healing Light",
        type = SpellType.HEAL,
        power = 0,
    )

    val All = listOf(Fireball, Heal)
}
