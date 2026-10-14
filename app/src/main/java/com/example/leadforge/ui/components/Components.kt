package com.example.leadforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leadforge.data.entity.LeadEntity
import com.example.leadforge.model.JobStatus
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
fun OperationHeader(
    title: String,
    subtitle: String,
    status: JobStatus,
    onQuickAction: (() -> Unit)? = null,
    quickActionText: String? = null
) {
    Surface(
        color = CyberSurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberBorder, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            color = CyberTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyberSurfaceVariant)
                                .border(1.dp, CyberBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "v2.0 PRO",
                                color = CyberPrimaryCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Text(
                        text = subtitle,
                        color = CyberTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                StatusPill(status = status)
            }

            if (onQuickAction != null && quickActionText != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onQuickAction,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPrimaryCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("header_quick_action")
                ) {
                    Text(
                        text = quickActionText,
                        color = CyberBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun StatusPill(status: JobStatus) {
    val (color, text) = when (status) {
        JobStatus.RUNNING -> Pair(CyberEmerald, "CRAWL_ACTIVE")
        JobStatus.PAUSED -> Pair(CyberAmber, "PAUSED")
        JobStatus.COMPLETED -> Pair(CyberPrimaryCyan, "ONLINE_IDLE")
        JobStatus.CANCELLED -> Pair(CyberTextMuted, "STANDBY")
        JobStatus.FAILED -> Pair(CyberCrimson, "FAULT")
        JobStatus.QUEUED -> Pair(CyberViolet, "QUEUED")
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
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
                Text(
                    text = title.uppercase(),
                    color = CyberTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = CyberTextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = CyberTextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadDetailSheet(
    lead: LeadEntity,
    onDismiss: () -> Unit,
    onDelete: (LeadEntity) -> Unit,
    onSyncWebhook: ((LeadEntity) -> Unit)? = null
) {
    val clipboardManager = LocalClipboardManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CyberSurface,
        scrimColor = Color.Black.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lead.name.ifEmpty { "Entity Record" },
                        color = CyberTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = lead.domain,
                        color = CyberPrimaryCyan,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Score Badge
                val scoreColor = when {
                    lead.keywordScore >= 75 -> CyberEmerald
                    lead.keywordScore >= 45 -> CyberAmber
                    else -> CyberTextSecondary
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(scoreColor.copy(alpha = 0.15f))
                        .border(1.dp, scoreColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${lead.keywordScore.toInt()}% SCORE",
                        color = scoreColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category & Description
            if (lead.category.isNotBlank()) {
                DetailItem(label = "CATEGORY", value = lead.category)
            }
            if (lead.description.isNotBlank()) {
                DetailItem(label = "DESCRIPTION", value = lead.description)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Contact section
            Text(
                text = "COMMUNICATION CHANNELS",
                color = CyberPrimaryCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (lead.email.isNotBlank()) {
                ContactRow(
                    icon = Icons.Default.Email,
                    label = "Primary Email",
                    value = lead.email,
                    onCopy = { clipboardManager.setText(AnnotatedString(lead.email)) }
                )
            }
            if (lead.emails.isNotBlank() && lead.emails != lead.email) {
                ContactRow(
                    icon = Icons.Default.Email,
                    label = "All Discovered Emails",
                    value = lead.emails,
                    onCopy = { clipboardManager.setText(AnnotatedString(lead.emails)) }
                )
            }
            if (lead.phone.isNotBlank()) {
                ContactRow(
                    icon = Icons.Default.Phone,
                    label = "Phone Number",
                    value = lead.phone,
                    onCopy = { clipboardManager.setText(AnnotatedString(lead.phone)) }
                )
            }
            if (lead.address.isNotBlank()) {
                ContactRow(
                    icon = Icons.Default.LocationOn,
                    label = "Physical Address",
                    value = lead.address,
                    onCopy = { clipboardManager.setText(AnnotatedString(lead.address)) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Social Profiles
            val socialsList = listOfNotNull(
                if (lead.linkedin.isNotBlank()) "LinkedIn" to lead.linkedin else null,
                if (lead.xTwitter.isNotBlank()) "X / Twitter" to lead.xTwitter else null,
                if (lead.facebook.isNotBlank()) "Facebook" to lead.facebook else null,
                if (lead.instagram.isNotBlank()) "Instagram" to lead.instagram else null,
                if (lead.youtube.isNotBlank()) "YouTube" to lead.youtube else null,
                if (lead.tiktok.isNotBlank()) "TikTok" to lead.tiktok else null
            )
            if (socialsList.isNotEmpty()) {
                Text(
                    text = "SOCIAL PROFILES",
                    color = CyberViolet,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                for ((network, url) in socialsList) {
                    Text(
                        text = "• $network: $url",
                        color = CyberTextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .clickable { clipboardManager.setText(AnnotatedString(url)) }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Technical Details
            Text(
                text = "TECHNICAL TELEMETRY & FINGERPRINT",
                color = CyberTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "SHA-256 FP: ${lead.fingerprint}",
                color = CyberTextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Source Target: ${lead.url}",
                color = CyberTextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Keywords: ${lead.keywordsFound.ifEmpty { "None" }}",
                color = CyberTextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (onSyncWebhook != null) {
                    Button(
                        onClick = { onSyncWebhook(lead); onDismiss() },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPrimaryCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(38.dp)
                            .testTag("sync_webhook_lead_btn")
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = CyberBackground, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync CRM", color = CyberBackground, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Button(
                    onClick = { onDelete(lead); onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCrimson.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .border(1.dp, CyberCrimson.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .testTag("delete_lead_button")
                ) {
                    Text("Delete", color = CyberCrimson, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(0.9f)
                        .height(38.dp)
                        .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
                        .testTag("close_detail_button")
                ) {
                    Text("Close", color = CyberTextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            color = CyberTextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = CyberTextPrimary,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 1.dp)
        )
    }
}

@Composable
private fun ContactRow(
    icon: ImageVector,
    label: String,
    value: String,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(CyberSurfaceVariant)
            .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
            .clickable { onCopy() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CyberPrimaryCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = label,
                    color = CyberTextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = value,
                    color = CyberTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Text(
            text = "COPY",
            color = CyberPrimaryCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun AuditItemRow(audit: com.example.leadforge.data.entity.AuditEntity) {
    val levelColor = when (audit.level) {
        "SECURITY" -> CyberCrimson
        "WARNING" -> CyberAmber
        "ERROR" -> CyberCrimson
        else -> CyberEmerald
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CyberSurface)
            .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(levelColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = audit.event,
                    color = levelColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date(audit.timestamp)),
                    color = CyberTextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = audit.detail,
                color = CyberTextSecondary,
                fontSize = 11.sp,
                maxLines = 2
            )
        }
    }
}

@Composable
fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) CyberPrimaryCyan else CyberSurfaceVariant)
            .border(1.dp, if (isSelected) CyberPrimaryCyan else CyberBorder, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) CyberBackground else CyberTextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun LeadCardItem(
    lead: LeadEntity,
    onClick: () -> Unit
) {
    val scoreColor = when {
        lead.keywordScore >= 75 -> CyberEmerald
        lead.keywordScore >= 45 -> CyberAmber
        else -> CyberTextSecondary
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .testTag("lead_card_${lead.fingerprint}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lead.name.ifEmpty { "Entity Record" },
                        color = CyberTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = lead.domain,
                        color = CyberPrimaryCyan,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Score pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(scoreColor.copy(alpha = 0.12f))
                        .border(1.dp, scoreColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${lead.keywordScore.toInt()}%",
                        color = scoreColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            if (lead.category.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lead.category,
                    color = CyberTextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Contact chips row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (lead.email.isNotBlank()) {
                    ContactPill(icon = Icons.Default.Email, text = lead.email.take(24), color = CyberPrimaryCyan)
                }
                if (lead.phone.isNotBlank()) {
                    ContactPill(icon = Icons.Default.Phone, text = lead.phone, color = CyberEmerald)
                }
            }

            if (lead.keywordsFound.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "KEYWORDS: ${lead.keywordsFound}",
                    color = CyberTextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun ContactPill(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(CyberSurfaceVariant)
            .border(1.dp, CyberBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = CyberTextPrimary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun PresetPill(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = CyberSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
        modifier = Modifier.testTag("preset_$label")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(CyberPrimaryCyan)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = CyberTextPrimary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

