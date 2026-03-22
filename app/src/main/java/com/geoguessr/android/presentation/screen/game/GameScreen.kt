package com.geoguessr.android.presentation.screen.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geoguessr.android.presentation.components.GeoGuessrButton
import com.geoguessr.android.presentation.components.GeoGuessrOutlinedButton
import com.geoguessr.android.presentation.components.GeoGuessrTextField
import com.geoguessr.android.presentation.components.InlineLoadingIndicator
import com.geoguessr.android.presentation.components.LoadingIndicator
import com.geoguessr.android.presentation.theme.GeoYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    roomId: String,
    onGameFinished: () -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(roomId) {
        viewModel.initGame(roomId)
    }

    LaunchedEffect(uiState.isGameFinished) {
        if (uiState.isGameFinished) {
            onGameFinished()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val room = uiState.currentRoom
                    val roundText = if (room != null)
                        "Round ${room.currentRound} of ${room.totalRounds}"
                    else "Loading..."
                    Text(
                        text = roundText,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                actions = {
                    IconButton(onClick = viewModel::endGame) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "End game",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.isRoundFinished -> {
                RoundResultContent(
                    roundScore = uiState.roundScore ?: 0,
                    totalScore = uiState.totalScore,
                    currentRound = uiState.currentRoom?.currentRound ?: 0,
                    totalRounds = uiState.currentRoom?.totalRounds ?: 5,
                    onNextRound = viewModel::nextRound,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {
                GamePlayContent(
                    uiState = uiState,
                    onLocationGuessChange = viewModel::onLocationGuessChange,
                    onSubmitGuess = viewModel::submitGuess,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun GamePlayContent(
    uiState: GameUiState,
    onLocationGuessChange: (String) -> Unit,
    onSubmitGuess: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ScoreTimerRow(
            timeRemaining = uiState.timeRemaining,
            totalScore = uiState.totalScore
        )

        StreetViewPlaceholder(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        )

        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Where is this?",
                style = MaterialTheme.typography.titleMedium
            )

            GeoGuessrTextField(
                value = uiState.locationGuess,
                onValueChange = onLocationGuessChange,
                label = "Your guess (e.g. \"Paris, France\")",
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            if (uiState.isSubmitting) {
                InlineLoadingIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(52.dp)
                )
            } else {
                GeoGuessrButton(
                    text = "Submit Guess",
                    onClick = onSubmitGuess,
                    enabled = !uiState.isSubmitting
                )
            }
        }
    }
}

@Composable
private fun ScoreTimerRow(
    timeRemaining: Int,
    totalScore: Int,
    modifier: Modifier = Modifier
) {
    val timerColor = when {
        timeRemaining <= 10 -> MaterialTheme.colorScheme.error
        timeRemaining <= 20 -> GeoYellow
        else -> MaterialTheme.colorScheme.primary
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = "Score",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "$totalScore pts",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Time",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "${timeRemaining}s",
                style = MaterialTheme.typography.titleMedium,
                color = timerColor
            )
        }
    }
}

@Composable
private fun StreetViewPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Street View",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = "Where in the world are you?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
private fun RoundResultContent(
    roundScore: Int,
    totalScore: Int,
    currentRound: Int,
    totalRounds: Int,
    onNextRound: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Round $currentRound Result",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "+$roundScore",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "points this round",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Total: $totalScore pts",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
            Text(
                text = "📍 Map view coming soon",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(32.dp))

        val isLastRound = currentRound >= totalRounds
        GeoGuessrButton(
            text = if (isLastRound) "See Final Results" else "Next Round",
            onClick = onNextRound
        )
    }
}
