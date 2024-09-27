package com.example.mynote.ui.features.note.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.mynote.databinding.VhNoteItemBinding


class NoteListAdapter(
    private val noteClickAction: (Int) -> Unit
): ListAdapter<NoteItemUiState, NoteListItemViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        NoteListItemViewHolder(
            VhNoteItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), noteClickAction
        )

    override fun onBindViewHolder(holder: NoteListItemViewHolder, position: Int) {
        val item = currentList[position]
        holder.bind(item)
    }

    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<NoteItemUiState>() {
            override fun areItemsTheSame(
                oldItem: NoteItemUiState,
                newItem: NoteItemUiState
            ): Boolean = oldItem.noteId == newItem.noteId

            override fun areContentsTheSame(
                oldItem: NoteItemUiState,
                newItem: NoteItemUiState
            ): Boolean = oldItem == newItem
        }
    }
}