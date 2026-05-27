package com.flowbytestudio.core.domain.purchase

interface PurchaseRepository {
    suspend fun createPurchase(items: Map<String, Int>): Result<Purchase>
    suspend fun payPurchase(id: String): Result<Purchase>
    suspend fun getPurchase(id: String): Result<Purchase>
}
