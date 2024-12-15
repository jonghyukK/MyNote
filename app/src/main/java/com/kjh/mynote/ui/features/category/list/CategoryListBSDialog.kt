package com.kjh.mynote.ui.features.category.list

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.R
import com.kjh.mynote.databinding.BsdCategoryListDialogBinding
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.UiState
import com.kjh.mynote.ui.base.BaseBottomSheetDialogFragment
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.parcelable
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 8..
 * Description:
 */

@AndroidEntryPoint
class CategoryListBSDialog : BaseBottomSheetDialogFragment<BsdCategoryListDialogBinding>({ BsdCategoryListDialogBinding.inflate(it) }) {

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

                launch {
                    viewModel.makeCategoryEventState.collectLatest { event ->
                        when (event) {
                            is UiState.Error -> showToast(event.errorMsg)
                            is UiState.Success -> dialogDismissAndSetNull()
                            else -> {}
                        }
                    }
                }

                launch {
                    viewModel.updateCategoryNameEventState.collectLatest { event ->
                        when (event) {
                            is UiState.Error -> showToast(event.errorMsg)
                            is UiState.Success -> {
                                updateCategoryNameAction(event.data)
                                dialogDismissAndSetNull()
                            }
                            else -> {}
                        }
                    }
                }

                launch {
                    viewModel.deleteCategoryEventState.collectLatest { event ->
                        when (event) {
                            is UiState.Error -> showToast(event.errorMsg)
                            is UiState.Success -> {
                                deleteCategoryAction(event.data)
                                dialogDismissAndSetNull()
                            }
                            else -> {}
                        }
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