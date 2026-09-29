package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PythonCyan
import com.example.ui.theme.PythonYellow
import com.example.ui.theme.RunGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun ConsolePlotView(
    title: String,
    type: String, // "line" or "bar"
    xLabels: List<String>,
    yValues: List<Float>,
    modifier: Modifier = Modifier
) {
    if (yValues.isEmpty()) return

    val maxY = yValues.maxOrNull()?.coerceAtLeast(1f) ?: 1f
    val minY = yValues.minOrNull()?.coerceAtMost(0f) ?: 0f
    val range = (maxY - minY).coerceAtLeast(1f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("console_plot_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PythonCyan.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Plot Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (type.lowercase() == "bar") Icons.Filled.BarChart else Icons.Filled.ShowChart,
                        contentDescription = null,
                        tint = PythonYellow,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title.ifEmpty { "Plot" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "${type.uppercase()} • Max: ${maxY.toInt()}",
                    fontSize = 10.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = PythonCyan
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Graph Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(DarkSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height

                    // Draw grid lines
                    val gridColor = Color(0xFF334155).copy(alpha = 0.5f)
                    drawLine(gridColor, Offset(0f, 0f), Offset(w, 0f), strokeWidth = 1f)
                    drawLine(gridColor, Offset(0f, h / 2f), Offset(w, h / 2f), strokeWidth = 1f)
                    drawLine(gridColor, Offset(0f, h), Offset(w, h), strokeWidth = 1f)

                    if (type.lowercase() == "bar") {
                        val barSpacing = w / yValues.size
                        val barWidth = (barSpacing * 0.65f).coerceIn(8f, 36f)

                        yValues.forEachIndexed { index, value ->
                            val normY = ((value - minY) / range).coerceIn(0f, 1f)
                            val barHeight = (normY * (h - 8f)).coerceAtLeast(4f)
                            val x = index * barSpacing + (barSpacing - barWidth) / 2f
                            val y = h - barHeight

                            val barColor = if (index % 2 == 0) PythonCyan else PythonYellow
                            drawRect(
                                color = barColor,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight)
                            )
                        }
                    } else {
                        // Line Chart
                        val stepX = if (yValues.size > 1) w / (yValues.size - 1) else w
                        val points = yValues.mapIndexed { index, value ->
                            val normY = ((value - minY) / range).coerceIn(0f, 1f)
                            val x = index * stepX
                            val y = h - (normY * (h - 16f)) - 8f
                            Offset(x, y)
                        }

                        val path = Path()
                        points.forEachIndexed { index, pt ->
                            if (index == 0) path.moveTo(pt.x, pt.y)
                            else path.lineTo(pt.x, pt.y)
                        }

                        drawPath(
                            path = path,
                            color = PythonCyan,
                            style = Stroke(width = 3.5f)
                        )

                        points.forEach { pt ->
                            drawCircle(
                                color = PythonYellow,
                                radius = 4f,
                                center = pt
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // X Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                xLabels.take(6).forEach { label ->
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                }
            }
        }
    }
}
