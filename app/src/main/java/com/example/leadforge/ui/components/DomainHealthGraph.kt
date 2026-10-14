package com.example.leadforge.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCrimson
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPrimaryCyan
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary

@Composable
fun DomainHealthGraph(
    latencies: List<Long>,
    successCount: Int,
    errorCount: Int,
    averageLatencyMs: Long,
    modifier: Modifier = Modifier
) {
    // Default sample telemetry points if crawl is starting
    val dataPoints = if (latencies.size >= 4) {
        latencies.takeLast(12).map { it.toFloat().coerceIn(20f, 600f) }
    } else {
        listOf(120f, 85f, 140f, 95f, 110f, 75f, 130f, 90f, 105f)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(CyberPrimaryCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DOMAIN TELEMETRY & LATENCY",
                        color = CyberTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "AVG: ${averageLatencyMs.coerceAtLeast(42)}ms",
                    color = CyberAmber,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sparkline Graph
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberSurfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (dataPoints.size < 2) return@Canvas

                    val maxVal = (dataPoints.maxOrNull() ?: 200f) * 1.15f
                    val minVal = 0f
                    val stepX = size.width / (dataPoints.size - 1)

                    val linePath = Path()
                    val fillPath = Path()

                    dataPoints.forEachIndexed { i, value ->
                        val x = i * stepX
                        val normalizedY = 1f - ((value - minVal) / (maxVal - minVal)).coerceIn(0f, 1f)
                        val y = normalizedY * (size.height * 0.82f) + (size.height * 0.08f)

                        if (i == 0) {
                            linePath.moveTo(x, y)
                            fillPath.moveTo(x, size.height)
                            fillPath.lineTo(x, y)
                        } else {
                            linePath.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }

                        // Data point node
                        drawCircle(
                            color = CyberPrimaryCyan,
                            radius = 2.5.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }

                    fillPath.lineTo(size.width, size.height)
                    fillPath.close()

                    // Draw gradient fill
                    val fillBrush = Brush.verticalGradient(
                        colors = listOf(
                            CyberPrimaryCyan.copy(alpha = 0.28f),
                            Color.Transparent
                        )
                    )
                    drawPath(path = fillPath, brush = fillBrush)

                    // Draw stroke
                    drawPath(
                        path = linePath,
                        color = CyberPrimaryCyan,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // HTTP Response Distribution Bar
            val totalRequests = (successCount + errorCount).coerceAtLeast(1)
            val successFraction = (successCount.toFloat() / totalRequests).coerceIn(0.05f, 0.98f)
            val errorFraction = 1f - successFraction

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "HTTP 200 OK (${(successFraction * 100).toInt()}%)",
                    color = CyberEmerald,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "ERRORS / RETRIES (${(errorFraction * 100).toInt()}%)",
                    color = if (errorCount > 0) CyberCrimson else CyberTextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(CyberBorder)
            ) {
                Box(
                    modifier = Modifier
                        .weight(successFraction)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(3.dp))
                        .background(CyberEmerald)
                )
                if (errorCount > 0) {
                    Box(
                        modifier = Modifier
                            .weight(errorFraction)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(3.dp))
                            .background(CyberCrimson)
                    )
                }
            }
        }
    }
}
