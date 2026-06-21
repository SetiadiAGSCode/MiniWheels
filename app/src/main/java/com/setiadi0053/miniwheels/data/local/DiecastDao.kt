package com.setiadi0053.miniwheels.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DiecastDao {
    @Query("SELECT * FROM diecasts WHERE ownerId = :userId")
    fun getDiecasts(userId: String): Flow<List<DiecastEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiecasts(diecasts: List<DiecastEntity>)

    @Query("DELETE FROM diecasts WHERE id = :id")
    suspend fun deleteDiecast(id: String)

    @Query("DELETE FROM diecasts WHERE ownerId = :userId")
    suspend fun clearAll(userId: String)
}
