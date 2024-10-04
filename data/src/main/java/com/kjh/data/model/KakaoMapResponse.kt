package com.kjh.data.model

import com.google.gson.annotations.SerializedName

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 4..
 * Description:
 */

data class KakaoMapResponse(
    @SerializedName("documents")
    val documents: List<KakaoPlaceModel>
)