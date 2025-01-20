package com.kjh.mynote.ui.features.mypage.paymentmethod.manage

import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.R
import com.kjh.mynote.databinding.ActivityPaymentMethodManageBinding
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.common.dialog.DefaultDialog
import com.kjh.mynote.ui.features.mypage.paymentmethod.edit.EditPaymentMethodDialogFragment
import com.kjh.mynote.ui.features.mypage.paymentmethod.make.MakePaymentMethodDialogFragment
import com.kjh.mynote.ui.features.mypage.paymentmethod.manage.adapter.PaymentMethodManageListAdapter
import com.kjh.mynote.utils.decorations.UnderLineDecoration
import com.kjh.mynote.utils.extensions.makeGone
import com.kjh.mynote.utils.extensions.makeVisible
import com.kjh.mynote.utils.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 31..
 * Description:
 */

@AndroidEntryPoint
class PaymentMethodManageActivity :
    BaseActivity<ActivityPaymentMethodManageBinding>({ ActivityPaymentMethodManageBinding.inflate(it) }), DefaultDialog.MyDefaultDialogEventListener {

    private val viewModel: PaymentMethodManageViewModel by viewModels()
    private var tempDeleteItemId: Int? = null

    private val listAdapter: PaymentMethodManageListAdapter by lazy {
        PaymentMethodManageListAdapter(
            editClickAction = editClickAction,
            deleteClickAction = deleteClickAction
        )
    }

    override fun onInitView() {
        with(binding) {
            rvPaymentMethods.apply {
                itemAnimator = null
                addItemDecoration(UnderLineDecoration(this@PaymentMethodManageActivity, height = 1))
                adapter = listAdapter
            }

            tbToolbar.setRightFirstButtonClickListener(addClickListener)
        }
    }

    override fun onInitUiData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { uiState ->
                        when (uiState) {
                            is PaymentMethodManageUiState.Loading -> {
                                binding.layoutLoading.root.makeVisible()
                            }

                            is PaymentMethodManageUiState.Error -> {
                                binding.layoutLoading.root.makeGone()
                                uiState.errorMsg?.let {
                                    showToast(it)
                                    viewModel.shownFetchError()
                                }
                            }

                            is PaymentMethodManageUiState.PaymentMethods -> {
                                binding.layoutLoading.root.makeGone()
                                listAdapter.submitList(uiState.items)
                            }
                        }
                    }
                }

                launch {
                    viewModel.deletePaymentMethodEvent.collect { event ->
                        when (event) {
                            is DeletePaymentMethodEventState.Loading -> {
                                binding.layoutLoading.root.makeVisible()
                            }
                            is DeletePaymentMethodEventState.Error -> {
                                binding.layoutLoading.root.makeGone()
                                showToast(event.errorMsg)
                            }
                            is DeletePaymentMethodEventState.Success -> {
                                binding.layoutLoading.root.makeGone()
                                tempDeleteItemId = null
                            }
                        }
                    }
                }
            }
        }
    }

    private val editClickAction: (PaymentMethodUiModel) -> Unit = { item ->
        EditPaymentMethodDialogFragment.newInstance(
            paymentMethodItem = item
        ).show(supportFragmentManager, EditPaymentMethodDialogFragment.TAG)
    }

    private val deleteClickAction: (PaymentMethodUiModel) -> Unit = { item ->
        tempDeleteItemId = item.paymentMethodId

        DefaultDialog.newInstance(
            title = getString(R.string.will_you_delete),
            desc = getString(R.string.desc_payment_method_remove),
            descColorRes = R.color.red_500,
            posBtnText = getString(R.string.yes_i_will_delete),
            negBtnText = getString(R.string.cancel)
        )
            .show(supportFragmentManager, DefaultDialog.TAG)
    }

    private val addClickListener = View.OnClickListener {
        MakePaymentMethodDialogFragment.newInstance()
            .show(supportFragmentManager, MakePaymentMethodDialogFragment.TAG)
    }

    override fun onDialogPositiveClick() {
        tempDeleteItemId?.let {
            viewModel.deletePaymentMethod(it)
        }
    }

    override fun onDialogNegativeClick() {
        tempDeleteItemId = null
    }

    override fun onDialogDismiss() {
        tempDeleteItemId = null
    }
}