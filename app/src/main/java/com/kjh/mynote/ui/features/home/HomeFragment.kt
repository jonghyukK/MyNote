package com.kjh.mynote.ui.features.home

import android.content.Intent
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.domain.model.CategoryWithStats
import com.github.mikephil.charting.data.PieEntry
import com.kjh.mynote.databinding.FragmentHomeBinding
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.base.BaseFragment
import com.kjh.mynote.ui.features.category.purchasestats.CategoryPurchaseNoteStatsActivity
import com.kjh.mynote.ui.features.place.detail.PlaceNoteDetailActivity
import com.kjh.mynote.ui.features.place.make.MakeOrModifyPlaceNoteActivity
import com.kjh.mynote.utils.constants.AppConstants
import dagger.hilt.android.AndroidEntryPoint
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
            sliceClickAction = sliceClickAction,
            categoryStatsItemClickAction = categoryStatsItemClickAction
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
                        placeNoteWeekViewAdapter.submitList(it)
                    }
                }
            }
        }
    }

    private val weekDayClickAction: (LocalDate) -> Unit = { localDate ->
        viewModel.changePlaceNoteWeekDay(localDate)
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
        viewModel.setHighlightPieEntry(it)
    }

    private val categoryStatsItemClickAction: (CategoryWithStats) -> Unit = { item ->
        Intent(requireContext(), CategoryPurchaseNoteStatsActivity::class.java).apply {
            putExtra(AppConstants.INTENT_CATEGORY_ITEM, CategoryUiModel(item.categoryId, item.categoryName))
            startActivity(this)
        }
    }

    companion object {
        const val TAG = "HomeFragment"

        fun newInstance() = HomeFragment()
    }
}