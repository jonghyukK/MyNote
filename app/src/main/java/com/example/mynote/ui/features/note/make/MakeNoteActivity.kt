package com.example.mynote.ui.features.note.make

import android.view.View
import com.example.mynote.databinding.ActivityMakeNoteBinding
import com.example.mynote.ui.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MakeNoteActivity: BaseActivity<ActivityMakeNoteBinding>({ ActivityMakeNoteBinding.inflate(it) }) {

    override fun onInitView() = with (binding) {
        tbToolbar.setBackButtonClickListener(backBtnClickListener)
    }

    override fun onInitUiData() {
    }

    private val backBtnClickListener = View.OnClickListener {
        finish()
    }
}