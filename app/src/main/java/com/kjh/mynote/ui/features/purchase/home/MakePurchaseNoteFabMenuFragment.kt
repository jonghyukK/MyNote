package com.kjh.mynote.ui.features.purchase.home

import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.animation.doOnEnd
import com.kjh.mynote.R
import com.kjh.mynote.databinding.DialogFragmentMakePurchaseNoteFabMenuBinding
import com.kjh.mynote.ui.base.BaseDialogFragment
import com.kjh.mynote.ui.base.DialogType
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import dagger.hilt.android.AndroidEntryPoint

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 31..
 * Description:
 */

@AndroidEntryPoint
class MakePurchaseNoteFabMenuFragment : BaseDialogFragment<DialogFragmentMakePurchaseNoteFabMenuBinding>(
    { DialogFragmentMakePurchaseNoteFabMenuBinding.inflate(it) },
    DialogType.FULL_SCREEN_WITH_DIM
) {

    private var eventListener: MakePurchaseNoteFabMenuClickListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        eventListener = when {
            parentFragment is MakePurchaseNoteFabMenuClickListener -> parentFragment as MakePurchaseNoteFabMenuClickListener
            context is MakePurchaseNoteFabMenuClickListener -> context
            else -> throw IllegalStateException("parent must implement MakePurchaseNoteFabMenuClickListener")
        }
    }

    override fun onInitView() {
        startFabMenuAnimation()

        with (binding) {
            clFabContainer.setOnThrottleClickListener(backgroundClickListener)
            ivFabClose.setOnThrottleClickListener(fabCloseClickListener)
            ivMakeSingleNote.setOnThrottleClickListener(makeSingleNoteFabClickListener)
            ivMakeMultipleNote.setOnThrottleClickListener(makeMultipleNoteFabClickListener)
        }
    }

    override fun onInitData() {}

    override fun onDestroyView() {
        super.onDestroyView()
        eventListener = null
    }

    private fun startFabMenuAnimation() = with (binding) {
        val fabSize = resources.getDimensionPixelSize(R.dimen.fab_button_size)
        val fabSpacing = resources.getDimensionPixelSize(R.dimen.fab_menu_icon_margin)

        val singleNoteTranslationY = (fabSize + fabSpacing).toFloat()
        val multiNoteTranslationY = (singleNoteTranslationY * 2)

        ivMakeSingleNote.apply {
            translationY = singleNoteTranslationY
        }.also { translationYAnim(it) }

        tvMakeSingleNote.apply {
            translationY = singleNoteTranslationY
        }.also { translationYAnim(it) }

        ivMakeMultipleNote.apply {
            translationY = multiNoteTranslationY
        }.also { translationYAnim(it) }

        tvMakeMultipleNote.apply {
            translationY = multiNoteTranslationY
        }.also { translationYAnim(it) }

        rotateFabWhenOpen(ivFabClose)
    }

    private fun translationYAnim(view: View) {
        view.animate()
            .translationY(0f)
            .setInterpolator(OvershootInterpolator())
            .setDuration(TRANSLATION_DURATION)
            .start()
    }

    private fun rotateFabWhenOpen(view: View) {
        ObjectAnimator.ofFloat(view, "rotation", ROTATION_ANGLE).apply {
            duration = ROTATE_DURATION
            start()
        }
    }

    private fun rotateFabWhenClose(view: View) {
        ObjectAnimator.ofFloat(view, "rotation", 0f).apply {
            duration = ROTATE_DURATION
            doOnEnd {
                dismiss()
            }
            start()
        }
    }

    private val backgroundClickListener = View.OnClickListener {
        rotateFabWhenClose(binding.ivFabClose)
    }

    private val fabCloseClickListener = View.OnClickListener {
        rotateFabWhenClose(binding.ivFabClose)
    }

    private val makeSingleNoteFabClickListener = View.OnClickListener {
        eventListener?.onMakeSingleNoteClick()
        dismiss()
    }

    private val makeMultipleNoteFabClickListener = View.OnClickListener {
        eventListener?.onMakeMultipleNoteClick()
        dismiss()
    }

    interface MakePurchaseNoteFabMenuClickListener {
        fun onMakeSingleNoteClick()
        fun onMakeMultipleNoteClick()
    }

    companion object {
        const val TAG = "MakePurchaseNoteFabMenuFragment"

        private const val ROTATE_DURATION = 100L
        private const val ROTATION_ANGLE = 45f
        private const val TRANSLATION_DURATION = 150L

        fun newInstance() = MakePurchaseNoteFabMenuFragment()
    }
}