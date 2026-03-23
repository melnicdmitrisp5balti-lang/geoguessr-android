package com.geoguessr.android.presentation.screen.multiplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geoguessr.android.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerLobbyScreen(
    onGameStarted: (roomId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: MultiplayerLobbyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.startedRoomId) {
        uiState.startedRoomId?.let { roomId ->
            onGameStarted(roomId)
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = GeoBgPrimary
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = GeoTextPrimary)
                }
                Text(
                    "Multiplayer",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GeoTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

            Text("👥", style = MaterialTheme.typography.displayMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Play with Friends",
                style = MaterialTheme.typography.headlineSmall,
                color = GeoTextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                "Create a room or join with a code",
                style = MaterialTheme.typography.bodyMedium,
                color = GeoTextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(40.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GeoBgSecondary),
                border = androidx.compose.foundation.BorderStroke(1.dp, GeoCardBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Create New Room",
                        style = MaterialTheme.typography.titleMedium,
                        color = GeoTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Share the room code with friends",
                        style = MaterialTheme.typography.bodySmall,
                        color = GeoTextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState.createdRoomCode != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = GeoBgTertiary)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Room Code", style = MaterialTheme.typography.labelMedium, color = GeoTextSecondary)
                                Text(
                                    uiState.createdRoomCode,,
                                    style = MaterialTheme.typography.displaySmall,
                                    color = GeoCyan,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "${uiState.playersInRoom} player(s) in room",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GeoGreen
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = viewModel::startMultiplayerGame,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GeoGreen),
                            enabled = !uiState.isLoading && uiState.playersInRoom >= 1
                        ) {
                            Text("Start Game", color = GeoBgPrimary, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = viewModel::createRoom,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GeoCyan),
                            enabled = !uiState.isLoading
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(color = GeoBgPrimary, modifier = Modifier.size(20.dp))
                            } else {
                                Icon(Icons.Filled.Add, contentDescription = null, tint = GeoBgPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Create Room", color = GeoBgPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GeoBgSecondary),
                border = androidx.compose.foundation.BorderStroke(1.dp, GeoCardBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Join a Room",
                        style = MaterialTheme.typography.titleMedium,
                        color = GeoTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = uiState.joinCode,
                        onValueChange = viewModel::onJoinCodeChange,
                        label = { Text("Room Code", color = GeoTextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GeoTextPrimary,
                            unfocusedTextColor = GeoTextPrimary,
                            focusedBorderColor = GeoCyan,
                            unfocusedBorderColor = GeoCardBorder,
                            cursorColor = GeoCyan
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = viewModel::joinRoom,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GeoGreen),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GeoGreen),
                        enabled = uiState.joinCode.isNotBlank() && !uiState.isLoading
                    ) {
                        Text("Join Room", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
