package cn.vectorpeak.AIchat_projects.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiEnvelope<T>(
    val success: Boolean = true,
    val message: String? = null,
    val data: T? = null,
)

@Serializable
data class ApiErrorEnvelope(
    val success: Boolean = false,
    val message: String? = null,
    val missingSettings: List<String> = emptyList(),
)
