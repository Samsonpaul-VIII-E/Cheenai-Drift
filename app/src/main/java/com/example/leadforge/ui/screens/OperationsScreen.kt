package com.example.leadforge.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leadforge.engine.OperationEvent
import com.example.leadforge.model.JobStatus
import com.example.leadforge.ui.LeadForgeViewModel
import com.example.leadforge.ui.components.OperationHeader
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCrimson
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPrimaryCyan
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTerminalBg
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberViolet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OperationsScreen(
    viewModel: LeadForgeViewModel
) {
    val telemetry by viewModel.telemetry.collectAsState()
    val events by viewModel.recentEventsList.collectAsState()
    val listState = rememberLazyListState()

    // Scroll to top when new events arrive
    LaunchedEffect(events.size) {
        if (events.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
    ) {
        OperationHeader(
            title = "LIVE OPERATIONS",
            subtitle = if (telemetry.currentTargetHost.isNotBlank()) "Target: ${telemetry.currentTargetHost}" else "Standby Mode",
            status = telemetry.status
        )

        // Telemetry Dashboard Bar
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Progress Bar
                val progress = if (telemetry.pagesDiscovered > 0) {
                    (telemetry.pagesProcessed.toFloat() / telemetry.pagesDiscovered.toFloat()).coerceIn(0f, 1f)
                } else if (telemetry.status == JobStatus.RUNNING) {
                    0.25f
                } else {
                    0f
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ACQUISITION PROGRESS: ${telemetry.pagesProcessed} / ${telemetry.pagesDiscovered} PAGES",
                        color = CyberTextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        color = CyberPrimaryCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = CyberPrimaryCyan,
                    trackColor = CyberSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Stats strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MiniStat(label = "LEADS", value = "${telemetry.leadsFound}", color = CyberEmerald)
                    MiniStat(label = "REQ/SEC", value = String.format("%.1f", telemetry.requestsPerSecond), color = CyberPrimaryCyan)
                    MiniStat(label = "LATENCY", value = "${telemetry.averageLatencyMs}ms", color = CyberAmber)
                    MiniStat(label = "ERRORS", value = "${telemetry.errorsCount}", color = if (telemetry.errorsCount > 0) CyberCrimson else CyberTextMuted)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Control Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (telemetry.status) {
                        JobStatus.RUNNING -> {
                            Button(
                                onClick = { viewModel.pauseOperation() },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberAmber),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("pause_button")
                            ) {
                                Icon(Icons.Default.Pause, contentDescription = null, tint = CyberBackground, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pause", color = CyberBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        JobStatus.PAUSED -> {
                            Button(
                                onClick = { viewModel.resumeOperation() },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("resume_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CyberBackground, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Resume", color = CyberBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        else -> {
                            Button(
                                onClick = { viewModel.launchOperationFromInputs() },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberPrimaryCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("restart_button")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = CyberBackground, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Restart", color = CyberBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.cancelOperation() },
                        enabled = telemetry.status == JobStatus.RUNNING || telemetry.status == JobStatus.PAUSED,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCrimson.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .border(1.dp, CyberCrimson.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .testTag("abort_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = CyberCrimson, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Abort", color = CyberCrimson, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Live Terminal Feed Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Terminal, contentDescription = null, tint = CyberPrimaryCyan, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "STREAMING OPERATIONAL TELEMETRY & EVENT LOGS",
                color = CyberPrimaryCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CyberTerminalBg)
                .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
        ) {
            if (events.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Terminal feed awaiting operation events...",
                        color = CyberTextMuted,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(events) { event ->
                        TerminalLineItem(event)
                    }
                }
            }
        }
    }
}

@Composable
fun MiniStat(label: String, value: String, color: Color) {
    Column {
        Text(text = label, color = CyberTextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        Text(text = value, color = color, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TerminalLineItem(event: OperationEvent) {
    val tagColor = when {
        event.isSecurity -> CyberEmerald
        event.isLead -> CyberPrimaryCyan
        event.isError -> CyberCrimson
        event.tag == "PARSED" -> CyberViolet
        event.tag == "ROBOTS" -> CyberAmber
        else -> CyberTextSecondary
    }

    val timeStr = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date(event.timestamp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = timeStr,
            color = CyberTextMuted,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "[${event.tag}]",
            color = tagColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = event.message,
            color = if (event.isLead) CyberEmerald else CyberTextPrimary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f)
        )
    }
}
