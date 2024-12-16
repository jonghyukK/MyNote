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
import com.kjh.mynote.ui.features.purchase.detail.PurchaseNoteDetailActivity
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.decorations.PurchaseNotesUiStateItemDecoration
import com.kjh.mynote.utils.extensions.addClickAnimation
import com.kjh.mynote.utils.extensions.highlightText
import com.kjh.mynote.utils.extensions.registerStartActivityResultLauncher
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.toComma
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

    private val listAdapter: CategoryPurchaseNoteStatsListAdapter by lazy {
        CategoryPurchaseNoteStatsListAdapter(purchaseNoteItemClickAction)
    }

    override fun onInitView() {
        with(binding) {
            rvPurchaseNotes.apply {
                itemAnimator = null
                addItemDecoration(PurchaseNotesUiStateItemDecoration())
                adapter = listAdapter
            }

            layoutStatsInfoContainer.clCategory.addClickAnimation()
            layoutStatsInfoContainer.clCategory.setOnThrottleClickListener(categoryClickListener)
            llDateContainer.setOnThrottleClickListener(dateClickListener)
        }
    }

    override fun onInitUiData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.currentCategory.collect { category ->
                        binding.layoutStatsInfoContainer.tvCategoryName.text = category?.categoryName
                    }
                }

                launch {
                    viewModel.currentDate.collect { date ->
                        binding.tvDate.text = date.toStringWithPattern("yyyy년 M월")
                    }
                }

                launch {
                    viewModel.uiState
                        .map { it.totalNoteCount }
                        .distinctUntilChanged()
                        .collect {
                            binding.layoutStatsInfoContainer.tvTotalNoteCount.highlightText(
                                fullText = "총 ${it}건",
                                wordToHighlight = it.toString()
                            )
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.totalNotePrice }
                        .distinctUntilChanged()
                        .collect {
                            binding.layoutStatsInfoContainer.tvTotalNotePrice.highlightText(
                                fullText = "총 금액 ${it.toComma()}원",
                                wordToHighlight = it.toComma()
                            )
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.isEmpty }
                        .distinctUntilChanged()
                        .collect { isEmpty ->
                            binding.layoutEmpty.root.isVisible = isEmpty
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.purchaseNotes }
                        .distinctUntilChanged()
                        .collect {
                            listAdapter.submitList(it)
                        }
                }
            }
        }
    }

    private val purchaseNoteDetailResultLauncher = registerStartActivityResultLauncher(resultOkBlock = {
        viewModel.getPurchaseNotes()
    })

    private val purchaseNoteItemClickAction: (PurchaseNoteUiModel) -> Unit = { noteItem ->
        Intent(this, PurchaseNoteDetailActivity::class.java).apply {
            putExtra(AppConstants.INTENT_PURCHASE_NOTE_ID, noteItem.id)
            purchaseNoteDetailResultLauncher.launch(this)
        }
    }

    private val dateClickListener = View.OnClickListener {
        SelectableYearMonthListBSDialog.newInstance(
            selectedDate = viewModel.currentDate.value,
            yearsRange = 1
        ).show(supportFragmentManager, SelectableYearMonthListBSDialog.TAG)
    }

    private val categoryClickListener = View.OnClickListener {
        CategoryListBSDialog.newInstance(
            isEditable = false,
            selectedCategoryItem = viewModel.currentCategory.value,
            selectCategoryAction = { selectedCategory ->
                viewModel.setCategory(selectedCategory)
            }
        ).show(supportFragmentManager, CategoryListBSDialog.TAG)
    }

    override fun onClickYearMonth(date: LocalDate) {
        viewModel.setDate(date)
    }
}