package com.kjh.mynote.ui.features.viewer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kjh.mynote.databinding.VhImagesViewerItemBinding

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 18..
 * Description:
 */

class ImagesViewerPagerAdapter(
    private val imageUrls: List<String>
): RecyclerView.Adapter<ImagesViewerListItemViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = ImagesViewerListItemViewHolder(
        VhImagesViewerItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun getItemCount() = imageUrls.size

    override fun onBindViewHolder(holder: ImagesViewerListItemViewHolder, position: Int) {
        holder.bind(imageUrls[position])
    }
}