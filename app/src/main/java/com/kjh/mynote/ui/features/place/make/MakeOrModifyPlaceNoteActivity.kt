package com.kjh.mynote.ui.features.place.make

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
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
import com.kjh.mynote.databinding.ActivityMakePlaceNoteBinding
import com.kjh.mynote.model.PlaceInfoUiModel
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.features.map.NaverMapSearchActivity
import com.kjh.mynote.ui.features.place.make.AddPurchaseNoteDialogFragment.Companion.BUNDLE_KEY_ADDED_TEMP_PURCHASE_NOTE
import com.kjh.mynote.ui.features.place.make.adapter.TempImageListAdapter
import com.kjh.mynote.ui.features.place.make.adapter.TempPurchaseNoteListAdapter
import com.kjh.mynote.utils.DatePickerManager
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.decorations.SpacingItemDecoration
import com.kjh.mynote.utils.extensions.parcelable
import com.kjh.mynote.utils.extensions.registerStartActivityResultLauncher
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.showToast
import com.kjh.mynote.utils.extensions.toLocalDate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.ZoneOffset

@AndroidEntryPoint
class MakeOrModifyPlaceNoteActivity: BaseActivity<ActivityMakePlaceNoteBinding>({ ActivityMakePlaceNoteBinding.inflate(it) }) {

    private val viewModel: MakeOrModifyPlaceNoteViewModel by viewModels()

    private val tempImageListAdapter: TempImageListAdapter by lazy {
        TempImageListAdapter(deleteImageClickAction = deleteTempImageClickAction)
    }

    private val tempPurchaseNoteListAdapter: TempPurchaseNoteListAdapter by lazy {
        TempPurchaseNoteListAdapter(
            tempPurchaseNoteItemClickAction = tempPurchaseNoteItemClickAction,
            removePurchaseNoteBtnClickAction = removePurchaseNoteBtnClickAction
        )
    }

    override fun onInitView() {
        with (binding) {
            rvTempImages.apply {
                adapter = tempImageListAdapter
            }

            rvPurchaseNotes.apply {
                itemAnimator = null
                addItemDecoration(SpacingItemDecoration(top = 6, exceptFirstItem = true))
                adapter = tempPurchaseNoteListAdapter
            }

            etNoteContents.addTextChangedListener(contentsTextWatcher)

            tvVisitPlace.setTextClickListener(searchMapClickListener)
            tvVisitDate.setTextClickListener(visitDateClickListener)

            clAttachImages.setOnThrottleClickListener(photoAttachClickListener)
            clAddPurchaseNote.setOnThrottleClickListener(addPurchaseNoteClickListener)
            btnSave.setOnThrottleClickListener(saveBtnClickListener)
        }

        supportFragmentManager.setFragmentResultListener(
            AddPurchaseNoteDialogFragment.REQUEST_KEY, this, handleTempPurchaseNoteAddResult
        )
    }

