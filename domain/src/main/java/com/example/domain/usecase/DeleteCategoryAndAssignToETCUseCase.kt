package com.example.domain.usecase

import com.example.domain.model.ApiResult
import com.example.domain.model.safeApiCall
import com.example.domain.repository.PurchaseNoteWithCategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 9..
 * Description:
 *
 *  카테고리 삭제시, 기존 카테고리를 가진 구매노트의 카테고리를 "기타" 카테고리로 변경 후, 카테고리 삭제.
 *
 */
class DeleteCategoryAndAssignToETCUseCase @Inject constructor(
    private val purchaseNoteWithCategoryRepository: PurchaseNoteWithCategoryRepository
) {
    suspend operator fun invoke(categoryId: Int): Flow<ApiResult<Unit>> =
        safeApiCall { purchaseNoteWithCategoryRepository.deleteCategoryAndReassignETC(categoryId) }
}