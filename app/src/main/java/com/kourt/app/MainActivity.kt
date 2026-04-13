package com.kourt.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.kourt.app.navigation.NavGraph
import com.kourt.app.ui.theme.KourtTheme
import com.kourt.app.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KourtTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                    NavGraph(
                        startDestination = viewModel.startDestination,
                        paddingValues = paddingValues,
                    )
                }
            }
        }
    }
}
