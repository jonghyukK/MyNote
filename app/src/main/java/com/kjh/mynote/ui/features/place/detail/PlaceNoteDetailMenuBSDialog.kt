package com.kjh.mynote.ui.features.place.detail

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
    private lateinit var deleteAction: () -> Unit
    private lateinit var modifyAction: () -> Unit

    override fun onInitView() {
        with(binding) {
            tvModify.onThrottleClick {
                modifyAction.invoke()
                dismiss()
            }
            tvDelete.onThrottleClick {
                deleteAction.invoke()
                dismiss()
            }
        }
    }

    override fun onInitData() {}

    companion object {
        const val TAG = "PlaceNoteDetailMenuBSDialog"

        fun newInstance(
            deleteAction: () -> Unit,
            modifyAction: () -> Unit
        ): PlaceNoteDetailMenuBSDialog = PlaceNoteDetailMenuBSDialog().apply {
            this.deleteAction = deleteAction
            this.modifyAction = modifyAction
        }
    }
}