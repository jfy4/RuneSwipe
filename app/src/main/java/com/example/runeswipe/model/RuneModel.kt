// ───────────────────────────────────────────────
// app/src/main/java/com/example/runeswipe/model/RuneModel.kt
// ───────────────────────────────────────────────
package com.example.runeswipe.model

import ai.onnxruntime.*
import android.content.Context
import java.nio.FloatBuffer
import kotlin.math.max

object RuneModel {
    private var env: OrtEnvironment? = null
    private var session: OrtSession? = null
    private var labels: List<String>? = null
    private const val MAX_POINTS = 72  // must match training

    fun load(context: Context) {
        if (session != null) return
        env = OrtEnvironment.getEnvironment()
        val opts = OrtSession.SessionOptions()
        val modelBytes = context.assets.open("rune_seq.onnx").readBytes()
        session = env!!.createSession(modelBytes, opts)
        labels = listOf("Fehu", "Lefu") // update for your dataset
    }

    fun predict(strokes: List<List<Point>>): String? {
        val env = env ?: return null
        val session = session ?: return null
        val input = preprocess(strokes)
        val shape = longArrayOf(1, MAX_POINTS.toLong(), 4) // 4 features: dx, dy, dt, pen
        val tensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(input), shape)
        val output = session.run(mapOf(session.inputNames.iterator().next() to tensor))
        val logits = (output[0].value as Array<FloatArray>)[0]
        val predIdx = logits.indices.maxByOrNull { logits[it] } ?: return null
        return labels?.get(predIdx)
    }

    // faithful port of Python load_trace()
    private fun preprocess(strokes: List<List<Point>>): FloatArray {
        val pts = strokes.flatten()
        if (pts.isEmpty()) return FloatArray(MAX_POINTS * 4)

        // --- normalize spatial coords by bounding box ---
        val xs = pts.map { it.x }
        val ys = pts.map { it.y }
        val minX = xs.minOrNull() ?: 0f
        val maxX = xs.maxOrNull() ?: 0f
        val minY = ys.minOrNull() ?: 0f
        val maxY = ys.maxOrNull() ?: 0f
        val w = max(maxX - minX, 1e-6f)
        val h = max(maxY - minY, 1e-6f)
        val scale = 1f / max(w, h)

        // --- normalize time to [0,1] ---
        val t0 = pts.first().t
        val tN = pts.last().t
        val tSpan = max(tN - t0, 1e-6f)

        val seq = mutableListOf<Float>()
        var prevX = 0f
        var prevY = 0f
        var prevT = 0f
        var firstPoint = true

        for (s in strokes) {
            // Python had: if len(s) > 50: s = s[::2]
            // val stroke = if (s.size > 50) s.filterIndexed { i, _ -> i % 2 == 0 } else s
            val stroke = s
            if (stroke.isEmpty()) continue

            for ((j, p) in stroke.withIndex()) {
                val x = (p.x - minX) * scale
                val y = (p.y - minY) * scale
                val t = (p.t - t0) / tSpan

                if (firstPoint) {
                    prevX = x
                    prevY = y
                    prevT = t
                    firstPoint = false
                    continue
                }

                val dx = x - prevX
                val dy = y - prevY
                val dt = t - prevT
                val penLift = if (j == 0) 1f else 0f

                seq.add(dx)
                seq.add(dy)
                seq.add(dt)
                seq.add(penLift)

                prevX = x
                prevY = y
                prevT = t
            }
        }

        // --- pad/trim to MAX_POINTS × 4 ---
        val arr = FloatArray(MAX_POINTS * 4)
        val copyLen = minOf(seq.size, arr.size)
        for (i in 0 until copyLen) arr[i] = seq[i]
        return arr
    }
}
// // ───────────────────────────────────────────────
// // app/src/main/java/com/example/runeswipe/model/RuneModel.kt
// // ───────────────────────────────────────────────
// package com.example.runeswipe.model

// import ai.onnxruntime.*
// import android.content.Context
// import java.nio.FloatBuffer

// object RuneModel {
//     private var env: OrtEnvironment? = null
//     private var session: OrtSession? = null
//     private var labels: List<String>? = null
//     private const val MAX_POINTS = 165  // match training

//     fun load(context: Context) {
//         if (session != null) return
//         env = OrtEnvironment.getEnvironment()
//         val opts = OrtSession.SessionOptions()
//         val modelBytes = context.assets.open("rune_seq.onnx").readBytes()
//         session = env!!.createSession(modelBytes, opts)

