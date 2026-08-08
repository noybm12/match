package com.example.match

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface FootballApiService {

    @GET("fixtures")
    suspend fun getTeamMatches(
        @Header("x-apisports-key") apiKey: String,
        @Query("team") teamId: Int,
        @Query("season") season: Int
    ): Response<ApiResponse>

    @GET("fixtures")
    suspend fun getMatchById(
        @Header("x-apisports-key") apiKey: String,
        @Query("id") matchId: Int
    ): Response<ApiResponse>
}