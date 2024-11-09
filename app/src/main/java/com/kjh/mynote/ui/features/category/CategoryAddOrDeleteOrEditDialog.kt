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

    private var posAction: (String) -> Unit = {}
    private var negAction: (() -> Unit)? = null

    override fun onInitView() {
        arguments?.let {
            val contents = it.getString(ARG_CONTENTS) ?: ""
            val categoryName = it.getString(ARG_CATEGORY_NAME)
            val posBtnText = it.getString(ARG_POS_BTN_TEXT) ?: getString(R.string.confirm)
            val negBtnText = it.getString(ARG_NEG_BTN_TEXT)

            setupContents(contents)
            setupCategoryName(categoryName)
            setupPositiveButton(posBtnText)
            setupNegativeButton(negBtnText)
        }
    }

    override fun onInitData() {}

    private fun setupContents(contents: String) {
        binding.tvContents.text = contents
    }

    private fun setupCategoryName(categoryName: String?) {
        if (categoryName.isNullOrBlank()) return

        binding.etCategory.setText(categoryName)
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

        private const val ARG_CONTENTS = "ARG_CONTENTS"
        private const val ARG_CATEGORY_NAME = "ARG_CATEGORY_NAME"
        private const val ARG_POS_BTN_TEXT = "ARG_POS_BTN_TEXT"
        private const val ARG_NEG_BTN_TEXT = "ARG_NEG_BTN_TEXT"

        fun newInstance(
            contents: String = "",
            categoryName: String = "",
            posBtnText: String = "",
            negBtnText: String = "",
            posAction: (String) -> Unit,
            negAction: (() -> Unit)? = null
        ): CategoryAddOrDeleteOrEditDialog = CategoryAddOrDeleteOrEditDialog().apply {
            arguments = Bundle().apply {
                putString(ARG_CONTENTS, contents)
                putString(ARG_CATEGORY_NAME, categoryName)
                putString(ARG_POS_BTN_TEXT, posBtnText)
                putString(ARG_NEG_BTN_TEXT, negBtnText)
            }

            this.posAction = posAction
            this.negAction = negAction
        }
    }
}