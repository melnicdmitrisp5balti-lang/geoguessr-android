package com.geoguessr.android.presentation.screen.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geoguessr.android.presentation.theme.*

@Composable
fun LeaderboardScreen(
    onBack: () -> Unit,
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GeoBgPrimary)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(GeoBgSecondary, GeoBgPrimary)))
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = GeoTextPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("🏆", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Leaderboard",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GeoTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GeoCyan)
                }
            }
            uiState.players.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🌍", style = MaterialTheme.typography.displayMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No players yet!", style = MaterialTheme.typography.titleMedium, color = GeoTextSecondary)
                        Text("Play some games to appear here.", style = MaterialTheme.typography.bodyMedium, color = GeoTextSecondary)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(uiState.players) { index, (name, score) ->
                        LeaderboardItem(rank = index + 1, name = name, score = score)
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardItem(rank: Int, name: String, score: Int) {
    val isTop3 = rank <= 3
    val rankEmoji = when (rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> "#$rank"
    }
    val cardColor = if (rank == 1) GeoBgTertiary else GeoBgSecondary
    val borderColor = when (rank) {
        1 -> GeoYellow.copy(alpha = 0.5f)
        2 -> GeoTextSecondary.copy(alpha = 0.3f)
        3 -> GeoOrange.copy(alpha = 0.3f)
        else -> GeoCardBorder
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isTop3) GeoYellow.copy(alpha = 0.1f) else GeoBgTertiary),
                contentAlignment = Alignment.Center
            ) {
                if (rank <= 3) {
                    Text(rankEmoji, style = MaterialTheme.typography.titleMedium)
                } else {
                    Text(rankEmoji, style = MaterialTheme.typography.bodyLarge, color = GeoTextSecondary)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                name,
                style = MaterialTheme.typography.bodyLarge,
                color = GeoTextPrimary,
                fontWeight = if (isTop3) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            Text(
                "$score pts",
                style = MaterialTheme.typography.titleMedium,
                color = if (isTop3) GeoYellow else GeoCyan,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
