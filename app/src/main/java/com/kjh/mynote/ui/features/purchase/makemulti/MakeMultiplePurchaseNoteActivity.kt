package com.kjh.mynote.ui.features.purchase.makemulti

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.datepicker.MaterialDatePicker
import com.kjh.mynote.R
import com.kjh.mynote.databinding.ActivityMakeMultiplePurchaseNoteBinding
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.model.PlaceInfoUiModel
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.common.bsdialog.categorylist.CategoryListBSDialog
import com.kjh.mynote.ui.features.map.NaverMapSearchActivity
import com.kjh.mynote.ui.common.bsdialog.paymentmethodlist.PaymentMethodListBSDialog
import com.kjh.mynote.ui.features.purchase.makemulti.adapter.MakeMultiplePurchaseNoteListAdapter
import com.kjh.mynote.utils.DatePickerManager
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.decorations.UnderLineDecoration
import com.kjh.mynote.utils.extensions.parcelable
import com.kjh.mynote.utils.extensions.registerStartActivityResultLauncher
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.showToast
import com.kjh.mynote.utils.extensions.toLocalDate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.ZoneOffset

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 3..
 * Description:
 */

@AndroidEntryPoint
class MakeMultiplePurchaseNoteActivity: BaseActivity<ActivityMakeMultiplePurchaseNoteBinding>({ ActivityMakeMultiplePurchaseNoteBinding.inflate(it) }) {

    private val viewModel: MakeMultiplePurchaseNoteViewModel by viewModels()

    private val listAdapter: MakeMultiplePurchaseNoteListAdapter by lazy {
        MakeMultiplePurchaseNoteListAdapter(
            addNextItemAction = addNextItemClickAction,
            deleteItemAction = deleteItemClickAction,
            categoryClickAction = categoryClickAction,
            purchaseNameInputAction = purchaseNameInputAction,
            recentPurchaseNameClickAction = recentPurchaseNameClickAction,
            purchasePriceInputAction = purchasePriceInputAction,
            purchaseDateClickAction = purchaseDateClickAction,
            paymentMethodClickAction = paymentMethodClickAction,
            placeClickAction = placeClickAction,
            attachImageClickAction = attachImageClickAction,
            deleteImageClickAction = deleteImageClickAction
        )
    }

    private var selectedTempId: Int? = null

    override fun onInitView() {
        with (binding) {
            rvItems.apply {
                itemAnimator = null
                adapter = listAdapter
                addItemDecoration(UnderLineDecoration(this@MakeMultiplePurchaseNoteActivity))
            }

            btnBottom.setOnThrottleClickListener(saveBtnClickListener)
        }

        supportFragmentManager.setFragmentResultListener(
            CategoryListBSDialog.REQUEST_KEY, this, handleCategorySelectionResult)

        supportFragmentManager.setFragmentResultListener(
            PaymentMethodListBSDialog.REQUEST_KEY, this, handlePaymentMethodSelectionResult)
    }

    override fun onInitUiData() {
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
                        .collect {
                            binding.layoutLoading.root.isVisible = it
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.tempPurchaseNoteItems }
                        .distinctUntilChanged()
                        .collect {
                            listAdapter.submitList(it)
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
                        .map { it.isMadeSuccess }
                        .distinctUntilChanged()
                        .collect { isMadeSuccess ->
                            if (isMadeSuccess) {
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

    private val searchPlaceResultLauncher = registerStartActivityResultLauncher(
        resultOkBlock = { result ->
            val placeItem =
                result.data?.parcelable<PlaceInfoUiModel>(AppConstants.INTENT_TEMP_PLACE_ITEM)
                    ?: return@registerStartActivityResultLauncher

            selectedTempId?.let {
                viewModel.setPlaceItem(it, placeItem)
            }
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

                if (tempImages.isNotEmpty() && selectedTempId != null) {
                    viewModel.setTempImageUrls(selectedTempId!!, tempImages)
                }
            }
        }
    )

    private val addNextItemClickAction: () -> Unit = {
        viewModel.addNextItem()
    }

    private val deleteItemClickAction: (Int) -> Unit = { tempId ->
        viewModel.deleteItem(tempId)
    }

    private val categoryClickAction: (Int, CategoryUiModel?) -> Unit = { tempId, categoryItem ->
        selectedTempId = tempId

        CategoryListBSDialog.newInstance(
            selectedCategoryItem = categoryItem
        ).show(supportFragmentManager, CategoryListBSDialog.TAG)
    }

    private val purchaseNameInputAction: (Int, String) -> Unit = { tempId, purchaseName ->
        viewModel.setPurchaseName(tempId, purchaseName, false)
    }

    private val recentPurchaseNameClickAction: (Int, String) -> Unit = { tempId, purchaseName ->
        viewModel.setPurchaseName(tempId, purchaseName, true)
    }

    private val purchasePriceInputAction: (Int, Long) -> Unit = { tempId, purchasePrice ->
        viewModel.setPurchasePrice(tempId, purchasePrice)
    }

    private val purchaseDateClickAction: (Int, Long?) -> Unit = { tempId, purchaseDate ->
        val selection = purchaseDate?.toLocalDate()?.atStartOfDay(ZoneOffset.UTC)?.toInstant()
                ?.toEpochMilli()
                ?: MaterialDatePicker.todayInUtcMilliseconds()

        DatePickerManager.build(
            title = getString(R.string.select_purchase_date),
            selection = selection,
            positiveButtonClickAction = { timeInMillis ->
                viewModel.setPurchaseDate(tempId, timeInMillis)
            }
        ).show(supportFragmentManager, DatePickerManager.TAG)
    }

    private val paymentMethodClickAction: (Int, PaymentMethodUiModel?) -> Unit = { tempId, item ->
        selectedTempId = tempId

        PaymentMethodListBSDialog.newInstance(
            selectedPaymentMethodItem = item
        ).show(supportFragmentManager, PaymentMethodListBSDialog.TAG)
    }

    private val placeClickAction: (Int, PlaceInfoUiModel?) -> Unit = { tempId, item ->
        selectedTempId = tempId

        val intent = Intent(this, NaverMapSearchActivity::class.java).apply {
            putExtra(AppConstants.INTENT_TEMP_PLACE_ITEM, item)
        }

        searchPlaceResultLauncher.launch(intent)
    }

    private val attachImageClickAction: (Int) -> Unit = { tempId ->
        selectedTempId = tempId

        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }

        multiPhotoPickerLauncher.launch(intent)
    }

    private val deleteImageClickAction: (Int, String) -> Unit = { tempId, url ->
        viewModel.deleteTempImageByUrl(tempId, url)
    }

    private val saveBtnClickListener = View.OnClickListener {
        if (binding.btnBottom.isEnable) {
            viewModel.requestMakeMultiplePurchaseNotes()
        }
    }

    private val handleCategorySelectionResult: (String, Bundle) -> Unit = { _, data ->
        val selectedCategoryItem =
            data.parcelable<CategoryUiModel>(CategoryListBSDialog.BUNDLE_KEY_SELECTED_CATEGORY)

        selectedTempId?.let {
            viewModel.setCategory(it, selectedCategoryItem)
        }
    }

    private val handlePaymentMethodSelectionResult: (String, Bundle) -> Unit = { _, data ->
        val selectedPaymentMethodItem =
            data.parcelable<PaymentMethodUiModel>(PaymentMethodListBSDialog.BUNDLE_KEY_SELECTED_PAYMENT_METHOD)

        selectedTempId?.let {
            viewModel.setPaymentMethod(it, selectedPaymentMethodItem)
        }
    }
}