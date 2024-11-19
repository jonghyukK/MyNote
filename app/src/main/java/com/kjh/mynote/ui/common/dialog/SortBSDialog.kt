package com.kjh.mynote.ui.common.dialog

import android.content.Context
import android.os.Bundle
import androidx.core.view.isVisible
import com.kjh.mynote.R
import com.kjh.mynote.databinding.BsdSortDilaogBinding
import com.kjh.mynote.ui.base.BaseBottomSheetDialogFragment
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.setTextColorRes
import dagger.hilt.android.AndroidEntryPoint

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 18..
 * Description:
 *
 *  Sort 관련 BottomSheetDialogFragment.
 *
 *  최근 날짜 순
 *  오래된 날짜 순
 */

@AndroidEntryPoint
class SortBSDialog: BaseBottomSheetDialogFragment<BsdSortDilaogBinding>({ BsdSortDilaogBinding.inflate(it) }) {

    private var isDescending: Boolean = true

    private var listener: SortBSDialogClickListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = when {
            parentFragment is SortBSDialogClickListener -> parentFragment as SortBSDialogClickListener
            context is SortBSDialogClickListener -> context
            else -> throw IllegalStateException("Parent must implement SortBSDialogClickListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            isDescending = it.getBoolean(ARG_BOOL_IS_DESCENDING)
        }
    }

    override fun onInitView() {
        with (binding) {
            ivCheckDescending.isVisible = isDescending
            ivCheckAscending.isVisible = !isDescending

            if (isDescending) {
                tvSortDescending.setTextColorRes(R.color.purple)
                tvSortAscending.setTextColorRes(R.color.black_800)
            } else {
                tvSortDescending.setTextColorRes(R.color.black_800)
                tvSortAscending.setTextColorRes(R.color.purple)
            }

            clSortDescending.onThrottleClick {
                listener?.onClickSort(isDescending = true)
                dismiss()
            }

            clSortAscending.onThrottleClick {
                listener?.onClickSort(isDescending = false)
                dismiss()
            }
        }
    }

    override fun onInitData() {}

    override fun onDestroy() {
        super.onDestroy()
        listener = null
    }

    interface SortBSDialogClickListener {
        fun onClickSort(isDescending: Boolean)
    }

    companion object {
        const val TAG = "SortBSDialog"
        const val ARG_BOOL_IS_DESCENDING = "ARG_BOOL_IS_DESCENDING"

        fun newInstance(
            currentSort: Boolean
        ) = SortBSDialog().apply {
            arguments = Bundle().apply {
                putBoolean(ARG_BOOL_IS_DESCENDING, currentSort)
            }
        }
    }
}