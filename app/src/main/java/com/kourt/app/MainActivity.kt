package com.kourt.app

import android.content.res.Configuration
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
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyLocale(viewModel.language)
        enableEdgeToEdge()
        setContent {
            KourtTheme(darkTheme = viewModel.isDarkTheme) {
                Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                    NavGraph(
                        startDestination = viewModel.startDestination,
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

    private fun applyLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
