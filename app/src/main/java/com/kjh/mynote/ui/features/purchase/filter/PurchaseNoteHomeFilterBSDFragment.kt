package com.kjh.mynote.ui.features.purchase.filter

import android.view.View.OnClickListener
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.tabs.TabLayoutMediator
import com.kjh.mynote.databinding.DialogFragmentPurchaseNoteFilterBinding
import com.kjh.mynote.model.Filters
import com.kjh.mynote.ui.base.BaseBottomSheetDialogFragment
import com.kjh.mynote.ui.features.purchase.filter.adapter.FilterType
import com.kjh.mynote.ui.features.purchase.filter.adapter.PurchaseNoteHomeAppliedFilterListAdapter
import com.kjh.mynote.ui.features.purchase.filter.adapter.PurchaseNoteHomeFilterPagerAdapter
import com.kjh.mynote.ui.features.purchase.home.PurchaseHomeViewModel
import com.kjh.mynote.utils.decorations.SpacingItemDecoration
import com.kjh.mynote.utils.extensions.dpToPx
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 11..
 * Description:
 */



@AndroidEntryPoint
class PurchaseNoteHomeFilterBSDFragment
    : BaseBottomSheetDialogFragment<DialogFragmentPurchaseNoteFilterBinding>({ DialogFragmentPurchaseNoteFilterBinding.inflate(it) }) {

    private val parentViewModel: PurchaseHomeViewModel by viewModels({ requireParentFragment() })
    private val viewModel: PurchaseNoteHomeFilterBSDViewModel by viewModels()

    private val appliedFilterListAdapter: PurchaseNoteHomeAppliedFilterListAdapter by lazy {
        PurchaseNoteHomeAppliedFilterListAdapter(
            appliedFilterClickAction = appliedFilterClickAction
        )
    }

    override fun onInitView() {
        with (binding) {
            vpPager.apply {
                adapter = PurchaseNoteHomeFilterPagerAdapter(this@PurchaseNoteHomeFilterBSDFragment)
                TabLayoutMediator(tlFilters, this) { tab, position ->
                    tab.text = FilterType.entries[position].title
                }.attach()
            }

            rvAppliedFilters.apply {
                itemAnimator = null
                addItemDecoration(SpacingItemDecoration(left = 8, right = 8, exceptFirstItem = false))
                adapter = appliedFilterListAdapter
            }

            tbToolbar.setResetClickListener(resetClickListener)
            tbToolbar.setCloseClickListener(closeClickListener)
            btnApply.setOnThrottleClickListener(applyButtonClickListener)
        }
    }

    override fun onInitData() {
        viewModel.setInitAppliedFilters(parentViewModel.appliedFilterItems.value)
        viewModel.getFilterItems()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState
                        .map { it.errorMsg }
                        .distinctUntilChanged()
                        .collect { errorMsg ->
                            errorMsg?.let {
                                showToast(it)
                                viewModel.shownError()
                            }
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.selectedFilters }
                        .distinctUntilChanged()
                        .collect { selectedFilters ->
                            val bottomPadding = if (selectedFilters.isNotEmpty()) 40.dpToPx() else 0

                            binding.groupAppliedFilters.isVisible = selectedFilters.isNotEmpty()
                            binding.vpPager.setPadding(0, 0, 0, bottomPadding)

                            appliedFilterListAdapter.submitList(selectedFilters) {
                                if (viewModel.shouldScrollToEnd) {
                                    binding.rvAppliedFilters.smoothScrollToPosition(selectedFilters.size - 1)
                                    viewModel.shouldScrollToEnd = false
                                }
                            }
                        }
                }

                launch {
                    viewModel.isChangedFilterFlow.collect {
                        binding.btnApply.isEnable = it
                    }
                }
            }
        }
    }

    private val appliedFilterClickAction: (Filters) -> Unit = { filter ->
        when (filter) {
            is Filters.Category -> viewModel.addOrDeleteCategoryFilter(filter)
            is Filters.PaymentMethod -> viewModel.addOrDeletePaymentMethodFilter(filter)
            else -> {}
        }
    }

    private val closeClickListener = OnClickListener {
        dismiss()
    }

    private val resetClickListener = OnClickListener {
        viewModel.resetFilters()
    }

    private val applyButtonClickListener = OnClickListener {
        if (binding.btnApply.isEnable) {
            val selectedFilters = viewModel.uiState.value.selectedFilters
            parentViewModel.setFilters(selectedFilters)
            dismiss()
        }
    }

    companion object {
        const val TAG = "CategoryFilterDialogFragment"

        fun newInstance() = PurchaseNoteHomeFilterBSDFragment()
    }
}