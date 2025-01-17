package com.kjh.mynote.ui.features.statistics.category.adapter

import androidx.core.content.ContextCompat
import com.kjh.mynote.R
import com.kjh.mynote.databinding.LayoutEmptyMyPurchasesBinding
import com.kjh.mynote.databinding.VhCategoryPurchaseNoteStatsRankingSectionItemBinding
import com.kjh.mynote.databinding.VhCategoryPurchaseNotesStatsInfoItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.statistics.category.CategoryPurchaseNoteStatsUiItems
import com.kjh.mynote.utils.decorations.SpacingItemDecoration
import com.kjh.mynote.utils.extensions.highlightText
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.toComma

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 18..
 * Description:
 */

/**
 * 카테고리별 구매노트 통계 화면 - 구매노트 총 갯수 + 총 가격 정보 Item ViewHolder.
 *
 * @property binding
 * @property categoryClickAction
 */
class CategoryPurchaseNoteStatsInfoItemViewHolder(
    private val binding: VhCategoryPurchaseNotesStatsInfoItemBinding,
    private val categoryClickAction: () -> Unit
): BaseViewHolder<CategoryPurchaseNoteStatsUiItems.StatsInfoItem>(binding.root) {

    init {
        binding.clCategory.onThrottleClick {
            bindItem?.let { categoryClickAction() }
        }
    }

    override fun bind(item: CategoryPurchaseNoteStatsUiItems.StatsInfoItem) {
        super.bind(item)

        with (binding) {
            tvCategoryName.text = item.currentCategory?.categoryName ?: "카테고리 없음"
            tvTotalNoteCount.highlightText(
                fullText = context.getString(R.string.format_total_count_with_unit, item.totalNoteCount),
                wordToHighlight = item.totalNoteCount.toString(),
                true
            )

            tvTotalNotePrice.highlightText(
                fullText = context.getString(R.string.format_total_price, item.totalNotePrice.toComma()),
                wordToHighlight = item.totalNotePrice.toComma(),
                true
            )
        }
    }
}

/**
 * 카테고리별 구매노트 통계 화면 - 구매노트명 랭킹 Item ViewHolder.
 *
 * @property binding
 */
class CategoryPurchaseNoteStatsRankingSectionItemViewHolder(
    private val binding: VhCategoryPurchaseNoteStatsRankingSectionItemBinding
): BaseViewHolder<CategoryPurchaseNoteStatsUiItems.PurchaseNameRankingItem>(binding.root) {

    private val innerListAdapter = PurchaseNameRankingListAdapter()

    init {
        binding.rvRankings.apply {
            itemAnimator = null
            if (itemDecorationCount == 0) {
                addItemDecoration(SpacingItemDecoration(top = 6))
            }
            adapter = innerListAdapter
        }
    }

    override fun bind(item: CategoryPurchaseNoteStatsUiItems.PurchaseNameRankingItem) {
        super.bind(item)

        innerListAdapter.submitList(item.purchaseNameStatsItems)
    }
}

/**
 * 카테고리별 구매노트 통계 화면 - 구매노트 목록 Empty Item ViewHolder.
 *
 * @property binding
 * @property makePurchaseNoteClickAction
 */
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

        binding.root.setBackgroundColor(ContextCompat.getColor(context, R.color.black_50))
    }
}