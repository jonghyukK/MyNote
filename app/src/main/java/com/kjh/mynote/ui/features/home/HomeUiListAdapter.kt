package com.kjh.mynote.ui.features.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.model.CategoryWithStats
import com.github.mikephil.charting.data.PieEntry
import com.kjh.mynote.databinding.VhHomePieChartItemBinding
import com.kjh.mynote.databinding.VhHomePlaceNoteWeekViewBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.features.home.piechart.HomePurchaseNoteCategoryPieChartViewHolder
import com.kjh.mynote.ui.features.home.weekview.HomePlaceNoteWeekViewItemViewHolder
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
    private val sliceClickAction: (PieEntry?) -> Unit,
    private val categoryStatsItemClickAction: (CategoryWithStats) -> Unit
): ListAdapter<HomeUiState, RecyclerView.ViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_PLACE_NOTE_WEEK_VIEW -> {
                HomePlaceNoteWeekViewItemViewHolder(
                    VhHomePlaceNoteWeekViewBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    ), weekDayClickAction, placeNoteClickAction, makePlaceNoteClickAction
                )
            }

            VIEW_TYPE_CATEGORY_PIE_CHART -> {
                HomePurchaseNoteCategoryPieChartViewHolder(
                    VhHomePieChartItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    ), sliceClickAction, categoryStatsItemClickAction
                )
            }

            else -> throw Exception("wrong ViewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is HomeUiState.PlaceNoteWeekItem ->
                (holder as HomePlaceNoteWeekViewItemViewHolder).bind(item)

            is HomeUiState.PurchaseNoteCategoryPieChartItem ->
                (holder as HomePurchaseNoteCategoryPieChartViewHolder).bind(item)
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is HomeUiState.PlaceNoteWeekItem -> VIEW_TYPE_PLACE_NOTE_WEEK_VIEW
        is HomeUiState.PurchaseNoteCategoryPieChartItem -> VIEW_TYPE_CATEGORY_PIE_CHART
        else -> 0
    }

    companion object {
        private const val VIEW_TYPE_PLACE_NOTE_WEEK_VIEW = 1
        private const val VIEW_TYPE_CATEGORY_PIE_CHART = 2

        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<HomeUiState>() {
            override fun areItemsTheSame(
                oldItem: HomeUiState,
                newItem: HomeUiState,
            ): Boolean = when {
                oldItem is HomeUiState.PlaceNoteWeekItem && newItem is HomeUiState.PlaceNoteWeekItem -> {
                    true
                }
                oldItem is HomeUiState.PurchaseNoteCategoryPieChartItem && newItem is HomeUiState.PurchaseNoteCategoryPieChartItem -> {
                    oldItem.pieEntries == newItem.pieEntries
                }
                else -> false
            }

            override fun areContentsTheSame(
                oldItem: HomeUiState,
                newItem: HomeUiState,
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

}
