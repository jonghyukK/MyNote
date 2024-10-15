package com.kjh.mynote.ui.features.main.product

import com.kjh.mynote.databinding.FragmentMyProductBinding
import com.kjh.mynote.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 11..
 * Description:
 */

@AndroidEntryPoint
class MyProductFragment: BaseFragment<FragmentMyProductBinding>({ FragmentMyProductBinding.inflate(it) }) {

    override fun onInitView() {

    }

    override fun onInitData() {

    }

    companion object {
        const val TAG = "MyProductFragment"
        fun newInstance() = MyProductFragment()
    }
}