package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ShopProfile
import com.example.ui.CustomerFilter
import com.example.ui.ScreenTab
import com.example.ui.ShopViewModel
import com.example.ui.SubScreen
import com.example.ui.components.AccountBalancesDialog
import com.example.ui.components.AddCustomerDialog
import com.example.ui.components.AddExpenseDialog
import com.example.ui.components.AddProductDialog
import com.example.ui.components.CollectDueDialog
import com.example.ui.components.CompletedSaleInvoiceDialog
import com.example.ui.components.ExpenseEntryDialog
import com.example.ui.components.OpeningCashDialog
import com.example.ui.components.PurchaseStockDialog
import com.example.ui.components.ShopBottomNavigation
import com.example.ui.components.ShopInfoDialog
import com.example.ui.screens.CartCheckoutScreen
import com.example.ui.screens.CustomerDetailScreen
import com.example.ui.screens.CustomerLedgerScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpenseHistoryScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MenuScreen
import com.example.ui.screens.PosScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.StockScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate950

class MainActivity : ComponentActivity() {

    private val viewModel: ShopViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                ShopApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ShopApp(viewModel: ShopViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val shopProfile by viewModel.shopProfile.collectAsStateWithLifecycle()
    val currentShopProfile = shopProfile ?: ShopProfile()

    if (!isLoggedIn) {
        LoginScreen(
            onLoginSuccess = { /* State is automatically updated inside viewModel on success */ },
            onAuthenticateOnline = { phone, pin -> viewModel.authenticateOnline(phone, pin) },
            checkNetworkOnline = { viewModel.isDeviceOnline() },
            initialPhone = viewModel.getRegisteredPhone(),
            shopName = currentShopProfile.shopName.ifBlank { "ফাইজা স্টোর" },
            ownerName = currentShopProfile.ownerName.ifBlank { "মোঃ শরিফ" }
        )
        return
    }

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val activeSubScreen by viewModel.activeSubScreen.collectAsStateWithLifecycle()

    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val customers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val sales by viewModel.allSales.collectAsStateWithLifecycle()
    val expenses by viewModel.allExpenses.collectAsStateWithLifecycle()

    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val posSearch by viewModel.posSearch.collectAsStateWithLifecycle()
    val posCategory by viewModel.posCategory.collectAsStateWithLifecycle()

    val stockSearch by viewModel.stockSearch.collectAsStateWithLifecycle()
    val stockCategory by viewModel.stockCategory.collectAsStateWithLifecycle()

    val customerSearch by viewModel.customerSearch.collectAsStateWithLifecycle()
    val customerFilter by viewModel.customerFilter.collectAsStateWithLifecycle()
    val selectedCustomer by viewModel.selectedCustomer.collectAsStateWithLifecycle()

    val accountBalances by viewModel.accountBalances.collectAsStateWithLifecycle()
    val editingExpense by viewModel.editingExpense.collectAsStateWithLifecycle()
    val editingProduct by viewModel.editingProduct.collectAsStateWithLifecycle()

    val checkoutCustomerName by viewModel.checkoutCustomerName.collectAsStateWithLifecycle()
    val checkoutCustomerPhone by viewModel.checkoutCustomerPhone.collectAsStateWithLifecycle()
    val checkoutSelectedCustomer by viewModel.checkoutSelectedCustomer.collectAsStateWithLifecycle()
    val checkoutIsCash by viewModel.checkoutIsCash.collectAsStateWithLifecycle()
    val checkoutDiscount by viewModel.checkoutDiscount.collectAsStateWithLifecycle()
    val checkoutPaidAmount by viewModel.checkoutPaidAmount.collectAsStateWithLifecycle()

    // Dialog states
    val showAddProduct by viewModel.showAddProductDialog.collectAsStateWithLifecycle()
    val showAddCustomer by viewModel.showAddCustomerDialog.collectAsStateWithLifecycle()
    val showAddExpense by viewModel.showAddExpenseDialog.collectAsStateWithLifecycle()
    val showCollectDue by viewModel.showCollectDueDialog.collectAsStateWithLifecycle()
    val showOpeningCash by viewModel.showOpeningCashDialog.collectAsStateWithLifecycle()
    val showPurchaseStock by viewModel.showPurchaseDialog.collectAsStateWithLifecycle()
    val showShopInfo by viewModel.showShopInfoDialog.collectAsStateWithLifecycle()
    val completedSaleInfo by viewModel.completedSaleInfo.collectAsStateWithLifecycle()

    // Handle Hardware / Gesture Back Press
    BackHandler(enabled = activeSubScreen != SubScreen.NONE || currentTab != ScreenTab.DASHBOARD) {
        if (activeSubScreen != SubScreen.NONE) {
            viewModel.closeSubScreen()
        } else if (currentTab != ScreenTab.DASHBOARD) {
            viewModel.navigateToTab(ScreenTab.DASHBOARD)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Slate950,
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = {
            if (activeSubScreen == SubScreen.NONE) {
                ShopBottomNavigation(
                    currentTab = currentTab,
                    cartItemCount = cart.sumOf { it.quantity },
                    onTabSelected = { tab -> viewModel.navigateToTab(tab) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Slate950)
        ) {
            AnimatedContent(
                targetState = Pair(currentTab, activeSubScreen),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { (tab, subScreen) ->
                when (subScreen) {
                    SubScreen.CART_CHECKOUT -> {
                        CartCheckoutScreen(
                            cartItems = cart,
                            customerName = checkoutCustomerName,
                            customerPhone = checkoutCustomerPhone,
                            selectedCustomer = checkoutSelectedCustomer,
                            isCash = checkoutIsCash,
                            discount = checkoutDiscount,
                            paidAmountStr = checkoutPaidAmount,
                            allCustomers = customers,
                            onBack = { viewModel.closeSubScreen() },
                            onClearCart = { viewModel.clearCart() },
                            onUpdateQuantity = { item, change -> viewModel.updateCartItemQuantity(item, change) },
                            onRemoveItem = { item -> viewModel.removeCartItem(item) },
                            onCustomerNameChange = { viewModel.setCheckoutCustomerName(it) },
                            onCustomerPhoneChange = { viewModel.setCheckoutCustomerPhone(it) },
                            onSelectCustomer = { viewModel.setCheckoutSelectedCustomer(it) },
                            onIsCashChange = { viewModel.setCheckoutIsCash(it) },
                            onDiscountChange = { viewModel.setCheckoutDiscount(it) },
                            onPaidAmountChange = { viewModel.setCheckoutPaidAmount(it) },
                            onCompleteSale = { viewModel.completeCheckout() }
                        )
                    }

                    SubScreen.CUSTOMER_DETAIL -> {
                        val cust = selectedCustomer ?: customers.firstOrNull()
                        if (cust != null) {
                            val transactions by viewModel.getTransactionsForCustomer(cust.id)
                                .collectAsStateWithLifecycle(emptyList())

                            CustomerDetailScreen(
                                customer = cust,
                                transactions = transactions,
                                onBack = { viewModel.closeSubScreen() },
                                onCollectDue = {
                                    viewModel.setShowCollectDueDialog(true)
                                },
                                onNewSale = {
                                    viewModel.setCheckoutSelectedCustomer(cust)
                                    viewModel.navigateToTab(ScreenTab.POS)
                                }
                            )
                        } else {
                            viewModel.closeSubScreen()
                        }
                    }

                    SubScreen.REPORTS -> {
                        ReportsScreen(
                            sales = sales,
                            expenses = expenses,
                            onBack = { viewModel.closeSubScreen() }
                        )
                    }

                    SubScreen.EXPENSE_HISTORY -> {
                        ExpenseHistoryScreen(
                            expenses = expenses,
                            accountBalances = accountBalances,
                            onBack = { viewModel.closeSubScreen() },
                            onOpenAddExpense = { viewModel.openAddExpense() },
                            onEditExpense = { expense -> viewModel.openEditExpense(expense) },
                            onDeleteExpense = { expense -> viewModel.deleteExpense(expense) },
                            onOpenManageBalances = { viewModel.setShowOpeningCashDialog(true) }
                        )
                    }

                    SubScreen.NONE -> {
                        when (tab) {
                            ScreenTab.DASHBOARD -> {
                                DashboardScreen(
                                    shopProfile = currentShopProfile,
                                    sales = sales,
                                    expenses = expenses,
                                    customers = customers,
                                    products = products,
                                    onNavigateTab = { viewModel.navigateToTab(it) },
                                    onOpenAddProduct = { viewModel.setShowAddProductDialog(true) },
                                    onOpenPurchaseStock = { viewModel.setShowPurchaseDialog(true) },
                                    onOpenAddExpense = { viewModel.openAddExpense() },
                                    onOpenCollectDue = { viewModel.setShowCollectDueDialog(true) },
                                    onOpenOpeningCash = { viewModel.setShowOpeningCashDialog(true) },
                                    onOpenReports = { viewModel.openReports() },
                                    onOpenExpenseHistory = { viewModel.openExpenseHistory() },
                                    onLockApp = { viewModel.logout() }
                                )
                            }

                            ScreenTab.POS -> {
                                PosScreen(
                                    products = products,
                                    cartItems = cart,
                                    searchQuery = posSearch,
                                    selectedCategory = posCategory,
                                    onSearchChange = { viewModel.setPosSearch(it) },
                                    onCategorySelect = { viewModel.setPosCategory(it) },
                                    onAddToCart = { product, variant -> viewModel.addToCart(product, variant) },
                                    onOpenCart = { viewModel.openCartCheckout() }
                                )
                            }

                            ScreenTab.STOCK -> {
                                StockScreen(
                                    products = products,
                                    searchQuery = stockSearch,
                                    selectedCategory = stockCategory,
                                    onSearchChange = { viewModel.setStockSearch(it) },
                                    onCategorySelect = { viewModel.setStockCategory(it) },
                                    onOpenAddProduct = { viewModel.openAddProduct() },
                                    onProductClick = { product ->
                                        // Handled inside StockScreen with management dialog
                                    },
                                    onEditProduct = { product ->
                                        viewModel.openEditProduct(product)
                                    },
                                    onDeleteProduct = { product ->
                                        viewModel.deleteProduct(product)
                                    },
                                    onUpdateStockQuantity = { product, newQuantity ->
                                        viewModel.updateProductStock(product, newQuantity)
                                    }
                                )
                            }

                            ScreenTab.BAKI -> {
                                CustomerLedgerScreen(
                                    customers = customers,
                                    searchQuery = customerSearch,
                                    filter = customerFilter,
                                    onSearchChange = { viewModel.setCustomerSearch(it) },
                                    onFilterSelect = { viewModel.setCustomerFilter(it) },
                                    onCustomerClick = { customer ->
                                        viewModel.openCustomerDetail(customer)
                                    },
                                    onOpenAddCustomer = { viewModel.setShowAddCustomerDialog(true) }
                                )
                            }

                            ScreenTab.MENU -> {
                                MenuScreen(
                                    shopProfile = currentShopProfile,
                                    onNavigateTab = { viewModel.navigateToTab(it) },
                                    onOpenShopInfo = { viewModel.setShowShopInfoDialog(true) },
                                    onOpenReports = { viewModel.openReports() },
                                    onResetDemoData = { viewModel.resetDemoData() },
                                    onLogout = { viewModel.logout() },
                                    onChangePin = { cur, new -> viewModel.updatePin(cur, new) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialogs
    if (showAddProduct || editingProduct != null) {
        val currentEditProduct = editingProduct
        AddProductDialog(
            editingProduct = currentEditProduct,
            onDismiss = { viewModel.closeProductDialog() },
            onConfirm = { name, category, sku, buy, sell, stock, variants, brand, lowStockLimit, note ->
                if (currentEditProduct != null) {
                    viewModel.updateProductDetails(
                        id = currentEditProduct.id,
                        name = name,
                        category = category,
                        sku = sku,
                        buy = buy,
                        sell = sell,
                        stock = stock,
                        variants = variants,
                        brand = brand,
                        lowStockLimit = lowStockLimit,
                        note = note
                    )
                } else {
                    viewModel.addProduct(name, category, sku, buy, sell, stock, variants, brand, lowStockLimit, note)
                }
            }
        )
    }

    if (showAddCustomer) {
        AddCustomerDialog(
            onDismiss = { viewModel.setShowAddCustomerDialog(false) },
            onConfirm = { name, phone, address, initialDue ->
                viewModel.addCustomer(name, phone, address, initialDue)
            }
        )
    }

    if (showAddExpense) {
        ExpenseEntryDialog(
            editingExpense = editingExpense,
            accountBalances = accountBalances,
            onDismiss = { viewModel.setShowAddExpenseDialog(false) },
            onConfirm = { category, customCategory, amount, paymentMethod, note ->
                val currentEdit = editingExpense
                if (currentEdit != null) {
                    viewModel.updateExpense(
                        expenseId = currentEdit.id,
                        category = category,
                        customCategory = customCategory,
                        amount = amount,
                        paymentMethod = paymentMethod,
                        note = note
                    )
                } else {
                    viewModel.addExpense(
                        category = category,
                        customCategory = customCategory,
                        amount = amount,
                        paymentMethod = paymentMethod,
                        note = note
                    )
                }
            },
            onOpenManageBalances = {
                viewModel.setShowAddExpenseDialog(false)
                viewModel.setShowOpeningCashDialog(true)
            }
        )
    }

    if (showCollectDue) {
        CollectDueDialog(
            customer = selectedCustomer,
            allCustomers = customers,
            onDismiss = { viewModel.setShowCollectDueDialog(false) },
            onConfirm = { customer, amount, note ->
                viewModel.collectDuePayment(customer, amount, note)
            }
        )
    }

    if (showOpeningCash) {
        AccountBalancesDialog(
            accountBalances = accountBalances,
            onDismiss = { viewModel.setShowOpeningCashDialog(false) },
            onConfirm = { cash, bkash, nagad, bank ->
                viewModel.updateAccountBalances(cash, bkash, nagad, bank)
            }
        )
    }

    if (showPurchaseStock) {
        PurchaseStockDialog(
            products = products,
            onDismiss = { viewModel.setShowPurchaseDialog(false) },
            onConfirm = { product, qty, size, buyPrice, supplier, paymentType, note ->
                viewModel.purchaseStockIn(product, qty, size, buyPrice, supplier, paymentType, note)
            }
        )
    }

    if (showShopInfo) {
        ShopInfoDialog(
            profile = currentShopProfile,
            onDismiss = { viewModel.setShowShopInfoDialog(false) },
            onConfirm = { shopName, ownerName, email, phone, address ->
                viewModel.updateShopProfile(shopName, ownerName, email, phone, address)
            }
        )
    }

    completedSaleInfo?.let { summary ->
        CompletedSaleInvoiceDialog(
            summary = summary,
            onDismiss = { viewModel.dismissCompletedSale() }
        )
    }
}
