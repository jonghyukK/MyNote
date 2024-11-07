package com.kjh.mynote.ui.features.home

import com.kjh.mynote.databinding.FragmentHomeBinding
import com.kjh.mynote.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 7..
 * Description:
 */

@AndroidEntryPoint
class HomeFragment: BaseFragment<FragmentHomeBinding>({ FragmentHomeBinding.inflate(it) }) {
    override fun onInitView() {

    }

    override fun onInitData() {

    }

    companion object {
        const val TAG = "HomeFragment"

        fun newInstance() = HomeFragment()
    }
}