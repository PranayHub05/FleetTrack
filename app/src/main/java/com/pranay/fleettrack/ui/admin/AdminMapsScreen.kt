package com.pranay.fleettrack.ui.admin

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pranay.fleettrack.data.FirestoreRepository
import com.pranay.fleettrack.model.LocationPoint
import com.pranay.fleettrack.ui.theme.*
import com.pranay.fleettrack.viewmodel.AdminViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMapsScreen(
    adminViewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val drivers by adminViewModel.drivers.collectAsStateWithLifecycle()

    var selectedDriverId by remember { mutableStateOf<String?>(null) }
    var selectedDate by remember { mutableStateOf(Date()) }
    var driverDropdownExpanded by remember { mutableStateOf(false) }
    var locationPoints by remember { mutableStateOf<List<LocationPoint>>(emptyList()) }
    var isLoadingPoints by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    // Fetch location data when driver or date changes
    LaunchedEffect(selectedDriverId, selectedDate) {
        val driverId = selectedDriverId
        if (driverId != null) {
            isLoadingPoints = true
            try {
                val points = withContext(Dispatchers.IO) {
                    FirestoreRepository.instance.getLocationHistory(driverId, selectedDate)
                }
                locationPoints = points
            } catch (e: Exception) {
                locationPoints = emptyList()
            } finally {
                isLoadingPoints = false
            }
        } else {
            locationPoints = emptyList()
        }
    }

    // MapView remembered to prevent recreation
    val mapView = remember {
        MapView(context).apply {
            Configuration.getInstance().userAgentValue = context.packageName
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(5.0)
            controller.setCenter(GeoPoint(20.5937, 78.9629))
        }
    }

    // Lifecycle handling
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    // Update map overlays when location points change
    LaunchedEffect(locationPoints) {
        mapView.overlays.clear()

        if (locationPoints.isNotEmpty()) {
            val geoPoints = locationPoints.map { GeoPoint(it.latitude, it.longitude) }

            // Draw polyline
            val polyline = Polyline().apply {
                setPoints(geoPoints)
                outlinePaint.color = Blue500.toArgb()
                outlinePaint.strokeWidth = 8f
            }
            mapView.overlays.add(polyline)

            // Start marker
            val startMarker = Marker(mapView).apply {
                position = geoPoints.first()
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Start"
                snippet = locationPoints.first().let {
                    val ts = it.timestamp.toDate()
                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(ts)
                }
            }
            mapView.overlays.add(startMarker)

            // End marker
            if (geoPoints.size > 1) {
                val endMarker = Marker(mapView).apply {
                    position = geoPoints.last()
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "End"
                    snippet = locationPoints.last().let {
                        val ts = it.timestamp.toDate()
                        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(ts)
                    }
                }
                mapView.overlays.add(endMarker)
            }

            // Zoom to fit route
            try {
                val boundingBox = BoundingBox.fromGeoPoints(geoPoints)
                mapView.zoomToBoundingBox(boundingBox.increaseByScale(1.3f), true)
            } catch (e: Exception) {
                mapView.controller.setZoom(13.0)
                mapView.controller.setCenter(geoPoints[geoPoints.size / 2])
            }
        } else {
            // Default: center on India
            mapView.controller.setZoom(5.0)
            mapView.controller.setCenter(GeoPoint(20.5937, 78.9629))
        }

        mapView.invalidate()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Live Map",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark
                )
            )
        },
        containerColor = SurfaceDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Controls Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Driver selector
                Box(modifier = Modifier.weight(1f)) {
                    FilterChip(
                        selected = selectedDriverId != null,
                        onClick = { driverDropdownExpanded = true },
                        label = {
                            Text(
                                text = if (selectedDriverId != null) {
                                    drivers.find { it.id == selectedDriverId }?.name ?: "Driver"
                                } else "Select Driver",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SurfaceCard,
                            labelColor = TextSecondary,
                            iconColor = TextMuted,
                            selectedContainerColor = Blue500.copy(alpha = 0.2f),
                            selectedLabelColor = Blue400,
                            selectedLeadingIconColor = Blue400
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = SurfaceCardLight,
                            selectedBorderColor = Blue500.copy(alpha = 0.5f),
                            enabled = true,
                            selected = selectedDriverId != null
                        )
                    )
                    DropdownMenu(
                        expanded = driverDropdownExpanded,
                        onDismissRequest = { driverDropdownExpanded = false },
                        containerColor = SurfaceCard
                    ) {
                        drivers.forEach { driver ->
                            DropdownMenuItem(
                                text = { Text(driver.name, color = TextPrimary) },
                                onClick = {
                                    selectedDriverId = driver.id
                                    driverDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Date selector
                val calendar = remember { Calendar.getInstance() }
                FilledTonalButton(
                    onClick = {
                        calendar.time = selectedDate
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                calendar.set(year, month, dayOfMonth)
                                selectedDate = calendar.time
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = SurfaceCard,
                        contentColor = TextSecondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = "Select date",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = dateFormat.format(selectedDate),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Status bar
            if (selectedDriverId != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLoadingPoints) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = Blue400
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Loading route…",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    } else {
                        Text(
                            text = if (locationPoints.isEmpty()) {
                                "No location data for this date"
                            } else {
                                "${locationPoints.size} points tracked"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (locationPoints.isEmpty()) Amber400 else Emerald400
                        )
                    }
                }
            }

            // Map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceCardLight)
                    .border(1.dp, SurfaceCardLight, RoundedCornerShape(16.dp))
            ) {
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
