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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kinet.service.StepTrackingService
import com.example.kinet.ui.AppTab
import com.example.kinet.ui.MainViewModel
import com.example.kinet.ui.MainViewModelFactory
import com.example.kinet.ui.calibration.CalibrationScreen
import com.example.kinet.ui.calibration.CalibrationViewModelFactory
import com.example.kinet.ui.dashboard.DashboardScreen
import com.example.kinet.ui.dashboard.DashboardViewModelFactory
import com.example.kinet.ui.habito.HabitoScreen
import com.example.kinet.ui.habito.HabitoSubScreen
import com.example.kinet.ui.habito.HabitoViewModel
import com.example.kinet.ui.habito.HabitoViewModelFactory
import com.example.kinet.ui.home.HomeScreen
import com.example.kinet.ui.profile.ProfileEditScreen
import com.example.kinet.ui.profile.ProfileSetupScreen
import com.example.kinet.ui.profile.ProfileViewScreen
import com.example.kinet.ui.reports.ReportsScreen
import com.example.kinet.ui.reports.ReportsViewModelFactory
import com.example.kinet.ui.theme.KinetTheme

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        startStepTrackingService()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestRequiredPermissions()
        setContent {
            val mainViewModel: MainViewModel = viewModel(
                factory = MainViewModelFactory(applicationContext)
            )
            val habitoViewModel: HabitoViewModel = viewModel(
                factory = HabitoViewModelFactory(applicationContext)
            )

            val isProfileSet by mainViewModel.isProfileSet.collectAsState()
            val showProfile by mainViewModel.showProfile.collectAsState()
            val showProfileEdit by mainViewModel.showProfileEdit.collectAsState()
            val showCalibration by mainViewModel.showCalibration.collectAsState()
            val userProfile by mainViewModel.userProfile.collectAsState()
            val currentTab by mainViewModel.currentTab.collectAsState()
            val appTheme by mainViewModel.appTheme.collectAsState()
            val currentStreak by mainViewModel.currentStreak.collectAsState()
            val bestStreak by mainViewModel.bestStreak.collectAsState()
            val habitoSubScreen by habitoViewModel.subScreen.collectAsState()

            KinetTheme(appTheme = appTheme) {
                when (isProfileSet) {
                    null -> Box(modifier = Modifier.fillMaxSize()) // loading splash
                    false -> ProfileSetupScreen(
                        onSave = { name, h, w, goal ->
                            mainViewModel.saveProfile(name, h, w, h * 0.415f, goal)
                        }
                    )
                    true -> when {
                        showProfileEdit -> ProfileEditScreen(
                            current = userProfile,
                            onSave = { name, h, w, s, goal -> mainViewModel.saveProfile(name, h, w, s, goal) },
                            onCancel = { mainViewModel.closeProfileEdit() }
                        )
                        showProfile -> ProfileViewScreen(
                            profile = userProfile,
                            appTheme = appTheme,
                            currentStreak = currentStreak,
                            bestStreak = bestStreak,
                            onThemeChange = { mainViewModel.setTheme(it) },
                            onEditProfile = { mainViewModel.openProfileEdit() },
                            onBack = { mainViewModel.closeProfile() },
                            onImagePick = { uri -> mainViewModel.saveProfileImage(uri) }
                        )
                        showCalibration -> CalibrationScreen(
                            viewModel = viewModel(
                                factory = CalibrationViewModelFactory(applicationContext)
                            ),
                            onDone = { mainViewModel.closeCalibration() },
                            onBack = { mainViewModel.closeCalibration() }
                        )
                        else -> Scaffold(
                            topBar = {
                                // Habito tab owns its own header; hide the generic TopAppBar
                                if (currentTab != AppTab.HABITO) {
                                    TopAppBar(
                                        title = { Text(currentTab.label) },
                                        actions = {
                                            IconButton(onClick = { mainViewModel.openProfile() }) {
                                                ProfileAvatar(imageUri = userProfile.profileImageUri)
                                            }
                                        }
                                    )
                                }
                            },
                            bottomBar = {
                                NavigationBar {
                                    AppTab.entries.forEach { tab ->
                                        NavigationBarItem(
                                            selected = currentTab == tab,
                                            onClick = { mainViewModel.setTab(tab) },
                                            icon = {
                                                Icon(
                                                    imageVector = tab.icon,
                                                    contentDescription = tab.label
                                                )
                                            },
                                            label = { Text(tab.label) }
                                        )
                                    }
                                }
                            },
                            floatingActionButton = {
                                if (currentTab == AppTab.HABITO &&
                                    habitoSubScreen == HabitoSubScreen.LIST
                                ) {
                                    FloatingActionButton(
                                        onClick = {
                                            habitoViewModel.navigateTo(HabitoSubScreen.ADD_EDIT)
                                        }
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = "Add Habit")
                                    }
                                }
                            }
                        ) { innerPadding ->
                            when (currentTab) {
                                AppTab.HOME -> HomeScreen(
                                    modifier = Modifier.padding(innerPadding)
                                )
                                AppTab.STEPS -> DashboardScreen(
                                    viewModel = viewModel(
                                        factory = DashboardViewModelFactory(applicationContext)
                                    ),
                                    onCalibrate = { mainViewModel.openCalibration() },
                                    modifier = Modifier.padding(innerPadding)
                                )
                                AppTab.HABITO -> HabitoScreen(
                                    viewModel = habitoViewModel,
                                    userName = userProfile.name,
                                    profileImageUri = userProfile.profileImageUri,
                                    onOpenProfile = { mainViewModel.openProfile() },
                                    modifier = Modifier.padding(innerPadding)
                                )
                                AppTab.REPORTS -> ReportsScreen(
                                    viewModel = viewModel(
                                        factory = ReportsViewModelFactory(applicationContext)
                                    ),
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }
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

@Composable
private fun ProfileAvatar(imageUri: String?) {
    val context = LocalContext.current
    if (imageUri != null) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUri)
                .crossfade(true)
                .build(),
            contentDescription = "Profile picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.Transparent)
        )
    } else {
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = "Profile"
        )
    }
}
