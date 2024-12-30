package com.kjh.mynote.ui.features.place.home.weekview

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.view.View.OnClickListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.databinding.FragmentPlaceNoteWeekViewTypeBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.base.BaseFragment
import com.kjh.mynote.ui.features.place.home.PlaceNoteHomeViewModel
import com.kjh.mynote.ui.features.place.home.PlaceNotesUiState
import com.kjh.mynote.ui.features.place.home.weekview.adapter.PlaceNoteWeekViewTypeListAdapter
import com.kjh.mynote.ui.features.place.home.weekview.dialog.MonthlyEventCalendarBSDialog
import com.kjh.mynote.ui.features.place.detail.PlaceNoteDetailActivity
import com.kjh.mynote.ui.features.place.make.MakeOrModifyPlaceNoteActivity
import com.kjh.mynote.ui.features.place.search.PlaceNoteSearchActivity
import com.kjh.mynote.ui.features.viewer.ImagesViewerActivity
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.decorations.SpacingItemDecoration
import com.kjh.mynote.utils.extensions.parcelable
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.toMillis
import com.kjh.mynote.utils.extensions.toStringWithPattern
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate


@AndroidEntryPoint
class PlaceNoteWeekViewTypeFragment
    : BaseFragment<FragmentPlaceNoteWeekViewTypeBinding>({ FragmentPlaceNoteWeekViewTypeBinding.inflate(it) }),
MonthlyEventCalendarBSDialog.CalendarMonthDialogDayClickListener{

    private val parentViewModel: PlaceNoteHomeViewModel by viewModels({ requireParentFragment() })
    private val viewModel: PlaceNoteWeekViewTypeViewModel by viewModels()

    private val listAdapter: PlaceNoteWeekViewTypeListAdapter by lazy {
        PlaceNoteWeekViewTypeListAdapter(
            placeClickAction = placeItemClickAction,
            imageClickAction = placeImageClickAction,
            makeNoteClickAction = makeNoteClickAction
        )
    }

    override fun onInitView() {
        with (binding) {
            rvNotes.apply {
                setHasFixedSize(true)
                itemAnimator = null
                addItemDecoration(SpacingItemDecoration(top = 20))
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
                    parentViewModel.currentDate.collect { currentDate ->
                        viewModel.setCurrentDate(currentDate)
                    }
                }

                launch {
                    parentViewModel.placeNotesUiState
                        .filterIsInstance<PlaceNotesUiState.Success>()
                        .map { it.placeNotes }
                        .distinctUntilChanged()
                        .collect { placeNotes ->
                            viewModel.setPlaceNotes(placeNotes)
                        }
                }

                launch {
                    viewModel.uiState.collect(::updateUi)
                }
            }
        }
    }

    private fun updateUi(uiState: PlaceNoteWeekViewUiState) {
        with (binding) {
            tvCurrentYearMonth.text = uiState.currentDate.toStringWithPattern("yyyy년 M월")
            myWeekCalendar.updateSelectDayWithEventDates(
                uiState.currentDate to uiState.eventDays
            )
        }

        listAdapter.submitList(uiState.selectedDayNoteUiItems)
    }

    private val makeNoteResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val insertedPlaceNoteItem = result.data?.parcelable<PlaceNoteUiModel>(
                AppConstants.INTENT_PLACE_NOTE_ITEM
            ) ?: return@registerForActivityResult

            parentViewModel.updateCurrentDate(insertedPlaceNoteItem.localDate)
        }
    }

    private val weekDayClickAction: (LocalDate) -> Unit = { localDate ->
        parentViewModel.updateCurrentDate(localDate)
    }

    private val placeItemClickAction: (PlaceNoteUiModel) -> Unit = { placeItem ->
        Intent(requireContext(), PlaceNoteDetailActivity::class.java).apply {
            putExtra(AppConstants.INTENT_NOTE_ID, placeItem.id)
            startActivity(this)
        }
    }

    private val makeNoteClickAction: () -> Unit = {
        val selectedDate = viewModel.uiState.value.currentDate

        Intent(requireContext(), MakeOrModifyPlaceNoteActivity::class.java).apply {
            putExtra(AppConstants.INTENT_PLACE_VISIT_DATE, selectedDate.toMillis())
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
        MonthlyEventCalendarBSDialog.newInstance(
            selectedDate = viewModel.uiState.value.currentDate,
            eventDays = viewModel.uiState.value.eventDays
        ).show(childFragmentManager, MonthlyEventCalendarBSDialog.TAG)
    }

    private val searchClickListener = OnClickListener {
        Intent(requireContext(), PlaceNoteSearchActivity::class.java).apply {
            startActivity(this)
        }
    }

    override fun onClickDay(date: LocalDate) {
        parentViewModel.updateCurrentDate(date)
    }

    companion object {
        const val TAG = "PlaceNoteWeekViewTypeFragment"
        fun newInstance() = PlaceNoteWeekViewTypeFragment()
    }
}
