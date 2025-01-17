package com.kjh.mynote.ui.features.paymentmethod

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.R
import com.kjh.mynote.databinding.BsdCategoriesOrPaymentMethodsListBinding
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.ui.base.BaseBottomSheetDialogFragment
import com.kjh.mynote.ui.features.mypage.paymentmethod.manage.PaymentMethodManageActivity
import com.kjh.mynote.ui.features.paymentmethod.adapter.PaymentMethodListAdapter
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
class PaymentMethodListBSDialog : BaseBottomSheetDialogFragment<BsdCategoriesOrPaymentMethodsListBinding>({
    BsdCategoriesOrPaymentMethodsListBinding.inflate(it)
}) {

    private val viewModel: PaymentMethodListViewModel by viewModels()

    private val listAdapter: PaymentMethodListAdapter by lazy {
        PaymentMethodListAdapter(paymentMethodItemClickAction)
    }

    override fun onInitView() {
        with (binding) {
            tvTitle.text = getString(R.string.payment_method_list)

            rvItems.apply {
                itemAnimator = null
                adapter = listAdapter
            }

            tvManage.setOnThrottleClickListener(manageBtnClickListener)
        }
    }

    override fun onInitData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { uiState ->
                        when (uiState) {
                            is PaymentMethodListUiState.Loading -> {}
                            is PaymentMethodListUiState.Error -> {
                                showToast(uiState.errorMsg)
                            }
                            is PaymentMethodListUiState.Success -> {
                                listAdapter.submitList(uiState.items)
                            }
                        }
                    }
                }

                launch {
                    viewModel.selectedPaymentMethodItem.collect { selectedItem ->
                        setFragmentResult(REQUEST_KEY,
                            bundleOf(BUNDLE_KEY_SELECTED_PAYMENT_METHOD to selectedItem))
                    }
                }
            }
        }
    }

    private val paymentMethodItemClickAction: (PaymentMethodUiModel) -> Unit = { item ->
        setFragmentResult(REQUEST_KEY, bundleOf(BUNDLE_KEY_SELECTED_PAYMENT_METHOD to item))
        dismiss()
    }

    private val manageBtnClickListener = View.OnClickListener {
        Intent(requireContext(), PaymentMethodManageActivity::class.java).apply {
            startActivity(this)
        }
    }

    companion object {
        const val TAG = "PaymentMethodListBSDialog"

        const val ARG_OBJ_SELECTED_PAYMENT_METHOD_ITEM = "selectedPaymentMethodItem"

        const val REQUEST_KEY = "PaymentMethodListBSDialog_request_key"
        const val BUNDLE_KEY_SELECTED_PAYMENT_METHOD = "bundle_key_selected_payment_method"

        fun newInstance(
            selectedPaymentMethodItem: PaymentMethodUiModel?
        ) = PaymentMethodListBSDialog().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_OBJ_SELECTED_PAYMENT_METHOD_ITEM, selectedPaymentMethodItem)
            }
        }
    }
}