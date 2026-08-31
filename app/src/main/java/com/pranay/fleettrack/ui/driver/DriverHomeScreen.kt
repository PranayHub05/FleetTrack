package com.pranay.fleettrack.ui.driver

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pranay.fleettrack.model.Car
import com.pranay.fleettrack.model.DailyLog
import com.pranay.fleettrack.ui.components.EmptyState
import com.pranay.fleettrack.ui.components.LoadingOverlay
import com.pranay.fleettrack.ui.theme.Amber400
import com.pranay.fleettrack.ui.theme.Amber500
import com.pranay.fleettrack.ui.theme.Emerald400
import com.pranay.fleettrack.ui.theme.Emerald500
import com.pranay.fleettrack.ui.theme.Emerald600
import com.pranay.fleettrack.ui.theme.ExpenseAmber
import com.pranay.fleettrack.ui.theme.Navy800
import com.pranay.fleettrack.ui.theme.Navy900
import com.pranay.fleettrack.ui.theme.ProfitGreen
import com.pranay.fleettrack.ui.theme.Rose400
import com.pranay.fleettrack.ui.theme.Rose500
import com.pranay.fleettrack.ui.theme.SurfaceCard
import com.pranay.fleettrack.ui.theme.TextMuted
import com.pranay.fleettrack.ui.theme.TextPrimary
import com.pranay.fleettrack.ui.theme.TextSecondary
import com.pranay.fleettrack.util.toCurrencyString
import com.pranay.fleettrack.util.toFormattedDate
import com.pranay.fleettrack.viewmodel.DriverViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverHomeScreen(
    driverViewModel: DriverViewModel,
    driverName: String,
    onNavigateToLogEntry: () -> Unit,
    onSignOut: () -> Unit,
    onDutyToggle: (Boolean) -> Unit
) {
    val driver by driverViewModel.driver.collectAsState()
    val recentLogs by driverViewModel.recentLogs.collectAsState()
    val isLoading by driverViewModel.isLoading.collectAsState()
    val assignedCars by driverViewModel.assignedCars.collectAsState()
    val isOnDuty by driverViewModel.isOnDuty.collectAsState()

    val todayLog = recentLogs.firstOrNull { log ->
        val logCal = Calendar.getInstance().apply { time = log.date.toDate() }
        val todayCal = Calendar.getInstance()
        logCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                logCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Navy900,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = driverName,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Navy800
                    ),
                    actions = {
                        IconButton(onClick = { driverViewModel.refreshData() }) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Refresh",
                                tint = TextSecondary
                            )
                        }
                        IconButton(onClick = onSignOut) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Sign Out",
                                tint = TextSecondary
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Welcome greeting
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Emerald500.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Emerald400,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Hello, $driverName!",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Welcome back",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // My Vehicle(s) cards
                if (assignedCars.isNotEmpty()) {
                    items(assignedCars) { car ->
                        MyVehicleCard(car = car)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Today's summary card
                if (todayLog != null) {
                    item {
                        TodaySummaryCard(log = todayLog!!)
                    }
                }

                // On Duty toggle card
                item {
                    OnDutyCard(
                        isOnDuty = isOnDuty,
                        onToggle = { newValue ->
                            driverViewModel.setOnDuty(newValue)
                            onDutyToggle(newValue)
                        }
                    )
                }

                // Log Today's Activity button
                item {
                    Button(
                        onClick = onNavigateToLogEntry,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald500
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Log Today's Activity",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Recent Entries header
                item {
                    Text(
                        text = "Recent Entries",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Recent entries list or empty state
                if (recentLogs.isEmpty() && !isLoading) {
                    item {
                        EmptyState(
                            message = "No entries yet. Start logging your daily activity!"
                        )
                    }
                } else {
                    items(recentLogs.take(10)) { log ->
                        RecentLogItem(log = log)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        LoadingOverlay(isLoading = isLoading)
    }
}

// ──────────────────────────────────────────────────
// My Vehicle Card
// ──────────────────────────────────────────────────

@Composable
private fun MyVehicleCard(car: Car) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header with gradient
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Emerald600.copy(alpha = 0.3f),
                                Emerald500.copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Emerald500.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = Emerald400,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = car.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = car.licensePlate,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
                Text(
                    text = "🚗",
                    fontSize = 28.sp
                )
            }

            // Details grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Key details row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    VehicleDetailChip(
                        emoji = "⛽",
                        label = "Mileage",
                        value = "${car.averageMileage} km/l",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    VehicleDetailChip(
                        emoji = "💰",
                        label = "Fuel",
                        value = "₹${car.fuelPrice}/l",
                        modifier = Modifier.weight(1f)
                    )
                }

                if (car.ownerName.isNotBlank()) {
                    VehicleDetailChip(
                        emoji = "👤",
                        label = "Owner",
                        value = car.ownerName,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HorizontalDivider(color = TextMuted.copy(alpha = 0.2f))

                // Expiry dates section
                Text(
                    text = "Document Status",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExpiryRow(label = "📋 Registration", dateString = car.registrationExpiry)
                    ExpiryRow(label = "🛡️ Insurance", dateString = car.insuranceExpiry)
                    ExpiryRow(label = "🔧 Fitness", dateString = car.fitnessExpiry)
                    ExpiryRow(label = "📜 Permit", dateString = car.permitExpiry)
                }
            }
        }
    }
}

@Composable
private fun VehicleDetailChip(
    emoji: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = Navy800,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextMuted
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun ExpiryRow(label: String, dateString: String) {
    val (statusText, statusColor) = getExpiryStatus(dateString)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextSecondary
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (dateString.isNotBlank()) {
                Text(
                    text = dateString,
                    fontSize = 13.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = statusText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor,
                modifier = Modifier
                    .background(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

private fun getExpiryStatus(dateString: String): Pair<String, Color> {
    if (dateString.isBlank()) {
        return "N/A" to TextMuted
    }
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val expiryDate = sdf.parse(dateString) ?: return "N/A" to TextMuted
        val now = Date()
        val diffMs = expiryDate.time - now.time
        val diffDays = TimeUnit.MILLISECONDS.toDays(diffMs)

        when {
            diffDays < 0 -> "Expired" to Rose500
            diffDays <= 15 -> "Expiring Soon" to Rose400
            diffDays <= 30 -> "Expiring Soon" to Amber500
            else -> "Valid" to Emerald500
        }
    } catch (_: Exception) {
        "N/A" to TextMuted
    }
}

// ──────────────────────────────────────────────────
// On Duty Card
// ──────────────────────────────────────────────────

@Composable
private fun OnDutyCard(
    isOnDuty: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "duty_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val cardBackground by animateColorAsState(
        targetValue = if (isOnDuty) Emerald600.copy(alpha = 0.15f) else SurfaceCard,
        animationSpec = tween(400),
        label = "card_bg"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = if (isOnDuty) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Emerald600.copy(alpha = 0.25f),
                                Emerald500.copy(alpha = 0.10f)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(SurfaceCard, SurfaceCard)
                        )
                    },
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pulsing dot + Icon
                Box(contentAlignment = Alignment.Center) {
                    if (isOnDuty) {
                        // Pulsing ring behind the icon
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(Emerald400.copy(alpha = 0.2f))
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isOnDuty) Emerald500.copy(alpha = 0.3f)
                                else TextMuted.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isOnDuty) Icons.Filled.LocationOn
                            else Icons.Filled.LocationOff,
                            contentDescription = null,
                            tint = if (isOnDuty) Emerald400 else TextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Text
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isOnDuty) "ON DUTY" else "OFF DUTY",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOnDuty) Emerald400 else TextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isOnDuty) "Route recording active"
                        else "Tap to start recording your route",
                        fontSize = 13.sp,
                        color = if (isOnDuty) Emerald400.copy(alpha = 0.7f) else TextMuted
                    )
                }

                // Switch
                Switch(
                    checked = isOnDuty,
                    onCheckedChange = { onToggle(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Emerald500,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = Navy800,
                        uncheckedBorderColor = TextMuted.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────
// Existing composables
// ──────────────────────────────────────────────────

@Composable
private fun TodaySummaryCard(log: DailyLog) {
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
            Text(
                text = "Today's Summary",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    label = "Income",
                    value = log.income.toCurrencyString(),
                    valueColor = ProfitGreen
                )
                SummaryItem(
                    label = "Expense",
                    value = log.expense.toCurrencyString(),
                    valueColor = ExpenseAmber
                )
                SummaryItem(
                    label = "Net",
                    value = (log.income - log.expense).toCurrencyString(),
                    valueColor = if (log.income - log.expense >= 0) ProfitGreen else ExpenseAmber
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun RecentLogItem(log: DailyLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.date.toFormattedDate(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = log.carName,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = log.income.toCurrencyString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ProfitGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "-${log.expense.toCurrencyString()}",
                    fontSize = 12.sp,
                    color = ExpenseAmber
                )
            }
        }
    }
}
