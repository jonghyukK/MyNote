package com.kjh.data.repository

import com.kjh.data.model.KakaoMapResponse
import com.kjh.data.model.Result
import com.kjh.data.source.remote.KakaoApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 4..
 * Description:
 */
class KakaoMapRepository @Inject constructor(
    private val kakaoMapRemoteDataSource: KakaoApiService
) {

    suspend fun getPlacesByQuery(query: String): Flow<Result<KakaoMapResponse>> = flow {
        emit(Result.Loading)

        try {
            val response = kakaoMapRemoteDataSource.getPlaceByQuery(query = query)
            if (response.isSuccessful) {
                emit(Result.Success(response.body()))
            } else {
                emit(Result.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message))
        }
    }
}