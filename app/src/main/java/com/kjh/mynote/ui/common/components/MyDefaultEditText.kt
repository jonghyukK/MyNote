package com.kjh.mynote.ui.common.components

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.kjh.mynote.R
import com.kjh.mynote.databinding.ComponentsMyDefaultEditTextBinding
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.showKeyboard

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 29..
 * Description:
 */
class MyDefaultEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ComponentsMyDefaultEditTextBinding.inflate(LayoutInflater.from(context), this, true)

    private var customTextWatcher: TextWatcher? = null

    var text: String = ""
        set(value) {
            binding.etText.setText(value)
            field = value
        }
        get() = binding.etText.text.toString()

    var inputType: Int = InputType.TYPE_CLASS_TEXT
        set(value) {
            binding.etText.inputType = value
            field = value
        }

    var imeOption: Int = EditorInfo.IME_ACTION_DONE
        set(value) {
            binding.etText.imeOptions = value
            field = value
        }

    var hint: String = ""
        set(value) {
            binding.etText.hint = value
            field = value
        }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyDefaultEditText, defStyleAttr, 0)

        inputType = typedArray.getInt(R.styleable.MyDefaultEditText_android_inputType, InputType.TYPE_CLASS_TEXT)
        imeOption = typedArray.getInteger(R.styleable.MyDefaultEditText_android_imeOptions, EditorInfo.IME_ACTION_DONE)
        hint = typedArray.getString(R.styleable.MyDefaultEditText_android_hint) ?: ""

        initializeListeners()

        typedArray.recycle()
    }

    private fun initializeListeners() {
        val internalTextWatcher = object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.ivClear.isVisible = text.isNotEmpty()
            }
        }

        binding.etText.addTextChangedListener(internalTextWatcher)
        binding.ivClear.onThrottleClick {
            binding.etText.text?.clear()
        }
    }

    /**
     * Add CustomTextWatcher to EditText.
     *
     * @param watcher
     */
    fun addCustomTextWatcher(watcher: TextWatcher) {
        customTextWatcher = watcher
        binding.etText.addTextChangedListener(customTextWatcher)
    }

    /**
     * Remove CustomTextWatcher from EditText.
     *
     */
    fun removeCustomTextWatcher() {
        customTextWatcher?.let {
            binding.etText.removeTextChangedListener(customTextWatcher)
        }
        customTextWatcher = null
    }

    /**
     * setClickListener to ivClear Button.
     *
     * @param listener
     */
    fun setClearButtonClickListener(listener: OnClickListener) {
        binding.ivClear.setOnThrottleClickListener(listener)
    }

    /**
     * setSelection To EditText.
     *
     * @param index
     */
    fun setSelection(index: Int) {
        binding.etText.setSelection(index)
    }

    /**
     * requestFocus to EditText.
     *
     */
    fun setFocus() = with (binding.etText) {
        val textLength = text?.length ?: 0

        showKeyboard()
        setSelection(textLength)
    }
}