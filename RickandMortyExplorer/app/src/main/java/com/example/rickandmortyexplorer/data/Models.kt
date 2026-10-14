package com.example.rickandmortyexplorer.data

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    val info: PageInfo,
    val results: List<T>
)

data class PageInfo(
    val count: Int,
    val pages: Int,
    val next: String?,
    val prev: String?
)

data class Character(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val image: String
)

data class Episode(
    val id: Int,
    val name: String,
    @SerializedName("air_date")
    val airDate: String,
    val episode: String
)

data class Location(
    val id: Int,
    val name: String,
    val type: String,
    val dimension: String
)