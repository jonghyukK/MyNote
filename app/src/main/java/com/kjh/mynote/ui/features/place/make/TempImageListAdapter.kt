package com.kjh.mynote.ui.features.place.make

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhTempImageItemBinding

/**
 * Created by kangjonghyuk.
 * Created On 2024. 9. 29..
 * Description:
 */

class TempImageListAdapter(
    private val deleteImageClickAction: (String) -> Unit,
    private val tempImageClickAction: (String) -> Unit
): ListAdapter<String, TempImageItemViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = TempImageItemViewHolder(
        VhTempImageItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ), deleteImageClickAction, tempImageClickAction
    )

    override fun onBindViewHolder(holder: TempImageItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(
                oldItem: String,
                newItem: String
            ): Boolean = oldItem == newItem

            override fun areContentsTheSame(
                oldItem: String,
                newItem: String
            ): Boolean = oldItem == newItem
        }
    }
}