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
import com.kjh.mynote.R
import com.kjh.mynote.databinding.ActivityPlaceNoteDetailBinding
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.common.components.MyDefaultDialog
import com.kjh.mynote.ui.features.viewer.ImagesViewerActivity
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.setDarkStatusBar
import com.kjh.mynote.utils.extensions.setLightStatusBar
import com.kjh.mynote.utils.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@AndroidEntryPoint
class PlaceNoteDetailActivity
    : BaseActivity<ActivityPlaceNoteDetailBinding>({ ActivityPlaceNoteDetailBinding.inflate(it) }) {

    private val viewModel: PlaceNoteDetailViewModel by viewModels()

    private val uiListAdapter: PlaceNoteDetailUiListAdapter by lazy {
        PlaceNoteDetailUiListAdapter(imageViewerClickAction)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
    }

    override fun onInitView() {
        with (binding) {
            ViewCompat.setOnApplyWindowInsetsListener(tbToolbar) { v, insets ->
                val systemBarsTop = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
                v.setPadding(v.left, systemBarsTop, v.right, v.bottom)
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
        showToast("수정하기")
    }

    private val toolbarMoreButtonClickListener = OnClickListener {
        showMenuDialog()
    }
}