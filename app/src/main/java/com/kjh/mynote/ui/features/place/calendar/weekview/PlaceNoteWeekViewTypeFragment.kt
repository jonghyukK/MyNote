package com.kjh.mynote.ui.features.place.calendar.weekview

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.view.View.OnClickListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.databinding.FragmentPlaceNoteWeekViewTypeBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.base.BaseFragment
import com.kjh.mynote.ui.features.place.calendar.PlaceNoteCalendarHomeViewModel
import com.kjh.mynote.ui.features.place.calendar.weekview.dialog.CalendarMonthBSDialog
import com.kjh.mynote.ui.features.place.calendar.weekview.adapter.PlaceNoteWeekViewTypeListAdapter
import com.kjh.mynote.ui.features.place.detail.PlaceNoteDetailActivity
import com.kjh.mynote.ui.features.place.make.MakeOrModifyPlaceNoteActivity
import com.kjh.mynote.ui.features.search.place.SearchPlaceNoteFragment
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
import java.time.LocalDate


@AndroidEntryPoint
class PlaceNoteWeekViewTypeFragment
    : BaseFragment<FragmentPlaceNoteWeekViewTypeBinding>({ FragmentPlaceNoteWeekViewTypeBinding.inflate(it) }) {

    private val viewModel: PlaceNoteCalendarHomeViewModel by activityViewModels()

    private val listAdapter: PlaceNoteWeekViewTypeListAdapter by lazy {
        PlaceNoteWeekViewTypeListAdapter(
            placeClickAction = placeItemClickAction,
            imageClickAction = placeImageClickAction,
            makeNoteClickAction = makeNoteClickAction
        )
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

            ivSearch.setOnThrottleClickListener(searchClickListener)
            clYearMonth.setOnThrottleClickListener(currentYearMonthClickListener)
        }
    }

    override fun onInitData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.weekViewUiState
                        .map { it.selectedDayNoteItems }
                        .distinctUntilChanged()
                        .collect { placeNoteItems ->
                            listAdapter.submitList(placeNoteItems)
                        }
                }

                launch {
                    viewModel.weekViewUiState
                        .map { it.currentDay to it.selectedMonthEventDays }
                        .distinctUntilChanged()
                        .collect {
                            binding.tvCurrentYearMonth.text = it.first.toStringWithPattern("yyyy년 M월")
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

            viewModel.setSelectedDate(insertedPlaceNoteItem.localDate)
        }
    }

    private val weekDayClickAction: (LocalDate) -> Unit = { localDate ->
        viewModel.setSelectedDate(localDate)
    }

    private val placeItemClickAction: (PlaceNoteUiModel) -> Unit = { placeItem ->
        Intent(requireContext(), PlaceNoteDetailActivity::class.java).apply {
            putExtra(AppConstants.INTENT_NOTE_ID, placeItem.id)
            startActivity(this)
        }
    }

    private val makeNoteClickAction: () -> Unit = {
        val selectedDay = viewModel.getSelectedDate().toMillis()

        Intent(requireContext(), MakeOrModifyPlaceNoteActivity::class.java).apply {
            putExtra(AppConstants.INTENT_PLACE_VISIT_DATE, selectedDay)
            makeNoteResultLauncher.launch(this)
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

    private val searchClickListener = OnClickListener {
        SearchPlaceNoteFragment.newInstance()
            .show(childFragmentManager, SearchPlaceNoteFragment.TAG)
    }

    companion object {
        const val TAG = "PlaceNoteWeekViewTypeFragment"
        fun newInstance() = PlaceNoteWeekViewTypeFragment()
    }
}
