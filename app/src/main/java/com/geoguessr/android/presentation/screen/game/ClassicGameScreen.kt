package com.geoguessr.android.presentation.screen.game

import android.os.Bundle
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.ktx.awaitMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.StreetViewPanoramaView
import com.geoguessr.android.domain.model.Location
import com.geoguessr.android.presentation.theme.*
import kotlinx.coroutines.launch

@Composable
fun ClassicGameScreen(
    gameType: String = "CLASSIC",
    onGameFinished: (totalScore: Int) -> Unit,
    viewModel: ClassicGameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (uiState.phase == ClassicGamePhase.LOADING) {
            viewModel.startGame(gameType)
        }
    }

    LaunchedEffect(uiState.phase) {
        if (uiState.phase == ClassicGamePhase.GAME_OVER) {
            onGameFinished(uiState.totalScore)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GeoBgPrimary)
    ) {
        when (uiState.phase) {
            ClassicGamePhase.LOADING -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GeoCyan)
                }
            }
            ClassicGamePhase.PLAYING -> {
                PlayingPhase(
                    uiState = uiState,
                    onMapTap = viewModel::onMapTap,
                    onToggleMap = viewModel::toggleMapExpanded,
                    onSubmitGuess = viewModel::submitGuess
                )
            }
            ClassicGamePhase.ROUND_RESULT -> {
                RoundResultPhase(
                    uiState = uiState,
                    onNext = viewModel::nextRound
                )
            }
            ClassicGamePhase.GAME_OVER -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GeoCyan)
                }
            }
        }
    }
}

