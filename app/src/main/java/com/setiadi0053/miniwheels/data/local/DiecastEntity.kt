package com.setiadi0053.miniwheels.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.setiadi0053.miniwheels.data.model.Diecast

@Entity(tableName = "diecasts")
data class DiecastEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val brand: String,
    val scale: String,
    val releaseYear: Int,
    val imageUrl: String,
    val ownerId: String
)

fun DiecastEntity.asExternalModel() = Diecast(
    id = id,
    name = name,
    brand = brand,
    scale = scale,
    releaseYear = releaseYear,
    imageUrl = imageUrl,
    ownerId = ownerId
)

fun Diecast.asEntity() = DiecastEntity(
    id = id,
    name = name,
    brand = brand,
    scale = scale,
    releaseYear = releaseYear,
    imageUrl = imageUrl,
    ownerId = ownerId
)
