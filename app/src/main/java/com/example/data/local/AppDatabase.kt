package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Customer
import com.example.data.model.CustomerTransaction
import com.example.data.model.Expense
import com.example.data.model.Product
import com.example.data.model.Sale
import com.example.data.model.SaleItem
import com.example.data.model.ShopProfile
import com.example.data.model.StockTransaction

@Database(
    entities = [
        Product::class,
        Customer::class,
        Sale::class,
        SaleItem::class,
        CustomerTransaction::class,
        Expense::class,
        ShopProfile::class,
        StockTransaction::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shopDao(): ShopDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Products
                db.execSQL("ALTER TABLE products ADD COLUMN syncId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE products ADD COLUMN shopId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE products ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE products ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE products ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'PENDING'")
                db.execSQL("UPDATE products SET syncId = 'prod_' || id, updatedAt = createdAt WHERE syncId = ''")

                // Customers
                db.execSQL("ALTER TABLE customers ADD COLUMN syncId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE customers ADD COLUMN shopId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE customers ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE customers ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE customers ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'PENDING'")
                db.execSQL("UPDATE customers SET syncId = 'cust_' || id, updatedAt = createdAt WHERE syncId = ''")

                // Customer Transactions
                db.execSQL("ALTER TABLE customer_transactions ADD COLUMN syncId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE customer_transactions ADD COLUMN customerSyncId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE customer_transactions ADD COLUMN shopId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE customer_transactions ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE customer_transactions ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE customer_transactions ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE customer_transactions ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'PENDING'")
                db.execSQL("UPDATE customer_transactions SET syncId = 'tx_' || id, createdAt = timestamp, updatedAt = timestamp WHERE syncId = ''")

                // Expenses
                db.execSQL("ALTER TABLE expenses ADD COLUMN syncId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE expenses ADD COLUMN shopId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE expenses ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE expenses ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE expenses ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE expenses ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'PENDING'")
                db.execSQL("UPDATE expenses SET syncId = 'exp_' || id, createdAt = timestamp, updatedAt = timestamp WHERE syncId = ''")

                // Sales
                db.execSQL("ALTER TABLE sales ADD COLUMN syncId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE sales ADD COLUMN customerSyncId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE sales ADD COLUMN shopId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE sales ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sales ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sales ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sales ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'PENDING'")
                db.execSQL("UPDATE sales SET syncId = 'sale_' || id, createdAt = timestamp, updatedAt = timestamp WHERE syncId = ''")

                // Sale Items
                db.execSQL("ALTER TABLE sale_items ADD COLUMN syncId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE sale_items ADD COLUMN saleSyncId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE sale_items ADD COLUMN productSyncId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE sale_items ADD COLUMN shopId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE sale_items ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sale_items ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sale_items ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sale_items ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'PENDING'")
                db.execSQL("UPDATE sale_items SET syncId = 'item_' || id WHERE syncId = ''")

                // Shop Profile
                db.execSQL("ALTER TABLE shop_profile ADD COLUMN syncId TEXT NOT NULL DEFAULT 'profile_main'")
                db.execSQL("ALTER TABLE shop_profile ADD COLUMN shopId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE shop_profile ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE shop_profile ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE shop_profile ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE shop_profile ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'PENDING'")

                // Stock Transactions
                db.execSQL("ALTER TABLE stock_transactions ADD COLUMN syncId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE stock_transactions ADD COLUMN productSyncId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE stock_transactions ADD COLUMN shopId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE stock_transactions ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE stock_transactions ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE stock_transactions ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE stock_transactions ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'PENDING'")
                db.execSQL("UPDATE stock_transactions SET syncId = 'stk_' || id, createdAt = timestamp, updatedAt = timestamp WHERE syncId = ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "faiza_hisab_db"
                )
                    .addMigrations(MIGRATION_4_5)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
