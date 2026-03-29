package com.example.kinet.domain.model

data class UserProfile(
    val heightCm: Float,
    val weightKg: Float,
    val strideLengthCm: Float
) {
    companion object {
        val Default = UserProfile(heightCm = 170f, weightKg = 70f, strideLengthCm = 75f)
    }
}
