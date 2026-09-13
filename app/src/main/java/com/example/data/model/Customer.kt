package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val address: String = "",
    val totalPurchased: Double = 0.0,
    val totalPaid: Double = 0.0,
    val currentDue: Double = 0.0,
    val lastSaleDate: String = "",
    val createdAt: Long = System.currentTimeMillis(),

    // Cloud Sync Fields
    val syncId: String = java.util.UUID.randomUUID().toString(),
    val shopId: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false,
    val syncStatus: String = "PENDING"
)
