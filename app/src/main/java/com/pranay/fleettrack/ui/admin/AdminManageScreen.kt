package com.pranay.fleettrack.ui.admin

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pranay.fleettrack.model.Car
import com.pranay.fleettrack.model.Driver
import com.pranay.fleettrack.ui.components.EmptyState
import com.pranay.fleettrack.ui.theme.*
import com.pranay.fleettrack.viewmodel.AdminViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminManageScreen(
    adminViewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val drivers by adminViewModel.drivers.collectAsStateWithLifecycle()
    val cars by adminViewModel.cars.collectAsStateWithLifecycle()
    val isLoading by adminViewModel.isLoading.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Drivers", "Cars")

    // Dialog states
    var showDriverDialog by remember { mutableStateOf(false) }
    var editingDriver by remember { mutableStateOf<Driver?>(null) }
    var showCarDialog by remember { mutableStateOf(false) }
    var editingCar by remember { mutableStateOf<Car?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Manage Fleet",
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) {
                        editingDriver = null
                        showDriverDialog = true
                    } else {
                        editingCar = null
                        showCarDialog = true
                    }
                },
                containerColor = Blue500,
                contentColor = TextPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = if (selectedTab == 0) "Add Driver" else "Add Car"
                )
            }
        },
        containerColor = SurfaceDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceCard,
                contentColor = Blue400,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Blue500
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selectedTab == index) Blue400 else TextMuted
                            )
                        }
                    )
                }
            }

            // Tab Content
            AnimatedVisibility(
                visible = selectedTab == 0,
                enter = fadeIn() + slideInHorizontally { -it },
                exit = fadeOut() + slideOutHorizontally { -it }
            ) {
                DriversTab(
                    drivers = drivers,
                    cars = cars,
                    onEditDriver = { driver ->
                        editingDriver = driver
                        showDriverDialog = true
                    },
                    onDeleteDriver = { driver ->
                        adminViewModel.deleteDriver(driver.id)
                    }
                )
            }

            AnimatedVisibility(
                visible = selectedTab == 1,
                enter = fadeIn() + slideInHorizontally { it },
                exit = fadeOut() + slideOutHorizontally { it }
            ) {
                CarsTab(
                    cars = cars,
                    onEditCar = { car ->
                        editingCar = car
                        showCarDialog = true
                    },
                    onDeleteCar = { car ->
                        adminViewModel.deleteCar(car.id)
                    }
                )
            }
        }
    }

    // Dialogs
    if (showDriverDialog) {
        AddEditDriverDialog(
            driver = editingDriver,
            cars = cars,
            onSave = { driver ->
                if (editingDriver != null) {
                    adminViewModel.updateDriver(driver)
                } else {
                    adminViewModel.addDriver(driver)
                }
                showDriverDialog = false
                editingDriver = null
            },
            onDismiss = {
                showDriverDialog = false
                editingDriver = null
            }
        )
    }

    if (showCarDialog) {
        AddEditCarDialog(
            car = editingCar,
            onSave = { car ->
                if (editingCar != null) {
                    adminViewModel.updateCar(car)
                } else {
                    adminViewModel.addCar(car)
                }
                showCarDialog = false
                editingCar = null
            },
            onDismiss = {
                showCarDialog = false
                editingCar = null
            }
        )
    }
}

@Composable
private fun DriversTab(
    drivers: List<Driver>,
    cars: List<Car>,
    onEditDriver: (Driver) -> Unit,
    onDeleteDriver: (Driver) -> Unit
) {
    if (drivers.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.PersonOff,
            title = "No Drivers",
            subtitle = "Add your first driver to get started"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(drivers, key = { it.id }) { driver ->
                val assignedCarNames = cars.filter { it.id in driver.assignedCarIds }.joinToString(", ") { it.name }
                DriverCard(
                    driver = driver,
                    assignedCarName = assignedCarNames.ifEmpty { null },
                    onEdit = { onEditDriver(driver) },
                    onDelete = { onDeleteDriver(driver) }
                )
            }
        }
    }
}

@Composable
private fun CarsTab(
    cars: List<Car>,
    onEditCar: (Car) -> Unit,
    onDeleteCar: (Car) -> Unit
) {
    if (cars.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.DirectionsCarFilled,
            title = "No Cars",
            subtitle = "Add your first car to get started"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(cars, key = { it.id }) { car ->
                CarCard(
                    car = car,
                    onEdit = { onEditCar(car) },
                    onDelete = { onDeleteCar(car) }
                )
            }
        }
    }
}

@Composable
private fun DriverCard(
    driver: Driver,
    assignedCarName: String?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Blue500.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = Blue400,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = driver.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(isActive = driver.isActive)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "📱 ${driver.phone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "🪪 ${driver.licenseNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                if (assignedCarName != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "🚗 $assignedCarName${if (driver.isFixedToCar) " (Fixed)" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Emerald400
                    )
                }
                if (driver.loginCode != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🔑 Code: ${driver.loginCode}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Blue400,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Driver Code", driver.loginCode)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Code copied!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Copy code",
                                tint = Blue400,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Column {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = TextMuted
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = Rose400
                    )
                }
            }
        }
    }
}

@Composable
private fun CarCard(
    car: Car,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Emerald500.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.DirectionsCar,
                    contentDescription = null,
                    tint = Emerald400,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = car.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(isActive = car.isActive)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "🔢 ${car.licensePlate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "⛽ ${car.averageMileage} km/l  ·  ₹${car.fuelPrice}/l",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                // Owner, Chassis, Engine info
                if (car.ownerName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "👤 ${car.ownerName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                if (car.chassisNumber.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "🔩 Chassis: ${car.chassisNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                if (car.engineNumber.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "⚙️ Engine: ${car.engineNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                // Expiry dates with color coding
                val expiryFields = listOf(
                    "📋 Reg" to car.registrationExpiry,
                    "🛡️ Ins" to car.insuranceExpiry,
                    "🏋️ Fit" to car.fitnessExpiry,
                    "📄 Per" to car.permitExpiry
                )

                val hasAnyExpiry = expiryFields.any { it.second.isNotBlank() }
                if (hasAnyExpiry) {
                    Spacer(modifier = Modifier.height(8.dp))
                    expiryFields.forEach { (label, dateStr) ->
                        if (dateStr.isNotBlank()) {
                            val color = getExpiryColor(dateStr)
                            Text(
                                text = "$label: $dateStr",
                                style = MaterialTheme.typography.bodySmall,
                                color = color,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Column {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = TextMuted
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = Rose400
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(isActive: Boolean) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isActive) Emerald500.copy(alpha = 0.15f) else Rose500.copy(alpha = 0.15f)
    ) {
        Text(
            text = if (isActive) "Active" else "Inactive",
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) Emerald400 else Rose400,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

/**
 * Parses a DD/MM/YYYY date string and returns a color based on proximity to today:
 * - Rose400 if expired or within 15 days
 * - Amber400 if within 30 days
 * - Emerald400 if more than 30 days away
 * - TextSecondary if unparseable
 */
@Composable
private fun getExpiryColor(dateStr: String): androidx.compose.ui.graphics.Color {
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.isLenient = false
        val expiryDate = sdf.parse(dateStr) ?: return TextSecondary
        val today = Date()
        val diffMillis = expiryDate.time - today.time
        val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)

        when {
            diffDays < 15 -> Rose400      // Expired or within 15 days
            diffDays < 30 -> Amber400     // Within 30 days
            else -> Emerald400            // More than 30 days
        }
    } catch (e: Exception) {
        TextSecondary
    }
}
