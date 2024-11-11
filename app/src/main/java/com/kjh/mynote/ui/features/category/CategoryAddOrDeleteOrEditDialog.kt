package com.kjh.mynote.ui.features.category

import android.os.Bundle
import androidx.core.view.isVisible
import com.kjh.mynote.R
import com.kjh.mynote.databinding.DialogAddOrDeleteOrEditCategoryBinding
import com.kjh.mynote.ui.base.BaseDialogFragment
import com.kjh.mynote.utils.extensions.onThrottleClick

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 8..
 * Description:
 */
class CategoryAddOrDeleteOrEditDialog: BaseDialogFragment<DialogAddOrDeleteOrEditCategoryBinding>({ DialogAddOrDeleteOrEditCategoryBinding.inflate(it) }) {

    init {
        isCancelable = false
    }

    private var posAction: (String) -> Unit = {}
    private var negAction: (() -> Unit)? = null

    override fun onInitView() {
        arguments?.let {
            val hideEditor = it.getBoolean(ARG_BOOL_HIDE_EDITOR)
            val title = it.getString(ARG_STR_TITLE) ?: ""
            val contents = it.getString(ARG_STR_CONTENTS) ?: ""
            val categoryName = it.getString(ARG_STR_CATEGORY_NAME)
            val noti = it.getString(ARG_STR_NOTI) ?: ""
            val posBtnText = it.getString(ARG_STR_POS_BTN_TEXT) ?: getString(R.string.confirm)
            val negBtnText = it.getString(ARG_STR_NEG_BTN_TEXT)

            setupEditor(hideEditor)
            setupTitle(title)
            setupContents(contents)
            setupCategoryName(categoryName)
            setupNoti(noti)
            setupPositiveButton(posBtnText)
            setupNegativeButton(negBtnText)
        }
    }

    override fun onInitData() {}

    private fun setupEditor(isHide: Boolean) {
        binding.etCategory.isVisible = !isHide
    }

    private fun setupTitle(title: String) {
        binding.tvTitle.text = title
    }

    private fun setupContents(contents: String) {
        binding.tvContents.isVisible = contents.isNotBlank()
        binding.tvContents.text = contents
    }

    private fun setupCategoryName(categoryName: String?) {
        if (categoryName.isNullOrBlank()) return

        binding.etCategory.setText(categoryName)
    }

    private fun setupNoti(text: String) {
        binding.tvNoti.isVisible = text.isNotBlank()
        binding.tvNoti.text = text
    }

    private fun setupPositiveButton(posBtnText: String) {
        binding.tvPositive.apply {
            text = posBtnText
            onThrottleClick {
                posAction.invoke(binding.etCategory.text.toString())
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
        const val TAG = "CategoryManagingDialog"

        private const val ARG_STR_TITLE = "ARG_TITLE"
        private const val ARG_STR_CONTENTS = "ARG_CONTENTS"
        private const val ARG_STR_CATEGORY_NAME = "ARG_CATEGORY_NAME"
        private const val ARG_STR_NOTI = "ARG_NOTI"
        private const val ARG_STR_POS_BTN_TEXT = "ARG_POS_BTN_TEXT"
        private const val ARG_STR_NEG_BTN_TEXT = "ARG_NEG_BTN_TEXT"
        private const val ARG_BOOL_HIDE_EDITOR = "ARG_BOOL_HIDE_EDITOR"

        fun newInstance(
            title: String = "",
            contents: String = "",
            categoryName: String = "",
            noti: String = "",
            hideEditor: Boolean = false,
            posBtnText: String = "",
            negBtnText: String = "",
            posAction: (String) -> Unit,
            negAction: (() -> Unit)? = null
        ): CategoryAddOrDeleteOrEditDialog = CategoryAddOrDeleteOrEditDialog().apply {
            arguments = Bundle().apply {
                putString(ARG_STR_TITLE, title)
                putString(ARG_STR_CONTENTS, contents)
                putString(ARG_STR_CATEGORY_NAME, categoryName)
                putString(ARG_STR_NOTI, noti)
                putString(ARG_STR_POS_BTN_TEXT, posBtnText)
                putString(ARG_STR_NEG_BTN_TEXT, negBtnText)
                putBoolean(ARG_BOOL_HIDE_EDITOR, hideEditor)
            }

            this.posAction = posAction
            this.negAction = negAction
        }
    }
}