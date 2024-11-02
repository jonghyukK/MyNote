package com.kjh.mynote.ui.features.place.calendar.weekview

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.view.View.OnClickListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.databinding.FragmentCalendarWithPlacesBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.base.BaseFragment
import com.kjh.mynote.ui.features.place.calendar.dialog.CalendarMonthBSDialog
import com.kjh.mynote.ui.features.place.calendar.PlaceNoteCalendarHomeViewModel
import com.kjh.mynote.ui.features.place.calendar.weekview.adapter.PlaceNoteWeekViewTypeListAdapter
import com.kjh.mynote.ui.features.place.detail.PlaceNoteDetailActivity
import com.kjh.mynote.ui.features.place.make.MakeOrModifyPlaceNoteActivity
import com.kjh.mynote.ui.features.viewer.ImagesViewerActivity
import com.kjh.mynote.utils.SpacingItemDecoration
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.parcelable
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.toMillis
import com.kjh.mynote.utils.extensions.toStringWithPattern
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate


@AndroidEntryPoint
class PlaceNoteWeekViewTypeFragment
    : BaseFragment<FragmentCalendarWithPlacesBinding>({ FragmentCalendarWithPlacesBinding.inflate(it) }) {

    private val parentViewModel: PlaceNoteCalendarHomeViewModel by activityViewModels()
    private val viewModel: PlaceNoteWeekViewTypeViewModel by viewModels()

    private val listAdapter: PlaceNoteWeekViewTypeListAdapter by lazy {
        PlaceNoteWeekViewTypeListAdapter(placeItemClickAction, placeImageClickAction)
    }

    private val spacingItemDecoration = SpacingItemDecoration(top = 20)

    override fun onInitView() {
        with (binding) {
            rvNotes.apply {
                setHasFixedSize(true)
                itemAnimator = null
                addItemDecoration(spacingItemDecoration)
                adapter = listAdapter
            }

            myWeekCalendar.setDayClickAction(weekDayClickAction)

            clYearMonth.setOnThrottleClickListener(currentYearMonthClickListener)
            emptyView.btnMakePlace.setOnThrottleClickListener(makeNoteButtonClickListener)
        }
    }

    override fun onInitData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    parentViewModel.selectedDay.collect {
                        Timber.tag("abc123 PlaceNoteWeekViewTypeFragment").e("parent selectedDay : $it")
                        viewModel.setSelectedDay(it)
                    }
                }

                launch {
                    viewModel.uiState
                        .map { it.selectedDayPlaceNoteItems }
                        .distinctUntilChanged()
                        .collect { placeNoteItems ->
                            binding.emptyView.root.isVisible = placeNoteItems.isEmpty()
                            listAdapter.submitList(placeNoteItems)
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.selectedDay to it.selectedMonthEventDays }
                        .distinctUntilChanged()
                        .collect {
                            binding.tvCurrentYearMonth.text = it.first.toStringWithPattern("yyyy년 MM월")
                            binding.myWeekCalendar.updateSelectDayWithEventDates(it)
                        }
                }
            }
        }
    }

    override fun onDestroyView() {
        binding.rvNotes.removeItemDecoration(spacingItemDecoration)
        binding.rvNotes.adapter = null
        super.onDestroyView()
    }

    private val makeNoteResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val insertedPlaceNoteItem = result.data?.parcelable<PlaceNoteUiModel>(
                AppConstants.INTENT_PLACE_NOTE_ITEM
            ) ?: return@registerForActivityResult

            parentViewModel.selectDay(insertedPlaceNoteItem.localDate)
        }
    }

    private val weekDayClickAction: (LocalDate) -> Unit = { localDate ->
        parentViewModel.selectDay(localDate)
    }

    private val placeItemClickAction: (PlaceNoteUiModel) -> Unit = { placeItem ->
        Intent(requireContext(), PlaceNoteDetailActivity::class.java).apply {
            putExtra(AppConstants.INTENT_NOTE_ID, placeItem.id)
            startActivity(this)
        }
    }

    private val placeImageClickAction: (List<String>, String) -> Unit = { images, clickedImage ->
        Intent(requireContext(), ImagesViewerActivity::class.java).apply {
            putExtra(AppConstants.INTENT_IMAGE_LIST, ArrayList(images))
            putExtra(AppConstants.INTENT_URL, clickedImage)
            startActivity(this)
        }
    }

    private val currentYearMonthClickListener = OnClickListener {
        CalendarMonthBSDialog.newInstance()
            .show(parentFragmentManager, CalendarMonthBSDialog.TAG)
    }

    private val makeNoteButtonClickListener = OnClickListener {
        val selectedDay = parentViewModel.getSelectedDate().toMillis()

        Intent(requireContext(), MakeOrModifyPlaceNoteActivity::class.java).apply {
            putExtra(AppConstants.INTENT_PLACE_VISIT_DATE, selectedDay)
            makeNoteResultLauncher.launch(this)
        }
    }

    companion object {
        const val TAG = "MyPlaceFragment"
        fun newInstance() = PlaceNoteWeekViewTypeFragment()
    }
}
