package cn.vectorpeak.AIchat_projects.data.repository

import cn.vectorpeak.AIchat_projects.data.FakeSessionStore
import cn.vectorpeak.AIchat_projects.data.model.SessionSnapshot
import cn.vectorpeak.AIchat_projects.data.remote.ChatApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

class ChatRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: ChatRepository
    private lateinit var sessionStore: FakeSessionStore

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        sessionStore = FakeSessionStore(
            initial = SessionSnapshot(
                onboardingSeen = true,
                token = "token-123",
                phoneNumber = "13800138000",
            ),
        )
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .build()
            .create(ChatApi::class.java)
        repository = ChatRepository(
            api = api,
            httpClient = OkHttpClient(),
            json = Json { ignoreUnknownKeys = true; explicitNulls = false },
            sessionStore = sessionStore,
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `fetchRoles maps role list`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """
                {"success":true,"data":[{"roleKey":"chenge","nickname":"辰哥","archetype":"技术专家","avatarUrl":"a","backgroundUrl":"b","personaSummary":"summary","openingMessage":"hello"}]}
                """.trimIndent(),
            ),
        )

        val roles = repository.fetchRoles()

        assertEquals(1, roles.size)
        assertEquals("chenge", roles.first().roleKey)
    }

    @Test
    fun `fetchHistory sends bearer token and stores last role`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """
                {"success":true,"data":{"role":{"roleKey":"chenge","nickname":"辰哥","archetype":"技术专家","avatarUrl":"a","backgroundUrl":"b","personaSummary":"summary","openingMessage":"hello"},"conversationId":7,"messages":[{"id":1,"role":"assistant","content":"hello","createdAt":"2026-04-09T13:00:00+08:00"}]}}
                """.trimIndent(),
            ),
        )

        val history = repository.fetchHistory("chenge")
        val request = server.takeRequest()

        assertEquals("Bearer token-123", request.getHeader("Authorization"))
        assertEquals(7L, history.conversationId)
        assertEquals("chenge", sessionStore.snapshot().lastRoleKey)
    }
}
