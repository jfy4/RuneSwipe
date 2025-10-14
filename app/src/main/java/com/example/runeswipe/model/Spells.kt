// ─────────────────────────────────────────────────────────────────────────────
// app/src/main/java/com/example/runeswipe/model/Spells.kt
// ─────────────────────────────────────────────────────────────────────────────
package com.example.runeswipe.model

// Sample rune templates (very simplified shapes). In a real game you'd define
// richer point paths or load from CSV.
object Runes {
    val CircleCCW = RuneTemplate(
        id = "circle_ccw",
        name = "Circle (CCW)",
        points = (0..360 step 15).map { deg ->
            val rad = Math.toRadians(deg.toDouble())
            // CCW circle centered near (0.5,0.5) in unit square then scaled
            Point((0.5 + 0.4 * kotlin.math.cos(rad)).toFloat(), (0.5 + 0.4 * kotlin.math.sin(rad)).toFloat())
        }
    )

    val Cross = RuneTemplate(
        id = "cross",
        name = "Cross",
        points = buildList {
            // Vertical line
            for (t in 0..10) add(Point(0.5f, 0.1f + 0.8f * (t / 10f)))
            // Move & Horizontal line
            for (t in 0..10) add(Point(0.1f + 0.8f * (t / 10f), 0.5f))
        }
    )

    val All = listOf(CircleCCW, Cross)
}

object SpellsRepo {
    val Fireball = Spell(
        id = "fireball",
        name = "Fireball",
        type = SpellType.ATTACK,
        power = 6,
        rune = Runes.CircleCCW
    )

    val Heal = Spell(
        id = "heal",
        name = "Healing Light",
        type = SpellType.HEAL,
        power = 5,
        rune = Runes.Cross
    )

    val All = listOf(Fireball, Heal)
}
