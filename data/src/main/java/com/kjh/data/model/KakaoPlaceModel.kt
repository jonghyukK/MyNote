package com.kjh.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 4..
 * Description:
 */

@Parcelize
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
): Parcelable