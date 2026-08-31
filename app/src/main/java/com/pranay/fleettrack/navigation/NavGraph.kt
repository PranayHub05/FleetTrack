package com.pranay.fleettrack.navigation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pranay.fleettrack.model.UserRole
import com.pranay.fleettrack.service.LocationTrackingService
import com.pranay.fleettrack.ui.admin.AdminDashboardScreen
import com.pranay.fleettrack.ui.admin.AdminLedgerScreen
import com.pranay.fleettrack.ui.admin.AdminManageScreen
import com.pranay.fleettrack.ui.admin.AdminMapsScreen
import com.pranay.fleettrack.ui.admin.AdminStatsScreen
import com.pranay.fleettrack.ui.admin.AdminTripsScreen
import com.pranay.fleettrack.ui.auth.LoginScreen
import com.pranay.fleettrack.ui.auth.WelcomeScreen
import com.pranay.fleettrack.ui.driver.DriverHomeScreen
import com.pranay.fleettrack.ui.driver.DriverLogEntryScreen
import com.pranay.fleettrack.viewmodel.AdminViewModel
import com.pranay.fleettrack.viewmodel.AuthViewModel
import com.pranay.fleettrack.viewmodel.DriverViewModel

@Composable
fun FleetTrackNavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)

    val currentUser by authViewModel.currentUser.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    // Check for existing session
    LaunchedEffect(isLoggedIn, currentUser) {
        if (isLoggedIn && currentUser != null) {
            val user = currentUser!!
            when (user.role) {
                UserRole.ADMIN -> {
                    navController.navigate("admin_dashboard") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
                UserRole.DRIVER -> {
                    val driverId = user.driverId ?: return@LaunchedEffect
                    val driverName = user.displayName.ifBlank { "Driver" }
                    navController.navigate("driver_home/$driverId/$driverName") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "welcome"
    ) {
        // Welcome screen
        composable("welcome") {
            WelcomeScreen(
                onAdminLoginClick = {
                    navController.navigate("login/ADMIN")
                },
                onDriverLoginClick = {
                    navController.navigate("login/DRIVER")
                }
            )
        }

        // Login screen
        composable(
            route = "login/{role}",
            arguments = listOf(
                navArgument("role") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val roleStr = backStackEntry.arguments?.getString("role") ?: "DRIVER"
            val role = if (roleStr == "ADMIN") UserRole.ADMIN else UserRole.DRIVER
            LoginScreen(
                authViewModel = authViewModel,
                role = role,
                onLoginSuccess = { user ->
                    when (user.role) {
                        UserRole.ADMIN -> {
                            navController.navigate("admin_dashboard") {
                                popUpTo("welcome") { inclusive = true }
                            }
                        }
                        UserRole.DRIVER -> {
                            val driverId = user.driverId ?: return@LoginScreen
                            val driverName = user.displayName.ifBlank { "Driver" }
                            navController.navigate("driver_home/$driverId/$driverName") {
                                popUpTo("welcome") { inclusive = true }
                            }
                        }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Admin Dashboard
        composable("admin_dashboard") {
            val adminViewModel: AdminViewModel = viewModel(factory = AdminViewModel.Factory)
            AdminDashboardScreen(
                adminViewModel = adminViewModel,
                onNavigateToManage = {
                    navController.navigate("admin_manage")
                },
                onNavigateToLedger = {
                    navController.navigate("admin_ledger")
                },
                onNavigateToStats = {
                    navController.navigate("admin_stats")
                },
                onNavigateToMaps = {
                    navController.navigate("admin_maps")
                },
                onNavigateToTrips = {
                    navController.navigate("admin_trips")
                },
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate("welcome") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Admin Manage
        composable("admin_manage") {
            val adminViewModel: AdminViewModel = viewModel(factory = AdminViewModel.Factory)
            AdminManageScreen(
                adminViewModel = adminViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Admin Ledger
        composable("admin_ledger") {
            val adminViewModel: AdminViewModel = viewModel(factory = AdminViewModel.Factory)
            AdminLedgerScreen(
                adminViewModel = adminViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Admin Stats
        composable("admin_stats") {
            val adminViewModel: AdminViewModel = viewModel(factory = AdminViewModel.Factory)
            AdminStatsScreen(
                adminViewModel = adminViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Admin Maps
        composable("admin_maps") {
            val adminViewModel: AdminViewModel = viewModel(factory = AdminViewModel.Factory)
            AdminMapsScreen(
                adminViewModel = adminViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Admin Trips
        composable("admin_trips") {
            val adminViewModel: AdminViewModel = viewModel(factory = AdminViewModel.Factory)
            AdminTripsScreen(
                adminViewModel = adminViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Driver Home
        composable(
            route = "driver_home/{driverId}/{driverName}",
            arguments = listOf(
                navArgument("driverId") { type = NavType.StringType },
                navArgument("driverName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val driverId = backStackEntry.arguments?.getString("driverId") ?: return@composable
            val driverName = backStackEntry.arguments?.getString("driverName") ?: "Driver"
            val driverViewModel: DriverViewModel = viewModel(
                factory = DriverViewModel.Factory(driverId)
            )
            val context = LocalContext.current

            // Location permission launcher
            val locationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                if (fineLocationGranted) {
                    driverViewModel.setOnDuty(true)
                    val intent = Intent(context, LocationTrackingService::class.java).apply {
                        putExtra(LocationTrackingService.EXTRA_DRIVER_ID, driverId)
                    }
                    ContextCompat.startForegroundService(context, intent)
                    Toast.makeText(context, "Route recording started", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Location permission is required for route tracking", Toast.LENGTH_LONG).show()
                    driverViewModel.setOnDuty(false)
                }
            }

            DriverHomeScreen(
                driverViewModel = driverViewModel,
                driverName = driverName,
                onNavigateToLogEntry = {
                    navController.navigate("driver_log_entry/$driverId")
                },
                onSignOut = {
                    // Stop location service if running
                    val stopIntent = Intent(context, LocationTrackingService::class.java)
                    context.stopService(stopIntent)
                    driverViewModel.setOnDuty(false)

                    authViewModel.signOut()
                    navController.navigate("welcome") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onDutyToggle = { wantOnDuty ->
                    if (wantOnDuty) {
                        // Check and request permissions
                        val permissionsNeeded = mutableListOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        locationPermissionLauncher.launch(permissionsNeeded.toTypedArray())
                    } else {
                        // Stop the service
                        val stopIntent = Intent(context, LocationTrackingService::class.java)
                        context.stopService(stopIntent)
                        driverViewModel.setOnDuty(false)
                        Toast.makeText(context, "Route recording stopped", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // Driver Log Entry
        composable(
            route = "driver_log_entry/{driverId}",
            arguments = listOf(
                navArgument("driverId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val driverId = backStackEntry.arguments?.getString("driverId") ?: return@composable
            val driverViewModel: DriverViewModel = viewModel(
                factory = DriverViewModel.Factory(driverId)
            )
            DriverLogEntryScreen(
                driverViewModel = driverViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onLogSubmitted = {
                    navController.popBackStack()
                }
            )
        }
    }
}
