package com.kjh.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kjh.data.model.entity.PaymentMethodEntity
import kotlinx.coroutines.flow.Flow

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 31..
 * Description:
 */

@Dao
interface PaymentMethodDao {

    /**
     * 결제수단 목록 조회.
     *
     * @return
     */
    @Query("SELECT * FROM paymentMethod")
    fun getAllPaymentMethods(): Flow<List<PaymentMethodEntity>>

    /**
     * 결제수단 등록.
     *
     * @param paymentMethodEntity
     * @return
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(paymentMethodEntity: PaymentMethodEntity): Long

    /**
     * 결제수단 수정.
     *
     * @param id
     * @param newPaymentMethodName
     */
    @Query("UPDATE paymentMethod SET paymentMethodName = :newPaymentMethodName WHERE paymentMethodId = :id")
    suspend fun updatePaymentMethod(
        id: Int,
        newPaymentMethodName: String
    )

    /**
     * 결제수단 삭제.
     *
     * @param id
     */
    @Query("DELETE FROM paymentMethod WHERE paymentMethodId = :id")
    suspend fun deletePaymentMethod(id: Int)
}