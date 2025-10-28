// ─────────────────────────────────────────────────────────────────────────────
// app/src/main/java/com/example/runeswipe/model/Spells.kt
// ─────────────────────────────────────────────────────────────────────────────
package com.example.runeswipe.model

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

object Runes {
    // Example reference rune shapes (for display or manual lookup)
    val CircleCCW = RuneTemplate(
        id = "circle_ccw",
        name = "Circle (CCW)",
        strokes = listOf(
            (0..360 step 15).map { deg ->
                val rad = Math.toRadians(deg.toDouble())
                Point(
                    (0.5 + 0.4 * cos(rad)).toFloat(),
                    (0.5 + 0.4 * sin(rad)).toFloat(),
                    t = deg.toFloat()  // dummy time so structure matches model
                )
            }
        )
    )

    val Cross = RuneTemplate(
        id = "cross",
        name = "Cross",
        strokes = listOf(
            (0..100 step 10).map { y -> Point(0.5f, y / 100f, t = y.toFloat()) },
            (0..100 step 10).map { x -> Point(x / 100f, 0.5f, t = (100 + x).toFloat()) }
        )
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
// // ─────────────────────────────────────────────────────────────────────────────
// // app/src/main/java/com/example/runeswipe/model/Spells.kt
// // ─────────────────────────────────────────────────────────────────────────────
// package com.example.runeswipe.model

// // Sample rune templates (very simplified shapes). In a real game you'd define
// // richer point paths or load from CSV.
// object Runes {

//     // Counter-clockwise circle
//     val CircleCCW = RuneTemplate(
//         id = "circle_ccw",
//         name = "Circle (CCW)",
//         points = (0..360 step 15).map { deg ->
//             val rad = Math.toRadians(deg.toDouble())
//             Point(
//                 (0.5 + 0.4 * kotlin.math.cos(rad)).toFloat(),
//                 (0.5 + 0.4 * kotlin.math.sin(rad)).toFloat()
//             )
//         }
//     )

//     // Clockwise circle (just reverse angle)
//     val CircleCW = RuneTemplate(
//         id = "circle_cw",
//         name = "Circle (CW)",
//         points = (0..360 step 15).map { deg ->
//             val rad = Math.toRadians(360 - deg.toDouble())
//             Point(
//                 (0.5 + 0.4 * kotlin.math.cos(rad)).toFloat(),
//                 (0.5 + 0.4 * kotlin.math.sin(rad)).toFloat()
//             )
//         }
//     )

//     // Plus-shaped cross (vertical stroke then horizontal stroke)
//     val Cross = RuneTemplate(
//         id = "cross",
//         name = "Cross",
//         points = buildList {
//             // vertical stroke
//             for (y in 0..100 step 10) add(Point(0.5f, y / 100f))
//             // center connect
//             add(Point(0.5f, 0.5f))
//             // horizontal stroke
//             for (x in 0..100 step 10) add(Point(x / 100f, 0.5f))
//         }
//     )

//     val All = listOf(CircleCCW, CircleCW, Cross)
// }


// // object Runes {
// //     val CircleCCW = RuneTemplate(
// //         id = "circle_ccw",
// //         name = "Circle (CCW)",
// //         points = (0..360 step 15).map { deg ->
// //             val rad = Math.toRadians(deg.toDouble())
// //             // CCW circle centered near (0.5,0.5) in unit square then scaled
// //             Point((0.5 + 0.4 * kotlin.math.cos(rad)).toFloat(), (0.5 + 0.4 * kotlin.math.sin(rad)).toFloat())
// //         }
// //     )

// //     val Cross = RuneTemplate(
// //         id = "cross",
// //         name = "Cross",
// //         points = buildList {
// //             // Vertical line
// //             for (t in 0..10) add(Point(0.5f, 0.1f + 0.8f * (t / 10f)))
// //             // Move & Horizontal line
// //             for (t in 0..10) add(Point(0.1f + 0.8f * (t / 10f), 0.5f))
// //         }
// //     )

// //     val All = listOf(CircleCCW, Cross)
// // }

// object SpellsRepo {
//     val Fireball = Spell(
//         id = "fireball",
//         name = "Fireball",
//         type = SpellType.ATTACK,
//         power = 6,
//         rune = Runes.CircleCCW
//     )

//     val Heal = Spell(
//         id = "heal",
//         name = "Healing Light",
//         type = SpellType.HEAL,
//         power = 5,
//         rune = Runes.Cross
//     )

//     val All = listOf(Fireball, Heal)
// }
