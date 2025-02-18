package com.kjh.mynote.ui.features.category.list

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.R
import com.kjh.mynote.databinding.BsdCategoriesOrPaymentMethodsListBinding
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.UiState
import com.kjh.mynote.ui.base.BaseBottomSheetDialogFragment
import com.kjh.mynote.ui.features.category.list.adapter.CategoryListAdapter
import com.kjh.mynote.ui.features.mypage.category.CategoryManageActivity
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 8..
 * Description:
 */

@AndroidEntryPoint
class CategoryListBSDialog :
    BaseBottomSheetDialogFragment<BsdCategoriesOrPaymentMethodsListBinding>({
        BsdCategoriesOrPaymentMethodsListBinding.inflate(it)
    }) {

    private val viewModel: CategoryListViewModel by viewModels()

    private val listAdapter: CategoryListAdapter by lazy {
        CategoryListAdapter(onCategoryClickAction)
    }

    override fun onInitView() {
        with (binding) {
            tvTitle.text = getString(R.string.category_list)

            rvItems.apply {
                itemAnimator = null
                adapter = listAdapter
            }

            tvManage.isVisible = arguments?.getBoolean(ARG_BOOL_IS_EDITABLE) ?: true
            tvManage.setOnThrottleClickListener(manageBtnClickListener)
        }
    }

    override fun onInitData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.errorMessage.collectLatest(::showToast)
                }

                launch {
                    viewModel.uiState.collect { uiState ->
                        if (uiState is UiState.Success) {
                            listAdapter.submitList(uiState.data)
                        }
                    }
                }

                launch {
                    viewModel.updateSelectedItemEvent.collect {
                        setFragmentResult(
                            REQUEST_KEY,
                            bundleOf(BUNDLE_KEY_SELECTED_CATEGORY to it)
                        )
                    }
                }
            }
        }
    }

    private val onCategoryClickAction: (CategoryUiModel) -> Unit = { category ->
        setFragmentResult(REQUEST_KEY, bundleOf(BUNDLE_KEY_SELECTED_CATEGORY to category))
        dismiss()
    }

    private val manageBtnClickListener = View.OnClickListener {
        Intent(requireContext(), CategoryManageActivity::class.java).apply {
            startActivity(this)
        }
    }

    companion object {
        const val TAG = "CategoryListBSDialog"

        const val ARG_STR_DATE = "date"
        const val ARG_BOOL_IS_EDITABLE = "isEditable"
        const val ARG_BOOL_SHOW_COUNT = "showCount"
        const val ARG_OBJ_SELECTED_CATEGORY_ITEM = "selectedCategoryItem"

        const val REQUEST_KEY = "CategoryListBSDialog_request_key"
        const val BUNDLE_KEY_SELECTED_CATEGORY = "bundle_key_selected_category"

        fun newInstance(
            date: LocalDate? = null,
            isEditable: Boolean = true,
            showCount: Boolean = false,
            selectedCategoryItem: CategoryUiModel?
        ) = CategoryListBSDialog().apply {
            arguments = Bundle().apply {
                putString(ARG_STR_DATE, date?.toString())
                putBoolean(ARG_BOOL_IS_EDITABLE, isEditable)
                putBoolean(ARG_BOOL_SHOW_COUNT, showCount)
                putParcelable(ARG_OBJ_SELECTED_CATEGORY_ITEM, selectedCategoryItem)
            }
        }
    }
}