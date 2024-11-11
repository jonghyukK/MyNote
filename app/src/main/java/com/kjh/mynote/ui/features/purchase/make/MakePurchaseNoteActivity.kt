package com.kjh.mynote.ui.features.purchase.make

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.View.OnClickListener
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.datepicker.MaterialDatePicker
import com.kjh.mynote.R
import com.kjh.mynote.databinding.ActivityMakePurchaseNoteBinding
import com.kjh.mynote.model.KakaoPlaceUiModel
import com.kjh.mynote.model.UiState
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.features.map.NaverMapActivity
import com.kjh.mynote.ui.features.place.make.adapter.TempImageListAdapter
import com.kjh.mynote.ui.features.category.list.CategoryListBSDialog
import com.kjh.mynote.utils.DatePickerManager
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.hideKeyboard
import com.kjh.mynote.utils.extensions.parcelable
import com.kjh.mynote.utils.extensions.registerStartActivityResultLauncher
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.showToast
import com.kjh.mynote.utils.extensions.toComma
import com.kjh.mynote.utils.extensions.toLocalDate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.ZoneOffset

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 7..
 * Description:
 */

@AndroidEntryPoint
class MakePurchaseNoteActivity: BaseActivity<ActivityMakePurchaseNoteBinding>({ ActivityMakePurchaseNoteBinding.inflate(it) }) {

    private val viewModel: MakePurchaseNoteViewModel by viewModels()

    private val tempImageListAdapter: TempImageListAdapter by lazy {
        TempImageListAdapter(
            deleteImageClickAction = deleteTempImageClickAction,
            tempImageClickAction = tempImageClickAction
        )
    }

    override fun onInitView() {
        with (binding) {
            rvTempImages.apply {
                adapter = tempImageListAdapter
            }

            clAttachImages.setOnThrottleClickListener(photoAttachClickListener)
            clPurchasePlaceContainer.setOnThrottleClickListener(searchMapClickListener)
            clPurchaseDateContainer.setOnThrottleClickListener(purchaseDateClickListener)
            etPrice.addTextChangedListener(priceTextWatcher)
            etPurchaseName.addTextChangedListener(purchaseNameTextWatcher)
            clCategoryContainer.setOnThrottleClickListener(categoryClickListener)
            btnSave.setOnThrottleClickListener(saveBtnClickListener)
        }
    }

    override fun onInitUiData() {
        val selectedPurchaseDate = intent.getLongExtra(AppConstants.INTENT_PURCHASE_DATE, -1)
        if (selectedPurchaseDate > 0) {
            viewModel.setPurchaseDate(selectedPurchaseDate)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
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
                        .map { it.tempPlaceItem }
                        .distinctUntilChanged()
                        .collect { tempPlaceItem ->
                            with (binding.tvPurchasePlace) {
                                if (tempPlaceItem == null) {
                                    text = getString(R.string.search_purchase_place)
                                    setTextColor(getColor(R.color.black_400))
                                } else {
                                    text = tempPlaceItem.placeName
                                    setTextColor(getColor(R.color.black_900))
                                }
                            }
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.purchaseDateText }
                        .distinctUntilChanged()
                        .collect { purchaseDateText ->
                            with (binding.tvPurchaseDate) {
                                if (purchaseDateText.isBlank()) {
                                    text = getString(R.string.select_purchase_date)
                                    setTextColor(getColor(R.color.black_400))
                                } else {
                                    text = purchaseDateText
                                    setTextColor(getColor(R.color.black_900))
                                }
                            }
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.categoryItem }
                        .distinctUntilChanged()
                        .collect { categoryItem ->
                            binding.tvCategoryName.text = categoryItem?.categoryName ?: ""
                        }
                }

                launch {
                    viewModel.saveValidateFlow.collectLatest { isValid ->
                        binding.btnSave.isEnable = isValid
                    }
                }

                launch {
                    viewModel.makePurchaseNoteEventState.collectLatest { event ->
                        when (event) {
                            is UiState.Init -> {}
                            is UiState.Loading -> {
                                binding.btnSave.isLoading = true
                            }
                            is UiState.Error -> {
                                binding.btnSave.isLoading = false
                                showToast(event.errorMsg)
                            }
                            is UiState.Success -> {
                                binding.btnSave.isLoading = false
                                Intent().apply {
                                    putExtra(AppConstants.INTENT_PURCHASE_NOTE_ITEM, event.data)
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

    override fun onDestroy() {
        binding.etPrice.removeTextChangedListener(priceTextWatcher)
        binding.etPurchaseName.removeTextChangedListener(purchaseNameTextWatcher)
        super.onDestroy()
    }

    private fun clearFocus() {
        binding.etPrice.hideKeyboard()
        binding.etPurchaseName.hideKeyboard()
    }

    private fun showDatePicker(positiveBtnClickAction: (Long) -> Unit) {
        val selection = if (viewModel.getVisitDateTimeMills() > 0) {
            viewModel.getVisitDateTimeMills().toLocalDate()
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

    private val priceTextWatcher = object: TextWatcher {
        private var current = ""

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            if (s.toString() != current) {
                binding.etPrice.removeTextChangedListener(this)

                val cleanString = s.toString().replace(",", "")
                if (cleanString.isNotEmpty()) {
                    viewModel.setPurchasePrice(cleanString)

                    val formatted = cleanString.toLong().toComma()
                    current = formatted
                    binding.etPrice.setText(formatted)
                    binding.etPrice.setSelection(formatted.length)
                } else {
                    viewModel.clearPurchasePrice()
                }

                binding.etPrice.addTextChangedListener(this)
            }
        }
    }

    private val purchaseNameTextWatcher = object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            viewModel.setPurchaseName(s.toString())
        }
    }

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

    private val searchPlaceResultLauncher = registerStartActivityResultLauncher(
        resultOkBlock = { result ->
            val placeItem =
                result.data?.parcelable<KakaoPlaceUiModel>(AppConstants.INTENT_TEMP_PLACE_ITEM)
                    ?: return@registerStartActivityResultLauncher

            viewModel.setTempPlaceItem(placeItem)
        })

    private val photoAttachClickListener = OnClickListener {
        clearFocus()

        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        multiPhotoPickerLauncher.launch(intent)
    }

    private val searchMapClickListener = OnClickListener {
        clearFocus()

        val intent = Intent(this@MakePurchaseNoteActivity, NaverMapActivity::class.java).apply {
            putExtra(AppConstants.INTENT_TEMP_PLACE_ITEM, viewModel.getTempPlaceItem())
        }
        searchPlaceResultLauncher.launch(intent)
    }

    private val purchaseDateClickListener = OnClickListener {
        clearFocus()

        showDatePicker(positiveBtnClickAction = { timeInMillis ->
            viewModel.setPurchaseDate(timeInMillis)
        })
    }

    private val categoryClickListener = OnClickListener {
        clearFocus()

        CategoryListBSDialog.newInstance()
            .show(supportFragmentManager, CategoryListBSDialog.TAG)
    }

    private val saveBtnClickListener = OnClickListener {
        clearFocus()

        if (binding.btnSave.isEnable) {
            viewModel.makePurchaseNote()
        }
    }

    private val deleteTempImageClickAction: (String) -> Unit = { uri ->
        clearFocus()

        viewModel.deleteTempImageByUrl(uri)
    }

    private val tempImageClickAction: (String) -> Unit = {
        showToast("개발 예정..")
    }

}