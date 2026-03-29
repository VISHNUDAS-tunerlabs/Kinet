package com.example.kinet.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kinet.domain.model.DailyActivity
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun WeeklyChart(
    activities: List<DailyActivity>,
    modifier: Modifier = Modifier
) {
    val barColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        if (activities.isEmpty()) return@Canvas

        val bottomPadding = 24.dp.toPx()
        val chartHeight = size.height - bottomPadding
        val maxSteps = activities.maxOf { it.steps }.coerceAtLeast(1)

        val totalBars = activities.size
        val barWidth = size.width / (totalBars * 2f)
        val gap = barWidth
        val totalUsed = barWidth * totalBars + gap * (totalBars - 1)
        val startX = (size.width - totalUsed) / 2f

        activities.forEachIndexed { index, activity ->
            val barHeight = (activity.steps.toFloat() / maxSteps) * chartHeight * 0.85f
            val x = startX + index * (barWidth + gap)
            val y = chartHeight - barHeight

            // Bar
            drawRoundRect(
                color = if (activity.steps > 0) barColor else Color.LightGray,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight.coerceAtLeast(4.dp.toPx())),
                cornerRadius = CornerRadius(4.dp.toPx())
            )

            // Day label
            val dayLabel = try {
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(activity.date)
                SimpleDateFormat("EEE", Locale.getDefault()).format(date!!).take(2)
            } catch (e: Exception) {
                ""
            }
            val measured = textMeasurer.measure(
                text = dayLabel,
                style = TextStyle(color = labelColor, fontSize = 10.sp)
            )
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(
                    x = x + (barWidth - measured.size.width) / 2f,
                    y = chartHeight + 6.dp.toPx()
                )
            )
        }
    }
}
