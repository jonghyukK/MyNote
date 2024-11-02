package com.kjh.mynote.ui.features.place.calendar.list

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.kjh.mynote.databinding.FragmentPlaceNoteListTypeBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.base.BaseFragment
import com.kjh.mynote.ui.features.place.calendar.PlaceNoteCalendarHomeViewModel
import com.kjh.mynote.ui.features.place.calendar.list.adapter.PlaceNoteListTypeOuterAdapter
import com.kjh.mynote.ui.features.place.detail.PlaceNoteDetailActivity
import com.kjh.mynote.ui.features.place.make.MakeOrModifyPlaceNoteActivity
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.parcelable
import com.kjh.mynote.utils.extensions.toMillis
import com.kjh.mynote.utils.extensions.toStringWithPattern
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 31..
 * Description:
 */

@AndroidEntryPoint
class PlaceNoteListTypeFragment: BaseFragment<FragmentPlaceNoteListTypeBinding>({ FragmentPlaceNoteListTypeBinding.inflate(it) }) {

    private val parentViewModel: PlaceNoteCalendarHomeViewModel by activityViewModels()
    private val viewModel: PlaceNoteListTypeViewModel by viewModels()

    private val listAdapter: PlaceNoteListTypeOuterAdapter by lazy {
        PlaceNoteListTypeOuterAdapter(placeItemClickAction, makeNoteClickAction)
    }

    private var currentPos = 0

    override fun onInitView() {
        binding.vpPager.apply {
            adapter = listAdapter
            registerOnPageChangeCallback(object: OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    currentPos = position
                    parentViewModel.selectDay(viewModel.findDateUsingPagerPosition(position))
                }
            })
        }
    }

    override fun onInitData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState
                        .map { it.currentMonth }
                        .distinctUntilChanged()
                        .collect {
                            binding.tvCurrentYearMonth.text = it.toStringWithPattern("yyyy년 MM월")
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.currentPagePos to it.monthsWithInMonthPlaceNoteItems }
                        .distinctUntilChanged()
                        .collect { (pagerPos, list) ->
                            listAdapter.submitList(list) {
                                if (currentPos != pagerPos) {
                                    binding.vpPager.setCurrentItem(pagerPos, false)
                                }
                            }
                        }
                }

                launch {
                    parentViewModel.selectedDay.collect { date ->
                        viewModel.setTargetDate(date.withDayOfMonth(1))
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        binding.vpPager.adapter = null
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

    private val placeItemClickAction: (PlaceNoteUiModel) -> Unit = { placeItem ->
        Intent(requireContext(), PlaceNoteDetailActivity::class.java).apply {
            putExtra(AppConstants.INTENT_NOTE_ID, placeItem.id)
            startActivity(this)
        }
    }

    private val makeNoteClickAction: () -> Unit = {
        val selectedDay = viewModel.uiState.value.currentMonth.toMillis()

        Intent(requireContext(), MakeOrModifyPlaceNoteActivity::class.java).apply {
            putExtra(AppConstants.INTENT_PLACE_VISIT_DATE, selectedDay)
            makeNoteResultLauncher.launch(this)
        }
    }

    companion object {
        const val TAG = "PlaceNoteListTypeFragment"

        fun newInstance() = PlaceNoteListTypeFragment()
    }
}