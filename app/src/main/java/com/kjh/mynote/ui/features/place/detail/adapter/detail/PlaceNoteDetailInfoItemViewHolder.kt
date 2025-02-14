package com.kjh.mynote.ui.features.place.detail.adapter.detail

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhPlaceNoteDetailInfoItemBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.common.listener.OnNestedHorizontalTouchListener
import com.kjh.mynote.ui.features.place.detail.PlaceNoteDetailUiItemState
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.ifNullOrEmpty
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.toStringWithFormat

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 20..
 * Description:
 */


class PlaceNoteDetailInfoItemViewHolder(
    private val binding: VhPlaceNoteDetailInfoItemBinding,
    private val imageViewerClickAction: (List<String>, String) -> Unit,
    private val addressClickAction: (PlaceNoteUiModel) -> Unit
): BaseViewHolder<PlaceNoteDetailUiItemState.PlaceDetailInfoItem>(binding.root) {

    private var imagePagerAdapter = PlaceNoteDetailPagerAdapter(imageClickAction = {
        bindItem?.let { item ->
            imageViewerClickAction.invoke(item.placeNoteItem.placeImages, it)
        }
    })

    init {
        binding.vpPlaceImages.apply {
            adapter = imagePagerAdapter
            (getChildAt(0) as RecyclerView)
                .addOnItemTouchListener(OnNestedHorizontalTouchListener())
            registerOnPageChangeCallback(object: OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    bindItem?.let { item ->
                        makeIndicator(position, item.placeNoteItem.placeImages.size)
                    }
                }
            })
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

    override fun bind(item: PlaceNoteDetailUiItemState.PlaceDetailInfoItem) {
        super.bind(item)

        imagePagerAdapter.submitList(item.placeNoteItem.placeImages)

        with (binding) {
            vpPlaceImages.setCurrentItem(0, false)

            tvPlaceName.text = item.placeNoteItem.placeInfo.placeName
            tvAddress.text = item.placeNoteItem.placeInfo.roadAddress
                .ifNullOrEmpty(item.placeNoteItem.placeInfo.address)

            tvNoteContents.isVisible = item.placeNoteItem.noteContents.isNotEmpty()
            tvNoteContents.text = item.placeNoteItem.noteContents
            tvVisitDate.text = item.placeNoteItem.visitDate
                .toStringWithFormat(AppConstants.DATE_FORMAT_YYYY_M_D_E)

            makeIndicator(0, item.placeNoteItem.placeImages.size)
        }
    }

    private fun makeIndicator(currentPos: Int, totalCount: Int) {
        binding.tvIndicator.text = context.getString(R.string.format_slash, currentPos + 1, totalCount)
    }
}