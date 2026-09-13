package com.example.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.example.data.local.ShopDao
import com.example.data.model.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.atomic.AtomicBoolean

class SyncManager(
    private val context: Context,
    private val dao: ShopDao
) {
    companion object {
        private const val TAG = "SyncManager"
    }

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _syncStatus = MutableStateFlow(SyncStatus(state = SyncState.SYNCED))
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private var currentShopId: String = ""
    private var isOnline: Boolean = false
    private val isSyncing = AtomicBoolean(false)

    private val snapshotListeners = mutableListOf<ListenerRegistration>()

    init {
        monitorNetwork()
    }

    private fun monitorNetwork() {
        if (connectivityManager == null) return

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isOnline = true
                Log.d(TAG, "Network became available. Triggering auto-sync.")
                triggerSync()
            }

            override fun onLost(network: Network) {
                isOnline = false
                Log.d(TAG, "Network lost. Switching to OFFLINE mode.")
                _syncStatus.value = _syncStatus.value.copy(state = SyncState.OFFLINE)
            }
        })

        // Initial check
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        isOnline = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        if (!isOnline) {
            _syncStatus.value = _syncStatus.value.copy(state = SyncState.OFFLINE)
        }
    }

    /**
     * Call when user logs in and shopId is confirmed.
     */
    fun startSyncForShop(shopId: String) {
        if (shopId.isBlank()) return
        currentShopId = shopId
        Log.i(TAG, "Starting cloud sync for shopId: $shopId")

        // Attach real-time snapshot listeners for multi-device sync
        attachSnapshotListeners(shopId)

        // Perform initial bidirectional sync
        triggerSync()
    }

    fun stopSync() {
        detachSnapshotListeners()
        currentShopId = ""
    }

    fun getCurrentShopId(): String = currentShopId

    fun triggerSync() {
        if (currentShopId.isBlank()) return
        if (!isOnline) {
            _syncStatus.value = _syncStatus.value.copy(state = SyncState.OFFLINE)
            return
        }

        syncScope.launch {
            performFullSync()
        }
    }

    suspend fun performFullSync() = withContext(Dispatchers.IO) {
        if (!isSyncing.compareAndSet(false, true)) {
            Log.d(TAG, "Sync already in progress. Skipping.")
            return@withContext
        }

        val shopId = currentShopId
        if (shopId.isBlank()) {
            isSyncing.set(false)
            return@withContext
        }

        try {
            _syncStatus.value = _syncStatus.value.copy(state = SyncState.SYNCING)

            // Step 1: Upload all pending local changes to Firestore
            uploadPendingChanges(shopId)

            // Step 2: Download remote changes from Firestore (multi-device downloads)
            downloadRemoteChanges(shopId)

            _syncStatus.value = SyncStatus(
                state = SyncState.SYNCED,
                pendingCount = 0,
                lastSyncTime = System.currentTimeMillis()
            )
            Log.i(TAG, "Full sync completed successfully for shop: $shopId")
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ${e.message}", e)
            _syncStatus.value = SyncStatus(
                state = if (isOnline) SyncState.FAILED else SyncState.OFFLINE,
                errorMessage = e.localizedMessage
            )
        } finally {
            isSyncing.set(false)
        }
    }

    // ==========================================
    // UPLOAD PENDING CHANGES TO FIRESTORE
    // ==========================================
    private suspend fun uploadPendingChanges(shopId: String) {
        val shopRef = firestore.collection("shops").document(shopId)

        // 1. Products
        val pendingProducts = dao.getPendingProducts()
        for (prod in pendingProducts) {
            try {
                val syncId = prod.syncId.ifBlank { "prod_${prod.id}" }
                val doc = prod.toMap(shopId, syncId)
                shopRef.collection("products").document(syncId).set(doc, SetOptions.merge()).await()
                dao.updateProductSyncStatus(prod.id, "SYNCED")
            } catch (e: Exception) {
                Log.e(TAG, "Failed uploading product ${prod.name}: ${e.message}")
            }
        }

        // 2. Customers
        val pendingCustomers = dao.getPendingCustomers()
        for (cust in pendingCustomers) {
            try {
                val syncId = cust.syncId.ifBlank { "cust_${cust.id}" }
                val doc = cust.toMap(shopId, syncId)
                shopRef.collection("customers").document(syncId).set(doc, SetOptions.merge()).await()
                dao.updateCustomerSyncStatus(cust.id, "SYNCED")
            } catch (e: Exception) {
                Log.e(TAG, "Failed uploading customer ${cust.name}: ${e.message}")
            }
        }

        // 3. Sales
        val pendingSales = dao.getPendingSales()
        for (sale in pendingSales) {
            try {
                val syncId = sale.syncId.ifBlank { "sale_${sale.id}" }
                val doc = sale.toMap(shopId, syncId)
                shopRef.collection("sales").document(syncId).set(doc, SetOptions.merge()).await()
                dao.updateSaleSyncStatus(sale.id, "SYNCED")
            } catch (e: Exception) {
                Log.e(TAG, "Failed uploading sale ${sale.invoiceNumber}: ${e.message}")
            }
        }

        // 4. Sale Items
        val pendingItems = dao.getPendingSaleItems()
        for (item in pendingItems) {
            try {
                val syncId = item.syncId.ifBlank { "item_${item.id}" }
                val doc = item.toMap(shopId, syncId)
                shopRef.collection("sale_items").document(syncId).set(doc, SetOptions.merge()).await()
                dao.updateSaleItemSyncStatus(item.id, "SYNCED")
            } catch (e: Exception) {
                Log.e(TAG, "Failed uploading sale item ${item.productName}: ${e.message}")
            }
        }

        // 5. Customer Transactions (Ledger & Due Payments)
        val pendingTxs = dao.getPendingTransactions()
        for (tx in pendingTxs) {
            try {
                val syncId = tx.syncId.ifBlank { "tx_${tx.id}" }
                val doc = tx.toMap(shopId, syncId)
                shopRef.collection("customer_transactions").document(syncId).set(doc, SetOptions.merge()).await()
                dao.updateTransactionSyncStatus(tx.id, "SYNCED")
            } catch (e: Exception) {
                Log.e(TAG, "Failed uploading customer transaction: ${e.message}")
            }
        }

        // 6. Expenses
        val pendingExpenses = dao.getPendingExpenses()
        for (exp in pendingExpenses) {
            try {
                val syncId = exp.syncId.ifBlank { "exp_${exp.id}" }
                val doc = exp.toMap(shopId, syncId)
                shopRef.collection("expenses").document(syncId).set(doc, SetOptions.merge()).await()
                dao.updateExpenseSyncStatus(exp.id, "SYNCED")
            } catch (e: Exception) {
                Log.e(TAG, "Failed uploading expense: ${e.message}")
            }
        }

        // 7. Stock Transactions (Purchases, Restock, Adjustments, Returns)
        val pendingStockTxs = dao.getPendingStockTransactions()
        for (stk in pendingStockTxs) {
            try {
                val syncId = stk.syncId.ifBlank { "stk_${stk.id}" }
                val doc = stk.toMap(shopId, syncId)
                shopRef.collection("stock_transactions").document(syncId).set(doc, SetOptions.merge()).await()
                dao.updateStockTxSyncStatus(stk.id, "SYNCED")
            } catch (e: Exception) {
                Log.e(TAG, "Failed uploading stock transaction: ${e.message}")
            }
        }

        // 8. Shop Profile & Settings
        val pendingProfile = dao.getPendingShopProfile()
        if (pendingProfile != null) {
            try {
                val doc = pendingProfile.toMap(shopId)
                shopRef.collection("profile").document("profile_main").set(doc, SetOptions.merge()).await()
                dao.updateShopProfileSyncStatus(pendingProfile.id, "SYNCED")
            } catch (e: Exception) {
                Log.e(TAG, "Failed uploading shop profile: ${e.message}")
            }
        }
    }

    // ==========================================
    // DOWNLOAD REMOTE CHANGES (Multi-device Sync)
    // ==========================================
    private suspend fun downloadRemoteChanges(shopId: String) {
        val shopRef = firestore.collection("shops").document(shopId)

        // 1. Download Products
        val prodDocs = shopRef.collection("products").get().await()
        for (doc in prodDocs.documents) {
            syncProductFromRemote(doc)
        }

        // 2. Download Customers
        val custDocs = shopRef.collection("customers").get().await()
        for (doc in custDocs.documents) {
            syncCustomerFromRemote(doc)
        }

        // 3. Download Sales
        val saleDocs = shopRef.collection("sales").get().await()
        for (doc in saleDocs.documents) {
            syncSaleFromRemote(doc)
        }

        // 4. Download Sale Items
        val itemDocs = shopRef.collection("sale_items").get().await()
        for (doc in itemDocs.documents) {
            syncSaleItemFromRemote(doc)
        }

        // 5. Download Customer Transactions
        val txDocs = shopRef.collection("customer_transactions").get().await()
        for (doc in txDocs.documents) {
            syncCustomerTxFromRemote(doc)
        }

        // 6. Download Expenses
        val expDocs = shopRef.collection("expenses").get().await()
        for (doc in expDocs.documents) {
            syncExpenseFromRemote(doc)
        }

        // 7. Download Stock Transactions
        val stkDocs = shopRef.collection("stock_transactions").get().await()
        for (doc in stkDocs.documents) {
            syncStockTxFromRemote(doc)
        }

        // 8. Download Shop Profile
        val profileDoc = shopRef.collection("profile").document("profile_main").get().await()
        if (profileDoc.exists()) {
            syncShopProfileFromRemote(profileDoc)
        }
    }

    // ==========================================
    // REAL-TIME SNAPSHOT LISTENERS
    // ==========================================
    private fun attachSnapshotListeners(shopId: String) {
        detachSnapshotListeners()
        val shopRef = firestore.collection("shops").document(shopId)

        // Listen for live Product changes
        val prodReg = shopRef.collection("products").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            syncScope.launch {
                for (change in snapshot.documentChanges) {
                    syncProductFromRemote(change.document)
                }
            }
        }
        snapshotListeners.add(prodReg)

        // Listen for live Customer changes
        val custReg = shopRef.collection("customers").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            syncScope.launch {
                for (change in snapshot.documentChanges) {
                    syncCustomerFromRemote(change.document)
                }
            }
        }
        snapshotListeners.add(custReg)

        // Listen for live Sales
        val saleReg = shopRef.collection("sales").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            syncScope.launch {
                for (change in snapshot.documentChanges) {
                    syncSaleFromRemote(change.document)
                }
            }
        }
        snapshotListeners.add(saleReg)

        // Listen for live Expenses
        val expReg = shopRef.collection("expenses").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            syncScope.launch {
                for (change in snapshot.documentChanges) {
                    syncExpenseFromRemote(change.document)
                }
            }
        }
        snapshotListeners.add(expReg)

        // Listen for live Stock Transactions
        val stkReg = shopRef.collection("stock_transactions").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            syncScope.launch {
                for (change in snapshot.documentChanges) {
                    syncStockTxFromRemote(change.document)
                }
            }
        }
        snapshotListeners.add(stkReg)

        // Listen for live Customer Transactions
        val txReg = shopRef.collection("customer_transactions").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            syncScope.launch {
                for (change in snapshot.documentChanges) {
                    syncCustomerTxFromRemote(change.document)
                }
            }
        }
        snapshotListeners.add(txReg)
    }

    private fun detachSnapshotListeners() {
        for (listener in snapshotListeners) {
            try {
                listener.remove()
            } catch (_: Exception) {}
        }
        snapshotListeners.clear()
    }

    // ==========================================
    // REMOTE CONFLICT RESOLUTION & DESERIALIZATION
    // ==========================================
    private suspend fun syncProductFromRemote(doc: DocumentSnapshot) {
        val syncId = doc.getString("syncId") ?: doc.id
        val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
        val remoteDeleted = doc.getBoolean("deleted") ?: false

        val local = dao.getProductBySyncId(syncId)
        if (local != null) {
            // Conflict handling using updatedAt timestamps
            if (remoteUpdatedAt > local.updatedAt) {
                val updated = local.copy(
                    name = doc.getString("name") ?: local.name,
                    sku = doc.getString("sku") ?: local.sku,
                    category = doc.getString("category") ?: local.category,
                    purchasePrice = doc.getDouble("purchasePrice") ?: local.purchasePrice,
                    sellingPrice = doc.getDouble("sellingPrice") ?: local.sellingPrice,
                    stockQuantity = doc.getLong("stockQuantity")?.toInt() ?: local.stockQuantity,
                    sizesOrVariants = doc.getString("sizesOrVariants") ?: local.sizesOrVariants,
                    imageUrl = doc.getString("imageUrl") ?: local.imageUrl,
                    isLowStockAlert = doc.getBoolean("isLowStockAlert") ?: local.isLowStockAlert,
                    brand = doc.getString("brand") ?: local.brand,
                    lowStockThreshold = doc.getLong("lowStockThreshold")?.toInt() ?: local.lowStockThreshold,
                    note = doc.getString("note") ?: local.note,
                    barcode = doc.getString("barcode") ?: local.barcode,
                    unit = doc.getString("unit") ?: local.unit,
                    wholesalePrice = doc.getDouble("wholesalePrice") ?: local.wholesalePrice,
                    discount = doc.getDouble("discount") ?: local.discount,
                    taxRate = doc.getDouble("taxRate") ?: local.taxRate,
                    openingStock = doc.getLong("openingStock")?.toInt() ?: local.openingStock,
                    supplierName = doc.getString("supplierName") ?: local.supplierName,
                    supplierPhone = doc.getString("supplierPhone") ?: local.supplierPhone,
                    supplierId = doc.getString("supplierId") ?: local.supplierId,
                    batchNumber = doc.getString("batchNumber") ?: local.batchNumber,
                    manufacturingDate = doc.getString("manufacturingDate") ?: local.manufacturingDate,
                    expiryDate = doc.getString("expiryDate") ?: local.expiryDate,
                    shopId = doc.getString("shopId") ?: local.shopId,
                    updatedAt = remoteUpdatedAt,
                    deleted = remoteDeleted,
                    syncStatus = "SYNCED"
                )
                dao.updateProduct(updated)
            }
        } else {
            // New record from another device
            val newProduct = Product(
                id = 0, // Auto-generate local SQLite ID
                name = doc.getString("name") ?: "",
                sku = doc.getString("sku") ?: "",
                category = doc.getString("category") ?: "",
                purchasePrice = doc.getDouble("purchasePrice") ?: 0.0,
                sellingPrice = doc.getDouble("sellingPrice") ?: 0.0,
                stockQuantity = doc.getLong("stockQuantity")?.toInt() ?: 0,
                sizesOrVariants = doc.getString("sizesOrVariants") ?: "Standard",
                imageUrl = doc.getString("imageUrl") ?: "",
                isLowStockAlert = doc.getBoolean("isLowStockAlert") ?: false,
                brand = doc.getString("brand") ?: "",
                lowStockThreshold = doc.getLong("lowStockThreshold")?.toInt() ?: 5,
                note = doc.getString("note") ?: "",
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                barcode = doc.getString("barcode") ?: "",
                unit = doc.getString("unit") ?: "পিস",
                wholesalePrice = doc.getDouble("wholesalePrice") ?: 0.0,
                discount = doc.getDouble("discount") ?: 0.0,
                taxRate = doc.getDouble("taxRate") ?: 0.0,
                openingStock = doc.getLong("openingStock")?.toInt() ?: 0,
                supplierName = doc.getString("supplierName") ?: "",
                supplierPhone = doc.getString("supplierPhone") ?: "",
                supplierId = doc.getString("supplierId") ?: "",
                batchNumber = doc.getString("batchNumber") ?: "",
                manufacturingDate = doc.getString("manufacturingDate") ?: "",
                expiryDate = doc.getString("expiryDate") ?: "",
                syncId = syncId,
                shopId = doc.getString("shopId") ?: currentShopId,
                updatedAt = remoteUpdatedAt,
                deleted = remoteDeleted,
                syncStatus = "SYNCED"
            )
            dao.insertProduct(newProduct)
        }
    }

    private suspend fun syncCustomerFromRemote(doc: DocumentSnapshot) {
        val syncId = doc.getString("syncId") ?: doc.id
        val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
        val remoteDeleted = doc.getBoolean("deleted") ?: false

        val local = dao.getCustomerBySyncId(syncId)
        if (local != null) {
            if (remoteUpdatedAt > local.updatedAt) {
                val updated = local.copy(
                    name = doc.getString("name") ?: local.name,
                    phone = doc.getString("phone") ?: local.phone,
                    address = doc.getString("address") ?: local.address,
                    totalPurchased = doc.getDouble("totalPurchased") ?: local.totalPurchased,
                    totalPaid = doc.getDouble("totalPaid") ?: local.totalPaid,
                    currentDue = doc.getDouble("currentDue") ?: local.currentDue,
                    lastSaleDate = doc.getString("lastSaleDate") ?: local.lastSaleDate,
                    shopId = doc.getString("shopId") ?: local.shopId,
                    updatedAt = remoteUpdatedAt,
                    deleted = remoteDeleted,
                    syncStatus = "SYNCED"
                )
                dao.updateCustomer(updated)
            }
        } else {
            val newCust = Customer(
                id = 0,
                name = doc.getString("name") ?: "",
                phone = doc.getString("phone") ?: "",
                address = doc.getString("address") ?: "",
                totalPurchased = doc.getDouble("totalPurchased") ?: 0.0,
                totalPaid = doc.getDouble("totalPaid") ?: 0.0,
                currentDue = doc.getDouble("currentDue") ?: 0.0,
                lastSaleDate = doc.getString("lastSaleDate") ?: "",
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                syncId = syncId,
                shopId = doc.getString("shopId") ?: currentShopId,
                updatedAt = remoteUpdatedAt,
                deleted = remoteDeleted,
                syncStatus = "SYNCED"
            )
            dao.insertCustomer(newCust)
        }
    }

    private suspend fun syncSaleFromRemote(doc: DocumentSnapshot) {
        val syncId = doc.getString("syncId") ?: doc.id
        val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
        val remoteDeleted = doc.getBoolean("deleted") ?: false

        val local = dao.getSaleBySyncId(syncId)
        if (local == null) {
            val newSale = Sale(
                id = 0,
                invoiceNumber = doc.getString("invoiceNumber") ?: "",
                customerId = doc.getLong("customerId"),
                customerName = doc.getString("customerName") ?: "",
                customerPhone = doc.getString("customerPhone") ?: "",
                subtotal = doc.getDouble("subtotal") ?: 0.0,
                discount = doc.getDouble("discount") ?: 0.0,
                paidAmount = doc.getDouble("paidAmount") ?: 0.0,
                dueAmount = doc.getDouble("dueAmount") ?: 0.0,
                paymentType = doc.getString("paymentType") ?: "CASH",
                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                dateString = doc.getString("dateString") ?: "",
                syncId = syncId,
                customerSyncId = doc.getString("customerSyncId") ?: "",
                shopId = doc.getString("shopId") ?: currentShopId,
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = remoteUpdatedAt,
                deleted = remoteDeleted,
                syncStatus = "SYNCED"
            )
            dao.insertSale(newSale)
        }
    }

    private suspend fun syncSaleItemFromRemote(doc: DocumentSnapshot) {
        val syncId = doc.getString("syncId") ?: doc.id
        val local = dao.getSaleItemBySyncId(syncId)
        if (local == null) {
            val newItem = SaleItem(
                id = 0,
                saleId = doc.getLong("saleId") ?: 0L,
                productId = doc.getLong("productId") ?: 0L,
                productName = doc.getString("productName") ?: "",
                variant = doc.getString("variant") ?: "",
                unitPrice = doc.getDouble("unitPrice") ?: 0.0,
                quantity = doc.getLong("quantity")?.toInt() ?: 1,
                totalPrice = doc.getDouble("totalPrice") ?: 0.0,
                syncId = syncId,
                saleSyncId = doc.getString("saleSyncId") ?: "",
                productSyncId = doc.getString("productSyncId") ?: "",
                shopId = doc.getString("shopId") ?: currentShopId,
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
                deleted = doc.getBoolean("deleted") ?: false,
                syncStatus = "SYNCED"
            )
            dao.insertSaleItems(listOf(newItem))
        }
    }

    private suspend fun syncCustomerTxFromRemote(doc: DocumentSnapshot) {
        val syncId = doc.getString("syncId") ?: doc.id
        val local = dao.getCustomerTransactionBySyncId(syncId)
        if (local == null) {
            val newTx = CustomerTransaction(
                id = 0,
                customerId = doc.getLong("customerId") ?: 0L,
                type = doc.getString("type") ?: "বিক্রি",
                amount = doc.getDouble("amount") ?: 0.0,
                note = doc.getString("note") ?: "",
                dateString = doc.getString("dateString") ?: "",
                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                syncId = syncId,
                customerSyncId = doc.getString("customerSyncId") ?: "",
                shopId = doc.getString("shopId") ?: currentShopId,
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
                deleted = doc.getBoolean("deleted") ?: false,
                syncStatus = "SYNCED"
            )
            dao.insertTransaction(newTx)
        }
    }

    private suspend fun syncExpenseFromRemote(doc: DocumentSnapshot) {
        val syncId = doc.getString("syncId") ?: doc.id
        val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
        val remoteDeleted = doc.getBoolean("deleted") ?: false

        val local = dao.getExpenseBySyncId(syncId)
        if (local != null) {
            if (remoteUpdatedAt > local.updatedAt) {
                val updated = local.copy(
                    category = doc.getString("category") ?: local.category,
                    customCategory = doc.getString("customCategory") ?: local.customCategory,
                    amount = doc.getDouble("amount") ?: local.amount,
                    paymentMethod = doc.getString("paymentMethod") ?: local.paymentMethod,
                    note = doc.getString("note") ?: local.note,
                    title = doc.getString("title") ?: local.title,
                    dateString = doc.getString("dateString") ?: local.dateString,
                    shopId = doc.getString("shopId") ?: local.shopId,
                    updatedAt = remoteUpdatedAt,
                    deleted = remoteDeleted,
                    syncStatus = "SYNCED"
                )
                dao.updateExpense(updated)
            }
        } else {
            val newExp = Expense(
                id = 0,
                category = doc.getString("category") ?: "অন্যান্য",
                customCategory = doc.getString("customCategory") ?: "",
                amount = doc.getDouble("amount") ?: 0.0,
                paymentMethod = doc.getString("paymentMethod") ?: "নগদ ক্যাশ",
                note = doc.getString("note") ?: "",
                title = doc.getString("title") ?: "",
                dateString = doc.getString("dateString") ?: "",
                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                syncId = syncId,
                shopId = doc.getString("shopId") ?: currentShopId,
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = remoteUpdatedAt,
                deleted = remoteDeleted,
                syncStatus = "SYNCED"
            )
            dao.insertExpense(newExp)
        }
    }

    private suspend fun syncStockTxFromRemote(doc: DocumentSnapshot) {
        val syncId = doc.getString("syncId") ?: doc.id
        val local = dao.getStockTransactionBySyncId(syncId)
        if (local == null) {
            val newStk = StockTransaction(
                id = 0,
                productId = doc.getLong("productId") ?: 0L,
                productName = doc.getString("productName") ?: "",
                type = doc.getString("type") ?: "PURCHASE",
                quantityChange = doc.getLong("quantityChange")?.toInt() ?: 0,
                balanceAfter = doc.getLong("balanceAfter")?.toInt() ?: 0,
                reason = doc.getString("reason") ?: "",
                referenceNo = doc.getString("referenceNo") ?: "",
                unitPrice = doc.getDouble("unitPrice") ?: 0.0,
                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                dateString = doc.getString("dateString") ?: "",
                syncId = syncId,
                productSyncId = doc.getString("productSyncId") ?: "",
                shopId = doc.getString("shopId") ?: currentShopId,
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
                deleted = doc.getBoolean("deleted") ?: false,
                syncStatus = "SYNCED"
            )
            dao.insertStockTransaction(newStk)
        }
    }

    private suspend fun syncShopProfileFromRemote(doc: DocumentSnapshot) {
        val profile = ShopProfile(
            id = 1,
            shopName = doc.getString("shopName") ?: "ফাইজা স্টোর",
            subtitle = doc.getString("subtitle") ?: "স্মার্ট শপ ম্যানেজমেন্ট ও পিওএস",
            ownerName = doc.getString("ownerName") ?: "মোঃ শরিফ",
            email = doc.getString("email") ?: "",
            phone = doc.getString("phone") ?: "",
            address = doc.getString("address") ?: "",
            openingCash = doc.getDouble("openingCash") ?: 0.0,
            openingBkash = doc.getDouble("openingBkash") ?: 0.0,
            openingNagad = doc.getDouble("openingNagad") ?: 0.0,
            openingBank = doc.getDouble("openingBank") ?: 0.0,
            currencySymbol = doc.getString("currencySymbol") ?: "৳",
            syncId = "profile_main",
            shopId = doc.getString("shopId") ?: currentShopId,
            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
            updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
            deleted = doc.getBoolean("deleted") ?: false,
            syncStatus = "SYNCED"
        )
        dao.insertOrUpdateShopProfile(profile)
    }

    // ==========================================
    // EXTENSION TO MAP FOR FIRESTORE
    // ==========================================
    private fun Product.toMap(shopId: String, syncId: String): Map<String, Any?> = mapOf(
        "id" to syncId,
        "syncId" to syncId,
        "shopId" to shopId,
        "name" to name,
        "sku" to sku,
        "category" to category,
        "purchasePrice" to purchasePrice,
        "sellingPrice" to sellingPrice,
        "stockQuantity" to stockQuantity,
        "sizesOrVariants" to sizesOrVariants,
        "imageUrl" to imageUrl,
        "isLowStockAlert" to isLowStockAlert,
        "brand" to brand,
        "lowStockThreshold" to lowStockThreshold,
        "note" to note,
        "createdAt" to createdAt,
        "barcode" to barcode,
        "unit" to unit,
        "wholesalePrice" to wholesalePrice,
        "discount" to discount,
        "taxRate" to taxRate,
        "openingStock" to openingStock,
        "supplierName" to supplierName,
        "supplierPhone" to supplierPhone,
        "supplierId" to supplierId,
        "batchNumber" to batchNumber,
        "manufacturingDate" to manufacturingDate,
        "expiryDate" to expiryDate,
        "updatedAt" to updatedAt,
        "deleted" to deleted,
        "syncStatus" to "SYNCED"
    )

    private fun Customer.toMap(shopId: String, syncId: String): Map<String, Any?> = mapOf(
        "id" to syncId,
        "syncId" to syncId,
        "shopId" to shopId,
        "name" to name,
        "phone" to phone,
        "address" to address,
        "totalPurchased" to totalPurchased,
        "totalPaid" to totalPaid,
        "currentDue" to currentDue,
        "lastSaleDate" to lastSaleDate,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
        "deleted" to deleted,
        "syncStatus" to "SYNCED"
    )

    private fun Sale.toMap(shopId: String, syncId: String): Map<String, Any?> = mapOf(
        "id" to syncId,
        "syncId" to syncId,
        "shopId" to shopId,
        "invoiceNumber" to invoiceNumber,
        "customerId" to customerId,
        "customerName" to customerName,
        "customerPhone" to customerPhone,
        "subtotal" to subtotal,
        "discount" to discount,
        "paidAmount" to paidAmount,
        "dueAmount" to dueAmount,
        "paymentType" to paymentType,
        "timestamp" to timestamp,
        "dateString" to dateString,
        "customerSyncId" to customerSyncId,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
        "deleted" to deleted,
        "syncStatus" to "SYNCED"
    )

    private fun SaleItem.toMap(shopId: String, syncId: String): Map<String, Any?> = mapOf(
        "id" to syncId,
        "syncId" to syncId,
        "shopId" to shopId,
        "saleId" to saleId,
        "productId" to productId,
        "productName" to productName,
        "variant" to variant,
        "unitPrice" to unitPrice,
        "quantity" to quantity,
        "totalPrice" to totalPrice,
        "saleSyncId" to saleSyncId,
        "productSyncId" to productSyncId,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
        "deleted" to deleted,
        "syncStatus" to "SYNCED"
    )

    private fun CustomerTransaction.toMap(shopId: String, syncId: String): Map<String, Any?> = mapOf(
        "id" to syncId,
        "syncId" to syncId,
        "shopId" to shopId,
        "customerId" to customerId,
        "type" to type,
        "amount" to amount,
        "note" to note,
        "dateString" to dateString,
        "timestamp" to timestamp,
        "customerSyncId" to customerSyncId,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
        "deleted" to deleted,
        "syncStatus" to "SYNCED"
    )

    private fun Expense.toMap(shopId: String, syncId: String): Map<String, Any?> = mapOf(
        "id" to syncId,
        "syncId" to syncId,
        "shopId" to shopId,
        "category" to category,
        "customCategory" to customCategory,
        "amount" to amount,
        "paymentMethod" to paymentMethod,
        "note" to note,
        "title" to title,
        "dateString" to dateString,
        "timestamp" to timestamp,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
        "deleted" to deleted,
        "syncStatus" to "SYNCED"
    )

    private fun StockTransaction.toMap(shopId: String, syncId: String): Map<String, Any?> = mapOf(
        "id" to syncId,
        "syncId" to syncId,
        "shopId" to shopId,
        "productId" to productId,
        "productName" to productName,
        "type" to type,
        "quantityChange" to quantityChange,
        "balanceAfter" to balanceAfter,
        "reason" to reason,
        "referenceNo" to referenceNo,
        "unitPrice" to unitPrice,
        "timestamp" to timestamp,
        "dateString" to dateString,
        "productSyncId" to productSyncId,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
        "deleted" to deleted,
        "syncStatus" to "SYNCED"
    )

    private fun ShopProfile.toMap(shopId: String): Map<String, Any?> = mapOf(
        "id" to "profile_main",
        "syncId" to "profile_main",
        "shopId" to shopId,
        "shopName" to shopName,
        "subtitle" to subtitle,
        "ownerName" to ownerName,
        "email" to email,
        "phone" to phone,
        "address" to address,
        "openingCash" to openingCash,
        "openingBkash" to openingBkash,
        "openingNagad" to openingNagad,
        "openingBank" to openingBank,
        "currencySymbol" to currencySymbol,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
        "deleted" to deleted,
        "syncStatus" to "SYNCED"
    )
}
