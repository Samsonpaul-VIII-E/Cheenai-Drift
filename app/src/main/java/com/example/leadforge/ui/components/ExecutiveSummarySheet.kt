package com.example.leadforge.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leadforge.data.entity.LeadEntity
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberIndigo
import com.example.ui.theme.CyberPrimaryCyan
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberViolet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExecutiveSummarySheet(
    leads: List<LeadEntity>,
    onDismiss: () -> Unit,
    onExportCsv: () -> Unit,
    onExportJson: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val total = leads.size.coerceAtLeast(1)
    val withEmail = leads.count { it.email.isNotBlank() }
    val withPhone = leads.count { it.phone.isNotBlank() }
    val withAddress = leads.count { it.address.isNotBlank() }
    val withSocials = leads.count { it.linkedin.isNotBlank() || it.xTwitter.isNotBlank() || it.facebook.isNotBlank() || it.instagram.isNotBlank() }

    val emailPct = (withEmail * 100) / total
    val phonePct = (withPhone * 100) / total
    val addressPct = (withAddress * 100) / total
    val socialPct = (withSocials * 100) / total

    val tierA = leads.count { it.keywordScore >= 90.0 }
    val tierB = leads.count { it.keywordScore in 70.0..89.9 }
    val tierC = leads.count { it.keywordScore in 40.0..69.9 }
    val tierD = leads.count { it.keywordScore < 40.0 }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CyberSurface,
        scrimColor = Color.Black.copy(alpha = 0.7f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 36.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        tint = CyberPrimaryCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "EXECUTIVE INTELLIGENCE BRIEFING",
                            color = CyberTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Aggregated Lead Quality & Signal Distribution",
                            color = CyberTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = CyberTextMuted)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // High-Level Metrics Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BriefingMetricPill(label = "TOTAL LEADS", value = "${leads.size}", color = CyberPrimaryCyan, modifier = Modifier.weight(1f))
                BriefingMetricPill(label = "HIGH SIGNAL", value = "$tierA", color = CyberEmerald, modifier = Modifier.weight(1f))
                BriefingMetricPill(label = "AVG SCORE", value = "${leads.map { it.keywordScore }.average().toInt()}%", color = CyberAmber, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Signal Completeness Bars
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "CONTACT CHANNEL COVERAGE",
                        color = CyberPrimaryCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    SignalCoverageBar(label = "Verified Emails", percent = emailPct, count = withEmail, icon = Icons.Default.Email, color = CyberEmerald)
                    Spacer(modifier = Modifier.height(10.dp))
                    SignalCoverageBar(label = "Direct Phone Numbers", percent = phonePct, count = withPhone, icon = Icons.Default.Phone, color = CyberPrimaryCyan)
                    Spacer(modifier = Modifier.height(10.dp))
                    SignalCoverageBar(label = "Physical Addresses", percent = addressPct, count = withAddress, icon = Icons.Default.LocationOn, color = CyberAmber)
                    Spacer(modifier = Modifier.height(10.dp))
                    SignalCoverageBar(label = "Social Intelligence", percent = socialPct, count = withSocials, icon = Icons.Default.Public, color = CyberViolet)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Score Distribution Breakdown
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "LEAD QUALITY TIERS (QUARTILES)",
                        color = CyberPrimaryCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TierBadge(tier = "Tier A", range = "90-100%", count = tierA, color = CyberEmerald)
                        TierBadge(tier = "Tier B", range = "70-89%", count = tierB, color = CyberPrimaryCyan)
                        TierBadge(tier = "Tier C", range = "40-69%", count = tierC, color = CyberAmber)
                        TierBadge(tier = "Tier D", range = "<40%", count = tierD, color = CyberTextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Quick Copy Briefing Text to Clipboard
            Button(
                onClick = {
                    val summaryText = buildString {
                        appendLine("### LEADFORGE X EXECUTIVE BRIEFING")
                        appendLine("- Total Verified Leads: ${leads.size}")
                        appendLine("- High Confidence Leads: $tierA")
                        appendLine("- Email Coverage: $emailPct% ($withEmail leads)")
                        appendLine("- Phone Coverage: $phonePct% ($withPhone leads)")
                        appendLine("- Social Coverage: $socialPct% ($withSocials leads)")
                        appendLine("- Quality Tiers: Tier A: $tierA | Tier B: $tierB | Tier C: $tierC | Tier D: $tierD")
                    }
                    clipboardManager.setText(AnnotatedString(summaryText))
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .border(1.dp, CyberPrimaryCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .testTag("copy_briefing_btn")
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = CyberPrimaryCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copy Markdown Briefing", color = CyberPrimaryCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Export Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onExportCsv,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPrimaryCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(42.dp).testTag("brief_export_csv")
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = CyberBackground, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export CSV", color = CyberBackground, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                Button(
                    onClick = onExportJson,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(42.dp).testTag("brief_export_json")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = CyberBackground, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export JSON", color = CyberBackground, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun BriefingMetricPill(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, color = CyberTextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, color = color, fontSize = 18.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SignalCoverageBar(
    label: String,
    percent: Int,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = label, color = CyberTextPrimary, fontSize = 12.sp)
            }
            Text(text = "$percent% ($count)", color = color, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = CyberBorder
        )
    }
}

@Composable
fun TierBadge(tier: String, range: String, count: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(text = tier, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text(text = "$count leads", color = CyberTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text(text = range, color = CyberTextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
    }
}
