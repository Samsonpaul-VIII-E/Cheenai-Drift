package com.example.leadforge.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leadforge.data.entity.LeadEntity
import com.example.leadforge.ui.screens.AuditHealthScreen
import com.example.leadforge.ui.screens.DashboardScreen
import com.example.leadforge.ui.screens.LeadsScreen
import com.example.leadforge.ui.screens.NewJobScreen
import com.example.leadforge.ui.screens.OperationsScreen
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberPrimaryCyan
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary

@Composable
fun LeadForgeApp(viewModel: LeadForgeViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val totalLeads by viewModel.leadCount.collectAsState()
    val pagesProcessed by viewModel.visitedCount.collectAsState()
    val recentAudits by viewModel.recentAudits.collectAsState()
    val allJobs by viewModel.allJobs.collectAsState()
    val allLeads by viewModel.repository.allLeads.collectAsState(initial = emptyList())

    // BackHandler: if not on Dashboard, back returns to Dashboard
    if (currentScreen != Screen.DASHBOARD) {
        BackHandler {
            viewModel.navigateTo(Screen.DASHBOARD)
        }
    }

    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground),
        containerColor = CyberBackground,
        bottomBar = {
            if (!isWideScreen) {
                NavigationBar(
                    containerColor = CyberSurface,
                    modifier = Modifier.border(1.dp, CyberBorder)
                ) {
                    NavigationBarItem(
                        selected = currentScreen == Screen.DASHBOARD,
                        onClick = { viewModel.navigateTo(Screen.DASHBOARD) },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Console", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberPrimaryCyan,
                            selectedTextColor = CyberPrimaryCyan,
                            unselectedIconColor = CyberTextMuted,
                            unselectedTextColor = CyberTextMuted,
                            indicatorColor = CyberSurface
                        ),
                        modifier = Modifier.testTag("nav_dashboard")
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.NEW_JOB,
                        onClick = { viewModel.navigateTo(Screen.NEW_JOB) },
                        icon = { Icon(Icons.Default.AddCircle, contentDescription = "New Job") },
                        label = { Text("New Target", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberPrimaryCyan,
                            selectedTextColor = CyberPrimaryCyan,
                            unselectedIconColor = CyberTextMuted,
                            unselectedTextColor = CyberTextMuted,
                            indicatorColor = CyberSurface
                        ),
                        modifier = Modifier.testTag("nav_new_job")
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.OPERATIONS,
                        onClick = { viewModel.navigateTo(Screen.OPERATIONS) },
                        icon = { Icon(Icons.Default.Terminal, contentDescription = "Operations") },
                        label = { Text("Live Ops", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberPrimaryCyan,
                            selectedTextColor = CyberPrimaryCyan,
                            unselectedIconColor = CyberTextMuted,
                            unselectedTextColor = CyberTextMuted,
                            indicatorColor = CyberSurface
                        ),
                        modifier = Modifier.testTag("nav_operations")
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.LEADS,
                        onClick = { viewModel.navigateTo(Screen.LEADS) },
                        icon = { Icon(Icons.Default.People, contentDescription = "Leads") },
                        label = { Text("Leads", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberPrimaryCyan,
                            selectedTextColor = CyberPrimaryCyan,
                            unselectedIconColor = CyberTextMuted,
                            unselectedTextColor = CyberTextMuted,
                            indicatorColor = CyberSurface
                        ),
                        modifier = Modifier.testTag("nav_leads")
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.AUDIT_HEALTH,
                        onClick = { viewModel.navigateTo(Screen.AUDIT_HEALTH) },
                        icon = { Icon(Icons.Default.HealthAndSafety, contentDescription = "Audit & Health") },
                        label = { Text("Doctor", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberPrimaryCyan,
                            selectedTextColor = CyberPrimaryCyan,
                            unselectedIconColor = CyberTextMuted,
                            unselectedTextColor = CyberTextMuted,
                            indicatorColor = CyberSurface
                        ),
                        modifier = Modifier.testTag("nav_audit")
                    )
                }
            }
        }
    ) { innerPadding ->
        if (isWideScreen) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavigationRail(
                    containerColor = CyberSurface,
                    modifier = Modifier
                        .fillMaxHeight()
                        .border(1.dp, CyberBorder)
                ) {
                    NavigationRailItem(
                        selected = currentScreen == Screen.DASHBOARD,
                        onClick = { viewModel.navigateTo(Screen.DASHBOARD) },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Console", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = CyberPrimaryCyan,
                            selectedTextColor = CyberPrimaryCyan,
                            unselectedIconColor = CyberTextMuted,
                            unselectedTextColor = CyberTextMuted,
                            indicatorColor = CyberSurface
                        )
                    )

                    NavigationRailItem(
                        selected = currentScreen == Screen.NEW_JOB,
                        onClick = { viewModel.navigateTo(Screen.NEW_JOB) },
                        icon = { Icon(Icons.Default.AddCircle, contentDescription = "New Job") },
                        label = { Text("New Target", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = CyberPrimaryCyan,
                            selectedTextColor = CyberPrimaryCyan,
                            unselectedIconColor = CyberTextMuted,
                            unselectedTextColor = CyberTextMuted,
                            indicatorColor = CyberSurface
                        )
                    )

                    NavigationRailItem(
                        selected = currentScreen == Screen.OPERATIONS,
                        onClick = { viewModel.navigateTo(Screen.OPERATIONS) },
                        icon = { Icon(Icons.Default.Terminal, contentDescription = "Operations") },
                        label = { Text("Live Ops", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = CyberPrimaryCyan,
                            selectedTextColor = CyberPrimaryCyan,
                            unselectedIconColor = CyberTextMuted,
                            unselectedTextColor = CyberTextMuted,
                            indicatorColor = CyberSurface
                        )
                    )

                    NavigationRailItem(
                        selected = currentScreen == Screen.LEADS,
                        onClick = { viewModel.navigateTo(Screen.LEADS) },
                        icon = { Icon(Icons.Default.People, contentDescription = "Leads") },
                        label = { Text("Leads", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = CyberPrimaryCyan,
                            selectedTextColor = CyberPrimaryCyan,
                            unselectedIconColor = CyberTextMuted,
                            unselectedTextColor = CyberTextMuted,
                            indicatorColor = CyberSurface
                        )
                    )

                    NavigationRailItem(
                        selected = currentScreen == Screen.AUDIT_HEALTH,
                        onClick = { viewModel.navigateTo(Screen.AUDIT_HEALTH) },
                        icon = { Icon(Icons.Default.HealthAndSafety, contentDescription = "Audit & Health") },
                        label = { Text("Doctor", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = CyberPrimaryCyan,
                            selectedTextColor = CyberPrimaryCyan,
                            unselectedIconColor = CyberTextMuted,
                            unselectedTextColor = CyberTextMuted,
                            indicatorColor = CyberSurface
                        )
                    )
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    ScreenContent(currentScreen, viewModel, telemetry, totalLeads, pagesProcessed, allLeads, recentAudits, allJobs)
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                ScreenContent(currentScreen, viewModel, telemetry, totalLeads, pagesProcessed, allLeads, recentAudits, allJobs)
            }
        }
    }
}

@Composable
private fun ScreenContent(
    currentScreen: Screen,
    viewModel: LeadForgeViewModel,
    telemetry: com.example.leadforge.engine.EngineTelemetry,
    totalLeads: Int,
    pagesProcessed: Int,
    leads: List<LeadEntity>,
    recentAudits: List<com.example.leadforge.data.entity.AuditEntity>,
    allJobs: List<com.example.leadforge.data.entity.JobEntity>
) {
    when (currentScreen) {
        Screen.DASHBOARD -> DashboardScreen(
            telemetry = telemetry,
            totalLeadsCount = totalLeads,
            pagesProcessedCount = pagesProcessed,
            leads = leads,
            recentAudits = recentAudits,
            allJobs = allJobs,
            onNavigate = { viewModel.navigateTo(it) },
            onPause = { viewModel.pauseOperation() },
            onResume = { viewModel.resumeOperation() }
        )
        Screen.NEW_JOB -> NewJobScreen(
            viewModel = viewModel
        )
        Screen.OPERATIONS -> OperationsScreen(
            viewModel = viewModel
        )
        Screen.LEADS -> LeadsScreen(
            viewModel = viewModel
        )
        Screen.AUDIT_HEALTH -> AuditHealthScreen(
            viewModel = viewModel
        )
    }
}
