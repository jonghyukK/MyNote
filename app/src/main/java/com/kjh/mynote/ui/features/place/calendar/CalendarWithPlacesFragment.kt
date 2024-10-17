package com.kjh.mynote.ui.features.place.calendar

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.view.View.OnClickListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.data.model.PlaceNoteModel
import com.kjh.mynote.databinding.FragmentCalendarWithPlacesBinding
import com.kjh.mynote.ui.base.BaseFragment
import com.kjh.mynote.ui.features.place.make.MakePlaceNoteActivity
import com.kjh.mynote.utils.CalendarUtils
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate


@AndroidEntryPoint
class CalendarWithPlacesFragment
    : BaseFragment<FragmentCalendarWithPlacesBinding>({ FragmentCalendarWithPlacesBinding.inflate(it) }) {

    private val viewModel: CalendarWithPlacesViewModel by viewModels()

    private val listAdapter: CalendarPlaceListAdapter by lazy {
        CalendarPlaceListAdapter(placeItemClickAction)
    }

    override fun onInitView() {
        with (binding) {
            rvNotes.apply {
                setHasFixedSize(true)
                itemAnimator = null
                adapter = listAdapter
            }

            myWeekCalendar.apply {
                setDayClickAction(weekDayClickAction)
                setDayHasEventChecker(weekDayHasEventChecker)
            }

            clYearMonth.setOnThrottleClickListener(currentYearMonthClickListener)
            emptyView.btnMakePlace.setOnThrottleClickListener(makeNoteButtonClickListener)
            fabMakeNote.setOnThrottleClickListener(fabButtonClickListener)
        }
    }

    override fun onInitData() {
        viewModel.getPlacesByMonth(LocalDate.now(), true)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.placeNotesByDayFlow
                        .collect { placeItems ->
                            binding.emptyView.root.isVisible = placeItems.isEmpty()
                            listAdapter.submitList(placeItems)
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.currentYearMonthText }
                        .distinctUntilChanged()
                        .collect { yearMonthText ->
                            binding.tvCurrentYearMonth.text = yearMonthText
                        }
                }

                launch {
                    viewModel.calendarUiEvent.collect { event ->
                        when (event) {
                            is CalendarUiEvent.UpdateDayToDay -> {
                                with (binding.myWeekCalendar) {
                                    updateSelectedDate(event.newDate)
                                    notifyDateChanged(event.oldDate)
                                    notifyDateChanged(event.newDate)
                                    scrollToWeek(event.newDate)
                                }
                            }
                            is CalendarUiEvent.UpdateMonthToMonth -> {
                                with (binding.myWeekCalendar) {
                                    updateSelectedDate(event.newDate)
                                    updateWeekData(event.newDate)
                                }
                            }

                            is CalendarUiEvent.Initialize -> {
                                event.hasNoteDaysMap.keys.forEach {
                                    binding.myWeekCalendar.notifyDateChanged(it)
                                }
                            }
                        }
                    }
                }
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
            viewModel.getPlacesByMonth(
                localDate = convertedLocalDate,
                withSelectedDayUpdate = true
            )
        }
    }

    private val weekDayClickAction: (LocalDate) -> Unit = { localDate ->
        viewModel.changeSelectedDay(localDate)
    }

    private val weekDayHasEventChecker: (LocalDate) -> Boolean = {
        viewModel.checkNoteInDay(it)
    }

    private val placeItemClickAction: (PlaceNoteModel) -> Unit = { placeItem ->
        Timber.tag("abc123").e("clicked: $placeItem")
    }

    private val currentYearMonthClickListener = OnClickListener {
        CalendarMonthBSDialog.newInstance()
            .show(childFragmentManager, CalendarMonthBSDialog.TAG)
    }

    private val makeNoteButtonClickListener = OnClickListener {
        val selectedDay = CalendarUtils.localDateToMillis(viewModel.getSelectedDate())

        Intent(requireContext(), MakePlaceNoteActivity::class.java).apply {
            putExtra(AppConstants.INTENT_PLACE_VISIT_DATE, selectedDay)
            makeNoteResultLauncher.launch(this)
        }
    }

    private val fabButtonClickListener = OnClickListener {
        Intent(requireContext(), MakePlaceNoteActivity::class.java).apply {
            makeNoteResultLauncher.launch(this)
        }
    }

    companion object {
        const val TAG = "MyPlaceFragment"
        fun newInstance() = CalendarWithPlacesFragment()
    }
}
