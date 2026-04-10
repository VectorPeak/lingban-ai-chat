package cn.vectorpeak.AIchat_projects.data

import android.content.Context
import cn.vectorpeak.AIchat_projects.data.local.AndroidSessionStore
import cn.vectorpeak.AIchat_projects.data.local.SessionStore
import cn.vectorpeak.AIchat_projects.data.remote.AuthApi
import cn.vectorpeak.AIchat_projects.data.remote.ChatApi
import cn.vectorpeak.AIchat_projects.data.remote.NetworkConfig
import cn.vectorpeak.AIchat_projects.data.repository.AuthRepository
import cn.vectorpeak.AIchat_projects.data.repository.ChatRepository
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val baseHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val authHttpClient = baseHttpClient.newBuilder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val chatHttpClient = baseHttpClient.newBuilder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val authRetrofit = Retrofit.Builder()
        .baseUrl(NetworkConfig.LOGIN_BASE_URL)
        .client(authHttpClient)
        .build()

    private val chatRetrofit = Retrofit.Builder()
        .baseUrl(NetworkConfig.CHAT_BASE_URL)
        .client(chatHttpClient)
        .build()

    private val authApi = authRetrofit.create(AuthApi::class.java)
    private val chatApi = chatRetrofit.create(ChatApi::class.java)

    val sessionStore: SessionStore = AndroidSessionStore(context.applicationContext)

    val authRepository = AuthRepository(
        api = authApi,
        json = json,
        sessionStore = sessionStore,
    )

    val chatRepository = ChatRepository(
        api = chatApi,
        httpClient = chatHttpClient,
        json = json,
        sessionStore = sessionStore,
    )
}
