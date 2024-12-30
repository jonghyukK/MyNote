package com.example.domain.repository

import com.example.domain.model.ApiResult
import com.example.domain.model.FilteredSearchPlaceNotes
import com.example.domain.model.PlaceNote
import com.example.domain.model.SearchPlaceNoteWithCount
import kotlinx.coroutines.flow.Flow

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 28..
 * Description:
 */
interface PlaceNoteRepository {

    fun getAllPlaceNotes(): Flow<List<PlaceNote>>

    suspend fun upsertAndGetPlaceNote(
        placeNote: PlaceNote,
        noteId: Int = -1
    ): PlaceNote

    suspend fun deletePlaceNoteById(noteId: Int): Int

    suspend fun getPlaceNoteById(noteId: Int): PlaceNote?

    suspend fun getPlaceNotesByPlaceName(placeName: String): List<PlaceNote>

    suspend fun searchByQueryFlow(query: String): List<SearchPlaceNoteWithCount>

    suspend fun getFilteredPlaceNotes(
        query: String,
        startDate: Long,
        endDate: Long,
        isDescending: Boolean
    ): List<FilteredSearchPlaceNotes>

    fun getPlaceNotesWithinDateRange(startDate: Long, endDate: Long): Flow<ApiResult<List<PlaceNote>>>
}