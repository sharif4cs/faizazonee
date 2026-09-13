package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sale_items")
data class SaleItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val productName: String,
    val variant: String = "",
    val unitPrice: Double,
    val quantity: Int,
    val totalPrice: Double,

    // Cloud Sync Fields
    val syncId: String = java.util.UUID.randomUUID().toString(),
    val saleSyncId: String = "",
    val productSyncId: String = "",
    val shopId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false,
    val syncStatus: String = "PENDING"
)
