package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sku: String,
    val category: String,
    val purchasePrice: Double,
    val sellingPrice: Double,
    val stockQuantity: Int,
    val sizesOrVariants: String, // e.g., "M, L, XL, XXL" or "M:20 | L:15 | XL:8"
    val imageUrl: String = "",
    val isLowStockAlert: Boolean = false,
    val brand: String = "",
    val lowStockThreshold: Int = 5,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
