package com.geoguessr.android.presentation.screen.profile

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geoguessr.android.presentation.theme.*

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = GeoTextPrimary)
                }
                Text(
                    "My Profile",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GeoTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(GeoCyan.copy(alpha = 0.3f), GeoBgTertiary))),
                contentAlignment = Alignment.Center
            ) {
                Text("🌍", style = MaterialTheme.typography.displaySmall)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                uiState.nickname.ifBlank { "Explorer" },
                style = MaterialTheme.typography.headlineSmall,
                color = GeoTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                uiState.email,
                style = MaterialTheme.typography.bodyMedium,
                color = GeoTextSecondary
            )
        }

        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileStatCard("Total Score", "${uiState.totalScore}", "pts", Modifier.weight(1f))
            ProfileStatCard("Games Played", "${uiState.gamesPlayed}", "games", Modifier.weight(1f))
            ProfileStatCard("Best Score", "${uiState.bestScore}", "pts", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.gamesPlayed > 0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GeoBgSecondary),
                border = androidx.compose.foundation.BorderStroke(1.dp, GeoCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "📊 Average Score per Game",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GeoTextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${uiState.totalScore / uiState.gamesPlayed} pts",
                        style = MaterialTheme.typography.titleMedium,
                        color = GeoCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ProfileStatCard(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
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
