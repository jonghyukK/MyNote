package com.kjh.mynote.ui.common.components

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.kjh.mynote.R
import com.kjh.mynote.databinding.CommonLayoutMyToolBarBinding
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener

class MyToolBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    private val binding = CommonLayoutMyToolBarBinding.inflate(LayoutInflater.from(context), this, true)

    /**
     *  Left Back Button.
     */
    var isShowBackButton: Boolean = false
        set(value) {
            binding.ivBack.isVisible = value
            field = value
        }

    /**
     *  Left BackButton Tint.
     */
    var backButtonTint: Int = R.color.white
        set(value) {
            binding.ivBack.setColorFilter(value, PorterDuff.Mode.SRC_IN)
            field = value
        }

    /**
     *  Left Title.
     */
    var leftTitle: String? = null
        set(value) {
            binding.tvTitleLeft.isVisible = !value.isNullOrBlank()
            binding.tvTitleLeft.text = value
            field = value
        }

    /**
     *  Right More Button.
     */
    var isShowMoreButton: Boolean = false
        set(value) {
            binding.ivMore.isVisible = value
            field = value
        }

    /**
     *  Right MoreButton Tint.
     */
    var moreButtonTint: Int = R.color.white
        set(value) {
            binding.ivMore.setColorFilter(value)
            field = value
        }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyToolBar, defStyleAttr, 0)

        isShowBackButton = typedArray.getBoolean(R.styleable.MyToolBar_isShowBackButton, false)
        leftTitle = typedArray.getString(R.styleable.MyToolBar_leftTitle)
        backButtonTint = typedArray.getInteger(R.styleable.MyToolBar_backButtonTint, R.color.white)
        isShowMoreButton = typedArray.getBoolean(R.styleable.MyToolBar_isShowMoreButton, false)
        moreButtonTint = typedArray.getInteger(R.styleable.MyToolBar_moreButtonTint, R.color.white)

        typedArray.recycle()
    }

    /**
     *  Left Back Button ClickListener.
     */
    fun setBackButtonClickListener(listener: OnClickListener) {
        binding.ivBack.setOnThrottleClickListener(listener)
    }

    /**
     *  Right More Button ClickListener.
     */
    fun setMoreButtonClickListener(listener: OnClickListener) {
        binding.ivMore.setOnThrottleClickListener(listener)
    }
}