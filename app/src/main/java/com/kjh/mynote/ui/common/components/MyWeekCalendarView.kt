package com.kjh.mynote.ui.common.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.WeekDayPosition
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.core.yearMonth
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekDayBinder
import com.kjh.mynote.R
import com.kjh.mynote.databinding.CalendarDayBinding
import com.kjh.mynote.databinding.CommonLayoutMyWeekCalendarBinding
import com.kjh.mynote.utils.extensions.addClickAnimation
import com.kjh.mynote.utils.extensions.getDrawableCompat
import com.kjh.mynote.utils.extensions.getWeekStartAndEndDates
import com.kjh.mynote.utils.extensions.makeInVisible
import com.kjh.mynote.utils.extensions.makeVisible
import com.kjh.mynote.utils.extensions.setTextColorRes
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 17..
 * Description:
 */

class MyWeekCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    private val binding = CommonLayoutMyWeekCalendarBinding.inflate(LayoutInflater.from(context), this, true)

    private var isInit = false
    private var selectedDate: LocalDate = LocalDate.now()
    private var thisMonthEventList: List<LocalDate> = emptyList()
    private var dayClickAction: ((LocalDate) -> Unit)? = null

    private var isOnlyThisWeek: Boolean = false

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyWeekCalendarView, defStyleAttr, 0)

        isOnlyThisWeek = typedArray.getBoolean(R.styleable.MyWeekCalendarView_isOnlyThisWeek, false)

        initWeekView()

        typedArray.recycle()
    }

    private fun initWeekView() {
        class DayViewContainer(view: View) : ViewContainer(view) {
            val dayBinding = CalendarDayBinding.bind(view)
            lateinit var day: WeekDay

            init {
                view.addClickAnimation()
                view.setOnClickListener {
                    if (day.position != WeekDayPosition.RangeDate)
                        return@setOnClickListener

                    dayClickAction?.invoke(day.date)
                }
            }
        }

        binding.calendarView.dayBinder = object : WeekDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: WeekDay) {
                container.day = data
                container.view.isClickable = !data.date.isAfter(LocalDate.now())

                bindDay(
                    dayBinding = container.dayBinding,
                    data = data,
                    selectedDate = selectedDate,
                    hasNoteItem = thisMonthEventList.contains(data.date)
                )
            }
        }

        setupWeekData()
    }

    private fun setupWeekData() = with(binding.calendarView) {
        val (startDate, endDate) = getWeekViewStartAndEndDate(selectedDate)
        setup(startDate, endDate, getDaysOfWeek())
        scrollToDate(LocalDate.now())
    }

    private fun updateWeekData(newDate: LocalDate) = with (binding.calendarView) {
        val (startDate, endDate) = getWeekViewStartAndEndDate(newDate)
        updateWeekData(startDate, endDate, getDaysOfWeek())

        notifyCalendarChanged()
        scrollToWeek(newDate)
    }

    private fun bindDay(
        dayBinding: CalendarDayBinding,
        data: WeekDay,
        selectedDate: LocalDate,
        hasNoteItem: Boolean
    ) {
        // 요일 Layouts..
        val dayOfWeekText = data.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
        val isDayOfWeekSunday = dayOfWeekText == "일"
        val dayOfWeekTextColor = if (isDayOfWeekSunday)
            dayOfWeekSundayTextColor else dayOfWeekNormalTextColor

        with (dayBinding.tvDayText) {
            text = dayOfWeekText
            setTextColorRes(dayOfWeekTextColor)
        }

        // 일 Layouts..
        val isSelectedDate = data.date == selectedDate
        val isDayInThisMonth = data.position == WeekDayPosition.RangeDate
        val isAfterDayFromToday = data.date.isAfter(LocalDate.now())
        val isToday = data.date == LocalDate.now()

        val dayTextColor = if (isAfterDayFromToday) {
            afterDayTextColor
        } else if (isSelectedDate) {
            selectedDayTextColor
        } else {
            unselectedDayTextColor
        }

        val dayBgColor = if (isSelectedDate) {
            selectedDayBgColor
        } else if (isToday) {
            todayBgBgColor
        } else {
            unselectedDayBgColor
        }

        val dayText = DateTimeFormatter.ofPattern("d").format(data.date)

        with (dayBinding.tvDateText) {
            if (isDayInThisMonth) makeVisible() else makeInVisible()
            text = dayText
            setTextColorRes(dayTextColor)
            background = context.getDrawableCompat(dayBgColor)
        }

        // 해당 일의 이벤트 존재 여부..
        with (dayBinding.tvHas) {
            if (isDayInThisMonth && hasNoteItem) makeVisible() else makeInVisible()
        }
    }

    fun updateSelectDayWithEventDates(selectedDateAndEventDates: Pair<LocalDate, List<LocalDate>>) {
        val (newDay, eventList) = selectedDateAndEventDates

        val isYearOrMonthDifferent =
            selectedDate.year != newDay.year || selectedDate.month != newDay.month
        val isSameDateDifferentEvents =
            newDay == selectedDate && thisMonthEventList != eventList

        when {
            !isInit -> {
                selectedDate = newDay
                thisMonthEventList = eventList

                if (isYearOrMonthDifferent) {
                    updateWeekData(selectedDate)
                } else {
                    binding.root.notifyCalendarChanged()
                }

                isInit = true
            }
            isYearOrMonthDifferent -> {
                selectedDate = newDay
                thisMonthEventList = eventList
                updateWeekData(newDay)
            }
            isSameDateDifferentEvents -> {
                selectedDate = newDay
                thisMonthEventList = eventList
                binding.root.notifyCalendarChanged()
            }
            else -> {
                thisMonthEventList = eventList
                binding.root.notifyDateChanged(selectedDate)
                binding.root.notifyDateChanged(newDay)
                binding.root.scrollToWeek(newDay)
                selectedDate = newDay
            }
        }
    }

    fun setDayClickAction(action: (LocalDate) -> Unit) {
        dayClickAction = action
    }

    private fun getWeekViewStartAndEndDate(targetDate: LocalDate): Pair<LocalDate, LocalDate> =
        if (isOnlyThisWeek) {
            targetDate.getWeekStartAndEndDates()
        } else {
            targetDate.yearMonth.minusMonths(0).atStartOfMonth() to
                    targetDate.yearMonth.plusMonths(0).atEndOfMonth()
        }

    private fun getDaysOfWeek() =
        if (isOnlyThisWeek) {
            daysOfWeek(DayOfWeek.MONDAY).first()
        } else {
            firstDayOfWeekFromLocale(Locale.KOREAN)
        }

    companion object {
        private val dayOfWeekSundayTextColor = R.color.red_500
        private val dayOfWeekNormalTextColor = R.color.black_800

        private val selectedDayTextColor = R.color.white
        private val unselectedDayTextColor = R.color.black_900

        private val selectedDayBgColor = R.drawable.shape_s_black_900_c_999
        private val unselectedDayBgColor = R.drawable.ripple_white
        private val todayBgBgColor = R.drawable.shape_c_999_l_purple

        private val afterDayTextColor = R.color.black_500

    }
}