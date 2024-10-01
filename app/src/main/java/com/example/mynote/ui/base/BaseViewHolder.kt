package com.example.mynote.ui.base

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 1..
 * Description:
 */

abstract class BaseViewHolder<ITEM: Any>(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {

    protected var bindItem: ITEM? = null

    protected val context: Context
        get() = itemView.context

    open fun bind(item: ITEM) {
        this.bindItem = item
    }
}