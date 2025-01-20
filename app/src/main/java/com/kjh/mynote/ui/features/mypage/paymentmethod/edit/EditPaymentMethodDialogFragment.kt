package com.kjh.mynote.ui.features.mypage.paymentmethod.edit

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.R
import com.kjh.mynote.databinding.DialogFragmentMakeOrEditPaymentMethodBinding
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.ui.base.BaseDialogFragment
import com.kjh.mynote.ui.base.DialogType
import com.kjh.mynote.utils.extensions.highlightText
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 31..
 * Description:
 */

@AndroidEntryPoint
class EditPaymentMethodDialogFragment: BaseDialogFragment<DialogFragmentMakeOrEditPaymentMethodBinding>({
    DialogFragmentMakeOrEditPaymentMethodBinding.inflate(it)
}, dialogType = DialogType.FULL_SCREEN) {

    private val viewModel: EditPaymentMethodViewModel by viewModels()

    override fun onInitView() {
        with (binding) {
            tbToolbar.leftTitle = getString(R.string.edit_payment_method)
            btnBottom.btnTitle = getString(R.string.do_modify)

            layoutTip.tvExample1.highlightText(
                fullText = getString(R.string.desc_payment_method_name_example1),
                wordToHighlight = getString(R.string.payment_method_name_example1_highlight_text),
                isBold = true
            )

            layoutTip.tvExample2.highlightText(
                fullText = getString(R.string.desc_payment_method_name_example2),
                wordToHighlight = getString(R.string.payment_method_name_example2_highlight_text),
                isBold = true
            )

            etPaymentMethodName.addCustomTextWatcher(paymentMethodNameTextWatcher)

            clRegisterDefaultPaymentMethod.setOnThrottleClickListener(registerDefaultPaymentMethodClickListener)
            btnBottom.setOnThrottleClickListener(modifyClickListener)

            etPaymentMethodName.post {
                etPaymentMethodName.setFocus()
            }
        }
    }

    override fun onInitData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.editPaymentMethodEvent.collect { event ->
                        when (event) {
                            is EditPaymentMethodEventState.Loading -> {
                                binding.btnBottom.isLoading = true
                            }
                            is EditPaymentMethodEventState.Error -> {
                                binding.btnBottom.isLoading = false
                                showToast(event.errorMsg)
                            }
                            is EditPaymentMethodEventState.Success -> {
                                binding.btnBottom.isLoading = false
                                dismiss()
                            }
                        }
                    }
                }

                launch {
                    viewModel.uiState
                        .map { it.paymentMethodName }
                        .distinctUntilChanged()
                        .collect { paymentMethodName ->
                            if (binding.etPaymentMethodName.text != paymentMethodName) {
                                binding.etPaymentMethodName.text = paymentMethodName
                            }
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.isDefault }
                        .distinctUntilChanged()
                        .collect { isDefault ->
                            binding.ivCheck.setImageResource(
                                if (isDefault)
                                    R.drawable.ic_selected_checkbox
                                else
                                    R.drawable.ic_unselected_checkbox
                            )
                        }
                }

                launch {
                    viewModel.editButtonEnable.collectLatest {
                        binding.btnBottom.isEnable = it
                    }
                }
            }
        }
    }

    private val paymentMethodNameTextWatcher = object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            viewModel.setPaymentMethodName(s.toString())
        }
    }

    private val registerDefaultPaymentMethodClickListener = View.OnClickListener {
        viewModel.toggleDefaultState()
    }

    private val modifyClickListener = View.OnClickListener {
        if (binding.btnBottom.isEnable) {
            viewModel.editPaymentMethod()
        }
    }

    companion object {
        const val TAG = "EditPaymentMethodDialogFragment"
        const val ARG_PAYMENT_METHOD_ITEM = "ARG_PAYMENT_METHOD_ITEM"

        fun newInstance(
            paymentMethodItem: PaymentMethodUiModel
        ) = EditPaymentMethodDialogFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_PAYMENT_METHOD_ITEM, paymentMethodItem)
            }
        }
    }
}