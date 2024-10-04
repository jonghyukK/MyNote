package com.kjh.mynote.ui.features.note.make

import android.net.Uri
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.R
import com.kjh.mynote.databinding.ActivityMakeNoteBinding
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MakeNoteActivity: BaseActivity<ActivityMakeNoteBinding>({ ActivityMakeNoteBinding.inflate(it) }) {

    private val viewModel: MakeNoteViewModel by viewModels()

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
        btnSave.setOnThrottleClickListener(saveBtnClickListener)
        clAttachImages.setOnThrottleClickListener(photoAttachClickListener)
    }

    override fun onInitUiData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState
                        .map { it.tempImageUris }
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
            }
        }
    }

    private val multiPhotoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(AppConstants.MAX_SELECTABLE_IMAGE_COUNT)
    ) { uris ->
        viewModel.setTempImageUris(uris)
    }

    private val deleteTempImageClickAction: (Uri) -> Unit = { uri ->
        viewModel.deleteTempImageByUri(uri)
    }

    private val tempImageClickAction: (Uri) -> Unit = {
        showToast("Expanded!")
    }

    private val backBtnClickListener = View.OnClickListener {
        finish()
    }

    private val saveBtnClickListener = View.OnClickListener {
        viewModel.savePlaceNote()
    }

    private val photoAttachClickListener = View.OnClickListener {
        multiPhotoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

}