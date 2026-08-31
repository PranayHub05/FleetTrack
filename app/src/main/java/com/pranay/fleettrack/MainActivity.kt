package com.pranay.fleettrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.pranay.fleettrack.navigation.FleetTrackNavGraph
import com.pranay.fleettrack.ui.theme.FleetTrackTheme

import com.pranay.fleettrack.data.AuthRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthRepository.instance.init(this)
        enableEdgeToEdge()
        setContent {
            FleetTrackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FleetTrackNavGraph()
                }
            }
        }
    }
}
