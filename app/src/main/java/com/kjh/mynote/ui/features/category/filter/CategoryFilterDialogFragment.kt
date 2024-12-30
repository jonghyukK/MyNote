package com.kjh.mynote.ui.features.category.filter

import android.os.Bundle
import android.view.View.OnClickListener
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.kjh.mynote.databinding.DialogFragmentPurchaseNoteFilterBinding
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.ui.base.BaseBottomSheetDialogFragment
import com.kjh.mynote.ui.features.category.filter.adapter.CategoryFilterListAdapter
import com.kjh.mynote.ui.features.purchase.home.PurchaseHomeViewModel
import com.kjh.mynote.utils.decorations.SpacingItemDecoration
import com.kjh.mynote.utils.extensions.parcelableArrayList
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 11..
 * Description:
 */

@AndroidEntryPoint
class CategoryFilterDialogFragment
    : BaseBottomSheetDialogFragment<DialogFragmentPurchaseNoteFilterBinding>({ DialogFragmentPurchaseNoteFilterBinding.inflate(it) }) {

    private val parentViewModel: PurchaseHomeViewModel by viewModels({ requireParentFragment() })
    private val viewModel: CategoryFilterViewModel by viewModels()

    private val listAdapter: CategoryFilterListAdapter by lazy {
        CategoryFilterListAdapter(onItemClickAction)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.parcelableArrayList<CategoryUiModel>(ARG_APPLIED_FILTER_ITEMS)?.let {
            viewModel.savedStateHandle[ARG_APPLIED_FILTER_ITEMS] = it.toList()
        }
    }

    override fun onInitView() {
        with (binding) {
            rvCategories.apply {
                itemAnimator = null
                layoutManager = FlexboxLayoutManager(requireContext()).apply {
                    flexDirection = FlexDirection.ROW
                    flexWrap = FlexWrap.WRAP
                    justifyContent = JustifyContent.FLEX_START
                }
                addItemDecoration(SpacingItemDecoration(right = 12, bottom = 10, exceptFirstItem = false))
                adapter = listAdapter
            }

            clResetContainer.setOnThrottleClickListener(resetClickListener)
            ivClose.setOnThrottleClickListener(closeClickListener)
            btnApply.setOnThrottleClickListener(applyButtonClickListener)
        }
    }

    override fun onInitData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState
                        .map { it.filterItems }
                        .distinctUntilChanged()
                        .collectLatest {
                            binding.layoutEmptyView.root.isVisible = it.isEmpty()
                            listAdapter.submitList(it)
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.isChanged }
                        .distinctUntilChanged()
                        .collectLatest {
                            binding.btnApply.isEnable = it
                        }
                }
            }
        }
    }

    private val onItemClickAction: (CategoryUiModel) -> Unit = {
        viewModel.addOrRemoveFilters(it)
    }

    private val closeClickListener = OnClickListener {
        dismiss()
    }

    private val resetClickListener = OnClickListener {
        viewModel.resetFilters()
    }

    private val applyButtonClickListener = OnClickListener {
        if (binding.btnApply.isEnable) {
            parentViewModel.setCategoryFilterItems(viewModel.selectedCategoryItems.value)
            dismiss()
        }
    }

    companion object {
        const val TAG = "CategoryFilterDialogFragment"
        const val ARG_APPLIED_FILTER_ITEMS = "ARG_APPLIED_FILTER_ITEMS"

        fun newInstance(
            appliedFilterItems: List<CategoryUiModel>
        ) = CategoryFilterDialogFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(ARG_APPLIED_FILTER_ITEMS, ArrayList(appliedFilterItems))
            }
        }
    }
}