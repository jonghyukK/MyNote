package com.kjh.mynote.ui.features.place.detail

import android.view.View
import com.kjh.mynote.databinding.BsdPlaceDetailMenuDialogBinding
import com.kjh.mynote.ui.base.BaseBottomSheetDialogFragment
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 22..
 * Description:
 */

class PlaceNoteDetailMenuBSDialog
    : BaseBottomSheetDialogFragment<BsdPlaceDetailMenuDialogBinding>({
    BsdPlaceDetailMenuDialogBinding.inflate(it)
}) {
    private var deleteClickAction: (() -> Unit)? = null
    private var modifyClickAction: (() -> Unit)? = null

    override fun onInitView() {
        with(binding) {
            tvModify.setOnThrottleClickListener(modifyClickListener)
            tvDelete.setOnThrottleClickListener(deleteClickListener)
        }
    }

    override fun onInitData() {}

    override fun onDestroy() {
        super.onDestroy()
        deleteClickAction = null
        modifyClickAction = null
    }

    private val modifyClickListener = View.OnClickListener {
        modifyClickAction?.invoke()
        dismiss()
    }

    private val deleteClickListener = View.OnClickListener {
        deleteClickAction?.invoke()
        dismiss()
    }

    companion object {
        const val TAG = "PlaceNoteDetailMenuBSDialog"

        fun newInstance(
            deleteClickAction: () -> Unit,
            modifyClickAction: () -> Unit,
        ): PlaceNoteDetailMenuBSDialog = PlaceNoteDetailMenuBSDialog().apply {
            this.deleteClickAction = deleteClickAction
            this.modifyClickAction = modifyClickAction
        }
    }
}