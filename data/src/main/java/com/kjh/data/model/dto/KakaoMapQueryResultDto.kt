package com.kjh.data.model.dto

import com.google.gson.annotations.SerializedName

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 4..
 * Description:
 */

data class KakaoMapQueryResultDto(
    @SerializedName("documents")
    val kakaoPlaces: List<KakaoPlaceDto>
)