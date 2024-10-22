package com.kjh.mynote.ui.features.place.calendar

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.kjh.data.model.PlaceNoteModel
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhPlaceNoteInCalendarFourPictureItemBinding
import com.kjh.mynote.databinding.VhPlaceNoteInCalendarOnePictureItemBinding
import com.kjh.mynote.databinding.VhPlaceNoteInCalendarOverPictureItemBinding
import com.kjh.mynote.databinding.VhPlaceNoteInCalendarThreePictureItemBinding
import com.kjh.mynote.databinding.VhPlaceNoteInCalendarTwoPictureItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.extensions.loadImage
import com.kjh.mynote.utils.extensions.makeGone
import com.kjh.mynote.utils.extensions.makeVisible
import com.kjh.mynote.utils.extensions.onThrottleClick

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 17..
 * Description:
 */


class CalendarPlaceNoteOnePictureItemViewHolder(
    private val binding: VhPlaceNoteInCalendarOnePictureItemBinding,
    private val placeClickAction: (PlaceNoteModel) -> Unit,
    private val imageClickAction: (List<String>, String) -> Unit
): BaseViewHolder<CalendarPlaceNoteUiState.OnePicturePlaceNoteItem>(binding.root) {

    init {
        itemView.onThrottleClick {
            bindItem?.let { item -> placeClickAction.invoke(item.item) }
        }

        binding.ivImage1.onThrottleClick {
            setAnimWhenClicked(binding.ivImage1)
            bindItem?.let { item -> imageClickAction.invoke(item.item.placeImages, item.item.placeImages[0])}
        }
    }

    override fun bind(item: CalendarPlaceNoteUiState.OnePicturePlaceNoteItem) {
        super.bind(item)

        with (binding) {
            ivImage1.loadImage(item.item.placeImages[0])

            layoutPlaceContents.tvPlaceName.text = item.item.placeName
            layoutPlaceContents.tvPlaceArea.text = item.item.placeRegion
            layoutPlaceContents.tvContents.text = item.item.noteContents
        }
    }
}

class CalendarPlaceNoteTwoPictureItemViewHolder(
    private val binding: VhPlaceNoteInCalendarTwoPictureItemBinding,
    private val placeClickAction: (PlaceNoteModel) -> Unit,
    private val imageClickAction: (List<String>, String) -> Unit
): BaseViewHolder<CalendarPlaceNoteUiState.TwoPicturePlaceNoteItem>(binding.root) {

    init {
        itemView.onThrottleClick {
            bindItem?.let { item -> placeClickAction.invoke(item.item) }
        }

        binding.ivImage1.onThrottleClick {
            setAnimWhenClicked(binding.ivImage1)
            bindItem?.let { item -> imageClickAction.invoke(item.item.placeImages, item.item.placeImages[0])}
        }

        binding.ivImage2.onThrottleClick {
            setAnimWhenClicked(binding.ivImage2)
            bindItem?.let { item -> imageClickAction.invoke(item.item.placeImages, item.item.placeImages[1])}
        }
    }

    override fun bind(item: CalendarPlaceNoteUiState.TwoPicturePlaceNoteItem) {
        super.bind(item)

        with (binding) {
            ivImage1.loadImage(item.item.placeImages[0])
            ivImage2.loadImage(item.item.placeImages[1])

            layoutPlaceContents.tvPlaceName.text = item.item.placeName
            layoutPlaceContents.tvPlaceArea.text = item.item.placeRegion
            layoutPlaceContents.tvContents.text = item.item.noteContents
        }
    }
}

class CalendarPlaceNoteThreePictureItemViewHolder(
    private val binding: VhPlaceNoteInCalendarThreePictureItemBinding,
    private val placeClickAction: (PlaceNoteModel) -> Unit,
    private val imageClickAction: (List<String>, String) -> Unit
): BaseViewHolder<CalendarPlaceNoteUiState.ThreePicturePlaceNoteItem>(binding.root) {

    init {
        itemView.onThrottleClick {
            bindItem?.let { item -> placeClickAction.invoke(item.item) }
        }

        binding.ivImage1.onThrottleClick {
            setAnimWhenClicked(binding.ivImage1)
            bindItem?.let { item -> imageClickAction.invoke(item.item.placeImages, item.item.placeImages[0])}
        }

        binding.ivImage2.onThrottleClick {
            setAnimWhenClicked(binding.ivImage2)
            bindItem?.let { item -> imageClickAction.invoke(item.item.placeImages, item.item.placeImages[1])}
        }

        binding.ivImage3.onThrottleClick {
            setAnimWhenClicked(binding.ivImage3)
            bindItem?.let { item -> imageClickAction.invoke(item.item.placeImages, item.item.placeImages[2])}
        }
    }

    override fun bind(item: CalendarPlaceNoteUiState.ThreePicturePlaceNoteItem) {
        super.bind(item)

        with (binding) {
            ivImage1.loadImage(item.item.placeImages[0])
            ivImage2.loadImage(item.item.placeImages[1])
            ivImage3.loadImage(item.item.placeImages[2])

            layoutPlaceContents.tvPlaceName.text = item.item.placeName
            layoutPlaceContents.tvPlaceArea.text = item.item.placeRegion
            layoutPlaceContents.tvContents.text = item.item.noteContents
        }
    }
}

