package com.pranay.fleettrack.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pranay.fleettrack.model.Car
import com.pranay.fleettrack.model.Driver
import com.pranay.fleettrack.ui.theme.*
import java.util.UUID
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDriverDialog(
    driver: Driver? = null,
    cars: List<Car>,
    onSave: (Driver) -> Unit,
    onDismiss: () -> Unit
) {
    val isEditing = driver != null

    var name by remember { mutableStateOf(driver?.name ?: "") }
    var phone by remember { mutableStateOf(driver?.phone ?: "") }
    var licenseNumber by remember { mutableStateOf(driver?.licenseNumber ?: "") }
    var selectedCarIds by remember { mutableStateOf(driver?.assignedCarIds ?: emptyList<String>()) }
    var isFixedToCar by remember { mutableStateOf(driver?.isFixedToCar ?: false) }
    var isActive by remember { mutableStateOf(driver?.isActive ?: true) }

    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var licenseError by remember { mutableStateOf(false) }

    var carDropdownExpanded by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                    text = if (isEditing) "Edit Driver" else "Add Driver",
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
            // Name
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = false
                },
                label = { Text("Name *") },
                isError = nameError,
                supportingText = if (nameError) {
                    { Text("Name is required") }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Blue500,
                    unfocusedBorderColor = SurfaceCardLight,
                    focusedLabelColor = Blue400,
                    unfocusedLabelColor = TextMuted,
                    cursorColor = Blue500,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )

            // Phone
            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    phoneError = false
                },
                label = { Text("Phone *") },
                isError = phoneError,
                supportingText = if (phoneError) {
                    { Text("Phone is required") }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Blue500,
                    unfocusedBorderColor = SurfaceCardLight,
                    focusedLabelColor = Blue400,
                    unfocusedLabelColor = TextMuted,
                    cursorColor = Blue500,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )

            // License Number
            OutlinedTextField(
                value = licenseNumber,
                onValueChange = {
                    licenseNumber = it
                    licenseError = false
                },
                label = { Text("License Number *") },
                isError = licenseError,
                supportingText = if (licenseError) {
                    { Text("License number is required") }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Blue500,
                    unfocusedBorderColor = SurfaceCardLight,
                    focusedLabelColor = Blue400,
                    unfocusedLabelColor = TextMuted,
                    cursorColor = Blue500,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )

            // Assigned Car Dropdown
            ExposedDropdownMenuBox(
                expanded = carDropdownExpanded,
                onExpandedChange = { carDropdownExpanded = it }
            ) {
                val selectedCarText = when {
                    selectedCarIds.isEmpty() -> "None"
                    selectedCarIds.size == 1 -> cars.find { it.id == selectedCarIds.first() }?.name ?: "1 car selected"
                    else -> "${selectedCarIds.size} cars selected"
                }
                OutlinedTextField(
                    value = selectedCarText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Assigned Cars") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = carDropdownExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Blue500,
                        unfocusedBorderColor = SurfaceCardLight,
                        focusedLabelColor = Blue400,
                        unfocusedLabelColor = TextMuted,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                ExposedDropdownMenu(
                    expanded = carDropdownExpanded,
                    onDismissRequest = { carDropdownExpanded = false },
                    containerColor = SurfaceCard
                ) {
                    cars.filter { it.isActive }.forEach { car ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "${car.name} (${car.licensePlate})",
                                    color = TextPrimary
                                )
                            },
                            trailingIcon = {
                                if (selectedCarIds.contains(car.id)) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Selected",
                                        tint = Blue400
                                    )
                                }
                            },
                            onClick = {
                                val newIds = selectedCarIds.toMutableList()
                                if (newIds.contains(car.id)) {
                                    newIds.remove(car.id)
                                } else {
                                    newIds.add(car.id)
                                }
                                selectedCarIds = newIds
                            }
                        )
                    }
                }
            }

            // Fixed to Car checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = isFixedToCar,
                    onCheckedChange = { isFixedToCar = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Blue500,
                        uncheckedColor = TextMuted,
                        checkmarkColor = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Fix this driver to selected car",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // Active toggle
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
                    phoneError = phone.isBlank()
                    licenseError = licenseNumber.isBlank()

                    if (!nameError && !phoneError && !licenseError) {
                        val savedDriver = Driver(
                            id = driver?.id ?: UUID.randomUUID().toString(),
                            name = name.trim(),
                            phone = phone.trim(),
                            licenseNumber = licenseNumber.trim(),
                            assignedCarIds = selectedCarIds,
                            isFixedToCar = isFixedToCar,
                            isActive = isActive,
                            userId = driver?.userId,
                            loginCode = driver?.loginCode
                        )
                        onSave(savedDriver)
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
                    text = if (isEditing) "Update Driver" else "Add Driver",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
