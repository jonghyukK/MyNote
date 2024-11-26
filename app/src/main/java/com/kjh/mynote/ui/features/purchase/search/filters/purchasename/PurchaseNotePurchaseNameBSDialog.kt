package com.kjh.mynote.ui.features.purchase.search.filters.purchasename

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.databinding.BsdPurchaseNameFilterDialogBinding
import com.kjh.mynote.ui.base.BaseBottomSheetDialogFragment
import com.kjh.mynote.ui.features.purchase.search.PurchaseNoteSearchViewModel
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.showKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 26..
 * Description:
 */

@AndroidEntryPoint
class PurchaseNotePurchaseNameBSDialog :
    BaseBottomSheetDialogFragment<BsdPurchaseNameFilterDialogBinding>({
        BsdPurchaseNameFilterDialogBinding.inflate(it)
    }) {

    private val parentViewModel: PurchaseNoteSearchViewModel by activityViewModels()
    private val viewModel: PurchaseNotePurchaseNameFilterViewModel by viewModels()

    override fun onInitView() {
        with (binding) {
            etPurchaseName.addTextChangedListener(purchaseNameTextWatcher)

            ivClose.setOnThrottleClickListener(closeBtnClickListener)
            ivClear.setOnThrottleClickListener(textClearBtnClickListener)
            clResetContainer.setOnThrottleClickListener(resetBtnClickListener)
            btnApply.setOnThrottleClickListener(applyBtnClickListener)
        }
    }

    override fun onInitData() {
        val appliedPurchaseNameFilter = parentViewModel.filtersUiState.value.purchaseNameFilter
        viewModel.setInitPurchaseNameFilter(appliedPurchaseNameFilter.purchaseName)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState
                        .map { it.tempPurchaseNameFilter }
                        .distinctUntilChanged()
                        .collect { filter ->
                            if (binding.etPurchaseName.text.toString() != filter.purchaseName) {
                                binding.etPurchaseName.setText(filter.purchaseName)
                                binding.etPurchaseName.setSelection(filter.purchaseName.length)
                            }

                            binding.ivClear.isVisible = filter.purchaseName.isNotEmpty()
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.isChanged() }
                        .distinctUntilChanged()
                        .collect {
                            binding.btnApply.isEnable = it
                        }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding.etPurchaseName.showKeyboard()
    }

    private val purchaseNameTextWatcher = object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            viewModel.setTempPurchaseNameFilter(s.toString())
        }
    }

    private val closeBtnClickListener = View.OnClickListener {
        dismiss()
    }

    private val resetBtnClickListener = View.OnClickListener {
        viewModel.clearTempFilter()
    }

    private val textClearBtnClickListener = View.OnClickListener {
        viewModel.clearTempFilter()
    }

    private val applyBtnClickListener = View.OnClickListener {
        if (binding.btnApply.isEnable) {
            val filter = viewModel.uiState.value.tempPurchaseNameFilter
            parentViewModel.setPurchaseName(filter)
            dismiss()
        }
    }

    companion object {
        const val TAG = "PurchaseNotePurchaseNameBSDialog"

        fun newInstance() = PurchaseNotePurchaseNameBSDialog()
    }
}