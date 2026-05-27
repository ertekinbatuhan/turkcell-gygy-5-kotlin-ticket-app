package com.flowbytestudio.repository

import com.flowbytestudio.core.domain.purchase.Purchase
import com.flowbytestudio.core.domain.purchase.PurchaseRepository
import com.flowbytestudio.data.dto.PurchaseCreateDto
import com.flowbytestudio.data.dto.PurchaseItemRequestDto
import com.flowbytestudio.data.mapper.toDomain
import com.flowbytestudio.data.util.runCatchingApi
import com.flowbytestudio.remote.PurchaseApi

class PurchaseRepositoryImpl(
    private val purchaseApi: PurchaseApi
) : PurchaseRepository {
    override suspend fun createPurchase(items: Map<String, Int>): Result<Purchase> =
        runCatchingApi {
            purchaseApi.createPurchase(
                PurchaseCreateDto(
                    items = items.map { PurchaseItemRequestDto(ticketTypeId = it.key, quantity = it.value) }
                )
            )
        }.map { it.toDomain() }

    override suspend fun payPurchase(id: String): Result<Purchase> =
        runCatchingApi {
            purchaseApi.pay(id = id)
        }.map { it.toDomain() }

    override suspend fun getPurchase(id: String): Result<Purchase> =
        runCatchingApi {
            purchaseApi.getPurchase(id = id)
        }.map { it.toDomain() }
}
