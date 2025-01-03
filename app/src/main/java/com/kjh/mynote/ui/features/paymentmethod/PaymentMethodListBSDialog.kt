package com.kjh.mynote.ui.features.paymentmethod

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.databinding.BsdPaymentMethodListDialogBinding
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.ui.base.BaseBottomSheetDialogFragment
import com.kjh.mynote.ui.features.mypage.paymentmethod.edit.EditPaymentMethodDialogFragment
import com.kjh.mynote.ui.features.mypage.paymentmethod.make.MakePaymentMethodDialogFragment
import com.kjh.mynote.ui.features.paymentmethod.adapter.PaymentMethodListAdapter
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.parcelable
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 3..
 * Description:
 */

@AndroidEntryPoint
class PaymentMethodListBSDialog : BaseBottomSheetDialogFragment<BsdPaymentMethodListDialogBinding>({
    BsdPaymentMethodListDialogBinding.inflate(it)
}), FragmentResultListener {

    private val viewModel: PaymentMethodListViewModel by viewModels()

    private val listAdapter: PaymentMethodListAdapter by lazy {
        PaymentMethodListAdapter(
            paymentMethodItemClickAction = paymentMethodItemClickAction,
            editPaymentMethodClickAction = editPaymentMethodClickAction
        )
    }

    override fun onInitView() {
        with (binding) {
            rvItems.apply {
                itemAnimator = null
                adapter = listAdapter
            }

            ivAdd.setOnThrottleClickListener(addPaymentMethodClickListener)
        }
    }

    override fun onInitData() {
        childFragmentManager.setFragmentResultListener(
            EditPaymentMethodDialogFragment.REQUEST_KEY, this, this)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    when (uiState) {
                        is PaymentMethodListUiState.Loading -> {}
                        is PaymentMethodListUiState.Error -> {
                            uiState.errorMsg?.let { showToast(it) }
                        }
                        is PaymentMethodListUiState.PaymentMethods -> {
                            listAdapter.submitList(uiState.items)
                        }
                    }
                }
            }
        }
    }

    private val paymentMethodItemClickAction: (PaymentMethodUiModel) -> Unit = { item ->
        setFragmentResult(REQUEST_KEY, bundleOf(RES_KEY_SELECTED_ITEM to item))
        dismiss()
    }

    private val editPaymentMethodClickAction: (PaymentMethodUiModel) -> Unit = { item ->
        EditPaymentMethodDialogFragment.newInstance(
            paymentMethodItem = item
        ).show(childFragmentManager, EditPaymentMethodDialogFragment.TAG)
    }

    private val addPaymentMethodClickListener = View.OnClickListener {
        MakePaymentMethodDialogFragment.newInstance()
            .show(childFragmentManager, MakePaymentMethodDialogFragment.TAG)
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        if (requestKey == EditPaymentMethodDialogFragment.REQUEST_KEY) {
            val data =
                result.parcelable<PaymentMethodUiModel>(EditPaymentMethodDialogFragment.RES_KEY_UPDATED_ITEM)
            data?.let {
                setFragmentResult(REQUEST_KEY, bundleOf(RES_KEY_UPDATED_ITEM to data))
            }
        }
    }

    companion object {
        const val TAG = "PaymentMethodListBSDialog"

        const val REQUEST_KEY = "REQUEST_KEY"
        const val RES_KEY_SELECTED_ITEM = "RES_KEY_SELECTED_ITEM"
        const val RES_KEY_UPDATED_ITEM = "RES_KEY_UPDATED_ITEM"

        fun newInstance(
            selectedPaymentMethodItem: PaymentMethodUiModel?
        ): PaymentMethodListBSDialog = PaymentMethodListBSDialog().apply {
            arguments = Bundle().apply {
                putParcelable(AppConstants.INTENT_PAYMENT_METHOD_ITEM, selectedPaymentMethodItem)
            }
        }
    }
}