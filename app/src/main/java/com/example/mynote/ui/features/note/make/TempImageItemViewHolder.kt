package com.example.mynote.ui.features.note.make

import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.mynote.R
import com.example.mynote.databinding.VhTempImageItemBinding
import com.example.mynote.ui.base.BaseViewHolder
import com.example.mynote.utils.extensions.onThrottleClick

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 1..
 * Description:
 */

class TempImageItemViewHolder(
    private val binding: VhTempImageItemBinding,
    private val deleteImageClickAction: (Uri) -> Unit,
    private val tempImageClickAction: (Uri) -> Unit
): BaseViewHolder<Uri>(binding.root) {

    init {
        binding.ivDelete.onThrottleClick {
            bindItem?.let { item -> deleteImageClickAction.invoke(item)}
        }

        binding.ivPreview.onThrottleClick {
            bindItem?.let { item -> tempImageClickAction.invoke(item) }
        }
    }

    override fun bind(item: Uri) {
        super.bind(item)

        Glide.with(context)
            .load(item)
            .transform(CenterCrop(), RoundedCorners(16))
            .error(R.drawable.shape_s_purple_c_999)
            .into(binding.ivPreview)
    }
}