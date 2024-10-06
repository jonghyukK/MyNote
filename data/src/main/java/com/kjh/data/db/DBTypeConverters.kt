package com.kjh.data.db

import androidx.room.TypeConverter

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 4..
 * Description:
 */
class DBTypeConverters {

    @TypeConverter
    fun fromUrisToString(uriList: List<String>?): String {
        return uriList?.joinToString(separator = ",") ?: ""
    }

    @TypeConverter
    fun toUriList(data: String?): List<String> {
        return data?.split(",") ?: emptyList()
    }
}