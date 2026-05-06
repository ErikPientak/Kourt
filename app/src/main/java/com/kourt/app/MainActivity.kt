package com.kourt.app

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.kourt.app.navigation.NavGraph
import com.kourt.app.ui.theme.KourtTheme
import com.kourt.app.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            viewModel.startDestination.value == null
        }
        applyLocale(viewModel.language)
        enableEdgeToEdge()
        setContent {

            val startDest by viewModel.startDestination.collectAsState()

            KourtTheme(darkTheme = viewModel.isDarkTheme) {
                startDest?.let { destination ->
                    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                        NavGraph(
                            startDestination = destination,
                            paddingValues = paddingValues,
                            isDarkTheme = viewModel.isDarkTheme,
                            currentLanguage = viewModel.language,
                            onToggleTheme = { viewModel.toggleTheme() },
                            onSetLanguage = { code ->
                                viewModel.updateLanguage(code)
                                recreate()
                            },
                        )
                    }
              }
            }
        }
    }

    private fun applyLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
