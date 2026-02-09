package com.habitmind

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.habitmind.navigation.HabitMindNavHost
import com.habitmind.navigation.rememberHabitMindNavController

@Composable
fun HabitMindApp() {
    val navController = rememberHabitMindNavController()
    
    HabitMindNavHost(navController = navController)
}
