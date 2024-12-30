package com.kjh.mynote.ui.features.place.home.weekview.dialog

import android.content.Context
import android.os.Bundle
import android.view.View.OnClickListener
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kizitonwose.calendar.view.MonthScrollListener
import com.kjh.mynote.R
import com.kjh.mynote.databinding.BsdCalendarMonthBinding
import com.kjh.mynote.ui.base.BaseBottomSheetDialogFragment
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.toStringWithPattern
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 15..
 * Description:
 */

@AndroidEntryPoint
class MonthlyEventCalendarBSDialog
    : BaseBottomSheetDialogFragment<BsdCalendarMonthBinding>({ BsdCalendarMonthBinding.inflate(it) }) {

    private val viewModel: MonthlyEventCalendarViewModel by viewModels()

    private var calendarDayClickListener: CalendarMonthDialogDayClickListener? = null
    private val today = LocalDate.now()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        calendarDayClickListener = when {
            parentFragment is CalendarMonthDialogDayClickListener -> parentFragment as CalendarMonthDialogDayClickListener
            context is CalendarMonthDialogDayClickListener -> context
            else -> throw IllegalStateException("Parent must implement CalendarMonthDialogDayClickListener")
        }
    }

    override fun onInitView() {
        with(binding) {
            calendarMonthView.updateCalendarUI(viewModel.selectedDate to viewModel.eventDays)
            calendarMonthView.setMonthScrollListener(monthScrollListener)
            calendarMonthView.setDayClickAction(monthDayClickAction)

            ivArrowLeft.setOnThrottleClickListener(onLeftArrowClickListener)
            ivArrowRight.setOnThrottleClickListener(onRightArrowClickListener)
        }
    }

    override fun onInitData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.currentMonth.collect { month ->
                        binding.tvCurrentYearMonth.text = month.toStringWithPattern("yyyy년 M월")
                        binding.ivArrowRight.isVisible =
                            month.withDayOfMonth(1) != today.withDayOfMonth(1)
                    }
                }

                launch {
                    viewModel.swipeMonthEvent.collect { date ->
                        binding.calendarMonthView.smoothScrollToMonth(date)
                    }
                }
            }
        }
    }

    private val monthDayClickAction: (LocalDate) -> Unit = { date ->
        calendarDayClickListener?.onClickDay(date)
        dialog?.dismiss()
    }

    private val monthScrollListener: MonthScrollListener = { date ->
        viewModel.changeMonth(date.yearMonth.atDay(1))
    }

    private val onLeftArrowClickListener = OnClickListener {
        viewModel.swipePrevMonth()
    }

    private val onRightArrowClickListener = OnClickListener {
        viewModel.swipeNextMonth()
    }

    interface CalendarMonthDialogDayClickListener {
        fun onClickDay(date: LocalDate)
    }

    companion object {
        const val TAG = "CalendarMonthBSDialog"

        fun newInstance(
            selectedDate: LocalDate,
            eventDays: List<LocalDate>,
        ) = MonthlyEventCalendarBSDialog().apply {
            arguments = Bundle().apply {
                putString(AppConstants.INTENT_DATE, selectedDate.toString())
                putStringArrayList(
                    AppConstants.INTENT_DATE_LIST,
                    ArrayList(eventDays.map { it.toString() })
                )
            }
        }
    }
}