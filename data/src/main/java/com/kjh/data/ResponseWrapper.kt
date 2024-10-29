package com.kjh.data

import retrofit2.HttpException
import retrofit2.Response

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 29..
 * Description:
 */
object ResponseWrapper {

    fun <T> parseResponse(response: Response<T>): T? {
        return try {
            if (response.isSuccessful) {
                response.body()
            } else {
                throw HttpException(response)
            }
        } catch (e: Exception) {
            // 그 외의 일반적인 예외 처리
            throw Exception("Unknown error occurred: ${e.message}", e)
        }
    }
}