package com.kjh.mynote.ui.features.place.detail

import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhPlaceNoteDetailItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.common.listener.OnNestedHorizontalTouchListener
import com.kjh.mynote.utils.CalendarUtils
import com.kjh.mynote.utils.extensions.onThrottleClick

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 20..
 * Description:
 */
class PlaceNoteDetailItemViewHolder(
    private val binding: VhPlaceNoteDetailItemBinding,
    private val imageViewerClickAction: (List<String>, String) -> Unit
): BaseViewHolder<PlaceNoteDetailUi.PlaceNoteDetailItem>(binding.root) {

    private var imagePagerAdapter = PlaceNoteDetailPagerAdapter(imageClickAction = {
        bindItem?.let { item ->
            imageViewerClickAction.invoke(item.placeNoteItem.placeImages, it)
        }
    })

    private var pageChangeCallback: OnPageChangeCallback = object: OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            bindItem?.let { item ->
                makeIndicator(position, item.placeNoteItem.placeImages.size)
            }
        }
    }

    init {
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
    }

    override fun bind(item: PlaceNoteDetailUi.PlaceNoteDetailItem) {
        super.bind(item)

        with (binding) {
            tvPlaceName.text = item.placeNoteItem.placeName
            tvAddress.text = item.placeNoteItem.placeAddress
            tvNoteContents.text = item.placeNoteItem.noteContents
            tvVisitDate.text = CalendarUtils.convertLongToDateFormat(
                timeInMillis = item.placeNoteItem.visitDate,
                format = "yyyy년 MM월 dd일 방문"
            )

            makeIndicator(vpPlaceImages.currentItem, item.placeNoteItem.placeImages.size)
        }

        imagePagerAdapter.submitList(item.placeNoteItem.placeImages)
    }

    private fun makeIndicator(currentPos: Int, totalCount: Int) {
        binding.tvIndicator.text = context.getString(R.string.format_slash, currentPos + 1, totalCount)
    }
}