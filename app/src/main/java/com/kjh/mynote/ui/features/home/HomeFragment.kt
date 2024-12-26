package com.kjh.mynote.ui.features.home

import android.content.Intent
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.domain.model.CategoryStats
import com.github.mikephil.charting.data.PieEntry
import com.kjh.mynote.databinding.FragmentHomeBinding
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.base.BaseFragment
import com.kjh.mynote.ui.features.category.statistics.CategoryStatisticsActivity
import com.kjh.mynote.ui.features.home.adapter.HomeUiListAdapter
import com.kjh.mynote.ui.features.place.detail.PlaceNoteDetailActivity
import com.kjh.mynote.ui.features.place.make.MakeOrModifyPlaceNoteActivity
import com.kjh.mynote.ui.features.purchase.statistics.PurchaseNoteStatisticsActivity
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 7..
 * Description:
 */

@AndroidEntryPoint
class HomeFragment: BaseFragment<FragmentHomeBinding>({ FragmentHomeBinding.inflate(it) }) {

    private val viewModel: HomeViewModel by viewModels()

    private val placeNoteWeekViewAdapter: HomeUiListAdapter by lazy {
        HomeUiListAdapter(
            weekDayClickAction = weekDayClickAction,
            placeNoteClickAction = placeNoteClickAction,
            makePlaceNoteClickAction = makePlaceNoteClickAction,
            seeAllPlaceNotesClickAction = seeAllPlaceNotesClickAction,
            sliceClickAction = sliceClickAction,
            categoryStatsItemClickAction = categoryStatsItemClickAction,
            seeAllPurchaseStatsClickAction = seeAllPurchaseStatsClickAction
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
        viewModel.getHomeData()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.uiState
                        .map { it.uiItems }
                        .distinctUntilChanged()
                        .collect {
                            placeNoteWeekViewAdapter.submitList(it)
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.errorMsg }
                        .distinctUntilChanged()
                        .collect {
                            it?.let {
                                showToast(it)
                                viewModel.shownError()
                            }
                        }
                }
            }
        }
    }

    private val weekDayClickAction: (LocalDate) -> Unit = { localDate ->
        viewModel.updatePlaceNoteWeekDay(localDate)
    }

    private val placeNoteClickAction: (PlaceNoteUiModel) -> Unit = { placeItem ->
        Intent(requireContext(), PlaceNoteDetailActivity::class.java).apply {
            putExtra(AppConstants.INTENT_NOTE_ID, placeItem.id)
            startActivity(this)
        }
    }

    private val makePlaceNoteClickAction: () -> Unit = {
        Intent(requireContext(), MakeOrModifyPlaceNoteActivity::class.java).apply {
            startActivity(this)
        }
    }

    private val sliceClickAction: (PieEntry?) -> Unit = {
        viewModel.updateHighlightPieEntry(it)
    }

    private val categoryStatsItemClickAction: (CategoryStats) -> Unit = { item ->
        Intent(requireContext(), CategoryStatisticsActivity::class.java).apply {
            putExtra(AppConstants.INTENT_CATEGORY_ITEM, CategoryUiModel(item.categoryId, item.categoryName))
            startActivity(this)
        }
    }

    private val seeAllPurchaseStatsClickAction: () -> Unit = {
        Intent(requireContext(), PurchaseNoteStatisticsActivity::class.java).apply {
            putExtra(AppConstants.INTENT_DATE, LocalDate.now())
            startActivity(this)
        }
    }

    private val seeAllPlaceNotesClickAction: () -> Unit = {

    }

    companion object {
        const val TAG = "HomeFragment"

        fun newInstance() = HomeFragment()
    }
}