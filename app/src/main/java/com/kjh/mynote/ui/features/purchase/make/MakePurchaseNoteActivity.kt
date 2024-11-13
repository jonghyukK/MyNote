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
import com.kjh.mynote.databinding.ActivityEditOrMakePurchaseNoteBinding
import com.kjh.mynote.model.KakaoPlaceUiModel
import com.kjh.mynote.model.UiState
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.features.category.list.CategoryListBSDialog
import com.kjh.mynote.ui.features.map.NaverMapActivity
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
class MakePurchaseNoteActivity: BaseActivity<ActivityEditOrMakePurchaseNoteBinding>({ ActivityEditOrMakePurchaseNoteBinding.inflate(it) }) {

    private val viewModel: MakePurchaseNoteViewModel by viewModels()

    private val tempImageListAdapter: TempImageListAdapter by lazy {
        TempImageListAdapter(
            deleteImageClickAction = deleteTempImageClickAction,
            tempImageClickAction = tempImageClickAction
        )
    }

    override fun onInitView() {
        with (binding) {
            tbToolbar.leftTitle = getString(R.string.make_purchase_note)
            btnBottom.btnTitle = getString(R.string.do_save)

            rvTempImages.apply {
                adapter = tempImageListAdapter
            }

            etPurchaseName.addMyTextWatcher(purchaseNameTextWatcher)
            etPurchasePrice.addMyTextWatcher(priceTextWatcher)

            tvCategory.setTextClickListener(categoryClickListener)
            tvPurchaseDate.setTextClickListener(purchaseDateClickListener)
            tvPurchasePlace.setTextClickListener(searchMapClickListener)

            clAttachImages.setOnThrottleClickListener(photoAttachClickListener)
            btnBottom.setOnThrottleClickListener(saveBtnClickListener)
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
                        .map { it.purchaseDateText }
                        .distinctUntilChanged()
                        .collect { purchaseDateText ->
                            with (binding.tvPurchaseDate) {
                                if (purchaseDateText.isBlank()) {
                                    text = getString(R.string.select_purchase_date)
                                    textColor = R.color.black_400
                                } else {
                                    text = purchaseDateText
                                    textColor = R.color.black_900
                                }
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
                    viewModel.saveValidateFlow.collectLatest { isValid ->
                        binding.btnBottom.isEnable = isValid
                    }
                }

                launch {
                    viewModel.makePurchaseNoteEventState.collectLatest { event ->
                        when (event) {
                            is UiState.Init -> {}
                            is UiState.Loading -> {
                                binding.btnBottom.isLoading = true
                            }
                            is UiState.Error -> {
                                binding.btnBottom.isLoading = false
                                showToast(event.errorMsg)
                            }
                            is UiState.Success -> {
                                binding.btnBottom.isLoading = false
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

    private fun clearFocus() {
        binding.etPurchaseName.hideKeyboard()
        binding.etPurchasePrice.hideKeyboard()
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
                result.data?.parcelable<KakaoPlaceUiModel>(AppConstants.INTENT_TEMP_PLACE_ITEM)
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
        clearFocus()

        viewModel.deleteTempImageByUrl(uri)
    }

    private val tempImageClickAction: (String) -> Unit = {
        showToast("개발 예정..")
    }

    private val categoryClickListener = OnClickListener {
        clearFocus()

        CategoryListBSDialog.newInstance(
            selectedCategoryItem = viewModel.uiState.value.categoryItem,
            selectCategoryAction = { categoryItem ->
                viewModel.setCategory(categoryItem)
            },
            updateCategoryNameAction = { categoryItem ->
                viewModel.updateSelectedCategoryWhenChanged(categoryItem)
            },
            deleteCategoryAction = { categoryId ->
                viewModel.deleteSelectedCategoryWhenChanged(categoryId)
            }
        ).show(supportFragmentManager, CategoryListBSDialog.TAG)
    }

    private val purchaseDateClickListener = OnClickListener {
        clearFocus()

        showDatePicker(positiveBtnClickAction = { timeInMillis ->
            viewModel.setPurchaseDate(timeInMillis)
        })
    }

    private val searchMapClickListener = OnClickListener {
        clearFocus()

        val intent = Intent(this@MakePurchaseNoteActivity, NaverMapActivity::class.java).apply {
            putExtra(AppConstants.INTENT_TEMP_PLACE_ITEM, viewModel.getTempPlaceItem())
        }
        searchPlaceResultLauncher.launch(intent)
    }

    private val photoAttachClickListener = OnClickListener {
        clearFocus()

        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        multiPhotoPickerLauncher.launch(intent)
    }

    private val saveBtnClickListener = OnClickListener {
        if (binding.btnBottom.isEnable) {
            clearFocus()
            viewModel.makePurchaseNote()
        }
    }
}