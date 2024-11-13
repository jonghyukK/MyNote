package com.kjh.mynote.ui.common.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.kjh.mynote.R
import com.kjh.mynote.databinding.ComponentsMyTitleWithSelectableTextViewBinding
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.setTextColorRes

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 13..
 * Description:
 */
class MyTitleWithSelectableTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ComponentsMyTitleWithSelectableTextViewBinding.inflate(LayoutInflater.from(context), this, true)

    /**
     *  Title
     */
    var title: String = ""
        set(value) {
            binding.tvTitle.text = value
            field = value
        }
        get() = binding.tvTitle.text.toString()

    /**
     *  Text
     */
    var text: String = ""
        set(value) {
            binding.tvValue.text = value
            field = value
        }
        get() = binding.tvValue.text.toString()

    /**
     *  Require Badge
     */
    var isRequired: Boolean = false
        set(value) {
            binding.tvRequired.isVisible = value
            field = value
        }

    /**
     *  Value Text Color
     */
    var textColor: Int = R.color.black_800
        set(value) {
            binding.tvValue.setTextColorRes(value)
            field = value
        }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyTitleWithSelectableTextView, defStyleAttr, 0)

        title = typedArray.getString(R.styleable.MyTitleWithSelectableTextView_title) ?: ""
        text = typedArray.getString(R.styleable.MyTitleWithSelectableTextView_android_text) ?: ""
        isRequired = typedArray.getBoolean(R.styleable.MyTitleWithSelectableTextView_isRequired,false)

        typedArray.recycle()
    }

    fun setTextClickListener(listener: OnClickListener) {
        binding.clTextContainer.setOnThrottleClickListener(listener)
    }
}