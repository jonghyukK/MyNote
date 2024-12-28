package com.kjh.mynote.ui.features.place.calendar.weekview.dialog

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.yearMonth
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.MonthScrollListener
import com.kizitonwose.calendar.view.ViewContainer
import com.kjh.mynote.R
import com.kjh.mynote.databinding.BsdCalendarMonthBinding
import com.kjh.mynote.databinding.CalendarDayBinding
import com.kjh.mynote.databinding.CalendarHeaderBinding
import com.kjh.mynote.ui.base.BaseBottomSheetDialogFragment
import com.kjh.mynote.ui.features.place.calendar.weekview.PlaceNoteWeekViewTypeViewModel
import com.kjh.mynote.ui.features.place.calendar.weekview.PlaceNoteWeekViewUiState
import com.kjh.mynote.ui.features.place.detail.PlaceNoteDetailMenuBSDialog.PlaceNoteDetailMenuClickListener
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.getDrawableCompat
import com.kjh.mynote.utils.extensions.makeInVisible
import com.kjh.mynote.utils.extensions.makeVisible
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.setBackgroundRes
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.setTextColorRes
import com.kjh.mynote.utils.extensions.toStringWithPattern
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 15..
 * Description:
 */

@AndroidEntryPoint
class CalendarMonthBSDialog
    : BaseBottomSheetDialogFragment<BsdCalendarMonthBinding>({ BsdCalendarMonthBinding.inflate(it) }) {

    private val viewModel: CalendarMonthViewModel by viewModels()

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
            calendarMonthView.customDayViewResource = R.layout.calendar_day
            calendarMonthView.selectedDayBgRes = R.drawable.shape_s_black_900_c_999

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
        ) = CalendarMonthBSDialog().apply {
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