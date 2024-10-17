package com.kjh.mynote.ui.common.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.WeekDayPosition
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekDayBinder
import com.kjh.mynote.R
import com.kjh.mynote.databinding.CalendarDayBinding
import com.kjh.mynote.databinding.CommonLayoutMyWeekCalendarBinding
import com.kjh.mynote.utils.extensions.getDrawableCompat
import com.kjh.mynote.utils.extensions.makeInVisible
import com.kjh.mynote.utils.extensions.makeVisible
import com.kjh.mynote.utils.extensions.setTextColorRes
import java.time.LocalDate
import java.time.YearMonth
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

    private var selectedDate: LocalDate = LocalDate.now()

    private var dayClickAction: (LocalDate) -> Unit = {}

    private var checkEventThisDay: (LocalDate) -> Boolean = { false }

    init {
        initWeekView()
    }

    fun updateWeekData(newDate: LocalDate) = with (binding.calendarView) {
        val currentMonth = YearMonth.of(newDate.year, newDate.month)
        updateWeekData(
            currentMonth.minusMonths(0).atStartOfMonth(),
            currentMonth.plusMonths(0).atEndOfMonth(),
            firstDayOfWeekFromLocale(Locale.KOREAN)
        )

        notifyCalendarChanged()
        scrollToWeek(newDate)
    }

    fun notifyDateChanged(date: LocalDate) {
        binding.root.notifyDateChanged(date)
    }

    fun scrollToWeek(date: LocalDate) {
        binding.root.scrollToWeek(date)
    }

    fun updateSelectedDate(newDate: LocalDate) {
        selectedDate = newDate
    }

    fun setDayClickAction(action: (LocalDate) -> Unit) {
        dayClickAction = action
    }

    fun setDayHasEventChecker(checker: (LocalDate) -> Boolean) {
        checkEventThisDay = checker
    }

    private fun initWeekView() {
        class DayViewContainer(view: View) : ViewContainer(view) {
            val dayBinding = CalendarDayBinding.bind(view)
            lateinit var day: WeekDay

            init {
                view.setOnClickListener {
                    if (day.position != WeekDayPosition.RangeDate)
                        return@setOnClickListener

                    dayClickAction.invoke(day.date)
                }
            }
        }

        binding.calendarView.dayBinder = object : WeekDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: WeekDay) {
                container.day = data
                bindDay(
                    dayBinding = container.dayBinding,
                    data = data,
                    selectedDate = selectedDate,
                    hasNoteItem = checkEventThisDay.invoke(data.date)
                )
            }
        }

        setupWeekData()
    }

    private fun setupWeekData() = with(binding.calendarView) {
        val currentMonth = YearMonth.now()
        setup(
            currentMonth.minusMonths(0).atStartOfMonth(),
            currentMonth.plusMonths(0).atEndOfMonth(),
            firstDayOfWeekFromLocale(Locale.KOREAN)
        )
        scrollToDate(LocalDate.now())
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

        val dayTextColor = if (isSelectedDate) selectedDayTextColor else unselectedDayTextColor
        val dayBgColor = if (isSelectedDate) selectedDayBgColor else unselectedDayBgColor
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

    companion object {
        private val dayOfWeekSundayTextColor = R.color.red_500
        private val dayOfWeekNormalTextColor = R.color.black_800

        private val selectedDayTextColor = R.color.white
        private val unselectedDayTextColor = R.color.black_900

        private val selectedDayBgColor = R.drawable.shape_s_black_900_c_999
        private val unselectedDayBgColor = R.drawable.ripple_white
    }
}