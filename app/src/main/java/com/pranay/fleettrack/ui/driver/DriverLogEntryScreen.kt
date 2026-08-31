package com.pranay.fleettrack.ui.driver

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pranay.fleettrack.model.Car
import com.pranay.fleettrack.ui.components.LoadingOverlay
import com.pranay.fleettrack.ui.theme.Emerald400
import com.pranay.fleettrack.ui.theme.Emerald500
import com.pranay.fleettrack.ui.theme.Navy800
import com.pranay.fleettrack.ui.theme.Navy900
import com.pranay.fleettrack.ui.theme.SurfaceCard
import com.pranay.fleettrack.ui.theme.TextMuted
import com.pranay.fleettrack.ui.theme.TextPrimary
import com.pranay.fleettrack.ui.theme.TextSecondary
import com.pranay.fleettrack.viewmodel.DriverViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverLogEntryScreen(
    driverViewModel: DriverViewModel,
    onBack: () -> Unit,
    onLogSubmitted: () -> Unit
) {
    val driver by driverViewModel.driver.collectAsState()
    val assignedCars by driverViewModel.assignedCars.collectAsState()
    val availableCars by driverViewModel.availableCars.collectAsState()
    val isLoading by driverViewModel.isLoading.collectAsState()
    val errorMessage by driverViewModel.errorMessage.collectAsState()
    val logSubmitted by driverViewModel.logSubmitted.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Form state
    var selectedDate by remember { mutableStateOf(Date()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedCar by remember { mutableStateOf<Car?>(null) }
    var carDropdownExpanded by remember { mutableStateOf(false) }
    var incomeText by remember { mutableStateOf("") }
    var expenseText by remember { mutableStateOf("") }
    var startMeterText by remember { mutableStateOf("") }
    var endMeterText by remember { mutableStateOf("") }
    var sourceText by remember { mutableStateOf("") }
    var destinationText by remember { mutableStateOf("") }
    var driverPayText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val isFixedToCar = driver?.isFixedToCar == true && assignedCars.isNotEmpty()

    // Set assigned car as selected if fixed to a single car
    LaunchedEffect(isFixedToCar, assignedCars) {
        if (isFixedToCar && assignedCars.size == 1) {
            selectedCar = assignedCars.first()
        }
    }

    // Handle log submission success
    LaunchedEffect(logSubmitted) {
        if (logSubmitted) {
            onLogSubmitted()
            driverViewModel.resetLogSubmitted()
        }
    }

    // Show error via snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            driverViewModel.clearError()
        }
    }

    // Show validation error via snackbar
    LaunchedEffect(validationError) {
        validationError?.let {
            snackbarHostState.showSnackbar(it)
            validationError = null
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate.time)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate = Date(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        cursorColor = Emerald500,
        focusedBorderColor = Emerald500,
        unfocusedBorderColor = TextMuted,
        focusedLabelColor = Emerald500,
        unfocusedLabelColor = TextSecondary,
        focusedPrefixColor = TextSecondary,
        unfocusedPrefixColor = TextMuted
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Navy900,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(snackbarData = data)
                }
            },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Log Daily Activity",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Navy800
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Date field
                        Text(
                            text = "Date",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = dateFormatter.format(selectedDate),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Select Date",
                                    tint = Emerald500,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // 2. Car selection
                        Text(
                            text = "Car",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // 2. Select Vehicle
                        if (isFixedToCar && assignedCars.size == 1) {
                            // Fixed to a single car
                            val singleAssignedCar = assignedCars.first()
                            OutlinedTextField(
                                value = singleAssignedCar.name,
                                onValueChange = {},
                                label = { Text("Vehicle") },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = textFieldColors,
                                shape = RoundedCornerShape(12.dp)
                            )
                        } else {
                            // Dropdown for car selection
                            ExposedDropdownMenuBox(
                                expanded = carDropdownExpanded,
                                onExpandedChange = { carDropdownExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedCar?.name ?: "Select a car",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Vehicle") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = carDropdownExpanded)
                                    },
                                    colors = textFieldColors,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = carDropdownExpanded,
                                    onDismissRequest = { carDropdownExpanded = false }
                                ) {
                                    val carsToDisplay = if (isFixedToCar) assignedCars else availableCars
                                    carsToDisplay.forEach { car ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(
                                                        text = car.name,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        text = car.licensePlate,
                                                        fontSize = 12.sp,
                                                        color = TextMuted
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedCar = car
                                                carDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Income
                        OutlinedTextField(
                            value = incomeText,
                            onValueChange = { incomeText = it },
                            label = { Text("Income (₹)") },
                            prefix = { Text("₹") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = textFieldColors,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // 4. Expense
                        OutlinedTextField(
                            value = expenseText,
                            onValueChange = { expenseText = it },
                            label = { Text("Expense (₹)") },
                            prefix = { Text("₹") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = textFieldColors,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // 5. Source
                        OutlinedTextField(
                            value = sourceText,
                            onValueChange = { sourceText = it },
                            label = { Text("Source / Pickup") },
                            placeholder = { Text("e.g. Mumbai Central") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = textFieldColors,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // 6. Destination
                        OutlinedTextField(
                            value = destinationText,
                            onValueChange = { destinationText = it },
                            label = { Text("Destination / Drop") },
                            placeholder = { Text("e.g. Pune Station") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = textFieldColors,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // 7. Start Meter
                        OutlinedTextField(
                            value = startMeterText,
                            onValueChange = { startMeterText = it },
                            label = { Text("Start Meter (km)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = textFieldColors,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // 6. End Meter
                        OutlinedTextField(
                            value = endMeterText,
                            onValueChange = { endMeterText = it },
                            label = { Text("End Meter (km)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = textFieldColors,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Auto-calculated distance display
                        val startMeter = startMeterText.toDoubleOrNull()
                        val endMeter = endMeterText.toDoubleOrNull()
                        if (startMeter != null && endMeter != null && endMeter >= startMeter) {
                            val distance = endMeter - startMeter
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = Emerald500.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "📏 Distance: ${String.format("%.1f", distance)} km",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Emerald400
                                )
                            }
                        }

                        // Driver Pay (optional)
                        OutlinedTextField(
                            value = driverPayText,
                            onValueChange = { driverPayText = it },
                            label = { Text("Driver Pay (optional)") },
                            prefix = { Text("₹") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = textFieldColors,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Notes (optional)
                        OutlinedTextField(
                            value = notesText,
                            onValueChange = { notesText = it },
                            label = { Text("Notes (optional)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            maxLines = 4,
                            colors = textFieldColors,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Submit button
                Button(
                    onClick = {
                        // Validation
                        val car = if (isFixedToCar && assignedCars.size == 1) assignedCars.first() else selectedCar
                        if (car == null) {
                            validationError = "Please select a car"
                            return@Button
                        }

                        val income = incomeText.toDoubleOrNull()
                        if (income == null || income < 0) {
                            validationError = "Please enter a valid income amount (≥ 0)"
                            return@Button
                        }

                        val expense = expenseText.toDoubleOrNull()
                        if (expense == null || expense < 0) {
                            validationError = "Please enter a valid expense amount (≥ 0)"
                            return@Button
                        }

                        val startMeter = startMeterText.toDoubleOrNull()
                        val endMeter = endMeterText.toDoubleOrNull()
                        var kmDriven: Double? = null

                        if (startMeterText.isNotBlank() || endMeterText.isNotBlank()) {
                            if (startMeter == null) {
                                validationError = "Please enter a valid start meter reading"
                                return@Button
                            }
                            if (endMeter == null) {
                                validationError = "Please enter a valid end meter reading"
                                return@Button
                            }
                            if (endMeter < startMeter) {
                                validationError = "End meter must be greater than or equal to start meter"
                                return@Button
                            }
                            kmDriven = endMeter - startMeter
                        }

                        val notes = notesText.ifBlank { null }
                        val driverPay = driverPayText.toDoubleOrNull()

                        driverViewModel.submitDailyLog(
                            carId = car.id,
                            carName = car.name,
                            date = selectedDate,
                            income = income,
                            expense = expense,
                            startMeter = startMeter,
                            endMeter = endMeter,
                            kmDriven = kmDriven,
                            source = sourceText.trim(),
                            destination = destinationText.trim(),
                            driverPay = driverPay,
                            notes = notes
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald500
                    )
                ) {
                    Text(
                        text = "Submit Log",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        LoadingOverlay(isLoading = isLoading)
    }
}
