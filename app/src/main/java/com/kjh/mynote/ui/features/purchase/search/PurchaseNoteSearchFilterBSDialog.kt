package com.kjh.mynote.ui.features.purchase.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.kjh.mynote.databinding.BsdPurchaseNoteSearchFilterBinding
import com.kjh.mynote.ui.base.BaseBottomSheetDialogFragment
import com.kjh.mynote.utils.SpacingItemDecoration
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.toComma
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 27..
 * Description:
 */

@AndroidEntryPoint
class PurchaseNoteSearchFilterBSDialog: BaseBottomSheetDialogFragment<BsdPurchaseNoteSearchFilterBinding>({ BsdPurchaseNoteSearchFilterBinding.inflate(it) }) {

    private val parentViewModel: PurchaseNoteSearchViewModel by activityViewModels()
    private val viewModel: PurchaseNoteSearchFilterViewModel by viewModels()

    private val categoryFilterAdapter: PurchaseNoteSearchFlexboxCategoryAdapter by lazy {
        PurchaseNoteSearchFlexboxCategoryAdapter(
            categoryItemClickAction = categoryItemClickAction
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onStart() {
        super.onStart()

        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val layoutParams = it.layoutParams
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            it.layoutParams = layoutParams

            val behavior = BottomSheetBehavior.from(it)

            // Expanded 상태로 설정
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            behavior.isDraggable = false
        }
    }

    override fun onInitView() {
        with (binding) {
            ivClose.setOnThrottleClickListener(closeBtnClickListener)
            ivClear.setOnThrottleClickListener(purchaseNameClearBtnClickListener)
            etPurchaseName.addTextChangedListener(purchaseNameTextWatcher)
            etMinPrice.addTextChangedListener(minPriceTextWatcher)
            etMaxPrice.addTextChangedListener(maxPriceTextWatcher)

            btnReset.setOnThrottleClickListener(resetBtnClickListener)
            btnApply.setOnThrottleClickListener(applyBtnClickListener)

            rvCategories.apply {
                itemAnimator = null
                layoutManager = FlexboxLayoutManager(requireContext()).apply {
                    flexDirection = FlexDirection.ROW
                    flexWrap = FlexWrap.WRAP
                    justifyContent = JustifyContent.FLEX_START
                }
                addItemDecoration(SpacingItemDecoration(right = 10, bottom = 10, exceptFirstItem = false))
                adapter = categoryFilterAdapter
            }
        }
    }

    override fun onInitData() {
        val appliedFilterState = parentViewModel.filtersUiState.value
        viewModel.setInitFilterUiState(appliedFilterState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.tempFilterUiState
                        .map { it.categoryFilters }
                        .distinctUntilChanged()
                        .collect { categoryFilters ->
                            categoryFilterAdapter.submitList(categoryFilters)
                        }
                }

                launch {
                    viewModel.tempFilterUiState
                        .map { it.purchaseNameFilter }
                        .distinctUntilChanged()
                        .collect { filter ->
                            with (binding) {
                                if (etPurchaseName.text.toString() != filter.purchaseName) {
                                    etPurchaseName.setText(filter.purchaseName)
                                }
                                ivClear.isVisible = filter.purchaseName.isNotBlank()
                            }
                        }
                }

                launch {
                    viewModel.tempFilterUiState
                        .map { it.priceFilter.minPrice }
                        .distinctUntilChanged()
                        .collect { minPrice ->
                            binding.etMinPrice.setText(minPrice.toString())
                        }
                }

                launch {
                    viewModel.tempFilterUiState
                        .map { it.priceFilter.maxPrice }
                        .distinctUntilChanged()
                        .collect { maxPrice ->
                            binding.etMaxPrice.setText(maxPrice.toString())
                        }
                }
            }
        }
    }

    private val purchaseNameTextWatcher = object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            viewModel.setPurchaseName(s.toString())
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
                binding.etMinPrice.removeTextChangedListener(this)

                val cleanString = s.toString().replace(",", "")
                if (cleanString.isNotEmpty()) {
                    viewModel.setMinPrice(cleanString)

                    val formatted = cleanString.toLong().toComma()
                    current = formatted
                    binding.etMinPrice.setText(formatted)
                    binding.etMinPrice.setSelection(formatted.length)
                } else {
                    viewModel.clearMinPrice()
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
                viewModel.clearMaxPrice()
                return
            }

            if (s.toString() != current) {
                binding.etMaxPrice.removeTextChangedListener(this)

                val cleanString = s.toString().replace(",", "")
                if (cleanString.isNotEmpty()) {
                    viewModel.setMaxPrice(cleanString)

                    val formatted = cleanString.toLong().toComma()
                    current = formatted
                    binding.etMaxPrice.setText(formatted)
                    binding.etMaxPrice.setSelection(formatted.length)
                } else {
                    viewModel.clearMaxPrice()
                }

                binding.etMaxPrice.addTextChangedListener(this)
            }
        }
    }

    private val categoryItemClickAction: (Filters.Category) -> Unit = { categoryItem ->
        viewModel.updateCategoryFilter(categoryItem.categoryItem.id)
    }

    private val purchaseNameClearBtnClickListener = View.OnClickListener {
        viewModel.clearPurchaseName()
    }

    private val closeBtnClickListener = View.OnClickListener {
        dismiss()
    }

    private val resetBtnClickListener = View.OnClickListener {
        viewModel.resetAllFilters()
    }

    private val applyBtnClickListener = View.OnClickListener {

    }

    companion object {
        const val TAG = "PurchaseNoteSearchFilterBSDialog"

        fun newInstance() = PurchaseNoteSearchFilterBSDialog()
    }
}