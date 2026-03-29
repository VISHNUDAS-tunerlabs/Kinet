package com.example.kinet

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kinet.service.StepTrackingService
import com.example.kinet.ui.MainViewModel
import com.example.kinet.ui.MainViewModelFactory
import com.example.kinet.ui.dashboard.DashboardScreen
import com.example.kinet.ui.dashboard.DashboardViewModelFactory
import com.example.kinet.ui.profile.ProfileSetupScreen
import com.example.kinet.ui.theme.KinetTheme

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        startStepTrackingService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestRequiredPermissions()
        setContent {
            KinetTheme {
                val mainViewModel: MainViewModel = viewModel(
                    factory = MainViewModelFactory(applicationContext)
                )
                val isProfileSet by mainViewModel.isProfileSet.collectAsState()

                when (isProfileSet) {
                    null -> Box(modifier = Modifier.fillMaxSize()) // loading — blank while DB query runs
                    false -> ProfileSetupScreen(
                        onSave = { h, w, s -> mainViewModel.saveProfile(h, w, s) }
                    )
                    true -> Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        DashboardScreen(
                            viewModel = viewModel(
                                factory = DashboardViewModelFactory(applicationContext)
                            ),
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }

    private fun requestRequiredPermissions() {
        val permissions = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isEmpty()) {
            startStepTrackingService()
        } else {
            permissionLauncher.launch(notGranted.toTypedArray())
        }
    }

    private fun startStepTrackingService() {
        val intent = Intent(this, StepTrackingService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }
}
