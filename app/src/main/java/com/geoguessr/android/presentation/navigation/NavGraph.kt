package com.geoguessr.android.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.geoguessr.android.presentation.screen.game.ClassicGameScreen
import com.geoguessr.android.presentation.screen.game.GameResultScreen
import com.geoguessr.android.presentation.screen.game.GameScreen
import com.geoguessr.android.presentation.screen.home.HomeScreen
import com.geoguessr.android.presentation.screen.leaderboard.LeaderboardScreen
import com.geoguessr.android.presentation.screen.login.LoginScreen
import com.geoguessr.android.presentation.screen.multiplayer.MultiplayerGameScreen
import com.geoguessr.android.presentation.screen.multiplayer.MultiplayerLobbyScreen
import com.geoguessr.android.presentation.screen.profile.ProfileScreen
import com.geoguessr.android.presentation.screen.register.RegisterScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object ClassicGame : Screen("classic_game/{gameType}") {
        fun createRoute(gameType: String = "CLASSIC") = "classic_game/$gameType"
    }
    object GameResult : Screen("game_result/{totalScore}") {
        fun createRoute(totalScore: Int) = "game_result/$totalScore"
    }
    object MultiplayerLobby : Screen("multiplayer_lobby")
    object MultiplayerGame : Screen("multiplayer_game/{roomId}") {
        fun createRoute(roomId: String) = "multiplayer_game/$roomId"
    }
    object Profile : Screen("profile")
    object Leaderboard : Screen("leaderboard")
    object Lobby : Screen("lobby")
    object Game : Screen("game/{roomId}") {
        fun createRoute(roomId: String) = "game/$roomId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onPlayClassic = {
                    navController.navigate(Screen.ClassicGame.createRoute("CLASSIC"))
                },
                onPlayMultiplayer = {
                    navController.navigate(Screen.MultiplayerLobby.route)
                },
                onPlayDaily = {
                    navController.navigate(Screen.ClassicGame.createRoute("DAILY"))
                },
                onOpenProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onOpenLeaderboard = {
                    navController.navigate(Screen.Leaderboard.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.ClassicGame.route,
            arguments = listOf(navArgument("gameType") { type = NavType.StringType })
        ) { backStackEntry ->
            val gameType = backStackEntry.arguments?.getString("gameType") ?: "CLASSIC"
            ClassicGameScreen(
                gameType = gameType,
                onGameFinished = { totalScore ->
                    navController.navigate(Screen.GameResult.createRoute(totalScore)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(
            route = Screen.GameResult.route,
            arguments = listOf(navArgument("totalScore") { type = NavType.IntType })
        ) { backStackEntry ->
            val totalScore = backStackEntry.arguments?.getInt("totalScore") ?: 0
            GameResultScreen(
                totalScore = totalScore,
                onPlayAgain = {
                    navController.navigate(Screen.ClassicGame.createRoute("CLASSIC")) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onBackToMenu = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.MultiplayerLobby.route) {
            MultiplayerLobbyScreen(
                onGameStarted = { roomId ->
                    navController.navigate(Screen.MultiplayerGame.createRoute(roomId))
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.MultiplayerGame.route,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            MultiplayerGameScreen(
                roomId = roomId,
                onGameFinished = { totalScore ->
                    navController.navigate(Screen.GameResult.createRoute(totalScore)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Leaderboard.route) {
            LeaderboardScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
