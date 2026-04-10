package cn.vectorpeak.AIchat_projects.data.remote

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ChatApi {
    @GET("api/chat/roles")
    suspend fun getRoles(): Response<ResponseBody>

    @GET("api/chat/history")
    suspend fun getHistory(
        @Header("Authorization") authorization: String,
        @Query("roleKey") roleKey: String,
        @Query("limit") limit: Int,
    ): Response<ResponseBody>

    @POST("api/chat/clear")
    suspend fun clearChat(
        @Header("Authorization") authorization: String,
        @Body body: RequestBody,
    ): Response<ResponseBody>

    @POST("api/chat/completions")
    suspend fun sendCompletion(
        @Header("Authorization") authorization: String,
        @Body body: RequestBody,
    ): Response<ResponseBody>
}
