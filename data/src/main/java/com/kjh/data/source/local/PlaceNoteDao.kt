package com.kjh.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
    fun observeAll(): Flow<List<PlaceNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(placeNoteEntity: PlaceNoteEntity): Long

    @Query("SELECT * FROM places WHERE id = :id")
    suspend fun getPlaceNoteById(id: Int): PlaceNoteEntity?

    @Query("DELETE FROM places WHERE id = :id")
    suspend fun deletePlaceNoteById(id: Int)

    @Query("SELECT * FROM places WHERE placeName = :placeName ORDER BY visitDate DESC")
    suspend fun getPlaceNotesByPlaceName(placeName: String): List<PlaceNoteEntity>

    @Query("""
        SELECT id, placeName, placeAddress, placeRoadAddress, COUNT(placeName) as count
        FROM places
        WHERE placeName LIKE '%' || :queryText || '%'
        OR placeAddress LIKE '%' || :queryText || '%'
        OR placeRoadAddress LIKE '%' || :queryText || '%'
        GROUP BY placeName
    """)
    suspend fun searchByQuery(queryText: String): List<SearchPlaceNoteWithCountEntity>
}
