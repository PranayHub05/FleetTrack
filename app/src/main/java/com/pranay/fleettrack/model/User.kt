package com.pranay.fleettrack.model

enum class UserRole {
    ADMIN, DRIVER
}

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: UserRole = UserRole.DRIVER,
    val driverId: String? = null
)
