package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customer_transactions")
data class CustomerTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val type: String, // "বিক্রি" (Sale), "পরিশোধ" (Payment)
    val amount: Double,
    val note: String = "",
    val dateString: String, // e.g. "06 Sep 2026"
    val timestamp: Long = System.currentTimeMillis()
)
