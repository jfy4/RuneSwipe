package com.example.runeswipe.model

import kotlin.math.*

// --- $N Multistroke Recognizer (Kotlin port) ---
object NRecognizer {
    private const val N = 96
    private const val SIZE = 250f
    private const val ANGLE_RANGE = Math.PI / 2
    private const val ANGLE_STEP = Math.PI / 90

    fun isGestureComplete(strokes: List<List<Point>>): Boolean {
	val allPoints = strokes.flatten()
	if (allPoints.size < 8) return false
	
	val minX = allPoints.minOf { it.x }
	val maxX = allPoints.maxOf { it.x }
	val minY = allPoints.minOf { it.y }
	val maxY = allPoints.maxOf { it.y }
	
	val width = maxX - minX
	val height = maxY - minY
	val aspect = if (height > 0) width / height else 0f
	
	val totalLength = (1 until allPoints.size).sumOf {
            hypot((allPoints[it].x - allPoints[it - 1].x), (allPoints[it].y - allPoints[it - 1].y)).toDouble()
	}
	
	// Reject if too short or too skinny in one dimension
	return totalLength > 0.25 * SIZE && aspect in 0.2f..5f
    }


    fun recognize(
        strokes: List<List<Point>>,
        templates: List<RuneTemplate>
    ): Pair<RuneTemplate?, Double> {
        // Combine strokes in all orders & directions (simplified)
        val perms = generateStrokeOrders(strokes)
        var best: RuneTemplate? = null
        var bestDist = Double.POSITIVE_INFINITY

        for (t in templates) {
            val tproc = scaleToSquare(resample(t.points, N), SIZE).normalize()
            for (p in perms) {
                val proc = scaleToSquare(resample(p.flatten(), N), SIZE).normalize()
                val d = distanceAtBestAngle(proc, tproc)
                if (d < bestDist) { bestDist = d; best = t }
            }
        }
        return best to bestDist
    }

    // --- helper functions copied from DollarOneRecognizer ---
    private fun resample(points: List<Point>, n: Int): List<Point> {
        val I = pathLength(points) / (n - 1)
        var D = 0f
        val newPts = mutableListOf<Point>()
        newPts += points.first()
        var lastPoint = points.first()
        for (i in 1 until points.size) {
            val d = dist(points[i - 1], points[i])
            if (D + d >= I) {
                val t = (I - D) / d
                val qx = points[i - 1].x + t * (points[i].x - points[i - 1].x)
                val qy = points[i - 1].y + t * (points[i].y - points[i - 1].y)
                val q = Point(qx, qy)
                newPts += q
                lastPoint = q
                D = 0f
            } else {
                D += d
            }
        }
        while (newPts.size < n) newPts += lastPoint
        return newPts
    }

    private fun pathLength(pts: List<Point>) =
        (1 until pts.size).sumOf { dist(pts[it - 1], pts[it]).toDouble() }.toFloat()

    private fun dist(a: Point, b: Point) = hypot(a.x - b.x, a.y - b.y)
    private fun List<Point>.centroid() =
        Point(sumOf { it.x.toDouble() }.toFloat() / size,
              sumOf { it.y.toDouble() }.toFloat() / size)

    private fun List<Point>.normalize(): List<Point> {
        val c = centroid()
        return map { Point(it.x - c.x, it.y - c.y) }
    }

    private fun scaleToSquare(points: List<Point>, size: Float): List<Point> {
        val minX = points.minOf { it.x }
        val maxX = points.maxOf { it.x }
        val minY = points.minOf { it.y }
        val maxY = points.maxOf { it.y }
        val scale = max(maxX - minX, maxY - minY)
        return points.map {
            Point((it.x - minX) / scale * size, (it.y - minY) / scale * size)
        }
    }

    private fun distanceAtBestAngle(points: List<Point>, template: List<Point>): Double {
        var a = -ANGLE_RANGE
        var b = ANGLE_RANGE
        val threshold = Math.toRadians(2.0)
        var x1 = PHI * a + (1 - PHI) * b
        var f1 = distanceAtAngle(points, template, x1)
        var x2 = (1 - PHI) * a + PHI * b
        var f2 = distanceAtAngle(points, template, x2)
        while (abs(b - a) > threshold) {
            if (f1 < f2) { b = x2; x2 = x1; f2 = f1; x1 = PHI * a + (1 - PHI) * b; f1 = distanceAtAngle(points, template, x1) }
            else { a = x1; x1 = x2; f1 = f2; x2 = (1 - PHI) * a + PHI * b; f2 = distanceAtAngle(points, template, x2) }
        }
        return min(f1, f2)
    }

    private fun distanceAtAngle(points: List<Point>, template: List<Point>, theta: Double): Double {
	val cosT = cos(theta)
	val sinT = sin(theta)
	val rotated = points.map {
            Point(
		(it.x * cosT - it.y * sinT).toFloat(),
		(it.x * sinT + it.y * cosT).toFloat()
            )
	}
	return pathDistance(rotated, template)
    }

    private fun pathDistance(a: List<Point>, b: List<Point>) =
        a.indices.sumOf { dist(a[it], b[it]).toDouble() } / a.size

    private val PHI = 0.5 * (-1.0 + sqrt(5.0))

    private fun generateStrokeOrders(strokes: List<List<Point>>): List<List<List<Point>>> {
	if (strokes.size <= 1) return listOf(strokes)
	
	val results = mutableListOf<List<List<Point>>>()
	
	fun permute(prefix: List<List<Point>>, remaining: List<List<Point>>) {
            if (remaining.isEmpty()) {
		results += prefix
            } else {
		for (i in remaining.indices) {
                    val nextRemaining = remaining.toMutableList()
                    val stroke = nextRemaining.removeAt(i)
                    // recurse with normal and reversed version of each stroke
		    permute(prefix + listOf(stroke), nextRemaining)
		    permute(prefix + listOf(stroke.reversed()), nextRemaining)
		}
            }
	}
	
	permute(emptyList(), strokes)
	return results
    }
}
