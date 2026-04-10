package cn.vectorpeak.AIchat_projects.data.remote

import cn.vectorpeak.AIchat_projects.data.model.ChatStreamChunkDto
import cn.vectorpeak.AIchat_projects.data.model.ChatStreamEvent
import kotlinx.serialization.json.Json

object ChatStreamParser {
    fun splitFrames(buffer: String): Pair<List<String>, String> {
        val frames = buffer.split("\n\n")
        if (frames.isEmpty()) {
            return emptyList<String>() to buffer
        }
        return frames.dropLast(1) to frames.last()
    }

    fun parseFrame(
        rawFrame: String,
        json: Json,
    ): List<ChatStreamEvent> {
        if (!rawFrame.startsWith("data: ")) {
            return emptyList()
        }

        val payload = rawFrame.removePrefix("data: ").trim()
        if (payload == "[DONE]") {
            return listOf(ChatStreamEvent.Completed)
        }

        val chunk = json.decodeFromString<ChatStreamChunkDto>(payload)
        val events = mutableListOf<ChatStreamEvent>()
        chunk.choices.forEach { choice ->
            val delta = choice.delta?.content.orEmpty()
            if (delta.isNotBlank()) {
                events += ChatStreamEvent.Delta(delta)
            }
        }
        if (chunk.usage != null) {
            events += ChatStreamEvent.Usage(chunk.usage)
        }
        return events
    }
}
