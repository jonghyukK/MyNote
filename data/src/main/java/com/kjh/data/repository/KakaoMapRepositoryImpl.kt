package com.kjh.data.repository

import com.example.domain.model.PlaceInfo
import com.example.domain.repository.KakaoMapRepository
import com.kjh.data.ResponseWrapper
import com.kjh.data.model.dto.toDomainModel
import com.kjh.data.source.remote.KakaoApiService
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 4..
 * Description:
 */
class KakaoMapRepositoryImpl @Inject constructor(
    private val kakaoMapRemoteDataSource: KakaoApiService
): KakaoMapRepository {

    override suspend fun getPlacesByQuery(query: String): List<PlaceInfo> {
        val response = kakaoMapRemoteDataSource.getPlaceByQuery(query = query)
        val res = ResponseWrapper.parseResponse(response)?.kakaoPlaces?.map {
            it.toDomainModel()
        } ?: emptyList()

        return res
    }
}