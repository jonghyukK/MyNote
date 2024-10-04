package com.kjh.mynote.ui.features.note.make

import android.net.Uri
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
    private val deleteImageClickAction: (Uri) -> Unit,
    private val tempImageClickAction: (Uri) -> Unit
): ListAdapter<Uri, TempImageItemViewHolder>(UI_MODEL_COMPARATOR) {

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
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<Uri>() {
            override fun areItemsTheSame(
                oldItem: Uri,
                newItem: Uri
            ): Boolean = oldItem == newItem

            override fun areContentsTheSame(
                oldItem: Uri,
                newItem: Uri
            ): Boolean = oldItem == newItem
        }
    }
}