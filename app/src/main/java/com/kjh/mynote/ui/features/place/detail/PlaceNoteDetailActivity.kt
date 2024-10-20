package com.kjh.mynote.ui.features.place.detail

import android.content.Intent
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.databinding.ActivityPlaceNoteDetailBinding
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.features.viewer.ImagesViewerActivity
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.loadImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.ArrayList


@AndroidEntryPoint
class PlaceNoteDetailActivity
    : BaseActivity<ActivityPlaceNoteDetailBinding>({ ActivityPlaceNoteDetailBinding.inflate(it) }) {

    private val viewModel: PlaceNoteDetailViewModel by viewModels()

    private val uiListAdapter: PlaceNoteDetailUiListAdapter by lazy {
        PlaceNoteDetailUiListAdapter(imageViewerClickAction)
    }

    override fun onInitView() {
        with (binding) {
            rvPlaceDetails.apply {
                setHasFixedSize(true)
                adapter = uiListAdapter
            }
        }
    }

    override fun onInitUiData() {
        viewModel.getPlaceNote()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState
                        .map { it.placeNoteDetailUis }
                        .distinctUntilChanged()
                        .collect { uis ->
                            uiListAdapter.submitList(uis)
                        }
                }
            }
        }
    }

    private val imageViewerClickAction: (List<String>, String) -> Unit = { images, clickedImage ->
        Intent(this@PlaceNoteDetailActivity, ImagesViewerActivity::class.java).apply {
            putExtra(AppConstants.INTENT_IMAGE_LIST, ArrayList(images))
            putExtra(AppConstants.INTENT_URL, clickedImage)
            startActivity(this)
        }
    }
}