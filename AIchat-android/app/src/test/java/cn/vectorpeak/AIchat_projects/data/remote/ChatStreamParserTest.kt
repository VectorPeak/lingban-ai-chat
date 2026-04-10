package cn.vectorpeak.AIchat_projects.data.remote

import cn.vectorpeak.AIchat_projects.data.model.ChatStreamEvent
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatStreamParserTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `splitFrames returns complete frames and remaining buffer`() {
        val input = "data: {\"choices\":[{\"delta\":{\"content\":\"你\"}}]}\n\npartial"

        val (frames, remaining) = ChatStreamParser.splitFrames(input)

        assertEquals(1, frames.size)
        assertEquals("partial", remaining)
    }

    @Test
    fun `parseFrame returns delta and completion events`() {
        val deltaFrame = "data: {\"choices\":[{\"delta\":{\"content\":\"你好\"}}]}"
        val doneFrame = "data: [DONE]"

        val deltaEvents = ChatStreamParser.parseFrame(deltaFrame, json)
        val doneEvents = ChatStreamParser.parseFrame(doneFrame, json)

        assertEquals(listOf(ChatStreamEvent.Delta("你好")), deltaEvents)
        assertTrue(doneEvents.single() is ChatStreamEvent.Completed)
    }
}
