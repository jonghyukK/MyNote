package com.kjh.mynote.ui.features.statistics.category

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.databinding.ActivityCategoryPurchaseNoteStatsBinding
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.common.dialog.yearmonths.SelectableYearMonthListBSDialog
import com.kjh.mynote.ui.features.category.list.CategoryListBSDialog
import com.kjh.mynote.ui.features.statistics.category.adapter.CategoryPurchaseNoteStatsUiListAdapter
import com.kjh.mynote.ui.features.purchase.detail.PurchaseNoteDetailActivity
import com.kjh.mynote.ui.features.purchase.make.MakePurchaseNoteActivity
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.ui.features.statistics.category.adapter.decoration.CategoryStatisticsListItemDecoration
import com.kjh.mynote.utils.extensions.makeGone
import com.kjh.mynote.utils.extensions.makeVisible
import com.kjh.mynote.utils.extensions.parcelable
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.showToast
import com.kjh.mynote.utils.extensions.toStringWithPattern
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 14..
 * Description:
 */

@AndroidEntryPoint
class CategoryStatisticsActivity
    : BaseActivity<ActivityCategoryPurchaseNoteStatsBinding>({ ActivityCategoryPurchaseNoteStatsBinding.inflate(it) }),
    SelectableYearMonthListBSDialog.YearMonthClickListener {

    private val viewModel: CategoryStatisticsViewModel by viewModels()

    private val listAdapter: CategoryPurchaseNoteStatsUiListAdapter by lazy {
        CategoryPurchaseNoteStatsUiListAdapter(
            categoryClickAction = categoryClickAction,
            purchaseNoteItemClickAction = purchaseNoteItemClickAction,
            makePurchaseNoteClickAction = makePurchaseNoteClickAction
        )
    }

    override fun onInitView() {
        with(binding) {
            rvPurchaseNotes.apply {
                itemAnimator = null
                addItemDecoration(CategoryStatisticsListItemDecoration())
                adapter = listAdapter
            }

            llDateContainer.setOnThrottleClickListener(dateClickListener)
            ivBack.setOnThrottleClickListener(backButtonClickListener)
        }

        supportFragmentManager.setFragmentResultListener(
            CategoryListBSDialog.REQUEST_KEY, this, handleCategorySelectionResult
        )
    }

    override fun onInitUiData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.currentDate.collect { date ->
                            binding.tvDate.text = date.toStringWithPattern("yyyy년 M월")
                        }
                }

                launch {
                    viewModel.uiState.collect { uiState ->
                        when (uiState) {
                            is CategoryStatisticsUiState.Loading -> {
                                binding.layoutLoading.root.makeVisible()
                            }
                            is CategoryStatisticsUiState.Error -> {
                                binding.layoutLoading.root.makeGone()
                                uiState.error.message?.let {
                                    showToast(it)
                                }
                            }
                            is CategoryStatisticsUiState.CategoryStatistics -> {
                                binding.layoutLoading.root.makeGone()
                                listAdapter.submitList(uiState.items)
                            }
                        }
                    }
                }
            }
        }
    }

    private val categoryClickAction: () -> Unit =  {
        CategoryListBSDialog.newInstance(
            date = viewModel.currentDate.value,
            isEditable = false,
            showCount = true,
            selectedCategoryItem = viewModel.currentCategory.value
        ).show(supportFragmentManager, CategoryListBSDialog.TAG)
    }

    private val purchaseNoteItemClickAction: (PurchaseNoteUiModel) -> Unit = { noteItem ->
        Intent(this, PurchaseNoteDetailActivity::class.java).apply {
            putExtra(AppConstants.INTENT_PURCHASE_NOTE_ID, noteItem.id)
            startActivity(this)
        }
    }

    private val makePurchaseNoteClickAction: () -> Unit = {
        Intent(this, MakePurchaseNoteActivity::class.java).apply {
            startActivity(this)
        }
    }

    private val dateClickListener = View.OnClickListener {
        SelectableYearMonthListBSDialog.newInstance(
            selectedDate = viewModel.currentDate.value,
            yearsRange = 1
        ).show(supportFragmentManager, SelectableYearMonthListBSDialog.TAG)
    }

    private val backButtonClickListener = View.OnClickListener {
        finish()
    }

    private val handleCategorySelectionResult: (String, Bundle) -> Unit = { _, data ->
        data.parcelable<CategoryUiModel>(CategoryListBSDialog.BUNDLE_KEY_SELECTED_CATEGORY)
            ?.let { selectedCategoryItem ->
                viewModel.setCategory(selectedCategoryItem)
            }
    }

    override fun onClickYearMonth(date: LocalDate) {
        viewModel.setDate(date)
    }
}