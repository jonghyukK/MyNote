package com.kjh.mynote.ui.features.category.list

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.databinding.BsdCategoryListDialogBinding
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.ui.base.BaseBottomSheetDialogFragment
import com.kjh.mynote.ui.common.dialog.categorymanage.CategoryAddOrDeleteOrEditDialog
import com.kjh.mynote.ui.common.dialog.categorymanage.CategoryDialogType
import com.kjh.mynote.ui.features.category.list.adapter.CategoryListAdapter
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 8..
 * Description:
 */

@AndroidEntryPoint
class CategoryListBSDialog : BaseBottomSheetDialogFragment<BsdCategoryListDialogBinding>({ BsdCategoryListDialogBinding.inflate(it) }),
CategoryAddOrDeleteOrEditDialog.CategoryManageEventCallback {

    private val viewModel: CategoryListViewModel by viewModels()

    private val listAdapter: CategoryListAdapter by lazy {
        CategoryListAdapter(
            onItemClickAction = onCategoryClickAction,
            onEditClickAction = onEditClickAction,
            onDeleteClickAction = onDeleteClickAction
        )
    }

    private var categoryClickAction: (CategoryUiModel) -> Unit = {}
    private var updateCategoryNameAction: (CategoryUiModel) -> Unit = {}
    private var deleteCategoryAction: (Int) -> Unit = {}

    private var categoryEditDialog: DialogFragment? = null

    override fun onInitView() {
        with (binding) {
            rvCategories.apply {
                itemAnimator = null
                adapter = listAdapter
            }

            ivAddCategory.setOnThrottleClickListener(addCategoryClickListener)
        }
    }

    override fun onInitData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isEditable.collect { isEditable ->
                        binding.ivAddCategory.isVisible = isEditable
                    }
                }

                launch {
                    viewModel.uiState.collect { categoryItems ->
                        listAdapter.submitList(categoryItems)
                    }
                }
            }
        }
    }

    private fun dialogDismissAndSetNull() {
        categoryEditDialog?.let {
            it.dismiss()
            categoryEditDialog = null
        }
    }

    private val onCategoryClickAction: (CategoryUiModel) -> Unit = { category ->
        categoryClickAction(category)
        dismiss()
    }

    // 수정..
    private val onEditClickAction: (CategoryUiModel) -> Unit = { category ->
        categoryEditDialog = CategoryAddOrDeleteOrEditDialog.newInstance(
            dialogType = CategoryDialogType.MODIFY,
            categoryItem = category
        )
        categoryEditDialog?.show(childFragmentManager, CategoryAddOrDeleteOrEditDialog.TAG)
    }

    // 삭제..
    private val onDeleteClickAction: (CategoryUiModel) -> Unit = { category ->
        categoryEditDialog = CategoryAddOrDeleteOrEditDialog.newInstance(
            dialogType = CategoryDialogType.DELETE,
            categoryItem = category
        )
        categoryEditDialog?.show(childFragmentManager, CategoryAddOrDeleteOrEditDialog.TAG)
    }

    // 추가..
    private val addCategoryClickListener = View.OnClickListener {
        categoryEditDialog = CategoryAddOrDeleteOrEditDialog.newInstance(
            dialogType = CategoryDialogType.ADD
        )
        categoryEditDialog?.show(childFragmentManager, CategoryAddOrDeleteOrEditDialog.TAG)
    }

    override fun addEventCallback() {
        dialogDismissAndSetNull()
    }

    override fun editEventCallback(category: CategoryUiModel) {
        updateCategoryNameAction(category)
        dialogDismissAndSetNull()
    }

    override fun deleteEventCallback(categoryId: Int) {
        deleteCategoryAction(categoryId)
        dialogDismissAndSetNull()
    }

    companion object {
        const val TAG = "CategoryListBSDialog"
        const val ARG_BOOL_IS_EDITABLE = "ARG_BOOL_IS_EDITABLE"

        fun newInstance(
            isEditable: Boolean = true,
            selectedCategoryItem: CategoryUiModel?,
            selectCategoryAction: (CategoryUiModel) -> Unit,
            updateCategoryNameAction: (CategoryUiModel) -> Unit = {},
            deleteCategoryAction: (Int) -> Unit = {}
        ) = CategoryListBSDialog().apply {
            arguments = Bundle().apply {
                putBoolean(ARG_BOOL_IS_EDITABLE, isEditable)
                putParcelable(AppConstants.INTENT_CATEGORY_ITEM, selectedCategoryItem)
            }

            this.categoryClickAction = selectCategoryAction
            this.updateCategoryNameAction = updateCategoryNameAction
            this.deleteCategoryAction = deleteCategoryAction
        }
    }
}