package com.pranay.fleettrack.model

data class Driver(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val licenseNumber: String = "",
    val assignedCarIds: List<String> = emptyList(),
    val isFixedToCar: Boolean = false,
    val isActive: Boolean = true,
    val userId: String? = null,
    val loginCode: String? = null
)
