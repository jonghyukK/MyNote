package com.kjh.mynote.ui.common.vh

import com.kjh.mynote.databinding.VhPurchaseNoteSearchResultDateItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.toStringWithPattern
import java.time.LocalDate

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 16..
 * Description:
 */

/**
 * 구매노트 날짜 Item ViewHolder.
 *
 * @property binding
 */
class PurchaseNoteDateItemViewHolder(
    private val binding: VhPurchaseNoteSearchResultDateItemBinding
): BaseViewHolder<LocalDate>(binding.root) {

    override fun bind(item: LocalDate) {
        super.bind(item)

        binding.tvDate.text = item.toStringWithPattern(AppConstants.DATE_FORMAT_YYYY_M_D_E)
    }
}