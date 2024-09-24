package com.example.mynote.ui.features.note.list

import androidx.recyclerview.widget.RecyclerView
import com.example.mynote.databinding.VhNoteItemBinding

class NoteListItemViewHolder(
    private val binding: VhNoteItemBinding
): RecyclerView.ViewHolder(binding.root) {

    fun bind(noteItem: NoteItemUiState) {
        binding.tvContents.text = noteItem.contents
    }
}