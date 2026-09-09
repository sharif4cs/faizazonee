package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // e.g. "দোকান ভাড়া", "বিদ্যুৎ বিল", "কর্মচারীর বেতন", "অন্যান্য"
    val customCategory: String = "", // custom category if "অন্যান্য" is chosen
    val amount: Double,
    val paymentMethod: String = "নগদ ক্যাশ", // "নগদ ক্যাশ", "bKash", "Nagad", "ব্যাংক"
    val note: String = "",
    val title: String = "",
    val dateString: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    val displayCategory: String
        get() = if (category == "অন্যান্য" && customCategory.isNotBlank()) customCategory else category
}
