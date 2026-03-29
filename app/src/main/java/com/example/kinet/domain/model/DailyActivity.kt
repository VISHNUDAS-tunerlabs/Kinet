package com.example.kinet.domain.model

data class DailyActivity(
    val date: String, // ISO format: "yyyy-MM-dd"
    val steps: Int,
    val distanceMeters: Float,
    val calories: Float,
    val activeMinutes: Int
)
