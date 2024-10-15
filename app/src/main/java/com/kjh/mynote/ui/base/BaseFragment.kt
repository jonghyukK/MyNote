package com.kjh.mynote.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 11..
 * Description:
 */
abstract class BaseFragment<B: ViewBinding>(
    private val bindingFactory: (LayoutInflater) -> B
): Fragment() {

    private var _binding: B? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bindingFactory(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onInitView()
        onInitData()
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    abstract fun onInitView()
    abstract fun onInitData()
}