//         // You can store label order manually or from JSON
//         labels = listOf("Fehu", "Unknown") // update to match your dataset
//     }

//     fun predict(strokes: List<List<Point>>): String? {
//         val env = env ?: return null
//         val session = session ?: return null
//         val input = preprocess(strokes)
// 	val shape = longArrayOf(1, MAX_POINTS.toLong(), 3)
// 	val tensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(input), shape)
//         // val tensor = OnnxTensor.createTensor(env, input, longArrayOf(1, MAX_POINTS.toLong(), 3))

//         val output = session.run(mapOf("trace" to tensor))
//         val logits = (output[0].value as Array<FloatArray>)[0]
//         val predIdx = logits.indices.maxByOrNull { logits[it] } ?: return null
//         return labels?.get(predIdx)
//     }

//     private fun preprocess(strokes: List<List<Point>>): FloatArray {
// 	val pts = strokes.flatten()
// 	if (pts.isEmpty()) return FloatArray(MAX_POINTS * 4)

// 	// --- normalize spatial coordinates ---
// 	val xs = pts.map { it.x }
// 	val ys = pts.map { it.y }
// 	val minX = xs.min(); val maxX = xs.max()
// 	val minY = ys.min(); val maxY = ys.max()
// 	val w = (maxX - minX).coerceAtLeast(1e-6f)
// 	val h = (maxY - minY).coerceAtLeast(1e-6f)
// 	val scale = 1f / maxOf(w, h)

// 	// --- normalize time to [0, 1] ---
// 	val t0 = pts.first().t
// 	val tN = pts.last().t
// 	val tSpan = (tN - t0).coerceAtLeast(1e-6f)

// 	val seq = mutableListOf<Float>()
// 	var prevX = 0f
// 	var prevY = 0f
// 	var prevT = 0f
// 	var first = true

// 	for (s in strokes) {
// 	    for ((j, p) in s.withIndex()) {
// 		val x = (p.x - minX) * scale
// 		val y = (p.y - minY) * scale
// 		val t = (p.t - t0) / tSpan
// 		if (first) {
// 		    prevX = x
// 		    prevY = y
// 		    prevT = t
// 		    first = false
// 		    continue
// 		}
// 		val dx = x - prevX
// 		val dy = y - prevY
// 		val dt = t - prevT
// 		val pen = if (j == 0) 1f else 0f
// 		seq.add(dx)
// 		seq.add(dy)
// 		seq.add(dt)
// 		seq.add(pen)
// 		prevX = x
// 		prevY = y
// 		prevT = t
// 	    }
// 	}

// 	// --- pad or trim to MAX_POINTS ---
// 	val arr = FloatArray(MAX_POINTS * 4)
// 	val copyLen = minOf(seq.size, arr.size)
// 	for (i in 0 until copyLen) arr[i] = seq[i]
// 	return arr
//     }
//     // private fun preprocess(strokes: List<List<Point>>): FloatArray {
//     //     val pts = strokes.flatten()
//     //     if (pts.isEmpty()) return FloatArray(MAX_POINTS * 3)

//     //     val xs = pts.map { it.x }
//     //     val ys = pts.map { it.y }
//     //     val minX = xs.min(); val maxX = xs.max()
//     //     val minY = ys.min(); val maxY = ys.max()
//     //     val w = (maxX - minX).coerceAtLeast(1e-6f)
//     //     val h = (maxY - minY).coerceAtLeast(1e-6f)
//     //     val scale = 1f / maxOf(w, h)

//     //     val seq = mutableListOf<Float>()
//     //     var prevX = 0f; var prevY = 0f
//     //     var first = true
//     //     for (s in strokes) {
//     //         for ((j, p) in s.withIndex()) {
//     //             val x = (p.x - minX) * scale
//     //             val y = (p.y - minY) * scale
//     //             if (first) { prevX = x; prevY = y; first = false; continue }
//     //             val dx = x - prevX
//     //             val dy = y - prevY
//     //             val pen = if (j == 0) 1f else 0f
//     //             seq.add(dx); seq.add(dy); seq.add(pen)
//     //             prevX = x; prevY = y
//     //         }
//     //     }

//     //     val arr = FloatArray(MAX_POINTS * 3)
//     //     val copyLen = minOf(seq.size, arr.size)
//     //     for (i in 0 until copyLen) arr[i] = seq[i]
//     //     return arr
//     // }
// }
