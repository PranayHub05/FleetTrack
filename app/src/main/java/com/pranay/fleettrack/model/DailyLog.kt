package com.pranay.fleettrack.model

import com.google.firebase.Timestamp

data class DailyLog(
    val id: String = "",
    val driverId: String = "",
    val driverName: String = "",
    val carId: String = "",
    val carName: String = "",
    val date: Timestamp = Timestamp.now(),
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val startMeter: Double? = null,
    val endMeter: Double? = null,
    val kmDriven: Double? = null,
    val source: String = "",
    val destination: String = "",
    val driverPay: Double? = null,
    val notes: String? = null,
    val createdAt: Timestamp = Timestamp.now()
)
