package com.kjh.mynote.ui.features.purchase.search.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kjh.mynote.R
import com.kjh.mynote.databinding.LayoutEmptySearchResultsBinding
import com.kjh.mynote.databinding.LayoutLoadingBinding
import com.kjh.mynote.databinding.VhPurchaseNoteSearchFilterItemBinding
import com.kjh.mynote.databinding.VhPurchaseNoteSearchResultDateItemBinding
import com.kjh.mynote.databinding.VhPurchaseNoteSearchResultItemBinding
import com.kjh.mynote.databinding.VhPurchaseNoteSearchSelectedFilterOuterBinding
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.ui.features.purchase.search.Filters
import com.kjh.mynote.ui.features.purchase.search.PurchaseNoteSearchUiState
import timber.log.Timber

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 22..
 * Description:
 */
class PurchaseNoteSearchUiListAdapter(
    private val purchaseNoteItemClickAction: (PurchaseNoteUiModel) -> Unit,
    private val categoryClickAction: (Int) -> Unit,
    private val purchaseNameClickAction: () -> Unit,
    private val dateFilterClickAction: () -> Unit,
    private val priceFilterClickAction: () -> Unit,
    private val selectedFilterClickAction: (Filters) -> Unit
): ListAdapter<PurchaseNoteSearchUiState, RecyclerView.ViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = when (viewType) {
        // 필터 아이템.
        R.layout.vh_purchase_note_search_filter_item -> {
            PurchaseNoteSearchFilterItemViewHolder(
                VhPurchaseNoteSearchFilterItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), categoryClickAction, purchaseNameClickAction, dateFilterClickAction, priceFilterClickAction
            )
        }
        // 선택된 필터 리스트 아이템들.
        R.layout.vh_purchase_note_search_selected_filter_outer -> {
            PurchaseNoteSearchSelectedFilterOuterItemViewHolder(
                VhPurchaseNoteSearchSelectedFilterOuterBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), selectedFilterClickAction
            )
        }
        // 로딩.
        R.layout.layout_loading -> {
            PurchaseNoteSearchLoadingItemViewHolder(
                LayoutLoadingBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
        // Empty.
        R.layout.layout_empty_search_results -> {
            PurchaseNoteSearchEmptyItemViewHolder(
                LayoutEmptySearchResultsBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
        // 날짜 아이템.
        R.layout.vh_purchase_note_search_result_date_item -> {
            PurchaseNoteSearchDateItemViewHolder(
                VhPurchaseNoteSearchResultDateItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
        // 결과 아이템.
        R.layout.vh_purchase_note_search_result_item -> {
            PurchaseNoteSearchResultItemViewHolder(
                VhPurchaseNoteSearchResultItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), purchaseNoteItemClickAction
            )
        }

        else -> throw Exception("wrong viewType: $viewType")
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (val item = getItem(position)) {
            is PurchaseNoteSearchUiState.Loading -> {
                (holder as PurchaseNoteSearchLoadingItemViewHolder).bind(Unit)
            }
            is PurchaseNoteSearchUiState.Empty -> {
                (holder as PurchaseNoteSearchEmptyItemViewHolder).bind(Unit)
            }
            is PurchaseNoteSearchUiState.ResultItem -> {
                (holder as PurchaseNoteSearchResultItemViewHolder).bind(item)
            }
            is PurchaseNoteSearchUiState.DateItem -> {
                (holder as PurchaseNoteSearchDateItemViewHolder).bind(item)
            }
            is PurchaseNoteSearchUiState.FilterItem -> {
                (holder as PurchaseNoteSearchFilterItemViewHolder).bind(item.purchaseNoteSearchFilterUiState)
            }
            is PurchaseNoteSearchUiState.AppliedFilterItems -> {
                (holder as PurchaseNoteSearchSelectedFilterOuterItemViewHolder).bind(item)
            }
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is PurchaseNoteSearchUiState.Loading -> R.layout.layout_loading
        is PurchaseNoteSearchUiState.Empty -> R.layout.layout_empty_search_results
        is PurchaseNoteSearchUiState.DateItem -> R.layout.vh_purchase_note_search_result_date_item
        is PurchaseNoteSearchUiState.ResultItem -> R.layout.vh_purchase_note_search_result_item
        is PurchaseNoteSearchUiState.FilterItem -> R.layout.vh_purchase_note_search_filter_item
        is PurchaseNoteSearchUiState.AppliedFilterItems -> R.layout.vh_purchase_note_search_selected_filter_outer
    }

    companion object {
        private val UI_MODEL_COMPARATOR =
            object : DiffUtil.ItemCallback<PurchaseNoteSearchUiState>() {
                override fun areItemsTheSame(
                    oldItem: PurchaseNoteSearchUiState,
                    newItem: PurchaseNoteSearchUiState
                ): Boolean =
                    when {
                        oldItem is PurchaseNoteSearchUiState.FilterItem
                                && newItem is PurchaseNoteSearchUiState.FilterItem -> true

                        oldItem is PurchaseNoteSearchUiState.Loading
                                && newItem is PurchaseNoteSearchUiState.Loading -> true

                        oldItem is PurchaseNoteSearchUiState.Empty
                                && newItem is PurchaseNoteSearchUiState.Empty -> true

                        oldItem is PurchaseNoteSearchUiState.DateItem
                                && newItem is PurchaseNoteSearchUiState.DateItem -> {
                            oldItem.date == newItem.date
                        }

                        oldItem is PurchaseNoteSearchUiState.ResultItem
                                && newItem is PurchaseNoteSearchUiState.ResultItem -> {
                            oldItem.purchaseNoteItem.id == newItem.purchaseNoteItem.id
                        }

                        oldItem is PurchaseNoteSearchUiState.AppliedFilterItems
                                && newItem is PurchaseNoteSearchUiState.AppliedFilterItems -> {
                            true
                        }
                        else -> false
                    }

                override fun areContentsTheSame(
                    oldItem: PurchaseNoteSearchUiState,
                    newItem: PurchaseNoteSearchUiState
                ): Boolean = oldItem == newItem
            }
    }
}