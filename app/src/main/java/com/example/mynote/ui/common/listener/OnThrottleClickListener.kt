package com.example.mynote.ui.common.listener

import android.view.View

/**
 * Created by kangjonghyuk.
 * Created On 2024. 9. 24..
 * Description:
 * 이중 클릭 방지용 ClickListener Wrap Class.
 */

class OnThrottleClickListener(
    private val clickListener: View.OnClickListener,
    private val interval: Long = 600,
) : View.OnClickListener {

    private var clickable = true

    override fun onClick(v: View?) {
        if (clickable) {
            clickable = false
            v?.run {
                postDelayed({
                    clickable = true
                }, interval)

                clickListener.onClick(v)
            }
        }
    }
}