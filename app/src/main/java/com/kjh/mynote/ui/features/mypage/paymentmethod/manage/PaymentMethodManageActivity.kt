package com.kjh.mynote.ui.features.mypage.paymentmethod.manage

import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.databinding.ActivityPaymentMethodManageBinding
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.features.mypage.paymentmethod.manage.adapter.PaymentMethodManageListAdapter
import com.kjh.mynote.utils.decorations.UnderLineDecoration
import com.kjh.mynote.utils.extensions.makeGone
import com.kjh.mynote.utils.extensions.makeVisible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 31..
 * Description:
 */

@AndroidEntryPoint
class PaymentMethodManageActivity: BaseActivity<ActivityPaymentMethodManageBinding>({ ActivityPaymentMethodManageBinding.inflate(it) }) {

    private val viewModel: PaymentMethodManageViewModel by viewModels()

    private val listAdapter: PaymentMethodManageListAdapter by lazy {
        PaymentMethodManageListAdapter(
            editClickAction = editClickAction,
            deleteClickAction = deleteClickAction
        )
    }

    override fun onInitView() {
        with (binding) {
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
                viewModel.uiState.collect { uiState ->
                    when (uiState) {
                        is PaymentMethodManageUiState.Loading -> {
                            binding.layoutLoading.root.makeVisible()
                        }
                        is PaymentMethodManageUiState.Error -> {
                            binding.layoutLoading.root.makeGone()
                        }
                        is PaymentMethodManageUiState.PaymentMethods -> {
                            binding.layoutLoading.root.makeGone()
                            listAdapter.submitList(uiState.items)
                        }
                    }
                }
            }
        }
    }

    private val editClickAction: (PaymentMethodUiModel) -> Unit = { item ->

    }

    private val deleteClickAction: (PaymentMethodUiModel) -> Unit = { item ->

    }

    private val addClickListener = View.OnClickListener {

    }
}