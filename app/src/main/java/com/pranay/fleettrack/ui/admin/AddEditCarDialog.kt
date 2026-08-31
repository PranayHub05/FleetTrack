package com.pranay.fleettrack.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pranay.fleettrack.model.Car
import com.pranay.fleettrack.ui.theme.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCarDialog(
    car: Car? = null,
    onSave: (Car) -> Unit,
    onDismiss: () -> Unit
) {
    val isEditing = car != null

    var name by remember { mutableStateOf(car?.name ?: "") }
    var licensePlate by remember { mutableStateOf(car?.licensePlate ?: "") }
    var averageMileage by remember { mutableStateOf(car?.averageMileage?.toString() ?: "") }
    var fuelPrice by remember { mutableStateOf(car?.fuelPrice?.toString() ?: "") }
    var isActive by remember { mutableStateOf(car?.isActive ?: true) }

    // New fields
    var chassisNumber by remember { mutableStateOf(car?.chassisNumber ?: "") }
    var engineNumber by remember { mutableStateOf(car?.engineNumber ?: "") }
    var ownerName by remember { mutableStateOf(car?.ownerName ?: "") }
    var registrationExpiry by remember { mutableStateOf(car?.registrationExpiry ?: "") }
    var insuranceExpiry by remember { mutableStateOf(car?.insuranceExpiry ?: "") }
    var fitnessExpiry by remember { mutableStateOf(car?.fitnessExpiry ?: "") }
    var permitExpiry by remember { mutableStateOf(car?.permitExpiry ?: "") }
    var vehicleNotes by remember { mutableStateOf(car?.vehicleNotes ?: "") }

    var nameError by remember { mutableStateOf(false) }
    var licensePlateError by remember { mutableStateOf(false) }
    var mileageError by remember { mutableStateOf(false) }
    var fuelPriceError by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Blue500,
        unfocusedBorderColor = SurfaceCardLight,
        focusedLabelColor = Blue400,
        unfocusedLabelColor = TextMuted,
        cursorColor = Blue500,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.size(width = 40.dp, height = 4.dp),
                    shape = RoundedCornerShape(2.dp),
                    color = TextMuted
                ) {}
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isEditing) "Edit Car" else "Add Car",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Car Name
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = false
                },
                label = { Text("Car Name *") },
                isError = nameError,
                supportingText = if (nameError) {
                    { Text("Car name is required") }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true
            )

            // License Plate
            OutlinedTextField(
                value = licensePlate,
                onValueChange = {
                    licensePlate = it
                    licensePlateError = false
                },
                label = { Text("License Plate *") },
                isError = licensePlateError,
                supportingText = if (licensePlateError) {
                    { Text("License plate is required") }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true
            )

            // Average Mileage
            OutlinedTextField(
                value = averageMileage,
                onValueChange = {
                    averageMileage = it
                    mileageError = false
                },
                label = { Text("Average Mileage (km/l)") },
                isError = mileageError,
                supportingText = if (mileageError) {
                    { Text("Enter a valid number") }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            // Fuel Price
            OutlinedTextField(
                value = fuelPrice,
                onValueChange = {
                    fuelPrice = it
                    fuelPriceError = false
                },
                label = { Text("Fuel Price (₹/litre)") },
                isError = fuelPriceError,
                supportingText = if (fuelPriceError) {
                    { Text("Enter a valid number") }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            // ── Vehicle Details Section ──────────────────────────────────
            SectionHeader(title = "Vehicle Details")

            // Chassis Number
            OutlinedTextField(
                value = chassisNumber,
                onValueChange = { chassisNumber = it },
                label = { Text("Chassis Number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true
            )

            // Engine Number
            OutlinedTextField(
                value = engineNumber,
                onValueChange = { engineNumber = it },
                label = { Text("Engine Number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true
            )

            // Owner Name
            OutlinedTextField(
                value = ownerName,
                onValueChange = { ownerName = it },
                label = { Text("Owner Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true
            )

            // ── Expiry Dates Section ─────────────────────────────────────
            SectionHeader(title = "Expiry Dates")

            // Registration Expiry
            OutlinedTextField(
                value = registrationExpiry,
                onValueChange = { registrationExpiry = it },
                label = { Text("Registration Expiry") },
                placeholder = { Text("DD/MM/YYYY", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true
            )

            // Insurance Expiry
            OutlinedTextField(
                value = insuranceExpiry,
                onValueChange = { insuranceExpiry = it },
                label = { Text("Insurance Expiry") },
                placeholder = { Text("DD/MM/YYYY", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true
            )

            // Fitness Expiry
            OutlinedTextField(
                value = fitnessExpiry,
                onValueChange = { fitnessExpiry = it },
                label = { Text("Fitness Expiry") },
                placeholder = { Text("DD/MM/YYYY", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true
            )

            // Permit Expiry
            OutlinedTextField(
                value = permitExpiry,
                onValueChange = { permitExpiry = it },
                label = { Text("Permit Expiry") },
                placeholder = { Text("DD/MM/YYYY", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true
            )

            // ── Vehicle Notes ────────────────────────────────────────────
            OutlinedTextField(
                value = vehicleNotes,
                onValueChange = { vehicleNotes = it },
                label = { Text("Vehicle Notes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                maxLines = 5,
                singleLine = false
            )

            // Active toggle (only when editing)
            if (isEditing) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Active",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextPrimary,
                            checkedTrackColor = Blue500,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = SurfaceCardLight
                        )
                    )
                }
            }

            // Save Button
            Button(
                onClick = {
                    nameError = name.isBlank()
                    licensePlateError = licensePlate.isBlank()
                    mileageError = averageMileage.isNotBlank() && averageMileage.toDoubleOrNull() == null
                    fuelPriceError = fuelPrice.isNotBlank() && fuelPrice.toDoubleOrNull() == null

                    if (!nameError && !licensePlateError && !mileageError && !fuelPriceError) {
                        val savedCar = Car(
                            id = car?.id ?: UUID.randomUUID().toString(),
                            name = name.trim(),
                            licensePlate = licensePlate.trim(),
                            averageMileage = averageMileage.toDoubleOrNull() ?: 0.0,
                            fuelPrice = fuelPrice.toDoubleOrNull() ?: 0.0,
                            isActive = isActive,
                            chassisNumber = chassisNumber.trim(),
                            engineNumber = engineNumber.trim(),
                            ownerName = ownerName.trim(),
                            registrationExpiry = registrationExpiry.trim(),
                            insuranceExpiry = insuranceExpiry.trim(),
                            fitnessExpiry = fitnessExpiry.trim(),
                            permitExpiry = permitExpiry.trim(),
                            vehicleNotes = vehicleNotes.trim()
                        )
                        onSave(savedCar)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blue500,
                    contentColor = TextPrimary
                )
            ) {
                Text(
                    text = if (isEditing) "Update Car" else "Add Car",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Column {
        HorizontalDivider(color = SurfaceCardLight, thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = Blue400
        )
    }
}
