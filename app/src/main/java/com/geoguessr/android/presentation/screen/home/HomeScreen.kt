package com.geoguessr.android.presentation.screen.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geoguessr.android.presentation.theme.*

@Composable
fun HomeScreen(
    onPlayClassic: () -> Unit,
    onPlayMultiplayer: () -> Unit,
    onPlayDaily: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onLogout: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GeoBgPrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            HomeHeader(
                playerName = uiState.playerName,
                onLogout = {
                    viewModel.logout()
                    onLogout()
                }
            )
            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Game Modes",
                    style = MaterialTheme.typography.titleMedium,
                    color = GeoTextSecondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))

                GameModeCardLarge(
                    icon = Icons.Filled.Map,
                    title = "Play Classic",
                    description = "5 rounds • Random locations worldwide",
                    gradient = Brush.horizontalGradient(listOf(Color(0xFF0084ff), GeoCyan)),
                    onClick = onPlayClassic
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GameModeCardSmall(
                        icon = Icons.Filled.Groups,
                        title = "Multiplayer",
                        description = "Play with friends",
                        gradient = Brush.verticalGradient(listOf(Color(0xFF11998e), GeoGreen)),
                        onClick = onPlayMultiplayer,
                        modifier = Modifier.weight(1f)
                    )
                    GameModeCardSmall(
                        icon = Icons.Filled.CalendarToday,
                        title = "Daily",
                        description = "One location/day",
                        gradient = Brush.verticalGradient(listOf(Color(0xFFf7971e), GeoYellow)),
                        onClick = onPlayDaily,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.gamesPlayed > 0) {
                StatsSection(
                    totalScore = uiState.totalScore,
                    gamesPlayed = uiState.gamesPlayed,
                    bestScore = uiState.bestScore
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            LeaderboardPreview(
                topPlayers = uiState.topPlayers,
                onSeeAll = onOpenLeaderboard
            )
            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedButton(
                    onClick = onOpenProfile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GeoCyan.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GeoCyan)
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("My Profile")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HomeHeader(playerName: String, onLogout: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(GeoBgSecondary, GeoBgPrimary)
                )
            )
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hey, $playerName! 👋",
                        style = MaterialTheme.typography.headlineSmall,
                        color = GeoTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Where will you explore today?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GeoTextSecondary
                    )
                }
                IconButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(GeoBgTertiary)
                ) {
                    Icon(
                        Icons.Filled.ExitToApp,
                        contentDescription = "Logout",
                        tint = GeoTextSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🌍 ", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "GeoGuessr",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = GeoCyan
                )
            }
        }
    }
}

@Composable
private fun GameModeCardLarge(
    icon: ImageVector,
    title: String,
    description: String,
    gradient: Brush,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, label = "scale")

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .scale(scale),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(description, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
private fun GameModeCardSmall(
    icon: ImageVector,
    title: String,
    description: String,
    gradient: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(16.dp)
        ) {
            Column {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.weight(1f))
                Text(title, color = Color.White, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(description, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun StatsSection(totalScore: Int, gamesPlayed: Int, bestScore: Int) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Your Stats",
            style = MaterialTheme.typography.titleMedium,
            color = GeoTextSecondary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard("Total Score", totalScore.toString(), "pts", Modifier.weight(1f))
            StatCard("Games", gamesPlayed.toString(), "played", Modifier.weight(1f))
            StatCard("Best Score", bestScore.toString(), "pts", Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GeoBgSecondary),
        border = androidx.compose.foundation.BorderStroke(1.dp, GeoCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, color = GeoCyan, fontWeight = FontWeight.Bold)
            Text(unit, style = MaterialTheme.typography.labelSmall, color = GeoTextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = GeoTextSecondary, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun LeaderboardPreview(topPlayers: List<Pair<String, Int>>, onSeeAll: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🏆 Top Players",
                style = MaterialTheme.typography.titleMedium,
                color = GeoTextPrimary
            )
            TextButton(onClick = onSeeAll) {
                Text("See All", color = GeoCyan, style = MaterialTheme.typography.labelMedium)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GeoBgSecondary),
            border = androidx.compose.foundation.BorderStroke(1.dp, GeoCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (topPlayers.isEmpty()) {
                    Text(
                        text = "No players yet. Be the first!",
                        color = GeoTextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    topPlayers.take(5).forEachIndexed { index, (name, score) ->
                        if (index > 0) HorizontalDivider(color = GeoCardBorder, thickness = 0.5.dp)
                        LeaderboardRow(rank = index + 1, name = name, score = score)
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRow(rank: Int, name: String, score: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = when (rank) { 1 -> "🥇"; 2 -> "🥈"; 3 -> "🥉"; else -> "#$rank" },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(36.dp)
        )
        Text(name, style = MaterialTheme.typography.bodyMedium, color = GeoTextPrimary, modifier = Modifier.weight(1f))
        Text("$score pts", style = MaterialTheme.typography.bodyMedium, color = GeoCyan, fontWeight = FontWeight.Bold)
    }
}
