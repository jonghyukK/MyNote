package com.kjh.mynote.ui.base

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.kjh.mynote.R
import com.kjh.mynote.utils.extensions.getDisplaySize

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 22..
 * Description:
 */

enum class DialogType {
    FULL_SCREEN,
    DIALOG
}

abstract class BaseDialogFragment<B: ViewBinding>(
    private val bindingFactory: (LayoutInflater) -> B,
    private val dialogType: DialogType = DialogType.DIALOG
): DialogFragment() {

    private var _binding: B? = null
    protected val binding: B
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (dialogType) {
            DialogType.FULL_SCREEN -> {
                setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
            }
            DialogType.DIALOG -> {
                setStyle(STYLE_NORMAL, R.style.MyDefaultDialog)
            }
        }
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

    override fun onStart() {
        super.onStart()
        adjustViewWidthWhenTypeIsDialog()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun adjustViewWidthWhenTypeIsDialog() {
        if (dialogType == DialogType.DIALOG) {
            activity?.let {
                val deviceWidth = it.getDisplaySize().width()
                val sideSpace = it.resources.getDimensionPixelOffset(R.dimen.dialog_side_space)

                dialog?.window?.let { window ->
                    val params = window.attributes
                    params.width = deviceWidth - sideSpace
                    window.attributes = params
                }
            }
        }
    }

    protected abstract fun onInitView()

    protected abstract fun onInitData()
}