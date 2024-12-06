package com.kjh.mynote.ui.common.components

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.kjh.mynote.R
import com.kjh.mynote.databinding.CommonLayoutMyToolBarBinding
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.setBackgroundRes
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
    var backButtonTint: Int = R.color.black_900
        set(value) {
            binding.ivBack.setColorFilter(value, PorterDuff.Mode.SRC_IN)
            field = value
        }

    /**
     *  Left BackButton Background.
     */
    var backButtonBg: Int = -1
        set(value) {
            if (value > -1) {
                binding.ivBack.setBackgroundRes(value)
            }
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

    /**
     *  Right MoreButton Background.
     */
    var moreButtonBg: Int = -1
        set(value) {
            if (value > -1) {
                binding.ivMore.setBackgroundRes(value)
            }
            field = value
        }

    /**
     *  Right First Image
     */
    var rightFirstImage: Drawable? = null
        set(value) {
            binding.ivRightFirst.isVisible = value != null
            value?.let {
                binding.ivRightFirst.setImageDrawable(it)
            }
            field = value
        }

    /**
     *  Right Second Image
     */
    var rightSecondImage: Drawable? = null
        set(value) {
            binding.ivRightSecond.isVisible = value != null
            value?.let {
                binding.ivRightSecond.setImageDrawable(it)
            }
            field = value
        }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyToolBar, defStyleAttr, 0)

        isShowBackButton = typedArray.getBoolean(R.styleable.MyToolBar_isShowBackButton, false)
        leftTitle = typedArray.getString(R.styleable.MyToolBar_leftTitle)
        backButtonTint = typedArray.getColor(R.styleable.MyToolBar_backButtonTint, ContextCompat.getColor(context, R.color.black_900))
        backButtonBg = typedArray.getResourceId(R.styleable.MyToolBar_backButtonBg, R.drawable.ripple_white)
        isShowMoreButton = typedArray.getBoolean(R.styleable.MyToolBar_isShowMoreButton, false)
        moreButtonTint = typedArray.getColor(R.styleable.MyToolBar_moreButtonTint, ContextCompat.getColor(context, R.color.black_900))
        moreButtonBg = typedArray.getResourceId(R.styleable.MyToolBar_moreButtonBg, R.drawable.ripple_white)
        rightFirstImage = typedArray.getDrawable(R.styleable.MyToolBar_rightFirstImage)
        rightSecondImage = typedArray.getDrawable(R.styleable.MyToolBar_rightSecondImage)

        if (context is AppCompatActivity && !binding.ivBack.hasOnClickListeners()) {
            binding.ivBack.onThrottleClick { context.onBackPressed() }
        }

        typedArray.recycle()
    }

    /**
     *  Right More Button ClickListener.
     */
    fun setMoreButtonClickListener(listener: OnClickListener) {
        binding.ivMore.setOnThrottleClickListener(listener)
    }

    /**
     *  Right First Button ClickListener.
     */
    fun setRightFirstButtonClickListener(listener: OnClickListener) {
        binding.ivRightFirst.setOnThrottleClickListener(listener)
    }

    /**
     *  Right Second Button ClickListener.
     */
    fun setRightSecondButtonClickListener(listener: OnClickListener) {
        binding.ivRightSecond.setOnThrottleClickListener(listener)
    }
}