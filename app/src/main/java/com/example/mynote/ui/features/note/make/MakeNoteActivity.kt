package com.example.mynote.ui.features.note.make

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.mynote.databinding.ActivityMakeNoteBinding
import com.example.mynote.ui.base.BaseActivity

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