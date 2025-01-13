package com.kjh.mynote.ui.features.purchase.statistics.adapter.contents

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.model.CategoryStats
import com.example.domain.model.PaymentMethodStats
import com.kjh.mynote.databinding.VhHomeCategoryPutchaseStatsItemBinding
import com.kjh.mynote.databinding.VhHomePlaceNoteWeekViewInnerMoreItemBinding
import com.kjh.mynote.ui.features.purchase.statistics.SeeAllEvent
import com.kjh.mynote.ui.features.purchase.statistics.StatsContentsItem

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 13..
 * Description:
 */

class StatsChildListAdapter(
    private val categoryStatsClickAction: (CategoryStats) -> Unit,
    private val paymentMethodStatsClickAction: (PaymentMethodStats) -> Unit,
    private val showAllClickAction: (SeeAllEvent) -> Unit,
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

            VIEW_TYPE_SEE_ALL -> {
                SeeAllItemViewHolder(
                    VhHomePlaceNoteWeekViewInnerMoreItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    ), showAllClickAction
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

            is StatsContentsItem.SeeAllItem -> {
                (holder as SeeAllItemViewHolder).bind(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is StatsContentsItem.CategoryStatsItem -> VIEW_TYPE_CATEGORY
        is StatsContentsItem.PaymentMethodStatsItem -> VIEW_TYPE_PAYMENT_METHOD
        is StatsContentsItem.SeeAllItem -> VIEW_TYPE_SEE_ALL
    }

    companion object {
        private const val VIEW_TYPE_CATEGORY = 0
        private const val VIEW_TYPE_PAYMENT_METHOD = 1
        private const val VIEW_TYPE_SEE_ALL = 2

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

                    oldItem is StatsContentsItem.SeeAllItem &&
                            newItem is StatsContentsItem.SeeAllItem -> {
                        oldItem.eventType == newItem.eventType
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