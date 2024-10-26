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
import com.kjh.mynote.ui.features.place.detail.PlaceNoteDetailActivity
import com.kjh.mynote.ui.features.place.make.MakeOrModifyPlaceNoteActivity
import com.kjh.mynote.ui.features.viewer.ImagesViewerActivity
import com.kjh.mynote.utils.SpacingItemDecoration
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.parcelable
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.toMillis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate


@AndroidEntryPoint
class CalendarWithPlacesFragment
    : BaseFragment<FragmentCalendarWithPlacesBinding>({ FragmentCalendarWithPlacesBinding.inflate(it) }) {

    private val viewModel: CalendarWithPlacesViewModel by viewModels()

    private val listAdapter: CalendarPlaceListAdapter by lazy {
        CalendarPlaceListAdapter(placeItemClickAction, placeImageClickAction)
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
            fabMakeNote.setOnThrottleClickListener(fabButtonClickListener)
        }
    }

    override fun onInitData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.selectedDayPlaceNotesFlow
                        .collect { placeItems ->
                            binding.emptyView.root.isVisible = placeItems.isEmpty()
                            listAdapter.submitList(placeItems)
                        }
                }

                launch {
                    viewModel.thisMonthDatesWithNotesFlow.collect {
                        binding.myWeekCalendar.updateSelectDayWithEventDates(it)
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
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvNotes.removeItemDecoration(spacingItemDecoration)
    }

    private val makeNoteResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val insertedPlaceNoteItem = result.data?.parcelable<PlaceNoteModel>(
                AppConstants.INTENT_PLACE_NOTE_ITEM
            ) ?: return@registerForActivityResult

            viewModel.selectDay(insertedPlaceNoteItem.localDate)
        }
    }

    private val weekDayClickAction: (LocalDate) -> Unit = { localDate ->
        viewModel.selectDay(localDate)
    }

    private val placeItemClickAction: (PlaceNoteModel) -> Unit = { placeItem ->
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
            .show(childFragmentManager, CalendarMonthBSDialog.TAG)
    }

    private val makeNoteButtonClickListener = OnClickListener {
        val selectedDay = viewModel.getSelectedDate().toMillis()

        Intent(requireContext(), MakeOrModifyPlaceNoteActivity::class.java).apply {
            putExtra(AppConstants.INTENT_PLACE_VISIT_DATE, selectedDay)
            makeNoteResultLauncher.launch(this)
        }
    }

    private val fabButtonClickListener = OnClickListener {
        Intent(requireContext(), MakeOrModifyPlaceNoteActivity::class.java).apply {
            makeNoteResultLauncher.launch(this)
        }
    }

    companion object {
        const val TAG = "MyPlaceFragment"
        fun newInstance() = CalendarWithPlacesFragment()
    }
}
