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
import android.util.Log
import org.json.JSONObject


object RuneModel {
    private var env: OrtEnvironment? = null
    private var session: OrtSession? = null
    private var labels: List<String>? = null
    private const val MAX_POINTS = 100  // must match training, fixed at 100

    fun load(context: Context) {
        if (session != null) return
	try {
	    // --- temporary test ---
	    val jsonStr = context.assets.open("sample_trace.json")
		.bufferedReader().use { it.readText() }

	    val strokes = parseStrokeJson(jsonStr)  // same parser as before
	    val arr = preprocess(strokes)

	    android.util.Log.d("RuneSwipe", "First 8 values: ${arr.take(8)}")
	} catch (e: Exception) {
	    android.util.Log.e("RuneSwipe", "Preprocess test failed: ${e.message}")
	}
        env = OrtEnvironment.getEnvironment()
        val opts = OrtSession.SessionOptions()
        val modelBytes = context.assets.open("rune_seq.onnx").readBytes()
        session = env!!.createSession(modelBytes, opts)
        // Load labels from a JSON file
        val labelsInputStream: InputStream = context.assets.open("labels.json")
        val labelsJsonString = labelsInputStream.bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(labelsJsonString)
        labels = List(jsonArray.length()) { jsonArray.getString(it) }
	Log.d("RuneSwipe", "Loaded labels: $labels")
        // labels = listOf("Fehu", "Lefu") // update for your dataset
    }


    // Parses {"strokes":[ [ {x,y,t}, ... ], [ ... ] ]}

// Parses {"strokes":[ [ {"x":..,"y":..,"t":..}, ... ], [ ... ] ]}
    private fun parseStrokeJson(json: String): List<List<Point>> {
	val root = JSONObject(json)
	val strokesArr = root.optJSONArray("strokes") ?: JSONArray()
	val strokes = mutableListOf<List<Point>>()

	for (i in 0 until strokesArr.length()) {
            val strokeArr = strokesArr.optJSONArray(i) ?: JSONArray()
            val pts = ArrayList<Point>(strokeArr.length())
            for (j in 0 until strokeArr.length()) {
		val p = strokeArr.getJSONObject(j)

		val x = p.optDouble("x", 0.0).toFloat()
		val y = p.optDouble("y", 0.0).toFloat()

		// t may be int/long/double/string; normalize to Float
		// val tAny = p.opt("t")
		// val tFloat: Float = when (tAny) {
                //     is Number -> tAny.toDouble().toFloat()
                //     is String -> (tAny.toDoubleOrNull() ?: 0.0).toFloat()
                //     else -> 0f
		// }
		val tAny = p.opt("t")
		val tDouble: Double = when (tAny) {
                    is Number -> tAny.toDouble()
                    is String -> tAny.toDoubleOrNull() ?: 0.0
                    else -> 0.0
		}

		pts.add(Point(x, y, tDouble))
            }
            strokes.add(pts)
	}
	return strokes
    }

    private fun parseStrokeJson(stream: java.io.InputStream): List<List<Point>> =
	parseStrokeJson(stream.bufferedReader().use { it.readText() })


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
	if (strokes.isEmpty() || strokes.all { it.isEmpty() }) {
            return FloatArray(MAX_POINTS * 4)
	}


	// --- collect all points ---
	val ptsAll = strokes.flatten()
	if (ptsAll.isEmpty()) return FloatArray(MAX_POINTS * 4)

	// --- normalize spatial coords by bounding box ---
	val xs = ptsAll.map { it.x }
	val ys = ptsAll.map { it.y }
	val minX = xs.minOrNull() ?: 0f
	val maxX = xs.maxOrNull() ?: 0f
	val minY = ys.minOrNull() ?: 0f
	val maxY = ys.maxOrNull() ?: 0f
	val w = max(maxX - minX, 1e-6f)
	val h = max(maxY - minY, 1e-6f)
	val scale = 1f / max(w, h)

