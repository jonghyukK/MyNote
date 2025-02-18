package com.kjh.mynote.ui.features.place.make

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.R
import com.kjh.mynote.databinding.DialogFragmentAddPurchaseNoteBinding
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.ui.base.BaseDialogFragment
import com.kjh.mynote.ui.base.DialogType
import com.kjh.mynote.ui.common.bsdialog.categorylist.CategoryListBSDialog
import com.kjh.mynote.ui.common.bsdialog.paymentmethodlist.PaymentMethodListBSDialog
import com.kjh.mynote.ui.features.place.make.adapter.TempImageListAdapter
import com.kjh.mynote.ui.features.purchase.makeedit.adapter.RecentRegisteredPurchaseNameListAdapter
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.decorations.SpacingItemDecoration
import com.kjh.mynote.utils.extensions.parcelable
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.showToast
import com.kjh.mynote.utils.extensions.toComma
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 4..
 * Description:
 */

@AndroidEntryPoint
class AddPurchaseNoteDialogFragment : BaseDialogFragment<DialogFragmentAddPurchaseNoteBinding>({
    DialogFragmentAddPurchaseNoteBinding.inflate(it)
}, DialogType.FULL_SCREEN) {

    private val viewModel: AddPurchaseNoteDialogViewModel by viewModels()

    private val tempImageListAdapter: TempImageListAdapter by lazy {
        TempImageListAdapter(deleteTempImageClickAction)
    }

    private val recentPurchaseNameListAdapter: RecentRegisteredPurchaseNameListAdapter by lazy {
        RecentRegisteredPurchaseNameListAdapter(recentPurchaseNameClickAction)
    }

    override fun onInitView() {
        with(binding) {
            rvTempImages.apply {
                adapter = tempImageListAdapter
            }

            rvRecentPurchaseNames.apply {
                itemAnimator = null
                addItemDecoration(SpacingItemDecoration(right = 6, exceptFirstItem = false))
                adapter = recentPurchaseNameListAdapter
            }

            etPurchaseName.addMyTextWatcher(purchaseNameTextWatcher)
            etPurchasePrice.addMyTextWatcher(priceTextWatcher)

            tvCategory.setTextClickListener(categoryClickListener)
            tvPaymentMethod.setTextClickListener(paymentMethodClickListener)

            tbToolbar.setRightFirstButtonClickListener(closeClickListener)
            clRegisterDefaultPaymentMethod.setOnThrottleClickListener(defaultPaymentCheckBoxClickListener)
            clAttachImages.setOnThrottleClickListener(photoAttachClickListener)
            btnBottom.setOnThrottleClickListener(bottomBtnClickListener)
        }

        childFragmentManager.setFragmentResultListener(
            CategoryListBSDialog.REQUEST_KEY, this, handleCategorySelectionResult)

        childFragmentManager.setFragmentResultListener(
            PaymentMethodListBSDialog.REQUEST_KEY, this, handlePaymentMethodSelectionResult)
    }

    override fun onInitData() {
        viewModel.initialize()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.errorMessage.collectLatest {
                        showToast(it)
                    }
                }

                launch {
                    viewModel.uiState
                        .map { it.titleRes }
                        .distinctUntilChanged()
                        .collect {
                            binding.tbToolbar.leftTitle = getString(it)
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.bottomBtnTextRes }
                        .distinctUntilChanged()
                        .collect {
                            binding.btnBottom.btnTitle = getString(it)
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.tempPurchaseNoteItem.purchaseName }
                        .distinctUntilChanged()
                        .collect { purchaseName ->
                            if (binding.etPurchaseName.text != purchaseName) {
                                binding.etPurchaseName.text = purchaseName
                            }
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.tempPurchaseNoteItem.purchasePrice }
                        .distinctUntilChanged()
                        .collect { price ->
                            if (price > 0 && binding.etPurchasePrice.text != price.toString()) {
                                binding.etPurchasePrice.text = price.toString()
                            } else {
                                binding.etPurchasePrice.text = ""
                            }
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.tempPurchaseNoteItem.categoryItem }
                        .distinctUntilChanged()
                        .collect { categoryItem ->
                            categoryItem?.let {
                                binding.tvCategory.text = it.categoryName
                                binding.tvCategory.textColor = R.color.black_800
                            } ?: run {
                                binding.tvCategory.text = getString(R.string.select_category)
                                binding.tvCategory.textColor = R.color.black_400
                            }
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.tempPurchaseNoteItem.paymentMethod }
                        .distinctUntilChanged()
                        .collect { paymentMethodItem ->
                            if (paymentMethodItem == null) {
                                binding.tvPaymentMethod.text = getString(R.string.select_payment_method)
                                binding.tvPaymentMethod.textColor = R.color.black_400
                            } else {
                                binding.tvPaymentMethod.text = paymentMethodItem.paymentMethodName
                                binding.tvPaymentMethod.textColor = R.color.black_800
                            }
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.shouldShowDefaultPaymentCheckBox }
                        .distinctUntilChanged()
                        .collect { isVisible ->
                            binding.clRegisterDefaultPaymentMethod.isVisible = isVisible
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.isDefaultPaymentCheckBoxChecked }
                        .distinctUntilChanged()
                        .collect { isChecked ->
                            val checkboxRes = if (isChecked)
                                R.drawable.ic_selected_checkbox
                            else
                                R.drawable.ic_unselected_checkbox

                            binding.ivCheckbox.setImageResource(checkboxRes)
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.tempPurchaseNoteItem.tempImageUrls }
                        .distinctUntilChanged()
                        .collect { tempImages ->
                            binding.tvImageCount.text = getString(
                                R.string.format_slash,
                                tempImages.size,
                                AppConstants.MAX_SELECTABLE_IMAGE_COUNT
                            )
                            tempImageListAdapter.submitList(tempImages)
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.recentPurchaseNames }
                        .distinctUntilChanged()
                        .collectLatest { recentPurchaseNames ->
                            binding.rvRecentPurchaseNames.isVisible = recentPurchaseNames.isNotEmpty()
                            recentPurchaseNameListAdapter.submitList(recentPurchaseNames)
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.tempPurchaseNoteItem.isFulfillRequired }
                        .distinctUntilChanged()
                        .collectLatest { isValid ->
                            binding.btnBottom.isEnable = isValid
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.isUpdatedDefaultPaymentMethod }
                        .collectLatest { isUpdated ->
                            if (isUpdated) {
                                setTempPurchaseNoteResultAndDismiss()
                            }
                        }
                }
            }
        }
    }

    private fun clearFocus() {
        binding.etPurchaseName.hideKeyboard()
        binding.etPurchasePrice.hideKeyboard()
    }

    private fun setTempPurchaseNoteResultAndDismiss() {
        val tempPurchaseNoteItem = viewModel.uiState.value.tempPurchaseNoteItem

        setFragmentResult(
            REQUEST_KEY,
            bundleOf(BUNDLE_KEY_ADDED_TEMP_PURCHASE_NOTE to tempPurchaseNoteItem)
        )

        dismiss()
    }

    private val purchaseNameTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            viewModel.setPurchaseName(s.toString())
        }
    }

    private val priceTextWatcher = object : TextWatcher {
        private var current = ""

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            if (s.toString().isEmpty()) {
                current = ""
                viewModel.clearPurchasePrice()
                return
            }

            if (s.toString() != current) {
                binding.etPurchasePrice.removeMyTextWatcher()

                val cleanString = s.toString().replace(",", "")
                if (cleanString.isNotEmpty()) {
                    viewModel.setPurchasePrice(cleanString)

                    val formatted = cleanString.toLong().toComma()
                    current = formatted
                    binding.etPurchasePrice.text = formatted
                    binding.etPurchasePrice.setSelection(formatted.length)
                } else {
                    viewModel.clearPurchasePrice()
                }

                binding.etPurchasePrice.addMyTextWatcher(this)
            }
        }
    }

    private val multiPhotoPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.clipData?.let { clipData ->
                val tempImages: MutableList<String> = mutableListOf()
                for (i in 0 until clipData.itemCount) {
                    val imageUri = clipData.getItemAt(i).uri

                    requireContext().contentResolver.takePersistableUriPermission(
                        imageUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                    tempImages.add(imageUri.toString())
                }

                if (tempImages.isNotEmpty()) {
                    viewModel.setTempImages(tempImages)
                }
            }
        }

    private val closeClickListener = View.OnClickListener {
        dismiss()
    }

    private val deleteTempImageClickAction: (String) -> Unit = { uri ->
        clearFocus()
        viewModel.deleteTempImageByUrl(uri)
    }

    private val recentPurchaseNameClickAction: (String) -> Unit = { recentPurchaseName ->
        clearFocus()
        binding.etPurchaseName.text = recentPurchaseName
    }

    private val categoryClickListener = View.OnClickListener {
        clearFocus()
        CategoryListBSDialog.newInstance(
            selectedCategoryItem = viewModel.uiState.value.tempPurchaseNoteItem.categoryItem
        ).show(childFragmentManager, CategoryListBSDialog.TAG)
    }

    private val paymentMethodClickListener = View.OnClickListener {
        clearFocus()
        PaymentMethodListBSDialog.newInstance(
            selectedPaymentMethodItem = viewModel.uiState.value.tempPurchaseNoteItem.paymentMethod
        ).show(childFragmentManager, PaymentMethodListBSDialog.TAG)
    }

    private val defaultPaymentCheckBoxClickListener = View.OnClickListener {
        clearFocus()
        viewModel.toggleDefaultPaymentMethodChecked()
    }

    private val photoAttachClickListener = View.OnClickListener {
        clearFocus()
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        multiPhotoPickerLauncher.launch(intent)
    }

    private val bottomBtnClickListener = View.OnClickListener {
        if (binding.btnBottom.isEnable) {
            clearFocus()

            if (viewModel.uiState.value.isDefaultPaymentCheckBoxChecked) {
                viewModel.updateDefaultPaymentMethod()
                return@OnClickListener
            }

            setTempPurchaseNoteResultAndDismiss()
        }
    }

    private val handleCategorySelectionResult: (String, Bundle) -> Unit = { _, data ->
        val selectedCategoryItem =
            data.parcelable<CategoryUiModel>(CategoryListBSDialog.BUNDLE_KEY_SELECTED_CATEGORY)
        viewModel.setCategory(selectedCategoryItem)
    }

    private val handlePaymentMethodSelectionResult: (String, Bundle) -> Unit = { _, data ->
        val selectedPaymentMethodItem =
            data.parcelable<PaymentMethodUiModel>(PaymentMethodListBSDialog.BUNDLE_KEY_SELECTED_PAYMENT_METHOD)
        viewModel.setPaymentMethod(selectedPaymentMethodItem)
    }

    companion object {
        const val TAG = "AddPurchaseNoteDialogFragment"

        const val ARG_OBJ_PURCHASE_NOTE_ITEM = "ARG_OBJ_PURCHASE_NOTE_ITEM"
        const val REQUEST_KEY = "${TAG}_request_key"

        const val BUNDLE_KEY_ADDED_TEMP_PURCHASE_NOTE =  "bundle_key_added_temp_purchase_note"

        fun newInstance(
            tempPurchaseNoteItem: TempPurchaseNoteItem? = null
        ) = AddPurchaseNoteDialogFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_OBJ_PURCHASE_NOTE_ITEM, tempPurchaseNoteItem)
            }
        }
    }
}