package com.kjh.data.repository

import com.example.domain.model.PlaceNote
import com.example.domain.repository.PlaceNoteRepository
import com.kjh.data.model.entity.PlaceNoteEntity
import com.kjh.data.model.entity.toDomainModel
import com.kjh.data.model.entity.toEntity
import com.kjh.data.source.local.PlaceNoteDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 9. 25..
 * Description:
 */

class PlaceNoteRepositoryImpl @Inject constructor(
    private val noteLocalDataSource: PlaceNoteDao
): PlaceNoteRepository {

    override val placeNotesFlow: Flow<List<PlaceNote>>
        get() = noteLocalDataSource.observeAll().map(List<PlaceNoteEntity>::toDomainModel)

    override suspend fun upsertAndGetPlaceNote(
        placeNote: PlaceNote,
        noteId: Int
    ): PlaceNote {
        var placeNoteEntity = placeNote.toEntity()
        if (noteId > 0) {
            placeNoteEntity = placeNoteEntity.copy(id = noteId)
        }

        return noteLocalDataSource.insert(placeNoteEntity).run {
            noteLocalDataSource.getPlaceNoteById(this.toInt())
        }.toDomainModel()
    }

    override suspend fun deletePlaceNoteById(noteId: Int): Int {
        noteLocalDataSource.deletePlaceNoteById(noteId)
        return noteId
    }

    override suspend fun getPlaceNoteById(noteId: Int): PlaceNote =
        noteLocalDataSource.getPlaceNoteById(noteId).toDomainModel()

    override suspend fun getPlaceNotesByPlaceName(placeName: String): List<PlaceNote> =
        noteLocalDataSource.getPlaceNotesByPlaceName(placeName).toDomainModel()
}