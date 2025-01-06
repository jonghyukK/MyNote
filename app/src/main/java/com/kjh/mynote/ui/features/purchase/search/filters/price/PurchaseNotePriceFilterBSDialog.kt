package com.kjh.mynote.ui.features.purchase.search.filters.price

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.databinding.BsdPriceFilterDialogBinding
import com.kjh.mynote.ui.base.BaseBottomSheetDialogFragment
import com.kjh.mynote.ui.features.purchase.search.FilterUiState
import com.kjh.mynote.ui.features.purchase.search.PurchaseNoteFilters
import com.kjh.mynote.ui.features.purchase.search.filters.whole.PriceValidateEvent
import com.kjh.mynote.ui.features.purchase.search.filters.whole.PurchaseNoteSearchWholeFilterViewModel
import com.kjh.mynote.ui.features.purchase.search.PurchaseNoteSearchViewModel
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.showToast
import com.kjh.mynote.utils.extensions.toComma
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 25..
 * Description:
 */

@AndroidEntryPoint
class PurchaseNotePriceFilterBSDialog: BaseBottomSheetDialogFragment<BsdPriceFilterDialogBinding>({ BsdPriceFilterDialogBinding.inflate(it) }) {

    private val parentViewModel: PurchaseNoteSearchViewModel by activityViewModels()
    private val viewModel: PurchaseNoteSearchWholeFilterViewModel by viewModels()

    override fun onInitView() {
        with (binding) {
            etMinPrice.addCustomTextWatcher(minPriceTextWatcher)
            etMaxPrice.addCustomTextWatcher(maxPriceTextWatcher)

            ivClose.setOnThrottleClickListener(closeBtnClickListener)
            clResetContainer.setOnThrottleClickListener(resetBtnClickListener)
            btnApply.setOnThrottleClickListener(applyBtnClickListener)
        }
    }

    override fun onInitData() {
        val parentFilterState =
            (parentViewModel.filterUiState.value as? FilterUiState.Success)?.filters ?: PurchaseNoteFilters()
        viewModel.setInitFilterUiState(parentFilterState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.tempFilterUiState
                        .map { it.priceFilter}
                        .distinctUntilChanged()
                        .collect { (minPrice, maxPrice, myMaxPrice) ->
                            with (binding) {
                                etMaxPrice.hint = myMaxPrice.toComma()

                                etMinPrice.text = minPrice?.toString() ?: ""
                                etMaxPrice.text = maxPrice?.toString() ?: ""
                            }
                        }
                }

                launch {
                    viewModel.isChangedPriceFilter.collect { isChanged ->
                        binding.btnApply.isEnable = isChanged
                    }
                }

                launch {
                    viewModel.priceValidateEventState.collect { event ->
                        when (event) {
                            is PriceValidateEvent.Error -> {
                                showToast(event.msg)
                            }
                            is PriceValidateEvent.Valid -> {
                                val tempPriceFilter = viewModel.tempFilterUiState.value.priceFilter
                                parentViewModel.setPriceFilter(tempPriceFilter)
                                dismiss()
                            }
                        }
                    }
                }
            }
        }
    }

    private val minPriceTextWatcher = object: TextWatcher {
        private var current = ""
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            if (s.toString().isEmpty()) {
                current = ""
                viewModel.clearMinPrice()
                return
            }

            if (s.toString() != current) {
                binding.etMinPrice.removeCustomTextWatcher()

                val cleanString = s.toString().replace(",", "")
                if (cleanString.isNotEmpty()) {
                    viewModel.setMinPrice(cleanString)

                    val formatted = cleanString.toLong().toComma()
                    current = formatted
                    binding.etMinPrice.text = formatted
                    binding.etMinPrice.setSelection(formatted.length)
                } else {
                    viewModel.clearMinPrice()
                }

                binding.etMinPrice.addCustomTextWatcher(this)
            }
        }
    }

    private val maxPriceTextWatcher = object: TextWatcher {
        private var current = ""
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            if (s.toString().isEmpty()) {
                current = ""
                viewModel.clearMaxPrice()
                return
            }

            if (s.toString() != current) {
                binding.etMaxPrice.removeCustomTextWatcher()

                val cleanString = s.toString().replace(",", "")
                if (cleanString.isNotEmpty()) {
                    viewModel.setMaxPrice(cleanString)

                    val formatted = cleanString.toLong().toComma()
                    current = formatted
                    binding.etMaxPrice.text = formatted
                    binding.etMaxPrice.setSelection(formatted.length)
                } else {
                    viewModel.clearMaxPrice()
                }

                binding.etMaxPrice.addCustomTextWatcher(this)
            }
        }
    }

    private val closeBtnClickListener = View.OnClickListener {
        dismiss()
    }

    private val resetBtnClickListener = View.OnClickListener {
        viewModel.resetPriceFilter()
    }

    private val applyBtnClickListener = View.OnClickListener {
        if (binding.btnApply.isEnable) {
            viewModel.checkPriceValidation()
        }
    }

    companion object {
        const val TAG = "PurchaseNotePriceFilterBSDialog"

        fun newInstance() = PurchaseNotePriceFilterBSDialog()
    }
}