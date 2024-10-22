package com.kjh.mynote.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.kjh.mynote.R
import com.kjh.mynote.utils.extensions.getDisplaySize

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 22..
 * Description:
 */

abstract class BaseDialogFragment<B: ViewBinding>(
    private val bindingFactory: (LayoutInflater) -> B
): DialogFragment() {

    private var _binding: B? = null
    protected val binding: B
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.MyDefaultDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = bindingFactory(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onInitView()
        onInitData()
    }

    override fun onResume() {
        super.onResume()

        activity?.let {
            val deviceWidth = it.getDisplaySize().width()
            val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
            params?.width = deviceWidth - it.resources.getDimensionPixelOffset(R.dimen.dialog_side_space)
            dialog?.window?.attributes = params as WindowManager.LayoutParams
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected abstract fun onInitView()

    protected abstract fun onInitData()
}