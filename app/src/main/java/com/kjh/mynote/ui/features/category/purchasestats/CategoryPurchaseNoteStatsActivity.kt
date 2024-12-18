package com.kjh.mynote.ui.features.category.purchasestats

import android.content.Intent
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.databinding.ActivityCategoryPurchaseNoteStatsBinding
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.common.dialog.yearmonths.SelectableYearMonthListBSDialog
import com.kjh.mynote.ui.features.category.list.CategoryListBSDialog
import com.kjh.mynote.ui.features.category.purchasestats.adapter.CategoryPurchaseNoteStatsUiListAdapter
import com.kjh.mynote.ui.features.purchase.detail.PurchaseNoteDetailActivity
import com.kjh.mynote.ui.features.purchase.make.MakePurchaseNoteActivity
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.decorations.PurchaseNotesUiStateItemDecoration2
import com.kjh.mynote.utils.extensions.registerStartActivityResultLauncher
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.showToast
import com.kjh.mynote.utils.extensions.toStringWithPattern
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 14..
 * Description:
 */

@AndroidEntryPoint
class CategoryPurchaseNoteStatsActivity
    : BaseActivity<ActivityCategoryPurchaseNoteStatsBinding>({ ActivityCategoryPurchaseNoteStatsBinding.inflate(it) }),
    SelectableYearMonthListBSDialog.YearMonthClickListener {

    private val viewModel: CategoryPurchaseNoteStatsViewModel by viewModels()

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
                addItemDecoration(PurchaseNotesUiStateItemDecoration2())
                adapter = listAdapter
            }

            llDateContainer.setOnThrottleClickListener(dateClickListener)
        }
    }

    override fun onInitUiData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState
                        .map { it.isLoading }
                        .distinctUntilChanged()
                        .collect { isLoading ->
                            binding.layoutLoading.root.isVisible = isLoading
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.errorMsg }
                        .distinctUntilChanged()
                        .collect { errorMsg ->
                            errorMsg?.let {
                                showToast(it)
                                viewModel.shownError()
                            }
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.currentDate }
                        .distinctUntilChanged()
                        .collect { date ->
                            binding.tvDate.text = date.toStringWithPattern("yyyy년 M월")
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.uiItems }
                        .distinctUntilChanged()
                        .collect { uiItems ->
                            listAdapter.submitList(uiItems)
                        }
                }
            }
        }
    }

    private val purchaseNoteDetailResultLauncher = registerStartActivityResultLauncher(resultOkBlock = {
        viewModel.getCategoryPurchaseNoteStats()
    })

    private val makeNoteResultLauncher = registerStartActivityResultLauncher(resultOkBlock = {
        viewModel.getCategoryPurchaseNoteStats()
    })

    private val categoryClickAction: () -> Unit =  {
        CategoryListBSDialog.newInstance(
            isEditable = false,
            selectedCategoryItem = viewModel.uiState.value.currentCategory,
            selectCategoryAction = { selectedCategory ->
                viewModel.setCategory(selectedCategory)
            }
        ).show(supportFragmentManager, CategoryListBSDialog.TAG)
    }

    private val purchaseNoteItemClickAction: (PurchaseNoteUiModel) -> Unit = { noteItem ->
        Intent(this, PurchaseNoteDetailActivity::class.java).apply {
            putExtra(AppConstants.INTENT_PURCHASE_NOTE_ID, noteItem.id)
            purchaseNoteDetailResultLauncher.launch(this)
        }
    }

    private val makePurchaseNoteClickAction: () -> Unit = {
        Intent(this, MakePurchaseNoteActivity::class.java).apply {
            makeNoteResultLauncher.launch(this)
        }
    }

    private val dateClickListener = View.OnClickListener {
        SelectableYearMonthListBSDialog.newInstance(
            selectedDate = viewModel.uiState.value.currentDate,
            yearsRange = 1
        ).show(supportFragmentManager, SelectableYearMonthListBSDialog.TAG)
    }

    override fun onClickYearMonth(date: LocalDate) {
        viewModel.setDate(date)
    }
}