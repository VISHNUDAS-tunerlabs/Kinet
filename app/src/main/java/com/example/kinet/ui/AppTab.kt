package com.example.kinet.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppTab(
    val label: String,
    val icon: ImageVector
) {
    HOME("Home", Icons.Filled.Home),
    STEPS("Steps", Icons.Filled.DirectionsWalk),
    HABITO("Habito", Icons.Filled.Checklist),
    REPORTS("Reports", Icons.Filled.BarChart)
}