	// --- normalize time to [0,1] ---
	val t0 = ptsAll.first().t
	val tN = ptsAll.last().t
	val tSpan = kotlin.math.max(tN - t0, 1e-6)

	if (strokes.isNotEmpty() && strokes.first().isNotEmpty()) {
	    val tNorms = strokes.first()
		.take(5)
		.map { p: Point -> ((p.t - t0) / tSpan).toFloat() }
	    Log.d("RuneSwipe", "tNorm first 5: $tNorms")
	}

	// --- flatten all strokes into (x, y, t, pen) points ---
	val pts = mutableListOf<FloatArray>()
	for (stroke in strokes) {
	    if (stroke.isEmpty()) continue
	    for ((j, p) in stroke.withIndex()) {
		val x = (p.x - minX) * scale
		val y = (p.y - minY) * scale
		val t = ((p.t - t0) / tSpan).toFloat()   // cast only here
		val pen = if (j == 0) 1f else 0f
		pts.add(floatArrayOf(x, y, t, pen))
	    }
	}
	var nPts = pts.size
	// --- Downsample original points to MAX_POINTS + 1 uniformly in index ---
	val targetPts = MAX_POINTS + 1
	val ptsResampled = mutableListOf<FloatArray>()
	if (nPts > targetPts) {
            val step = (nPts - 1).toFloat() / (targetPts - 1).toFloat()
            for (i in 0 until targetPts) {
		val idx = (i * step).roundToInt().coerceIn(0, nPts - 1)
		ptsResampled.add(pts[idx])
            }
	} else if (nPts < targetPts) {
            ptsResampled.addAll(pts)
            val last = pts.last()
            repeat(targetPts - nPts) { ptsResampled.add(last.copyOf()) }
	} else {
            ptsResampled.addAll(pts)
	}

	// --- Compute deltas between consecutive points ---
	val seq = mutableListOf<FloatArray>()
	for (i in 1 until ptsResampled.size) {
            val prev = ptsResampled[i - 1]
            val curr = ptsResampled[i]
            val dx = curr[0] - prev[0]
            val dy = curr[1] - prev[1]
            val dt = curr[2] - prev[2]
            val pen = curr[3] // pen value at current point
            seq.add(floatArrayOf(dx, dy, dt, pen))
	}

	// ── Denoise / remove nearly-zero motion ──
	val filtered = mutableListOf<FloatArray>()
	for (v in seq) {
            val dx = v[0]
            val dy = v[1]
            val mag = kotlin.math.sqrt(dx * dx + dy * dy)
            if (mag > 1e-5f) filtered.add(v)
	}

	// ── Standardize (zero mean, unit var) ──
	if (filtered.isNotEmpty()) {
            val n = filtered.size
            val means = FloatArray(4)
            val stds = FloatArray(4)

            for (v in filtered) for (j in 0 until 4) means[j] += v[j]
            for (j in 0 until 4) means[j] /= n.toFloat()

            for (v in filtered) for (j in 0 until 4) {
				    val d = v[j] - means[j]
				    stds[j] += d * d
				}
            for (j in 0 until 4) {
		stds[j] = kotlin.math.sqrt(stds[j] / n.toFloat()).coerceAtLeast(1e-6f)
            }

            for (v in filtered) for (j in 0 until 4) {
				    v[j] = (v[j] - means[j]) / stds[j]
				}
	}

	// --- Pad / Trim to exactly MAX_POINTS × 4 ---
	val arr = FloatArray(MAX_POINTS * 4)
	val numPoints = filtered.size
	if (numPoints >= MAX_POINTS) {
            val step = (numPoints - 1).toFloat() / (MAX_POINTS - 1).toFloat()
            for (i in 0 until MAX_POINTS) {
		val src = (i * step).roundToInt().coerceIn(0, numPoints - 1)
		val v = filtered[src]
		for (j in 0 until 4) arr[i * 4 + j] = v[j]
            }
	} else {
            var idx = 0
            for (v in filtered) {
		for (j in 0 until 4) arr[idx++] = v[j]
            }
            // Remaining entries default to 0f
	}

	return arr
    }
}
