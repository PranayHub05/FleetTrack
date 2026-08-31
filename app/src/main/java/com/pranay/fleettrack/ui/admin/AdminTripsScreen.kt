package com.pranay.fleettrack.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pranay.fleettrack.model.DailyLog
import com.pranay.fleettrack.ui.components.EmptyState
import com.pranay.fleettrack.ui.theme.*
import com.pranay.fleettrack.util.toCurrencyString
import com.pranay.fleettrack.util.toFormattedDate
import com.pranay.fleettrack.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTripsScreen(
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
                        text = "Trips",
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

            // Trip entries
            if (filteredLogs.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Route,
                    title = "No trips found",
                    subtitle = "No trips match the selected filters"
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredLogs, key = { it.id }) { log ->
                        TripCard(log = log)
                    }
                }
            }
        }
    }
}

@Composable
private fun TripCard(log: DailyLog) {
    val profit = log.income - log.expense - (log.driverPay ?: 0.0)
    val source = log.source.ifBlank { "Not specified" }
    val destination = log.destination.ifBlank { "Not specified" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // ── Route header + Date ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Route: Source → Destination
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Blue500.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Navigation,
                            contentDescription = null,
                            tint = Blue400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = source,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Blue400,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = destination,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Date badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Navy700
                ) {
                    Text(
                        text = log.date.toFormattedDate(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = SurfaceCardLight.copy(alpha = 0.5f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // ── Driver & Car row ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "👤", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = log.driverName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "|",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "🚗", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = log.carName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // ── Distance row ──
            if (log.startMeter != null && log.endMeter != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Navy700.copy(alpha = 0.5f))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📏", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${log.startMeter.toLong()} → ${log.endMeter.toLong()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Blue500.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Distance: ${log.kmDriven?.toLong() ?: 0} km",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Blue400,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Financial summary row ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Income
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Income",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "₹${log.income.toCurrencyString()}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = ProfitGreen
                    )
                }

                // Divider dot
                Text(
                    text = "•",
                    color = TextMuted,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Expense
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Expense",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "₹${log.expense.toCurrencyString()}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseAmber
                    )
                }

                // Divider dot
                Text(
                    text = "•",
                    color = TextMuted,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Profit
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Profit",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (profit >= 0) "₹${profit.toCurrencyString()}" else "-₹${(-profit).toCurrencyString()}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (profit >= 0) Emerald400 else Rose400
                    )
                }
            }

            // ── Driver Pay row ──
            if (log.driverPay != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Blue500.copy(alpha = 0.08f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "💰", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Driver Pay:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "₹${log.driverPay.toCurrencyString()}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Blue400
                    )
                }
            }

            // ── Notes row ──
            if (!log.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceCardLight.copy(alpha = 0.3f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "📝",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 1.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = log.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
