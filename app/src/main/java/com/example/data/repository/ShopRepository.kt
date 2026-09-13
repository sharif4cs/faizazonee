package com.example.data.repository

import com.example.data.local.ShopDao
import com.example.data.model.Customer
import com.example.data.model.CustomerTransaction
import com.example.data.model.Expense
import com.example.data.model.Product
import com.example.data.model.Sale
import com.example.data.model.SaleItem
import com.example.data.model.ShopProfile
import com.example.data.model.StockTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ShopRepository(
    private val dao: ShopDao,
    private val syncManager: com.example.sync.SyncManager? = null
) {

    val allProducts: Flow<List<Product>> = dao.getAllProducts()
    val allCustomers: Flow<List<Customer>> = dao.getAllCustomers()
    val allSales: Flow<List<Sale>> = dao.getAllSales()
    val allExpenses: Flow<List<Expense>> = dao.getAllExpenses()
    val shopProfile: Flow<ShopProfile?> = dao.getShopProfile()
    val allTransactions: Flow<List<CustomerTransaction>> = dao.getAllTransactions()
    val allStockTransactions: Flow<List<StockTransaction>> = dao.getAllStockTransactions()

    fun getTransactionsForCustomer(customerId: Long): Flow<List<CustomerTransaction>> {
        return dao.getTransactionsForCustomer(customerId)
    }

    fun getStockTransactionsForProduct(productId: Long): Flow<List<StockTransaction>> {
        return dao.getStockTransactionsForProduct(productId)
    }

    fun getSaleItems(saleId: Long): Flow<List<SaleItem>> {
        return dao.getSaleItems(saleId)
    }

    suspend fun insertProduct(product: Product): Long = withContext(Dispatchers.IO) {
        val toInsert = product.copy(
            updatedAt = System.currentTimeMillis(),
            syncStatus = "PENDING"
        )
        val id = dao.insertProduct(toInsert)
        syncManager?.triggerSync()
        id
    }

    suspend fun updateProduct(product: Product) = withContext(Dispatchers.IO) {
        val toUpdate = product.copy(
            updatedAt = System.currentTimeMillis(),
            syncStatus = "PENDING"
        )
        dao.updateProduct(toUpdate)
        syncManager?.triggerSync()
    }

    suspend fun deleteProduct(product: Product) = withContext(Dispatchers.IO) {
        // Soft delete instead of immediate hard delete for cloud synchronization
        dao.softDeleteProduct(product.id, System.currentTimeMillis())
        syncManager?.triggerSync()
    }

    suspend fun addStock(productId: Long, quantity: Int) = withContext(Dispatchers.IO) {
        dao.addStock(productId, quantity, System.currentTimeMillis())
        syncManager?.triggerSync()
    }

    suspend fun insertCustomer(customer: Customer): Long = withContext(Dispatchers.IO) {
        val toInsert = customer.copy(
            updatedAt = System.currentTimeMillis(),
            syncStatus = "PENDING"
        )
        val id = dao.insertCustomer(toInsert)
        syncManager?.triggerSync()
        id
    }

    suspend fun insertTransaction(transaction: CustomerTransaction) = withContext(Dispatchers.IO) {
        val toInsert = transaction.copy(
            updatedAt = System.currentTimeMillis(),
            syncStatus = "PENDING"
        )
        val id = dao.insertTransaction(toInsert)
        syncManager?.triggerSync()
        id
    }

    suspend fun updateCustomer(customer: Customer) = withContext(Dispatchers.IO) {
        val toUpdate = customer.copy(
            updatedAt = System.currentTimeMillis(),
            syncStatus = "PENDING"
        )
        dao.updateCustomer(toUpdate)
        syncManager?.triggerSync()
    }

    suspend fun deleteCustomer(customer: Customer) = withContext(Dispatchers.IO) {
        // Soft delete instead of immediate hard delete for cloud synchronization
        dao.softDeleteCustomer(customer.id, System.currentTimeMillis())
        syncManager?.triggerSync()
    }

    suspend fun insertExpense(expense: Expense): Long = withContext(Dispatchers.IO) {
        val toInsert = expense.copy(
            updatedAt = System.currentTimeMillis(),
            syncStatus = "PENDING"
        )
        val id = dao.insertExpense(toInsert)
        syncManager?.triggerSync()
        id
    }

    suspend fun updateExpense(expense: Expense) = withContext(Dispatchers.IO) {
        val toUpdate = expense.copy(
            updatedAt = System.currentTimeMillis(),
            syncStatus = "PENDING"
        )
        dao.updateExpense(toUpdate)
        syncManager?.triggerSync()
    }

    suspend fun deleteExpense(expense: Expense) = withContext(Dispatchers.IO) {
        // Soft delete instead of immediate hard delete for cloud synchronization
        dao.softDeleteExpense(expense.id, System.currentTimeMillis())
        syncManager?.triggerSync()
    }

    suspend fun updateShopProfile(profile: ShopProfile) = withContext(Dispatchers.IO) {
        val toUpdate = profile.copy(
            updatedAt = System.currentTimeMillis(),
            syncStatus = "PENDING"
        )
        dao.insertOrUpdateShopProfile(toUpdate)
        syncManager?.triggerSync()
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
            val remaining = (item.product.stockQuantity - item.quantity).coerceAtLeast(0)
            dao.insertStockTransaction(
                StockTransaction(
                    productId = item.product.id,
                    productName = item.product.name,
                    type = "SALE",
                    quantityChange = -item.quantity,
                    balanceAfter = remaining,
                    reason = "বিক্রি সম্পন্ন (POS Sale)",
                    referenceNo = invoiceNo,
                    unitPrice = item.product.sellingPrice,
                    timestamp = System.currentTimeMillis(),
                    dateString = dateString
                )
            )
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

        syncManager?.triggerSync()
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
            currentDue = updatedDue,
            updatedAt = System.currentTimeMillis(),
            syncStatus = "PENDING"
        )
        dao.updateCustomer(updatedCustomer)

        dao.insertTransaction(
            CustomerTransaction(
                customerId = customer.id,
                type = "পরিশোধ",
                amount = amount,
                note = note.ifBlank { "বাকি আদায়" },
                dateString = dateString,
                timestamp = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                syncStatus = "PENDING"
            )
        )
        syncManager?.triggerSync()
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        dao.clearProducts()
        dao.clearCustomers()
        dao.clearSales()
        dao.clearSaleItems()
        dao.clearTransactions()
        dao.clearExpenses()
        dao.clearStockTransactions()
    }

    suspend fun recordRestock(
        product: Product,
        quantity: Int,
        unitPrice: Double,
        supplierName: String = "",
        supplierPhone: String = "",
        batchNumber: String = "",
        mfgDate: String = "",
        expiryDate: String = "",
        note: String = ""
    ): Product = withContext(Dispatchers.IO) {
        val newQuantity = product.stockQuantity + quantity
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val dateString = dateFormat.format(Date())

        val updatedProduct = product.copy(
            stockQuantity = newQuantity,
            purchasePrice = if (unitPrice > 0) unitPrice else product.purchasePrice,
            supplierName = supplierName.ifBlank { product.supplierName },
            supplierPhone = supplierPhone.ifBlank { product.supplierPhone },
            batchNumber = batchNumber.ifBlank { product.batchNumber },
            manufacturingDate = mfgDate.ifBlank { product.manufacturingDate },
            expiryDate = expiryDate.ifBlank { product.expiryDate },
            isLowStockAlert = newQuantity <= product.lowStockThreshold,
            updatedAt = System.currentTimeMillis(),
            syncStatus = "PENDING"
        )
        dao.updateProduct(updatedProduct)

        dao.insertStockTransaction(
            StockTransaction(
                productId = product.id,
                productName = product.name,
                type = "PURCHASE",
                quantityChange = quantity,
                balanceAfter = newQuantity,
                reason = if (note.isNotBlank()) "রিস্টক: $note" else "নতুন মাল ক্রয় / রিস্টক",
                referenceNo = if (batchNumber.isNotBlank()) "ব্যাচ: $batchNumber" else supplierName.ifBlank { "রিস্টক এন্ট্রি" },
                unitPrice = if (unitPrice > 0) unitPrice else product.purchasePrice,
                timestamp = System.currentTimeMillis(),
                dateString = dateString,
                updatedAt = System.currentTimeMillis(),
                syncStatus = "PENDING"
            )
        )
        syncManager?.triggerSync()
        updatedProduct
    }

    suspend fun recordStockAdjustment(
        product: Product,
        adjustmentType: String, // "STOCK_IN", "STOCK_OUT", "DAMAGED", "LOST", "EXPIRED", "CORRECTION"
        quantity: Int, // magnitude of adjustment
        reason: String = "",
        note: String = ""
    ): Product = withContext(Dispatchers.IO) {
        val isDeduction = adjustmentType in listOf("STOCK_OUT", "DAMAGED", "LOST", "EXPIRED")
        val change = if (isDeduction) -quantity else quantity
        val newStock = (product.stockQuantity + change).coerceAtLeast(0)

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val dateString = dateFormat.format(Date())

        val updatedProduct = product.copy(
            stockQuantity = newStock,
            isLowStockAlert = newStock <= product.lowStockThreshold,
            updatedAt = System.currentTimeMillis(),
            syncStatus = "PENDING"
        )
        dao.updateProduct(updatedProduct)

        val typeLabel = when (adjustmentType) {
            "STOCK_IN" -> "স্টক বৃদ্ধি (+)"
            "STOCK_OUT" -> "স্টক হ্রাস (-)"
            "DAMAGED" -> "নষ্ট পণ্য (Damaged)"
            "LOST" -> "হারিয়ে যাওয়া (Lost)"
            "EXPIRED" -> "মেয়াদ উত্তীর্ণ অপসারণ (Expired)"
            "CORRECTION" -> "হিসাব সংশোধন (Correction)"
            else -> adjustmentType
        }

        dao.insertStockTransaction(
            StockTransaction(
                productId = product.id,
                productName = product.name,
                type = adjustmentType,
                quantityChange = change,
                balanceAfter = newStock,
                reason = if (reason.isNotBlank()) "$typeLabel • $reason" else typeLabel,
                referenceNo = note.ifBlank { "অ্যাডজাস্টমেন্ট" },
                unitPrice = product.purchasePrice,
                timestamp = System.currentTimeMillis(),
                dateString = dateString,
                updatedAt = System.currentTimeMillis(),
                syncStatus = "PENDING"
            )
        )
        syncManager?.triggerSync()
        updatedProduct
    }

    suspend fun bulkImportProducts(productsToImport: List<Product>): Pair<Int, Int> = withContext(Dispatchers.IO) {
        var successCount = 0
        var failureCount = 0
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val dateString = dateFormat.format(Date())

        for (p in productsToImport) {
            try {
                if (p.name.isBlank()) {
                    failureCount++
                    continue
                }
                val newId = dao.insertProduct(p)
                successCount++
                if (p.stockQuantity > 0) {
                    dao.insertStockTransaction(
                        StockTransaction(
                            productId = newId,
                            productName = p.name,
                            type = "INITIAL_IMPORT",
                            quantityChange = p.stockQuantity,
                            balanceAfter = p.stockQuantity,
                            reason = "বাল্ক ইম্পোর্ট (Initial Stock)",
                            referenceNo = p.sku.ifBlank { p.barcode.ifBlank { "CSV Import" } },
                            unitPrice = p.purchasePrice,
                            timestamp = System.currentTimeMillis(),
                            dateString = dateString
                        )
                    )
                }
            } catch (_: Exception) {
                failureCount++
            }
        }
        Pair(successCount, failureCount)
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
                    name = "মেবেলিন ফিট মি ম্যাট ফাউন্ডেশন (Fit Me 128)",
                    sku = "MB-FND-128",
                    category = "মেকআপ (Makeup)",
                    purchasePrice = 720.0,
                    sellingPrice = 1050.0,
                    stockQuantity = 24,
                    sizesOrVariants = "Shade 128 Warm Nude: 30ml",
                    brand = "Maybelline New York",
                    barcode = "8901526401284",
                    unit = "বোতল",
                    wholesalePrice = 680.0,
                    lowStockThreshold = 8,
                    batchNumber = "BATCH-MB2025",
                    manufacturingDate = "2024-06-01",
                    expiryDate = "2027-05-31",
                    supplierName = "গ্লোবাল কসমেটিক্স ইম্পোর্ট",
                    note = "টপ সেলিং ফাউন্ডেশন"
                ),
                Product(
                    name = "দি অর্ডিনারি নায়াসিনামাইড ১০% + জিংক ১%",
                    sku = "TO-NIA-01",
                    category = "স্কিন কেয়ার (Skincare)",
                    purchasePrice = 850.0,
                    sellingPrice = 1250.0,
                    stockQuantity = 18,
                    sizesOrVariants = "30ml Dropper",
                    brand = "The Ordinary",
                    barcode = "8902847103921",
                    unit = "ড্রপার",
                    wholesalePrice = 800.0,
                    lowStockThreshold = 5,
                    batchNumber = "ORD-993A",
                    manufacturingDate = "2024-03-10",
                    expiryDate = "2026-11-20",
                    supplierName = "স্কিনকেয়ার ঢাকা লিমিটেড",
                    note = "অরিজিনাল কানাডা সংস্করণ"
                ),
                Product(
                    name = "ম্যাক রেট্রো ম্যাট লিপস্টিক (Ruby Woo)",
                    sku = "MAC-RW-007",
                    category = "মেকআপ (Makeup)",
                    purchasePrice = 1800.0,
                    sellingPrice = 2450.0,
                    stockQuantity = 4,
                    sizesOrVariants = "3g Red",
                    brand = "M.A.C Cosmetics",
                    barcode = "8903348821094",
                    unit = "পিস",
                    wholesalePrice = 1700.0,
                    lowStockThreshold = 6,
                    batchNumber = "MC-8841",
                    manufacturingDate = "2023-11-15",
                    expiryDate = "2026-10-31",
                    supplierName = "লাক্সারি বিউটি হাব",
                    note = "হট সেলিং শেড • রিঅর্ডার দ্রুত প্রয়োজন",
                    isLowStockAlert = true
                ),
                Product(
                    name = "সেরাভে হাইড্রেটিং ফেসিয়াল ক্লিনজার (236ml)",
                    sku = "CV-CLN-236",
                    category = "স্কিন কেয়ার (Skincare)",
                    purchasePrice = 1150.0,
                    sellingPrice = 1600.0,
                    stockQuantity = 15,
                    sizesOrVariants = "236ml Pump",
                    brand = "CeraVe",
                    barcode = "8905549012378",
                    unit = "বোতল",
                    wholesalePrice = 1080.0,
                    lowStockThreshold = 5,
                    batchNumber = "CV-552B",
                    manufacturingDate = "2024-02-01",
                    expiryDate = "2027-01-31",
                    supplierName = "ইউএস কসমেটিক্স কর্নার",
                    note = "ড্রাই টু নরমাল স্কিন"
                ),
                Product(
                    name = "লরিয়েল প্যারিস এক্সট্রাঅর্ডিনারি অয়েল সিরাম",
                    sku = "LOR-OIL-100",
                    category = "হেয়ার কেয়ার (Hair Care)",
                    purchasePrice = 640.0,
                    sellingPrice = 920.0,
                    stockQuantity = 20,
                    sizesOrVariants = "100ml",
                    brand = "L'Oréal Paris",
                    barcode = "8906612984532",
                    unit = "বোতল",
                    wholesalePrice = 600.0,
                    lowStockThreshold = 6,
                    batchNumber = "LOR-7712",
                    manufacturingDate = "2024-01-10",
                    expiryDate = "2026-12-15",
                    supplierName = "গ্লোবাল কসমেটিক্স ইম্পোর্ট",
                    note = "সিল্কি স্মুথ হেয়ার সিরাম"
                ),
                Product(
                    name = "গার্নিয়ার স্কিন অ্যাক্টিভ মাইসেলার ওয়াটার",
                    sku = "GAR-MIC-400",
                    category = "স্কিন কেয়ার (Skincare)",
                    purchasePrice = 520.0,
                    sellingPrice = 780.0,
                    stockQuantity = 2,
                    sizesOrVariants = "400ml Pink Cap",
                    brand = "Garnier",
                    barcode = "8907723490184",
                    unit = "বোতল",
                    wholesalePrice = 490.0,
                    lowStockThreshold = 8,
                    batchNumber = "GAR-2024",
                    manufacturingDate = "2023-08-01",
                    expiryDate = "2026-07-30",
                    supplierName = "বিউটি ভ্যালি লিমিটেড",
                    note = "কম স্টক সতর্কবার্তা",
                    isLowStockAlert = true
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
