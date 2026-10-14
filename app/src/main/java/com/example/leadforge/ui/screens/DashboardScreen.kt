package com.example.leadforge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leadforge.data.entity.AuditEntity
import com.example.leadforge.data.entity.JobEntity
import com.example.leadforge.data.entity.LeadEntity
import com.example.leadforge.engine.EngineTelemetry
import com.example.leadforge.model.JobStatus
import com.example.leadforge.ui.Screen
import com.example.leadforge.ui.components.AuditItemRow
import com.example.leadforge.ui.components.DomainHealthGraph
import com.example.leadforge.ui.components.MetricCard
import com.example.leadforge.ui.components.OperationHeader
import com.example.leadforge.ui.components.VisualRadarScanner
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCrimson
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPrimaryCyan
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberViolet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    telemetry: EngineTelemetry,
    totalLeadsCount: Int,
    pagesProcessedCount: Int,
    leads: List<LeadEntity>,
    recentAudits: List<AuditEntity>,
    allJobs: List<JobEntity>,
    onNavigate: (Screen) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
    ) {
        OperationHeader(
            title = "LEADFORGE X",
            subtitle = "Public Data Intelligence & Lead Operations Console",
            status = telemetry.status,
            onQuickAction = { onNavigate(Screen.NEW_JOB) },
            quickActionText = "+ CONFIGURE NEW OPERATION"
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // SSRF & Defense status banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyberEmerald.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Shield Active",
                            tint = CyberEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SSRF SHIELD ACTIVE • PUBLIC DESTINATIONS ONLY",
                                color = CyberEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Loopback (127.0.0.1), RFC1918 private IPs, and cloud metadata strictly blocked.",
                                color = CyberTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Visual Radar Scanner
            item {
                VisualRadarScanner(
                    leads = leads,
                    isCrawling = telemetry.status == JobStatus.RUNNING,
                    modifier = Modifier.testTag("visual_radar_scanner")
                )
            }

            // Metric Cards Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Leads Secured",
                        value = totalLeadsCount.toString(),
                        subtitle = "Verified Records",
                        icon = Icons.Default.TrendingUp,
                        accentColor = CyberEmerald,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("metric_leads_secured")
                    )
                    MetricCard(
                        title = "Pages Scanned",
                        value = pagesProcessedCount.toString(),
                        subtitle = "HTTP Fetches",
                        icon = Icons.Default.Language,
                        accentColor = CyberPrimaryCyan,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("metric_pages_scanned")
                    )
                }
            }

            // Domain Health and Latency Telemetry Graph
            item {
                DomainHealthGraph(
                    latencies = emptyList(),
                    successCount = pagesProcessedCount,
                    errorCount = telemetry.errorsCount,
                    averageLatencyMs = telemetry.averageLatencyMs,
                    modifier = Modifier.testTag("domain_health_graph")
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val latStr = if (telemetry.averageLatencyMs > 0) "${telemetry.averageLatencyMs}ms" else "Optimal"
                    MetricCard(
                        title = "Avg Latency",
                        value = latStr,
                        subtitle = "Per-Domain Throttled",
                        icon = Icons.Default.Speed,
                        accentColor = CyberAmber,
                        modifier = Modifier.weight(1f)
                    )
                    val successRate = if (pagesProcessedCount > 0) {
                        val errors = telemetry.errorsCount
                        val rate = ((pagesProcessedCount - errors).toDouble() / pagesProcessedCount * 100).toInt()
                        "$rate%"
                    } else {
                        "100%"
                    }
                    MetricCard(
                        title = "Health Rate",
                        value = successRate,
                        subtitle = "0 CAPTCHAs Bypassed",
                        icon = Icons.Default.CheckCircle,
                        accentColor = CyberViolet,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Active Operation Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Radar,
                                    contentDescription = null,
                                    tint = CyberPrimaryCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "OPERATION ENGINE STATUS",
                                    color = CyberTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Text(
                                text = telemetry.status.name,
                                color = when (telemetry.status) {
                                    JobStatus.RUNNING -> CyberEmerald
                                    JobStatus.PAUSED -> CyberAmber
                                    else -> CyberTextSecondary
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (telemetry.status == JobStatus.RUNNING || telemetry.status == JobStatus.PAUSED) {
                            Text(
                                text = "Target: ${telemetry.currentUrl}",
                                color = CyberPrimaryCyan,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = {
                                    if (telemetry.pagesDiscovered > 0) {
                                        (telemetry.pagesProcessed.toFloat() / telemetry.pagesDiscovered.toFloat()).coerceIn(0f, 1f)
                                    } else {
                                        0.2f
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = CyberPrimaryCyan,
                                trackColor = CyberSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (telemetry.status == JobStatus.RUNNING) {
                                    Button(
                                        onClick = onPause,
                                        colors = ButtonDefaults.buttonColors(containerColor = CyberAmber),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(36.dp)
                                    ) {
                                        Text("Pause", color = CyberBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                } else {
                                    Button(
                                        onClick = onResume,
                                        colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(36.dp)
                                    ) {
                                        Text("Resume", color = CyberBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }

                                Button(
                                    onClick = { onNavigate(Screen.OPERATIONS) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(36.dp).border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
                                ) {
                                    Text("Open Terminal", color = CyberTextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        } else {
                            Text(
                                text = "Crawler idle. Ready to deploy targeted public lead extraction.",
                                color = CyberTextSecondary,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onNavigate(Screen.NEW_JOB) },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberPrimaryCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(36.dp).testTag("start_operation_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = CyberBackground, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Launch Public Operation", color = CyberBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Quick Nav Shortcuts
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberSurface),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
                            .clickable { onNavigate(Screen.LEADS) }
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("VIEW LEADS TABLE", color = CyberEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("Search, filter & briefing", color = CyberTextSecondary, fontSize = 11.sp)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberSurface),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
                            .clickable { onNavigate(Screen.AUDIT_HEALTH) }
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("SYSTEM DOCTOR", color = CyberPrimaryCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("Diagnostics & audit", color = CyberTextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Recent Audit Log Feed
            item {
                Text(
                    text = "RECENT AUDIT TRAIL",
                    color = CyberTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (recentAudits.isEmpty()) {
                item {
                    Text(
                        text = "No audit events logged yet.",
                        color = CyberTextMuted,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                items(recentAudits.take(5).size) { index ->
                    val audit = recentAudits[index]
                    AuditItemRow(audit)
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
