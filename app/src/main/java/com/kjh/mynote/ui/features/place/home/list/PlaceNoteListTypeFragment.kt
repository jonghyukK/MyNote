package com.kjh.mynote.ui.features.place.home.list

import android.content.Intent
import android.view.View.OnClickListener
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.kjh.mynote.R
import com.kjh.mynote.databinding.FragmentPlaceNoteListTypeBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.base.BaseFragment
import com.kjh.mynote.ui.features.place.home.PlaceNoteHomeViewModel
import com.kjh.mynote.ui.features.place.home.PlaceNotesUiState
import com.kjh.mynote.ui.features.place.home.list.adapter.PlaceNoteListTypePagerAdapter
import com.kjh.mynote.ui.features.place.detail.PlaceNoteDetailActivity
import com.kjh.mynote.ui.features.place.make.MakeOrModifyPlaceNoteActivity
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.toMillis
import com.kjh.mynote.utils.extensions.toStringWithPattern
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 31..
 * Description:
 */

@AndroidEntryPoint
class PlaceNoteListTypeFragment: BaseFragment<FragmentPlaceNoteListTypeBinding>({ FragmentPlaceNoteListTypeBinding.inflate(it) }) {

    private val parentViewModel: PlaceNoteHomeViewModel by viewModels({ requireParentFragment() })
    private val viewModel: PlaceNoteListTypeViewModel by viewModels()

    private val pagerAdapter: PlaceNoteListTypePagerAdapter by lazy {
        PlaceNoteListTypePagerAdapter(placeItemClickAction, makeNoteClickAction)
    }

    private var currentPos = 0

    override fun onInitView() {
        with (binding) {
            ivArrowLeft.setOnThrottleClickListener(prevPageClickListener)
            ivArrowRight.setOnThrottleClickListener(nextPageClickListener)

            vpPager.apply {
                adapter = pagerAdapter
                offscreenPageLimit = 3
                registerOnPageChangeCallback(viewPagerPageChangeCallback)
            }
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

    override fun onDestroyView() {
        binding.vpPager.unregisterOnPageChangeCallback(viewPagerPageChangeCallback)
        super.onDestroyView()
    }

    private fun updateUi(uiState: PlaceNoteListTypeUiState) {
        with (binding) {
            tvCurrentYearMonth.text = uiState.currentMonth.toStringWithPattern("yyyy년 M월")
            ivArrowRight.isClickable = !uiState.isLastPage
            ivArrowRight.setColorFilter(getRightArrowBtnColorRes(uiState.isLastPage))
        }

        pagerAdapter.submitList(uiState.monthWithPlaceNoteUiItems) {
            if (currentPos != uiState.currentPagePos && uiState.currentPagePos > -1) {
                binding.vpPager.setCurrentItem(
                    uiState.currentPagePos,
                    false
                )
            }
        }
    }

    private fun getRightArrowBtnColorRes(isLastPage: Boolean) =
        if (isLastPage) {
            ContextCompat.getColor(requireContext(), R.color.black_400)
        } else {
            ContextCompat.getColor(requireContext(), R.color.black_800)
        }

    private val viewPagerPageChangeCallback = object: OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            currentPos = position

            if (!this@PlaceNoteListTypeFragment.isHidden) {
                val monthByPosition = viewModel.uiState.value.monthWithPlaceNoteUiItems[position].month
                parentViewModel.updateCurrentDate(monthByPosition)
            }
        }
    }

    private val placeItemClickAction: (PlaceNoteUiModel) -> Unit = { placeItem ->
        Intent(requireContext(), PlaceNoteDetailActivity::class.java).apply {
            putExtra(AppConstants.INTENT_NOTE_ID, placeItem.id)
            startActivity(this)
        }
    }

    private val makeNoteClickAction: () -> Unit = {
        val selectedMonth = viewModel.uiState.value.currentMonth

        Intent(requireContext(), MakeOrModifyPlaceNoteActivity::class.java).apply {
            putExtra(AppConstants.INTENT_PLACE_VISIT_DATE, selectedMonth.toMillis())
            startActivity(this)
        }
    }

    private val prevPageClickListener = OnClickListener {
        val prevMonth = viewModel.uiState.value.currentMonth.minusMonths(1)
        parentViewModel.updateCurrentDate(prevMonth)
    }

    private val nextPageClickListener = OnClickListener {
        val nextMonth = viewModel.uiState.value.currentMonth.plusMonths(1)
        parentViewModel.updateCurrentDate(nextMonth)
    }

    companion object {
        const val TAG = "PlaceNoteListTypeFragment"

        fun newInstance() = PlaceNoteListTypeFragment()
    }
}