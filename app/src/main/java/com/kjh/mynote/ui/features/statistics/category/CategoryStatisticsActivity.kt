package com.kjh.mynote.ui.features.statistics.category

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ConcatAdapter
import com.kjh.mynote.databinding.ActivityCategoryPurchaseNoteStatsBinding
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.common.bsdialog.categorylist.CategoryListBSDialog
import com.kjh.mynote.ui.common.dialog.yearmonths.SelectableYearMonthListBSDialog
import com.kjh.mynote.ui.features.purchase.detail.PurchaseNoteDetailActivity
import com.kjh.mynote.ui.features.purchase.makeedit.MakeEditPurchaseNoteActivity
import com.kjh.mynote.ui.features.purchase.search.adapter.PurchaseNoteSearchResultListAdapter
import com.kjh.mynote.ui.features.statistics.category.adapter.CategoryStatisticsListAdapter
import com.kjh.mynote.ui.features.statistics.category.decoration.CategoryStatisticsListItemDecoration
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.makeGone
import com.kjh.mynote.utils.extensions.makeVisible
import com.kjh.mynote.utils.extensions.parcelable
import com.kjh.mynote.utils.extensions.showToast
import com.kjh.mynote.utils.extensions.toStringWithPattern
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
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

    private val listAdapter: CategoryStatisticsListAdapter by lazy {
        CategoryStatisticsListAdapter(
            categoryClickAction = categoryClickAction,
            purchaseNoteItemClickAction = purchaseNoteItemClickAction
        )
    }

    override fun onInitView() {
        with(binding) {
            rvPurchaseNotes.apply {
                itemAnimator = null
                addItemDecoration(CategoryStatisticsListItemDecoration())
                adapter = listAdapter
            }

            tbToolbar.setDateClickListener(dateClickListener)
            tbToolbar.setBackButtonClickListener(backButtonClickListener)
        }

        supportFragmentManager.setFragmentResultListener(
            CategoryListBSDialog.REQUEST_KEY, this, handleCategorySelectionResult
        )
    }

    override fun onInitUiData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.errorMessage.collectLatest(::showToast)
                }

                launch {
                    viewModel.currentDate.collect { date ->
                        binding.tbToolbar.date = date.toStringWithPattern(AppConstants.DATE_FORMAT_YYYY_M)
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
                            }
                            is CategoryStatisticsUiState.Success -> {
                                binding.layoutLoading.root.makeGone()
                                listAdapter.submitList(uiState.uiItems)
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