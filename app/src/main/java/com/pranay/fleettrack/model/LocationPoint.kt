package com.pranay.fleettrack.model

import com.google.firebase.Timestamp

data class LocationPoint(
    val id: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Timestamp = Timestamp.now(),
    val speed: Float = 0f
)
