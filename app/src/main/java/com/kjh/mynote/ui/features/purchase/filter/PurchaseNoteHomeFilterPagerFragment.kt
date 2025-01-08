package com.kjh.mynote.ui.features.purchase.filter

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.databinding.FragmentPurchaseNoteHomeFilterBinding
import com.kjh.mynote.model.Filters
import com.kjh.mynote.ui.base.BaseFragment
import com.kjh.mynote.ui.features.purchase.filter.adapter.FilterType
import com.kjh.mynote.ui.features.purchase.filter.adapter.PurchaseNoteHomeFilterListAdapter
import com.kjh.mynote.utils.extensions.parcelable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@AndroidEntryPoint
class PurchaseNoteHomeFilterPagerFragment : BaseFragment<FragmentPurchaseNoteHomeFilterBinding>({
    FragmentPurchaseNoteHomeFilterBinding.inflate(it)
}) {
    private val parentViewModel: PurchaseNoteHomeFilterBSDViewModel by viewModels({ requireParentFragment() })

    private val listAdapter: PurchaseNoteHomeFilterListAdapter by lazy {
        PurchaseNoteHomeFilterListAdapter(filterItemClickAction)
    }

    override fun onInitView() {
        binding.rvList.apply {
            itemAnimator = null
            adapter = listAdapter
        }
    }

    override fun onInitData() {
        val filterType = arguments?.parcelable<FilterType>(ARG_STR_FILTER_TYPE) ?: FilterType.Category
        when (filterType) {
            FilterType.Category -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        parentViewModel.uiState
                            .map { it.categoryItems }
                            .distinctUntilChanged()
                            .collect {
                                listAdapter.submitList(it)
                            }
                    }
                }
            }
            FilterType.PaymentMethod -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        parentViewModel.uiState
                            .map { it.paymentMethodItem }
                            .distinctUntilChanged()
                            .collect {
                                listAdapter.submitList(it)
                            }
                    }
                }
            }
        }
    }

    private val filterItemClickAction: (Filters) -> Unit = { filterItem ->
        when (filterItem) {
            is Filters.Category -> {
                parentViewModel.addOrDeleteCategoryFilter(filterItem)
            }
            is Filters.PaymentMethod -> {
                parentViewModel.addOrDeletePaymentMethodFilter(filterItem)
            }
            else -> {}
        }
    }

    companion object {
        private const val ARG_STR_FILTER_TYPE = "ARG_STR_FILTER_TYPE"

        @JvmStatic
        fun newInstance(
            filterType: FilterType
        ) = PurchaseNoteHomeFilterPagerFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_STR_FILTER_TYPE, filterType)
            }
        }
    }
}