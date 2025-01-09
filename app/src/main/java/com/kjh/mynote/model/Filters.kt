package com.kjh.mynote.model

import com.kjh.mynote.ui.features.place.search.result.DateRangeFilter
import com.kjh.mynote.utils.constants.AppConstants

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 7..
 * Description:
 */

sealed class Filters {
    abstract fun isApplied(): Boolean

    data class PurchaseName(
        val purchaseName: String = "",
    ) : Filters() {
        override fun isApplied(): Boolean = purchaseName.isNotBlank()
    }

    data class Category(
        val categoryItem: CategoryUiModel,
        val isSelected: Boolean = false,
    ) : Filters() {
        override fun isApplied(): Boolean = isSelected
    }

    data class PaymentMethod(
        val paymentMethod: PaymentMethodUiModel,
        val isSelected: Boolean = false,
    ) : Filters() {
        override fun isApplied(): Boolean = isSelected
    }

    data class DateRange(
        val dateRangeFilter: DateRangeFilter = DateRangeFilter.Monthly(),
    ) : Filters() {
        override fun isApplied(): Boolean = true
    }

    data class Price(
        val minPrice: Long? = null,
        val maxPrice: Long? = null,
        val myMaxPrice: Long = AppConstants.PRICE_MAX_LIMIT,
    ) : Filters() {
        override fun isApplied(): Boolean =
            minPrice != null || maxPrice != null
    }
}

fun List<Filters>.getAppliedCategoryIds(): List<Int> =
    filterIsInstance<Filters.Category>()
        .filter { it.isApplied() }
        .map { it.categoryItem.id }

fun List<Filters>.getAppliedPaymentMethodIds(): List<Int> =
    filterIsInstance<Filters.PaymentMethod>()
        .filter { it.isApplied() }
        .map { it.paymentMethod.paymentMethodId }

fun Filters.matchesFilter(other: Filters): Boolean =
    when (this) {
        is Filters.Category ->
            other is Filters.Category && categoryItem.id == other.categoryItem.id
        is Filters.PaymentMethod ->
            other is Filters.PaymentMethod && paymentMethod.paymentMethodId == other.paymentMethod.paymentMethodId
        is Filters.Price ->
            other is Filters.Price
        is Filters.PurchaseName ->
            other is Filters.PurchaseName
        else -> false
    }