package com.kjh.mynote.ui.features.place.detail

import android.content.Intent
import android.os.Bundle
import android.view.View.OnClickListener
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.kjh.data.model.PlaceNoteModel
import com.kjh.mynote.R
import com.kjh.mynote.databinding.ActivityPlaceNoteDetailBinding
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.common.components.MyDefaultDialog
import com.kjh.mynote.ui.features.place.detail.adapter.PlaceNoteDetailUiListAdapter
import com.kjh.mynote.ui.features.place.make.MakeOrModifyPlaceNoteActivity
import com.kjh.mynote.ui.features.place.map.PlaceMapActivity
import com.kjh.mynote.ui.features.viewer.ImagesViewerActivity
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.parcelable
import com.kjh.mynote.utils.extensions.registerStartActivityResultLauncher
import com.kjh.mynote.utils.extensions.setDarkStatusBar
import com.kjh.mynote.utils.extensions.setLightStatusBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@AndroidEntryPoint
class PlaceNoteDetailActivity
    : BaseActivity<ActivityPlaceNoteDetailBinding>({ ActivityPlaceNoteDetailBinding.inflate(it) }) {

    private val viewModel: PlaceNoteDetailViewModel by viewModels()

    private val uiListAdapter: PlaceNoteDetailUiListAdapter by lazy {
        PlaceNoteDetailUiListAdapter(imageViewerClickAction, addressClickAction)
    }

    private var isAppliedInset = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
    }

    override fun onInitView() {
        with (binding) {
            ViewCompat.setOnApplyWindowInsetsListener(tbToolbar) { v, insets ->
                if (!isAppliedInset) {
                    val systemBarsTop = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
                    v.setPadding(v.left, systemBarsTop, v.right, v.bottom)
                }

                isAppliedInset = true
                insets
            }

            rvPlaceDetails.apply {
                setHasFixedSize(true)
                adapter = uiListAdapter
                addOnScrollListener(onScrollListener)
            }

            tbToolbar.setMoreButtonClickListener(toolbarMoreButtonClickListener)
        }
    }

    override fun onInitUiData() {
        viewModel.getPlaceNoteDetail()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState
                        .map { it.placeNoteDetailUiItems }
                        .distinctUntilChanged()
                        .collect { uis ->
                            uiListAdapter.submitList(uis)
                        }
                }

                launch {
                    viewModel.deleteEvent.collect { deleteNoteId ->
                        Intent().apply {
                            putExtra(AppConstants.INTENT_NOTE_ID, deleteNoteId)
                            setResult(RESULT_OK, this)
                            finish()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        binding.rvPlaceDetails.removeOnScrollListener(onScrollListener)
        super.onDestroy()
    }

    private fun showMenuDialog() {
        PlaceNoteDetailMenuBSDialog.newInstance(
            deleteAction = noteDeleteAction,
            modifyAction = noteModifyAction
        ).show(supportFragmentManager, PlaceNoteDetailMenuBSDialog.TAG)
    }

    private val modifyResultLauncher = registerStartActivityResultLauncher(
        resultOkBlock = { result ->
            val updatedNoteItem = result.data?.parcelable<PlaceNoteModel>(AppConstants.INTENT_PLACE_NOTE_ITEM)
            updatedNoteItem?.let {
                viewModel.getPlaceNoteDetail()

                Intent().apply {
                    putExtra(AppConstants.INTENT_PLACE_NOTE_ITEM, updatedNoteItem)
                    setResult(RESULT_OK, this)
                }
            }
        }
    )

    private val onScrollListener = object: RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val totalScroll = recyclerView.computeVerticalScrollOffset()
            val pagerHeight = resources.getDimensionPixelSize(R.dimen.place_detail_pager_height)
            val toolbarHeight = binding.tbToolbar.height
            val progress = (totalScroll / (pagerHeight - toolbarHeight).toFloat()).coerceIn(0f, 1f)

            binding.motionLayout.progress = progress

            if (progress >= 1f) {
                setDarkStatusBar()
            } else {
                setLightStatusBar()
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

    private val addressClickAction: (PlaceNoteModel) -> Unit = { item ->
        Intent(this@PlaceNoteDetailActivity, PlaceMapActivity::class.java).apply {
            putExtra(AppConstants.INTENT_PLACE_NOTE_ITEM, item)
            startActivity(this)
        }
    }

    private val noteDeleteAction: () -> Unit = {
        MyDefaultDialog.newInstance(
            contents = getString(R.string.will_you_delete),
            posBtnText = getString(R.string.yes_i_will_delete),
            negBtnText = getString(R.string.cancel),
            posAction = {
                viewModel.deletePlaceNote()
            }
        ).show(supportFragmentManager, MyDefaultDialog.TAG)
    }

    private val noteModifyAction: () -> Unit = {
        val noteItem = viewModel.uiState.value.placeNoteItem
        noteItem?.let {
            Intent(this@PlaceNoteDetailActivity, MakeOrModifyPlaceNoteActivity::class.java).apply {
                putExtra(AppConstants.INTENT_PLACE_NOTE_ITEM, it)
                modifyResultLauncher.launch(this)
            }
        }
    }

    private val toolbarMoreButtonClickListener = OnClickListener {
        showMenuDialog()
    }
}