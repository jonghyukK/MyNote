package com.kjh.mynote.ui.features.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.model.CategoryStats
import com.github.mikephil.charting.data.PieEntry
import com.kjh.mynote.databinding.VhHomePieChartItemBinding
import com.kjh.mynote.databinding.VhHomePlaceNoteWeekViewBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.features.home.HomeUiItem
import com.kjh.mynote.ui.features.home.adapter.statistics.HomePurchaseNoteStatisticsSectionItemViewHolder
import com.kjh.mynote.ui.features.home.adapter.weeklyplacenotes.HomeWeeklyPlaceNoteSectionItemViewHolder
import java.time.LocalDate

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 9..
 * Description:
 */
class HomeUiListAdapter(
    private val weekDayClickAction: (LocalDate) -> Unit,
    private val placeNoteClickAction: (PlaceNoteUiModel) -> Unit,
    private val makePlaceNoteClickAction: () -> Unit,
    private val seeAllPlaceNotesClickAction: () -> Unit,
    private val sliceClickAction: (PieEntry?) -> Unit,
    private val categoryStatsItemClickAction: (CategoryStats) -> Unit,
    private val seeAllPurchaseStatsClickAction: () -> Unit
): ListAdapter<HomeUiItem, RecyclerView.ViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_PLACE_NOTE_WEEK_VIEW -> {
                HomeWeeklyPlaceNoteSectionItemViewHolder(
                    VhHomePlaceNoteWeekViewBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    ), weekDayClickAction, placeNoteClickAction, makePlaceNoteClickAction, seeAllPlaceNotesClickAction
                )
            }

            VIEW_TYPE_CATEGORY_PIE_CHART -> {
                HomePurchaseNoteStatisticsSectionItemViewHolder(
                    VhHomePieChartItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    ), sliceClickAction, categoryStatsItemClickAction, seeAllPurchaseStatsClickAction
                )
            }

            else -> throw Exception("wrong ViewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is HomeUiItem.HomeWeeklyPurchaseNoteItem ->
                (holder as HomeWeeklyPlaceNoteSectionItemViewHolder).bind(item)

            is HomeUiItem.HomeMonthlyPurchaseStatisticsItem ->
                (holder as HomePurchaseNoteStatisticsSectionItemViewHolder).bind(item)
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is HomeUiItem.HomeWeeklyPurchaseNoteItem -> VIEW_TYPE_PLACE_NOTE_WEEK_VIEW
        is HomeUiItem.HomeMonthlyPurchaseStatisticsItem -> VIEW_TYPE_CATEGORY_PIE_CHART
    }

    companion object {
        private const val VIEW_TYPE_PLACE_NOTE_WEEK_VIEW = 1
        private const val VIEW_TYPE_CATEGORY_PIE_CHART = 2

        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<HomeUiItem>() {
            override fun areItemsTheSame(
                oldItem: HomeUiItem,
                newItem: HomeUiItem,
            ): Boolean = when {
                oldItem is HomeUiItem.HomeWeeklyPurchaseNoteItem
                        && newItem is HomeUiItem.HomeWeeklyPurchaseNoteItem -> {
                    true
                }
                oldItem is HomeUiItem.HomeMonthlyPurchaseStatisticsItem
                        && newItem is HomeUiItem.HomeMonthlyPurchaseStatisticsItem -> {
                    oldItem.pieEntries == newItem.pieEntries
                }
                else -> false
            }

            override fun areContentsTheSame(
                oldItem: HomeUiItem,
                newItem: HomeUiItem,
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
