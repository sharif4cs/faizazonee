package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shop_profile")
data class ShopProfile(
    @PrimaryKey
    val id: Int = 1,
    val shopName: String = "ফাইজা স্টোর",
    val subtitle: String = "স্মার্ট শপ ম্যানেজমেন্ট ও পিওএস",
    val ownerName: String = "মোঃ শরিফ",
    val email: String = "sharif4cs@gmail.com",
    val phone: String = "01798113899",
    val address: String = "ঢাকা, বাংলাদেশ",
    val openingCash: Double = 0.0,
    val openingBkash: Double = 0.0,
    val openingNagad: Double = 0.0,
    val openingBank: Double = 0.0,
    val currencySymbol: String = "৳",

    // Cloud Sync Fields
    val syncId: String = "profile_main",
    val shopId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false,
    val syncStatus: String = "PENDING"
)
