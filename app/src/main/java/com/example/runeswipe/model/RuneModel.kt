// ───────────────────────────────────────────────
// app/src/main/java/com/example/runeswipe/model/RuneModel.kt
// ───────────────────────────────────────────────
package com.example.runeswipe.model

import ai.onnxruntime.*
import android.content.Context
import java.nio.FloatBuffer
import kotlin.math.max
import kotlin.math.roundToInt
import org.json.JSONArray
import java.io.InputStream

object RuneModel {
    private var env: OrtEnvironment? = null
    private var session: OrtSession? = null
    private var labels: List<String>? = null
    private const val MAX_POINTS = 100  // must match training, fixed at 100

    fun load(context: Context) {
        if (session != null) return
        env = OrtEnvironment.getEnvironment()
        val opts = OrtSession.SessionOptions()
        val modelBytes = context.assets.open("rune_seq.onnx").readBytes()
        session = env!!.createSession(modelBytes, opts)
        // Load labels from a JSON file
        val labelsInputStream: InputStream = context.assets.open("labels.json")
        val labelsJsonString = labelsInputStream.bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(labelsJsonString)
        labels = List(jsonArray.length()) { jsonArray.getString(it) }
        // labels = listOf("Fehu", "Lefu") // update for your dataset
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
    fun preprocess(strokes: List<List<Point>>): FloatArray {
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

	// --- build Δx, Δy, Δt, pen_lift sequence ---
	val seq = mutableListOf<Float>()
	var prevX = 0f
	var prevY = 0f
	var prevT = 0f
	var firstPoint = true

	for (stroke in strokes) {
            if (stroke.isEmpty()) continue
            for ((j, p) in stroke.withIndex()) {
		val x = (p.x - minX) * scale
		val y = (p.y - minY) * scale
		val t = (p.t - t0) / tSpan
		if (firstPoint) {
                    prevX = x; prevY = y; prevT = t
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
		prevX = x; prevY = y; prevT = t
            }
	}

	// ── Denoise / remove nearly-zero motion ──
	val filtered = mutableListOf<Float>()
	for (i in 0 until seq.size step 4) {
            val dx = seq[i]; val dy = seq[i + 1]
            val mag = kotlin.math.sqrt(dx * dx + dy * dy)
            if (mag > 1e-5f) {
		filtered.add(dx)
		filtered.add(dy)
		filtered.add(seq[i + 2])
		filtered.add(seq[i + 3])
            }
	}

	// ── Standardize (zero mean, unit var) ──
	if (filtered.isNotEmpty()) {
            val n = filtered.size / 4
            val means = FloatArray(4)
            val stds = FloatArray(4)
            // compute means
            for (i in 0 until n) {
		for (j in 0 until 4) {
                    means[j] += filtered[i * 4 + j]
		}
            }
            for (j in 0 until 4) means[j] /= n.toFloat()
            // compute stds
            for (i in 0 until n) {
		for (j in 0 until 4) {
                    val d = filtered[i * 4 + j] - means[j]
                    stds[j] += d * d
		}
            }
            for (j in 0 until 4) stds[j] = kotlin.math.sqrt(stds[j] / n.toFloat()).coerceAtLeast(1e-6f)
            // normalize in place
            for (i in 0 until n) {
		for (j in 0 until 4) {
                    val idx = i * 4 + j
                    filtered[idx] = (filtered[idx] - means[j]) / stds[j]
		}
            }
	}

	// ── Pad / Trim ──
	val arr = FloatArray(MAX_POINTS * 4)
	val numPoints = filtered.size / 4
	if (numPoints > MAX_POINTS) {
            val step = (numPoints - 1).toFloat() / (MAX_POINTS - 1)
            for (i in 0 until MAX_POINTS) {
		val src = (i * step).roundToInt() * 4
		for (j in 0 until 4) {
                    arr[i * 4 + j] = filtered.getOrElse(src + j) { 0f }
		}
            }
	} else {
            for (i in filtered.indices) arr[i] = filtered[i]
	}
	return arr
    }
}
