package com.kjh.mynote.ui.features.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.data.PieEntry
import com.kjh.mynote.databinding.VhHomePieChartItemBinding
import com.kjh.mynote.databinding.VhHomePlaceNoteWeekViewBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.features.home.piechart.HomePurchaseNoteCategoryPieChartViewHolder
import com.kjh.mynote.ui.features.home.weekview.HomePlaceNoteWeekViewItemViewHolder
import timber.log.Timber
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
    private val sliceClickAction: (PieEntry?) -> Unit
): ListAdapter<HomeItem, RecyclerView.ViewHolder>(UI_MODEL_COMPARATOR) {

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
                    ), sliceClickAction
                )
            }

            else -> throw Exception("wrong ViewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is HomeItem.HomePlaceNoteWeekView ->
                (holder as HomePlaceNoteWeekViewItemViewHolder).bind(item)

            is HomeItem.HomePurchaseNoteCategoryPirChart ->
                (holder as HomePurchaseNoteCategoryPieChartViewHolder).bind(item)
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is HomeItem.HomePlaceNoteWeekView -> VIEW_TYPE_PLACE_NOTE_WEEK_VIEW
        is HomeItem.HomePurchaseNoteCategoryPirChart -> VIEW_TYPE_CATEGORY_PIE_CHART
        else -> 0
    }

    companion object {
        private const val VIEW_TYPE_PLACE_NOTE_WEEK_VIEW = 1
        private const val VIEW_TYPE_CATEGORY_PIE_CHART = 2

        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<HomeItem>() {
            override fun areItemsTheSame(
                oldItem: HomeItem,
                newItem: HomeItem,
            ): Boolean = when {
                oldItem is HomeItem.HomePlaceNoteWeekView && newItem is HomeItem.HomePlaceNoteWeekView -> {
                    true
                }
                oldItem is HomeItem.HomePurchaseNoteCategoryPirChart && newItem is HomeItem.HomePurchaseNoteCategoryPirChart -> {
                    oldItem.pieEntries == newItem.pieEntries
                }
                else -> false
            }

            override fun areContentsTheSame(
                oldItem: HomeItem,
                newItem: HomeItem,
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

}
