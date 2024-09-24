package com.example.mynote.ui.common.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.example.mynote.R
import com.example.mynote.databinding.CommonLayoutMyToolBarBinding
import com.example.mynote.utils.extensions.setOnThrottleClickListener

class MyToolBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    private val binding = CommonLayoutMyToolBarBinding.inflate(LayoutInflater.from(context), this, true)

    /**
     *  Left Back Button.
     */
    private var isShowBackButton: Boolean = false
        set(value) {
            binding.ivBack.isVisible = value
            field = value
        }

    /**
     *  Left Title.
     */
    private var leftTitle: String? = null
        set(value) {
            binding.tvTitleLeft.isVisible = !value.isNullOrBlank()
            binding.tvTitleLeft.text = value
            field = value
        }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyToolBar, defStyleAttr, 0)

        isShowBackButton = typedArray.getBoolean(R.styleable.MyToolBar_isShowBackButton, false)
        leftTitle = typedArray.getString(R.styleable.MyToolBar_leftTitle)

        typedArray.recycle()
    }

    /**
     *  좌측 백 버튼 ClickListener
     */
    fun setBackButtonClickListener(listener: OnClickListener) {
        binding.ivBack.setOnThrottleClickListener(listener)
    }
}