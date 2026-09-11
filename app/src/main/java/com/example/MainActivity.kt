package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.data.AuthRepository
import com.example.data.DestinationRepository
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val authRepository = remember { AuthRepository(applicationContext) }
        val destinationRepository = remember { DestinationRepository(applicationContext) }

        Surface(modifier = Modifier.fillMaxSize()) {
          AppNavigation(
            authRepository = authRepository,
            destinationRepository = destinationRepository
          )
        }
      }
    }
  }
}