    override fun onInitUiData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.errorMessage.collectLatest {
                        showToast(it)
                    }
                }

                launch {
                    viewModel.uiState
                        .map { it.titleRes to it.bottomBtnTextRes }
                        .distinctUntilChanged()
                        .collect { (titleRes, bottomBtnTextRes) ->
                            binding.tbToolbar.leftTitle = getString(titleRes)
                            binding.btnSave.btnTitle = getString(bottomBtnTextRes)
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
                        .map { it.tempPlaceItem }
                        .distinctUntilChanged()
                        .collect { tempPlaceItem ->
                            if (tempPlaceItem == null) {
                                binding.tvVisitPlace.text = getString(R.string.search_visit_place)
                                binding.tvVisitPlace.textColor = R.color.black_500
                            } else {
                                binding.tvVisitPlace.text = tempPlaceItem.placeName
                                binding.tvVisitPlace.textColor = R.color.black_900
                            }
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.visitDateText }
                        .distinctUntilChanged()
                        .collect { visitDateText ->
                            if (visitDateText.isBlank()) {
                                binding.tvVisitDate.text = getString(R.string.select_visit_date)
                                binding.tvVisitDate.textColor = R.color.black_500
                            } else {
                                binding.tvVisitDate.text = visitDateText
                                binding.tvVisitDate.textColor = R.color.black_900
                            }
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.contents }
                        .distinctUntilChanged()
                        .collect { contents ->
                            if (contents != binding.etNoteContents.text.toString()) {
                                binding.etNoteContents.setText(contents)
                            }
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.tempPurchaseNoteItems }
                        .distinctUntilChanged()
                        .collect { tempPurchaseNoteItems ->
                            binding.rvPurchaseNotes.isVisible = tempPurchaseNoteItems.isNotEmpty()
                            tempPurchaseNoteListAdapter.submitList(tempPurchaseNoteItems)
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.makeOrEditResult }
                        .filterNotNull()
                        .collect { newPlaceNoteItem ->
                            Intent().apply {
                                putExtra(AppConstants.INTENT_PLACE_NOTE_ITEM, newPlaceNoteItem)
                                setResult(RESULT_OK, this)
                                finish()
                            }
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.canSaveOrEdit }
                        .distinctUntilChanged()
                        .collectLatest { isValid ->
                            binding.btnSave.isEnable = isValid
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

    private val contentsTextWatcher = object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            viewModel.setContents(s.toString())
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

                if (tempImages.isNotEmpty()) {
                    viewModel.setTempImages(tempImages)
                }
            }
        }
    )

    private val searchPlaceResultLauncher = registerStartActivityResultLauncher(
        resultOkBlock = { result ->
            val placeItem =
                result.data?.parcelable<PlaceInfoUiModel>(AppConstants.INTENT_TEMP_PLACE_ITEM)
                    ?: return@registerStartActivityResultLauncher

            viewModel.setTempPlaceItem(placeItem)
        })

    private val deleteTempImageClickAction: (String) -> Unit = { uri ->
        viewModel.deleteTempImageByUrl(uri)
    }

    private val tempPurchaseNoteItemClickAction: (TempPurchaseNoteItem) -> Unit = { tempPurchaseNoteItem ->
        AddPurchaseNoteDialogFragment.newInstance(
            tempPurchaseNoteItem = tempPurchaseNoteItem
        ).show(supportFragmentManager, AddPurchaseNoteDialogFragment.TAG)
    }

    private val removePurchaseNoteBtnClickAction: (TempPurchaseNoteItem) -> Unit = {
        viewModel.removeTempPurchaseNoteItem(it)
    }

    private val searchMapClickListener = View.OnClickListener {
        val tempPlaceItem = viewModel.uiState.value.tempPlaceItem
        val intent = Intent(this, NaverMapSearchActivity::class.java).apply {
            putExtra(AppConstants.INTENT_TEMP_PLACE_ITEM, tempPlaceItem)
        }
        searchPlaceResultLauncher.launch(intent)
    }

    private val visitDateClickListener = OnClickListener {
        val selection = if (viewModel.uiState.value.visitDate > 0) {
            viewModel.uiState.value.visitDate.toLocalDate()
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        } else {
            MaterialDatePicker.todayInUtcMilliseconds()
        }

        DatePickerManager.build(
            title = getString(R.string.select_visit_date),
            selection = selection,
            positiveButtonClickAction = { long -> viewModel.setVisitDate(long) }
        ).show(supportFragmentManager, DatePickerManager.TAG)
    }

    private val photoAttachClickListener = View.OnClickListener {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        multiPhotoPickerLauncher.launch(intent)
    }

    private val addPurchaseNoteClickListener = View.OnClickListener {
        AddPurchaseNoteDialogFragment.newInstance()
            .show(supportFragmentManager, AddPurchaseNoteDialogFragment.TAG)
    }

    private val saveBtnClickListener = View.OnClickListener {
        if (binding.btnSave.isEnable) {
            viewModel.requestUpsertPlaceNote()
        }
    }

    private val handleTempPurchaseNoteAddResult: (String, Bundle) -> Unit =  { _, data ->
        val tempPurchaseNoteItem = data.parcelable<TempPurchaseNoteItem>(
            BUNDLE_KEY_ADDED_TEMP_PURCHASE_NOTE
        )
        tempPurchaseNoteItem?.let {
            viewModel.addOrUpdatePurchaseNoteItem(it)
        }
    }
}