package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.RoutineRepository
import com.example.notifications.NotificationHelper
import com.example.ui.RoutineViewModel
import com.example.ui.screens.WeeklyRoutineScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channel for routine reminders
        NotificationHelper.createNotificationChannel(applicationContext)

        // Initialize Room Database & Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = RoutineRepository(database.routineDao(), database.progressSnapshotDao())

        val viewModelFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return RoutineViewModel(repository) as T
            }
        }

        setContent {
            MyApplicationTheme {
                val routineViewModel: RoutineViewModel = viewModel(factory = viewModelFactory)
                WeeklyRoutineScreen(viewModel = routineViewModel)
            }
        }
    }
}
