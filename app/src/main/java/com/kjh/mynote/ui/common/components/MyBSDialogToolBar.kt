package com.kjh.mynote.ui.common.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.kjh.mynote.R
import com.kjh.mynote.databinding.ComponentsMyBsDialogToolBarBinding
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 13..
 * Description:
 */
class MyBSDialogToolBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ComponentsMyBsDialogToolBarBinding.inflate(LayoutInflater.from(context), this, true)

    var title: String? = null
        set(value) {
            binding.tvTitle.text = value
            field = value
        }

    var showResetBtn: Boolean = true
        set(value) {
            binding.clResetContainer.isVisible = value
            field = value
        }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyBSDialogToolBar, defStyleAttr, 0)

        title = typedArray.getString(R.styleable.MyBSDialogToolBar_title)
        showResetBtn = typedArray.getBoolean(R.styleable.MyBSDialogToolBar_showResetBtn, true)

        typedArray.recycle()
    }

    fun setResetClickListener(listener: View.OnClickListener) {
        binding.clResetContainer.setOnThrottleClickListener(listener)
    }

    fun setCloseClickListener(listener: View.OnClickListener) {
        binding.ivClose.setOnThrottleClickListener(listener)
    }
}