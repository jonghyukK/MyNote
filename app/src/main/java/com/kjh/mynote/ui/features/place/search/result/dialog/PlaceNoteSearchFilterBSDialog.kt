package com.kjh.mynote.ui.features.place.search.result.dialog

import android.view.View.OnClickListener
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.R
import com.kjh.mynote.databinding.BsdPlaceNoteSearchFilterDialogBinding
import com.kjh.mynote.ui.base.BaseBottomSheetDialogFragment
import com.kjh.mynote.ui.common.dialog.yearmonths.SelectableYearMonthListBSDialog
import com.kjh.mynote.ui.features.place.search.result.DateRangeFilter
import com.kjh.mynote.ui.features.place.search.result.SearchPlaceNoteResultViewModel
import com.kjh.mynote.utils.DatePickerManager
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.setTextColorRes
import com.kjh.mynote.utils.extensions.toLocalDate
import com.kjh.mynote.utils.extensions.toMillis
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 18..
 * Description:
 */

class PlaceNoteSearchFilterBSDialog: BaseBottomSheetDialogFragment<BsdPlaceNoteSearchFilterDialogBinding>(
    { BsdPlaceNoteSearchFilterDialogBinding.inflate(it) }),
    SelectableYearMonthListBSDialog.YearMonthClickListener
{

    private val parentViewModel: SearchPlaceNoteResultViewModel by viewModels({ requireParentFragment() })
    private val viewModel: PlaceNoteSearchFilterBSDialogViewModel by viewModels()

    override fun onInitView() {
        with (binding) {
            tbToolbar.setResetClickListener(resetClickListener)
            tbToolbar.setCloseClickListener(closeBtnClickListener)

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
        val appliedMonthFilter = parentViewModel.uiState.value.monthFilter
        viewModel.setInitFilter(appliedMonthFilter)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState
                        .map { it.isChangedFilter() }
                        .distinctUntilChanged()
                        .collect { isChangedFilter ->
                            binding.btnApply.isEnable = isChangedFilter
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.tempMonthFilter }
                        .distinctUntilChanged()
                        .collect { monthFilter ->

                            when (monthFilter) {
                                is DateRangeFilter.Monthly -> {
                                    makeFilterUis(binding.tvMonthly)

                                    binding.tvSelectableMonth.text = monthFilter.getUiText()
                                }

                                is DateRangeFilter.MonthOne -> {
                                    makeFilterUis(binding.tvOneMonth)
                                }

                                is DateRangeFilter.MonthThree -> {
                                    makeFilterUis(binding.tvThreeMonth)
                                }

                                is DateRangeFilter.Directly -> {
                                    makeFilterUis(binding.tvDirectly)

                                    binding.tvStartDate.text = monthFilter.getStartDateUiText()
                                    binding.tvEndDate.text = monthFilter.getEndDateUiText()
                                }

                                else -> {
                                    makeFilterUis(null)
                                }
                            }
                        }
                }
            }
        }
    }

    private fun makeFilterUis(selectedView: AppCompatTextView?) = with (binding) {
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

    private val resetClickListener = OnClickListener {
        viewModel.resetTempMonthFilter()
    }

    private val monthlyClickListener = OnClickListener {
        viewModel.setTempMonthFilter(DateRangeFilter.Monthly(date = LocalDate.now()))
    }

    private val oneMonthClickListener = OnClickListener {
        viewModel.setTempMonthFilter(DateRangeFilter.MonthOne())
    }

    private val threeMonthClickListener = OnClickListener {
        viewModel.setTempMonthFilter(DateRangeFilter.MonthThree())
    }

    private val directlyClickListener = OnClickListener {
        viewModel.setTempMonthFilter(
            DateRangeFilter.Directly(
                startDate = LocalDate.now().minusYears(1),
                endDate = LocalDate.now()
            )
        )
    }

    private val selectableMonthClickListener = OnClickListener {
        val filterItem = viewModel.uiState.value.tempMonthFilter
        if (filterItem is DateRangeFilter.Monthly) {
            SelectableYearMonthListBSDialog.newInstance(
                selectedDate = filterItem.date
            ).show(childFragmentManager, SelectableYearMonthListBSDialog.TAG)
        }
    }

    private val startDateClickListener = OnClickListener {
        val monthFilter = viewModel.uiState.value.tempMonthFilter
        if (monthFilter is DateRangeFilter.Directly) {
            DatePickerManager.build(
                title = getString(R.string.select_start_date),
                selection = monthFilter.startDate.toMillis(),
                minDate = 0L,
                maxDate = monthFilter.endDate.toMillis(),
                positiveButtonClickAction = { date ->
                    viewModel.setTempMonthFilter(
                        DateRangeFilter.Directly(
                            startDate = date.toLocalDate(),
                            endDate = monthFilter.endDate
                        )
                    )
                }
            ).show(childFragmentManager, TAG_START_DATE_PICKER)
        }
    }

    private val endDateClickListener = OnClickListener {
        val monthFilter = viewModel.uiState.value.tempMonthFilter
        if (monthFilter is DateRangeFilter.Directly) {
            DatePickerManager.build(
                title = getString(R.string.select_end_date),
                selection = monthFilter.endDate.toMillis(),
                minDate = monthFilter.startDate.toMillis(),
                maxDate = LocalDate.now().toMillis(),
                positiveButtonClickAction = { date ->
                    viewModel.setTempMonthFilter(
                        DateRangeFilter.Directly(
                            startDate = monthFilter.startDate,
                            endDate = date.toLocalDate()
                        )
                    )
                }
            ).show(childFragmentManager, TAG_END_DATE_PICKER)
        }
    }

    private val closeBtnClickListener = OnClickListener {
        dismiss()
    }

    private val applyBtnClickListener = OnClickListener {
        if (binding.btnApply.isEnable) {
            val selectedFilter = viewModel.uiState.value.tempMonthFilter
            parentViewModel.applyMonthFilter(selectedFilter)
            dismiss()
        }
    }

    override fun onClickYearMonth(date: LocalDate) {
        viewModel.setTempMonthFilter(DateRangeFilter.Monthly(date = date))
    }

    companion object {
        const val TAG = "PlaceNoteSearchFilterBSDialog"

        private const val TAG_START_DATE_PICKER = "TAG_START_DATE_PICKER"
        private const val TAG_END_DATE_PICKER = "TAG_END_DATE_PICKER"

        private val defaultTextColor = R.color.black_800
        private val defaultBackground = R.drawable.shape_s_white_c_4_l_black_200

        private val selectedTextColor = R.color.purple
        private val selectedBackground = R.drawable.shape_s_white_c_4_l_purple

        fun newInstance() = PlaceNoteSearchFilterBSDialog()
    }
}