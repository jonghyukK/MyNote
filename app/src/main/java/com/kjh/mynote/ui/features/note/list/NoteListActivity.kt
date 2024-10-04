package com.kjh.mynote.ui.features.note.list

import android.content.Intent
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.databinding.ActivityMainBinding
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.features.note.detail.NoteDetailActivity
import com.kjh.mynote.ui.features.note.make.MakeNoteActivity
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NoteListActivity : BaseActivity<ActivityMainBinding>({ ActivityMainBinding.inflate(it) }) {

    private val viewModel: NoteListViewModel by viewModels()

    private val noteListAdapter: NoteListAdapter by lazy {
        NoteListAdapter(noteClickAction)
    }

    override fun onInitView() = with (binding) {
        rvNoteList.apply {
            setHasFixedSize(true)
            adapter = noteListAdapter
        }

        fabAddNote.setOnThrottleClickListener(addNoteClickListener)
    }

    override fun onInitUiData() {
        viewModel.fetchNoteItems()

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
                        .map { it.isEmpty }
                        .distinctUntilChanged()
                        .collect { isEmpty -> binding.tvEmpty.isVisible = isEmpty }
                }

                launch {
                    viewModel.uiState
                        .map { it.noteItems }
                        .distinctUntilChanged()
                        .collect { noteItems ->
                            noteListAdapter.submitList(noteItems)
                        }
                }
            }
        }
    }

    private val addNoteClickListener = View.OnClickListener {
        val intent = Intent(this, MakeNoteActivity::class.java)
        startActivity(intent)
    }

    private val noteClickAction: (Int) -> Unit = { noteId ->
        val intent = Intent(this, NoteDetailActivity::class.java).apply {
            putExtra(AppConstants.INTENT_NOTE_ID, noteId)
        }
        startActivity(intent)
    }
}