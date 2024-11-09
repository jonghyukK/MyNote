package com.kjh.mynote.ui.features.category

import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.R
import com.kjh.mynote.databinding.BsdCategoryListDialogBinding
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.UiState
import com.kjh.mynote.ui.base.BaseBottomSheetDialogFragment
import com.kjh.mynote.ui.common.components.MyDefaultDialog
import com.kjh.mynote.ui.features.purchase.make.MakePurchaseNoteViewModel
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

    private val parentViewModel: MakePurchaseNoteViewModel by activityViewModels()
    private val viewModel: CategoryListViewModel by viewModels()

    private val listAdapter: CategoryListAdapter by lazy {
        CategoryListAdapter(
            onItemClickAction = onItemClickAction,
            onEditClickAction = onEditClickAction,
            onDeleteClickAction = onDeleteClickAction
        )
    }

    private var categoryEditDialog: DialogFragment? = null

    override fun onInitView() {
        with (binding) {
            rvCategories.apply {
                adapter = listAdapter
            }

            ivAddCategory.setOnThrottleClickListener(addCategoryClickListener)
        }
    }

    override fun onInitData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        listAdapter.submitList(state)
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
                            is UiState.Success -> dialogDismissAndSetNull()
                            else -> {}
                        }
                    }
                }

                launch {
                    viewModel.deleteCategoryEventState.collectLatest { event ->
                        when (event) {
                            is UiState.Error -> showToast(event.errorMsg)
                            is UiState.Success -> dialogDismissAndSetNull()
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

    private val onItemClickAction: (CategoryUiModel) -> Unit = {
        parentViewModel.setCategory(it.categoryName)
        dismiss()
    }

    // 수정..
    private val onEditClickAction: (CategoryUiModel) -> Unit = { category ->
        categoryEditDialog = CategoryAddOrDeleteOrEditDialog.newInstance(
            contents = getString(R.string.title_input_category_for_edit),
            categoryName = category.categoryName,
            posBtnText = getString(R.string.do_modify),
            posAction = { text ->
                viewModel.editCategory(category.copy(categoryName = text))
            },
            negBtnText = getString(R.string.do_cancel)
        )
        categoryEditDialog?.show(childFragmentManager, CategoryAddOrDeleteOrEditDialog.TAG)
    }

    // 삭제..
    private val onDeleteClickAction: (CategoryUiModel) -> Unit = { category ->
        categoryEditDialog = MyDefaultDialog.newInstance(
            contents = getString(R.string.title_will_you_delete_category),
            posBtnText = getString(R.string.yes_i_will_delete),
            posAction = { viewModel.deleteCategory(category.id) },
            negBtnText = getString(R.string.cancel)
        )
        categoryEditDialog?.show(childFragmentManager, MyDefaultDialog.TAG)
    }

    // 추가..
    private val addCategoryClickListener = View.OnClickListener {
        categoryEditDialog = CategoryAddOrDeleteOrEditDialog.newInstance(
            contents = getString(R.string.title_input_category_for_add),
            posBtnText = getString(R.string.do_add),
            posAction = { text ->
                viewModel.makeCategory(text)
            },
            negBtnText = getString(R.string.do_cancel)
        )

        categoryEditDialog?.show(childFragmentManager, CategoryAddOrDeleteOrEditDialog.TAG)
    }

    companion object {
        const val TAG = "CategoryListBSDialog"

        fun newInstance() = CategoryListBSDialog()
    }
}