package com.example.domain.repository

import com.example.domain.model.PlaceInfo

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 29..
 * Description:
 */
interface KakaoMapRepository {

    suspend fun getPlacesByQuery(query: String): List<PlaceInfo>
}