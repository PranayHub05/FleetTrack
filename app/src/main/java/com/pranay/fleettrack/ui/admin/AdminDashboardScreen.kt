package com.pranay.fleettrack.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pranay.fleettrack.model.DailyLog
import com.pranay.fleettrack.ui.components.SummaryCard
import com.pranay.fleettrack.ui.theme.*
import com.pranay.fleettrack.util.toCurrencyString
import com.pranay.fleettrack.util.toFormattedDate
import com.pranay.fleettrack.util.getStartOfMonth
import com.pranay.fleettrack.util.getEndOfMonth
import com.pranay.fleettrack.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    adminViewModel: AdminViewModel,
    onNavigateToManage: () -> Unit,
    onNavigateToLedger: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToMaps: () -> Unit,
    onNavigateToTrips: () -> Unit,
    onSignOut: () -> Unit
) {
    val drivers by adminViewModel.drivers.collectAsStateWithLifecycle()
    val cars by adminViewModel.cars.collectAsStateWithLifecycle()
    val dailyLogs by adminViewModel.dailyLogs.collectAsStateWithLifecycle()
    val isLoading by adminViewModel.isLoading.collectAsStateWithLifecycle()
    val aiSummary by adminViewModel.aiSummary.collectAsStateWithLifecycle()
    val isGeneratingSummary by adminViewModel.isGeneratingSummary.collectAsStateWithLifecycle()

    val startSec = getStartOfMonth().seconds
    val endSec = getEndOfMonth().seconds
    val currentMonthLogs = dailyLogs.filter { it.date.seconds in startSec..endSec }
    val monthlyIncome = currentMonthLogs.sumOf { it.income }
    val monthlyExpense = currentMonthLogs.sumOf { it.expense }
    val monthlyProfit = monthlyIncome - monthlyExpense

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Dashboard",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                actions = {
                    IconButton(onClick = { adminViewModel.refreshData() }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh",
                            tint = TextSecondary
                        )
                    }
                    IconButton(onClick = onSignOut) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign Out",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark
                )
            )
        },
        containerColor = SurfaceDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Summary Cards
            SummaryCard(
                title = "Total Income",
                value = monthlyIncome.toCurrencyString(),
                icon = Icons.Filled.TrendingUp,
                gradientColors = listOf(Blue600, Blue500, Blue400)
            )

            SummaryCard(
                title = "Total Expenses",
                value = monthlyExpense.toCurrencyString(),
                icon = Icons.Filled.LocalGasStation,
                gradientColors = listOf(Amber600, Amber500, Amber400)
            )

            SummaryCard(
                title = "Net Profit",
                value = monthlyProfit.toCurrencyString(),
                icon = Icons.Filled.AccountBalance,
                gradientColors = listOf(Emerald600, Emerald500, Emerald400)
            )

            // Quick Actions
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    icon = Icons.Filled.DirectionsCar,
                    label = "Fleet",
                    onClick = onNavigateToManage,
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    icon = Icons.Filled.MenuBook,
                    label = "Ledger",
                    onClick = onNavigateToLedger,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    icon = Icons.Filled.BarChart,
                    label = "Stats",
                    onClick = onNavigateToStats,
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    icon = Icons.Filled.Route,
                    label = "Trips",
                    onClick = onNavigateToTrips,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    icon = Icons.Filled.MyLocation,
                    label = "Live Tracking",
                    onClick = onNavigateToMaps,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            // Recent Activity
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            val recentLogs = dailyLogs.take(5)
            if (recentLogs.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recent activity",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                }
            } else {
                recentLogs.forEach { log ->
                    RecentActivityItem(log = log)
                }
            }

            // AI Summary
            AISummarySection(
                aiSummary = aiSummary,
                isGenerating = isGeneratingSummary,
                onGenerateClick = {
                    adminViewModel.generateAISummary(monthlyIncome, monthlyExpense, monthlyProfit)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(listOf(Blue600, Blue400))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun RecentActivityItem(log: DailyLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Blue500.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Receipt,
                    contentDescription = null,
                    tint = Blue400,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.driverName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${log.carName} · ${log.date.toFormattedDate()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${log.income.toCurrencyString()}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = ProfitGreen
                )
                Text(
                    text = "-${log.expense.toCurrencyString()}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = ExpenseAmber
                )
            }
        }
    }
}
