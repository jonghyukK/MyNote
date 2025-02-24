package com.kjh.mynote.ui.features.statistics.purchasenote

import android.content.Intent
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.github.mikephil.charting.data.PieEntry
import com.kjh.mynote.R
import com.kjh.mynote.databinding.ActivityPurchaseNoteStatisticsBinding
import com.kjh.mynote.model.CategoryStatsUiModel
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.PaymentMethodStatsUiModel
import com.kjh.mynote.model.WeeklyPurchaseNoteStatsUiModel
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.common.dialog.yearmonths.SelectableYearMonthListBSDialog
import com.kjh.mynote.ui.features.statistics.category.CategoryStatisticsActivity
import com.kjh.mynote.ui.features.statistics.purchasenote.adapter.category.CategoryStatsSectionAdapter
import com.kjh.mynote.ui.features.statistics.purchasenote.adapter.paymentmethod.PaymentMethodStatsSectionAdapter
import com.kjh.mynote.ui.features.statistics.purchasenote.adapter.total.TotalStatsSectionAdapter
import com.kjh.mynote.ui.features.statistics.purchasenote.adapter.weekly.WeeklyStatsSectionAdapter
import com.kjh.mynote.ui.features.statistics.weekly.WeeklyStatisticsDetailActivity
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.decorations.SpacingItemDecoration
import com.kjh.mynote.utils.extensions.makeGone
import com.kjh.mynote.utils.extensions.makeVisible
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.showToast
import com.kjh.mynote.utils.extensions.toStringWithPattern
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
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

    private val totalStatsAdapter: TotalStatsSectionAdapter by lazy {
        TotalStatsSectionAdapter(
            dateClickAction = dateClickAction,
            prevMonthClickAction = prevMonthClickAction,
            nextMonthClickAction = nextMonthClickAction
        )
    }

    private val weeklyStatsAdapter: WeeklyStatsSectionAdapter by lazy {
        WeeklyStatsSectionAdapter(weekStatsClickAction)
    }

    private val categoryStatsSectionAdapter: CategoryStatsSectionAdapter by lazy {
        CategoryStatsSectionAdapter(
            categoryPieSliceClickAction = categoryPieSliceClickAction,
            categoryStatsClickAction = categoryStatsClickAction,
            categoryStatsMoreClickAction = categoryStatsMoreClickAction
        )
    }

    private val paymentMethodStatsSectionAdapter: PaymentMethodStatsSectionAdapter by lazy {
        PaymentMethodStatsSectionAdapter(
            paymentMethodPieSliceClickAction = paymentMethodPieSliceClickAction,
            paymentMethodStatsClickAction = paymentMethodStatsClickAction,
            paymentMethodStatsMoreClickAction = paymentMethodStatsMoreClickAction
        )
    }

    override fun onInitView() {
        with (binding) {
            tvTitle.text = getString(R.string.purchase_note_statistics)

            rvUis.apply {
                itemAnimator = null
                addItemDecoration(SpacingItemDecoration(top = 10))
                addOnScrollListener(object: OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        val offset = recyclerView.computeVerticalScrollOffset()
                        val targetPos = resources.getDimensionPixelSize(R.dimen.dimen_52)

                        switchToolbarDateTitle(offset > targetPos)
                    }
                })
                adapter = ConcatAdapter(totalStatsAdapter, weeklyStatsAdapter, categoryStatsSectionAdapter, paymentMethodStatsSectionAdapter)
            }

            clSelectableDate.setOnThrottleClickListener(dateClickListener)
            ivBack.setOnThrottleClickListener(backBtnClickListener)
        }
    }

    override fun onInitUiData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.errorMessage.collectLatest(::showToast)
                }

                launch {
                    viewModel.queryDate.collect { date ->
                        binding.tvDate.text = date.toStringWithPattern(AppConstants.DATE_FORMAT_YYYY_M)
                    }
                }

                launch {
                    viewModel.uiState.collect { uiState ->
                        when (uiState) {
                            is PurchaseNoteStatisticsUiState.Loading -> {
                                binding.layoutLoading.root.makeVisible()
                            }
                            is PurchaseNoteStatisticsUiState.Error -> {
                                binding.layoutLoading.root.makeGone()
                            }
                            is PurchaseNoteStatisticsUiState.Success -> {
                                binding.layoutLoading.root.makeGone()

                                totalStatsAdapter.submitList(listOf(uiState.totalStatsItem))
                                weeklyStatsAdapter.submitList(listOf(uiState.weeklyStatsItem))
                                categoryStatsSectionAdapter.submitList(listOf(uiState.categoryStatsItem))
                                paymentMethodStatsSectionAdapter.submitList(listOf(uiState.paymentStatsItem))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun switchToolbarDateTitle(isScrolled: Boolean) = with (binding) {
        tvTitle.isVisible = !isScrolled
        clSelectableDate.isVisible = isScrolled
    }

    private val dateClickAction: () -> Unit = {
        SelectableYearMonthListBSDialog.newInstance(
            selectedDate = viewModel.queryDate.value,
            yearsRange = 1
        ).show(supportFragmentManager, SelectableYearMonthListBSDialog.TAG)
    }

    private val prevMonthClickAction: (LocalDate) -> Unit = { prevMonth ->
        viewModel.setDate(prevMonth)
    }

    private val nextMonthClickAction: (LocalDate) -> Unit = { nextMonth ->
        viewModel.setDate(nextMonth)
    }

    private val categoryPieSliceClickAction: (PieEntry?) -> Unit = { pieEntry ->
        viewModel.updateHighlightForCategoryPie(pieEntry)
    }

    private val paymentMethodPieSliceClickAction: (PieEntry?) -> Unit = { pieEntry ->
        viewModel.updateHighlightForPaymentMethodPie(pieEntry)
    }

    private val categoryStatsClickAction: (CategoryStatsUiModel) -> Unit = { item ->
        Intent(this, CategoryStatisticsActivity::class.java).apply {
            putExtra(AppConstants.INTENT_CATEGORY_ITEM, CategoryUiModel(item.categoryId, item.categoryName))
            putExtra(AppConstants.INTENT_DATE, viewModel.queryDate.value)
            startActivity(this)
        }
    }

    private val paymentMethodStatsClickAction: (PaymentMethodStatsUiModel) -> Unit = { item ->

    }

    private val weekStatsClickAction: (WeeklyPurchaseNoteStatsUiModel) -> Unit = {
        Intent(this, WeeklyStatisticsDetailActivity::class.java).apply {
            putExtra(AppConstants.INTENT_START_DATE, it.weekStartDate)
            putExtra(AppConstants.INTENT_END_DATE, it.weekEndDate)
            putExtra(AppConstants.INTENT_WEEK, it.week)
            startActivity(this)
        }
    }

    private val categoryStatsMoreClickAction: () -> Unit = {
        viewModel.toggleExpandForCategoryStats()
    }

    private val paymentMethodStatsMoreClickAction: () -> Unit = {
        viewModel.toggleExpandForPaymentMethodStats()
    }

    private val backBtnClickListener = View.OnClickListener {
        finish()
    }

    private val dateClickListener = View.OnClickListener {
        dateClickAction()
    }

    override fun onClickYearMonth(date: LocalDate) {
        viewModel.setDate(date)
    }
}