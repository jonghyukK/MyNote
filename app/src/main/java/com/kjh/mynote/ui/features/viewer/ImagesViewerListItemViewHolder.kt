package com.kjh.mynote.ui.features.viewer

import com.kjh.mynote.databinding.VhImagesViewerItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.extensions.loadImage

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 18..
 * Description:
 */
class ImagesViewerListItemViewHolder(
    private val binding: VhImagesViewerItemBinding
): BaseViewHolder<String>(binding.root) {

    override fun bind(item: String) {
        super.bind(item)

        binding.ivImage.loadImage(item)
    }
}