package dev.toliner.openrouter.l1.account

import kotlinx.serialization.Serializable

@Serializable
data class Activity(
    val data: List<DailyActivity>
)

@Serializable
data class DailyActivity(
    val date: String,
    val requests: Int,
    val cost: Double
)
