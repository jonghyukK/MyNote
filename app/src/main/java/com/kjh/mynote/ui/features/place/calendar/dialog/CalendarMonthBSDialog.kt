package com.kjh.mynote.ui.features.place.calendar.dialog

import android.view.View
import android.view.View.OnClickListener
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
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
import com.kjh.mynote.ui.features.place.calendar.PlaceNoteCalendarHomeViewModel
import com.kjh.mynote.utils.extensions.getDrawableCompat
import com.kjh.mynote.utils.extensions.makeInVisible
import com.kjh.mynote.utils.extensions.makeVisible
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.setTextColorRes
import com.kjh.mynote.utils.extensions.toStringWithPattern
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

class CalendarMonthBSDialog
    : BaseBottomSheetDialogFragment<BsdCalendarMonthBinding>({ BsdCalendarMonthBinding.inflate(it) }) {

    private val viewModel: PlaceNoteCalendarHomeViewModel by activityViewModels()

    private var todayDate = LocalDate.now()
    private lateinit var currentYearMonth: LocalDate

    override fun onInitView() {
        onInitCalendarMonthView()
        setMonthTextAndHideRightBtnWhenTodayMonth(viewModel.getSelectedDate())

        with (binding) {
            ivArrowLeft.setOnThrottleClickListener(onLeftArrowClickListener)
            ivArrowRight.setOnThrottleClickListener(onRightArrowClickListener)
        }
    }

    override fun onInitData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.groupedNotesByLocalDateFlow.collect { eventLists ->
                    eventLists.keys.forEach {
                        binding.calendarMonthView.notifyDateChanged(it)
                    }
                }
            }
        }
    }

    private fun onInitCalendarMonthView() {
        val daysOfWeek = daysOfWeek()
        val currentMonth = viewModel.getSelectedDate().yearMonth
        val startMonth = currentMonth.minusMonths(50)
        val endMonth = currentMonth.plusMonths(0)

        configureBinders(daysOfWeek)

        with (binding.calendarMonthView) {
            setup(startMonth, endMonth, daysOfWeek.first())
            monthScrollListener = monthViewScrollListener
            scrollToMonth(currentMonth)
        }
    }

    private fun setMonthTextAndHideRightBtnWhenTodayMonth(date: LocalDate) = with (binding) {
        currentYearMonth = date
        tvCurrentYearMonth.text = date.toStringWithPattern("yyyy년 MM월")
        ivArrowRight.isVisible =
            date.withDayOfMonth(1) != todayDate.withDayOfMonth(1)
    }

    private fun selectDate(date: LocalDate) {
        viewModel.setSelectedDate(date)
        dialog?.dismiss()
    }

    private fun configureBinders(daysOfWeek: List<DayOfWeek>) {
        class DayViewContainer(view: View): ViewContainer(view) {
            lateinit var day: CalendarDay
            val binding = CalendarDayBinding.bind(view)

            init {
                view.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        selectDate(day.date)
                    }
                }
            }
        }

        binding.calendarMonthView.dayBinder = object: MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                container.view.isClickable = !data.date.isAfter(todayDate)

                // 일 View..
                val isSelectedDate = data.date == viewModel.getSelectedDate()
                val isDayInThisMonth = data.position == DayPosition.MonthDate
                val isAfterDayFromToday = data.date.isAfter(todayDate)
                val isToday = data.date == todayDate

                with (container.binding.tvDateText) {
                    if (isDayInThisMonth) makeVisible() else makeInVisible()
                    text = data.date.dayOfMonth.toString()
                    background = context.getDrawableCompat(getDayBgColorRes(isSelectedDate, isToday))
                    setTextColorRes(getDayTextColorRes(isSelectedDate, isAfterDayFromToday))
                }

                // 해당 일의 이벤트 존재 여부..
                val hasEventThisDay = viewModel.groupedNotesByLocalDateFlow.value.keys.contains(data.date)

                with (container.binding.tvHas) {
                    if (isDayInThisMonth && hasEventThisDay) makeVisible() else makeInVisible()
                }
            }
        }

        class MonthViewContainer(view: View): ViewContainer(view) {
            val legendLayout = CalendarHeaderBinding.bind(view).legendLayout.root
        }

        binding.calendarMonthView.monthHeaderBinder =
            object: MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                    if (container.legendLayout.tag == null) {
                        container.legendLayout.tag = true
                        container.legendLayout.children.map { it as AppCompatTextView }
                            .forEachIndexed { index, tv ->
                                tv.text = daysOfWeek[index]
                                    .getDisplayName(TextStyle.SHORT, Locale.KOREAN).also {
                                        if (it == "일") {
                                            tv.setTextColorRes(R.color.red_500)
                                        } else {
                                            tv.setTextColorRes(R.color.black_800)
                                        }
                                    }
                            }
                    }
                }
            }
    }

    private fun getDayTextColorRes(isSelectedDay: Boolean, isAfterDayFromToday: Boolean) = when {
        isSelectedDay -> R.color.white
        isAfterDayFromToday -> R.color.black_500
        else -> R.color.black_900
    }

    private fun getDayBgColorRes(isSelectedDay: Boolean, isToday: Boolean) = when {
        isSelectedDay -> R.drawable.shape_s_black_900_c_999
        isToday -> R.drawable.shape_c_999_l_purple
        else -> R.drawable.ripple_white
    }

    private val monthViewScrollListener: MonthScrollListener = { date ->
        setMonthTextAndHideRightBtnWhenTodayMonth(date.yearMonth.atDay(1))
    }

    private val onLeftArrowClickListener = OnClickListener {
        binding.calendarMonthView.smoothScrollToMonth(
            currentYearMonth.minusMonths(1).yearMonth)
    }

    private val onRightArrowClickListener = OnClickListener {
        binding.calendarMonthView.smoothScrollToMonth(
            currentYearMonth.plusMonths(1).yearMonth)
    }

    companion object {
        const val TAG = "CalendarMonthBSDialog"
        fun newInstance() = CalendarMonthBSDialog()
    }
}