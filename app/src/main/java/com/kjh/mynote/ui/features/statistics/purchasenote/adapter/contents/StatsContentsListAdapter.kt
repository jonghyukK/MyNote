package com.kjh.mynote.ui.features.purchase.statistics.adapter.section.contents

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.model.CategoryStats
import com.example.domain.model.PaymentMethodStats
import com.kjh.mynote.databinding.VhHomeCategoryPutchaseStatsItemBinding
import com.kjh.mynote.ui.features.statistics.purchasenote.StatsContentsItem

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 13..
 * Description:
 */

class StatsChildListAdapter(
    private val categoryStatsClickAction: (CategoryStats) -> Unit,
    private val paymentMethodStatsClickAction: (PaymentMethodStats) -> Unit
) : ListAdapter<StatsContentsItem, RecyclerView.ViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            VIEW_TYPE_CATEGORY -> {
                CategoryStatsItemViewHolder(
                    VhHomeCategoryPutchaseStatsItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    ), categoryStatsClickAction
                )
            }

            VIEW_TYPE_PAYMENT_METHOD -> {
                PaymentMethodStatsItemViewHolder(
                    VhHomeCategoryPutchaseStatsItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    ), paymentMethodStatsClickAction
                )
            }

            else -> throw IllegalArgumentException("Wrong ViewType: $viewType")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is StatsContentsItem.CategoryStatsItem -> {
                (holder as CategoryStatsItemViewHolder).bind(item)
            }

            is StatsContentsItem.PaymentMethodStatsItem -> {
                (holder as PaymentMethodStatsItemViewHolder).bind(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is StatsContentsItem.CategoryStatsItem -> VIEW_TYPE_CATEGORY
        is StatsContentsItem.PaymentMethodStatsItem -> VIEW_TYPE_PAYMENT_METHOD
    }

    companion object {
        private const val VIEW_TYPE_CATEGORY = 0
        private const val VIEW_TYPE_PAYMENT_METHOD = 1

        private val UI_MODEL_COMPARATOR =
            object : DiffUtil.ItemCallback<StatsContentsItem>() {
                override fun areItemsTheSame(
                    oldItem: StatsContentsItem,
                    newItem: StatsContentsItem,
                ): Boolean = when {
                    oldItem is StatsContentsItem.CategoryStatsItem &&
                            newItem is StatsContentsItem.CategoryStatsItem -> {
                        oldItem.categoryStatsItem.categoryId == newItem.categoryStatsItem.categoryId
                    }

                    oldItem is StatsContentsItem.PaymentMethodStatsItem &&
                            newItem is StatsContentsItem.PaymentMethodStatsItem -> {
                        oldItem.paymentMethodStatsItem.paymentMethodId == newItem.paymentMethodStatsItem.paymentMethodId
                    }

                    else -> false
                }

                override fun areContentsTheSame(
                    oldItem: StatsContentsItem,
                    newItem: StatsContentsItem,
                ): Boolean = oldItem == newItem
            }
    }
}