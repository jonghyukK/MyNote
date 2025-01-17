package com.kjh.mynote.ui.features.category.list

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.databinding.BsdCategoryListDialogBinding
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.ui.base.BaseBottomSheetDialogFragment
import com.kjh.mynote.ui.features.category.list.adapter.CategoryListAdapter
import com.kjh.mynote.ui.features.mypage.category.CategoryManageActivity
import com.kjh.mynote.ui.features.mypage.paymentmethod.edit.EditPaymentMethodDialogFragment.Companion.REQUEST_KEY
import com.kjh.mynote.ui.features.mypage.paymentmethod.edit.EditPaymentMethodDialogFragment.Companion.RES_KEY_UPDATED_ITEM
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 8..
 * Description:
 */

@AndroidEntryPoint
class CategoryListBSDialog : BaseBottomSheetDialogFragment<BsdCategoryListDialogBinding>({ BsdCategoryListDialogBinding.inflate(it) }) {

    private val viewModel: CategoryListViewModel by viewModels()

    private val listAdapter: CategoryListAdapter by lazy {
        CategoryListAdapter(onCategoryClickAction)
    }

    override fun onInitView() {
        with (binding) {
            rvCategories.apply {
                itemAnimator = null
                adapter = listAdapter
            }

            tvCategoryManage.isVisible = arguments?.getBoolean(ARG_BOOL_IS_EDITABLE) ?: true
            tvCategoryManage.setOnThrottleClickListener(categoryManageClickListener)
        }
    }

    override fun onInitData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    when (uiState) {
                        is CategoryListUiState.Loading -> {}
                        is CategoryListUiState.Error -> {
                            showToast(uiState.errorMsg)
                        }
                        is CategoryListUiState.Success -> {
                            listAdapter.submitList(uiState.categoryItems)
                        }
                    }
                }
            }
        }
    }

    private val onCategoryClickAction: (CategoryUiModel) -> Unit = { category ->
        setFragmentResult(
            REQUEST_KEY,
            bundleOf(BUNDLE_KEY_SELECTED_CATEGORY to category)
        )

        dismiss()
    }

    private val categoryManageClickListener = View.OnClickListener {
        Intent(requireContext(), CategoryManageActivity::class.java).apply {
            startActivity(this)
        }
    }

    companion object {
        const val TAG = "CategoryListBSDialog"

        const val ARG_STR_DATE = "date"
        const val ARG_BOOL_IS_EDITABLE = "isEditable"
        const val ARG_OBJ_SELECTED_CATEGORY_ITEM = "selectedCategoryItem"

        const val REQUEST_KEY = "CategoryListBSDialog_request_key"
        const val BUNDLE_KEY_SELECTED_CATEGORY = "bundle_key_selected_category"

        fun newInstance(
            date: LocalDate? = null,
            isEditable: Boolean = true,
            selectedCategoryItem: CategoryUiModel?
        ) = CategoryListBSDialog().apply {
            arguments = Bundle().apply {
                putString(ARG_STR_DATE, date?.toString())
                putBoolean(ARG_BOOL_IS_EDITABLE, isEditable)
                putParcelable(ARG_OBJ_SELECTED_CATEGORY_ITEM, selectedCategoryItem)
            }
        }
    }
}