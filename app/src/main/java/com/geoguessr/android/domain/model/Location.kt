package com.geoguessr.android.domain.model

data class Location(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val panoramaId: String = "",
    val country: String = "",
    val city: String = "",
    val hint: String = ""
)
