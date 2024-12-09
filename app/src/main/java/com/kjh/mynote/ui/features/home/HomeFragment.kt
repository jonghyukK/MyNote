package com.kjh.mynote.ui.features.home

import android.content.Intent
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.databinding.FragmentHomeBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.base.BaseFragment
import com.kjh.mynote.ui.features.home.weekview.HomePlaceNoteWeekViewAdapter
import com.kjh.mynote.ui.features.place.detail.PlaceNoteDetailActivity
import com.kjh.mynote.utils.constants.AppConstants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 7..
 * Description:
 */

@AndroidEntryPoint
class HomeFragment: BaseFragment<FragmentHomeBinding>({ FragmentHomeBinding.inflate(it) }) {

    private val viewModel: HomeViewModel by viewModels()

    private val placeNoteWeekViewAdapter: HomePlaceNoteWeekViewAdapter by lazy {
        HomePlaceNoteWeekViewAdapter(
            weekDayClickAction = weekDayClickAction,
            placeNoteClickAction = placeNoteClickAction
        )
    }

    override fun onInitView() {
        with (binding) {
            rvHomeUis.apply {
                itemAnimator = null
                adapter = placeNoteWeekViewAdapter
            }
        }
    }

    override fun onInitData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.uiState.collect {
                        it.map { data ->
                            if (data is HomeItem.HomePlaceNoteWeekView) {
                                Timber.tag("abc123").e("""
                                    seletedDate : ${data.selectedDate}
                                    items       : ${data.displayedPlaceNotes.size}
                                """.trimIndent())
                            }
                        }
                        placeNoteWeekViewAdapter.submitList(it)
                    }
                }
            }
        }
    }

    private val weekDayClickAction: (LocalDate) -> Unit = { localDate ->
        Timber.tag("abc123").e("WeekDay Click: $localDate")
        viewModel.changePlaceNoteWeekDay(localDate)
    }

    private val placeNoteClickAction: (PlaceNoteUiModel) -> Unit = { placeItem ->
        Intent(requireContext(), PlaceNoteDetailActivity::class.java).apply {
            putExtra(AppConstants.INTENT_NOTE_ID, placeItem.id)
            startActivity(this)
        }
    }

    companion object {
        const val TAG = "HomeFragment"

        fun newInstance() = HomeFragment()
    }
}