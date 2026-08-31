package com.pranay.fleettrack.model

data class Car(
    val id: String = "",
    val name: String = "",
    val licensePlate: String = "",
    val averageMileage: Double = 0.0,
    val fuelPrice: Double = 0.0,
    val isActive: Boolean = true,
    val registrationExpiry: String = "",
    val insuranceExpiry: String = "",
    val fitnessExpiry: String = "",
    val permitExpiry: String = "",
    val chassisNumber: String = "",
    val engineNumber: String = "",
    val ownerName: String = "",
    val vehicleNotes: String = ""
)
