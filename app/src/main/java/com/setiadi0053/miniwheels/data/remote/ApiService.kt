package com.setiadi0053.miniwheels.data.remote

import com.setiadi0053.miniwheels.data.model.Diecast
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("diecasts")
    suspend fun getDiecasts(
        @Header("Authorization") token: String
    ): Response<List<Diecast>>

    @Multipart
    @POST("diecasts")
    suspend fun addDiecast(
        @Header("Authorization") token: String,
        @Part("name") name: RequestBody,
        @Part("brand") brand: RequestBody,
        @Part("scale") scale: RequestBody,
        @Part("releaseYear") year: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<Diecast>

    @DELETE("diecasts/{id}")
    suspend fun deleteDiecast(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>

    @Multipart
    @PUT("diecasts/{id}")
    suspend fun updateDiecast(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Part("name") name: RequestBody,
        @Part("brand") brand: RequestBody,
        @Part("scale") scale: RequestBody,
        @Part("releaseYear") year: RequestBody,
        @Part image: MultipartBody.Part? = null
    ): Response<Unit>
}
