package com.kjh.mynote.ui.features.mypage

import com.kjh.mynote.databinding.FragmentMyPageBinding
import com.kjh.mynote.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 30..
 * Description:
 */

@AndroidEntryPoint
class MyPageFragment: BaseFragment<FragmentMyPageBinding>({ FragmentMyPageBinding.inflate(it) }) {

    override fun onInitView() {

    }

    override fun onInitData() {

    }

    companion object {
        const val TAG = "MyPageFragment"
        fun newInstance() = MyPageFragment()
    }
}