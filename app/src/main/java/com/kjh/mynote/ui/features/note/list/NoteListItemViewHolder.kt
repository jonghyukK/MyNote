package com.kjh.mynote.ui.features.note.list

import androidx.recyclerview.widget.RecyclerView
import com.kjh.mynote.databinding.VhNoteItemBinding
import com.kjh.mynote.utils.extensions.onThrottleClick

class NoteListItemViewHolder(
    private val binding: VhNoteItemBinding,
    private val noteClickAction: (Int) -> Unit
): RecyclerView.ViewHolder(binding.root) {

    private var bindItem: NoteItemUiState? = null

    init {
        itemView.onThrottleClick {
            bindItem?.let { data -> noteClickAction.invoke(data.noteId) }
        }
    }

    fun bind(noteItem: NoteItemUiState) {
        bindItem = noteItem
        binding.tvContents.text = noteItem.contents
    }
}