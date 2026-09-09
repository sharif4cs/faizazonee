package com.example.data.repository

import com.example.data.local.ShopDao
import com.example.data.model.Customer
import com.example.data.model.CustomerTransaction
import com.example.data.model.Expense
import com.example.data.model.Product
import com.example.data.model.Sale
import com.example.data.model.SaleItem
import com.example.data.model.ShopProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ShopRepository(private val dao: ShopDao) {

    val allProducts: Flow<List<Product>> = dao.getAllProducts()
    val allCustomers: Flow<List<Customer>> = dao.getAllCustomers()
    val allSales: Flow<List<Sale>> = dao.getAllSales()
    val allExpenses: Flow<List<Expense>> = dao.getAllExpenses()
    val shopProfile: Flow<ShopProfile?> = dao.getShopProfile()
    val allTransactions: Flow<List<CustomerTransaction>> = dao.getAllTransactions()

    fun getTransactionsForCustomer(customerId: Long): Flow<List<CustomerTransaction>> {
        return dao.getTransactionsForCustomer(customerId)
    }

    fun getSaleItems(saleId: Long): Flow<List<SaleItem>> {
        return dao.getSaleItems(saleId)
    }

    suspend fun insertProduct(product: Product): Long = withContext(Dispatchers.IO) {
        dao.insertProduct(product)
    }

    suspend fun updateProduct(product: Product) = withContext(Dispatchers.IO) {
        dao.updateProduct(product)
    }

    suspend fun deleteProduct(product: Product) = withContext(Dispatchers.IO) {
        dao.deleteProduct(product)
    }

    suspend fun addStock(productId: Long, quantity: Int) = withContext(Dispatchers.IO) {
        dao.addStock(productId, quantity)
    }

    suspend fun insertCustomer(customer: Customer): Long = withContext(Dispatchers.IO) {
        dao.insertCustomer(customer)
    }

    suspend fun insertTransaction(transaction: CustomerTransaction) = withContext(Dispatchers.IO) {
        dao.insertTransaction(transaction)
    }

    suspend fun updateCustomer(customer: Customer) = withContext(Dispatchers.IO) {
        dao.updateCustomer(customer)
    }

    suspend fun deleteCustomer(customer: Customer) = withContext(Dispatchers.IO) {
        dao.deleteCustomer(customer)
    }

    suspend fun insertExpense(expense: Expense): Long = withContext(Dispatchers.IO) {
        dao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: Expense) = withContext(Dispatchers.IO) {
        dao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) = withContext(Dispatchers.IO) {
        dao.deleteExpense(expense)
    }

    suspend fun updateShopProfile(profile: ShopProfile) = withContext(Dispatchers.IO) {
        dao.insertOrUpdateShopProfile(profile)
    }

    suspend fun completeSale(
        items: List<SaleCartItem>,
        customer: Customer?,
        customerNameInput: String,
        customerPhoneInput: String,
        discount: Double,
        paidAmount: Double,
        isCash: Boolean
    ): Long = withContext(Dispatchers.IO) {
        val totalSubtotal = items.sumOf { it.product.sellingPrice * it.quantity }
        val netTotal = (totalSubtotal - discount).coerceAtLeast(0.0)
        val finalPaid = if (isCash) netTotal.coerceAtMost(paidAmount) else paidAmount
        val due = (netTotal - finalPaid).coerceAtLeast(0.0)

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val dateString = dateFormat.format(Date())
        val invoiceNo = "INV-" + (System.currentTimeMillis() % 1000000)

        var finalCustomer = customer
        if (finalCustomer == null && customerNameInput.isNotBlank()) {
            // Find existing customer by name/phone or create new
            val existing = dao.getAllCustomers().firstOrNull()?.find {
                (customerPhoneInput.isNotBlank() && it.phone == customerPhoneInput.trim()) ||
                        it.name.equals(customerNameInput.trim(), ignoreCase = true)
            }
            if (existing != null) {
                finalCustomer = existing
            } else {
                val newCustId = dao.insertCustomer(
                    Customer(
                        name = customerNameInput.trim(),
                        phone = customerPhoneInput.trim(),
                        totalPurchased = 0.0,
                        totalPaid = 0.0,
                        currentDue = 0.0,
                        lastSaleDate = dateString
                    )
                )
                finalCustomer = Customer(
                    id = newCustId,
                    name = customerNameInput.trim(),
                    phone = customerPhoneInput.trim(),
                    lastSaleDate = dateString
                )
            }
        }

        val sale = Sale(
            invoiceNumber = invoiceNo,
            customerId = finalCustomer?.id,
            customerName = finalCustomer?.name ?: customerNameInput.ifBlank { "নগদ ক্রেতা" },
            customerPhone = finalCustomer?.phone ?: customerPhoneInput,
            subtotal = totalSubtotal,
            discount = discount,
            paidAmount = finalPaid,
            dueAmount = due,
            paymentType = if (isCash && due == 0.0) "CASH" else "DUE",
            timestamp = System.currentTimeMillis(),
            dateString = dateString
        )

        val saleId = dao.insertSale(sale)

        // Insert sale items and deduct stock
        val saleItems = items.map {
            SaleItem(
                saleId = saleId,
                productId = it.product.id,
                productName = it.product.name,
                variant = it.variant,
                unitPrice = it.product.sellingPrice,
                quantity = it.quantity,
                totalPrice = it.product.sellingPrice * it.quantity
            )
        }
        dao.insertSaleItems(saleItems)

        for (item in items) {
            dao.deductStock(item.product.id, item.quantity)
        }

        // Update customer statistics and transaction ledger
        if (finalCustomer != null) {
            val updatedCust = finalCustomer.copy(
                totalPurchased = finalCustomer.totalPurchased + netTotal,
                totalPaid = finalCustomer.totalPaid + finalPaid,
                currentDue = finalCustomer.currentDue + due,
                lastSaleDate = dateString
            )
            dao.updateCustomer(updatedCust)

            // Record transaction for this sale
            dao.insertTransaction(
                CustomerTransaction(
                    customerId = finalCustomer.id,
                    type = "বিক্রি",
                    amount = netTotal,
                    note = "ইনভয়েস #$invoiceNo",
                    dateString = dateString,
                    timestamp = System.currentTimeMillis()
                )
            )

            if (finalPaid > 0.0 && due > 0.0) {
                // partial payment record
                dao.insertTransaction(
                    CustomerTransaction(
                        customerId = finalCustomer.id,
                        type = "পরিশোধ",
                        amount = finalPaid,
                        note = "নগদ জমা (#$invoiceNo)",
                        dateString = dateString,
                        timestamp = System.currentTimeMillis() + 1
                    )
                )
            }
        }

        saleId
    }

    suspend fun recordDuePayment(
        customer: Customer,
        amount: Double,
        note: String
    ) = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val dateString = dateFormat.format(Date())

        val updatedDue = (customer.currentDue - amount).coerceAtLeast(0.0)
        val updatedCustomer = customer.copy(
            totalPaid = customer.totalPaid + amount,
            currentDue = updatedDue
        )
        dao.updateCustomer(updatedCustomer)

        dao.insertTransaction(
            CustomerTransaction(
                customerId = customer.id,
                type = "পরিশোধ",
                amount = amount,
                note = note.ifBlank { "বাকি আদায়" },
                dateString = dateString,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        dao.clearProducts()
        dao.clearCustomers()
        dao.clearSales()
        dao.clearSaleItems()
        dao.clearTransactions()
        dao.clearExpenses()
    }

    suspend fun initializeShopProfileIfNeeded(
        defaultName: String = "আমার দোকান",
        defaultOwner: String = "মালিক"
    ) = withContext(Dispatchers.IO) {
        val current = dao.getShopProfile().firstOrNull()
        if (current == null) {
            dao.insertOrUpdateShopProfile(
                ShopProfile(
                    id = 1,
                    shopName = defaultName,
                    subtitle = "স্মার্ট শপ ম্যানেজমেন্ট",
                    ownerName = defaultOwner,
                    email = "",
                    phone = "",
                    address = "",
                    openingCash = 0.0
                )
            )
        }
    }

    suspend fun insertProducts(products: List<Product>) = withContext(Dispatchers.IO) {
        dao.insertProducts(products)
    }

    suspend fun seedInitialStockIfEmpty() = withContext(Dispatchers.IO) {
        val existing = dao.getAllProducts().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val sampleProducts = listOf(
                Product(
                    name = "পোলো টি-শার্ট (Polo T-Shirt Navy)",
                    sku = "TS-101",
                    category = "T-Shirt",
                    purchasePrice = 450.0,
                    sellingPrice = 750.0,
                    stockQuantity = 6,
                    sizesOrVariants = "M:2, L:2, XL:2",
                    isLowStockAlert = true,
                    brand = "Faiza Exclusive",
                    lowStockThreshold = 10,
                    note = "রি-অর্ডার প্রয়োজন (কম স্টক)"
                ),
                Product(
                    name = "সেমি লং পাঞ্জাবি (Semilong Panjabi)",
                    sku = "PJ-202",
                    category = "Shirt",
                    purchasePrice = 870.0,
                    sellingPrice = 1470.0,
                    stockQuantity = 10,
                    sizesOrVariants = "40:3, 42:4, 44:3",
                    isLowStockAlert = true,
                    brand = "Faiza Premium",
                    lowStockThreshold = 10,
                    note = "রি-অর্ডার প্রয়োজন (কম স্টক)"
                ),
                Product(
                    name = "প্রিমিয়াম কটন টি-শার্ট (Round Neck)",
                    sku = "TS-103",
                    category = "T-Shirt",
                    purchasePrice = 310.0,
                    sellingPrice = 490.0,
                    stockQuantity = 120,
                    sizesOrVariants = "M:40, L:50, XL:30",
                    isLowStockAlert = false,
                    brand = "Faiza Basic",
                    lowStockThreshold = 10,
                    note = "রানিং হট সেলিং আইটেম"
                ),
                Product(
                    name = "এক্সিকিউটিভ ফরমাল শার্ট (Cotton Shirt)",
                    sku = "FS-204",
                    category = "Shirt",
                    purchasePrice = 460.0,
                    sellingPrice = 740.0,
                    stockQuantity = 100,
                    sizesOrVariants = "15:30, 15.5:40, 16:30",
                    isLowStockAlert = false,
                    brand = "Faiza Classic",
                    lowStockThreshold = 10,
                    note = "অফিস ও ফরমাল ওয়্যার"
                ),
                Product(
                    name = "স্ট্রেচেবল ডেনিম জিন্স (Slim Fit)",
                    sku = "DP-305",
                    category = "Pants",
                    purchasePrice = 590.0,
                    sellingPrice = 940.0,
                    stockQuantity = 80,
                    sizesOrVariants = "30:20, 32:30, 34:20, 36:10",
                    isLowStockAlert = false,
                    brand = "Faiza Denim",
                    lowStockThreshold = 10,
                    note = "হাই কোয়ালিটি ওয়াশড ডেনিম"
                ),
                Product(
                    name = "ক্যাজুয়াল চিনো প্যান্ট (Stretch Chino)",
                    sku = "CP-306",
                    category = "Pants",
                    purchasePrice = 660.0,
                    sellingPrice = 1070.0,
                    stockQuantity = 60,
                    sizesOrVariants = "30:15, 32:25, 34:20",
                    isLowStockAlert = false,
                    brand = "Faiza Casual",
                    lowStockThreshold = 10,
                    note = "খাকি ও নেভি কালার"
                )
            )
            dao.insertProducts(sampleProducts)
        }
    }

    suspend fun resetToDefaultDemoData() = withContext(Dispatchers.IO) {
        clearAllData()
        seedInitialStockIfEmpty()
    }
}

data class SaleCartItem(
    val product: Product,
    val variant: String = "Default",
    val quantity: Int = 1
)
