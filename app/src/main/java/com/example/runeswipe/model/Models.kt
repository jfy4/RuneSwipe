
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
enum class StatusEffect { NONE, POISON, SHIELD, IMMOBILE }

data class Stats(
    var life: Int = 30,
    var strength: Int = 5,
    var defense: Int = 5,
    var constitution: Int = 5,
    var speed: Int = 5,
    var dexterity: Int = 5,
)

data class Player(
    val name: String,
    val stats: Stats,
    var xp: Int = 0,
    var level: Int = 1,
    var status: StatusEffect = StatusEffect.NONE
) {
    var cooldownMs by mutableStateOf(0L)

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
    // val playerStatusInflict StatusEffect = StatusEffect.NONE,
    val statusInflict: StatusEffect = StatusEffect.NONE,
)

// // ─────────────────────────────────────────────────────────────────────────────
// // app/src/main/java/com/example/runeswipe/model/Models.kt
// // ─────────────────────────────────────────────────────────────────────────────
// package com.example.runeswipe.model

// import androidx.compose.runtime.mutableStateOf
// import androidx.compose.runtime.getValue
// import androidx.compose.runtime.setValue
// import kotlin.math.*

// enum class SpellType { ATTACK, DEFENSE, HEAL, STATUS }

// enum class StatusEffect { NONE, POISON, SHIELD, IMMOBILE }

// data class Stats(
//     var life: Int = 30,
//     var strength: Int = 5,
//     var defense: Int = 5,
//     var constitution: Int = 5,
//     var speed: Int = 5,
//     var dexterity: Int = 5,
// )

// data class Player(
//     val name: String,
//     val stats: Stats,
//     var xp: Int = 0,
//     var level: Int = 1,
//     var status: StatusEffect = StatusEffect.NONE
// ) {
//     var cooldownMs by mutableStateOf(0L)

//     companion object {
//         fun default(name: String) = Player(name, Stats())
//     }
// }

// data class Spell(
//     val id: String,
//     val name: String,
//     val type: SpellType,
//     val power: Int,
//     val rune: RuneTemplate,
//     val statusInflict: StatusEffect = StatusEffect.NONE,
// )

// // ─────────────────────────────────────────────────────────────────────────────
// // Rune templates and $1 recognizer
// // ─────────────────────────────────────────────────────────────────────────────
// data class Point(val x: Float, val y: Float, val t: Float)

// data class RuneTemplate(
//     val id: String,
//     val name: String,
//     val points: List<Point>
// )

// object DollarOneRecognizer {
//     private const val N = 96
//     private const val SIZE = 250f
//     private const val ANGLE_RANGE = Math.PI / 2 // ±90°
//     private const val ANGLE_STEP = Math.PI / 90 // 2°

//     fun recognize(candidate: List<Point>, templates: List<RuneTemplate>): Pair<RuneTemplate?, Double> {
//         if (candidate.size < 10) return null to Double.POSITIVE_INFINITY
//         val processed = scaleToSquare(resample(candidate, N), SIZE).normalize()
//         var best: RuneTemplate? = null
//         var bestDist = Double.POSITIVE_INFINITY
//         for (t in templates) {
//             val tp = scaleToSquare(resample(t.points, N), SIZE).normalize()
//             val d = distanceAtBestAngle(processed, tp)
//             if (d < bestDist) { bestDist = d; best = t }
//         }
//         return best to bestDist
//     }

//     private fun resample(points: List<Point>, n: Int): List<Point> {
// 	val I = pathLength(points) / (n - 1)
// 	var D = 0f
// 	val newPts = mutableListOf<Point>()
// 	newPts += points.first()
// 	var lastPoint = points.first()
	
// 	for (i in 1 until points.size) {
//             val d = dist(points[i - 1], points[i])
//             if (D + d >= I) {
// 		val t = (I - D) / d
// 		val qx = points[i - 1].x + t * (points[i].x - points[i - 1].x)
// 		val qy = points[i - 1].y + t * (points[i].y - points[i - 1].y)
// 		val q = Point(qx, qy)
// 		newPts += q
// 		lastPoint = q
// 		D = 0f
//             } else {
// 		D += d
//             }
// 	}
	
// 	// Add the final point if we need more to reach n
// 	while (newPts.size < n) {
//             newPts += lastPoint
// 	}
	
