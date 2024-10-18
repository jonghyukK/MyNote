package com.kjh.mynote.ui.features.viewer

import android.view.View.OnClickListener
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.kjh.mynote.R
import com.kjh.mynote.databinding.ActivityImagesViewerBinding
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 18..
 * Description:
 */

@AndroidEntryPoint
class ImagesViewerActivity
    : BaseActivity<ActivityImagesViewerBinding>({ ActivityImagesViewerBinding.inflate(it) }) {

    private var allImages: List<String> = emptyList()
    private var initImage: String = ""

    override fun onInitView() {
        binding.ivBack.setOnThrottleClickListener(backButtonClickListener)
    }

    override fun onInitUiData() {
        allImages = intent.getStringArrayListExtra(AppConstants.INTENT_IMAGE_LIST) ?: emptyList()
        initImage = intent.getStringExtra(AppConstants.INTENT_URL) ?: ""

        if (allImages.isEmpty() || initImage.isBlank()) {
            finish()
            return
        }

        onInitPagerAdapter()
    }

    private fun onInitPagerAdapter() {
        binding.vpPager.apply {
            adapter = ImagesViewerPagerAdapter(allImages)
            setCurrentItem(allImages.indexOf(initImage), false)
            registerOnPageChangeCallback(pageChangeCallback)
        }
    }

    override fun onDestroy() {
        binding.vpPager.unregisterOnPageChangeCallback(pageChangeCallback)
        super.onDestroy()
    }

    private val pageChangeCallback = object: OnPageChangeCallback() {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            binding.tvIndicator.text = getString(R.string.format_slash_with_space, position + 1, allImages.size)
        }
    }

    private val backButtonClickListener = OnClickListener {
        finish()
    }
}