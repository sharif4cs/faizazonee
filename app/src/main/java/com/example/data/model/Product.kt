package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class ProductStockStatus {
    IN_STOCK,
    LOW_STOCK,
    OUT_OF_STOCK
}

enum class ExpiryStatus {
    SAFE,
    EXPIRING_SOON,
    EXPIRED,
    NOT_SET
}

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
    val sizesOrVariants: String = "Standard", // e.g., "M, L, XL" or shade/volume like "50ml, 100ml"
    val imageUrl: String = "",
    val isLowStockAlert: Boolean = false,
    val brand: String = "",
    val lowStockThreshold: Int = 5,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    
    // Cosmetics & Enhanced Inventory Fields
    val barcode: String = "",
    val unit: String = "পিস", // e.g., "পিস", "বক্স", "বোতল", "টিউব", "জার", "সেট"
    val wholesalePrice: Double = 0.0,
    val discount: Double = 0.0,
    val taxRate: Double = 0.0,
    val openingStock: Int = 0,
    val supplierName: String = "",
    val supplierPhone: String = "",
    val supplierId: String = "",
    val batchNumber: String = "",
    val manufacturingDate: String = "", // e.g. "2024-01-15"
    val expiryDate: String = "", // e.g. "2026-12-31"

    // Cloud Sync Fields
    val syncId: String = java.util.UUID.randomUUID().toString(),
    val shopId: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false,
    val syncStatus: String = "PENDING"
) {
    /**
     * Calculates days until expiry.
     * Returns positive integer if future, negative if past, null if not set or invalid.
     */
    fun daysUntilExpiry(): Long? {
        if (expiryDate.isBlank()) return null
        val formats = listOf("yyyy-MM-dd", "dd/MM/yyyy", "dd-MM-yyyy", "yyyy/MM/dd")
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US).apply { isLenient = false }
                val exp = sdf.parse(expiryDate.trim())
                if (exp != null) {
                    val now = Date()
                    val diff = exp.time - now.time
                    return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
                }
            } catch (_: Exception) { }
        }
        return null
    }

    fun isExpired(): Boolean {
        val days = daysUntilExpiry() ?: return false
        return days < 0
    }

    fun isExpiringSoon(thresholdDays: Int = 30): Boolean {
        val days = daysUntilExpiry() ?: return false
        return days in 0..thresholdDays
    }

    fun getExpiryStatus(): ExpiryStatus {
        val days = daysUntilExpiry() ?: return ExpiryStatus.NOT_SET
        return when {
            days < 0 -> ExpiryStatus.EXPIRED
            days <= 30 -> ExpiryStatus.EXPIRING_SOON
            else -> ExpiryStatus.SAFE
        }
    }

    fun getStockStatus(): ProductStockStatus {
        return when {
            stockQuantity <= 0 -> ProductStockStatus.OUT_OF_STOCK
            stockQuantity <= lowStockThreshold || isLowStockAlert -> ProductStockStatus.LOW_STOCK
            else -> ProductStockStatus.IN_STOCK
        }
    }
}

