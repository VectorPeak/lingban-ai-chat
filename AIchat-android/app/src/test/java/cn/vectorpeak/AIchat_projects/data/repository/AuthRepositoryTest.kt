package cn.vectorpeak.AIchat_projects.data.repository

import cn.vectorpeak.AIchat_projects.data.FakeSessionStore
import cn.vectorpeak.AIchat_projects.data.model.LoginRequest
import cn.vectorpeak.AIchat_projects.data.model.SendCodeRequest
import cn.vectorpeak.AIchat_projects.data.remote.AuthApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

class AuthRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: AuthRepository
    private val sessionStore = FakeSessionStore()

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .build()
            .create(AuthApi::class.java)
        repository = AuthRepository(
            api = api,
            json = Json { ignoreUnknownKeys = true; explicitNulls = false },
            sessionStore = sessionStore,
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `sendCode decodes success envelope`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """
                {"success":true,"data":{"requestId":"req-1","validTimeSeconds":300,"codeLength":6}}
                """.trimIndent(),
            ),
        )

        val result = repository.sendCode(
            SendCodeRequest(phoneNumber = "13800138000"),
        )

        assertEquals("req-1", result.requestId)
        assertEquals(300, result.validTimeSeconds)
        assertEquals(6, result.codeLength)
    }

    @Test
    fun `login stores token into session store`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """
                {"success":true,"data":{"token":"jwt-token","tokenType":"Bearer","expiresIn":7200,"user":{"phoneNumber":"13800138000","countryCode":"86"}}}
                """.trimIndent(),
            ),
        )

        repository.login(
            LoginRequest(
                phoneNumber = "13800138000",
                verifyCode = "123456",
            ),
        )

        val snapshot = sessionStore.snapshot()
        assertEquals("jwt-token", snapshot.token)
        assertEquals("13800138000", snapshot.phoneNumber)
        assertTrue(snapshot.isLoggedIn)
    }
}
