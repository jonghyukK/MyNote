package com.kjh.data.db

import androidx.room.TypeConverter
import com.example.domain.model.PurchasePlaceInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 4..
 * Description:
 */
class DBTypeConverters {

    /**
     *  List<String> ...
     */
    @TypeConverter
    fun fromUrisToString(uriList: List<String>?): String? {
        return uriList?.joinToString(separator = ",")
    }

    @TypeConverter
    fun toUriList(data: String?): List<String> {
        return data?.split(",") ?: emptyList()
    }

    private val gson = Gson()

    /**
     *  PurchasePlaceNoteInfo ...
     */
    @TypeConverter
    fun fromPurchasePlaceInfo(placeInfo: PurchasePlaceInfo): String {
        return gson.toJson(placeInfo)
    }

    @TypeConverter
    fun toPurchasePlaceInfo(data: String): PurchasePlaceInfo {
        val type = object: TypeToken<PurchasePlaceInfo>() {}.type
        return gson.fromJson(data, type)
    }

}