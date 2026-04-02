package com.example.kinet.ui.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * GitHub-style activity heatmap for habit completion.
 *
 * Displays [weeks] as columns (oldest → newest, left → right).
 * Each column holds 7 cells (Mon–Sun, top → bottom).
 * Cell colour intensity maps to the completion rate for that day.
 */
@Composable
fun HabitHeatmap(
    weeks: List<List<HeatmapDay>>,
    modifier: Modifier = Modifier
) {
    if (weeks.isEmpty()) return

    // Resolve theme colours outside Canvas (can't call CompositionLocals inside)
    val emptyColor   = MaterialTheme.colorScheme.surfaceVariant
    val level1       = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
    val level2       = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
    val level3       = MaterialTheme.colorScheme.primary.copy(alpha = 0.72f)
    val level4       = MaterialTheme.colorScheme.primary
    val labelColor   = MaterialTheme.colorScheme.onSurfaceVariant

    val textMeasurer = rememberTextMeasurer()
    val dateFmt      = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val monthFmt     = remember { SimpleDateFormat("MMM", Locale.getDefault()) }

    // Layout constants (dp-independent; resolved to px inside Canvas)
    val cellDp       = 11.dp
    val gapDp        = 3.dp
    val dayLabelDp   = 18.dp   // left margin for Mon/Wed/Fri labels
    val monthLabelDp = 18.dp   // top margin for month name labels
    val labelSizeSp  = 9.sp

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(monthLabelDp + cellDp * 7 + gapDp * 6 + 4.dp)
            .padding(horizontal = 2.dp)
    ) {
        val cell   = cellDp.toPx()
        val gap    = gapDp.toPx()
        val slot   = cell + gap         // distance from cell origin to next cell origin
        val leftX  = dayLabelDp.toPx()
        val topY   = monthLabelDp.toPx()
        val radius = CornerRadius(3.dp.toPx())

        // ── Day-of-week labels ─────────────────────────────────────────────────
        val dayLabels = mapOf(0 to "M", 2 to "W", 4 to "F")
        dayLabels.forEach { (row, label) ->
            val measured = textMeasurer.measure(
                text = label,
                style = TextStyle(color = labelColor, fontSize = labelSizeSp)
            )
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(
                    x = (leftX - measured.size.width) / 2f,
                    y = topY + row * slot + (cell - measured.size.height) / 2f
                )
            )
        }

        // ── Month labels + cells ───────────────────────────────────────────────
        var lastMonth = -1

        weeks.forEachIndexed { colIdx, days ->
            val colX = leftX + colIdx * slot

            // Month label — draw when the month changes from previous column
            val firstDate = runCatching { dateFmt.parse(days.first().date) }.getOrNull()
            if (firstDate != null) {
                val cal = java.util.Calendar.getInstance().also { it.time = firstDate }
                val month = cal.get(java.util.Calendar.MONTH)
                if (month != lastMonth) {
                    lastMonth = month
                    val label = monthFmt.format(firstDate)
                    val measured = textMeasurer.measure(
                        text = label,
                        style = TextStyle(color = labelColor, fontSize = labelSizeSp)
                    )
                    drawText(
                        textLayoutResult = measured,
                        topLeft = Offset(x = colX, y = (topY - measured.size.height) / 2f)
                    )
                }
            }

            // Draw 7 day cells for this column
            days.forEachIndexed { row, day ->
                val cellColor: Color = when {
                    day.isFuture || day.total == 0 -> emptyColor
                    day.rate <= 0f                 -> level1
                    day.rate <= 0.33f              -> level2
                    day.rate <= 0.66f              -> level3
                    else                           -> level4
                }
                drawRoundRect(
                    color = cellColor,
                    topLeft = Offset(x = colX, y = topY + row * slot),
                    size = Size(cell, cell),
                    cornerRadius = radius
                )
            }
        }

        // ── Legend ─────────────────────────────────────────────────────────────
        // Drawn bottom-right: □ Less … More □□□□
        val legendLabels = listOf("Less", "More")
        val legendColors = listOf(emptyColor, level1, level2, level3, level4)
        val legendY = topY + 7 * slot + 2.dp.toPx()   // just below the grid

        val lessText = textMeasurer.measure(
            "Less", TextStyle(color = labelColor, fontSize = labelSizeSp)
        )
        val moreText = textMeasurer.measure(
            "More", TextStyle(color = labelColor, fontSize = labelSizeSp)
        )

        val legendSquareSize = 8.dp.toPx()
        val legendGap = 2.dp.toPx()
        val totalLegendWidth =
            lessText.size.width + legendGap +
            legendColors.size * (legendSquareSize + legendGap) +
            moreText.size.width

        var lx = size.width - totalLegendWidth
        drawText(
            textLayoutResult = lessText,
            topLeft = Offset(lx, legendY + (legendSquareSize - lessText.size.height) / 2f)
        )
        lx += lessText.size.width + legendGap

        legendColors.forEach { color ->
            drawRoundRect(
                color = color,
                topLeft = Offset(lx, legendY),
                size = Size(legendSquareSize, legendSquareSize),
                cornerRadius = CornerRadius(2.dp.toPx())
            )
            lx += legendSquareSize + legendGap
        }

        drawText(
            textLayoutResult = moreText,
            topLeft = Offset(lx, legendY + (legendSquareSize - moreText.size.height) / 2f)
        )
    }
}
