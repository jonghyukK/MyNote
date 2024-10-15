package com.kjh.mynote.ui.features.main.place

import android.view.View
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
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.kjh.mynote.R
import com.kjh.mynote.databinding.BsdCalendarMonthBinding
import com.kjh.mynote.databinding.CalendarDayBinding
import com.kjh.mynote.databinding.CalendarHeaderBinding
import com.kjh.mynote.ui.base.BaseBottomSheetDialogFragment
import com.kjh.mynote.utils.CalendarUtils.localDateToStringWithPattern
import com.kjh.mynote.utils.CalendarUtils.yearMonthToStringWithPattern
import com.kjh.mynote.utils.extensions.getDrawableCompat
import com.kjh.mynote.utils.extensions.makeInVisible
import com.kjh.mynote.utils.extensions.makeVisible
import com.kjh.mynote.utils.extensions.setTextColorRes
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 15..
 * Description:
 */

class CalendarMonthBSDialog
    : BaseBottomSheetDialogFragment<BsdCalendarMonthBinding>({ BsdCalendarMonthBinding.inflate(it) }) {

        private val parentViewModel: MyPlaceViewModel by viewModels({ requireParentFragment()} )

    override fun onInitView() {
        onInitCalendarMonthView()
    }

    override fun onInitData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {

                }
            }
        }
    }

    private fun onInitCalendarMonthView() {
        val initCurrentDate = parentViewModel.getSelectedDate()

        binding.tvCurrentYearMonth.text = localDateToStringWithPattern(
            initCurrentDate, "yyyy년 MM월"
        )

        val daysOfWeek = daysOfWeek()
        val currentMonth = YearMonth.from(initCurrentDate)
        val startMonth = currentMonth.minusMonths(50)
        val endMonth = currentMonth.plusMonths(50)

        configureBinders(daysOfWeek)
        binding.calendarMonthView.setup(startMonth, endMonth,daysOfWeek.first())
        binding.calendarMonthView.scrollToMonth(currentMonth)

        binding.calendarMonthView.monthScrollListener = {
            binding.tvCurrentYearMonth.text = yearMonthToStringWithPattern(
                it.yearMonth, "yyyy년 MM월"
            )

            parentViewModel.getPlacesInMonth(it.yearMonth.atDay(1))
        }
    }

    private fun selectDate(date: LocalDate) {
        parentViewModel.changeSelectedDate(date)
        dialog?.hide()
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

                with (container.binding) {
                    tvDateText.text = data.date.dayOfMonth.toString()

                    if (data.position == DayPosition.MonthDate) {
                        tvDateText.makeVisible()

                        tvHas.visibility = if (parentViewModel.checkHasNoteInDay(data.date))
                            View.VISIBLE else View.INVISIBLE

                        when (data.date) {
                            parentViewModel.getSelectedDate() -> {
                                tvDateText.setTextColorRes(R.color.white)
                                tvDateText.background =
                                    requireContext().getDrawableCompat(R.drawable.shape_s_black_900_c_999)
                            }
                            else -> {
                                tvDateText.setTextColorRes(R.color.black_900)
                                tvDateText.background =
                                    requireContext().getDrawableCompat(R.drawable.ripple_white)
                            }
                        }
                    } else {
                        tvDateText.makeInVisible()
                        tvHas.makeInVisible()
                    }
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
                                tv.text = daysOfWeek[index].getDisplayName(
                                    TextStyle.SHORT, Locale.KOREAN
                                ).also {
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
}