package com.harrisonog.taperAndroid.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Displays a line graph showing the taper progress over weeks.
 *
 * @param startPerDay Number of alarms per day at the start
 * @param endPerDay Number of alarms per day at the end
 * @param totalWeeks Total number of weeks in the taper plan
 * @param currentWeek The current week (1-indexed)
 * @param isGoodHabit Whether this is a good habit (ramp up) or taper habit (ramp down)
 * @param primaryColor The color to use for the line and current week highlight
 */
@Composable
fun TaperProgressGraph(
    startPerDay: Int,
    endPerDay: Int,
    totalWeeks: Int,
    currentWeek: Int,
    isGoodHabit: Boolean,
    primaryColor: Color,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Padding for labels
        val paddingBottom = 40f
        val paddingLeft = 75f  // Increased to make room for Y-axis label
        val paddingRight = 20f
        val paddingTop = 20f

        val graphWidth = canvasWidth - paddingLeft - paddingRight
        val graphHeight = canvasHeight - paddingTop - paddingBottom

        // Calculate data points for each week
        val dataPoints = mutableListOf<Pair<Float, Float>>()
        val maxValue = maxOf(startPerDay, endPerDay).toFloat()
        val minValue = 0f

        for (week in 1..totalWeeks) {
            // Calculate average alarms per day for this week
            // Linear interpolation between start and end
            val progress = (week - 1).toFloat() / (totalWeeks - 1).toFloat()
            val alarmsPerDay = startPerDay + (endPerDay - startPerDay) * progress

            val x = paddingLeft + (week - 1) * (graphWidth / (totalWeeks - 1).coerceAtLeast(1))
            val y = paddingTop + graphHeight - (alarmsPerDay - minValue) / (maxValue - minValue) * graphHeight

            dataPoints.add(Pair(x, y))
        }

        // Draw grid lines
        val numHorizontalLines = 5
        for (i in 0..numHorizontalLines) {
            val y = paddingTop + (i * graphHeight / numHorizontalLines)
            drawLine(
                color = gridColor,
                start = Offset(paddingLeft, y),
                end = Offset(paddingLeft + graphWidth, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
            )

            // Y-axis labels
            val value = maxValue - (i * maxValue / numHorizontalLines)
            drawContext.canvas.nativeCanvas.drawText(
                value.roundToInt().toString(),
                paddingLeft - 20f,
                y + 5f,
                android.graphics.Paint().apply {
                    color = textColor.hashCode()
                    textSize = 30f
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
            )
        }

        // Draw the line connecting all points
        val path = Path()
        dataPoints.forEachIndexed { index, point ->
            if (index == 0) {
                path.moveTo(point.first, point.second)
            } else {
                path.lineTo(point.first, point.second)
            }
        }

        drawPath(
            path = path,
            color = primaryColor.copy(alpha = 0.6f),
            style = Stroke(
                width = 4f,
                cap = StrokeCap.Round
            )
        )

        // Draw points for each week
        dataPoints.forEachIndexed { index, point ->
            val week = index + 1
            val isCurrentWeek = week == currentWeek

            // Draw larger circle for current week
            if (isCurrentWeek) {
                // Highlight circle
                drawCircle(
                    color = primaryColor.copy(alpha = 0.2f),
                    radius = 20f,
                    center = Offset(point.first, point.second)
                )
            }

            // Draw point
            drawCircle(
                color = if (isCurrentWeek) primaryColor else primaryColor.copy(alpha = 0.8f),
                radius = if (isCurrentWeek) 10f else 6f,
                center = Offset(point.first, point.second)
            )

            // Draw week labels on X-axis
            drawContext.canvas.nativeCanvas.drawText(
                "W$week",
                point.first,
                canvasHeight - paddingBottom + 30f,
                android.graphics.Paint().apply {
                    color = if (isCurrentWeek) primaryColor.hashCode() else textColor.hashCode()
                    textSize = if (isCurrentWeek) 36f else 30f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = isCurrentWeek
                }
            )
        }

        // Draw Y-axis label
        drawContext.canvas.nativeCanvas.apply {
            save()
            rotate(-90f, 20f, canvasHeight / 2)
            drawText(
                "Alarms/Day",
                20f,
                canvasHeight / 2,
                android.graphics.Paint().apply {
                    color = textColor.hashCode()
                    textSize = 32f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
            restore()
        }
    }
}

/**
 * Helper function to convert Compose Color to Android Paint color int
 */
private fun Color.hashCode(): Int {
    return android.graphics.Color.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}