// 	return newPts
//     }


//     // private fun resample(points: List<Point>, n: Int): List<Point> {
//     //     val I = pathLength(points) / (n - 1)
//     //     var D = 0f
//     //     val newPts = mutableListOf<Point>()
//     //     newPts += points.first()
//     //     for (i in 1 until points.size) {
//     //         val d = dist(points[i-1], points[i])
//     //         if (D + d >= I) {
//     //             var qx = points[i-1].x + ((I - D) / d) * (points[i].x - points[i-1].x)
//     //             var qy = points[i-1].y + ((I - D) / d) * (points[i].y - points[i-1].y)
//     //             val q = Point(qx, qy)
//     //             newPts += q
//     //             points[i-1] = q // <-- not allowed since list may be immutable; handle differently
//     //         }
//     //         D += d
//     //     }
//     //     // The above in-place step is tricky in Kotlin immutable lists; do a safer pass:
//     //     if (newPts.size < n) newPts += points.last()
//     //     return newPts.take(n)
//     // }

//     private fun pathLength(points: List<Point>): Float =
//         (1 until points.size).sumOf { dist(points[it-1], points[it]).toDouble() }.toFloat()

//     private fun dist(a: Point, b: Point): Float = hypot(a.x - b.x, a.y - b.y)

//     private fun centroid(points: List<Point>): Point {
//         val x = points.sumOf { it.x.toDouble() } / points.size
//         val y = points.sumOf { it.y.toDouble() } / points.size
//         return Point(x.toFloat(), y.toFloat())
//     }

//     private fun indicativeAngle(points: List<Point>): Double {
//         val c = centroid(points)
//         return atan2((points[0].y - c.y), (points[0].x - c.x)).toDouble()
//     }

//     private fun rotateBy(points: List<Point>, angle: Double): List<Point> {
//         val c = centroid(points)
//         val cos = cos(angle)
//         val sin = sin(angle)
//         return points.map { p ->
//             val qx = (p.x - c.x) * cos - (p.y - c.y) * sin + c.x
//             val qy = (p.x - c.x) * sin + (p.y - c.y) * cos + c.y
//             Point(qx.toFloat(), qy.toFloat())
//         }
//     }

//     private fun scaleToSquare(points: List<Point>, size: Float): List<Point> {
//         val xs = points.map { it.x }
//         val ys = points.map { it.y }
//         val minX = xs.min(); val maxX = xs.max()
//         val minY = ys.min(); val maxY = ys.max()
//         val scale = max(maxX - minX, maxY - minY)
//         return points.map { p -> Point(((p.x - minX) / scale) * size, ((p.y - minY) / scale) * size) }
//     }

//     private fun translateToOrigin(points: List<Point>): List<Point> {
//         val c = centroid(points)
//         return points.map { p -> Point(p.x - c.x, p.y - c.y) }
//     }

//     private fun List<Point>.normalize(): List<Point> = translateToOrigin(rotateBy(this, -indicativeAngle(this)))

//     private fun pathDistance(a: List<Point>, b: List<Point>): Double =
//         (a.indices).sumOf { dist(a[it], b[it]).toDouble() } / a.size

//     private fun distanceAtAngle(points: List<Point>, template: List<Point>, angle: Double): Double =
//         pathDistance(rotateBy(points, angle), template)

//     private fun distanceAtBestAngle(points: List<Point>, template: List<Point>): Double {
//         var a = -ANGLE_RANGE
//         var b = ANGLE_RANGE
//         val phi = 0.5 * (-1 + sqrt(5.0))
//         var x1 = phi * a + (1 - phi) * b
//         var f1 = distanceAtAngle(points, template, x1)
//         var x2 = (1 - phi) * a + phi * b
//         var f2 = distanceAtAngle(points, template, x2)
//         while (abs(b - a) > ANGLE_STEP) {
//             if (f1 < f2) {
//                 b = x2; x2 = x1; f2 = f1; x1 = phi * a + (1 - phi) * b; f1 = distanceAtAngle(points, template, x1)
//             } else {
//                 a = x1; x1 = x2; f1 = f2; x2 = (1 - phi) * a + phi * b; f2 = distanceAtAngle(points, template, x2)
//             }
//         }
//         return min(f1, f2)
//     }
// }
