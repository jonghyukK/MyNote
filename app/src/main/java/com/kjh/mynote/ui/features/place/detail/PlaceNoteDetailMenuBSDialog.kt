package com.kjh.mynote.ui.features.place.detail

import android.content.Context
import com.kjh.mynote.databinding.BsdPlaceDetailMenuDialogBinding
import com.kjh.mynote.ui.base.BaseBottomSheetDialogFragment
import com.kjh.mynote.utils.extensions.onThrottleClick

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 22..
 * Description:
 */

class PlaceNoteDetailMenuBSDialog
    : BaseBottomSheetDialogFragment<BsdPlaceDetailMenuDialogBinding>({ BsdPlaceDetailMenuDialogBinding.inflate(it) })
{
    private var menuClickListener: PlaceNoteDetailMenuClickListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        menuClickListener = when {
            parentFragment is PlaceNoteDetailMenuClickListener -> parentFragment as PlaceNoteDetailMenuClickListener
            context is PlaceNoteDetailMenuClickListener -> context
            else -> throw IllegalStateException("Parent must implement PlaceNoteDetailMenuClickListener")
        }
    }

    override fun onInitView() {
        with(binding) {
            tvModify.onThrottleClick {
                menuClickListener?.onClickModifyMenu()
                dismiss()
            }
            tvDelete.onThrottleClick {
                menuClickListener?.onClickDeleteMenu()
                dismiss()
            }
        }
    }

    override fun onInitData() {}

    override fun onDetach() {
        super.onDetach()
        menuClickListener = null
    }

    interface PlaceNoteDetailMenuClickListener {
        fun onClickDeleteMenu()
        fun onClickModifyMenu()
    }

    companion object {
        const val TAG = "PlaceNoteDetailMenuBSDialog"

        fun newInstance(): PlaceNoteDetailMenuBSDialog = PlaceNoteDetailMenuBSDialog()
    }
}