package com.example.data.sync

import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

@JsonClass(generateAdapter = true)
data class SyncDataWrapper(
    val entriesJson: String
)

@JsonClass(generateAdapter = true)
data class RemoteRequest(
    val name: String,
    val data: SyncDataWrapper
)

@JsonClass(generateAdapter = true)
data class RemoteResponse(
    val id: String,
    val name: String,
    val data: SyncDataWrapper?
)

interface SyncApi {
    @POST("objects")
    suspend fun createSyncObject(@Body request: RemoteRequest): Response<RemoteResponse>

    @GET("objects/{id}")
    suspend fun getSyncObject(@Path("id") id: String): Response<RemoteResponse>

    companion object {
        private const val BASE_URL = "https://api.restful-api.dev/"

        fun create(): SyncApi {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
            return retrofit.create(SyncApi::class.java)
        }
    }
}
