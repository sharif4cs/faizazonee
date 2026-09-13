package com.example

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.ShopDao
import com.example.data.model.Customer
import com.example.data.model.Expense
import com.example.data.model.Product
import com.example.data.repository.SaleCartItem
import com.example.data.repository.ShopRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SyncIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: ShopDao
    private lateinit var repository: ShopRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.shopDao()
        repository = ShopRepository(dao, null)
    }

    @After
    fun tearDown() {
        database.close()
    }

    // 8. Test Product create -> marked PENDING, unique syncId, scoped for Firestore
    @Test
    fun testProductCreate_HasPendingStatusAndSyncId() = runBlocking {
        val product = Product(
            name = "হিমালয়া ফেসওয়াশ",
            sku = "HIM-001",
            category = "স্কিন কেয়ার",
            purchasePrice = 180.0,
            sellingPrice = 250.0,
            stockQuantity = 20,
            shopId = "shop_01798113899"
        )
        val id = repository.insertProduct(product)
        assertTrue(id > 0)

        val pending = dao.getPendingProducts()
        assertEquals(1, pending.size)
        val saved = pending.first()
        assertEquals("হিমালয়া ফেসওয়াশ", saved.name)
        assertEquals("shop_01798113899", saved.shopId)
        assertEquals("PENDING", saved.syncStatus)
        assertFalse(saved.deleted)
        assertTrue(saved.syncId.isNotBlank())
    }

    // 9. Test Product update -> updatedAt refreshed, marked PENDING
    @Test
    fun testProductUpdate_RefreshesUpdatedAtAndSetsPending() = runBlocking {
        val product = Product(
            name = "পন্ডস কোল্ড ক্রিম",
            sku = "PON-001",
            category = "স্কিন কেয়ার",
            purchasePrice = 120.0,
            sellingPrice = 160.0,
            stockQuantity = 15,
            shopId = "shop_01798113899",
            updatedAt = 1000L,
            syncStatus = "SYNCED"
        )
        val id = dao.insertProduct(product)
        val saved = dao.getProductById(id).first()!!

        val updated = saved.copy(
            sellingPrice = 180.0,
            stockQuantity = 25
        )
        repository.updateProduct(updated)

        val afterUpdate = dao.getProductById(id).first()!!
        assertEquals(180.0, afterUpdate.sellingPrice, 0.01)
        assertEquals(25, afterUpdate.stockQuantity)
        assertEquals("PENDING", afterUpdate.syncStatus)
        assertTrue(afterUpdate.updatedAt > 1000L)
    }

    // 10. Test Product delete -> soft delete (deleted = true), marked PENDING
    @Test
    fun testProductDelete_SoftDeletePreservesHistoryAndMarksPending() = runBlocking {
        val product = Product(
            name = "লরিয়েল শ্যাম্পু",
            sku = "LOR-001",
            category = "হেয়ার কেয়ার",
            purchasePrice = 300.0,
            sellingPrice = 420.0,
            stockQuantity = 10,
            shopId = "shop_01798113899"
        )
        val id = repository.insertProduct(product)
        val saved = dao.getProductById(id).first()!!

        repository.deleteProduct(saved)

        // Must NOT appear in active products list
        val activeProducts = dao.getAllProducts().first()
        assertTrue(activeProducts.none { it.id == id })

        // But must exist in database marked as deleted = true with PENDING for cloud synchronization
        val rawProduct = dao.getProductBySyncId(saved.syncId)
        assertNotNull(rawProduct)
        assertTrue(rawProduct!!.deleted)
        assertEquals("PENDING", rawProduct.syncStatus)
    }

    // 11. Test Sale -> Stock update & transactions
    @Test
    fun testSale_DecreasesStockAndRecordsStockTransaction() = runBlocking {
        val product = Product(
            name = "নিভিয়া লোশন",
            sku = "NIV-001",
            category = "বডি কেয়ার",
            purchasePrice = 200.0,
            sellingPrice = 300.0,
            stockQuantity = 50,
            shopId = "shop_01798113899"
        )
        val prodId = repository.insertProduct(product)
        val savedProd = dao.getProductById(prodId).first()!!

        val cartItem = SaleCartItem(
            product = savedProd,
            quantity = 3
        )

        val saleId = repository.completeSale(
            items = listOf(cartItem),
            customer = null,
            customerNameInput = "করিম সাহেব",
            customerPhoneInput = "01811223344",
            discount = 50.0,
            paidAmount = 850.0,
            isCash = true
        )

        assertTrue(saleId > 0)

        // Stock must decrease by 3 (50 - 3 = 47)
        val updatedProd = dao.getProductById(prodId).first()!!
        assertEquals(47, updatedProd.stockQuantity)

        // Sale record must be marked PENDING
        val sales = dao.getAllSales().first()
        assertEquals(1, sales.size)
        val sale = sales.first()
        assertEquals(850.0, sale.paidAmount, 0.01)
        assertEquals(0.0, sale.dueAmount, 0.01)
        assertEquals("PENDING", sale.syncStatus)

        // Stock transaction record created
        val stockTxs = dao.getAllStockTransactions().first()
        assertTrue(stockTxs.any { it.productId == prodId && it.quantityChange == -3 && it.type == "SALE" })
    }

    // 12. Test Customer / Due -> records due and due payments correctly
    @Test
    fun testCustomerDueAndPayment_UpdatesBalancesAndLedger() = runBlocking {
        val customer = Customer(
            name = "রাহিম উল্লাহ",
            phone = "01711223344",
            totalPurchased = 0.0,
            totalPaid = 0.0,
            currentDue = 500.0,
            shopId = "shop_01798113899"
        )
        val custId = repository.insertCustomer(customer)
        val savedCust = dao.getCustomerById(custId).first()!!

        assertEquals(500.0, savedCust.currentDue, 0.01)

        // Pay 300 tk due
        repository.recordDuePayment(savedCust, 300.0, "বিকাশ পেমেন্ট")

        val afterPayment = dao.getCustomerById(custId).first()!!
        assertEquals(200.0, afterPayment.currentDue, 0.01)
        assertEquals(300.0, afterPayment.totalPaid, 0.01)
        assertEquals("PENDING", afterPayment.syncStatus)

        // Transaction ledger record
        val txs = dao.getTransactionsForCustomer(custId).first()
        assertEquals(1, txs.size)
        assertEquals(300.0, txs.first().amount, 0.01)
        assertEquals("পরিশোধ", txs.first().type)
        assertEquals("PENDING", txs.first().syncStatus)
    }

    // 13. Test Expense -> records expense marked PENDING with shopId
    @Test
    fun testExpense_InsertedWithShopIdAndPendingStatus() = runBlocking {
        val expense = Expense(
            title = "দোকানের বিদ্যুৎ বিল",
            category = "বিদ্যুৎ বিল",
            amount = 1450.0,
            dateString = "13 Sep 2026",
            shopId = "shop_01798113899"
        )
        val expId = repository.insertExpense(expense)
        assertTrue(expId > 0)

        val pendingExp = dao.getPendingExpenses()
        assertEquals(1, pendingExp.size)
        assertEquals(1450.0, pendingExp.first().amount, 0.01)
        assertEquals("shop_01798113899", pendingExp.first().shopId)
        assertEquals("PENDING", pendingExp.first().syncStatus)
    }

    // 14 & 15. Conflict resolution: Last write wins based on updatedAt timestamp
    @Test
    fun testConflictResolution_NewerRemoteUpdatedAtWins() = runBlocking {
        val localProduct = Product(
            name = "লোকাল লিপস্টিক",
            sku = "LIP-001",
            category = "লিপ কেয়ার",
            purchasePrice = 100.0,
            sellingPrice = 150.0,
            stockQuantity = 10,
            shopId = "shop_01798113899",
            syncId = "prod_lip_101",
            updatedAt = 2000L,
            syncStatus = "SYNCED"
        )
        dao.insertProduct(localProduct)

        // Remote device updated it at timestamp 5000L
        val remoteUpdatedAt = 5000L
        val existing = dao.getProductBySyncId("prod_lip_101")!!

        if (remoteUpdatedAt > existing.updatedAt) {
            val resolved = existing.copy(
                name = "রিমোট আপডেট লিপস্টিক",
                sellingPrice = 180.0,
                stockQuantity = 8,
                updatedAt = remoteUpdatedAt,
                syncStatus = "SYNCED"
            )
            dao.updateProduct(resolved)
        }

        val finalProd = dao.getProductBySyncId("prod_lip_101")!!
        assertEquals("রিমোট আপডেট লিপস্টিক", finalProd.name)
        assertEquals(180.0, finalProd.sellingPrice, 0.01)
        assertEquals(8, finalProd.stockQuantity)
        assertEquals(5000L, finalProd.updatedAt)
    }
}
