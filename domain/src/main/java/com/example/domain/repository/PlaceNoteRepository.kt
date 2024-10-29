package com.example.domain.repository

import com.example.domain.model.PlaceNote
import kotlinx.coroutines.flow.Flow

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 28..
 * Description:
 */
interface PlaceNoteRepository {

    val placeNotesFlow: Flow<List<PlaceNote>>

    suspend fun upsertAndGetPlaceNote(
        placeNote: PlaceNote,
        noteId: Int = -1
    ): PlaceNote

    suspend fun deletePlaceNoteById(noteId: Int): Int

    suspend fun getPlaceNoteById(noteId: Int): PlaceNote

    suspend fun getPlaceNotesByPlaceName(placeName: String): List<PlaceNote>
}