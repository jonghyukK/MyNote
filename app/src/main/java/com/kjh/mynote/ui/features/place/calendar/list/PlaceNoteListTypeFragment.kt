package com.kjh.mynote.ui.features.place.calendar.list

import android.content.Intent
import android.view.View.OnClickListener
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.kjh.mynote.R
import com.kjh.mynote.databinding.FragmentPlaceNoteListTypeBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.base.BaseFragment
import com.kjh.mynote.ui.features.place.calendar.PlaceNoteCalendarHomeViewModel
import com.kjh.mynote.ui.features.place.calendar.list.adapter.PlaceNoteListTypeOuterAdapter
import com.kjh.mynote.ui.features.place.detail.PlaceNoteDetailActivity
import com.kjh.mynote.ui.features.place.make.MakeOrModifyPlaceNoteActivity
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.toMillis
import com.kjh.mynote.utils.extensions.toStringWithPattern
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
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

    private val viewModel: PlaceNoteCalendarHomeViewModel by activityViewModels()

    private val listAdapter: PlaceNoteListTypeOuterAdapter by lazy {
        PlaceNoteListTypeOuterAdapter(placeItemClickAction, makeNoteClickAction)
    }

    private var currentPos = 0

    override fun onInitView() {
        with (binding) {
            ivArrowLeft.setOnThrottleClickListener(prevPageClickListener)
            ivArrowRight.setOnThrottleClickListener(nextPageClickListener)

            vpPager.apply {
                adapter = listAdapter
                offscreenPageLimit = 3
                registerOnPageChangeCallback(object: OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        currentPos = position
                        viewModel.setSelectedMonth(position)
                    }
                })
            }
        }
    }

    override fun onInitData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.listUiState
                        .map { it.currentMonth }
                        .distinctUntilChanged()
                        .collect {
                            binding.tvCurrentYearMonth.text = it.toStringWithPattern("yyyy년 M월")
                        }
                }

                launch {
                    viewModel.listUiState
                        .map { it.currentPagePos to it.monthsWithInMonthPlaceNoteItems }
                        .distinctUntilChanged()
                        .collectLatest { (pagerPos, list) ->
                            listAdapter.submitList(list) {
                                if (currentPos != pagerPos && pagerPos > -1) {
                                    binding.vpPager.setCurrentItem(pagerPos, false)
                                }
                            }
                        }
                }

                launch {
                    viewModel.listUiState
                        .map { it.isLastPage }
                        .distinctUntilChanged()
                        .collect { isLastPage ->
                            with (binding.ivArrowRight) {
                                isClickable = !isLastPage
                                setColorFilter(getNextMonthButtonColorRes(isLastPage))
                            }
                        }
                }
            }
        }
    }

    override fun onDestroyView() {
        binding.vpPager.adapter = null
        super.onDestroyView()
    }

    private fun getNextMonthButtonColorRes(isLastPage: Boolean) =
        if (isLastPage) {
            ContextCompat.getColor(requireContext(), R.color.black_400)
        } else {
            ContextCompat.getColor(requireContext(), R.color.black_800)
        }

    private val placeItemClickAction: (PlaceNoteUiModel) -> Unit = { placeItem ->
        Intent(requireContext(), PlaceNoteDetailActivity::class.java).apply {
            putExtra(AppConstants.INTENT_NOTE_ID, placeItem.id)
            startActivity(this)
        }
    }

    private val makeNoteClickAction: () -> Unit = {
        val selectedDay = viewModel.listUiState.value.currentMonth.toMillis()

        Intent(requireContext(), MakeOrModifyPlaceNoteActivity::class.java).apply {
            putExtra(AppConstants.INTENT_PLACE_VISIT_DATE, selectedDay)
            startActivity(this)
        }
    }

    private val prevPageClickListener = OnClickListener {
        viewModel.moveToPrevMonth()
    }

    private val nextPageClickListener = OnClickListener {
        viewModel.moveToNextMonth()
    }

    companion object {
        const val TAG = "PlaceNoteListTypeFragment"

        fun newInstance() = PlaceNoteListTypeFragment()
    }
}