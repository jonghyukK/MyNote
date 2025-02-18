package com.kjh.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kjh.data.model.entity.PlaceNoteEntity
import com.kjh.data.model.entity.SearchPlaceNoteWithCountEntity
import kotlinx.coroutines.flow.Flow

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 2..
 * Description:
 */

@Dao
interface PlaceNoteDao {

    @Query("SELECT * FROM places ORDER BY visitDate DESC")
    fun getAllPlaceNotes(): Flow<List<PlaceNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(placeNoteEntity: PlaceNoteEntity): Long

    @Update
    suspend fun update(placeNoteEntity: PlaceNoteEntity)

    @Query("SELECT * FROM places WHERE id = :id")
    suspend fun getPlaceNoteById(id: Int): PlaceNoteEntity?

    @Query("SELECT * FROM places WHERE id = :id")
    fun getPlaceNoteByIdFlow(id: Int): Flow<PlaceNoteEntity?>

    @Query("DELETE FROM places WHERE id = :id")
    suspend fun deletePlaceNoteById(id: Int)

    @Query("SELECT * FROM places WHERE placeName = :placeName ORDER BY visitDate DESC")
    suspend fun getPlaceNotesByPlaceName(placeName: String): List<PlaceNoteEntity>

    @Query("SELECT * FROM places WHERE placeName = :placeName ORDER BY visitDate DESC")
    fun getPlaceNotesByPlaceNameFlow(placeName: String): Flow<List<PlaceNoteEntity>>

    @Query("""
        SELECT id, placeName, placeAddress, placeRoadAddress, COUNT(placeName) as count
        FROM places
        WHERE placeName LIKE '%' || :queryText || '%'
        OR placeAddress LIKE '%' || :queryText || '%'
        OR placeRoadAddress LIKE '%' || :queryText || '%'
        GROUP BY placeName
    """)
    suspend fun searchByQuery(queryText: String): List<SearchPlaceNoteWithCountEntity>

    @Query("""
        SELECT * FROM places
        WHERE (placeName LIKE '%' || :queryText || '%'
        OR placeAddress LIKE '%' || :queryText || '%'
        OR placeRoadAddress LIKE '%' || :queryText || '%')
          AND visitDate >= :startDate
          AND visitDate <= :endDate
        ORDER BY 
          CASE WHEN :isDescending = 1 THEN visitDate END DESC,
          CASE WHEN :isDescending = 0 THEN visitDate END ASC
    """)
    suspend fun getFilteredPlaceNotes(
        queryText: String = "",
        startDate: Long,
        endDate: Long,
        isDescending: Boolean
    ): List<PlaceNoteEntity>

    @Query("""
        SELECT * FROM places
        WHERE visitDate BETWEEN :startDate And :endDate
    """)
    fun getPlaceNotesWithinDateRange(startDate: Long, endDate: Long): Flow<List<PlaceNoteEntity>>
}
