package com.example.kinet.domain.model

data class UserProfile(
    val heightCm: Float,
    val weightKg: Float,
    val strideLengthCm: Float,
    val dailyStepGoal: Int = 10_000,
    val name: String = "",
    val profileImageUri: String? = null
) {
    companion object {
        val Default = UserProfile(
            heightCm = 170f,
            weightKg = 70f,
            strideLengthCm = 75f,
            dailyStepGoal = 10_000
        )
    }
}
