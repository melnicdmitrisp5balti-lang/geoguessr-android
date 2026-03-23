package com.geoguessr.android.presentation.screen.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geoguessr.android.domain.model.RoundResult
import com.geoguessr.android.presentation.theme.*

@Composable
fun GameResultScreen(
    totalScore: Int,
    onPlayAgain: () -> Unit,
    onBackToMenu: () -> Unit,
    viewModel: ClassicGameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val rounds = uiState.roundResults

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GeoBgPrimary)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(GeoBgSecondary, GeoBgPrimary)))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🏆", style = MaterialTheme.typography.displayMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Game Over!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = GeoTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$totalScore",
                    style = MaterialTheme.typography.displayLarge,
                    color = GeoCyan,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "points",
                    style = MaterialTheme.typography.titleMedium,
                    color = GeoTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                val message = when {
                    totalScore >= 20000 -> "🌟 Perfect Explorer!"
                    totalScore >= 15000 -> "🎯 Excellent!"
                    totalScore >= 10000 -> "👍 Good Job!"
                    totalScore >= 5000 -> "🗺️ Keep Exploring!"
                    else -> "📍 Practice Makes Perfect!"
                }
                Text(message, style = MaterialTheme.typography.bodyLarge, color = GeoGreen)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (rounds.isNotEmpty()) {
            Text(
                text = "Round Summary",
                style = MaterialTheme.typography.titleMedium,
                color = GeoTextSecondary,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            rounds.forEach { round ->
                RoundSummaryCard(round = round)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onPlayAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GeoCyan)
            ) {
                Icon(Icons.Filled.Replay, contentDescription = null, tint = GeoBgPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Play Again", color = GeoBgPrimary, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge)
            }
            OutlinedButton(
                onClick = onBackToMenu,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GeoCyan.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GeoCyan)
            ) {
                Icon(Icons.Filled.Home, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Back to Menu", style = MaterialTheme.typography.labelLarge)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun RoundSummaryCard(round: RoundResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GeoBgSecondary),
        border = androidx.compose.foundation.BorderStroke(1.dp, GeoCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(GeoBgTertiary, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Text("${round.roundNumber}", style = MaterialTheme.typography.labelLarge, color = GeoCyan)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (round.actualLocation.city.isNotEmpty())
                        "${round.actualLocation.city}, ${round.actualLocation.country}"
                    else "Round ${round.roundNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GeoTextPrimary
                )
                val dist = round.distanceKm
                val distText = if (dist < 1) "${(dist * 1000).toInt()} m" else "${dist.toInt()} km"
                Text(distText, style = MaterialTheme.typography.labelMedium, color = GeoTextSecondary)
            }
            Text(
                text = "+${round.score}",
                style = MaterialTheme.typography.titleMedium,
                color = GeoCyan,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
