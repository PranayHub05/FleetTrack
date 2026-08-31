package com.pranay.fleettrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pranay.fleettrack.data.FirestoreRepository
import com.pranay.fleettrack.model.Car
import com.pranay.fleettrack.model.DailyLog
import com.pranay.fleettrack.model.Driver
import com.pranay.fleettrack.util.getEndOfMonth
import com.pranay.fleettrack.util.getStartOfMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel(
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val _drivers = MutableStateFlow<List<Driver>>(emptyList())
    val drivers: StateFlow<List<Driver>> = _drivers.asStateFlow()

    private val _cars = MutableStateFlow<List<Car>>(emptyList())
    val cars: StateFlow<List<Car>> = _cars.asStateFlow()

    private val _dailyLogs = MutableStateFlow<List<DailyLog>>(emptyList())
    val dailyLogs: StateFlow<List<DailyLog>> = _dailyLogs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _aiSummary = MutableStateFlow<String?>(null)
    val aiSummary: StateFlow<String?> = _aiSummary.asStateFlow()

    private val _isGeneratingSummary = MutableStateFlow(false)
    val isGeneratingSummary: StateFlow<Boolean> = _isGeneratingSummary.asStateFlow()

    // Computed properties for current month
    val monthlyIncome: Double
        get() {
            val startSec = getStartOfMonth().seconds
            val endSec = getEndOfMonth().seconds
            return _dailyLogs.value
                .filter { it.date.seconds in startSec..endSec }
                .sumOf { it.income }
        }

    val monthlyExpense: Double
        get() {
            val startSec = getStartOfMonth().seconds
            val endSec = getEndOfMonth().seconds
            return _dailyLogs.value
                .filter { it.date.seconds in startSec..endSec }
                .sumOf { it.expense }
        }

    val monthlyProfit: Double
        get() = monthlyIncome - monthlyExpense

    init {
        viewModelScope.launch {
            firestoreRepository.getDriversFlow().collect { driverList ->
                _drivers.value = driverList
            }
        }

        viewModelScope.launch {
            firestoreRepository.getCarsFlow().collect { carList ->
                _cars.value = carList
            }
        }

        viewModelScope.launch {
            firestoreRepository.getDailyLogsFlow().collect { logList ->
                _dailyLogs.value = logList
            }
        }
    }

    fun addDriver(driver: Driver) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
                val code = (1..6).map { chars.random() }.joinToString("")
                val driverWithCode = driver.copy(loginCode = code)
                firestoreRepository.addDriver(driverWithCode)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to add driver"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateDriver(driver: Driver) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val existingDriver = drivers.value.find { it.id == driver.id }
                val code = if (!driver.loginCode.isNullOrBlank()) {
                    driver.loginCode
                } else if (existingDriver != null && !existingDriver.loginCode.isNullOrBlank()) {
                    existingDriver.loginCode
                } else {
                    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
                    (1..6).map { chars.random() }.joinToString("")
                }
                val driverWithCode = driver.copy(loginCode = code)
                firestoreRepository.updateDriver(driverWithCode)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to update driver"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addCar(car: Car) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                firestoreRepository.addCar(car)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to add car"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCar(car: Car) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                firestoreRepository.updateCar(car)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to update car"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getFilteredLogs(driverId: String?, carId: String?): List<DailyLog> {
        return _dailyLogs.value.filter { log ->
            (driverId == null || log.driverId == driverId) &&
                    (carId == null || log.carId == carId)
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun deleteDriver(id: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                firestoreRepository.deleteDriver(id)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to delete driver"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCar(id: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                firestoreRepository.deleteCar(id)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to delete car"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            // Data is collected via flows, so we just add a small delay for visual feedback
            kotlinx.coroutines.delay(500)
            _isLoading.value = false
        }
    }

    fun generateAISummary(monthlyIncome: Double, monthlyExpense: Double, monthlyProfit: Double) {
        viewModelScope.launch {
            _isGeneratingSummary.value = true
            try {
                val apiKey = "AIzaSyCIZ-vT-vuPDTCcS1YwiGPPjkrPfw0iKh8"
                val generativeModel = com.google.ai.client.generativeai.GenerativeModel(
                    modelName = "gemini-3.5-flash",
                    apiKey = apiKey
                )
                
                val logs = _dailyLogs.value.take(10)
                var prompt = "You are a helpful AI assistant for a fleet management app called FleetTrack.\n" +
                             "Generate a short (3-4 sentences) summary and actionable insight for the admin based on this data:\n" +
                             "Current Month Income: $monthlyIncome\n" +
                             "Current Month Expense: $monthlyExpense\n" +
                             "Current Month Profit: $monthlyProfit\n\n" +
                             "Recent Logs:\n"
                logs.forEach { log ->
                    prompt += "- Driver: ${log.driverName}, Car: ${log.carName}, Income: ${log.income}, Expense: ${log.expense}, Date: ${log.date.toDate()}\n"
                }
                
                val response = generativeModel.generateContent(prompt)
                _aiSummary.value = response.text
            } catch (e: Exception) {
                _aiSummary.value = "Failed to generate summary: ${e.message}"
            } finally {
                _isGeneratingSummary.value = false
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AdminViewModel(FirestoreRepository.instance) as T
            }
        }
    }
}
