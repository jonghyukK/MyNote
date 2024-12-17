package com.kjh.mynote.ui.features.category.purchasestats

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.R
import com.kjh.mynote.databinding.LayoutEmptyMyPurchasesBinding
import com.kjh.mynote.databinding.VhCategoryPurchaseNoteStatsBarChartItemBinding
import com.kjh.mynote.databinding.VhCategoryPurchaseNotesStatsInfoItemBinding
import com.kjh.mynote.databinding.VhPurchaseNameStatsBarItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.extensions.highlightText
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.toComma

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 16..
 * Description:
 */

class CategoryPurchaseNoteStatsInfoItemViewHolder(
    private val binding: VhCategoryPurchaseNotesStatsInfoItemBinding,
    private val categoryClickAction: () -> Unit
): BaseViewHolder<CategoryPurchaseNoteStatsUiState.StatsInfoItem>(binding.root) {

    init {
        binding.clCategory.onThrottleClick {
            bindItem?.let { categoryClickAction() }
        }
    }

    override fun bind(item: CategoryPurchaseNoteStatsUiState.StatsInfoItem) {
        super.bind(item)

        with (binding) {
            tvCategoryName.text = item.currentCategory?.categoryName ?: "카테고리 없음"
            tvTotalNoteCount.highlightText(
                fullText = context.getString(R.string.format_total_count_with_unit, item.totalNoteCount),
                wordToHighlight = item.totalNoteCount.toString()
            )

            tvTotalNotePrice.highlightText(
                fullText = context.getString(R.string.format_total_price, item.totalNotePrice.toComma()),
                wordToHighlight = item.totalNotePrice.toComma()
            )
        }
    }
}

class CategoryPurchaseNoteStatsBarChartItem(
    private val binding: VhCategoryPurchaseNoteStatsBarChartItemBinding
): BaseViewHolder<CategoryPurchaseNoteStatsUiState.ChartItem>(binding.root) {

    private val innerListAdapter = TestListAdapter()

    init {
        binding.rvPurchaseNameBars.apply {
            itemAnimator = null
            adapter = innerListAdapter
        }
    }

    override fun bind(item: CategoryPurchaseNoteStatsUiState.ChartItem) {
        super.bind(item)

        binding.tvTitle.highlightText(
            "생활비 카테고리의 구매노트명 비율",
            "생활비"
        )
        innerListAdapter.submitList(item.items)
    }
}

class TestListAdapter(

): ListAdapter<PurchaseNameStatsWithColor, TestItemViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        TestItemViewHolder(
            VhPurchaseNameStatsBarItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: TestItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR =
            object : DiffUtil.ItemCallback<PurchaseNameStatsWithColor>() {
                override fun areItemsTheSame(
                    oldItem: PurchaseNameStatsWithColor,
                    newItem: PurchaseNameStatsWithColor,
                ): Boolean = oldItem.purchaseName == newItem.purchaseName

                override fun areContentsTheSame(
                    oldItem: PurchaseNameStatsWithColor,
                    newItem: PurchaseNameStatsWithColor,
                ): Boolean = oldItem == newItem
            }
    }

}

class TestItemViewHolder(
    private val binding: VhPurchaseNameStatsBarItemBinding
): BaseViewHolder<PurchaseNameStatsWithColor>(binding.root) {

    override fun bind(item: PurchaseNameStatsWithColor) {
        super.bind(item)

        with (binding) {
            tvPurchaseName.text = item.purchaseName
            tvCount.text = "${item.totalCount}건"

            lpiProgressBar.setIndicatorColor(ContextCompat.getColor(context, item.color))
            val progress = item.totalCount
            lpiProgressBar.max = 6
            lpiProgressBar.setProgress(progress, true)
        }
    }
}

class PurchaseNotesEmptyItemViewHolder(
    private val binding: LayoutEmptyMyPurchasesBinding,
    private val makePurchaseNoteClickAction: () -> Unit
): BaseViewHolder<Unit>(binding.root) {

    init {
        binding.btnMakePurchase.onThrottleClick {
            bindItem?.let { makePurchaseNoteClickAction() }
        }
    }

    override fun bind(item: Unit) {
        super.bind(item)

        binding.root.apply {
            setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            val lp = layoutParams
            lp.height = context.resources.getDimensionPixelSize(R.dimen.purchase_note_empty_view_height)
            layoutParams = lp
        }
    }
}