package cn.vectorpeak.AIchat_projects.data.remote

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/send-code")
    suspend fun sendCode(
        @Body body: RequestBody,
    ): Response<ResponseBody>

    @POST("api/auth/login")
    suspend fun login(
        @Body body: RequestBody,
    ): Response<ResponseBody>
}
