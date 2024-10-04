package com.kjh.data.model

import com.google.gson.annotations.SerializedName

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 4..
 * Description:
 */

data class KakaoPlaceModel(

    @SerializedName("id")
    val id: String,

    @SerializedName("place_name")
    val placeName: String,

    @SerializedName("address_name")
    val addressName: String,

    @SerializedName("road_address_name")
    val roadAddressName: String,

    @SerializedName("x")
    val x: String,

    @SerializedName("y")
    val y: String,
)