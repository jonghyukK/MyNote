package com.kjh.data.repository

import com.example.domain.model.ApiResult
import com.example.domain.model.FilteredSearchPlaceNotes
import com.example.domain.model.PlaceNote
import com.example.domain.model.SearchPlaceNoteWithCount
import com.example.domain.model.asResult
import com.example.domain.model.safeApiCall
import com.example.domain.repository.PlaceNoteRepository
import com.kjh.data.model.entity.toDomainModel
import com.kjh.data.model.entity.toEntity
import com.kjh.data.source.local.PlaceNoteDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneId
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
    override fun getAllPlaceNotes(): Flow<List<PlaceNote>> =
        noteLocalDataSource.getAllPlaceNotes()
            .map { it.toDomainModel() }

    override fun observePlaceNoteById(noteId: Int): Flow<ApiResult<PlaceNote?>> =
        noteLocalDataSource.getPlaceNoteByIdFlow(noteId)
            .map { it?.toDomainModel() }
            .asResult()

    override fun observePlaceNotesByPlaceName(placeName: String): Flow<ApiResult<List<PlaceNote>>> =
        noteLocalDataSource.getPlaceNotesByPlaceNameFlow(placeName)
            .map { it.toDomainModel() }
            .asResult()

    /**
     *  장소노트 Insert Or Update 후, 장소노트 반환.
     *
     *  return PlaceNote
     */
    override suspend fun upsertAndGetPlaceNote(placeNote: PlaceNote): PlaceNote {
        val placeNoteEntity = placeNote.toEntity()

        val existingNote = noteLocalDataSource.getPlaceNoteById(placeNoteEntity.id)
        val newId = if (existingNote == null) {
            noteLocalDataSource.insert(placeNoteEntity).toInt()
        } else {
            noteLocalDataSource.update(placeNoteEntity)
            placeNote.id
        }

        return noteLocalDataSource.getPlaceNoteById(newId)!!.toDomainModel()
    }

    /**
     *  장소노트 삭제.
     *
     *  return noteId
     */
    override suspend fun deletePlaceNoteById(noteId: Int): Flow<ApiResult<Int>> =
        safeApiCall {
            noteLocalDataSource.deletePlaceNoteById(noteId)
            noteId
        }

    /**
     *  장소노트 조회 By Id.
     *
     *  return PlaceNote
     */
    override suspend fun getPlaceNoteById(noteId: Int): PlaceNote? =
        noteLocalDataSource.getPlaceNoteById(noteId)?.toDomainModel()

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
    override suspend fun searchByQueryFlow(query: String): Flow<ApiResult<List<SearchPlaceNoteWithCount>>> =
        safeApiCall {
            noteLocalDataSource.searchByQuery(query).toDomainModel()
        }

    override suspend fun getFilteredPlaceNotes(
        query: String,
        startDate: Long,
        endDate: Long,
        isDescending: Boolean
    ): List<FilteredSearchPlaceNotes> {
        val placeNotes = noteLocalDataSource.getFilteredPlaceNotes(
            query, startDate, endDate, isDescending
        )

        return placeNotes
            .groupBy { placeNote ->
                Instant.ofEpochMilli(placeNote.visitDate)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
            .map { (date, notes) ->
                FilteredSearchPlaceNotes(
                    date = date,
                    placeNotes = notes.toDomainModel()
                )
            }
            .let { groupedNotes ->
                if (isDescending) {
                    // 최신순
                    groupedNotes.sortedByDescending { it.date }
                } else {
                    // 오래된 순
                    groupedNotes.sortedBy { it.date }
                }
            }
    }

    override fun getPlaceNotesWithinDateRange(startDate: Long, endDate: Long): Flow<ApiResult<List<PlaceNote>>> =
        noteLocalDataSource.getPlaceNotesWithinDateRange(startDate, endDate).map {
            it.toDomainModel()
        }.asResult()
}