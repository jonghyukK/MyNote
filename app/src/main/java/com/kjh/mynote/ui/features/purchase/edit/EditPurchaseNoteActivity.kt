package com.kjh.mynote.ui.features.purchase.edit

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.datepicker.MaterialDatePicker
import com.kjh.mynote.R
import com.kjh.mynote.databinding.ActivityEditOrMakePurchaseNoteBinding
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.model.PlaceInfoUiModel
import com.kjh.mynote.model.UiState
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.features.category.list.CategoryListBSDialog
import com.kjh.mynote.ui.features.map.NaverMapSearchActivity
import com.kjh.mynote.ui.features.paymentmethod.PaymentMethodListBSDialog
import com.kjh.mynote.ui.features.place.make.adapter.TempImageListAdapter
import com.kjh.mynote.utils.DatePickerManager
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.parcelable
import com.kjh.mynote.utils.extensions.registerStartActivityResultLauncher
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.showToast
import com.kjh.mynote.utils.extensions.toComma
import com.kjh.mynote.utils.extensions.toLocalDate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.ZoneOffset

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 13..
 * Description:
 */

@AndroidEntryPoint
class EditPurchaseNoteActivity : BaseActivity<ActivityEditOrMakePurchaseNoteBinding>({
    ActivityEditOrMakePurchaseNoteBinding.inflate(it)
}) {

    private val viewModel: EditPurchaseNoteViewModel by viewModels()

    private val tempImageListAdapter: TempImageListAdapter by lazy {
        TempImageListAdapter(deleteTempImageClickAction)
    }

    override fun onInitView() {
        with (binding) {
            tbToolbar.leftTitle = getString(R.string.purchase_note_edit)
            btnBottom.btnTitle = getString(R.string.do_modify)

            rvTempImages.apply {
                adapter = tempImageListAdapter
            }

            etPurchaseName.addMyTextWatcher(purchaseNameTextWatcher)
            etPurchasePrice.addMyTextWatcher(priceTextWatcher)

            tvCategory.setTextClickListener(categoryClickListener)
            tvPaymentMethod.setTextClickListener(paymentMethodClickListener)
            tvPurchaseDate.setTextClickListener(purchaseDateClickListener)
            tvPurchasePlace.setTextClickListener(searchMapClickListener)

            clAttachImages.setOnThrottleClickListener(photoAttachClickListener)
            btnBottom.setOnThrottleClickListener(editButtonClickListener)
        }

        setPaymentMethodBSDialogFragmentResult()
    }

    override fun onInitUiData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState
                        .map { it.purchaseName }
                        .distinctUntilChanged()
                        .collect { purchaseName ->
                            if (binding.etPurchaseName.text != purchaseName) {
                                binding.etPurchaseName.text = purchaseName
                            }
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.categoryItem }
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
                        .map { it.paymentMethod }
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
                        .map { it.purchaseDateText }
                        .distinctUntilChanged()
                        .collect { purchaseDateText ->
                            if (purchaseDateText.isEmpty()) {
                                binding.tvPurchaseDate.text = getString(R.string.select_purchase_date)
                                binding.tvPurchaseDate.textColor = R.color.black_400
                            } else {
                                binding.tvPurchaseDate.text = purchaseDateText
                                binding.tvPurchaseDate.textColor = R.color.black_900
                            }
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.purchasePrice }
                        .distinctUntilChanged()
                        .collect { price ->
                            if (price == 0L) {
                                binding.etPurchasePrice.text = ""
                            } else {
                                binding.etPurchasePrice.text = price.toString()
                            }
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.tempPlaceItem }
                        .distinctUntilChanged()
                        .collect { tempPlaceItem ->
                            with (binding.tvPurchasePlace) {
                                if (tempPlaceItem == null) {
                                    text = getString(R.string.search_purchase_place)
                                    textColor = R.color.black_400
                                } else {
                                    text = tempPlaceItem.placeName
                                    textColor = R.color.black_900
                                }
                            }
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.tempImageUrls }
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
                    viewModel.editValidateFlow.collect {
                        binding.btnBottom.isEnable = it
                    }
                }

                launch {
                    viewModel.editPurchaseNoteEventState.collect { state ->
                        when (state) {
                            is UiState.Init -> {}
                            is UiState.Loading -> {
                                binding.btnBottom.isLoading = true
                            }
                            is UiState.Error -> {
                                binding.btnBottom.isLoading = false

                                showToast(state.errorMsg)
                            }
                            is UiState.Success -> {
                                binding.btnBottom.isLoading = false

                                Intent().apply {
                                    setResult(RESULT_OK, this)
                                    finish()
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus

            if (v is AppCompatEditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    v.clearFocus()
                    val imm: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun showDatePicker(positiveBtnClickAction: (Long) -> Unit) {
        val selection = if (viewModel.getPurchaseDateTimeMills() > 0) {
            viewModel.getPurchaseDateTimeMills().toLocalDate()
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        } else {
            MaterialDatePicker.todayInUtcMilliseconds()
        }

        DatePickerManager.build(
            title = getString(R.string.select_purchase_date),
            selection = selection,
            positiveButtonClickAction = positiveBtnClickAction
        ).show(supportFragmentManager, "DATE_PICKER")
    }

    private val purchaseNameTextWatcher = object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            viewModel.setPurchaseName(s.toString())
        }
    }

    private val priceTextWatcher = object: TextWatcher {
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

    private val searchPlaceResultLauncher = registerStartActivityResultLauncher(
        resultOkBlock = { result ->
            val placeItem =
                result.data?.parcelable<PlaceInfoUiModel>(AppConstants.INTENT_TEMP_PLACE_ITEM)
                    ?: return@registerStartActivityResultLauncher

            viewModel.setTempPlaceItem(placeItem)
        })

    private val multiPhotoPickerLauncher = registerStartActivityResultLauncher(
        resultOkBlock = { result ->
            result.data?.clipData?.let { clipData ->
                val tempImages: MutableList<String> = mutableListOf()
                for (i in 0 until clipData.itemCount) {
                    val imageUri = clipData.getItemAt(i).uri

                    contentResolver.takePersistableUriPermission(
                        imageUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                    tempImages.add(imageUri.toString())
                }

                viewModel.setTempImages(tempImages)
            }
        }
    )

    private val deleteTempImageClickAction: (String) -> Unit = { uri ->
        viewModel.deleteTempImageByUrl(uri)
    }

    private val categoryClickListener = View.OnClickListener {
        CategoryListBSDialog.newInstance(
            selectedCategoryItem = viewModel.uiState.value.categoryItem,
            selectCategoryAction = { categoryItem ->
                viewModel.setCategoryItem(categoryItem)
            },
            updateCategoryNameAction = { categoryItem ->
                viewModel.updateSelectedCategoryWhenChanged(categoryItem)
            },
            deleteCategoryAction = { categoryId ->
                viewModel.deleteSelectedCategoryWhenChanged(categoryId)
            }
        ).show(supportFragmentManager, CategoryListBSDialog.TAG)
    }

    private val paymentMethodClickListener = OnClickListener {
        PaymentMethodListBSDialog.newInstance(
            selectedPaymentMethodItem = viewModel.uiState.value.paymentMethod
        ).show(supportFragmentManager, PaymentMethodListBSDialog.TAG)
    }

    private val purchaseDateClickListener = View.OnClickListener {
        showDatePicker(positiveBtnClickAction = { timeInMillis ->
            viewModel.setPurchaseDate(timeInMillis)
        })
    }

    private val searchMapClickListener = View.OnClickListener {
        val intent = Intent(this, NaverMapSearchActivity::class.java).apply {
            putExtra(AppConstants.INTENT_TEMP_PLACE_ITEM, viewModel.getTempPlaceItem())
        }
        searchPlaceResultLauncher.launch(intent)
    }

    private val photoAttachClickListener = View.OnClickListener {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        multiPhotoPickerLauncher.launch(intent)
    }

    private val editButtonClickListener = OnClickListener {
        if (binding.btnBottom.isEnable) {
            viewModel.requestEditPurchaseNote()
        }
    }

    private fun setPaymentMethodBSDialogFragmentResult() {
        supportFragmentManager.setFragmentResultListener(
            PaymentMethodListBSDialog.REQUEST_KEY, this
        ) { _, result ->
            val selectedItem =
                result.parcelable<PaymentMethodUiModel>(PaymentMethodListBSDialog.RES_KEY_SELECTED_ITEM)
            selectedItem?.let {
                viewModel.setPaymentMethod(selectedItem)
            }

            val updatedItem =
                result.parcelable<PaymentMethodUiModel>(PaymentMethodListBSDialog.RES_KEY_UPDATED_ITEM)
            updatedItem?.let {
                viewModel.updateSelectedPaymentNameWhenChanged(updatedItem)
            }
        }
    }
}