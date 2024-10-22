package com.kjh.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kjh.data.db.entity.PlaceNoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 2..
 * Description:
 */

@Dao
interface PlaceNoteDao {

    @Query("SELECT * FROM places")
    fun observeAll(): Flow<List<PlaceNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(placeNoteEntity: PlaceNoteEntity): Long

    @Query("SELECT * FROM places ORDER BY visitDate DESC LIMIT 1")
    suspend fun getRecentVisitPlace(): PlaceNoteEntity?

    @Query("SELECT * FROM places WHERE visitDate BETWEEN :startDate AND :endDate")
    suspend fun getPlacesInDateRange(startDate: Long, endDate: Long): List<PlaceNoteEntity>

    @Query("SELECT * FROM places WHERE id = :id")
    suspend fun getPlaceNoteById(id: Int): PlaceNoteEntity

    @Query("DELETE FROM places WHERE id = :id")
    suspend fun deletePlaceNoteById(id: Int)
}