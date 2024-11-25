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
    private val viewModel: PurchaseNotePriceFilterViewModel by viewModels()

    override fun onInitView() {
        with (binding) {
            etMinPrice.addTextChangedListener(minPriceTextWatcher)
            etMaxPrice.addTextChangedListener(maxPriceTextWatcher)

            ivClose.setOnThrottleClickListener(closeBtnClickListener)
            clResetContainer.setOnThrottleClickListener(resetBtnClickListener)
            btnApply.setOnThrottleClickListener(applyBtnClickListener)
        }
    }

    override fun onInitData() {
        val appliedPriceFilter = parentViewModel.filtersUiState.value.priceFilter
        viewModel.setInitPrices(appliedPriceFilter)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState
                        .map { it.tempPriceFilter.minPrice }
                        .distinctUntilChanged()
                        .collect { minPrice ->
                            if (minPrice == 0L) {
                                binding.etMinPrice.setText("0")
                            } else {
                                binding.etMinPrice.setText(minPrice.toString())
                            }
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.tempPriceFilter.maxPrice }
                        .distinctUntilChanged()
                        .collect { maxPrice->
                            if (maxPrice == 0L) {
                                binding.etMaxPrice.setText("0")
                            } else {
                                binding.etMaxPrice.setText(maxPrice.toString())
                            }
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.isChangedFilter() }
                        .distinctUntilChanged()
                        .collect { isChanged ->
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
                                val tempPriceFilter = viewModel.uiState.value.tempPriceFilter
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
                viewModel.clearTempMinPrice()
                return
            }

            if (s.toString() != current) {
                binding.etMinPrice.removeTextChangedListener(this)

                val cleanString = s.toString().replace(",", "")
                if (cleanString.isNotEmpty()) {
                    viewModel.setTempMinPrice(cleanString)

                    val formatted = cleanString.toLong().toComma()
                    current = formatted
                    binding.etMinPrice.setText(formatted)
                    binding.etMinPrice.setSelection(formatted.length)
                } else {
                    viewModel.clearTempMinPrice()
                }

                binding.etMinPrice.addTextChangedListener(this)
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
                viewModel.clearTempMaxPrice()
                return
            }

            if (s.toString() != current) {
                binding.etMaxPrice.removeTextChangedListener(this)

                val cleanString = s.toString().replace(",", "")
                if (cleanString.isNotEmpty()) {
                    viewModel.setTempMaxPrice(cleanString)

                    val formatted = cleanString.toLong().toComma()
                    current = formatted
                    binding.etMaxPrice.setText(formatted)
                    binding.etMaxPrice.setSelection(formatted.length)
                } else {
                    viewModel.clearTempMaxPrice()
                }

                binding.etMaxPrice.addTextChangedListener(this)
            }
        }
    }

    private val closeBtnClickListener = View.OnClickListener {
        dismiss()
    }

    private val resetBtnClickListener = View.OnClickListener {
        viewModel.resetTempPrices()
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