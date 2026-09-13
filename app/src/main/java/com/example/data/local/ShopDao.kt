package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Customer
import com.example.data.model.CustomerTransaction
import com.example.data.model.Expense
import com.example.data.model.Product
import com.example.data.model.Sale
import com.example.data.model.SaleItem
import com.example.data.model.ShopProfile
import com.example.data.model.StockTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDao {

    // Products
    @Query("SELECT * FROM products WHERE deleted = 0 ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id AND deleted = 0")
    fun getProductById(id: Long): Flow<Product?>

    @Query("SELECT * FROM products WHERE syncId = :syncId LIMIT 1")
    suspend fun getProductBySyncId(syncId: String): Product?

    @Query("SELECT * FROM products WHERE syncStatus = 'PENDING'")
    suspend fun getPendingProducts(): List<Product>

    @Query("UPDATE products SET syncStatus = :status WHERE id = :id")
    suspend fun updateProductSyncStatus(id: Long, status: String)

    @Query("UPDATE products SET deleted = 1, syncStatus = 'PENDING', updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteProduct(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("UPDATE products SET stockQuantity = stockQuantity - :quantity, updatedAt = :updatedAt, syncStatus = 'PENDING' WHERE id = :productId")
    suspend fun deductStock(productId: Long, quantity: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE products SET stockQuantity = stockQuantity + :quantity, updatedAt = :updatedAt, syncStatus = 'PENDING' WHERE id = :productId")
    suspend fun addStock(productId: Long, quantity: Int, updatedAt: Long = System.currentTimeMillis())

    // Customers
    @Query("SELECT * FROM customers WHERE deleted = 0 ORDER BY lastSaleDate DESC, name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id AND deleted = 0")
    fun getCustomerById(id: Long): Flow<Customer?>

    @Query("SELECT * FROM customers WHERE syncId = :syncId LIMIT 1")
    suspend fun getCustomerBySyncId(syncId: String): Customer?

    @Query("SELECT * FROM customers WHERE syncStatus = 'PENDING'")
    suspend fun getPendingCustomers(): List<Customer>

    @Query("UPDATE customers SET syncStatus = :status WHERE id = :id")
    suspend fun updateCustomerSyncStatus(id: Long, status: String)

    @Query("UPDATE customers SET deleted = 1, syncStatus = 'PENDING', updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteCustomer(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<Customer>)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    // Sales
    @Query("SELECT * FROM sales WHERE deleted = 0 ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE syncId = :syncId LIMIT 1")
    suspend fun getSaleBySyncId(syncId: String): Sale?

    @Query("SELECT * FROM sales WHERE syncStatus = 'PENDING'")
    suspend fun getPendingSales(): List<Sale>

    @Query("UPDATE sales SET syncStatus = :status WHERE id = :id")
    suspend fun updateSaleSyncStatus(id: Long, status: String)

    @Query("UPDATE sales SET deleted = 1, syncStatus = 'PENDING', updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteSale(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSales(sales: List<Sale>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItem>)

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId AND deleted = 0")
    fun getSaleItems(saleId: Long): Flow<List<SaleItem>>

    @Query("SELECT * FROM sale_items WHERE syncId = :syncId LIMIT 1")
    suspend fun getSaleItemBySyncId(syncId: String): SaleItem?

    @Query("SELECT * FROM sale_items WHERE syncStatus = 'PENDING'")
    suspend fun getPendingSaleItems(): List<SaleItem>

    @Query("UPDATE sale_items SET syncStatus = :status WHERE id = :id")
    suspend fun updateSaleItemSyncStatus(id: Long, status: String)

    // Transactions
    @Query("SELECT * FROM customer_transactions WHERE deleted = 0 ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<CustomerTransaction>>

    @Query("SELECT * FROM customer_transactions WHERE customerId = :customerId AND deleted = 0 ORDER BY timestamp DESC")
    fun getTransactionsForCustomer(customerId: Long): Flow<List<CustomerTransaction>>

    @Query("SELECT * FROM customer_transactions WHERE syncId = :syncId LIMIT 1")
    suspend fun getCustomerTransactionBySyncId(syncId: String): CustomerTransaction?

    @Query("SELECT * FROM customer_transactions WHERE syncStatus = 'PENDING'")
    suspend fun getPendingTransactions(): List<CustomerTransaction>

    @Query("UPDATE customer_transactions SET syncStatus = :status WHERE id = :id")
    suspend fun updateTransactionSyncStatus(id: Long, status: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: CustomerTransaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(txs: List<CustomerTransaction>)

    // Expenses
    @Query("SELECT * FROM expenses WHERE deleted = 0 ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE syncId = :syncId LIMIT 1")
    suspend fun getExpenseBySyncId(syncId: String): Expense?

    @Query("SELECT * FROM expenses WHERE syncStatus = 'PENDING'")
    suspend fun getPendingExpenses(): List<Expense>

    @Query("UPDATE expenses SET syncStatus = :status WHERE id = :id")
    suspend fun updateExpenseSyncStatus(id: Long, status: String)

    @Query("UPDATE expenses SET deleted = 1, syncStatus = 'PENDING', updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteExpense(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<Expense>)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    // Shop Profile
    @Query("SELECT * FROM shop_profile WHERE id = 1 LIMIT 1")
    fun getShopProfile(): Flow<ShopProfile?>

    @Query("SELECT * FROM shop_profile WHERE syncStatus = 'PENDING' LIMIT 1")
    suspend fun getPendingShopProfile(): ShopProfile?

    @Query("UPDATE shop_profile SET syncStatus = :status WHERE id = :id")
    suspend fun updateShopProfileSyncStatus(id: Int, status: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateShopProfile(profile: ShopProfile)

    // Stock Transactions
    @Query("SELECT * FROM stock_transactions WHERE deleted = 0 ORDER BY timestamp DESC")
    fun getAllStockTransactions(): Flow<List<StockTransaction>>

    @Query("SELECT * FROM stock_transactions WHERE productId = :productId AND deleted = 0 ORDER BY timestamp DESC")
    fun getStockTransactionsForProduct(productId: Long): Flow<List<StockTransaction>>

    @Query("SELECT * FROM stock_transactions WHERE syncId = :syncId LIMIT 1")
    suspend fun getStockTransactionBySyncId(syncId: String): StockTransaction?

    @Query("SELECT * FROM stock_transactions WHERE syncStatus = 'PENDING'")
    suspend fun getPendingStockTransactions(): List<StockTransaction>

    @Query("UPDATE stock_transactions SET syncStatus = :status WHERE id = :id")
    suspend fun updateStockTxSyncStatus(id: Long, status: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockTransaction(transaction: StockTransaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockTransactions(transactions: List<StockTransaction>)

    @Query("DELETE FROM stock_transactions")
    suspend fun clearStockTransactions()

    // Clear Data
    @Query("DELETE FROM products")
    suspend fun clearProducts()

    @Query("DELETE FROM customers")
    suspend fun clearCustomers()

    @Query("DELETE FROM sales")
    suspend fun clearSales()

    @Query("DELETE FROM sale_items")
    suspend fun clearSaleItems()

    @Query("DELETE FROM customer_transactions")
    suspend fun clearTransactions()

    @Query("DELETE FROM expenses")
    suspend fun clearExpenses()
}
