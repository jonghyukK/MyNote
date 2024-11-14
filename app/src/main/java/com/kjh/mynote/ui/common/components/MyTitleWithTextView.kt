package com.kjh.mynote.ui.common.components

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.kjh.mynote.R
import com.kjh.mynote.databinding.ComponentsMyTitleWithTextViewBinding
import com.kjh.mynote.utils.extensions.setTextColorRes

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 13..
 * Description:
 */
class MyTitleWithTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ComponentsMyTitleWithTextViewBinding.inflate(LayoutInflater.from(context), this, true)

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
     *  Value Text Color
     */
    var textColor: Int = R.color.black_800
        set(value) {
            binding.tvValue.setTextColorRes(value)
            field = value
        }

    /**
     *  Value Text Gravity
     */
    var gravity: Int = Gravity.START
        set(value) {
            binding.tvValue.gravity = value
            field = value
        }

    /**
     *  textStyle
     */
    var typeFace: Int = Typeface.NORMAL
        set(value) {
            binding.tvValue.setTypeface(null, value)
            field = value
        }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyTitleWithTextView, defStyleAttr, 0)

        title = typedArray.getString(R.styleable.MyTitleWithTextView_title) ?: ""
        text = typedArray.getString(R.styleable.MyTitleWithTextView_android_text) ?: ""
        gravity = typedArray.getInteger(R.styleable.MyTitleWithTextView_android_gravity, Gravity.START)
        typeFace = typedArray.getInt(R.styleable.MyTitleWithTextView_android_textStyle, Typeface.NORMAL)

        typedArray.recycle()
    }
}