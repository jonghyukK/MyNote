package com.kjh.mynote.ui.features.home.weekview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kjh.mynote.databinding.VhHomePlaceNoteWeekViewBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.home.HomeItem
import java.time.LocalDate

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 9..
 * Description:
 */
class HomePlaceNoteWeekViewAdapter(
    private val weekDayClickAction: (LocalDate) -> Unit,
    private val placeNoteClickAction: (PlaceNoteUiModel) -> Unit,
    private val makePlaceNoteClickAction: () -> Unit
): ListAdapter<HomeItem, RecyclerView.ViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ) = when (viewType) {
        VIEW_TYPE_PLACE_NOTE_WEEK_VIEW -> HomePlaceNoteWeekViewItemViewHolder(
            VhHomePlaceNoteWeekViewBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), weekDayClickAction, placeNoteClickAction, makePlaceNoteClickAction
        )

        else -> throw Exception("wrong ViewType: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is HomeItem.HomePlaceNoteWeekView ->
                (holder as HomePlaceNoteWeekViewItemViewHolder).bind(item)
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is HomeItem.HomePlaceNoteWeekView -> VIEW_TYPE_PLACE_NOTE_WEEK_VIEW
        else -> 0
    }

    companion object {
        private const val VIEW_TYPE_PLACE_NOTE_WEEK_VIEW = 1

        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<HomeItem>() {
            override fun areItemsTheSame(
                oldItem: HomeItem,
                newItem: HomeItem,
            ): Boolean = when {
                oldItem is HomeItem.HomePlaceNoteWeekView && newItem is HomeItem.HomePlaceNoteWeekView -> {
                    oldItem.selectedDate == newItem.selectedDate
                }
                else -> false
            }

            override fun areContentsTheSame(
                oldItem: HomeItem,
                newItem: HomeItem,
            ): Boolean = oldItem == newItem
        }
    }

}
