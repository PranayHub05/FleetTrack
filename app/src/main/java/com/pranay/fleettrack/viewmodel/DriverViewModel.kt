package com.pranay.fleettrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.pranay.fleettrack.data.FirestoreRepository
import com.pranay.fleettrack.model.Car
import com.pranay.fleettrack.model.DailyLog
import com.pranay.fleettrack.model.Driver
import com.pranay.fleettrack.model.LocationPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

class DriverViewModel(
    private val firestoreRepository: FirestoreRepository,
    private val driverId: String
) : ViewModel() {

    private val _driver = MutableStateFlow<Driver?>(null)
    val driver: StateFlow<Driver?> = _driver.asStateFlow()

    private val _assignedCars = MutableStateFlow<List<Car>>(emptyList())
    val assignedCars: StateFlow<List<Car>> = _assignedCars.asStateFlow()

    private val _availableCars = MutableStateFlow<List<Car>>(emptyList())
    val availableCars: StateFlow<List<Car>> = _availableCars.asStateFlow()

    private val _recentLogs = MutableStateFlow<List<DailyLog>>(emptyList())
    val recentLogs: StateFlow<List<DailyLog>> = _recentLogs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _logSubmitted = MutableStateFlow(false)
    val logSubmitted: StateFlow<Boolean> = _logSubmitted.asStateFlow()

    private val _isOnDuty = MutableStateFlow(false)
    val isOnDuty: StateFlow<Boolean> = _isOnDuty.asStateFlow()

    init {
        loadDriverProfile()
        loadAvailableCars()
        loadDriverLogs()
    }

    private fun loadDriverProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val driverObj = firestoreRepository.getDriverById(driverId)
                _driver.value = driverObj
                driverObj?.assignedCarIds?.let { carIds ->
                    if (carIds.isNotEmpty()) {
                        loadAssignedCars(carIds)
                    } else {
                        _assignedCars.value = emptyList()
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadAssignedCars(carIds: List<String>) {
        viewModelScope.launch {
            try {
                // Fetch all assigned cars
                val cars = carIds.mapNotNull { firestoreRepository.getCarById(it) }
                _assignedCars.value = cars
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load assigned cars: ${e.message}"
            }
        }
    }

    private fun loadAvailableCars() {
        viewModelScope.launch {
            firestoreRepository.getCarsFlow()
                .catch { e ->
                    _errorMessage.value = "Failed to load cars: ${e.message}"
                }
                .collect { cars ->
                    _availableCars.value = cars.filter { it.isActive }
                }
        }
    }

    private fun loadDriverLogs() {
        viewModelScope.launch {
            firestoreRepository.getLogsByDriverFlow(driverId)
                .catch { e ->
                    _errorMessage.value = "Failed to load logs: ${e.message}"
                }
                .collect { logs ->
                    _recentLogs.value = logs.sortedByDescending { it.date.toDate().time }
                }
        }
    }

    fun submitDailyLog(
        carId: String,
        carName: String,
        date: Date,
        income: Double,
        expense: Double,
        startMeter: Double?,
        endMeter: Double?,
        kmDriven: Double?,
        source: String,
        destination: String,
        driverPay: Double?,
        notes: String?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val currentDriver = _driver.value
                val log = DailyLog(
                    id = UUID.randomUUID().toString(),
                    driverId = driverId,
                    driverName = currentDriver?.name ?: "",
                    carId = carId,
                    carName = carName,
                    date = Timestamp(date),
                    income = income,
                    expense = expense,
                    startMeter = startMeter,
                    endMeter = endMeter,
                    kmDriven = kmDriven,
                    source = source,
                    destination = destination,
                    driverPay = driverPay,
                    notes = notes,
                    createdAt = Timestamp.now()
                )
                firestoreRepository.addDailyLog(log)
                _logSubmitted.value = true
            } catch (e: Exception) {
                _errorMessage.value = "Failed to submit log: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setOnDuty(onDuty: Boolean) {
        _isOnDuty.value = onDuty
    }

    fun resetLogSubmitted() {
        _logSubmitted.value = false
    }

    fun refreshData() {
        loadDriverProfile()
        loadAvailableCars()
        loadDriverLogs()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun getDriverId(): String = driverId

    companion object {
        fun Factory(driverId: String): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DriverViewModel(
                        firestoreRepository = FirestoreRepository.instance,
                        driverId = driverId
                    ) as T
                }
            }
        }
    }
}
