package com.kjh.mynote.ui.features.purchase.statistics

import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.mikephil.charting.data.PieEntry
import com.kjh.mynote.databinding.ActivityPurchaseNoteStatisticsBinding
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.common.dialog.yearmonths.SelectableYearMonthListBSDialog
import com.kjh.mynote.ui.features.purchase.statistics.adapter.PurchaseNoteStatisticsUiListAdapter
import com.kjh.mynote.ui.features.purchase.statistics.decoration.PurchaseNoteStatisticsItemDecoration
import com.kjh.mynote.utils.extensions.showToast
import com.kjh.mynote.utils.extensions.toStringWithPattern
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
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
            sliceClickAction = sliceClickAction
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
        viewModel.fetchPurchaseNoteStatistics()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
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

                launch {
                    viewModel.uiState
                        .map { it.uiItems }
                        .distinctUntilChanged()
                        .collect {
                            listAdapter.submitList(it)
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
            selectedDate = viewModel.uiState.value.currentDate,
            yearsRange = 1
        ).show(supportFragmentManager, SelectableYearMonthListBSDialog.TAG)
    }

    override fun onClickYearMonth(date: LocalDate) {
        viewModel.setDate(date)
    }
}