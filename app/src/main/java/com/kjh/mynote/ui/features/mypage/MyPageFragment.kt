package com.kjh.mynote.ui.features.mypage

import android.content.Intent
import android.view.View
import com.kjh.mynote.databinding.FragmentMyPageBinding
import com.kjh.mynote.ui.base.BaseFragment
import com.kjh.mynote.ui.features.mypage.category.CategoryManageActivity
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import dagger.hilt.android.AndroidEntryPoint

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 30..
 * Description:
 */

@AndroidEntryPoint
class MyPageFragment: BaseFragment<FragmentMyPageBinding>({ FragmentMyPageBinding.inflate(it) }) {

    override fun onInitView() {
        with (binding) {
            clMyCategories.setOnThrottleClickListener(categoryManageClickListener)
        }
    }

    override fun onInitData() {

    }

    private val categoryManageClickListener = View.OnClickListener {
        Intent(requireContext(), CategoryManageActivity::class.java).apply {
            startActivity(this)
        }
    }

    companion object {
        const val TAG = "MyPageFragment"
        fun newInstance() = MyPageFragment()
    }
}