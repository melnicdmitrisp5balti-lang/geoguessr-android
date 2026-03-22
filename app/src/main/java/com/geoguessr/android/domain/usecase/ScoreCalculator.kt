package com.geoguessr.android.domain.usecase

import com.geoguessr.android.domain.model.Location
import kotlin.math.*

object ScoreCalculator {
    private const val MAX_SCORE = 5000
    private const val EARTH_RADIUS_KM = 6371.0

    fun calculateScore(actual: Location, guessed: Location): Int {
        val distanceKm = haversineDistance(actual, guessed)
        return when {
            distanceKm < 0.1 -> MAX_SCORE
            distanceKm > 20000 -> 0
            else -> {
                val score = MAX_SCORE * exp(-distanceKm / 2000.0)
                score.toInt().coerceIn(0, MAX_SCORE)
            }
        }
    }

    fun haversineDistance(loc1: Location, loc2: Location): Double {
        val lat1 = Math.toRadians(loc1.latitude)
        val lat2 = Math.toRadians(loc2.latitude)
        val deltaLat = Math.toRadians(loc2.latitude - loc1.latitude)
        val deltaLon = Math.toRadians(loc2.longitude - loc1.longitude)

        val a = sin(deltaLat / 2).pow(2) +
                cos(lat1) * cos(lat2) * sin(deltaLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }
}
