package com.kjh.mynote.ui.features.main.place

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.view.View
import android.view.View.OnClickListener
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.WeekDayPosition
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekDayBinder
import com.kjh.data.model.PlaceNoteModel
import com.kjh.mynote.R
import com.kjh.mynote.databinding.CalendarDayBinding
import com.kjh.mynote.databinding.FragmentWeekCalendarBinding
import com.kjh.mynote.ui.base.BaseBottomSheetDialogFragment
import com.kjh.mynote.ui.base.BaseFragment
import com.kjh.mynote.ui.features.note.make.MakeNoteActivity
import com.kjh.mynote.utils.CalendarUtils
import com.kjh.mynote.utils.CalendarUtils.localDateToStringWithPattern
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.getDrawableCompat
import com.kjh.mynote.utils.extensions.makeInVisible
import com.kjh.mynote.utils.extensions.makeVisible
import com.kjh.mynote.utils.extensions.parcelable
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.setTextColorRes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


@AndroidEntryPoint
class MyPlaceFragment: BaseFragment<FragmentWeekCalendarBinding>({ FragmentWeekCalendarBinding.inflate(it) }) {

    private val viewModel: MyPlaceViewModel by viewModels()

    private val listAdapter: CalendarPlaceListAdapter by lazy {
        CalendarPlaceListAdapter(placeItemClickAction)
    }

    private var prevSelectedDate: LocalDate = LocalDate.now()

    override fun onInitView() {
        onInitWeekView()

        with (binding) {
            rvNotes.apply {
                adapter = listAdapter
            }

            clYearMonth.setOnThrottleClickListener(currentYearMonthClickListener)
            emptyView.btnMakePlace.setOnThrottleClickListener(makeNoteButtonClickListener)
            fabMakeNote.setOnThrottleClickListener(makeNoteButtonClickListener)
        }
    }

    override fun onInitData() {
        viewModel.getPlacesInMonth()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.placeItemsInDayFlow
                        .collect { placeItems ->
                            binding.emptyView.root.isVisible = placeItems.isEmpty()
                            listAdapter.submitList(placeItems)
                        }
                }

                launch {
                    viewModel.selectedDate.collect { selectedDate ->
                        binding.tvCurrentYearMonth.text = localDateToStringWithPattern(
                            selectedDate, "yyyy년 MM월"
                        )

                        if (selectedDate != prevSelectedDate) {
                            binding.calendarView.notifyDateChanged(prevSelectedDate)

                            if (selectedDate.monthValue != prevSelectedDate.monthValue) {
                                val currentMonth = YearMonth.of(selectedDate.year, selectedDate.month)
                                binding.calendarView.setup(
                                    currentMonth.minusMonths(0).atStartOfMonth(),
                                    currentMonth.plusMonths(0).atEndOfMonth(),
                                    firstDayOfWeekFromLocale(Locale.KOREAN)
                                )
                            }

                            prevSelectedDate = selectedDate
                        }

                        binding.calendarView.notifyDateChanged(selectedDate)
                        binding.calendarView.scrollToWeek(selectedDate)
                    }
                }

                launch {
                    viewModel.weekEvents.collect { eventsMap ->
                        eventsMap.keys.forEach { date ->
                            binding.calendarView.notifyDateChanged(date)
                        }
                    }
                }
            }
        }
    }

    private fun onInitWeekView() {
        class DayViewContainer(view: View): ViewContainer(view) {
            val dayBinding = CalendarDayBinding.bind(view)
            lateinit var day: WeekDay

            init {
                view.setOnClickListener {
                    if (day.position != WeekDayPosition.RangeDate) {
                        return@setOnClickListener
                    }

                    dateClickAction.invoke(day.date)
                }
            }
        }

        with (binding.calendarView) {
            dayBinder = object: WeekDayBinder<DayViewContainer> {
                override fun create(view: View) = DayViewContainer(view)
                override fun bind(container: DayViewContainer, data: WeekDay) {
                    container.day = data
                    bindDay(container.dayBinding, data)
                }
            }

            val currentMonth = YearMonth.now()
            setup(
                currentMonth.minusMonths(0).atStartOfMonth(),
                currentMonth.plusMonths(0).atEndOfMonth(),
                firstDayOfWeekFromLocale(Locale.KOREAN)
            )

            scrollToDate(LocalDate.now())
        }
    }

    private fun bindDay(dayBinding: CalendarDayBinding, data: WeekDay) {
        with (dayBinding) {
            tvDayText.text = data.date.dayOfWeek.getDisplayName(
                TextStyle.SHORT,
                Locale.KOREAN
            ).also {
                if (it == "일") {
                    tvDayText.setTextColorRes(R.color.red_500)
                } else {
                    tvDayText.setTextColorRes(R.color.black_800)
                }
            }

            if (data.position == WeekDayPosition.RangeDate) {
                tvDateText.makeVisible()
                tvDateText.text = DateTimeFormatter.ofPattern("dd").format(data.date)

                tvHas.visibility = if (viewModel.checkHasNoteInDay(data.date))
                    View.VISIBLE else View.INVISIBLE

                if (data.date == viewModel.getSelectedDate()) {
                    tvDateText.setTextColorRes(R.color.white)
                    tvDateText.background =
                        requireContext().getDrawableCompat(R.drawable.shape_s_black_900_c_999)
                } else {
                    tvDateText.setTextColorRes(R.color.black_900)
                    tvDateText.background =
                        requireContext().getDrawableCompat(R.drawable.ripple_white)
                }
            } else {
                tvDateText.makeInVisible()
                tvHas.makeInVisible()
            }
        }
    }

    private val makeNoteResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val insertDate = result.data?.getLongExtra(
                AppConstants.INTENT_PLACE_VISIT_DATE, -1
            ) ?: return@registerForActivityResult

            val convertedLocalDate = CalendarUtils.millisToLocalDate(insertDate)
            viewModel.changeSelectedDate(convertedLocalDate)
            viewModel.getPlacesInMonth()

            binding.calendarView.scrollToWeek(convertedLocalDate)
        }
    }

    private val currentYearMonthClickListener = OnClickListener {
        CalendarMonthBSDialog().show(childFragmentManager, "TEST")
    }

    private val makeNoteButtonClickListener = OnClickListener {
        Intent(requireContext(), MakeNoteActivity::class.java).apply {
            makeNoteResultLauncher.launch(this)
        }
    }

    private val dateClickAction: (LocalDate) -> Unit = { localDate ->
        viewModel.changeSelectedDate(localDate)
    }

    private val placeItemClickAction: (PlaceNoteModel) -> Unit = { placeItem ->
        Timber.tag("abc123").e("clicked: $placeItem")
    }

    companion object {
        const val TAG = "MyPlaceFragment"
        fun newInstance() = MyPlaceFragment()
    }
}
