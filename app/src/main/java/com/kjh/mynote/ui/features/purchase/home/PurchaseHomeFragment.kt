package com.kjh.mynote.ui.features.purchase.home

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.view.View
import android.view.View.OnClickListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kjh.mynote.R
import com.kjh.mynote.databinding.FragmentPurchaseBinding
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.ui.base.BaseFragment
import com.kjh.mynote.ui.features.purchase.filter.PurchaseNoteHomeFilterBSDFragment
import com.kjh.mynote.ui.features.purchase.detail.PurchaseNoteDetailActivity
import com.kjh.mynote.ui.features.purchase.home.adapter.PurchaseHomeListAdapter
import com.kjh.mynote.ui.features.purchase.makeedit.MakeEditPurchaseNoteActivity
import com.kjh.mynote.ui.features.purchase.search.PurchaseNoteSearchActivity
import com.kjh.mynote.ui.features.statistics.purchasenote.PurchaseNoteStatisticsActivity
import com.kjh.mynote.utils.decorations.SpacingItemDecoration
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.parcelable
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.toComma
import com.kjh.mynote.utils.extensions.toMillis
import com.kjh.mynote.utils.extensions.toStringWithPattern
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
class PurchaseHomeFragment: BaseFragment<FragmentPurchaseBinding>({ FragmentPurchaseBinding.inflate(it) }) {

    private val viewModel: PurchaseHomeViewModel by viewModels()

    private val listAdapter: PurchaseHomeListAdapter by lazy {
        PurchaseHomeListAdapter(purchaseNoteItemClickAction)
    }

    override fun onInitView() {
        with (binding) {
            calendarMonthView.setMonthScrollListener(monthScrollListener)
            calendarMonthView.setDayClickAction(monthDayClickAction)

            rvPurchases.apply {
                itemAnimator = null
                addItemDecoration(SpacingItemDecoration(bottom = 15, exceptFirstItem = false))
                adapter = listAdapter
            }

            ivStatistics.setOnThrottleClickListener(statisticsClickListener)
            ivFilter.setOnThrottleClickListener(filterClickListener)
            ivSearch.setOnThrottleClickListener(searchClickListener)
            layoutEmpty.btnMakePurchase.setOnThrottleClickListener(makePurchaseEmptyBtnClickListener)
            fabMakePurchaseNote.setOnThrottleClickListener(makePurchaseFabClickListener)
        }
    }

    override fun onInitData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.currentMonth.collect { currentDate ->
                        binding.tvCurrentYearMonth.text = currentDate.toStringWithPattern("yyyy년 M월")
                    }
                }

                launch {
                    viewModel.uiState
                        .map { it.selectedDayPurchaseNotes }
                        .distinctUntilChanged()
                        .collect { notesInDay ->
                            binding.layoutEmpty.root.isVisible = notesInDay.isEmpty()
                            listAdapter.submitList(notesInDay)
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.selectedDay to it.hasEventDays }
                        .distinctUntilChanged()
                        .collect {
                            binding.calendarMonthView.updateCalendarUI(it)
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.selectedDayTotalPrice }
                        .distinctUntilChanged()
                        .collect { totalPrice ->
                            if (totalPrice > 0) {
                                binding.clTotalPriceContainer.isVisible = true
                                binding.tvTotalPrice.text = getString(R.string.format_total_purchase_price, totalPrice.toComma())
                            } else {
                                binding.clTotalPriceContainer.isVisible = false
                            }
                        }
                }

                launch {
                    viewModel.appliedFilterItems.collect { appliedFilterItems ->
                        with (binding) {
                            if (appliedFilterItems.isEmpty()) {
                                tvFilteredCount.isVisible = false
                                ivFilter.setColorFilter(ContextCompat.getColor(requireContext(), R.color.black_800))
                            } else {
                                tvFilteredCount.isVisible = true
                                tvFilteredCount.text = appliedFilterItems.size.toString()
                                ivFilter.setColorFilter(ContextCompat.getColor(requireContext(), R.color.purple))
                            }
                        }
                    }
                }
            }
        }
    }

    private val makeNoteResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val insertedPlaceNoteItem = result.data?.parcelable<PurchaseNoteUiModel>(
                AppConstants.INTENT_PURCHASE_NOTE_ITEM
            ) ?: return@registerForActivityResult

            viewModel.setSelectedDay(insertedPlaceNoteItem.purchaseLocalDate)
        }
    }

    private val purchaseNoteItemClickAction: (PurchaseNoteUiModel) -> Unit = { noteItem ->
        Intent(requireContext(), PurchaseNoteDetailActivity::class.java).apply {
            putExtra(AppConstants.INTENT_PURCHASE_NOTE_ID, noteItem.id)
            startActivity(this)
        }
    }

    private val monthScrollListener: (CalendarMonth) -> Unit = { date ->
        viewModel.setCurrentMonth(date.yearMonth)
    }

    private val monthDayClickAction: (LocalDate) -> Unit = { date ->
        viewModel.setSelectedDay(date)
    }

    private val statisticsClickListener = OnClickListener {
        Intent(requireContext(), PurchaseNoteStatisticsActivity::class.java).apply {
            putExtra(AppConstants.INTENT_DATE, viewModel.currentMonth.value.atStartOfMonth())
            startActivity(this)
        }
    }

    private val filterClickListener = OnClickListener {
        PurchaseNoteHomeFilterBSDFragment.newInstance()
            .show(childFragmentManager, PurchaseNoteHomeFilterBSDFragment.TAG)
    }

    private val searchClickListener = OnClickListener {
        Intent(requireContext(), PurchaseNoteSearchActivity::class.java).apply {
            startActivity(this)
        }
    }

    private val makePurchaseFabClickListener = View.OnClickListener {
        val selectedDay = viewModel.uiState.value.selectedDay.toMillis()

        Intent(requireContext(), MakeEditPurchaseNoteActivity::class.java).apply {
            putExtra(AppConstants.INTENT_PURCHASE_DATE, selectedDay)
            makeNoteResultLauncher.launch(this)
        }
    }

    private val makePurchaseEmptyBtnClickListener = View.OnClickListener {
        val selectedDay = viewModel.uiState.value.selectedDay.toMillis()

        Intent(requireContext(), MakeEditPurchaseNoteActivity::class.java).apply {
            putExtra(AppConstants.INTENT_PURCHASE_DATE, selectedDay)
            makeNoteResultLauncher.launch(this)
        }
    }

    companion object {
        const val TAG = "PurchaseHomeFragment"

        fun newInstance() = PurchaseHomeFragment()
    }
}