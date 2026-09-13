package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val customerId: Long? = null,
    val customerName: String = "",
    val customerPhone: String = "",
    val subtotal: Double,
    val discount: Double = 0.0,
    val paidAmount: Double,
    val dueAmount: Double = 0.0,
    val paymentType: String, // "CASH", "DUE"
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String, // e.g. "07 Sep 2026"

    // Cloud Sync Fields
    val syncId: String = java.util.UUID.randomUUID().toString(),
    val customerSyncId: String = "",
    val shopId: String = "",
    val createdAt: Long = timestamp,
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false,
    val syncStatus: String = "PENDING"
)
