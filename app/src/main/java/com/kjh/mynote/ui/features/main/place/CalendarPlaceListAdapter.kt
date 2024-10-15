package com.kjh.mynote.ui.features.main.place

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.kjh.data.model.PlaceNoteModel
import com.kjh.mynote.databinding.VhPlaceNoteInCalendarItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.extensions.onThrottleClick

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 15..
 * Description:
 */
class CalendarPlaceListAdapter(
    private val placeClickAction: (PlaceNoteModel) -> Unit
): ListAdapter<PlaceNoteModel, CalendarPlaceListItemViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = CalendarPlaceListItemViewHolder(
        VhPlaceNoteInCalendarItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ), placeClickAction
    )

    override fun onBindViewHolder(holder: CalendarPlaceListItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<PlaceNoteModel>() {
            override fun areItemsTheSame(
                oldItem: PlaceNoteModel,
                newItem: PlaceNoteModel
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: PlaceNoteModel,
                newItem: PlaceNoteModel
            ): Boolean = oldItem == newItem
        }
    }
}

class CalendarPlaceListItemViewHolder(
    private val binding: VhPlaceNoteInCalendarItemBinding,
    private val placeClickAction: (PlaceNoteModel) -> Unit
): BaseViewHolder<PlaceNoteModel>(binding.root) {

    init {
        itemView.onThrottleClick {
            bindItem?.let { item -> placeClickAction.invoke(item) }
        }
    }

    override fun bind(item: PlaceNoteModel) {
        super.bind(item)

        Glide.with(context)
            .load(item.placeImages[0])
            .into(binding.ivPlaceFirstImage)

        binding.tvPlaceName.text = item.placeName
        binding.tvPlaceAddress.text = item.placeAddress
    }
}