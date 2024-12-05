package com.example.domain.usecase

import com.example.domain.model.ApiResult
import com.example.domain.model.PlaceNote
import com.example.domain.model.PlaceNoteDetail
import com.example.domain.model.PurchaseNote
import com.example.domain.model.getResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 5..
 * Description:
 */
class GetPlaceNoteDetailByIdUseCase @Inject constructor(
    private val getPlaceNoteByIdUseCase: GetPlaceNoteByIdUseCase,
    private val getPlaceNotesByPlaceNameUseCase: GetPlaceNotesByPlaceNameUseCase,
    private val getPurchaseNotesByPlaceAndDateUseCase: GetPurchaseNotesByPlaceAndDateUseCase
) {
    suspend operator fun invoke(placeNoteId: Int): Flow<ApiResult<PlaceNoteDetail>> = flow {
        emit(ApiResult.Loading)

        // Step 1.
        getPlaceNoteByIdUseCase(placeNoteId).collect { placeNoteResult ->
            placeNoteResult.getResult(
                loading = {
                    emit(ApiResult.Loading)
                },
                success = { result ->
                    val placeNote = result.data
                        ?: return@getResult emit(ApiResult.Error(NullPointerException()))

                    // Step 2.
                    combine(
                        getPlaceNotesByPlaceNameUseCase(placeNote.placeInfo.name),
                        getPurchaseNotesByPlaceAndDateUseCase(
                            placeName = placeNote.placeInfo.name,
                            date = placeNote.visitDate
                        )
                    ) { samePlaceNotesResult, purchaseNotesResult ->
                        combineResults(placeNote, samePlaceNotesResult, purchaseNotesResult)
                    }.collect { combinedResult ->
                        emit(combinedResult)
                    }
                },
                error = { error ->
                    emit(ApiResult.Error(error.error))
                }
            )
        }
    }

    private fun combineResults(
        placeNote: PlaceNote,
        samePlaceNotesResult: ApiResult<List<PlaceNote>>,
        purchaseNotesResult: ApiResult<List<PurchaseNote>>
    ): ApiResult<PlaceNoteDetail> =
        if (samePlaceNotesResult is ApiResult.Success && purchaseNotesResult is ApiResult.Success) {
            ApiResult.Success(
                PlaceNoteDetail(
                    placeNote = placeNote,
                    samePlaceNameNotes = samePlaceNotesResult.data.filter { it.id != placeNote.id },
                    purchaseNotes = purchaseNotesResult.data
                )
            )
        } else {
            when {
                samePlaceNotesResult is ApiResult.Error -> ApiResult.Error(samePlaceNotesResult.error)
                purchaseNotesResult is ApiResult.Error -> ApiResult.Error(purchaseNotesResult.error)
                else -> ApiResult.Loading
            }
        }
}