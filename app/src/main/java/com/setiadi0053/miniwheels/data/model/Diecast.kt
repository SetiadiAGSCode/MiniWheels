package com.setiadi0053.miniwheels.data.model

data class Diecast(
    val id: String,
    val name: String,
    val brand: String,
    val scale: String,
    val releaseYear: Int,
    val imageUrl: String,
    val ownerId: String
)
