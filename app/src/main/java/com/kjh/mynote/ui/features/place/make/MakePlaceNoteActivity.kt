package com.kjh.mynote.ui.features.place.make

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.OnClickListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.datepicker.MaterialDatePicker
import com.kjh.data.model.KakaoPlaceModel
import com.kjh.mynote.R
import com.kjh.mynote.databinding.ActivityMakePlaceNoteBinding
import com.kjh.mynote.model.UiState
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.base.BaseViewModel
import com.kjh.mynote.ui.features.map.NaverMapActivity
import com.kjh.mynote.utils.DatePickerManager
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.parcelable
import com.kjh.mynote.utils.extensions.registerStartActivityResultLauncher
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MakePlaceNoteActivity: BaseActivity<ActivityMakePlaceNoteBinding>({ ActivityMakePlaceNoteBinding.inflate(it) }) {

    private val viewModel: MakePlaceNoteViewModel by viewModels()

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    private val tempImageListAdapter: TempImageListAdapter by lazy {
        TempImageListAdapter(
            deleteImageClickAction = deleteTempImageClickAction,
            tempImageClickAction = tempImageClickAction
        )
    }

    override fun onInitView() = with (binding) {
        rvTempImages.apply {
            setHasFixedSize(true)
            adapter = tempImageListAdapter
        }

        tbToolbar.setBackButtonClickListener(backBtnClickListener)
        clAttachImages.setOnThrottleClickListener(photoAttachClickListener)
        clVisitPlaceContainer.setOnThrottleClickListener(searchMapClickListener)
        clVisitDateContainer.setOnThrottleClickListener(visitDateClickListener)
        etNoteTitle.addTextChangedListener(titleTextWatcher)
        etNoteContents.addTextChangedListener(contentsTextWatcher)
        btnSave.setOnThrottleClickListener(saveBtnClickListener)
    }

    override fun onInitUiData() {
        val tempVisitDate = intent.getLongExtra(AppConstants.INTENT_PLACE_VISIT_DATE, -1)
        if (tempVisitDate > 0) {
            viewModel.setVisitDate(tempVisitDate)
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
                            if (tempPlaceItem == null) {
                                binding.tvVisitPlace.text = getString(R.string.search_visit_place)
                                binding.tvVisitPlace.setTextColor(getColor(R.color.black_500))
                            } else {
                                binding.tvVisitPlace.text = tempPlaceItem.placeName
                                binding.tvVisitPlace.setTextColor(getColor(R.color.black_900))
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
                                binding.tvVisitDate.setTextColor(getColor(R.color.black_500))
                            } else {
                                binding.tvVisitDate.text = visitDateText
                                binding.tvVisitDate.setTextColor(getColor(R.color.black_900))
                            }
                        }
                }

                launch {
                    viewModel.isSavedNoteEvent.collect { saveResult ->
                        binding.btnSave.isLoading = saveResult is UiState.Loading

                        if (saveResult is UiState.Success) {
                            showToast(getString(R.string.msg_created_place_note))
                            Intent().apply {
                                putExtra(AppConstants.INTENT_PLACE_VISIT_DATE, saveResult.data)
                                setResult(RESULT_OK, this)
                                finish()
                            }
                        }
                    }
                }

                launch {
                    viewModel.saveValidateFlow.collect { isValid ->
                        binding.btnSave.isEnable = isValid
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        with (binding) {
            etNoteTitle.removeTextChangedListener(titleTextWatcher)
            etNoteContents.removeTextChangedListener(contentsTextWatcher)
        }
        super.onDestroy()
    }

    private fun showDatePicker(positiveBtnClickAction: (Long) -> Unit) {
        val selection = if (viewModel.getVisitDateTimeMills() > 0) {
            viewModel.getVisitDateTimeMills()
        } else {
            MaterialDatePicker.todayInUtcMilliseconds()
        }

        DatePickerManager.build(
            title = getString(R.string.select_visit_date),
            selection = selection,
            positiveButtonClickAction = positiveBtnClickAction
        ).show(supportFragmentManager, "DATE_PICKER")
    }

    private val titleTextWatcher = object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            viewModel.setTitle(s.toString())
        }
    }

    private val contentsTextWatcher = object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            viewModel.setContents(s.toString())
        }
    }

    private val multiPhotoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) {
            return@registerForActivityResult
        }

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

    private val searchPlaceResultLauncher =
        registerStartActivityResultLauncher(resultOkBlock = { result ->
            val placeItem =
                result.data?.parcelable<KakaoPlaceModel>(AppConstants.INTENT_TEMP_PLACE_ITEM)
                    ?: return@registerStartActivityResultLauncher

            viewModel.setTempPlaceItem(placeItem)
        })

    private val deleteTempImageClickAction: (String) -> Unit = { uri ->
        viewModel.deleteTempImageByUrl(uri)
    }

    private val tempImageClickAction: (String) -> Unit = {
        showToast("개발 예정..")
    }

    private val backBtnClickListener = View.OnClickListener {
        finish()
    }

    private val searchMapClickListener = View.OnClickListener {
        val intent = Intent(this@MakePlaceNoteActivity, NaverMapActivity::class.java).apply {
            putExtra(AppConstants.INTENT_TEMP_PLACE_ITEM, viewModel.getTempPlaceItem())
        }
        searchPlaceResultLauncher.launch(intent)
    }

    private val visitDateClickListener = OnClickListener {
        showDatePicker(positiveBtnClickAction = datePickerPositiveBtnClickAction)
    }

    private val datePickerPositiveBtnClickAction: (Long) -> Unit = { long ->
        viewModel.setVisitDate(long)
    }

    private val saveBtnClickListener = View.OnClickListener {
        if (binding.btnSave.isEnable) {
            viewModel.savePlaceNote()
        }
    }

    private val photoAttachClickListener = View.OnClickListener {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        multiPhotoPickerLauncher.launch(intent)
    }
}