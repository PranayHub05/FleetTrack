package com.pranay.fleettrack.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pranay.fleettrack.model.Car
import com.pranay.fleettrack.model.DailyLog
import com.pranay.fleettrack.model.Driver
import com.pranay.fleettrack.model.LocationPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

class FirestoreRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // ── Driver operations ────────────────────────────────────────────────

    suspend fun addDriver(driver: Driver): String {
        val docRef = firestore.collection("drivers").document()
        val driverWithId = driver.copy(id = docRef.id)
        docRef.set(driverToMap(driverWithId)).await()
        return docRef.id
    }

    suspend fun updateDriver(driver: Driver) {
        firestore.collection("drivers").document(driver.id)
            .set(driverToMap(driver)).await()
    }

    suspend fun getDrivers(): List<Driver> {
        val snapshot = firestore.collection("drivers").get().await()
        return snapshot.documents.mapNotNull { it.toDriver() }
    }

    fun getDriversFlow(): Flow<List<Driver>> = callbackFlow {
        val listener = firestore.collection("drivers")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val drivers = snapshot?.documents?.mapNotNull { it.toDriver() } ?: emptyList()
                trySend(drivers)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getDriverById(id: String): Driver? {
        val document = firestore.collection("drivers").document(id).get().await()
        return document.toDriver()
    }

    suspend fun deleteDriver(id: String) {
        firestore.collection("drivers").document(id).delete().await()
    }

    // ── Car operations ───────────────────────────────────────────────────

    suspend fun addCar(car: Car): String {
        val docRef = firestore.collection("cars").document()
        val carWithId = car.copy(id = docRef.id)
        docRef.set(carToMap(carWithId)).await()
        return docRef.id
    }

    suspend fun updateCar(car: Car) {
        firestore.collection("cars").document(car.id)
            .set(carToMap(car)).await()
    }

    suspend fun getCars(): List<Car> {
        val snapshot = firestore.collection("cars").get().await()
        return snapshot.documents.mapNotNull { it.toCarModel() }
    }

    fun getCarsFlow(): Flow<List<Car>> = callbackFlow {
        val listener = firestore.collection("cars")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val cars = snapshot?.documents?.mapNotNull { it.toCarModel() } ?: emptyList()
                trySend(cars)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getCarById(id: String): Car? {
        val document = firestore.collection("cars").document(id).get().await()
        return document.toCarModel()
    }

    suspend fun deleteCar(id: String) {
        firestore.collection("cars").document(id).delete().await()
    }

    // ── DailyLog operations ──────────────────────────────────────────────

    suspend fun addDailyLog(log: DailyLog): String {
        val docRef = firestore.collection("dailyLogs").document()
        val logWithId = log.copy(id = docRef.id)
        docRef.set(dailyLogToMap(logWithId)).await()
        return docRef.id
    }

    suspend fun getDailyLogs(): List<DailyLog> {
        val snapshot = firestore.collection("dailyLogs")
            .orderBy("date", Query.Direction.DESCENDING)
            .get().await()
        return snapshot.documents.mapNotNull { it.toDailyLog() }
    }

    fun getDailyLogsFlow(): Flow<List<DailyLog>> = callbackFlow {
        val listener = firestore.collection("dailyLogs")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val logs = snapshot?.documents?.mapNotNull { it.toDailyLog() } ?: emptyList()
                trySend(logs)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getLogsByDriver(driverId: String): List<DailyLog> {
        val snapshot = firestore.collection("dailyLogs")
            .whereEqualTo("driverId", driverId)
            .orderBy("date", Query.Direction.DESCENDING)
            .get().await()
        return snapshot.documents.mapNotNull { it.toDailyLog() }
    }

    suspend fun getLogsByDateRange(startDate: Timestamp, endDate: Timestamp): List<DailyLog> {
        val snapshot = firestore.collection("dailyLogs")
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .orderBy("date", Query.Direction.DESCENDING)
            .get().await()
        return snapshot.documents.mapNotNull { it.toDailyLog() }
    }

    fun getLogsByDriverFlow(driverId: String): Flow<List<DailyLog>> = callbackFlow {
        val listener = firestore.collection("dailyLogs")
            .whereEqualTo("driverId", driverId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val logs = snapshot?.documents?.mapNotNull { it.toDailyLog() } ?: emptyList()
                trySend(logs)
            }
        awaitClose { listener.remove() }
    }

    // ── Location tracking operations ─────────────────────────────────────

    suspend fun addLocationPoint(driverId: String, point: LocationPoint) {
        val docRef = firestore.collection("drivers").document(driverId)
            .collection("locationHistory").document()
        val pointWithId = point.copy(id = docRef.id)
        docRef.set(locationPointToMap(pointWithId)).await()
    }

    fun getLocationHistoryFlow(driverId: String, date: Date): Flow<List<LocationPoint>> = callbackFlow {
        val cal = Calendar.getInstance().apply { time = date }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = Timestamp(cal.time)

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val endOfDay = Timestamp(cal.time)

        val listener = firestore.collection("drivers").document(driverId)
            .collection("locationHistory")
            .whereGreaterThanOrEqualTo("timestamp", startOfDay)
            .whereLessThanOrEqualTo("timestamp", endOfDay)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val points = snapshot?.documents?.mapNotNull { it.toLocationPoint() } ?: emptyList()
                trySend(points)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getLocationHistory(driverId: String, date: Date): List<LocationPoint> {
        val cal = Calendar.getInstance().apply { time = date }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = Timestamp(cal.time)

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val endOfDay = Timestamp(cal.time)

        val snapshot = firestore.collection("drivers").document(driverId)
            .collection("locationHistory")
            .whereGreaterThanOrEqualTo("timestamp", startOfDay)
            .whereLessThanOrEqualTo("timestamp", endOfDay)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get().await()
        return snapshot.documents.mapNotNull { it.toLocationPoint() }
    }

    // ── Document-to-model mapping extensions ─────────────────────────────

    private fun DocumentSnapshot.toDriver(): Driver? {
        if (!exists()) return null
        return try {
            val legacyAssignedCarId = getString("assignedCarId")
            @Suppress("UNCHECKED_CAST")
            val assignedCarIdsRaw = get("assignedCarIds") as? List<String>
            val assignedCarIds = assignedCarIdsRaw ?: (legacyAssignedCarId?.let { listOf(it) } ?: emptyList())

            Driver(
                id = id,
                name = getString("name") ?: "",
                phone = getString("phone") ?: "",
                licenseNumber = getString("licenseNumber") ?: "",
                assignedCarIds = assignedCarIds,
                isFixedToCar = getBoolean("isFixedToCar") ?: false,
                isActive = getBoolean("isActive") ?: true,
                userId = getString("userId"),
                loginCode = getString("loginCode")
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun DocumentSnapshot.toCarModel(): Car? {
        if (!exists()) return null
        return try {
            Car(
                id = id,
                name = getString("name") ?: "",
                licensePlate = getString("licensePlate") ?: "",
                averageMileage = getDouble("averageMileage") ?: 0.0,
                fuelPrice = getDouble("fuelPrice") ?: 0.0,
                isActive = getBoolean("isActive") ?: true,
                registrationExpiry = getString("registrationExpiry") ?: "",
                insuranceExpiry = getString("insuranceExpiry") ?: "",
                fitnessExpiry = getString("fitnessExpiry") ?: "",
                permitExpiry = getString("permitExpiry") ?: "",
                chassisNumber = getString("chassisNumber") ?: "",
                engineNumber = getString("engineNumber") ?: "",
                ownerName = getString("ownerName") ?: "",
                vehicleNotes = getString("vehicleNotes") ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun DocumentSnapshot.toDailyLog(): DailyLog? {
        if (!exists()) return null
        return try {
            DailyLog(
                id = id,
                driverId = getString("driverId") ?: "",
                driverName = getString("driverName") ?: "",
                carId = getString("carId") ?: "",
                carName = getString("carName") ?: "",
                date = getTimestamp("date") ?: Timestamp.now(),
                income = getDouble("income") ?: 0.0,
                expense = getDouble("expense") ?: 0.0,
                startMeter = getDouble("startMeter"),
                endMeter = getDouble("endMeter"),
                kmDriven = getDouble("kmDriven"),
                source = getString("source") ?: "",
                destination = getString("destination") ?: "",
                driverPay = getDouble("driverPay"),
                notes = getString("notes"),
                createdAt = getTimestamp("createdAt") ?: Timestamp.now()
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun DocumentSnapshot.toLocationPoint(): LocationPoint? {
        if (!exists()) return null
        return try {
            LocationPoint(
                id = id,
                latitude = getDouble("latitude") ?: 0.0,
                longitude = getDouble("longitude") ?: 0.0,
                timestamp = getTimestamp("timestamp") ?: Timestamp.now(),
                speed = getDouble("speed")?.toFloat() ?: 0f
            )
        } catch (e: Exception) {
            null
        }
    }

    // ── Model-to-map helpers ─────────────────────────────────────────────

    private fun driverToMap(driver: Driver): Map<String, Any?> {
        val map = mutableMapOf<String, Any>(
            "id" to driver.id,
            "name" to driver.name,
            "phone" to driver.phone,
            "licenseNumber" to driver.licenseNumber,
            "assignedCarIds" to driver.assignedCarIds,
            "isFixedToCar" to driver.isFixedToCar,
            "isActive" to driver.isActive
        )
        driver.userId?.let { map["userId"] = it }
        driver.loginCode?.let { map["loginCode"] = it }
        return map
    }

    private fun carToMap(car: Car): Map<String, Any?> = mapOf(
        "name" to car.name,
        "licensePlate" to car.licensePlate,
        "averageMileage" to car.averageMileage,
        "fuelPrice" to car.fuelPrice,
        "isActive" to car.isActive,
        "registrationExpiry" to car.registrationExpiry,
        "insuranceExpiry" to car.insuranceExpiry,
        "fitnessExpiry" to car.fitnessExpiry,
        "permitExpiry" to car.permitExpiry,
        "chassisNumber" to car.chassisNumber,
        "engineNumber" to car.engineNumber,
        "ownerName" to car.ownerName,
        "vehicleNotes" to car.vehicleNotes
    )

    private fun dailyLogToMap(log: DailyLog): Map<String, Any?> = mapOf(
        "driverId" to log.driverId,
        "driverName" to log.driverName,
        "carId" to log.carId,
        "carName" to log.carName,
        "date" to log.date,
        "income" to log.income,
        "expense" to log.expense,
        "startMeter" to log.startMeter,
        "endMeter" to log.endMeter,
        "kmDriven" to log.kmDriven,
        "source" to log.source,
        "destination" to log.destination,
        "driverPay" to log.driverPay,
        "notes" to log.notes,
        "createdAt" to log.createdAt
    )

    private fun locationPointToMap(point: LocationPoint): Map<String, Any?> = mapOf(
        "id" to point.id,
        "latitude" to point.latitude,
        "longitude" to point.longitude,
        "timestamp" to point.timestamp,
        "speed" to point.speed.toDouble()
    )

    companion object {
        val instance: FirestoreRepository by lazy { FirestoreRepository() }
    }
}
