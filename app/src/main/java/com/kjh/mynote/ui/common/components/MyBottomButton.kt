package com.kjh.mynote.ui.common.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.kjh.mynote.R
import com.kjh.mynote.databinding.CommonLayoutMyBottomButtonBinding

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 11..
 * Description:
*/

class MyBottomButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    private val binding = CommonLayoutMyBottomButtonBinding.inflate(LayoutInflater.from(context), this, true)

    /**
     *  Button Title..
     */
    var btnTitle: String = ""
        set(value) {
            binding.tvBtnTitle.text = value
            field = value
        }

    /**
     *  Button Enabled ..
     */
    var isEnable: Boolean = false
        set(value) {
            binding.clBottomBtnContainer.background = if (value) {
                ContextCompat.getDrawable(context, R.drawable.ripple_shape_s_purple_c_8)
            } else {
                ContextCompat.getDrawable(context, R.drawable.shape_s_black_500_c_8)
            }
            field = value
        }

    /**
     *  isLoading ..
     */
    var isLoading: Boolean = false
        set(value) {
            binding.cpiLoading.isVisible = value
            binding.tvBtnTitle.isVisible = !value
            field = value
        }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyBottomButton, defStyleAttr, 0)

        btnTitle = typedArray.getString(R.styleable.MyBottomButton_btnTitle) ?: ""
        isEnable = typedArray.getBoolean(R.styleable.MyBottomButton_isEnable, false)
        isLoading = typedArray.getBoolean(R.styleable.MyBottomButton_isLoading, false)

        typedArray.recycle()
    }
}