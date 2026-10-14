package com.example.rickandmortyexplorer.data

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL = "https://rickandmortyapi.com/api/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
    .baseUrl(com.example.rickandmortyexplorer.data.BASE_URL)
    .build()

interface RickAndMortyApi {
    @GET("character")
    suspend fun getCharacters(
        @Query("name") name: String? = null,
        @Query("page") page: Int = 1
    ): ApiResponse<Character>

    @GET("episode")
    suspend fun getEpisodes(
        @Query("name") name: String? = null,
        @Query("page") page: Int = 1
    ): ApiResponse<Episode>

    @GET("location")
    suspend fun getLocations(
        @Query("name") name: String? = null,
        @Query("page") page: Int = 1
    ): ApiResponse<Location>
}

object RetrofitInstance {
    val api: RickAndMortyApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://rickandmortyapi.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RickAndMortyApi::class.java)
    }
}