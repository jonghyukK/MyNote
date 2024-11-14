package com.kjh.data.repository

import com.example.domain.model.PlaceNote
import com.example.domain.model.SearchPlaceNoteWithCount
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

    /**
     *  장소노트 전체 조회.
     *
     *  return Flow<List<PlaceNote>>
     */
    override val placeNotesFlow: Flow<List<PlaceNote>>
        get() = noteLocalDataSource.observeAll().map(List<PlaceNoteEntity>::toDomainModel)

    /**
     *  장소노트 Insert Or Update 후, 장소노트 반환.
     *
     *  return PlaceNote
     */
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

    /**
     *  장소노트 삭제.
     *
     *  return noteId
     */
    override suspend fun deletePlaceNoteById(noteId: Int): Int {
        noteLocalDataSource.deletePlaceNoteById(noteId)
        return noteId
    }

    /**
     *  장소노트 조회 By Id.
     *
     *  return PlaceNote
     */
    override suspend fun getPlaceNoteById(noteId: Int): PlaceNote =
        noteLocalDataSource.getPlaceNoteById(noteId).toDomainModel()

    /**
     * 장소노트 목록 조회 by PlaceName.
     *
     * return List<PlaceNote>
     */
    override suspend fun getPlaceNotesByPlaceName(placeName: String): List<PlaceNote> =
        noteLocalDataSource.getPlaceNotesByPlaceName(placeName).toDomainModel()

    /**
     *  장소노트 목록 및 카운트 조회 by Query.
     *
     *  return List<SearchPlaceNoteWithCount>
     */
    override suspend fun searchByQueryFlow(query: String): List<SearchPlaceNoteWithCount> =
        noteLocalDataSource.searchByQuery(query).toDomainModel()
}