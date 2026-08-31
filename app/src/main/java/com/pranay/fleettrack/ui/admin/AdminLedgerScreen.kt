package com.pranay.fleettrack.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pranay.fleettrack.model.Car
import com.pranay.fleettrack.model.DailyLog
import com.pranay.fleettrack.model.Driver
import com.pranay.fleettrack.ui.components.EmptyState
import com.pranay.fleettrack.ui.theme.*
import com.pranay.fleettrack.util.toCurrencyString
import com.pranay.fleettrack.util.toFormattedDate
import com.pranay.fleettrack.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLedgerScreen(
    adminViewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val drivers by adminViewModel.drivers.collectAsStateWithLifecycle()
    val cars by adminViewModel.cars.collectAsStateWithLifecycle()
    val dailyLogs by adminViewModel.dailyLogs.collectAsStateWithLifecycle()

    var selectedDriverId by remember { mutableStateOf<String?>(null) }
    var selectedCarId by remember { mutableStateOf<String?>(null) }

    var driverDropdownExpanded by remember { mutableStateOf(false) }
    var carDropdownExpanded by remember { mutableStateOf(false) }

    val filteredLogs = remember(dailyLogs, selectedDriverId, selectedCarId) {
        adminViewModel.getFilteredLogs(selectedDriverId, selectedCarId)
            .sortedByDescending { it.date }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ledger",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
        ) {
            // Filter section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Driver filter
                Box(modifier = Modifier.weight(1f)) {
                    FilterChip(
                        selected = selectedDriverId != null,
                        onClick = { driverDropdownExpanded = true },
                        label = {
                            Text(
                                text = if (selectedDriverId != null) {
                                    drivers.find { it.id == selectedDriverId }?.name ?: "Driver"
                                } else "By Driver",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = if (selectedDriverId != null) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SurfaceCard,
                            labelColor = TextSecondary,
                            iconColor = TextMuted,
                            selectedContainerColor = Blue500.copy(alpha = 0.2f),
                            selectedLabelColor = Blue400,
                            selectedLeadingIconColor = Blue400
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = SurfaceCardLight,
                            selectedBorderColor = Blue500.copy(alpha = 0.5f),
                            enabled = true,
                            selected = selectedDriverId != null
                        )
                    )
                    DropdownMenu(
                        expanded = driverDropdownExpanded,
                        onDismissRequest = { driverDropdownExpanded = false },
                        containerColor = SurfaceCard
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Drivers", color = TextSecondary) },
                            onClick = {
                                selectedDriverId = null
                                driverDropdownExpanded = false
                            }
                        )
                        drivers.forEach { driver ->
                            DropdownMenuItem(
                                text = { Text(driver.name, color = TextPrimary) },
                                onClick = {
                                    selectedDriverId = driver.id
                                    driverDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Car filter
                Box(modifier = Modifier.weight(1f)) {
                    FilterChip(
                        selected = selectedCarId != null,
                        onClick = { carDropdownExpanded = true },
                        label = {
                            Text(
                                text = if (selectedCarId != null) {
                                    cars.find { it.id == selectedCarId }?.name ?: "Car"
                                } else "By Car",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.DirectionsCar,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = if (selectedCarId != null) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SurfaceCard,
                            labelColor = TextSecondary,
                            iconColor = TextMuted,
                            selectedContainerColor = Emerald500.copy(alpha = 0.2f),
                            selectedLabelColor = Emerald400,
                            selectedLeadingIconColor = Emerald400
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = SurfaceCardLight,
                            selectedBorderColor = Emerald500.copy(alpha = 0.5f),
                            enabled = true,
                            selected = selectedCarId != null
                        )
                    )
                    DropdownMenu(
                        expanded = carDropdownExpanded,
                        onDismissRequest = { carDropdownExpanded = false },
                        containerColor = SurfaceCard
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Cars", color = TextSecondary) },
                            onClick = {
                                selectedCarId = null
                                carDropdownExpanded = false
                            }
                        )
                        cars.forEach { car ->
                            DropdownMenuItem(
                                text = { Text(car.name, color = TextPrimary) },
                                onClick = {
                                    selectedCarId = car.id
                                    carDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Log entries
            if (filteredLogs.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.MenuBook,
                    title = "No Entries",
                    subtitle = "No ledger entries found for the selected filters"
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredLogs, key = { it.id }) { log ->
                        LedgerEntryCard(log = log)
                    }
                }
            }
        }
    }
}

@Composable
private fun LedgerEntryCard(log: DailyLog) {
    val net = log.income - log.expense

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top row: date and net
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = log.date.toFormattedDate(),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (net >= 0) Emerald500.copy(alpha = 0.15f) else Rose500.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (net >= 0) "+${net.toCurrencyString()}" else net.toCurrencyString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (net >= 0) Emerald400 else Rose400,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Driver and car
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Blue500.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = Blue400,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = log.driverName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = log.carName,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Income and Expense row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.TrendingUp,
                        contentDescription = "Income",
                        tint = ProfitGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = log.income.toCurrencyString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = ProfitGreen
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.TrendingDown,
                        contentDescription = "Expense",
                        tint = ExpenseAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = log.expense.toCurrencyString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = ExpenseAmber
                    )
                }
            }

            // Distance info row
            if (log.startMeter != null && log.endMeter != null) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = SurfaceCardLight, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "📏",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${log.startMeter.toLong()} → ${log.endMeter.toLong()} km",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = "${log.kmDriven?.toLong() ?: 0} km",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Blue400
                    )
                }
            }
        }
    }
}
