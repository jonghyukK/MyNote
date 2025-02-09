package com.kjh.mynote.ui.features.purchase.makeedit

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View.OnClickListener
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.datepicker.MaterialDatePicker
import com.kjh.mynote.R
import com.kjh.mynote.databinding.ActivityEditOrMakePurchaseNoteBinding
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.model.PlaceInfoUiModel
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.features.category.list.CategoryListBSDialog
import com.kjh.mynote.ui.features.map.NaverMapSearchActivity
import com.kjh.mynote.ui.features.paymentmethod.PaymentMethodListBSDialog
import com.kjh.mynote.ui.features.place.make.adapter.TempImageListAdapter
import com.kjh.mynote.ui.features.purchase.makeedit.adapter.RecentRegisteredPurchaseNameListAdapter
import com.kjh.mynote.utils.DatePickerManager
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.decorations.SpacingItemDecoration
import com.kjh.mynote.utils.extensions.parcelable
import com.kjh.mynote.utils.extensions.registerStartActivityResultLauncher
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.showToast
import com.kjh.mynote.utils.extensions.toComma
import com.kjh.mynote.utils.extensions.toLocalDate
import com.kjh.mynote.utils.extensions.toStringWithFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.ZoneOffset

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 7..
 * Description:
 */

@AndroidEntryPoint
class MakeEditPurchaseNoteActivity : BaseActivity<ActivityEditOrMakePurchaseNoteBinding>({
    ActivityEditOrMakePurchaseNoteBinding.inflate(it)
}) {

    private val viewModel: MakeEditPurchaseNoteViewModel by viewModels()

    private val tempImageListAdapter: TempImageListAdapter by lazy {
        TempImageListAdapter(deleteTempImageClickAction)
    }

    private val recentPurchaseNameListAdapter: RecentRegisteredPurchaseNameListAdapter by lazy {
        RecentRegisteredPurchaseNameListAdapter(recentPurchaseNameClickAction)
    }

    override fun onInitView() {
        with (binding) {
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
            tvPurchaseDate.setTextClickListener(purchaseDateClickListener)
            tvPurchasePlace.setTextClickListener(searchMapClickListener)

            clRegisterDefaultPaymentMethod.setOnThrottleClickListener(defaultPaymentChangeCheckBoxClickListener)
            clAttachImages.setOnThrottleClickListener(photoAttachClickListener)
            btnBottom.setOnThrottleClickListener(saveBtnClickListener)
        }

        supportFragmentManager.setFragmentResultListener(
            CategoryListBSDialog.REQUEST_KEY, this, handleCategorySelectionResult)

        supportFragmentManager.setFragmentResultListener(
            PaymentMethodListBSDialog.REQUEST_KEY, this, handlePaymentMethodSelectionResult)
    }

    override fun onInitUiData() {
        viewModel.initialize()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.errorMessage.collect {
                        showToast(it)
                    }
                }

                launch {
                    viewModel.uiState
                        .map { it.isLoading }
                        .distinctUntilChanged()
                        .collect { isLoading ->
                            binding.layoutLoading.root.isVisible = isLoading
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.viewType }
                        .distinctUntilChanged()
                        .collect { viewType ->
                            when (viewType) {
                                ViewType.MAKE -> {
                                    binding.tbToolbar.leftTitle = getString(R.string.make_purchase_note)
                                    binding.btnBottom.btnTitle = getString(R.string.do_save)
                                }
                                ViewType.EDIT -> {
                                    binding.tbToolbar.leftTitle = getString(R.string.purchase_note_edit)
                                    binding.btnBottom.btnTitle = getString(R.string.do_modify)
                                }
                            }
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.categoryItem }
                        .distinctUntilChanged()
                        .collect { categoryItem ->
                            if (categoryItem == null) {
                                binding.tvCategory.text = getString(R.string.select_category)
                                binding.tvCategory.textColor = R.color.black_400
                            } else {
                                binding.tvCategory.text = categoryItem.categoryName
                                binding.tvCategory.textColor = R.color.black_800
                            }
                        }
                }

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
                        .map { it.recentPurchaseNameItems }
                        .distinctUntilChanged()
                        .collect { recentPurchaseNames ->
                            binding.rvRecentPurchaseNames.isVisible = recentPurchaseNames.isNotEmpty()
                            recentPurchaseNameListAdapter.submitList(recentPurchaseNames)
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.purchasePrice }
                        .distinctUntilChanged()
                        .collect { price ->
                            binding.etPurchasePrice.text = if (price == 0L) "" else price.toString()
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.purchaseDate }
                        .distinctUntilChanged()
                        .collect { purchaseDate ->
                            if (purchaseDate == null) {
                                binding.tvPurchaseDate.text = getString(R.string.select_purchase_date)
                                binding.tvPurchaseDate.textColor = R.color.black_400
                            } else {
                                binding.tvPurchaseDate.text =
                                    purchaseDate.toStringWithFormat("yyyy년 M월 d일 (E)")
                                binding.tvPurchaseDate.textColor = R.color.black_900
                            }
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.paymentMethodItem }
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
                        .collect { showDefaultPaymentCheckBox ->
                            binding.clRegisterDefaultPaymentMethod.isVisible = showDefaultPaymentCheckBox
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.tempPlaceItem }
                        .distinctUntilChanged()
                        .collect { tempPlaceItem ->
                            if (tempPlaceItem == null) {
                                binding.tvPurchasePlace.text = getString(R.string.search_purchase_place)
                                binding.tvPurchasePlace.textColor = R.color.black_400
                            } else {
                                binding.tvPurchasePlace.text = tempPlaceItem.placeName
                                binding.tvPurchasePlace.textColor = R.color.black_900
                            }
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
                    viewModel.uiState
                        .map { it.canSave }
                        .distinctUntilChanged()
                        .collect { canSave ->
                            binding.btnBottom.isEnable = canSave
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.makeEditResult }
                        .distinctUntilChanged()
                        .filterNotNull()
                        .collect { result ->
                            Intent().apply {
                                putExtra(AppConstants.INTENT_PURCHASE_NOTE_ITEM, result)
                                setResult(RESULT_OK, this)
                                finish()
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

                if (tempImages.isNotEmpty()) {
                    viewModel.setTempImageUrls(tempImages)
                }
            }
        }
    )

    private val recentPurchaseNameClickAction: (String) -> Unit = { recentPurchaseName ->
        binding.etPurchaseName.text = recentPurchaseName
    }

    private val deleteTempImageClickAction: (String) -> Unit = { url ->
        viewModel.deleteTempImageByUrl(url)
    }

    private val categoryClickListener = OnClickListener {
        CategoryListBSDialog.newInstance(
            selectedCategoryItem = viewModel.uiState.value.categoryItem
        ).show(supportFragmentManager, CategoryListBSDialog.TAG)
    }

    private val paymentMethodClickListener = OnClickListener {
        PaymentMethodListBSDialog.newInstance(
            selectedPaymentMethodItem = viewModel.uiState.value.paymentMethodItem
        ).show(supportFragmentManager, PaymentMethodListBSDialog.TAG)
    }

    private val defaultPaymentChangeCheckBoxClickListener = OnClickListener {
        viewModel.toggleDefaultPaymentMethodChecked()
    }

    private val purchaseDateClickListener = OnClickListener {
        val currentDateMillis = viewModel.uiState.value.purchaseDate
        val selection =
            currentDateMillis?.toLocalDate()?.atStartOfDay(ZoneOffset.UTC)?.toInstant()
                ?.toEpochMilli()
                ?: MaterialDatePicker.todayInUtcMilliseconds()

        DatePickerManager.build(
            title = getString(R.string.select_purchase_date),
            selection = selection,
            positiveButtonClickAction = { timeInMillis ->
                viewModel.setPurchaseDate(timeInMillis)
            }
        ).show(supportFragmentManager, "DATE_PICKER")
    }

    private val searchMapClickListener = OnClickListener {
        val tempPlaceItem = viewModel.uiState.value.tempPlaceItem
        val intent = Intent(this@MakeEditPurchaseNoteActivity, NaverMapSearchActivity::class.java).apply {
            putExtra(AppConstants.INTENT_TEMP_PLACE_ITEM, tempPlaceItem)
        }

        searchPlaceResultLauncher.launch(intent)
    }

    private val photoAttachClickListener = OnClickListener {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        multiPhotoPickerLauncher.launch(intent)
    }

    private val saveBtnClickListener = OnClickListener {
        if (binding.btnBottom.isEnable) {
            viewModel.makeOrEditPurchaseNote()
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
}