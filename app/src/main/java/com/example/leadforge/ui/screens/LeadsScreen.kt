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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import com.example.leadforge.data.entity.LeadEntity
import com.example.leadforge.ui.LeadFilter
import com.example.leadforge.ui.LeadForgeViewModel
import com.example.leadforge.ui.components.ExecutiveSummarySheet
import com.example.leadforge.ui.components.FilterChipItem
import com.example.leadforge.ui.components.LeadCardItem
import com.example.leadforge.ui.components.LeadDetailSheet
import com.example.leadforge.ui.components.OperationHeader
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPrimaryCyan
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberViolet

@Composable
fun LeadsScreen(
    viewModel: LeadForgeViewModel
) {
    val leads by viewModel.filteredLeads.collectAsState()
    val allLeads by viewModel.repository.allLeads.collectAsState(initial = emptyList())
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentFilter by viewModel.leadFilter.collectAsState()
    val selectedLead by viewModel.selectedLead.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val bannerNotice by viewModel.bannerNotice.collectAsState()

    var showExecutiveSummary by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
    ) {
        OperationHeader(
            title = "LEADS EXPLORER",
            subtitle = "Verified public business records & enriched contact points",
            status = telemetry.status
        )

        // Banner notification if export ran
        bannerNotice?.let { notice ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberEmerald.copy(alpha = 0.15f))
                    .border(1.dp, CyberEmerald.copy(alpha = 0.4f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = notice, color = CyberEmerald, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                IconButton(onClick = { viewModel.clearBannerNotice() }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = CyberEmerald, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Search and Actions Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search by name, domain, email, keyword...", color = CyberTextMuted, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyberPrimaryCyan) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = CyberTextMuted)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lead_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberPrimaryCyan,
                    unfocusedBorderColor = CyberBorder,
                    focusedTextColor = CyberTextPrimary,
                    unfocusedTextColor = CyberTextPrimary,
                    focusedContainerColor = CyberSurface,
                    unfocusedContainerColor = CyberSurface
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChipItem(
                    label = "ALL (${leads.size})",
                    isSelected = currentFilter == LeadFilter.ALL,
                    onClick = { viewModel.setLeadFilter(LeadFilter.ALL) }
                )
                FilterChipItem(
                    label = "HIGH SIGNAL ≥70%",
                    isSelected = currentFilter == LeadFilter.HIGH_SCORE,
                    onClick = { viewModel.setLeadFilter(LeadFilter.HIGH_SCORE) }
                )
                FilterChipItem(
                    label = "HAS EMAIL",
                    isSelected = currentFilter == LeadFilter.HAS_EMAIL,
                    onClick = { viewModel.setLeadFilter(LeadFilter.HAS_EMAIL) }
                )
                FilterChipItem(
                    label = "HAS PHONE",
                    isSelected = currentFilter == LeadFilter.HAS_PHONE,
                    onClick = { viewModel.setLeadFilter(LeadFilter.HAS_PHONE) }
                )
                FilterChipItem(
                    label = "SOCIAL PROFILES",
                    isSelected = currentFilter == LeadFilter.HAS_SOCIALS,
                    onClick = { viewModel.setLeadFilter(LeadFilter.HAS_SOCIALS) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Executive Briefing + CSV + JSON
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showExecutiveSummary = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(38.dp)
                        .border(1.dp, CyberPrimaryCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .testTag("executive_summary_btn")
                ) {
                    Icon(Icons.Default.Assessment, contentDescription = null, tint = CyberPrimaryCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Executive Briefing", color = CyberPrimaryCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                Button(
                    onClick = { viewModel.exportCsv() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
                        .testTag("export_csv_btn")
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = CyberTextPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("CSV", color = CyberTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                Button(
                    onClick = { viewModel.exportJson() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
                        .testTag("export_json_btn")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = CyberEmerald, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("JSON", color = CyberEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Leads List
        if (leads.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NO LEADS MATCH CRITERIA",
                        color = CyberTextMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Adjust filters or run a new target operation to acquire leads.",
                        color = CyberTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(leads, key = { it.fingerprint }) { lead ->
                    LeadCardItem(
                        lead = lead,
                        onClick = { viewModel.selectLead(lead) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Detail BottomSheet
    selectedLead?.let { lead ->
        LeadDetailSheet(
            lead = lead,
            onDismiss = { viewModel.selectLead(null) },
            onDelete = { viewModel.deleteLead(it) },
            onSyncWebhook = { viewModel.syncLeadToWebhook(it) }
        )
    }

    // Executive Summary Briefing BottomSheet
    if (showExecutiveSummary) {
        ExecutiveSummarySheet(
            leads = if (leads.isNotEmpty()) leads else allLeads,
            onDismiss = { showExecutiveSummary = false },
            onExportCsv = {
                viewModel.exportCsv()
                showExecutiveSummary = false
            },
            onExportJson = {
                viewModel.exportJson()
                showExecutiveSummary = false
            }
        )
    }
}
