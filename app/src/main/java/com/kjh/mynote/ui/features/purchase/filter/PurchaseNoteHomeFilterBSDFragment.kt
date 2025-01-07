package com.kjh.mynote.ui.features.purchase.filter

import android.view.View.OnClickListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.tabs.TabLayoutMediator
import com.kjh.mynote.databinding.DialogFragmentPurchaseNoteFilterBinding
import com.kjh.mynote.ui.base.BaseBottomSheetDialogFragment
import com.kjh.mynote.ui.features.purchase.filter.adapter.FilterType
import com.kjh.mynote.ui.features.purchase.filter.adapter.PurchaseNoteHomeFilterPagerAdapter
import com.kjh.mynote.ui.features.purchase.home.PurchaseHomeViewModel
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

    override fun onInitView() {
        with (binding) {
            vpPager.apply {
                adapter = PurchaseNoteHomeFilterPagerAdapter(this@PurchaseNoteHomeFilterBSDFragment)
                TabLayoutMediator(tlFilters, this) { tab, position ->
                    tab.text = FilterType.entries[position].title
                }.attach()
            }

            clResetContainer.setOnThrottleClickListener(resetClickListener)
            ivClose.setOnThrottleClickListener(closeClickListener)
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
                    viewModel.isChangedFilterFlow.collect {
                        binding.btnApply.isEnable = it
                    }
                }
            }
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
            val selectedFilters = viewModel.uiState.value.selectedFilterItems()
            parentViewModel.setFilters(selectedFilters)
            dismiss()
        }
    }

    companion object {
        const val TAG = "CategoryFilterDialogFragment"

        fun newInstance() = PurchaseNoteHomeFilterBSDFragment()
    }
}