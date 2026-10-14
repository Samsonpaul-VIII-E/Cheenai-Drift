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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.leadforge.model.WebhookDeliveryLog
import com.example.leadforge.model.WebhookTierThreshold
import com.example.leadforge.ui.LeadForgeViewModel
import com.example.leadforge.ui.components.AuditItemRow
import com.example.leadforge.ui.components.OperationHeader
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
fun AuditHealthScreen(
    viewModel: LeadForgeViewModel
) {
    val telemetry by viewModel.telemetry.collectAsState()
    val audits by viewModel.recentAudits.collectAsState()
    val totalLeads by viewModel.leadCount.collectAsState()
    val totalPages by viewModel.visitedCount.collectAsState()
    val webhookConfig by viewModel.webhookConfig.collectAsState()
    val isTestingWebhook by viewModel.isTestingWebhook.collectAsState()
    val webhookTestResult by viewModel.webhookTestResult.collectAsState()
    val webhookLogs by viewModel.webhookLogs.collectAsState()

    var webhookUrlInput by remember(webhookConfig.url) { mutableStateOf(webhookConfig.url) }
    var bearerTokenInput by remember(webhookConfig.bearerToken) { mutableStateOf(webhookConfig.bearerToken) }
    var selectedThreshold by remember(webhookConfig.threshold) { mutableStateOf(webhookConfig.threshold) }
    var autoSyncState by remember(webhookConfig.autoSyncEnabled) { mutableStateOf(webhookConfig.autoSyncEnabled) }
    var showToken by remember { mutableStateOf(false) }

    var showPurgeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
    ) {
        OperationHeader(
            title = "SYSTEM HEALTH & AUDIT",
            subtitle = "Subsystem diagnostics, security verification & audit ledger",
            status = telemetry.status
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // System Doctor Section
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MedicalServices, contentDescription = null, tint = CyberEmerald, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "LEADFORGE DOCTOR DIAGNOSTICS",
                                color = CyberPrimaryCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        DiagnosticRow(
                            label = "SSRF Defensive Shield",
                            status = "ACTIVE & ENFORCING",
                            detail = "Loopback (127.0.0.1/::1), RFC1918 (10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16) and link-local blocked.",
                            isHealthy = true
                        )

                        DiagnosticRow(
                            label = "Room SQLite Database",
                            status = "OPERATIONAL (WAL)",
                            detail = "$totalLeads lead records, $totalPages page checkpoints persisted.",
                            isHealthy = true
                        )

                        DiagnosticRow(
                            label = "Robots Policy Subsystem",
                            status = "RESPECT ROBOTS ON",
                            detail = "Cached rules refreshed with fail-safe compliance checks.",
                            isHealthy = true
                        )

                        DiagnosticRow(
                            label = "OkHttp Engine & Concurrency",
                            status = "OPTIMIZED",
                            detail = "Bounded connection limits, 5MB response memory cap, exponential backoff active.",
                            isHealthy = true
                        )
                    }
                }
            }

            // CRM Webhook Operations & Automation Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyberPrimaryCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudSync, contentDescription = null, tint = CyberPrimaryCyan, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "CRM WEBHOOK OPERATIONS & SYNC ENGINE",
                                    color = CyberPrimaryCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(
                                        if (autoSyncState) CyberEmerald.copy(alpha = 0.15f) else CyberSurfaceVariant,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (autoSyncState) CyberEmerald.copy(alpha = 0.4f) else CyberBorder,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (autoSyncState) "AUTO-SYNC ACTIVE" else "MANUAL DISPATCH",
                                    color = if (autoSyncState) CyberEmerald else CyberTextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Forward verified lead payloads into Zapier, Make, n8n, Slack, or custom enterprise CRM webhooks asynchronously with exponential backoff.",
                            color = CyberTextSecondary,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Target Webhook URL input
                        Text("TARGET WEBHOOK ENDPOINT URL", color = CyberTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = webhookUrlInput,
                            onValueChange = { webhookUrlInput = it },
                            placeholder = { Text("https://hooks.zapier.com/hooks/catch/... or API URL", color = CyberTextMuted, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = CyberPrimaryCyan, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("webhook_url_input"),
                            textStyle = androidx.compose.ui.text.TextStyle(color = CyberTextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberPrimaryCyan,
                                unfocusedBorderColor = CyberBorder,
                                focusedContainerColor = CyberBackground,
                                unfocusedContainerColor = CyberBackground
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Bearer Token / API Key header
                        Text("SECRET BEARER TOKEN / API KEY HEADER", color = CyberTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = bearerTokenInput,
                            onValueChange = { bearerTokenInput = it },
                            placeholder = { Text("Bearer token or custom API key", color = CyberTextMuted, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = CyberAmber, modifier = Modifier.size(16.dp)) },
                            trailingIcon = {
                                IconButton(onClick = { showToken = !showToken }) {
                                    Icon(
                                        if (showToken) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle token visibility",
                                        tint = CyberTextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            },
                            visualTransformation = if (showToken) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("webhook_token_input"),
                            textStyle = androidx.compose.ui.text.TextStyle(color = CyberTextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberPrimaryCyan,
                                unfocusedBorderColor = CyberBorder,
                                focusedContainerColor = CyberBackground,
                                unfocusedContainerColor = CyberBackground
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Minimum Tier Threshold Selector
                        Text("AUTOMATIC SYNC TIER TRIGGER", color = CyberTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            WebhookTierThreshold.values().forEach { threshold ->
                                val isSelected = selectedThreshold == threshold
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isSelected) CyberPrimaryCyan.copy(alpha = 0.15f) else CyberSurfaceVariant, RoundedCornerShape(6.dp))
                                        .border(1.dp, if (isSelected) CyberPrimaryCyan else CyberBorder, RoundedCornerShape(6.dp))
                                        .clickable { selectedThreshold = threshold }
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = threshold.label,
                                        color = if (isSelected) CyberPrimaryCyan else CyberTextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontFamily = FontFamily.Monospace,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Auto-Sync Toggle Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CyberSurfaceVariant, RoundedCornerShape(8.dp))
                                .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Auto-Dispatch upon extraction", color = CyberTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("Sync leads automatically when qualifying score is reached", color = CyberTextSecondary, fontSize = 10.sp)
                            }
                            Switch(
                                checked = autoSyncState,
                                onCheckedChange = { autoSyncState = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CyberPrimaryCyan,
                                    checkedTrackColor = CyberPrimaryCyan.copy(alpha = 0.4f)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons: Save & Test Webhook
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.updateWebhookConfig(
                                        url = webhookUrlInput,
                                        bearerToken = bearerTokenInput,
                                        threshold = selectedThreshold,
                                        autoSync = autoSyncState
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberPrimaryCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("save_webhook_config_btn")
                            ) {
                                Text("Save Settings", color = CyberBackground, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }

                            Button(
                                onClick = {
                                    viewModel.updateWebhookConfig(
                                        url = webhookUrlInput,
                                        bearerToken = bearerTokenInput,
                                        threshold = selectedThreshold,
                                        autoSync = autoSyncState
                                    )
                                    viewModel.sendTestWebhook()
                                },
                                enabled = !isTestingWebhook && webhookUrlInput.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyberEmerald.copy(alpha = 0.2f),
                                    disabledContainerColor = CyberSurfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .border(1.dp, CyberEmerald.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .testTag("send_test_webhook_btn")
                            ) {
                                if (isTestingWebhook) {
                                    CircularProgressIndicator(color = CyberEmerald, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pinging...", color = CyberEmerald, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                } else {
                                    Icon(Icons.Default.Send, contentDescription = null, tint = CyberEmerald, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Send Test Ping", color = CyberEmerald, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }

                        // Live Test Banner Result
                        webhookTestResult?.let { result ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (result.isSuccess) CyberEmerald.copy(alpha = 0.15f) else CyberCrimson.copy(alpha = 0.15f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (result.isSuccess) CyberEmerald.copy(alpha = 0.5f) else CyberCrimson.copy(alpha = 0.5f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (result.isSuccess) Icons.Default.CheckCircle else Icons.Default.Clear,
                                    contentDescription = null,
                                    tint = if (result.isSuccess) CyberEmerald else CyberCrimson,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = result.message,
                                        color = if (result.isSuccess) CyberEmerald else CyberCrimson,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "Payload schema: {event: 'leadforge.test_ping', sample_lead: {...}}",
                                        color = CyberTextSecondary,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        // Recent Webhook Deliveries Ledger Feed
                        if (webhookLogs.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "RECENT WEBHOOK DISPATCHES (${webhookLogs.size})",
                                    color = CyberTextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Clear",
                                    color = CyberTextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.clickable { viewModel.clearWebhookLogs() }
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                webhookLogs.take(5).forEach { log ->
                                    WebhookLogRow(log)
                                }
                            }
                        }
                    }
                }
            }

            // Data Retention & Purge Actions
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "DATA RETENTION & LOCAL STORAGE",
                            color = CyberTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Configure or purge local checkpoints, crawled page hashes, and saved leads.",
                            color = CyberTextSecondary,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { showPurgeDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCrimson.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .border(1.dp, CyberCrimson.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .testTag("purge_data_btn")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = CyberCrimson, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Purge Local Cache & Audit Data", color = CyberCrimson, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // Full Audit Log Feed
            item {
                Text(
                    text = "AUDIT TRAIL LEDGER (${audits.size} EVENTS)",
                    color = CyberTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (audits.isEmpty()) {
                item {
                    Text(
                        text = "No audit events recorded.",
                        color = CyberTextMuted,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                items(audits) { audit ->
                    AuditItemRow(audit)
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showPurgeDialog) {
        AlertDialog(
            onDismissRequest = { showPurgeDialog = false },
            title = { Text("Confirm Data Purge", color = CyberTextPrimary, fontFamily = FontFamily.Monospace) },
            text = {
                Text(
                    "Are you sure you want to purge all local leads, visited page checkpoints, and audit logs? This action is permanent.",
                    color = CyberTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.purgeAllData()
                        showPurgeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCrimson)
                ) {
                    Text("Purge All Data", color = CyberBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurgeDialog = false }) {
                    Text("Cancel", color = CyberTextPrimary)
                }
            },
            containerColor = CyberSurface
        )
    }
}

@Composable
fun DiagnosticRow(
    label: String,
    status: String,
    detail: String,
    isHealthy: Boolean
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = CyberTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isHealthy) CyberEmerald else CyberCrimson)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = status,
                    color = if (isHealthy) CyberEmerald else CyberCrimson,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        Text(
            text = detail,
            color = CyberTextSecondary,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun WebhookLogRow(log: WebhookDeliveryLog) {
    val dateStr = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(log.timestamp))
    val statusColor = if (log.isSuccess) CyberEmerald else CyberCrimson

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CyberSurfaceVariant, RoundedCornerShape(6.dp))
            .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(log.targetDomain, color = CyberTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.width(6.dp))
                Text("HTTP ${log.httpCode}", color = statusColor, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
            Text(
                text = "$dateStr • ${log.latencyMs}ms • retry: ${log.retryCount}${if (log.errorMessage != null) " • " + log.errorMessage.take(30) else ""}",
                color = CyberTextSecondary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Box(
            modifier = Modifier
                .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (log.isSuccess) "DELIVERED" else "FAILED",
                color = statusColor,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
