package com.habitmind

import androidx.compose.runtime.Composable
import com.habitmind.navigation.HabitMindNavHost
import com.habitmind.navigation.rememberHabitMindNavController

@Composable
fun HabitMindApp() {
    PermissionGate {
        val navController = rememberHabitMindNavController()
        HabitMindNavHost(navController = navController)
    }
}
