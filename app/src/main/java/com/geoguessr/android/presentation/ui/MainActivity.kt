package com.geoguessr.android.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.geoguessr.android.domain.repository.AuthRepository
import com.geoguessr.android.presentation.navigation.NavGraph
import com.geoguessr.android.presentation.navigation.Screen
import com.geoguessr.android.presentation.theme.GeoGuessrTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoGuessrTheme {
                val startDestination = if (authRepository.isLoggedIn()) {
                    Screen.Lobby.route
                } else {
                    Screen.Login.route
                }
                NavGraph(startDestination = startDestination)
            }
        }
    }
}
