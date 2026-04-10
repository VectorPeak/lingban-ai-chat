package cn.vectorpeak.AIchat_projects.data.model

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.CHINA)

fun String.toDisplayTime(): String {
    return runCatching {
        OffsetDateTime.parse(this).format(timeFormatter)
    }.getOrDefault("刚刚")
}
