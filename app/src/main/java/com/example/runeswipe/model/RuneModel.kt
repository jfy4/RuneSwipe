// ───────────────────────────────────────────────
// app/src/main/java/com/example/runeswipe/model/RuneModel.kt
// ───────────────────────────────────────────────
package com.example.runeswipe.model

import ai.onnxruntime.*
import android.content.Context
import java.nio.FloatBuffer
import org.json.JSONArray
import java.io.InputStream
import android.util.Log
import org.json.JSONObject


object RuneModel {
    private var env: OrtEnvironment? = null
    private var session: OrtSession? = null
    private var labels: List<String>? = null

    fun load(context: Context) {
        if (session != null) return
	try {
	    // --- temporary test ---
	    val jsonStr = context.assets.open("sample_trace.json")
		.bufferedReader().use { it.readText() }

	    val strokes = parseStrokeJson(jsonStr)  // same parser as before
	    val (arr, count) = flattenRawPoints(strokes)

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

	// Convert strokes to raw (x,y,t,pen)
	val (data, count) = flattenRawPoints(strokes)  // we add this helper below
	if (count <= 1) return null  // transformers need at least 2 points to make deltas

	val shape = longArrayOf(1, count.toLong(), 4)
	val tensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(data), shape)

	val output = session.run(mapOf(session.inputNames.iterator().next() to tensor))
	val logits = (output[0].value as Array<FloatArray>)[0]
	val predIdx = logits.indices.maxByOrNull { logits[it] } ?: return null
	return labels?.get(predIdx)
    }

    private fun flattenRawPoints(strokes: List<List<Point>>): Pair<FloatArray, Int> {
	// Count total points
	val count = strokes.sumOf { it.size }
	val out = FloatArray(count * 4)

	var idx = 0
	for (stroke in strokes) {
            if (stroke.isEmpty()) continue
            for ((j, p) in stroke.withIndex()) {
		out[idx++] = p.x
		out[idx++] = p.y
		out[idx++] = p.t.toFloat()
		out[idx++] = if (j == 0) 1f else 0f
            }
	}

	return out to count
    }

}
