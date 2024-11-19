package com.kjh.mynote.ui.common.dialog.yearmonths

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.databinding.BsdSelectableYearMonthListDialogBinding
import com.kjh.mynote.ui.base.BaseBottomSheetDialogFragment
import com.kjh.mynote.ui.common.dialog.yearmonths.adapter.SelectableYearMonthListAdapter
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.toMillis
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 18..
 * Description: 현재 날짜를 기준으로 yearsRange 값의 년까지 년월 리스트를 표시하는 BottomSheetDialogFragment.
 *
 */

class SelectableYearMonthListBSDialog
    : BaseBottomSheetDialogFragment<BsdSelectableYearMonthListDialogBinding>({
    BsdSelectableYearMonthListDialogBinding.inflate(it)
}) {
    private val viewModel: SelectableYearMonthListViewModel by viewModels()

    private val listAdapter: SelectableYearMonthListAdapter by lazy {
        SelectableYearMonthListAdapter(itemClickAction)
    }

    private var yearMonthClickListener: YearMonthClickListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        yearMonthClickListener = when {
            parentFragment is YearMonthClickListener -> parentFragment as YearMonthClickListener
            context is YearMonthClickListener -> context
            else -> throw IllegalStateException("Parent must implement YearMonthClickListener")
        }
    }
    override fun onInitView() {
        with (binding) {
            rvYearMonthList.apply {
                itemAnimator = null
                adapter = listAdapter
            }

            ivClose.setOnThrottleClickListener(closeClickListener)
        }
    }

    override fun onInitData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { yearMonthList ->
                    listAdapter.submitList(yearMonthList)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        yearMonthClickListener = null
    }

    private val itemClickAction: (LocalDate) -> Unit = {
        yearMonthClickListener?.onClickYearMonth(it)
        dismiss()
    }

    private val closeClickListener = View.OnClickListener {
        dismiss()
    }

    interface YearMonthClickListener {
        fun onClickYearMonth(date: LocalDate)
    }

    companion object {
        const val TAG = "SelectableYearMonthListBSDialog"
        const val ARG_LONG_DATE = "ARG_LONG_DATE"
        const val ARG_INT_YEARS_RANGE = "ARG_INT_YEARS_RANGE"

        fun newInstance(
            selectedDate: LocalDate,
            yearsRange: Int = AppConstants.DEFAULT_YEARS_RANGE
        ) = SelectableYearMonthListBSDialog().apply {
            arguments = Bundle().apply {
                putLong(ARG_LONG_DATE, selectedDate.toMillis())
                putInt(ARG_INT_YEARS_RANGE, yearsRange)
            }
        }
    }
}