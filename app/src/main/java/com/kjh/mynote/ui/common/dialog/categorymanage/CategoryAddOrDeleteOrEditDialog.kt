package com.kjh.mynote.ui.common.dialog.categorymanage

import android.content.Context
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.R
import com.kjh.mynote.databinding.DialogAddOrDeleteOrEditCategoryBinding
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.UiState
import com.kjh.mynote.ui.base.BaseDialogFragment
import com.kjh.mynote.ui.base.DialogType
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 8..
 * Description:
 */

@AndroidEntryPoint
class CategoryAddOrDeleteOrEditDialog :
    BaseDialogFragment<DialogAddOrDeleteOrEditCategoryBinding>(
        { DialogAddOrDeleteOrEditCategoryBinding.inflate(it) },
        dialogType = DialogType.DIALOG
    ) {

    private var eventCallback: CategoryManageEventCallback? = null

    private val viewModel: CategoryAddOrDeleteOrEditDialogViewModel by viewModels()

    init {
        isCancelable = false
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        eventCallback = when {
            parentFragment is CategoryManageEventCallback -> parentFragment as CategoryManageEventCallback
            context is CategoryManageEventCallback -> context
            else -> throw IllegalStateException("Parent must implement CategoryManageEventCallback")
        }
    }

    override fun onInitView() {}

    override fun onInitData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.dialogType.collect { dialogType ->
                        when (dialogType) {
                            CategoryDialogType.ADD -> setupCategoryAddUI()
                            CategoryDialogType.MODIFY -> setupCategoryModifyUI()
                            CategoryDialogType.DELETE -> setupCategoryDeleteUI()
                        }
                    }
                }

                launch {
                    viewModel.makeCategoryEventState.collect { eventState ->
                        when (eventState) {
                            is UiState.Loading -> {}
                            is UiState.Error -> {
                                showToast(eventState.errorMsg)
                            }
                            is UiState.Success -> {
                                eventCallback?.addEventCallback()
                                dismiss()
                            }
                            else -> {}
                        }
                    }
                }

                launch {
                    viewModel.updateCategoryNameEventState.collect { eventState ->
                        when (eventState) {
                            is UiState.Loading -> {}
                            is UiState.Error -> {
                                showToast(eventState.errorMsg)
                            }
                            is UiState.Success -> {
                                eventCallback?.editEventCallback(eventState.data)
                                dismiss()
                            }
                            else -> {}
                        }
                    }
                }

                launch {
                    viewModel.deleteCategoryEventState.collect { eventState ->
                        when (eventState) {
                            is UiState.Loading -> {}
                            is UiState.Error -> {
                                showToast(eventState.errorMsg)
                            }
                            is UiState.Success -> {
                                eventCallback?.deleteEventCallback(eventState.data)
                                dismiss()
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun isValidCategoryName(): Boolean {
        if (binding.etCategory.text.toString().isBlank()) {
            showToast(getString(R.string.title_input_category_for_add))
            return false
        } else {
            return true
        }
    }

    /**
     *  카테고리 추가 UI..
     */
    private fun setupCategoryAddUI() = with (binding) {
        tvTitle.text = getString(R.string.title_input_category_for_add)
        etCategory.isVisible = true
        btnPositive.text = getString(R.string.do_add)
        btnPositive.onThrottleClick {
            if (isValidCategoryName()) {
                viewModel.makeCategory(categoryName = etCategory.text.toString())
            }
        }
        btnNegative.text = getString(R.string.do_cancel)
        btnNegative.onThrottleClick {
            dismiss()
        }
    }

    /**
     *  카테고리 수정 UI..
     */
    private fun setupCategoryModifyUI() = with (binding) {
        val currentCategoryItem = viewModel.categoryItem.value

        tvTitle.text = getString(R.string.title_input_category_for_edit)

        etCategory.isVisible = true
        etCategory.setText(currentCategoryItem?.categoryName ?: "")

        tvNoti.isVisible = true
        tvNoti.text = getString(R.string.desc_when_edit_category_name_change_same_category_notes)

        btnPositive.text = getString(R.string.do_modify)
        btnPositive.onThrottleClick {
            if (isValidCategoryName()) {
                currentCategoryItem?.let {
                    viewModel.editCategory(it.copy(categoryName = etCategory.text.toString()))
                }
            }
        }
        btnNegative.text = getString(R.string.do_cancel)
        btnNegative.onThrottleClick {
            dismiss()
        }
    }

    /**
     *  카테고리 삭제 UI..
     */
    private fun setupCategoryDeleteUI() = with (binding) {
        val currentCategoryItem = viewModel.categoryItem.value

        tvTitle.text = getString(R.string.title_will_you_delete_category)

        tvNoti.isVisible = true
        tvNoti.text = getString(R.string.desc_when_delete_category_change_same_category_notes)

        btnPositive.text = getString(R.string.yes_i_will_delete)
        btnPositive.onThrottleClick {
            currentCategoryItem?.let {
                viewModel.deleteCategory(it.id)
            }
        }
        btnNegative.text = getString(R.string.do_cancel)
        btnNegative.onThrottleClick {
            dismiss()
        }
    }

    interface CategoryManageEventCallback {
        fun addEventCallback()
        fun editEventCallback(category: CategoryUiModel)
        fun deleteEventCallback(categoryId: Int)
    }

    companion object {
        const val TAG = "CategoryAddOrDeleteOrEditDialog"

        const val ARG_ENUM_TYPE = "ARG_ENUM_TYPE"
        const val ARG_CATEGORY_ITEM = "ARG_CATEGORY_ITEM"

        fun newInstance(
            dialogType: CategoryDialogType,
            categoryItem: CategoryUiModel? = null
        ): CategoryAddOrDeleteOrEditDialog = CategoryAddOrDeleteOrEditDialog().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_ENUM_TYPE, dialogType)
                putParcelable(ARG_CATEGORY_ITEM, categoryItem)
            }
        }
    }
}