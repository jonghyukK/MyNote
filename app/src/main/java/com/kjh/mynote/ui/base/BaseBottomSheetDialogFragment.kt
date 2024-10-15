package com.kjh.mynote.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kjh.mynote.R

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 15..
 * Description:
 */

abstract class BaseBottomSheetDialogFragment<B: ViewBinding>(
    private val bindingFactory: (LayoutInflater) -> B
): BottomSheetDialogFragment() {

    private var _binding: B? = null
    protected val binding: B
        get() = _binding!!

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)
//    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = bindingFactory(layoutInflater)
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

    protected abstract fun onInitView()

    protected abstract fun onInitData()
}