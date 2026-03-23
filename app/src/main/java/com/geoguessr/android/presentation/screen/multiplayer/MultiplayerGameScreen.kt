package com.geoguessr.android.presentation.screen.multiplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geoguessr.android.presentation.screen.game.GameViewModel
import com.geoguessr.android.presentation.theme.*

@Composable
fun MultiplayerGameScreen(
    roomId: String,
    onGameFinished: (totalScore: Int) -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(roomId) {
        viewModel.initGame(roomId)
    }

    LaunchedEffect(uiState.isGameFinished) {
        if (uiState.isGameFinished) {
            onGameFinished(uiState.totalScore)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GeoBgPrimary),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator(color = GeoCyan)
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Multiplayer Game",
                        style = MaterialTheme.typography.headlineSmall,
                        color = GeoTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Room: ${uiState.currentRoom?.code ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GeoTextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Round ${uiState.currentRoom?.currentRound ?: 1}/${uiState.currentRoom?.totalRounds ?: 5}",
                        style = MaterialTheme.typography.titleMedium,
                        color = GeoCyan
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = GeoBgSecondary)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "Score: ${uiState.totalScore} pts",
                                style = MaterialTheme.typography.bodyLarge,
                                color = GeoCyan
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = viewModel::endGame,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = GeoError)
                            ) {
                                Text("End Game", color = GeoTextPrimary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
