package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_transactions")
data class StockTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val productName: String,
    val type: String, // "PURCHASE" (Restock), "SALE", "SALE_RETURN", "DAMAGED", "LOST", "EXPIRED", "ADJUSTMENT", "INITIAL_IMPORT"
    val quantityChange: Int, // e.g., +20 or -5
    val balanceAfter: Int,
    val reason: String = "",
    val referenceNo: String = "", // Invoice #, Batch #, Supplier Name, etc.
    val unitPrice: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String = "",

    // Cloud Sync Fields
    val syncId: String = java.util.UUID.randomUUID().toString(),
    val productSyncId: String = "",
    val shopId: String = "",
    val createdAt: Long = timestamp,
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false,
    val syncStatus: String = "PENDING"
)
