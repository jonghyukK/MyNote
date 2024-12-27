package com.kjh.mynote.ui.features.purchase.statistics

import android.content.Intent
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.domain.model.CategoryStats
import com.github.mikephil.charting.data.PieEntry
import com.kjh.mynote.databinding.ActivityPurchaseNoteStatisticsBinding
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.common.dialog.yearmonths.SelectableYearMonthListBSDialog
import com.kjh.mynote.ui.features.category.statistics.CategoryStatisticsActivity
import com.kjh.mynote.ui.features.purchase.statistics.adapter.PurchaseNoteStatisticsUiListAdapter
import com.kjh.mynote.ui.features.purchase.statistics.decoration.PurchaseNoteStatisticsItemDecoration
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 20..
 * Description:
 */

@AndroidEntryPoint
class PurchaseNoteStatisticsActivity:
    BaseActivity<ActivityPurchaseNoteStatisticsBinding>({ ActivityPurchaseNoteStatisticsBinding.inflate(it) }),
        SelectableYearMonthListBSDialog.YearMonthClickListener
{

    private val viewModel: PurchaseNoteStatisticsViewModel by viewModels()

    private val listAdapter: PurchaseNoteStatisticsUiListAdapter by lazy {
        PurchaseNoteStatisticsUiListAdapter(
            dateClickAction = dateClickAction,
            sliceClickAction = sliceClickAction,
            categoryStatsClickAction = categoryStatsClickAction
        )
    }

    override fun onInitView() {
        with (binding) {
            rvUis.apply {
                itemAnimator = null
                addItemDecoration(PurchaseNoteStatisticsItemDecoration())
                adapter = listAdapter
            }
        }
    }

    override fun onInitUiData() {
        viewModel.getPurchaseNoteStatistics()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    when (uiState) {
                        is PurchaseNoteStatisticsUiState.Loading -> {}
                        is PurchaseNoteStatisticsUiState.Error -> {
                            uiState.error.message?.let {
                                showToast(it)
                            }
                        }

                        is PurchaseNoteStatisticsUiState.Success -> {
                            listAdapter.submitList(uiState.uiItems)
                        }
                    }
                }
            }
        }
    }

    private val sliceClickAction: (PieEntry?) -> Unit = {
        viewModel.updateHighlightEntry(it)
    }

    private val dateClickAction: () -> Unit = {
        SelectableYearMonthListBSDialog.newInstance(
            selectedDate = viewModel.currentDate.value,
            yearsRange = 1
        ).show(supportFragmentManager, SelectableYearMonthListBSDialog.TAG)
    }

    private val categoryStatsClickAction: (CategoryStats) -> Unit = { item ->
        Intent(this, CategoryStatisticsActivity::class.java).apply {
            putExtra(AppConstants.INTENT_CATEGORY_ITEM, CategoryUiModel(item.categoryId, item.categoryName))
            putExtra(AppConstants.INTENT_DATE, viewModel.currentDate.value)
            startActivity(this)
        }
    }

    override fun onClickYearMonth(date: LocalDate) {
        viewModel.setDate(date)
    }
}