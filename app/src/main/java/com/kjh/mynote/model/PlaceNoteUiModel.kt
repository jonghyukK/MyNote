package com.kjh.mynote.model

import android.os.Parcelable
import com.example.domain.model.PlaceNote
import com.kjh.mynote.utils.extensions.toLocalDate
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 28..
 * Description:
 */

@Parcelize
data class PlaceNoteUiModel(
    val id: Int = 0,
    val placeImages: List<String>,
    val placeInfo: PlaceInfoUiModel,
    val visitDate: Long,
    val noteContents: String,
    val localDate: LocalDate
): Parcelable

/**
 *  PlaceNote (domain) -> PlaceNoteUiModel (presentation)
 */
fun PlaceNote.toUiModel() = PlaceNoteUiModel(
    id = id,
    placeImages = placeImages,
    placeInfo = placeInfo.toUiModel(),
    visitDate = visitDate,
    noteContents = noteContents,
    localDate = visitDate.toLocalDate()
)

fun List<PlaceNote>.toUiModel() = map(PlaceNote::toUiModel)