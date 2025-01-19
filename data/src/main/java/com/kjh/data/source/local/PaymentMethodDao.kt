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
     * 결제수단 조회 (by PaymentMethodName)
     *
     * @param paymentMethodName
     * @return
     */
    @Query("SELECT * FROM paymentMethod WHERE paymentMethodName = :paymentMethodName")
    suspend fun getPaymentMethodByName(paymentMethodName: String): PaymentMethodEntity?

    /**
     * 결제수단 삭제.
     *
     * @param id
     */
    @Query("DELETE FROM paymentMethod WHERE paymentMethodId = :id")
    suspend fun deletePaymentMethod(id: Int)

    /**
     * 결제수단 등록 or 수정.
     *
     * @param paymentMethodEntity
     * @return
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(paymentMethodEntity: PaymentMethodEntity): Long

    /**
     *  결제수단 전체 isDefault = false로 설정.
     */
    @Query("UPDATE paymentMethod SET isDefault = 0 WHERE isDefault = 1")
    suspend fun resetDefaultPaymentMethods()
}