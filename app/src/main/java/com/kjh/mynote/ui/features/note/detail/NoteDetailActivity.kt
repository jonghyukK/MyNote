package com.kjh.mynote.ui.features.note.detail

import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.kjh.mynote.databinding.ActivityNoteDetailBinding
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.base.BaseViewModel
import com.kjh.mynote.utils.constants.AppConstants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 9. 26..
 * Description:
 */

@AndroidEntryPoint
class NoteDetailActivity: BaseActivity<ActivityNoteDetailBinding>({ ActivityNoteDetailBinding.inflate(it) }) {

    private val viewModel: NoteDetailViewModel by viewModels()

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    private var noteId: Int = -1

    override fun onInitView() {
        with (binding) {
            tbToolbar.setBackButtonClickListener(backBtnClickListener)

            Glide.with(this@NoteDetailActivity)
                .load("https://science.nasa.gov/wp-content/uploads/2023/09/Milky_Way_illustration-1.jpeg?w=800")
                .into(ivImage)
        }
    }

    override fun onInitUiData() {
        noteId = intent.getIntExtra(AppConstants.INTENT_NOTE_ID, -1)
        if (noteId < 0) {
            finish()
            return
        }

        viewModel.getNoteById(noteId)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState
                        .map { it.isLoading }
                        .distinctUntilChanged()
                        .collect { isLoading ->
                            binding.cpiLoading.isVisible = isLoading
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.noteItem }
                        .collect { noteItem ->
                            binding.tvContents.text = noteItem?.contents ?: ""
                        }
                }
            }
        }
    }

    private val backBtnClickListener = View.OnClickListener {
        finish()
    }
}