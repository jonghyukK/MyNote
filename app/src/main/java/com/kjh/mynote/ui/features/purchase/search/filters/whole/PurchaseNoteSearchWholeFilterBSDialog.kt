package com.kjh.mynote.ui.features.purchase.search.filters.whole

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
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
import com.kjh.mynote.R
import com.kjh.mynote.databinding.BsdPurchaseNoteSearchFilterBinding
import com.kjh.mynote.ui.base.BaseBottomSheetDialogFragment
import com.kjh.mynote.ui.features.purchase.search.Filters
import com.kjh.mynote.ui.features.purchase.search.PurchaseNoteSearchViewModel
import com.kjh.mynote.utils.SpacingItemDecoration
import com.kjh.mynote.utils.extensions.setBackgroundRes
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.showToast
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
class PurchaseNoteSearchWholeFilterBSDialog: BaseBottomSheetDialogFragment<BsdPurchaseNoteSearchFilterBinding>({ BsdPurchaseNoteSearchFilterBinding.inflate(it) }) {

    private val parentViewModel: PurchaseNoteSearchViewModel by activityViewModels()
    private val viewModel: PurchaseNoteSearchWholeFilterViewModel by viewModels()

    private val categoryFilterAdapter: PurchaseNoteSearchFlexboxCategoryAdapter by lazy {
        PurchaseNoteSearchFlexboxCategoryAdapter(
            categoryItemClickAction = categoryItemClickAction
        )
    }

    override fun onStart() {
        super.onStart()
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val layoutParams = it.layoutParams
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            it.layoutParams = layoutParams

            val behavior = BottomSheetBehavior.from(it)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            behavior.isDraggable = false
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
                addItemDecoration(SpacingItemDecoration(right = 10, bottom = 10, exceptFirstItem = false))
                adapter = categoryFilterAdapter
            }

            etPurchaseName.addCustomTextWatcher(purchaseNameTextWatcher)
            etPurchaseName.setClearButtonClickListener(purchaseNameClearBtnClickListener)

            etMinPrice.addCustomTextWatcher(minPriceTextWatcher)
            etMaxPrice.addCustomTextWatcher(maxPriceTextWatcher)

            ivClose.setOnThrottleClickListener(closeBtnClickListener)
            clReset.setOnThrottleClickListener(resetBtnClickListener)
            btnApply.setOnThrottleClickListener(applyBtnClickListener)
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
                                if (etPurchaseName.text != filter.purchaseName) {
                                    etPurchaseName.text = filter.purchaseName
                                }
                            }
                        }
                }

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
                    viewModel.isChangedFilters.collect { isChangedFilter ->
                        val backgroundRes = if (isChangedFilter) {
                            R.drawable.ripple_shape_s_color_primary_c_8
                        } else {
                            R.drawable.shape_s_black_200_c_8
                        }

                        binding.btnApply.setBackgroundRes(backgroundRes)
                    }
                }

                launch {
                    viewModel.priceValidateEventState.collect { event ->
                        when (event) {
                            is PriceValidateEvent.Error -> {
                                showToast(event.msg)
                            }
                            PriceValidateEvent.Valid -> {
                                val filterState = viewModel.tempFilterUiState.value
                                parentViewModel.applyAllFilters(filterState)

                                dismiss()
                            }
                        }
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
        viewModel.checkPriceValidation()
    }

    companion object {
        const val TAG = "PurchaseNoteSearchFilterBSDialog"

        fun newInstance() = PurchaseNoteSearchWholeFilterBSDialog()
    }
}