class CalendarPlaceNoteFourPictureItemViewHolder(
    private val binding: VhPlaceNoteInCalendarFourPictureItemBinding,
    private val placeClickAction: (PlaceNoteModel) -> Unit,
    private val imageClickAction: (List<String>, String) -> Unit
): BaseViewHolder<CalendarPlaceNoteUiState.FourPicturePlaceNoteItem>(binding.root) {

    init {
        itemView.onThrottleClick {
            bindItem?.let { item -> placeClickAction.invoke(item.item) }
        }

        binding.ivImage1.onThrottleClick {
            setAnimWhenClicked(binding.ivImage1)
            bindItem?.let { item -> imageClickAction.invoke(item.item.placeImages, item.item.placeImages[0])}
        }

        binding.ivImage2.onThrottleClick {
            setAnimWhenClicked(binding.ivImage2)
            bindItem?.let { item -> imageClickAction.invoke(item.item.placeImages, item.item.placeImages[1])}
        }

        binding.ivImage3.onThrottleClick {
            setAnimWhenClicked(binding.ivImage3)
            bindItem?.let { item -> imageClickAction.invoke(item.item.placeImages, item.item.placeImages[2])}
        }

        binding.ivImage4.onThrottleClick {
            setAnimWhenClicked(binding.ivImage4)
            bindItem?.let { item -> imageClickAction.invoke(item.item.placeImages, item.item.placeImages[3])}
        }
    }

    override fun bind(item: CalendarPlaceNoteUiState.FourPicturePlaceNoteItem) {
        super.bind(item)

        with (binding) {
            ivImage1.loadImage(item.item.placeImages[0])
            ivImage2.loadImage(item.item.placeImages[1])
            ivImage3.loadImage(item.item.placeImages[2])
            ivImage4.loadImage(item.item.placeImages[3])

            layoutPlaceContents.tvPlaceName.text = item.item.placeName
            layoutPlaceContents.tvPlaceArea.text = item.item.placeRegion
            layoutPlaceContents.tvContents.text = item.item.noteContents
        }
    }
}


class CalendarPlaceNoteOverPictureItemViewHolder(
    private val binding: VhPlaceNoteInCalendarOverPictureItemBinding,
    private val placeClickAction: (PlaceNoteModel) -> Unit,
    private val imageClickAction: (List<String>, String) -> Unit
): BaseViewHolder<CalendarPlaceNoteUiState.OverPicturePlaceNoteItem>(binding.root) {

    init {
        itemView.onThrottleClick {
            bindItem?.let { item -> placeClickAction.invoke(item.item) }
        }

        binding.ivImage1.onThrottleClick {
            setAnimWhenClicked(binding.ivImage1)
            bindItem?.let { item -> imageClickAction.invoke(item.item.placeImages, item.item.placeImages[0])}
        }

        binding.ivImage2.onThrottleClick {
            setAnimWhenClicked(binding.ivImage2)
            bindItem?.let { item -> imageClickAction.invoke(item.item.placeImages, item.item.placeImages[1])}
        }

        binding.ivImage3.onThrottleClick {
            setAnimWhenClicked(binding.ivImage3)
            bindItem?.let { item -> imageClickAction.invoke(item.item.placeImages, item.item.placeImages[2])}
        }

        binding.ivImage4.onThrottleClick {
            setAnimWhenClicked(binding.ivImage4)
            bindItem?.let { item -> imageClickAction.invoke(item.item.placeImages, item.item.placeImages[3])}
        }

        binding.ivImage5.onThrottleClick {
            setAnimWhenClicked(binding.ivImage5)
            bindItem?.let { item -> imageClickAction.invoke(item.item.placeImages, item.item.placeImages[4])}
        }
    }

    override fun bind(item: CalendarPlaceNoteUiState.OverPicturePlaceNoteItem) {
        super.bind(item)

        with (binding) {
            ivImage1.loadImage(item.item.placeImages[0])
            ivImage2.loadImage(item.item.placeImages[1])
            ivImage3.loadImage(item.item.placeImages[2])
            ivImage4.loadImage(item.item.placeImages[3])
            ivImage5.loadImage(item.item.placeImages[4])

            layoutPlaceContents.tvPlaceName.text = item.item.placeName
            layoutPlaceContents.tvPlaceArea.text = item.item.placeRegion
            layoutPlaceContents.tvContents.text = item.item.noteContents

            groupOver.isVisible = item.remainImgCount > 0
            tvRemainImgCount.text = context.getString(R.string.format_plus_value, item.remainImgCount)
        }
    }
}


private fun setAnimWhenClicked(view: View) {
    val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 0.95f).setDuration(50)
    val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 0.95f).setDuration(50)

    val scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1f).setDuration(50)
    val scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1f).setDuration(50)

    val scaleDown = AnimatorSet().apply {
        playTogether(scaleDownX, scaleDownY)
    }

    val scaleUp = AnimatorSet().apply {
        playTogether(scaleUpX, scaleUpY)
    }

    val animation = AnimatorSet().apply {
        playSequentially(scaleDown, scaleUp)
    }

    animation.start()
}