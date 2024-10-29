package com.example.domain.usecase

import com.example.domain.model.KakaoPlace
import com.example.domain.model.Result
import com.example.domain.repository.KakaoMapRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 29..
 * Description:
 */
@Singleton
class GetKakaoPlacesByQueryUseCase @Inject constructor(
    private val kakaoMapRepository: KakaoMapRepository
) {

    suspend operator fun invoke(query: String): Flow<Result<List<KakaoPlace>>> = flow {
        emit(Result.Loading)

        try {
            emit(Result.Success(kakaoMapRepository.getPlacesByQuery(query)))
        } catch (e: Exception) {
            emit(Result.Error(e.message))
        }
    }
}