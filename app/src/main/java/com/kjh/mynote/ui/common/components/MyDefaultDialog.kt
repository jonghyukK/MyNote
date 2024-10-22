package com.kjh.mynote.ui.common.components

import android.os.Bundle
import androidx.core.view.isVisible
import com.kjh.mynote.R
import com.kjh.mynote.databinding.DialogMyDefaultBinding
import com.kjh.mynote.ui.base.BaseDialogFragment
import com.kjh.mynote.utils.extensions.onThrottleClick

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 22..
 * Description:
 */
class MyDefaultDialog
    : BaseDialogFragment<DialogMyDefaultBinding>({ DialogMyDefaultBinding.inflate(it) }) {

    private var posAction: () -> Unit = {}
    private var negAction: (() -> Unit)? = null

    override fun onInitView() {
        arguments?.let {
            val contents = it.getString(ARG_CONTENTS) ?: ""
            val posBtnText = it.getString(ARG_POS_BTN_TEXT) ?: getString(R.string.confirm)
            val negBtnText = it.getString(ARG_NEG_BTN_TEXT)

            binding.tvContents.text = contents
            setupPositiveButton(posBtnText)
            setupNegativeButton(negBtnText)
        }
    }

    override fun onInitData() {}

    private fun setupPositiveButton(posBtnText: String) {
        binding.tvPositive.apply {
            text = posBtnText
            onThrottleClick {
                posAction.invoke()
                dismiss()
            }
        }
    }

    private fun setupNegativeButton(negBtnText: String?) {
        if (negBtnText.isNullOrBlank()) {
            binding.tvNegative.isVisible = false
            return
        }

        binding.tvNegative.apply {
            isVisible = true
            text = negBtnText
            onThrottleClick {
                negAction?.invoke()
                dismiss()
            }
        }

    }

    companion object {
        const val TAG = "MyDefaultDialog"

        private const val ARG_CONTENTS = "ARG_CONTENTS"
        private const val ARG_POS_BTN_TEXT = "ARG_POS_BTN_TEXT"
        private const val ARG_NEG_BTN_TEXT = "ARG_NEG_BTN_TEXT"

        fun newInstance(
            contents: String,
            posBtnText: String = "",
            negBtnText: String = "",
            posAction: () -> Unit,
            negAction: (() -> Unit)? = null
        ): MyDefaultDialog = MyDefaultDialog().apply {
            arguments = Bundle().apply {
                putString(ARG_CONTENTS, contents)
                putString(ARG_POS_BTN_TEXT, posBtnText)
                putString(ARG_NEG_BTN_TEXT, negBtnText)
            }

            this.posAction = posAction
            this.negAction = negAction
        }
    }
}