package cn.vectorpeak.AIchat_projects.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RoleDto(
    val roleKey: String,
    val nickname: String,
    val archetype: String,
    val avatarUrl: String,
    val backgroundUrl: String,
    val personaSummary: String,
    val openingMessage: String,
)

data class Role(
    val roleKey: String,
    val nickname: String,
    val archetype: String,
    val avatarUrl: String,
    val backgroundUrl: String,
    val personaSummary: String,
    val openingMessage: String,
)

fun RoleDto.toDomain(): Role = Role(
    roleKey = roleKey,
    nickname = nickname,
    archetype = archetype,
    avatarUrl = avatarUrl,
    backgroundUrl = backgroundUrl,
    personaSummary = personaSummary,
    openingMessage = openingMessage,
)
