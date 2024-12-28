package com.kjh.mynote.ui.common.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.children
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
import com.kjh.mynote.databinding.CalendarHeaderBinding
import com.kjh.mynote.databinding.CalendarPurchaseNoteDayBinding
import com.kjh.mynote.databinding.CommonLayoutMyMonthCalendarBinding
import com.kjh.mynote.utils.extensions.getDrawableCompat
import com.kjh.mynote.utils.extensions.makeInVisible
import com.kjh.mynote.utils.extensions.makeVisible
import com.kjh.mynote.utils.extensions.setTextColorRes
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 7..
 * Description:
 */
class MyMonthCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    var selectedDayTextColorRes = R.color.white
    var unselectedDayTextColorRes = R.color.black_900
    var afterDayTextColorRes = R.color.black_500

    var selectedDayBgRes = R.drawable.shape_oval_s_purple_200_wh_30
    var unselectedDayBgRes = R.drawable.ripple_white
    var todayBgRes = R.drawable.shape_c_999_l_purple

    var customDayViewResource: Int = R.layout.calendar_day
        set(value) {
            binding.calendarView.dayViewResource = value
            field = value
        }

    private val binding = CommonLayoutMyMonthCalendarBinding.inflate(LayoutInflater.from(context), this, true)

    private val todayDate: LocalDate = LocalDate.now()
    private var dayClickAction: (LocalDate) -> Unit = {}

    private var selectedDate: LocalDate = LocalDate.now()
    private var hasEventDates: List<LocalDate> = emptyList()

    init {
        onInitCalendarMonthView()
    }

    private fun onInitCalendarMonthView() {
        val daysOfWeek = daysOfWeek()
        val currentMonth = LocalDate.now().yearMonth
        val startMonth = currentMonth.minusMonths(50)
        val endMonth = currentMonth.plusMonths(0)

        configureBinders(daysOfWeek)

        binding.calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        binding.calendarView.scrollToMonth(currentMonth)
    }

    private fun configureBinders(daysOfWeek: List<DayOfWeek>) {
        class DayViewContainer(view: View): ViewContainer(view) {
            lateinit var day: CalendarDay
            val binding = CalendarPurchaseNoteDayBinding.bind(view)

            init {
                view.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        dayClickAction.invoke(day.date)
                    }
                }
            }
        }

        binding.calendarView.dayBinder = object: MonthDayBinder<DayViewContainer> {
            override fun create(view: View): DayViewContainer = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                container.view.isClickable = !data.date.isAfter(todayDate)

                // 일 View..
                val isSelectedDate = data.date == selectedDate
                val isDayInThisMonth = data.position == DayPosition.MonthDate
                val isAfterDayFromToday = data.date.isAfter(todayDate)
                val isToday = data.date == todayDate

                val dayTextColor = if (isAfterDayFromToday) {
                    afterDayTextColorRes
                } else if (isSelectedDate) {
                    selectedDayTextColorRes
                } else {
                    unselectedDayTextColorRes
                }

                val dayBgRes = if (isSelectedDate) {
                    selectedDayBgRes
                } else if (isToday) {
                    todayBgRes
                } else {
                    unselectedDayBgRes
                }

                with (container.binding.tvDateText) {
                    text = data.date.dayOfMonth.toString()
                    background = context.getDrawableCompat(dayBgRes)
                    setTextColorRes(dayTextColor)
                    if (isDayInThisMonth) makeVisible() else makeInVisible()
                }

                // 해당 일의 이벤트 존재 여부..
                if (isDayInThisMonth && !isAfterDayFromToday && hasEventDates.contains(data.date)) {
                    container.binding.tvHas.makeVisible()
                } else {
                    container.binding.tvHas.makeInVisible()
                }

            }
        }

        class MonthViewContainer(view: View): ViewContainer(view) {
            val legendLayout = CalendarHeaderBinding.bind(view).legendLayout.root
        }

        binding.calendarView.monthHeaderBinder = object: MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View): MonthViewContainer = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                if (container.legendLayout.tag == null) {
                    container.legendLayout.tag = true
                    container.legendLayout.children.map { it as AppCompatTextView }
                        .forEachIndexed { index, tv ->
                            tv.text = daysOfWeek[index]
                                .getDisplayName(TextStyle.SHORT, Locale.KOREAN).also {
                                    if (it == "일") {
                                        tv.setTextColorRes(dayOfWeekSundayTextColorRes)
                                    } else {
                                        tv.setTextColorRes(dayOfWeekNormalTextColorRes)
                                    }
                                }
                        }
                }
            }
        }
    }

    fun updateCalendarUI(data: Pair<LocalDate, List<LocalDate>>) {
        val (selectedDay, hasEventDays) = data

        if (selectedDay == selectedDate && hasEventDays == hasEventDates)
            return

        selectedDate = selectedDay
        hasEventDates = hasEventDays

        binding.calendarView.scrollToMonth(selectedDay.yearMonth)
        binding.calendarView.notifyCalendarChanged()
    }

    fun setDayClickAction(action: (LocalDate) -> Unit) {
        dayClickAction = action
    }

    fun setMonthScrollListener(listener: MonthScrollListener) {
        binding.calendarView.monthScrollListener = listener
    }

    fun smoothScrollToMonth(month: LocalDate) {
        binding.calendarView.smoothScrollToMonth(month.yearMonth)
    }

    companion object {
        private val dayOfWeekSundayTextColorRes = R.color.red_500
        private val dayOfWeekNormalTextColorRes = R.color.black_800
    }
}