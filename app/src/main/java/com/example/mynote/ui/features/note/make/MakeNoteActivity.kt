package com.example.mynote.ui.features.note.make

import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mynote.databinding.ActivityMakeNoteBinding
import com.example.mynote.ui.base.BaseActivity
import com.example.mynote.utils.extensions.setOnThrottleClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MakeNoteActivity: BaseActivity<ActivityMakeNoteBinding>({ ActivityMakeNoteBinding.inflate(it) }) {

    private val viewModel: MakeNoteViewModel by viewModels()

    override fun onInitView() = with (binding) {
        tbToolbar.setBackButtonClickListener(backBtnClickListener)
        btnSave.setOnThrottleClickListener(saveBtnClickListener)
    }

    override fun onInitUiData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isSavedNote.collect { isSaved ->
                    if (isSaved) {
                        finish()
                    }
                }
            }
        }
    }

    private val backBtnClickListener = View.OnClickListener {
        finish()
    }

    private val saveBtnClickListener = View.OnClickListener {
        val contents = binding.etContents.text.toString()
        viewModel.saveNote(contents)
    }
}