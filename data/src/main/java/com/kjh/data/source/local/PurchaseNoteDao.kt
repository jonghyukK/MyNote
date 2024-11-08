package com.kjh.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kjh.data.model.entity.PurchaseNoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 7..
 * Description:
 */

@Dao
interface PurchaseNoteDao {

    @Query("SELECT * FROM purchase ORDER BY purchaseDate DESC")
    fun observeAll(): Flow<List<PurchaseNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(purchaseNoteEntity: PurchaseNoteEntity): Long

    @Query("SELECT * FROM purchase WHERE id = :id")
    suspend fun getPurchaseNoteById(id: Int): PurchaseNoteEntity
}