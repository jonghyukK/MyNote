package com.kjh.mynote.ui.features.note.make

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhTempImageItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.extensions.onThrottleClick

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 1..
 * Description:
 */

class TempImageItemViewHolder(
    private val binding: VhTempImageItemBinding,
    private val deleteImageClickAction: (String) -> Unit,
    private val tempImageClickAction: (String) -> Unit
): BaseViewHolder<String>(binding.root) {

    init {
        binding.ivDelete.onThrottleClick {
            bindItem?.let { item -> deleteImageClickAction.invoke(item)}
        }

        binding.ivPreview.onThrottleClick {
            bindItem?.let { item -> tempImageClickAction.invoke(item) }
        }
    }

    override fun bind(item: String) {
        super.bind(item)

        Glide.with(context)
            .load(item)
            .transform(CenterCrop(), RoundedCorners(16))
            .error(R.drawable.shape_s_purple_c_999)
            .into(binding.ivPreview)
    }
}