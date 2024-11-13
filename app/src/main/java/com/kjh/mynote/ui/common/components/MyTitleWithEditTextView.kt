package com.kjh.mynote.ui.common.components

import android.content.Context
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.kjh.mynote.R
import com.kjh.mynote.databinding.ComponentsMyTitleWithEditTextViewBinding
import com.kjh.mynote.utils.extensions.hideKeyboard

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 13..
 * Description:
 */
class MyTitleWithEditTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ComponentsMyTitleWithEditTextViewBinding.inflate(LayoutInflater.from(context), this, true)

    private var myTextWatcher: TextWatcher? = null

    /**
     *  Title Text
     */
    var title: String = ""
        set(value) {
            binding.tvTitle.text = value
            field = value
        }

    /**
     *  Require Badge
     */
    var isRequired: Boolean = false
        set(value) {
            binding.tvRequired.isVisible = value
            field = value
        }

    /**
     *  Input Type
     */
    var inputType: Int = InputType.TYPE_CLASS_TEXT
        set(value) {
            binding.etInput.inputType = value
            field = value
        }

    /**
     *  ImeOption
     */
    var imeOption: Int = EditorInfo.IME_ACTION_DONE
        set(value) {
            binding.etInput.imeOptions = value
            field = value
        }

    /**
     *  Hint
     */
    var hint: String = ""
        set(value) {
            binding.etInput.hint = value
            field = value
        }

    /**
     *  EditText Text
     */
    var text: String = ""
        set(value) {
            binding.etInput.setText(value)
            field = value
        }
        get() = binding.etInput.text.toString()

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyTitleWithEditTextView, defStyleAttr, 0)

        title = typedArray.getString(R.styleable.MyTitleWithEditTextView_title) ?: ""
        isRequired = typedArray.getBoolean(R.styleable.MyTitleWithEditTextView_isRequired, false)
        inputType = typedArray.getInt(R.styleable.MyTitleWithEditTextView_android_inputType, InputType.TYPE_CLASS_TEXT)
        imeOption = typedArray.getInteger(R.styleable.MyTitleWithEditTextView_android_imeOptions, EditorInfo.IME_ACTION_DONE)
        hint = typedArray.getString(R.styleable.MyTitleWithEditTextView_android_hint) ?: ""

        typedArray.recycle()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        myTextWatcher?.let {
            binding.etInput.removeTextChangedListener(it)
        }
    }

    /**
     *  Add TextWatcher to EditText.
     */
    fun addMyTextWatcher(watcher: TextWatcher) {
        myTextWatcher = watcher
        binding.etInput.addTextChangedListener(myTextWatcher)
    }

    /**
     *  Remove TextWatcher.
     */
    fun removeMyTextWatcher() {
        binding.etInput.removeTextChangedListener(myTextWatcher)
        myTextWatcher = null
    }

    fun setSelection(index: Int) {
        binding.etInput.setSelection(index)
    }

    fun hideKeyboard() {
        binding.etInput.hideKeyboard()
    }
}