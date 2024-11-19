package com.kjh.mynote.ui.common.dialog

import android.content.Context
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
 *
 *  일반 대화상자형 Dialog
 */
class DefaultDialog
    : BaseDialogFragment<DialogMyDefaultBinding>({ DialogMyDefaultBinding.inflate(it) }) {

    private var eventListener: MyDefaultDialogEventListener? = null

    private var contents: String = ""
    private var posBtnText: String = ""
    private var negBtnText: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        eventListener = when {
            parentFragment is MyDefaultDialogEventListener -> parentFragment as MyDefaultDialogEventListener
            context is MyDefaultDialogEventListener -> context
            else -> throw IllegalStateException("Parent must implement MyDefaultDialogEventListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            contents = it.getString(ARG_CONTENTS) ?: ""
            posBtnText = it.getString(ARG_POS_BTN_TEXT) ?: getString(R.string.confirm)
            negBtnText = it.getString(ARG_NEG_BTN_TEXT)
        }
    }

    override fun onInitView() {
        with (binding) {
            tvContents.text = contents

            tvPositive.apply {
                text = posBtnText
                onThrottleClick {
                    eventListener?.onClickPositive()
                    dismiss()
                }
            }

            tvNegative.apply {
                isVisible = !negBtnText.isNullOrEmpty()
                text = negBtnText ?: ""
                onThrottleClick {
                    eventListener?.onClickNegative()
                    dismiss()
                }
            }
        }
    }

    override fun onInitData() {}

    override fun onDestroy() {
        super.onDestroy()
        eventListener = null
    }

    interface MyDefaultDialogEventListener {
        fun onClickPositive()
        fun onClickNegative()
    }

    companion object {
        const val TAG = "MyDefaultDialog"

        private const val ARG_CONTENTS = "ARG_CONTENTS"
        private const val ARG_POS_BTN_TEXT = "ARG_POS_BTN_TEXT"
        private const val ARG_NEG_BTN_TEXT = "ARG_NEG_BTN_TEXT"

        fun newInstance(
            contents: String,
            posBtnText: String = "",
            negBtnText: String = ""
        ): DefaultDialog = DefaultDialog().apply {
            arguments = Bundle().apply {
                putString(ARG_CONTENTS, contents)
                putString(ARG_POS_BTN_TEXT, posBtnText)
                putString(ARG_NEG_BTN_TEXT, negBtnText)
            }
        }
    }
}