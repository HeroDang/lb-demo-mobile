package com.group20.lbdemo.network

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET

data class ServerResponse(
    val message: String
)

interface ApiService {
    @GET("/api/hello")
    suspend fun hello(): Response<ApiResponse>

    @GET("/api/slow")
    suspend fun slow(): Response<ApiResponse>

    @GET("/api/health")
    suspend fun health(): Response<ApiResponse>
}
