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
}