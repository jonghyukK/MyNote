package com.kjh.mynote.ui.features.place.calendar

import android.view.View
import android.view.View.OnClickListener
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.children
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
import com.kjh.mynote.utils.CalendarUtils.localDateToStringWithPattern
import com.kjh.mynote.utils.extensions.getDrawableCompat
import com.kjh.mynote.utils.extensions.makeInVisible
import com.kjh.mynote.utils.extensions.makeVisible
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.setTextColorRes
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

    private val parentViewModel: CalendarWithPlacesViewModel by viewModels({ requireParentFragment()} )

    private lateinit var currentYearMonth: LocalDate

    override fun onInitView() {
        onInitCalendarMonthView()
        setYearMonthTitle(parentViewModel.getSelectedDate())

        with (binding) {
            ivArrowLeft.setOnThrottleClickListener(onLeftArrowClickListener)
            ivArrowRight.setOnThrottleClickListener(onRightArrowClickListener)
        }
    }

    override fun onInitData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                parentViewModel.wholePlaceMap.collect { map ->
                    map.keys.forEach {
                        binding.calendarMonthView.notifyDateChanged(it)
                    }
                }
            }
        }
    }

    private fun onInitCalendarMonthView() {
        val daysOfWeek = daysOfWeek()
        val currentMonth = parentViewModel.getSelectedDate().yearMonth
        val startMonth = currentMonth.minusMonths(50)
        val endMonth = currentMonth.plusMonths(50)

        configureBinders(daysOfWeek)

        with (binding.calendarMonthView) {
            setup(startMonth, endMonth,daysOfWeek.first())
            monthScrollListener = monthViewScrollListener
            scrollToMonth(currentMonth)
        }
    }

    private fun setYearMonthTitle(date: LocalDate) = with (binding) {
        currentYearMonth = date
        tvCurrentYearMonth.text = localDateToStringWithPattern(
            date, "yyyy년 MM월"
        )
    }

    private fun selectDate(date: LocalDate) {
        parentViewModel.changeSelectedDay(date)
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

                // 일 View..
                val dayText = data.date.dayOfMonth.toString()
                val isSelectedDate = data.date == parentViewModel.getSelectedDate()
                val isDayInThisMonth = data.position == DayPosition.MonthDate

                val dayTextColor = if (isSelectedDate) R.color.white else R.color.black_900
                val dayBgColor = if (isSelectedDate) R.drawable.shape_s_black_900_c_999 else R.drawable.ripple_white

                with (container.binding.tvDateText) {
                    if (isDayInThisMonth) makeVisible() else makeInVisible()
                    text = dayText
                    setTextColorRes(dayTextColor)
                    background = context.getDrawableCompat(dayBgColor)
                }

                // 해당 일의 이벤트 존재 여부..
                val hasEventThisDay = parentViewModel.checkNoteInDay(data.date)

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

    private val monthViewScrollListener: MonthScrollListener = { date ->
        parentViewModel.getPlacesByMonth(date.yearMonth.atDay(1))

        setYearMonthTitle(date.yearMonth.atDay(1))
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