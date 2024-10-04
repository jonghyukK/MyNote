package com.kjh.data.source.remote

import com.kjh.data.model.KakaoMapResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 4..
 * Description:
 */

interface KakaoApiService {

    @GET("v2/local/search/keyword.json")
    suspend fun getPlaceByQuery(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 15
    ): Response<KakaoMapResponse>
}