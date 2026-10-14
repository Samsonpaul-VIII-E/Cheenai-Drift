package com.example.leadforge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.LinearProgressIndicator
import com.example.leadforge.model.BatchItemStatus
import com.example.leadforge.model.BatchPreflightResult
import com.example.leadforge.model.BatchQueueItem
import com.example.leadforge.model.BatchQueueTelemetry
import com.example.leadforge.model.JobStatus
import com.example.leadforge.security.ValidationResult
import com.example.leadforge.ui.LeadForgeViewModel
import com.example.leadforge.ui.components.OperationHeader
import com.example.leadforge.ui.components.PresetPill
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

@Composable
fun NewJobScreen(
    viewModel: LeadForgeViewModel
) {
    val targetTab by viewModel.targetTab.collectAsState()
    val targetUrl by viewModel.targetUrlInput.collectAsState()
    val maxPages by viewModel.maxPagesInput.collectAsState()
    val concurrency by viewModel.concurrencyInput.collectAsState()
    val rateLimit by viewModel.rateLimitInput.collectAsState()
    val maxDepth by viewModel.maxDepthInput.collectAsState()
    val respectRobots by viewModel.respectRobotsInput.collectAsState()
    val enrichWebsites by viewModel.enrichWebsitesInput.collectAsState()
    val keywords by viewModel.keywordsInput.collectAsState()
    val ssrfValidation by viewModel.ssrfPreviewState.collectAsState()

    val isProbing by viewModel.isProbing.collectAsState()
    val probeResult by viewModel.probeResult.collectAsState()

    // Bulk Ingestion state
    val bulkInput by viewModel.bulkInputText.collectAsState()
    val preflight by viewModel.preflightResult.collectAsState()
    val batchTelemetry by viewModel.batchTelemetry.collectAsState()
    val batchQueueItems by viewModel.batchQueueItems.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
    ) {
        OperationHeader(
            title = if (targetTab == 0) "NEW OPERATION" else "BULK QUEUE INGESTION",
            subtitle = if (targetTab == 0) "Configure public-data intelligence target parameters" else "Ingest multiple target domains for sequential batch processing",
            status = if (targetTab == 1 && batchTelemetry.isRunning) JobStatus.RUNNING else JobStatus.QUEUED
        )

        // Mode Switcher Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(CyberSurfaceVariant, RoundedCornerShape(8.dp))
                .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            Button(
                onClick = { viewModel.setTargetTab(0) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (targetTab == 0) CyberPrimaryCyan else CyberSurfaceVariant
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .testTag("tab_single_mission")
            ) {
                Icon(
                    Icons.Default.Tune,
                    contentDescription = null,
                    tint = if (targetTab == 0) CyberBackground else CyberTextSecondary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SINGLE MISSION",
                    color = if (targetTab == 0) CyberBackground else CyberTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Button(
                onClick = { viewModel.setTargetTab(1) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (targetTab == 1) CyberPrimaryCyan else CyberSurfaceVariant
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .testTag("tab_bulk_queue")
            ) {
                Icon(
                    Icons.Default.FormatListNumbered,
                    contentDescription = null,
                    tint = if (targetTab == 1) CyberBackground else CyberTextSecondary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "BULK QUEUE (${batchQueueItems.size})",
                    color = if (targetTab == 1) CyberBackground else CyberTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        if (targetTab == 1) {
            BulkQueueContent(
                viewModel = viewModel,
                bulkInput = bulkInput,
                preflight = preflight,
                batchTelemetry = batchTelemetry,
                batchQueueItems = batchQueueItems
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Real Public Presets Strip
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = CyberPrimaryCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "VERIFIED PUBLIC TARGET PRESETS",
                            color = CyberPrimaryCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Real public web organizations with verified open data & public contacts.",
                        color = CyberTextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetPill(
                            label = "Wikimedia Foundation",
                            onClick = {
                                viewModel.targetUrlInput.value = "https://wikimediafoundation.org"
                                viewModel.keywordsInput.value = "knowledge, education, research, wiki, open, global"
                                viewModel.validateTargetUrl("https://wikimediafoundation.org")
                            }
                        )
                        PresetPill(
                            label = "EFF Digital Rights",
                            onClick = {
                                viewModel.targetUrlInput.value = "https://www.eff.org"
                                viewModel.keywordsInput.value = "privacy, rights, legal, technology, security, civil"
                                viewModel.validateTargetUrl("https://www.eff.org")
                            }
                        )
                        PresetPill(
                            label = "Python Foundation",
                            onClick = {
                                viewModel.targetUrlInput.value = "https://www.python.org/psf"
                                viewModel.keywordsInput.value = "python, software, community, sponsors, code, language"
                                viewModel.validateTargetUrl("https://www.python.org/psf")
                            }
                        )
                        PresetPill(
                            label = "W3C Web Standards",
                            onClick = {
                                viewModel.targetUrlInput.value = "https://www.w3.org"
                                viewModel.keywordsInput.value = "web, standards, consortium, html, specifications, accessibility"
                                viewModel.validateTargetUrl("https://www.w3.org")
                            }
                        )
                    }
                }
            }

            // Target URL section with SSRF indicator
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TARGET DESTINATION (PUBLIC HTTP/S)",
                        color = CyberPrimaryCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = targetUrl,
                        onValueChange = {
                            viewModel.targetUrlInput.value = it
                            viewModel.validateTargetUrl(it)
                        },
                        placeholder = { Text("e.g. https://wikimediafoundation.org", color = CyberTextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("target_url_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberPrimaryCyan,
                            unfocusedBorderColor = CyberBorder,
                            focusedTextColor = CyberTextPrimary,
                            unfocusedTextColor = CyberTextPrimary,
                            focusedContainerColor = CyberSurfaceVariant,
                            unfocusedContainerColor = CyberSurfaceVariant
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // SSRF Validation result badge
                    when (val v = ssrfValidation) {
                        is ValidationResult.Valid -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyberEmerald.copy(alpha = 0.12f))
                                    .border(1.dp, CyberEmerald.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CyberEmerald, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SSRF VERIFIED: ${v.host} (${v.resolvedIps.firstOrNull() ?: "Public Host"})",
                                    color = CyberEmerald,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        is ValidationResult.Rejected -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyberCrimson.copy(alpha = 0.12f))
                                    .border(1.dp, CyberCrimson.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = CyberCrimson, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "BLOCKED: ${v.reason}",
                                    color = CyberCrimson,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        null -> {}
                    }
                }
            }

            // Live Single-Target Recon Probe Card (Instant real-time verification)
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberPrimaryCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = CyberPrimaryCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "INSTANT LIVE RECON PROBE",
                                color = CyberPrimaryCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Button(
                            onClick = { viewModel.executeLiveProbe() },
                            enabled = !isProbing && ssrfValidation is ValidationResult.Valid,
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPrimaryCyan),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(34.dp).testTag("run_live_probe_btn")
                        ) {
                            if (isProbing) {
                                CircularProgressIndicator(color = CyberBackground, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Probing...", color = CyberBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            } else {
                                Text("Probe URL Now", color = CyberBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Instantly sends a live HTTP request to verify actual server responses, titles, emails, and phone contacts in 1 second.",
                        color = CyberTextSecondary,
                        fontSize = 11.sp
                    )

                    // Display probe result if available
                    probeResult?.let { res ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberSurface)
                                .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            if (res.error != null) {
                                Text(
                                    text = "Probe Error: ${res.error}",
                                    color = CyberCrimson,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            } else if (res.data != null) {
                                val d = res.data
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "HTTP ${res.statusCode} OK • ${res.elapsedMs}ms",
                                            color = CyberEmerald,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Button(
                                            onClick = { viewModel.saveProbeResultToLeads() },
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.height(28.dp).testTag("save_probe_btn")
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = CyberBackground, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Save to Leads", color = CyberBackground, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Entity: ${d.businessName}",
                                        color = CyberTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (d.title.isNotBlank()) {
                                        Text(
                                            text = "Title: ${d.title}",
                                            color = CyberTextSecondary,
                                            fontSize = 11.sp,
                                            maxLines = 1
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Contacts found in live probe
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Emails: ${if (d.emails.isNotEmpty()) d.emails.joinToString(", ") else "None directly on root"}",
                                            color = if (d.emails.isNotEmpty()) CyberPrimaryCyan else CyberTextMuted,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Phones: ${if (d.phones.isNotEmpty()) d.phones.joinToString(", ") else "None"}",
                                            color = if (d.phones.isNotEmpty()) CyberEmerald else CyberTextMuted,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "Links Discovered: ${d.links.size}",
                                            color = CyberViolet,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Crawl Parameters Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ACQUISITION & THROTTLING PARAMETERS",
                        color = CyberPrimaryCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Max Pages Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Max Pages Limit", color = CyberTextPrimary, fontSize = 13.sp)
                        Text("$maxPages pages", color = CyberPrimaryCyan, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = maxPages.toFloat(),
                        onValueChange = { viewModel.maxPagesInput.value = it.toInt() },
                        valueRange = 5f..150f,
                        steps = 28,
                        colors = SliderDefaults.colors(thumbColor = CyberPrimaryCyan, activeTrackColor = CyberPrimaryCyan)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Concurrency Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Concurrency Workers", color = CyberTextPrimary, fontSize = 13.sp)
                        Text("$concurrency threads", color = CyberEmerald, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = concurrency.toFloat(),
                        onValueChange = { viewModel.concurrencyInput.value = it.toInt() },
                        valueRange = 1f..12f,
                        steps = 10,
                        colors = SliderDefaults.colors(thumbColor = CyberEmerald, activeTrackColor = CyberEmerald)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Rate limit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Politeness Rate Limit", color = CyberTextPrimary, fontSize = 13.sp)
                        Text(String.format("%.1f req/sec", rateLimit), color = CyberAmber, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = rateLimit.toFloat(),
                        onValueChange = { viewModel.rateLimitInput.value = it.toDouble() },
                        valueRange = 0.5f..8f,
                        colors = SliderDefaults.colors(thumbColor = CyberAmber, activeTrackColor = CyberAmber)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Max Depth
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Crawl Link Depth", color = CyberTextPrimary, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(1, 2, 3).forEach { depth ->
                                val isSelected = maxDepth == depth
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) CyberPrimaryCyan else CyberSurfaceVariant)
                                        .border(1.dp, if (isSelected) CyberPrimaryCyan else CyberBorder, RoundedCornerShape(6.dp))
                                        .clickable { viewModel.maxDepthInput.value = depth }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Depth $depth",
                                        color = if (isSelected) CyberBackground else CyberTextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.testTag("depth_$depth")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Target Keywords Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "LEAD SCORING KEYWORDS (COMMA-SEPARATED)",
                        color = CyberPrimaryCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Matches increment lead score (+10 pts per keyword hit, normalized).",
                        color = CyberTextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = keywords,
                        onValueChange = { viewModel.keywordsInput.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("keywords_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberPrimaryCyan,
                            unfocusedBorderColor = CyberBorder,
                            focusedTextColor = CyberTextPrimary,
                            unfocusedTextColor = CyberTextPrimary,
                            focusedContainerColor = CyberSurfaceVariant,
                            unfocusedContainerColor = CyberSurfaceVariant
                        )
                    )
                }
            }

            // Policy & Switches Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SAFETY & COMPLIANCE CONTROLS",
                        color = CyberPrimaryCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enforce robots.txt policy", color = CyberTextPrimary, fontSize = 13.sp)
                            Text("Always respect webmaster access rules", color = CyberTextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = respectRobots,
                            onCheckedChange = { viewModel.respectRobotsInput.value = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyberEmerald, checkedTrackColor = CyberEmerald.copy(alpha = 0.4f))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enrich company contacts", color = CyberTextPrimary, fontSize = 13.sp)
                            Text("Scan /contact and /about for phones & emails", color = CyberTextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = enrichWebsites,
                            onCheckedChange = { viewModel.enrichWebsitesInput.value = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyberPrimaryCyan, checkedTrackColor = CyberPrimaryCyan.copy(alpha = 0.4f))
                        )
                    }
                }
            }

            // Responsible Use Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = CyberEmerald, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LAWFUL / DEFENSIVE MODE: No CAPTCHAs are bypassed. No credentials collected. Operates strictly on authorized, publicly presented web data.",
                        color = CyberTextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Multi-Page Full Crawl Launch Button
            val canLaunch = ssrfValidation is ValidationResult.Valid
            Button(
                onClick = { viewModel.launchOperationFromInputs() },
                enabled = canLaunch,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberPrimaryCyan,
                    disabledContainerColor = CyberSurfaceVariant
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("launch_operation_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = if (canLaunch) CyberBackground else CyberTextMuted)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (canLaunch) "EXECUTE MULTI-PAGE OPERATION" else "TARGET INVALID / BLOCKED",
                        color = if (canLaunch) CyberBackground else CyberTextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
}

@Composable
fun BulkQueueContent(
    viewModel: LeadForgeViewModel,
    bulkInput: String,
    preflight: BatchPreflightResult,
    batchTelemetry: BatchQueueTelemetry,
    batchQueueItems: List<BatchQueueItem>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card 1: Input & Pre-Flight Sanitization
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlaylistPlay, contentDescription = null, tint = CyberPrimaryCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BULK TARGET INGESTION & SANITIZER",
                            color = CyberPrimaryCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { viewModel.loadPresetBulkBatch() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPrimaryCyan.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .height(30.dp)
                                .border(1.dp, CyberPrimaryCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .testTag("load_preset_batch_btn")
                        ) {
                            Text("Load Preset 5 Domains", color = CyberPrimaryCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        if (bulkInput.isNotBlank()) {
                            Button(
                                onClick = { viewModel.onBulkInputChanged("") },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .height(30.dp)
                                    .border(1.dp, CyberBorder, RoundedCornerShape(6.dp))
                            ) {
                                Text("Clear", color = CyberTextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Paste multiple public domains/URLs (separated by newlines, commas, or CSV lines). Pre-flight validation screens Zero-Trust SSRF policies and deduplicates automatically.",
                    color = CyberTextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = bulkInput,
                    onValueChange = { viewModel.onBulkInputChanged(it) },
                    placeholder = {
                        Text(
                            text = "https://wikimediafoundation.org\nhttps://www.eff.org\nhttps://www.python.org/psf\nhttps://www.mozilla.org\nhttps://www.apache.org",
                            color = CyberTextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("bulk_targets_input"),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = CyberTextPrimary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberPrimaryCyan,
                        unfocusedBorderColor = CyberBorder,
                        focusedContainerColor = CyberBackground,
                        unfocusedContainerColor = CyberBackground
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Pre-flight validation diagnostics strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PreflightBadge(
                        label = "VALID TARGETS",
                        value = "${preflight.validUrls.size}",
                        color = if (preflight.validUrls.isNotEmpty()) CyberEmerald else CyberTextMuted
                    )
                    PreflightBadge(
                        label = "DUPLICATES FILTERED",
                        value = "${preflight.duplicatesRemoved}",
                        color = if (preflight.duplicatesRemoved > 0) CyberAmber else CyberTextSecondary
                    )
                    PreflightBadge(
                        label = "SSRF BLOCKED",
                        value = "${preflight.ssrfBlockedCount}",
                        color = if (preflight.ssrfBlockedCount > 0) CyberCrimson else CyberEmerald
                    )
                    PreflightBadge(
                        label = "RAW TOKENS",
                        value = "${preflight.rawCount}",
                        color = CyberTextMuted
                    )
                }

                // Sample domain preview pills
                if (preflight.sampleValidDomains.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "PRE-FLIGHT VERIFIED TARGETS:",
                        color = CyberTextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        preflight.sampleValidDomains.forEach { domain ->
                            Box(
                                modifier = Modifier
                                    .background(CyberSurfaceVariant, RoundedCornerShape(4.dp))
                                    .border(1.dp, CyberPrimaryCyan.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(domain, color = CyberPrimaryCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Launch Batch Button
                val canLaunchBatch = preflight.validUrls.isNotEmpty() && !batchTelemetry.isRunning
                Button(
                    onClick = { viewModel.launchBatchQueue() },
                    enabled = canLaunchBatch,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberEmerald,
                        disabledContainerColor = CyberSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("launch_bulk_queue_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = if (canLaunchBatch) CyberBackground else CyberTextMuted)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (batchTelemetry.isRunning) "BATCH QUEUE ACTIVE (PROCESSING)"
                                   else if (canLaunchBatch) "LAUNCH SEQUENTIAL BATCH (${preflight.validUrls.size} TARGETS)"
                                   else "INPUT TARGETS TO ENQUEUE",
                            color = if (canLaunchBatch) CyberBackground else CyberTextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Card 2: Live Batch Progress & Telemetry Card
        if (batchTelemetry.isRunning || batchQueueItems.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (batchTelemetry.isRunning) CyberPrimaryCyan else CyberBorder,
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = CyberPrimaryCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BATCH QUEUE TELEMETRY",
                                color = CyberPrimaryCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Status Badge
                        val (statusText, statusBg, statusColor) = when {
                            batchTelemetry.isRunning && batchTelemetry.isPaused -> Triple("PAUSED", CyberAmber.copy(alpha = 0.2f), CyberAmber)
                            batchTelemetry.isRunning -> Triple("PROCESSING SEQUENTIAL", CyberPrimaryCyan.copy(alpha = 0.2f), CyberPrimaryCyan)
                            batchTelemetry.completedItems + batchTelemetry.failedItems == batchTelemetry.totalItems && batchTelemetry.totalItems > 0 ->
                                Triple("BATCH COMPLETE", CyberEmerald.copy(alpha = 0.2f), CyberEmerald)
                            else -> Triple("READY", CyberSurfaceVariant, CyberTextSecondary)
                        }

                        Box(
                            modifier = Modifier
                                .background(statusBg, RoundedCornerShape(4.dp))
                                .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(statusText, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress bar
                    val total = batchTelemetry.totalItems.coerceAtLeast(1)
                    val done = (batchTelemetry.completedItems + batchTelemetry.failedItems)
                    val progressRatio = (done.toFloat() / total.toFloat()).coerceIn(0f, 1f)

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Progress: $done of $total (${(progressRatio * 100).toInt()}%)",
                                color = CyberTextPrimary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            if (batchTelemetry.currentTargetDomain.isNotBlank()) {
                                Text(
                                    text = "Target: ${batchTelemetry.currentTargetDomain}",
                                    color = CyberPrimaryCyan,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progressRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = CyberPrimaryCyan,
                            trackColor = CyberSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Telemetry Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BatchMetricBox(label = "COMPLETED", value = "${batchTelemetry.completedItems}", color = CyberEmerald, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(6.dp))
                        BatchMetricBox(label = "FAILED", value = "${batchTelemetry.failedItems}", color = CyberCrimson, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(6.dp))
                        BatchMetricBox(label = "PENDING", value = "${(batchTelemetry.totalItems - done).coerceAtLeast(0)}", color = CyberAmber, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(6.dp))
                        BatchMetricBox(label = "AVG LATENCY", value = "${batchTelemetry.averageLatencyMs}ms", color = CyberPrimaryCyan, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Queue Control Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (batchTelemetry.isRunning) {
                            if (!batchTelemetry.isPaused) {
                                Button(
                                    onClick = { viewModel.pauseBatchQueue() },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberAmber.copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .border(1.dp, CyberAmber.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                        .testTag("pause_batch_btn")
                                ) {
                                    Icon(Icons.Default.Pause, contentDescription = null, tint = CyberAmber, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pause", color = CyberAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.resumeBatchQueue() },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald.copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .border(1.dp, CyberEmerald.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                        .testTag("resume_batch_btn")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CyberEmerald, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Resume", color = CyberEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                            }

                            Button(
                                onClick = { viewModel.cancelBatchQueue() },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberCrimson.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .border(1.dp, CyberCrimson.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                    .testTag("cancel_batch_btn")
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null, tint = CyberCrimson, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cancel", color = CyberCrimson, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        } else {
                            Button(
                                onClick = { viewModel.clearBatchQueue() },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .border(1.dp, CyberBorder, RoundedCornerShape(6.dp))
                                    .testTag("clear_batch_btn")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = CyberTextSecondary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Clear Finished Queue", color = CyberTextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }

        // Card 3: Sequential Item Audit Table
        if (batchQueueItems.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SEQUENTIAL QUEUE EXECUTION AUDIT (${batchQueueItems.size} ITEMS)",
                        color = CyberTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        batchQueueItems.forEachIndexed { index, item ->
                            BatchItemRowView(index + 1, item)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PreflightBadge(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .background(CyberSurfaceVariant, RoundedCornerShape(6.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text(label, color = CyberTextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun BatchMetricBox(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(CyberSurfaceVariant, RoundedCornerShape(6.dp))
            .border(1.dp, CyberBorder, RoundedCornerShape(6.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text(label, color = CyberTextSecondary, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun BatchItemRowView(index: Int, item: BatchQueueItem) {
    val (statusLabel, statusColor) = when (item.status) {
        BatchItemStatus.PENDING -> Pair("QUEUED", CyberTextSecondary)
        BatchItemStatus.IN_PROGRESS -> Pair("ACTIVE", CyberPrimaryCyan)
        BatchItemStatus.COMPLETED -> Pair("COMPLETED", CyberEmerald)
        BatchItemStatus.FAILED -> Pair("FAILED", CyberCrimson)
        BatchItemStatus.CANCELLED -> Pair("CANCELLED", CyberAmber)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CyberSurfaceVariant, RoundedCornerShape(6.dp))
            .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "#$index",
                    color = CyberTextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.domain,
                    color = CyberTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
            }
            if (item.latencyMs != null || item.leadsFound > 0 || item.errorMessage != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    item.httpStatus?.let {
                        Text("HTTP $it  •  ", color = if (it in 200..299) CyberEmerald else CyberCrimson, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    item.latencyMs?.let {
                        Text("${it}ms  •  ", color = CyberTextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    if (item.leadsFound > 0) {
                        Text("${item.leadsFound} lead saved", color = CyberEmerald, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    item.errorMessage?.let {
                        Text(it.take(35), color = CyberCrimson, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(statusLabel, color = statusColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
    }
}
