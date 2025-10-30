// ─────────────────────────────────────────────────────────────────────────────
// app/src/main/java/com/example/runeswipe/model/Spells.kt
// ─────────────────────────────────────────────────────────────────────────────
package com.example.runeswipe.model

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

// object Runes {
//     // Example reference rune shapes (for display or manual lookup)
//     val CircleCCW = RuneTemplate(
//         id = "circle_ccw",
//         name = "Circle (CCW)",
//         strokes = listOf(
//             (0..360 step 15).map { deg ->
//                 val rad = Math.toRadians(deg.toDouble())
//                 Point(
//                     (0.5 + 0.4 * cos(rad)).toFloat(),
//                     (0.5 + 0.4 * sin(rad)).toFloat(),
//                     t = deg.toFloat()  // dummy time so structure matches model
//                 )
//             }
//         )
//     )

//     val Cross = RuneTemplate(
//         id = "cross",
//         name = "Cross",
//         strokes = listOf(
//             (0..100 step 10).map { y -> Point(0.5f, y / 100f, t = y.toFloat()) },
//             (0..100 step 10).map { x -> Point(x / 100f, 0.5f, t = (100 + x).toFloat()) }
//         )
//     )

//     val All = listOf(CircleCCW, Cross)
// }

object SpellsRepo {
    val Fireball = Spell(
        id = "Fehu",
        name = "Fireball",
        type = SpellType.ATTACK,
        power = 6,
        // rune = Runes.CircleCCW
    )

    val Heal = Spell(
        id = "Lefu",
        name = "Healing Light",
        type = SpellType.HEAL,
        power = 5,
        // rune = Runes.Cross
    )

    val All = listOf(Fireball, Heal)
}
