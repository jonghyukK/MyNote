package com.kjh.mynote.ui.features.place.detail.adapter

import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.kjh.data.model.PlaceNoteModel
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhPlaceNoteDetailItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.common.listener.OnNestedHorizontalTouchListener
import com.kjh.mynote.ui.features.place.detail.PlaceNoteDetailUi
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.toStringWithFormat
import timber.log.Timber

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 20..
 * Description:
 */
class PlaceNoteDetailItemViewHolder(
    private val binding: VhPlaceNoteDetailItemBinding,
    private val imageViewerClickAction: (List<String>, String) -> Unit,
    private val addressClickAction: (PlaceNoteModel) -> Unit
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

        imagePagerAdapter?.submitList(item.placeNoteItem.placeImages)

        with (binding) {
            vpPlaceImages.setCurrentItem(0, false)

            tvPlaceName.text = item.placeNoteItem.placeName
            tvAddress.text = item.placeNoteItem.placeAddress
            tvNoteContents.text = item.placeNoteItem.noteContents
            tvVisitDate.text = item.placeNoteItem.visitDate.toStringWithFormat("yyyy년 MM월 dd일 (E)")

            makeIndicator(0, item.placeNoteItem.placeImages.size)
        }
    }

    private fun makeIndicator(currentPos: Int, totalCount: Int) {
        binding.tvIndicator.text = context.getString(R.string.format_slash, currentPos + 1, totalCount)
    }
}