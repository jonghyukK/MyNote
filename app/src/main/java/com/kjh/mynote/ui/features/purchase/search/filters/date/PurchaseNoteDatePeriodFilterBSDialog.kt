package com.kjh.mynote.ui.features.purchase.search.filters.date

import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.R
import com.kjh.mynote.databinding.BsdDateRangeFilterDialogBinding
import com.kjh.mynote.ui.base.BaseBottomSheetDialogFragment
import com.kjh.mynote.ui.common.dialog.yearmonths.SelectableYearMonthListBSDialog
import com.kjh.mynote.ui.features.place.search.result.DateRangeFilter
import com.kjh.mynote.ui.features.purchase.search.FilterUiState
import com.kjh.mynote.ui.features.purchase.search.PurchaseNoteFilters
import com.kjh.mynote.ui.features.purchase.search.filters.whole.PurchaseNoteSearchWholeFilterViewModel
import com.kjh.mynote.ui.features.purchase.search.PurchaseNoteSearchViewModel
import com.kjh.mynote.utils.DatePickerManager
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.setTextColorRes
import com.kjh.mynote.utils.extensions.toLocalDate
import com.kjh.mynote.utils.extensions.toMillis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 25..
 * Description:
 */

@AndroidEntryPoint
class PurchaseNoteDatePeriodFilterBSDialog :
    BaseBottomSheetDialogFragment<BsdDateRangeFilterDialogBinding>({
        BsdDateRangeFilterDialogBinding.inflate(it)
    }), SelectableYearMonthListBSDialog.YearMonthClickListener {

    private val parentViewModel: PurchaseNoteSearchViewModel by activityViewModels()
    private val viewModel: PurchaseNoteSearchWholeFilterViewModel by viewModels()

    override fun onInitView() {
        with (binding) {
            clResetContainer.setOnThrottleClickListener(resetClickListener)
            ivClose.setOnThrottleClickListener(closeBtnClickListener)

            tvMonthly.setOnThrottleClickListener(monthlyClickListener)
            tvOneMonth.setOnThrottleClickListener(oneMonthClickListener)
            tvThreeMonth.setOnThrottleClickListener(threeMonthClickListener)
            tvDirectly.setOnThrottleClickListener(directlyClickListener)

            clSelectMonthlyContainer.setOnThrottleClickListener(selectableMonthClickListener)
            tvStartDate.setOnThrottleClickListener(startDateClickListener)
            tvEndDate.setOnThrottleClickListener(endDateClickListener)

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
                    viewModel.isChangedDateFilter.collect { isChangedFilter ->
                        binding.btnApply.isEnable = isChangedFilter
                    }
                }

                launch {
                    viewModel.tempFilterUiState
                        .map { it.dateRangeFilter }
                        .distinctUntilChanged()
                        .collect { monthFilter ->
                            when (monthFilter.dateRangeFilter) {
                                is DateRangeFilter.Monthly -> {
                                    makeFilterUis(binding.tvMonthly)

                                    binding.tvSelectableMonth.text = monthFilter.dateRangeFilter.getUiText()
                                }

                                is DateRangeFilter.MonthOne -> {
                                    makeFilterUis(binding.tvOneMonth)
                                }

                                is DateRangeFilter.MonthThree -> {
                                    makeFilterUis(binding.tvThreeMonth)
                                }

                                is DateRangeFilter.Directly -> {
                                    makeFilterUis(binding.tvDirectly)

                                    binding.tvStartDate.text = monthFilter.dateRangeFilter.getStartDateUiText()
                                    binding.tvEndDate.text = monthFilter.dateRangeFilter.getEndDateUiText()
                                }
                            }
                        }
                }
            }
        }
    }

    private fun makeFilterUis(selectedView: AppCompatTextView) = with (binding) {
        clSelectMonthlyContainer.isVisible = selectedView == tvMonthly
        clSelectDirectlyContainer.isVisible = selectedView == tvDirectly

        listOf(tvMonthly, tvOneMonth, tvThreeMonth, tvDirectly).forEach { view ->
            if (view == selectedView) {
                view.setTextColorRes(selectedTextColor)
                view.background = ContextCompat.getDrawable(requireContext(), selectedBackground)
            } else {
                view.setTextColorRes(defaultTextColor)
                view.background = ContextCompat.getDrawable(requireContext(), defaultBackground)
            }
        }
    }

    private val resetClickListener = View.OnClickListener {
        viewModel.resetDateFilter()
    }

    private val monthlyClickListener = View.OnClickListener {
        viewModel.setDateFilter(DateRangeFilter.Monthly(date = LocalDate.now()))
    }

    private val oneMonthClickListener = View.OnClickListener {
        viewModel.setDateFilter(DateRangeFilter.MonthOne())
    }

    private val threeMonthClickListener = View.OnClickListener {
        viewModel.setDateFilter(DateRangeFilter.MonthThree())
    }

    private val directlyClickListener = View.OnClickListener {
        viewModel.setDateFilter(
            DateRangeFilter.Directly(
                startDate = LocalDate.now().minusYears(1),
                endDate = LocalDate.now()
            )
        )
    }

    private val selectableMonthClickListener = View.OnClickListener {
        val filterItem = viewModel.tempFilterUiState.value.dateRangeFilter.dateRangeFilter
        if (filterItem is DateRangeFilter.Monthly) {
            SelectableYearMonthListBSDialog.newInstance(
                selectedDate = filterItem.date
            ).show(childFragmentManager, SelectableYearMonthListBSDialog.TAG)
        }
    }

    private val startDateClickListener = View.OnClickListener {
        val monthFilter = viewModel.tempFilterUiState.value.dateRangeFilter.dateRangeFilter
        if (monthFilter is DateRangeFilter.Directly) {
            DatePickerManager.build(
                title = getString(R.string.select_start_date),
                selection = monthFilter.startDate.toMillis(),
                minDate = 0L,
                maxDate = monthFilter.endDate.toMillis(),
                positiveButtonClickAction = { date ->
                    viewModel.setDateFilter(
                        DateRangeFilter.Directly(
                            startDate = date.toLocalDate(),
                            endDate = monthFilter.endDate
                        )
                    )
                }
            ).show(childFragmentManager, TAG_START_DATE_PICKER)
        }
    }

    private val endDateClickListener = View.OnClickListener {
        val monthFilter = viewModel.tempFilterUiState.value.dateRangeFilter.dateRangeFilter
        if (monthFilter is DateRangeFilter.Directly) {
            DatePickerManager.build(
                title = getString(R.string.select_end_date),
                selection = monthFilter.endDate.toMillis(),
                minDate = monthFilter.startDate.toMillis(),
                maxDate = LocalDate.now().toMillis(),
                positiveButtonClickAction = { date ->
                    viewModel.setDateFilter(
                        DateRangeFilter.Directly(
                            startDate = monthFilter.startDate,
                            endDate = date.toLocalDate()
                        )
                    )
                }
            ).show(childFragmentManager, TAG_END_DATE_PICKER)
        }
    }

    private val closeBtnClickListener = View.OnClickListener {
        dismiss()
    }

    private val applyBtnClickListener = View.OnClickListener {
        if (binding.btnApply.isEnable) {
            val selectedFilter = viewModel.tempFilterUiState.value.dateRangeFilter
            parentViewModel.setDateRangeFilter(selectedFilter)

            dismiss()
        }
    }

    override fun onClickYearMonth(date: LocalDate) {
        viewModel.setDateFilter(DateRangeFilter.Monthly(date = date))
    }

    companion object {
        const val TAG = "PurchaseNoteDatePeriodFilterBSDialog"

        private const val TAG_START_DATE_PICKER = "TAG_START_DATE_PICKER"
        private const val TAG_END_DATE_PICKER = "TAG_END_DATE_PICKER"

        private val defaultTextColor = R.color.black_800
        private val defaultBackground = R.drawable.shape_s_white_c_4_l_black_200

        private val selectedTextColor = R.color.purple
        private val selectedBackground = R.drawable.shape_s_white_c_4_l_purple

        fun newInstance() = PurchaseNoteDatePeriodFilterBSDialog()
    }
}