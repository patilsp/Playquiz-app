package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import com.example.PlayQuizApp.PlayQuizApp
import com.example.data.remote.configureAppCheck
import com.example.ui.theme.PlayQuizTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // STEP 1: App Check contract requirement - must configure App Check with intent before any UI or Firebase operation
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            PlayQuizTheme {
                Surface {
                    PlayQuizApp()
                }
            }
        }
    }
}
