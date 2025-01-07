package com.kjh.mynote.ui.features.purchase.filter.adapter

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kjh.mynote.ui.features.purchase.filter.PurchaseNoteHomeFilterBSDFragment
import com.kjh.mynote.ui.features.purchase.filter.PurchaseNoteHomeFilterPagerFragment
import kotlinx.parcelize.Parcelize

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 7..
 * Description:
 */

@Parcelize
enum class FilterType(val title: String): Parcelable {
    Category("카테고리"),
    PaymentMethod("결제수단")
}

class PurchaseNoteHomeFilterPagerAdapter(
    fragment: PurchaseNoteHomeFilterBSDFragment
): FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = FilterType.entries.size

    override fun createFragment(position: Int): Fragment =
        PurchaseNoteHomeFilterPagerFragment.newInstance(FilterType.entries[position])
}