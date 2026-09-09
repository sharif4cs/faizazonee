package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Customer
import com.example.data.model.CustomerTransaction
import com.example.data.model.Expense
import com.example.data.model.Product
import com.example.data.model.Sale
import com.example.data.model.ShopProfile
import com.example.data.repository.SaleCartItem
import com.example.data.repository.ShopRepository
import com.example.security.AuthManager
import com.example.security.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ScreenTab {
    DASHBOARD,
    POS,
    STOCK,
    BAKI,
    MENU
}

enum class SubScreen {
    NONE,
    CART_CHECKOUT,
    CUSTOMER_DETAIL,
    REPORTS,
    EXPENSE_HISTORY
}

data class AccountBalances(
    val cash: Double = 0.0,
    val bkash: Double = 0.0,
    val nagad: Double = 0.0,
    val bank: Double = 0.0
) {
    val total: Double get() = cash + bkash + nagad + bank
}

enum class CustomerFilter {
    ALL,
    HAS_DUE,
    PAID
}

class ShopViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ShopRepository

    private val prefs = application.getSharedPreferences("shop_app_prefs", android.content.Context.MODE_PRIVATE)
    private val authManager = AuthManager(application)

    // Authentication / Login State
    private val _isLoggedIn = MutableStateFlow(authManager.isUserLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun getRegisteredPhone(): String = authManager.getRegisteredPhone()

    suspend fun authenticateOnline(phone: String, pin: String): AuthResult {
        val result = authManager.authenticateOnline(phone, pin)
        if (result is AuthResult.Success) {
            _isLoggedIn.value = true
        }
        return result
    }

    fun isDeviceOnline(): Boolean = authManager.isNetworkConnected()

    suspend fun verifyServerOnline(): Boolean = authManager.verifyOnlineServerReachability()

    fun updatePin(currentPin: String, newPin: String): Boolean {
        return authManager.updateOwnerPin(currentPin, newPin)
    }

    fun logout() {
        authManager.setLoggedIn(false)
        _isLoggedIn.value = false
    }

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ShopRepository(db.shopDao())
        viewModelScope.launch {
            val stockSeeded = prefs.getBoolean("faiza_stock_seeded_v3", false)
            if (!stockSeeded) {
                repository.seedInitialStockIfEmpty()
                prefs.edit().putBoolean("faiza_stock_seeded_v3", true).apply()
            }
            repository.initializeShopProfileIfNeeded(
                defaultName = "ফাইজা স্টোর",
                defaultOwner = "মোঃ শরিফ"
            )
        }
    }

    // Navigation State
    private val _currentTab = MutableStateFlow(ScreenTab.DASHBOARD)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    private val _activeSubScreen = MutableStateFlow(SubScreen.NONE)
    val activeSubScreen: StateFlow<SubScreen> = _activeSubScreen.asStateFlow()

    fun navigateToTab(tab: ScreenTab) {
        _activeSubScreen.value = SubScreen.NONE
        _currentTab.value = tab
    }

    fun openCartCheckout() {
        _activeSubScreen.value = SubScreen.CART_CHECKOUT
    }

    fun openCustomerDetail(customer: Customer) {
        _selectedCustomer.value = customer
        _activeSubScreen.value = SubScreen.CUSTOMER_DETAIL
    }

    fun openReports() {
        _activeSubScreen.value = SubScreen.REPORTS
    }

    fun openExpenseHistory() {
        _activeSubScreen.value = SubScreen.EXPENSE_HISTORY
    }

    fun closeSubScreen() {
        _activeSubScreen.value = SubScreen.NONE
    }

    // Data streams from repository
    val allProducts = repository.allProducts.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allCustomers = repository.allCustomers.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allSales = repository.allSales.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allExpenses = repository.allExpenses.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allTransactions = repository.allTransactions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val shopProfile = repository.shopProfile.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), ShopProfile()
    )

    val accountBalances: StateFlow<AccountBalances> = combine(
        shopProfile,
        allSales,
        allTransactions,
        allExpenses
    ) { profile, sales, txs, expenses ->
        val p = profile ?: ShopProfile()
        val cashSales = sales.filter { it.paymentType == "CASH" }.sumOf { it.paidAmount }
        val customerPayments = txs.filter { it.type == "পরিশোধ" }.sumOf { it.amount }
        val cashExpenses = expenses.filter { 
            it.paymentMethod.contains("ক্যাশ") || it.paymentMethod.contains("Cash", ignoreCase = true) 
        }.sumOf { it.amount }
        
        val bkashExpenses = expenses.filter { 
            it.paymentMethod.contains("bKash", ignoreCase = true) || it.paymentMethod.contains("বিকাশ") 
        }.sumOf { it.amount }
        
        val nagadExpenses = expenses.filter { 
            (it.paymentMethod.contains("Nagad", ignoreCase = true) || it.paymentMethod.contains("নগদ")) && !it.paymentMethod.contains("ক্যাশ") 
        }.sumOf { it.amount }
        
        val bankExpenses = expenses.filter { 
            it.paymentMethod.contains("ব্যাংক") || it.paymentMethod.contains("Bank", ignoreCase = true) 
        }.sumOf { it.amount }

        AccountBalances(
            cash = (p.openingCash + cashSales + customerPayments - cashExpenses).coerceAtLeast(0.0),
            bkash = (p.openingBkash - bkashExpenses).coerceAtLeast(0.0),
            nagad = (p.openingNagad - nagadExpenses).coerceAtLeast(0.0),
            bank = (p.openingBank - bankExpenses).coerceAtLeast(0.0)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AccountBalances()
    )

    private val _editingExpense = MutableStateFlow<Expense?>(null)
    val editingExpense: StateFlow<Expense?> = _editingExpense.asStateFlow()

    fun openAddExpense() {
        _editingExpense.value = null
        _showAddExpenseDialog.value = true
    }

    fun openEditExpense(expense: Expense) {
        _editingExpense.value = expense
        _showAddExpenseDialog.value = true
    }

    // Customer Detail state
    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    fun selectCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
    }

    fun getTransactionsForCustomer(customerId: Long): StateFlow<List<CustomerTransaction>> {
        return repository.getTransactionsForCustomer(customerId).stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
    }

    // POS & Cart state
    private val _cart = MutableStateFlow<List<SaleCartItem>>(emptyList())
    val cart: StateFlow<List<SaleCartItem>> = _cart.asStateFlow()

    private val _posSearch = MutableStateFlow("")
    val posSearch: StateFlow<String> = _posSearch.asStateFlow()

    private val _posCategory = MutableStateFlow("সব")
    val posCategory: StateFlow<String> = _posCategory.asStateFlow()

    fun setPosSearch(query: String) {
        _posSearch.value = query
    }

    fun setPosCategory(category: String) {
        _posCategory.value = category
    }

    fun addToCart(product: Product, variant: String = "M") {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id && it.variant == variant }
        if (index >= 0) {
            val existing = currentList[index]
            currentList[index] = existing.copy(quantity = existing.quantity + 1)
        } else {
            currentList.add(SaleCartItem(product = product, variant = variant, quantity = 1))
        }
        _cart.value = currentList
    }

    fun updateCartItemQuantity(item: SaleCartItem, change: Int) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOf(item)
        if (index >= 0) {
            val newQty = item.quantity + change
            if (newQty <= 0) {
                currentList.removeAt(index)
            } else {
                currentList[index] = item.copy(quantity = newQty)
            }
            _cart.value = currentList
        }
    }

    fun removeCartItem(item: SaleCartItem) {
        _cart.value = _cart.value.filter { it != item }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    // Cart Checkout Form
    private val _checkoutCustomerName = MutableStateFlow("")
    val checkoutCustomerName: StateFlow<String> = _checkoutCustomerName.asStateFlow()

    private val _checkoutCustomerPhone = MutableStateFlow("")
    val checkoutCustomerPhone: StateFlow<String> = _checkoutCustomerPhone.asStateFlow()

    private val _checkoutSelectedCustomer = MutableStateFlow<Customer?>(null)
    val checkoutSelectedCustomer: StateFlow<Customer?> = _checkoutSelectedCustomer.asStateFlow()

    private val _checkoutIsCash = MutableStateFlow(true)
    val checkoutIsCash: StateFlow<Boolean> = _checkoutIsCash.asStateFlow()

    private val _checkoutDiscount = MutableStateFlow(0.0)
    val checkoutDiscount: StateFlow<Double> = _checkoutDiscount.asStateFlow()

    private val _checkoutPaidAmount = MutableStateFlow("")
    val checkoutPaidAmount: StateFlow<String> = _checkoutPaidAmount.asStateFlow()

    fun setCheckoutCustomerName(name: String) {
        _checkoutCustomerName.value = name
    }

    fun setCheckoutCustomerPhone(phone: String) {
        _checkoutCustomerPhone.value = phone
    }

    fun setCheckoutSelectedCustomer(customer: Customer?) {
        _checkoutSelectedCustomer.value = customer
        if (customer != null) {
            _checkoutCustomerName.value = customer.name
            _checkoutCustomerPhone.value = customer.phone
        }
    }

    fun setCheckoutIsCash(isCash: Boolean) {
        _checkoutIsCash.value = isCash
    }

    fun setCheckoutDiscount(discount: Double) {
        _checkoutDiscount.value = discount
    }

    fun setCheckoutPaidAmount(amount: String) {
        _checkoutPaidAmount.value = amount
    }

    // Last completed sale dialog
    private val _completedSaleInfo = MutableStateFlow<CompletedSaleSummary?>(null)
    val completedSaleInfo: StateFlow<CompletedSaleSummary?> = _completedSaleInfo.asStateFlow()

    fun dismissCompletedSale() {
        _completedSaleInfo.value = null
    }

    fun completeCheckout() {
        val items = _cart.value
        if (items.isEmpty()) return

        val totalSubtotal = items.sumOf { it.product.sellingPrice * it.quantity }
        val discount = _checkoutDiscount.value
        val netTotal = (totalSubtotal - discount).coerceAtLeast(0.0)
        val enteredPaid = _checkoutPaidAmount.value.toDoubleOrNull() ?: if (_checkoutIsCash.value) netTotal else 0.0

        viewModelScope.launch {
            val saleId = repository.completeSale(
                items = items,
                customer = _checkoutSelectedCustomer.value,
                customerNameInput = _checkoutCustomerName.value,
                customerPhoneInput = _checkoutCustomerPhone.value,
                discount = discount,
                paidAmount = enteredPaid,
                isCash = _checkoutIsCash.value
            )

            val invoiceNo = "INV-" + (System.currentTimeMillis() % 1000000)
            val customerName = _checkoutCustomerName.value.ifBlank {
                _checkoutSelectedCustomer.value?.name ?: "নগদ ক্রেতা"
            }

            _completedSaleInfo.value = CompletedSaleSummary(
                invoiceNo = invoiceNo,
                customerName = customerName,
                itemCount = items.sumOf { it.quantity },
                subtotal = totalSubtotal,
                discount = discount,
                netTotal = netTotal,
                paidAmount = enteredPaid,
                dueAmount = (netTotal - enteredPaid).coerceAtLeast(0.0)
            )

            clearCart()
            _checkoutDiscount.value = 0.0
            _checkoutCustomerName.value = ""
            _checkoutCustomerPhone.value = ""
            _checkoutSelectedCustomer.value = null
            _checkoutPaidAmount.value = ""
            _checkoutIsCash.value = true
            _activeSubScreen.value = SubScreen.NONE
        }
    }

    // Stock Screen state
    private val _stockSearch = MutableStateFlow("")
    val stockSearch: StateFlow<String> = _stockSearch.asStateFlow()

    private val _stockCategory = MutableStateFlow("সব")
    val stockCategory: StateFlow<String> = _stockCategory.asStateFlow()

    fun setStockSearch(query: String) {
        _stockSearch.value = query
    }

    fun setStockCategory(category: String) {
        _stockCategory.value = category
    }

    // Customer Screen state
    private val _customerSearch = MutableStateFlow("")
    val customerSearch: StateFlow<String> = _customerSearch.asStateFlow()

    private val _customerFilter = MutableStateFlow(CustomerFilter.ALL)
    val customerFilter: StateFlow<CustomerFilter> = _customerFilter.asStateFlow()

    fun setCustomerSearch(query: String) {
        _customerSearch.value = query
    }

    fun setCustomerFilter(filter: CustomerFilter) {
        _customerFilter.value = filter
    }

    // Dialogs state
    private val _showAddProductDialog = MutableStateFlow(false)
    val showAddProductDialog: StateFlow<Boolean> = _showAddProductDialog.asStateFlow()

    private val _editingProduct = MutableStateFlow<Product?>(null)
    val editingProduct: StateFlow<Product?> = _editingProduct.asStateFlow()

    private val _showAddCustomerDialog = MutableStateFlow(false)
    val showAddCustomerDialog: StateFlow<Boolean> = _showAddCustomerDialog.asStateFlow()

    private val _showAddExpenseDialog = MutableStateFlow(false)
    val showAddExpenseDialog: StateFlow<Boolean> = _showAddExpenseDialog.asStateFlow()

    private val _showCollectDueDialog = MutableStateFlow(false)
    val showCollectDueDialog: StateFlow<Boolean> = _showCollectDueDialog.asStateFlow()

    private val _showOpeningCashDialog = MutableStateFlow(false)
    val showOpeningCashDialog: StateFlow<Boolean> = _showOpeningCashDialog.asStateFlow()

    private val _showPurchaseDialog = MutableStateFlow(false)
    val showPurchaseDialog: StateFlow<Boolean> = _showPurchaseDialog.asStateFlow()

    private val _showShopInfoDialog = MutableStateFlow(false)
    val showShopInfoDialog: StateFlow<Boolean> = _showShopInfoDialog.asStateFlow()

    fun setShowAddProductDialog(show: Boolean) { 
        _showAddProductDialog.value = show
        if (!show) _editingProduct.value = null
    }
    fun openAddProduct() {
        _editingProduct.value = null
        _showAddProductDialog.value = true
    }
    fun openEditProduct(product: Product) {
        _editingProduct.value = product
        _showAddProductDialog.value = true
    }
    fun closeProductDialog() {
        _editingProduct.value = null
        _showAddProductDialog.value = false
    }
    fun setShowAddCustomerDialog(show: Boolean) { _showAddCustomerDialog.value = show }
    fun setShowAddExpenseDialog(show: Boolean) { _showAddExpenseDialog.value = show }
    fun setShowCollectDueDialog(show: Boolean) { _showCollectDueDialog.value = show }
    fun setShowOpeningCashDialog(show: Boolean) { _showOpeningCashDialog.value = show }
    fun setShowPurchaseDialog(show: Boolean) { _showPurchaseDialog.value = show }
    fun setShowShopInfoDialog(show: Boolean) { _showShopInfoDialog.value = show }

    // Add operations
    fun addProduct(
        name: String,
        category: String,
        sku: String,
        buyPrice: Double,
        sellPrice: Double,
        stock: Int,
        variants: String,
        brand: String = "",
        lowStockLimit: Int = 5,
        note: String = ""
    ) {
        viewModelScope.launch {
            repository.insertProduct(
                Product(
                    name = name,
                    category = category.ifBlank { "Shirt" },
                    sku = sku.ifBlank { "SKU-" + (System.currentTimeMillis() % 10000) },
                    purchasePrice = buyPrice,
                    sellingPrice = sellPrice,
                    stockQuantity = stock,
                    sizesOrVariants = variants.ifBlank { "স্ট্যান্ডার্ড" },
                    brand = brand,
                    lowStockThreshold = lowStockLimit,
                    note = note,
                    isLowStockAlert = stock <= lowStockLimit
                )
            )
            _showAddProductDialog.value = false
        }
    }

    fun updateProductDetails(
        id: Long,
        name: String,
        category: String,
        sku: String,
        buy: Double,
        sell: Double,
        stock: Int,
        variants: String,
        brand: String,
        lowStockLimit: Int,
        note: String
    ) {
        viewModelScope.launch {
            repository.updateProduct(
                Product(
                    id = id,
                    name = name,
                    category = category,
                    sku = sku,
                    purchasePrice = buy,
                    sellingPrice = sell,
                    stockQuantity = stock,
                    sizesOrVariants = variants.ifBlank { "স্ট্যান্ডার্ড" },
                    brand = brand,
                    lowStockThreshold = lowStockLimit,
                    note = note,
                    isLowStockAlert = stock <= lowStockLimit
                )
            )
            _editingProduct.value = null
            _showAddProductDialog.value = false
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun updateProductStock(product: Product, newQuantity: Int) {
        viewModelScope.launch {
            repository.updateProduct(
                product.copy(
                    stockQuantity = newQuantity.coerceAtLeast(0),
                    isLowStockAlert = newQuantity <= product.lowStockThreshold
                )
            )
        }
    }

    fun addCustomer(name: String, phone: String, address: String, initialDue: Double) {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
            val dateString = dateFormat.format(Date())
            val custId = repository.insertCustomer(
                Customer(
                    name = name,
                    phone = phone,
                    address = address,
                    totalPurchased = initialDue,
                    totalPaid = 0.0,
                    currentDue = initialDue,
                    lastSaleDate = dateString
                )
            )
            if (initialDue > 0.0) {
                repository.insertTransaction(
                    CustomerTransaction(
                        customerId = custId,
                        type = "বিক্রি",
                        amount = initialDue,
                        note = "পূর্বের বাকি হিসাব",
                        dateString = dateString,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
            _showAddCustomerDialog.value = false
        }
    }

    fun addExpense(
        category: String,
        customCategory: String,
        amount: Double,
        paymentMethod: String,
        note: String
    ) {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
            val dateString = dateFormat.format(Date())
            val finalCategory = if (category == "অন্যান্য" && customCategory.isNotBlank()) customCategory.trim() else category
            repository.insertExpense(
                Expense(
                    category = category,
                    customCategory = customCategory.trim(),
                    amount = amount,
                    paymentMethod = paymentMethod,
                    note = note.trim(),
                    title = finalCategory,
                    dateString = dateString,
                    timestamp = System.currentTimeMillis()
                )
            )
            _showAddExpenseDialog.value = false
            _editingExpense.value = null
        }
    }

    fun addExpense(title: String, category: String, amount: Double) {
        addExpense(
            category = category.ifBlank { "অন্যান্য" },
            customCategory = title,
            amount = amount,
            paymentMethod = "নগদ ক্যাশ",
            note = title
        )
    }

    fun updateExpense(
        expenseId: Long,
        category: String,
        customCategory: String,
        amount: Double,
        paymentMethod: String,
        note: String
    ) {
        viewModelScope.launch {
            val current = allExpenses.value.find { it.id == expenseId }
            val finalCategory = if (category == "অন্যান্য" && customCategory.isNotBlank()) customCategory.trim() else category
            val updated = current?.copy(
                category = category,
                customCategory = customCategory.trim(),
                amount = amount,
                paymentMethod = paymentMethod,
                note = note.trim(),
                title = finalCategory
            ) ?: Expense(
                id = expenseId,
                category = category,
                customCategory = customCategory.trim(),
                amount = amount,
                paymentMethod = paymentMethod,
                note = note.trim(),
                title = finalCategory,
                dateString = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date()),
                timestamp = System.currentTimeMillis()
            )
            repository.updateExpense(updated)
            _showAddExpenseDialog.value = false
            _editingExpense.value = null
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun updateAccountBalances(cash: Double, bkash: Double, nagad: Double, bank: Double) {
        viewModelScope.launch {
            val current = shopProfile.value ?: ShopProfile()
            repository.updateShopProfile(
                current.copy(
                    openingCash = cash,
                    openingBkash = bkash,
                    openingNagad = nagad,
                    openingBank = bank
                )
            )
            _showOpeningCashDialog.value = false
        }
    }

    fun collectDuePayment(customer: Customer, amount: Double, note: String) {
        viewModelScope.launch {
            repository.recordDuePayment(customer, amount, note)
            _showCollectDueDialog.value = false
            // refresh selected customer in state
            val updated = customer.copy(
                totalPaid = customer.totalPaid + amount,
                currentDue = (customer.currentDue - amount).coerceAtLeast(0.0)
            )
            _selectedCustomer.value = updated
        }
    }

    fun updateOpeningCash(amount: Double) {
        viewModelScope.launch {
            val current = shopProfile.value ?: ShopProfile()
            repository.updateShopProfile(current.copy(openingCash = amount))
            _showOpeningCashDialog.value = false
        }
    }

    fun purchaseStockIn(
        product: Product,
        quantity: Int,
        selectedSize: String = "",
        purchasePrice: Double = product.purchasePrice,
        supplier: String = "",
        paymentType: String = "নগদ পরিশোধ (Cash Out from Register)",
        note: String = ""
    ) {
        viewModelScope.launch {
            // 1. Calculate updated sizesOrVariants string if variant selected
            val updatedSizesOrVariants = if (selectedSize.isNotBlank() && product.sizesOrVariants.isNotBlank()) {
                val parts = product.sizesOrVariants.split("|").map { it.trim() }
                var matched = false
                val newParts = parts.map { part ->
                    if (part.contains(":")) {
                        val sub = part.split(":")
                        val sName = sub[0].trim()
                        val sQty = sub.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
                        if (sName.equals(selectedSize, ignoreCase = true)) {
                            matched = true
                            "$sName:${sQty + quantity}"
                        } else {
                            part
                        }
                    } else if (part.equals(selectedSize, ignoreCase = true)) {
                        matched = true
                        "$part:${quantity}"
                    } else {
                        part
                    }
                }
                if (!matched) {
                    if (parts.any { it.contains(":") }) {
                        (newParts + "$selectedSize:$quantity").joinToString(" | ")
                    } else {
                        product.sizesOrVariants
                    }
                } else {
                    newParts.joinToString(" | ")
                }
            } else {
                product.sizesOrVariants
            }

            // 2. Update product total stock and price
            val updatedProduct = product.copy(
                stockQuantity = product.stockQuantity + quantity,
                purchasePrice = if (purchasePrice > 0) purchasePrice else product.purchasePrice,
                sizesOrVariants = updatedSizesOrVariants
            )
            repository.updateProduct(updatedProduct)

            // 3. If Cash Out from Register, record Expense
            val totalCost = quantity * purchasePrice
            if (paymentType.contains("নগদ") && totalCost > 0) {
                val expNote = buildString {
                    if (supplier.isNotBlank()) append("সাপ্লায়ার: $supplier. ")
                    if (selectedSize.isNotBlank()) append("সাইজ: $selectedSize. ")
                    if (note.isNotBlank()) append(note)
                }.trim()

                val now = Date()
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now)
                repository.insertExpense(
                    Expense(
                        category = "পণ্য ক্রয় / স্টক ইন",
                        amount = totalCost,
                        paymentMethod = "নগদ ক্যাশ",
                        note = expNote,
                        title = "পণ্য ক্রয়: ${product.name} ($quantity pcs)",
                        dateString = dateStr,
                        timestamp = now.time
                    )
                )
            }

            _showPurchaseDialog.value = false
        }
    }

    fun updateShopProfile(
        shopName: String,
        ownerName: String,
        email: String,
        phone: String,
        address: String
    ) {
        viewModelScope.launch {
            val current = shopProfile.value ?: ShopProfile()
            repository.updateShopProfile(
                current.copy(
                    shopName = shopName,
                    ownerName = ownerName,
                    email = email,
                    phone = phone,
                    address = address
                )
            )
            _showShopInfoDialog.value = false
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            clearCart()
            _activeSubScreen.value = SubScreen.NONE
            _currentTab.value = ScreenTab.DASHBOARD
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.clearAllData()
            repository.seedInitialStockIfEmpty()
            repository.initializeShopProfileIfNeeded(
                defaultName = "ফাইজা স্টোর",
                defaultOwner = "মোঃ শরিফ"
            )
            clearCart()
            _activeSubScreen.value = SubScreen.NONE
            _currentTab.value = ScreenTab.DASHBOARD
        }
    }
}

data class CompletedSaleSummary(
    val invoiceNo: String,
    val customerName: String,
    val itemCount: Int,
    val subtotal: Double,
    val discount: Double,
    val netTotal: Double,
    val paidAmount: Double,
    val dueAmount: Double
)
