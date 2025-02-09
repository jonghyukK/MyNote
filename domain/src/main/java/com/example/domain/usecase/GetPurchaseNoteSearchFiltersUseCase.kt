package com.example.domain.usecase

import com.example.domain.model.ApiResult
import com.example.domain.model.Category
import com.example.domain.model.PaymentMethod
import com.example.domain.model.PurchaseNoteFilters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 9..
 * Description:
 */
class GetPurchaseNoteSearchFiltersUseCase @Inject constructor(
    private val observeAllCategoriesUseCase: ObserveAllCategoriesUseCase,
    private val getMaxPurchasePriceUseCase: GetMaxPurchasePriceUseCase,
    private val observeAllPaymentMethodsUseCase: ObserveAllPaymentMethodsUseCase,
) {
    operator fun invoke(): Flow<ApiResult<PurchaseNoteFilters>> {
        return combine(
            observeAllCategoriesUseCase(),
            observeAllPaymentMethodsUseCase(),
            getMaxPurchasePriceUseCase()
        ) { categoriesResult, paymentMethodsResult, maxPriceResult ->
            handleFilterResults(categoriesResult, paymentMethodsResult, maxPriceResult)
        }
    }

    private fun handleFilterResults(
        categoriesResult: ApiResult<List<Category>>,
        paymentMethodsResult: ApiResult<List<PaymentMethod>>,
        maxPriceResult: ApiResult<Long?>
    ): ApiResult<PurchaseNoteFilters> {
        return when {
            categoriesResult is ApiResult.Loading
                    || paymentMethodsResult is ApiResult.Loading
                    || maxPriceResult is ApiResult.Loading -> {
                ApiResult.Loading
            }
            categoriesResult is ApiResult.Error -> {
                ApiResult.Error(categoriesResult.error)
            }
            paymentMethodsResult is ApiResult.Error -> {
                ApiResult.Error(paymentMethodsResult.error)
            }
            maxPriceResult is ApiResult.Error -> {
                ApiResult.Error(maxPriceResult.error)
            }

            categoriesResult is ApiResult.Success
                    && paymentMethodsResult is ApiResult.Success
                    && maxPriceResult is ApiResult.Success -> {

                ApiResult.Success(
                    PurchaseNoteFilters(
                        categories = categoriesResult.data,
                        paymentMethods = paymentMethodsResult.data,
                        highestPrice = maxPriceResult.data
                    )
                )
            }
            else -> ApiResult.Error(Throwable("알 수 없는 에러가 발생하였습니다."))
        }
    }
}