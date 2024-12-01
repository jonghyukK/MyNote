package com.kjh.mynote.ui.common.dialog.sort

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import com.kjh.mynote.databinding.BsdSortDilaogBinding
import com.kjh.mynote.ui.base.BaseBottomSheetDialogFragment
import com.kjh.mynote.ui.common.dialog.sort.adapter.SortListAdapter
import com.kjh.mynote.utils.extensions.parcelableArrayList
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize

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

@Parcelize
data class SortItem(
    val type: SortType = SortType.LATEST,
    val isSelected: Boolean = false
): Parcelable

enum class SortType(val title: String) {
    LATEST("최근 날짜 순"),
    OLDEST("오래된 날짜 순"),
    HIGH_PRICE("가격 높은 순"),
    LOW_PRICE("가격 낮은 순")
}

@AndroidEntryPoint
class SortBSDialog: BaseBottomSheetDialogFragment<BsdSortDilaogBinding>({ BsdSortDilaogBinding.inflate(it) }) {

    private val sortListAdapter: SortListAdapter by lazy {
        SortListAdapter(sortItemClickAction = sortItemClickAction)
    }

    private var listener: SortBSDialogClickListener? = null
    private var sortList: List<SortItem> = emptyList()

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
            sortList = it.parcelableArrayList<SortItem>(ARG_LIST_SORT_ITEMS) ?: emptyList()
        }
    }

    override fun onInitView() {
        with (binding) {
            rvSort.apply {
                itemAnimator = null
                adapter = sortListAdapter
            }

            ivClose.setOnThrottleClickListener(closeClickListener)
        }
    }

    override fun onInitData() {
        sortListAdapter.submitList(sortList)
    }

    override fun onDestroy() {
        super.onDestroy()
        listener = null
    }

    private val sortItemClickAction: (SortType) -> Unit = { sort ->
        listener?.onClickSort(sort)
        dismiss()
    }

    private val closeClickListener = View.OnClickListener {
        dismiss()
    }

    interface SortBSDialogClickListener {
        fun onClickSort(sort: SortType)
    }

    companion object {
        const val TAG = "SortBSDialog"
        const val ARG_LIST_SORT_ITEMS = "ARG_LIST_SORT_ITEMS"

        fun newInstance(
            sortList: List<SortItem>
        ) = SortBSDialog().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(ARG_LIST_SORT_ITEMS, ArrayList(sortList))
            }
        }
    }
}