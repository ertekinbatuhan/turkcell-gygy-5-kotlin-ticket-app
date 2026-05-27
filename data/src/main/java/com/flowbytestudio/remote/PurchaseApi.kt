package com.flowbytestudio.remote

import com.flowbytestudio.data.dto.PurchaseCreateDto
import com.flowbytestudio.data.dto.PurchaseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PurchaseApi {
    @POST("/purchases")
    suspend fun createPurchase(@Body request: PurchaseCreateDto): PurchaseDto

    @POST("/purchases/{id}/pay")
    suspend fun pay(@Path("id") id: String): PurchaseDto

    @GET("/purchases/{id}")
    suspend fun getPurchase(@Path("id") id: String): PurchaseDto
}
