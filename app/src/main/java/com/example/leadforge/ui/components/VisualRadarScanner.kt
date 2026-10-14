package com.example.leadforge.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leadforge.data.entity.LeadEntity
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPrimaryCyan
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import kotlin.math.cos
import kotlin.math.sin

data class RadarBlip(
    val id: String,
    val label: String,
    val angleDeg: Float,
    val distanceFraction: Float, // 0.1 to 0.9
    val isVerified: Boolean = true
)

@Composable
fun VisualRadarScanner(
    leads: List<LeadEntity>,
    isCrawling: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "radar_sweep")
    val sweepAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_angle"
    )

    // Generate blips from lead items deterministically
    val blips = remember(leads) {
        leads.take(8).mapIndexed { index, lead ->
            val angle = ((lead.fingerprint.hashCode().toFloat() % 360f) + 360f) % 360f
            val dist = 0.25f + ((index * 0.08f) % 0.65f)
            RadarBlip(
                id = lead.fingerprint,
                label = lead.name.ifEmpty { lead.domain },
                angleDeg = angle,
                distanceFraction = dist,
                isVerified = lead.email.isNotBlank() || lead.phone.isNotBlank()
            )
        }
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
                            .background(if (isCrawling) CyberEmerald else CyberPrimaryCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TACTICAL RADAR SCANNER",
                        color = CyberTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "${blips.size} CONTACTS LOCKED",
                    color = CyberPrimaryCyan,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Radar Scope Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = (minOf(size.width, size.height) / 2f) * 0.88f

                    // Grid Background Rings
                    val ringColor = Color(0x1F00E5FF)
                    val ringStroke = Stroke(width = 1.2.dp.toPx())

                    drawCircle(color = ringColor, radius = radius * 0.33f, center = center, style = ringStroke)
                    drawCircle(color = ringColor, radius = radius * 0.66f, center = center, style = ringStroke)
                    drawCircle(color = Color(0x4000E5FF), radius = radius, center = center, style = Stroke(width = 1.8.dp.toPx()))

                    // Crosshair Axes
                    val axisColor = Color(0x2E00E5FF)
                    val axisStroke = Stroke(width = 1.dp.toPx())
                    drawLine(color = axisColor, start = Offset(center.x - radius, center.y), end = Offset(center.x + radius, center.y), strokeWidth = axisStroke.width)
                    drawLine(color = axisColor, start = Offset(center.x, center.y - radius), end = Offset(center.x, center.y + radius), strokeWidth = axisStroke.width)

                    // Diagonal Grid Guides
                    val diagOffset = radius * 0.707f
                    drawLine(color = Color(0x1400E5FF), start = Offset(center.x - diagOffset, center.y - diagOffset), end = Offset(center.x + diagOffset, center.y + diagOffset), strokeWidth = 1f)
                    drawLine(color = Color(0x1400E5FF), start = Offset(center.x - diagOffset, center.y + diagOffset), end = Offset(center.x + diagOffset, center.y - diagOffset), strokeWidth = 1f)

                    // Sweeping Beam
                    rotate(degrees = sweepAngle, pivot = center) {
                        val sweepBrush = Brush.sweepGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                Color(0x0500E5FF),
                                Color(0x3500E5FF),
                                Color(0x9900E5FF)
                            ),
                            center = center
                        )
                        drawCircle(
                            brush = sweepBrush,
                            radius = radius,
                            center = center
                        )
                        drawLine(
                            color = CyberPrimaryCyan,
                            start = center,
                            end = Offset(center.x + radius, center.y),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // Target Blips
                    for (blip in blips) {
                        val angleRad = Math.toRadians(blip.angleDeg.toDouble())
                        val r = radius * blip.distanceFraction
                        val blipX = center.x + (r * cos(angleRad)).toFloat()
                        val blipY = center.y + (r * sin(angleRad)).toFloat()

                        // Calculate angular difference to sweep beam for fade trail
                        val diff = ((sweepAngle - blip.angleDeg + 360f) % 360f)
                        val blipAlpha = (1f - (diff / 180f)).coerceIn(0.25f, 1f)
                        val blipColor = if (blip.isVerified) CyberEmerald else CyberPrimaryCyan

                        // Outer Ping Ring
                        drawCircle(
                            color = blipColor.copy(alpha = blipAlpha * 0.4f),
                            radius = 7.dp.toPx(),
                            center = Offset(blipX, blipY)
                        )
                        // Core Dot
                        drawCircle(
                            color = blipColor.copy(alpha = blipAlpha),
                            radius = 3.5.dp.toPx(),
                            center = Offset(blipX, blipY)
                        )
                    }
                }

                // Scope Corner HUD Markings
                Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    Text(
                        text = "RANGE: 100km\nAZIMUTH: ${(sweepAngle).toInt()}°",
                        color = CyberPrimaryCyan.copy(alpha = 0.7f),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 11.sp,
                        modifier = Modifier.align(Alignment.TopStart)
                    )
                    Text(
                        text = if (isCrawling) "MODE: ACTIVE RECON\nPING: 24ms" else "MODE: PASSIVE\nPING: IDLE",
                        color = if (isCrawling) CyberEmerald else CyberTextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 11.sp,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer telemetry readout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TARGET PROXIMITY COORD",
                    color = CyberTextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (leads.isNotEmpty()) "LOCKED: ${leads.first().domain}" else "NO TARGET ACQUIRED",
                    color = CyberTextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }
        }
    }
}