@Composable
private fun PlayingPhase(
    uiState: ClassicGameUiState,
    onMapTap: (Double, Double) -> Unit,
    onToggleMap: () -> Unit,
    onSubmitGuess: () -> Unit
) {
    val location = uiState.currentLocation ?: return

    Box(modifier = Modifier.fillMaxSize()) {
        StreetViewCompose(
            latitude = location.latitude,
            longitude = location.longitude,
            modifier = Modifier.fillMaxSize()
        )

        GameHUD(
            round = uiState.currentRound,
            totalRounds = uiState.totalRounds,
            score = uiState.totalScore,
            timeRemaining = uiState.timeRemaining,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        )

        if (uiState.isMapExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable { onToggleMap() }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(0.6f)
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(16.dp))
                        .border(2.dp, GeoCyan, RoundedCornerShape(16.dp))
                ) {
                    GuessMapCompose(
                        guessedLocation = uiState.guessedLocation,
                        onMapTap = onMapTap,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Button(
                    onClick = onSubmitGuess,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 48.dp)
                        .fillMaxWidth(0.9f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GeoCyan)
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = GeoBgPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SUBMIT GUESS", color = GeoBgPrimary, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.dp, GeoCyan, RoundedCornerShape(12.dp))
                        .clickable { onToggleMap() }
                ) {
                    GuessMapCompose(
                        guessedLocation = uiState.guessedLocation,
                        onMapTap = onMapTap,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (uiState.guessedLocation == null) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Tap to\nguess",
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                Button(
                    onClick = onSubmitGuess,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GeoCyan),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        "GUESS",
                        color = GeoBgPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun GameHUD(
    round: Int,
    totalRounds: Int,
    score: Int,
    timeRemaining: Int,
    modifier: Modifier = Modifier
) {
    val timerColor = when {
        timeRemaining <= 10 -> Color(0xFFFF6B6B)
        timeRemaining <= 30 -> GeoYellow
        else -> GeoCyan
    }
    Box(
        modifier = modifier
            .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = GeoBgSecondary.copy(alpha = 0.9f))
            ) {
                Text(
                    text = "Round $round/$totalRounds",
                    style = MaterialTheme.typography.labelLarge,
                    color = GeoTextPrimary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = GeoBgSecondary.copy(alpha = 0.9f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Timer, contentDescription = null, tint = timerColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${timeRemaining}s", style = MaterialTheme.typography.labelLarge, color = timerColor)
                }
            }
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = GeoBgSecondary.copy(alpha = 0.9f))
            ) {
                Text(
                    text = "$score pts",
                    style = MaterialTheme.typography.labelLarge,
                    color = GeoCyan,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun RoundResultPhase(
    uiState: ClassicGameUiState,
    onNext: () -> Unit
) {
    val isLastRound = uiState.currentRound >= uiState.totalRounds
    val actual = uiState.currentLocation
    val guessed = uiState.guessedLocation

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GeoBgPrimary)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (actual != null) {
                ResultMapCompose(
                    actualLocation = actual,
                    guessedLocation = guessed,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Round ${uiState.currentRound} Result",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = GeoBgSecondary)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (actual != null && actual.city.isNotEmpty()) {
                    Text(
                        text = "📍 ${actual.city}, ${actual.country}",
                        style = MaterialTheme.typography.titleMedium,
                        color = GeoTextPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "+${uiState.roundScore}",
                            style = MaterialTheme.typography.displaySmall,
                            color = GeoCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text("points", style = MaterialTheme.typography.labelMedium, color = GeoTextSecondary)
                    }
                    VerticalDivider(modifier = Modifier.height(64.dp), color = GeoCardBorder)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val dist = uiState.roundDistance
                        val distText = if (dist < 1) "${(dist * 1000).toInt()} m" else "${dist.toInt()} km"
                        Text(distText, style = MaterialTheme.typography.displaySmall, color = GeoGreen, fontWeight = FontWeight.Bold)
                        Text("distance", style = MaterialTheme.typography.labelMedium, color = GeoTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Total: ${uiState.totalScore} pts",
                    style = MaterialTheme.typography.bodyLarge,
                    color = GeoTextSecondary
                )
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GeoCyan)
                ) {
                    Text(
                        if (isLastRound) "See Final Results" else "Next Round →",
                        color = GeoBgPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
fun StreetViewCompose(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val streetViewPanoramaView = remember { StreetViewPanoramaView(context) }

    DisposableEffect(lifecycleOwner) {
        streetViewPanoramaView.onCreate(null)
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> streetViewPanoramaView.onResume()
                Lifecycle.Event.ON_PAUSE -> streetViewPanoramaView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            streetViewPanoramaView.onDestroy()
        }
    }

    AndroidView(
        factory = { streetViewPanoramaView },
        update = { view ->
            view.getStreetViewPanoramaAsync { panorama ->
                panorama.isStreetNamesEnabled = false
                panorama.isZoomGesturesEnabled = true
                panorama.isPanningGesturesEnabled = true
                panorama.setPosition(LatLng(latitude, longitude))
            }
        },
        modifier = modifier
    )
}

@Composable
fun GuessMapCompose(
    guessedLocation: Location?,
    onMapTap: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val mapView = remember { MapView(context) }
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var guessMarker by remember { mutableStateOf<Marker?>(null) }

    DisposableEffect(lifecycleOwner) {
        mapView.onCreate(null)
        mapView.onStart()
        mapView.onResume()
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    LaunchedEffect(guessedLocation) {
        val map = googleMap ?: return@LaunchedEffect
        guessMarker?.remove()
        if (guessedLocation != null && (guessedLocation.latitude != 0.0 || guessedLocation.longitude != 0.0)) {
            val latlng = LatLng(guessedLocation.latitude, guessedLocation.longitude)
            guessMarker = map.addMarker(
                MarkerOptions()
                    .position(latlng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                    .title("Your guess")
            )
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 4f))
        }
    }

    AndroidView(
        factory = { mapView },
        update = { view ->
            if (googleMap == null) {
                coroutineScope.launch {
                    val map = view.awaitMap()
                    googleMap = map
                    map.uiSettings.isZoomControlsEnabled = false
                    map.uiSettings.isMapToolbarEnabled = false
                    map.setOnMapClickListener { latlng ->
                        onMapTap(latlng.latitude, latlng.longitude)
                    }
                }
            }
        },
        modifier = modifier
    )
}

@Composable
fun ResultMapCompose(
    actualLocation: Location,
    guessedLocation: Location?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val mapView = remember { MapView(context) }
    var mapReady by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        mapView.onCreate(null)
        mapView.onStart()
        mapView.onResume()
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    AndroidView(
        factory = { mapView },
        update = { view ->
            if (!mapReady) {
                mapReady = true
                coroutineScope.launch {
                    val map = view.awaitMap()
                    val actualLatLng = LatLng(actualLocation.latitude, actualLocation.longitude)
                    map.addMarker(
                        MarkerOptions()
                            .position(actualLatLng)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                            .title("Actual location")
                    )
                    if (guessedLocation != null && (guessedLocation.latitude != 0.0 || guessedLocation.longitude != 0.0)) {
                        val guessedLatLng = LatLng(guessedLocation.latitude, guessedLocation.longitude)
                        map.addMarker(
                            MarkerOptions()
                                .position(guessedLatLng)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                                .title("Your guess")
                        )
                        map.addPolyline(
                            PolylineOptions()
                                .add(actualLatLng, guessedLatLng)
                                .color(android.graphics.Color.parseColor("#00d9ff"))
                                .width(4f)
                        )
                        val builder = LatLngBounds.Builder()
                        builder.include(actualLatLng)
                        builder.include(guessedLatLng)
                        try {
                            map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100))
                        } catch (e: Exception) {
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(actualLatLng, 4f))
                        }
                    } else {
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(actualLatLng, 6f))
                    }
                }
            }
        },
        modifier = modifier
    )
}
