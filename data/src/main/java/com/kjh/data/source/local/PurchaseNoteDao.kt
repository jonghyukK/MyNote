package com.kjh.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.kjh.data.model.entity.PurchaseNoteEntity
import com.kjh.data.model.entity.PurchaseNoteWithCategoryEntity
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

    @Transaction
    @Query("SELECT * FROM purchase WHERE id = :id")
    suspend fun getPurchaseNoteById(id: Int): PurchaseNoteWithCategoryEntity

    @Transaction
    @Query("SELECT * FROM purchase")
    fun getPurchaseNotesWithCategory(): Flow<List<PurchaseNoteWithCategoryEntity>>

    @Query("SELECT * FROM purchase WHERE categoryId IN (:categoryIds)")
    fun getPurchaseNotesByCategoryIds(categoryIds: List<Int>): Flow<List<PurchaseNoteWithCategoryEntity>>

    @Query("UPDATE purchase SET categoryId = :etcCategoryId WHERE categoryId = :categoryId")
    suspend fun updateCategoryIdForPurchaseNote(etcCategoryId: Int, categoryId: Int)

    @Query("DELETE FROM purchase WHERE id = :id")
    suspend fun deletePurchaseNoteById(id: Int)

}