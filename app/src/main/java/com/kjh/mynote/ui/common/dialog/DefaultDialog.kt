package com.kjh.mynote.ui.common.dialog

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.annotation.ColorRes
import androidx.core.view.isVisible
import com.kjh.mynote.R
import com.kjh.mynote.databinding.DialogMyDefaultBinding
import com.kjh.mynote.ui.base.BaseDialogFragment
import com.kjh.mynote.ui.base.DialogType
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.setTextColorRes
import timber.log.Timber

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 22..
 * Description:
 *
 *  일반 대화상자형 Dialog
 */
class DefaultDialog
    : BaseDialogFragment<DialogMyDefaultBinding>({ DialogMyDefaultBinding.inflate(it) }, dialogType = DialogType.DIALOG) {

    private var eventListener: MyDefaultDialogEventListener? = null

    private var title: String? = null
    private var contents: String? = null
    private var description: String? = null
    private var descColorRes: Int = R.color.black_600
    private var posBtnText: String? = null
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
            title = it.getString(ARG_STR_TITLE)
            contents = it.getString(ARG_STR_CONTENTS)
            description = it.getString(ARG_STR_DESC)
            descColorRes = it.getInt(ARG_INT_DESC_COLOR)
            posBtnText = it.getString(ARG_POS_BTN_TEXT)
            negBtnText = it.getString(ARG_NEG_BTN_TEXT)
        }
    }

    override fun onInitView() {
        setupTitle()
        setupContents()
        setupDescription()
        setupPositiveBtnText()
        setupNegativeBtnText()
    }

    private fun setupTitle() = with (binding) {
        tvTitle.isVisible = !title.isNullOrBlank()
        tvTitle.text = title
    }

    private fun setupContents() = with (binding) {
        tvContents.isVisible = !contents.isNullOrBlank()
        tvContents.text = contents
    }

    private fun setupDescription() = with (binding) {
        tvDesc.isVisible = !description.isNullOrBlank()
        tvDesc.text = description
        tvDesc.setTextColorRes(descColorRes)
    }

    private fun setupPositiveBtnText() = with (binding) {
        tvPositive.apply {
            text = posBtnText ?: getString(R.string.confirm)
            onThrottleClick {
                eventListener?.onDialogPositiveClick()
                dismiss()
            }
        }
    }

    private fun setupNegativeBtnText() = with (binding) {
        tvNegative.apply {
            isVisible = !negBtnText.isNullOrEmpty()
            text = negBtnText
            onThrottleClick {
                eventListener?.onDialogNegativeClick()
                dismiss()
            }
        }
    }

    override fun onInitData() {}

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        eventListener?.onDialogDismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        eventListener = null
    }

    interface MyDefaultDialogEventListener {
        fun onDialogPositiveClick()
        fun onDialogNegativeClick()
        fun onDialogDismiss()
    }

    companion object {
        const val TAG = "MyDefaultDialog"

        private const val ARG_STR_TITLE = "ARG_STR_TITLE"
        private const val ARG_STR_CONTENTS = "ARG_STR_CONTENTS"
        private const val ARG_STR_DESC = "ARG_STR_DESC"
        private const val ARG_INT_DESC_COLOR = "ARG_INT_DESC_COLOR"
        private const val ARG_POS_BTN_TEXT = "ARG_POS_BTN_TEXT"
        private const val ARG_NEG_BTN_TEXT = "ARG_NEG_BTN_TEXT"

        fun newInstance(
            title: String? = null,
            contents: String? = null,
            desc: String? = null,
            @ColorRes descColorRes: Int = R.color.black_600,
            posBtnText: String? = null,
            negBtnText: String? = null
        ): DefaultDialog = DefaultDialog().apply {
            arguments = Bundle().apply {
                putString(ARG_STR_TITLE, title)
                putString(ARG_STR_CONTENTS, contents)
                putString(ARG_STR_DESC, desc)
                putInt(ARG_INT_DESC_COLOR, descColorRes)
                putString(ARG_POS_BTN_TEXT, posBtnText)
                putString(ARG_NEG_BTN_TEXT, negBtnText)
            }
        }
    }
}