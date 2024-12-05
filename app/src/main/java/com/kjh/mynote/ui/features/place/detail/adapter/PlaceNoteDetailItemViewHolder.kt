package com.kjh.mynote.ui.features.place.detail.adapter

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhPlaceNoteDetailItemBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.common.listener.OnNestedHorizontalTouchListener
import com.kjh.mynote.ui.features.place.detail.PlaceNoteDetailUi
import com.kjh.mynote.utils.extensions.ifNullOrEmpty
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.toStringWithFormat

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 20..
 * Description:
 */
class PlaceNoteDetailItemViewHolder(
    private val binding: VhPlaceNoteDetailItemBinding,
    private val imageViewerClickAction: (List<String>, String) -> Unit,
    private val addressClickAction: (PlaceNoteUiModel) -> Unit
): BaseViewHolder<PlaceNoteDetailUi.DetailItem>(binding.root) {

    private var imagePagerAdapter: PlaceNoteDetailPagerAdapter? = null

    private var pageChangeCallback: OnPageChangeCallback = object: OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)

            bindItem?.let { item ->
                makeIndicator(position, item.placeNoteItem.placeImages.size)
            }
        }
    }

    init {
        imagePagerAdapter = PlaceNoteDetailPagerAdapter(imageClickAction = {
            bindItem?.let { item ->
                imageViewerClickAction.invoke(item.placeNoteItem.placeImages, it)
            }
        })

        binding.vpPlaceImages.apply {
            adapter = imagePagerAdapter
            (getChildAt(0) as RecyclerView)
                .addOnItemTouchListener(OnNestedHorizontalTouchListener())
            registerOnPageChangeCallback(pageChangeCallback)
        }

        binding.tvViewer.onThrottleClick {
            bindItem?.let { item ->
                imageViewerClickAction.invoke(
                    item.placeNoteItem.placeImages, item.placeNoteItem.placeImages[0]
                )
            }
        }

        binding.clLocation.onThrottleClick {
            bindItem?.let { item ->
                addressClickAction.invoke(item.placeNoteItem)
            }
        }
    }

    override fun bind(item: PlaceNoteDetailUi.DetailItem) {
        super.bind(item)

        val placeNoteItem = item.placeNoteItem

        imagePagerAdapter?.submitList(placeNoteItem.placeImages)

        with (binding) {
            vpPlaceImages.setCurrentItem(0, false)

            tvPlaceName.text = placeNoteItem.placeInfo.placeName
            tvAddress.text = placeNoteItem.placeInfo.roadAddress
                .ifNullOrEmpty(placeNoteItem.placeInfo.address)

            tvNoteContents.isVisible = placeNoteItem.noteContents.isNotEmpty()
            tvNoteContents.text = placeNoteItem.noteContents
            tvVisitDate.text = placeNoteItem.visitDate.toStringWithFormat("yyyy년 M월 d일 (E)")

            makeIndicator(0, placeNoteItem.placeImages.size)
        }
    }

    private fun makeIndicator(currentPos: Int, totalCount: Int) {
        binding.tvIndicator.text = context.getString(R.string.format_slash, currentPos + 1, totalCount)
    }
}