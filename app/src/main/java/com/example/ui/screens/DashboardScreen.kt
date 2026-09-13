package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.model.Expense
import com.example.data.model.ExpiryStatus
import com.example.data.model.Product
import com.example.data.model.Sale
import com.example.data.model.ShopProfile
import com.example.sync.SyncStatus
import com.example.ui.ScreenTab
import com.example.ui.components.CloudSyncBadge
import com.example.ui.theme.AmberOrange
import com.example.ui.theme.CoralPink
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.TextPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    shopProfile: ShopProfile,
    sales: List<Sale>,
    expenses: List<Expense>,
    customers: List<Customer>,
    products: List<Product>,
    onNavigateTab: (ScreenTab) -> Unit,
    onOpenAddProduct: () -> Unit,
    onOpenPurchaseStock: () -> Unit,
    onOpenAddExpense: () -> Unit,
    onOpenCollectDue: () -> Unit,
    onOpenOpeningCash: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenExpenseHistory: () -> Unit = {},
    onLockApp: () -> Unit = {},
    syncStatus: SyncStatus = SyncStatus(),
    onSyncClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var dateFilter by remember { mutableStateOf("আজ") }
    var showFilterDropdown by remember { mutableStateOf(false) }

    val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val filteredSales = when (dateFilter) {
        "আজ" -> sales.filter { it.dateString.startsWith(todayDateStr) || it.dateString.contains("আজ") }
        else -> sales
    }
    val filteredExpenses = when (dateFilter) {
        "আজ" -> expenses.filter { it.dateString.startsWith(todayDateStr) || it.dateString.contains("আজ") }
        else -> expenses
    }

    val totalSalesAmount = (if (filteredSales.isNotEmpty()) filteredSales else sales).sumOf { it.subtotal - it.discount }
    val totalExpensesAmount = (if (filteredExpenses.isNotEmpty()) filteredExpenses else expenses).sumOf { it.amount }
    val grossProfit = totalSalesAmount * 0.30
    val netProfit = grossProfit - totalExpensesAmount

    val totalCustomerDue = customers.sumOf { it.currentDue }
    val dueCustomersCount = customers.count { it.currentDue > 0 }
    val currentCash = shopProfile.openingCash
    val lowStockCount = products.count { it.stockQuantity <= it.lowStockThreshold || it.isLowStockAlert || it.stockQuantity <= 10 }

    val totalStockValue = remember(products) {
        products.sumOf { it.purchasePrice * it.stockQuantity }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        val screenWidth = maxWidth
        val isSmallMobile = screenWidth < 360.dp
        val isTablet = screenWidth >= 640.dp
        val isDesktop = screenWidth >= 960.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 1200.dp)
                .align(Alignment.TopCenter)
        ) {
            // Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (isTablet) 20.dp else 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onNavigateTab(ScreenTab.MENU) }
                    .weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary.copy(alpha = 0.2f))
                        .border(1.dp, EmeraldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Store,
                        contentDescription = "Shop",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = shopProfile.shopName.ifBlank { "আমার দোকান" },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (shopProfile.ownerName.isNotBlank()) "প্রো: ${shopProfile.ownerName}" else "ব্যবসা হিসাব ও পিওএস",
                        fontSize = 12.sp,
                        color = Slate400
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Cloud Sync Status Badge
                CloudSyncBadge(
                    syncStatus = syncStatus,
                    onSyncClick = onSyncClick
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Date Filter Dropdown
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate900)
                            .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                            .clickable { showFilterDropdown = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dateFilter,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = EmeraldPrimary
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Filter",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showFilterDropdown,
                        onDismissRequest = { showFilterDropdown = false },
                        modifier = Modifier.background(Slate900)
                    ) {
                        listOf("আজ", "এই সপ্তাহ", "এই মাস", "সব").forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item, color = TextPrimary) },
                                onClick = {
                                    dateFilter = item
                                    showFilterDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Quick Security Lock Button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate900)
                        .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                        .clickable { onLockApp() }
                        .testTag("button_dashboard_lock"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock App",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Responsive Stat Cards Grid
            item {
                if (isDesktop) {
                    // Desktop: 6 cards in 2 rows of 3 columns
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(
                                title = "$dateFilter মোট বিক্রি",
                                amount = "৳ %,.0f".format(totalSalesAmount),
                                subtitle = "${sales.size} টি বিক্রি সম্পন্ন",
                                gradientColors = listOf(Color(0xFF047857), Color(0xFF065F46)),
                                modifier = Modifier.weight(1f).clickable { onOpenReports() },
                                testTag = "stat_sales"
                            )
                            StatCard(
                                title = "নেট লাভ / ক্ষতি",
                                amount = "৳ %,.0f".format(netProfit),
                                subtitle = if (netProfit >= 0.0) "মোট মুনাফা" else "লোকসান",
                                gradientColors = listOf(Color(0xFF1E293B), Color(0xFF0F172A)),
                                modifier = Modifier.weight(1f).clickable { onOpenReports() },
                                testTag = "stat_profit"
                            )
                            StatCard(
                                title = "$dateFilter মোট খরচ",
                                amount = "৳ %,.0f".format(totalExpensesAmount),
                                subtitle = "${expenses.size} টি এন্ট্রি",
                                gradientColors = listOf(Color(0xFFBE123C), Color(0xFF9F1239)),
                                modifier = Modifier.weight(1f).clickable { onOpenExpenseHistory() },
                                testTag = "stat_expenses"
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(
                                title = "মোট বাকি খাতা",
                                amount = "৳ %,.0f".format(totalCustomerDue),
                                subtitle = "$dueCustomersCount জন বাকিদার",
                                gradientColors = listOf(Color(0xFFB45309), Color(0xFF92400E)),
                                modifier = Modifier.weight(1f).clickable { onNavigateTab(ScreenTab.BAKI) },
                                testTag = "stat_due"
                            )
                            StatCard(
                                title = "মোট স্টক মূল্য",
                                amount = "৳ %,.0f".format(totalStockValue),
                                subtitle = "${products.size} টি পণ্য",
                                gradientColors = listOf(Color(0xFF2563EB), Color(0xFF1D4ED8)),
                                modifier = Modifier.weight(1f).clickable { onNavigateTab(ScreenTab.STOCK) },
                                testTag = "stat_stock_value"
                            )
                            StatCard(
                                title = "কম স্টক সতর্কতা",
                                amount = "$lowStockCount টি পণ্য",
                                subtitle = if (lowStockCount > 0) "রি-অর্ডার প্রয়োজন" else "পর্যাপ্ত স্টক",
                                gradientColors = if (lowStockCount > 0) listOf(Color(0xFFDC2626), Color(0xFF991B1B)) else listOf(Color(0xFF047857), Color(0xFF065F46)),
                                modifier = Modifier.weight(1f).clickable { onNavigateTab(ScreenTab.STOCK) },
                                testTag = "stat_low_stock"
                            )
                        }
                    }
                } else if (isTablet) {
                    // Tablet: 2 rows of 3 columns
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(
                                title = "$dateFilter মোট বিক্রি",
                                amount = "৳ %,.0f".format(totalSalesAmount),
                                subtitle = "${sales.size} টি বিক্রি",
                                gradientColors = listOf(Color(0xFF047857), Color(0xFF065F46)),
                                modifier = Modifier.weight(1f).clickable { onOpenReports() },
                                testTag = "stat_sales"
                            )
                            StatCard(
                                title = "নেট লাভ / ক্ষতি",
                                amount = "৳ %,.0f".format(netProfit),
                                subtitle = if (netProfit >= 0.0) "মোট মুনাফা" else "লোকসান",
                                gradientColors = listOf(Color(0xFF1E293B), Color(0xFF0F172A)),
                                modifier = Modifier.weight(1f).clickable { onOpenReports() },
                                testTag = "stat_profit"
                            )
                            StatCard(
                                title = "$dateFilter মোট খরচ",
                                amount = "৳ %,.0f".format(totalExpensesAmount),
                                subtitle = "${expenses.size} টি খরচ",
                                gradientColors = listOf(Color(0xFFBE123C), Color(0xFF9F1239)),
                                modifier = Modifier.weight(1f).clickable { onOpenExpenseHistory() },
                                testTag = "stat_expenses"
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(
                                title = "মোট বাকি খাতা",
                                amount = "৳ %,.0f".format(totalCustomerDue),
                                subtitle = "$dueCustomersCount জন বাকিদার",
                                gradientColors = listOf(Color(0xFFB45309), Color(0xFF92400E)),
                                modifier = Modifier.weight(1f).clickable { onNavigateTab(ScreenTab.BAKI) },
                                testTag = "stat_due"
                            )
                            StatCard(
                                title = "মোট স্টক মূল্য",
                                amount = "৳ %,.0f".format(totalStockValue),
                                subtitle = "${products.size} টি পণ্য",
                                gradientColors = listOf(Color(0xFF2563EB), Color(0xFF1D4ED8)),
                                modifier = Modifier.weight(1f).clickable { onNavigateTab(ScreenTab.STOCK) },
                                testTag = "stat_stock_value"
                            )
                            StatCard(
                                title = "কম স্টক",
                                amount = "$lowStockCount টি পণ্য",
                                subtitle = if (lowStockCount > 0) "রি-অর্ডার করুন" else "পর্যাপ্ত",
                                gradientColors = if (lowStockCount > 0) listOf(Color(0xFFDC2626), Color(0xFF991B1B)) else listOf(Color(0xFF047857), Color(0xFF065F46)),
                                modifier = Modifier.weight(1f).clickable { onNavigateTab(ScreenTab.STOCK) },
                                testTag = "stat_low_stock"
                            )
                        }
                    }
                } else if (isSmallMobile) {
                    // Small Mobile (<360dp): Clean 1-column list so text never overflows
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatCard(
                            title = "$dateFilter মোট বিক্রি",
                            amount = "৳ %,.0f".format(totalSalesAmount),
                            subtitle = "${sales.size} টি বিক্রি সম্পন্ন",
                            gradientColors = listOf(Color(0xFF047857), Color(0xFF065F46)),
                            modifier = Modifier.fillMaxWidth().clickable { onOpenReports() },
                            testTag = "stat_sales"
                        )
                        StatCard(
                            title = "নেট লাভ / ক্ষতি",
                            amount = "৳ %,.0f".format(netProfit),
                            subtitle = if (netProfit >= 0.0) "মোট মুনাফা" else "লোকসান",
                            gradientColors = listOf(Color(0xFF1E293B), Color(0xFF0F172A)),
                            modifier = Modifier.fillMaxWidth().clickable { onOpenReports() },
                            testTag = "stat_profit"
                        )
                        StatCard(
                            title = "$dateFilter মোট খরচ",
                            amount = "৳ %,.0f".format(totalExpensesAmount),
                            subtitle = "${expenses.size} টি এন্ট্রি",
                            gradientColors = listOf(Color(0xFFBE123C), Color(0xFF9F1239)),
                            modifier = Modifier.fillMaxWidth().clickable { onOpenExpenseHistory() },
                            testTag = "stat_expenses"
                        )
                        StatCard(
                            title = "মোট বাকি খাতা",
                            amount = "৳ %,.0f".format(totalCustomerDue),
                            subtitle = "$dueCustomersCount জন বাকিদার",
                            gradientColors = listOf(Color(0xFFB45309), Color(0xFF92400E)),
                            modifier = Modifier.fillMaxWidth().clickable { onNavigateTab(ScreenTab.BAKI) },
                            testTag = "stat_due"
                        )
                        StatCard(
                            title = "মোট স্টক মূল্য",
                            amount = "৳ %,.0f".format(totalStockValue),
                            subtitle = "${products.size} টি পণ্য",
                            gradientColors = listOf(Color(0xFF2563EB), Color(0xFF1D4ED8)),
                            modifier = Modifier.fillMaxWidth().clickable { onNavigateTab(ScreenTab.STOCK) },
                            testTag = "stat_stock_value"
                        )
                    }
                } else {
                    // Normal Mobile (360-640dp): 2 columns (3 rows)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(
                                title = "$dateFilter মোট বিক্রি",
                                amount = "৳ %,.0f".format(totalSalesAmount),
                                subtitle = "${sales.size} টি বিক্রি",
                                gradientColors = listOf(Color(0xFF047857), Color(0xFF065F46)),
                                modifier = Modifier.weight(1f).clickable { onOpenReports() },
                                testTag = "stat_sales"
                            )
                            StatCard(
                                title = "নেট লাভ",
                                amount = "৳ %,.0f".format(netProfit),
                                subtitle = if (netProfit >= 0.0) "মোট মুনাফা" else "লোকসান",
                                gradientColors = listOf(Color(0xFF1E293B), Color(0xFF0F172A)),
                                modifier = Modifier.weight(1f).clickable { onOpenReports() },
                                testTag = "stat_profit"
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(
                                title = "$dateFilter মোট খরচ",
                                amount = "৳ %,.0f".format(totalExpensesAmount),
                                subtitle = "${expenses.size} টি খরচ",
                                gradientColors = listOf(Color(0xFFBE123C), Color(0xFF9F1239)),
                                modifier = Modifier.weight(1f).clickable { onOpenExpenseHistory() },
                                testTag = "stat_expenses"
                            )
                            StatCard(
                                title = "মোট বাকি খাতা",
                                amount = "৳ %,.0f".format(totalCustomerDue),
                                subtitle = "$dueCustomersCount জন বাকিদার",
                                gradientColors = listOf(Color(0xFFB45309), Color(0xFF92400E)),
                                modifier = Modifier.weight(1f).clickable { onNavigateTab(ScreenTab.BAKI) },
                                testTag = "stat_due"
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(
                                title = "মোট স্টক মূল্য",
                                amount = "৳ %,.0f".format(totalStockValue),
                                subtitle = "${products.size} টি পণ্য",
                                gradientColors = listOf(Color(0xFF2563EB), Color(0xFF1D4ED8)),
                                modifier = Modifier.weight(1f).clickable { onNavigateTab(ScreenTab.STOCK) },
                                testTag = "stat_stock_value"
                            )
                            StatCard(
                                title = "কম স্টক",
                                amount = "$lowStockCount টি পণ্য",
                                subtitle = if (lowStockCount > 0) "রি-অর্ডার সতর্কতা" else "পর্যাপ্ত স্টক",
                                gradientColors = if (lowStockCount > 0) listOf(Color(0xFFDC2626), Color(0xFF991B1B)) else listOf(Color(0xFF047857), Color(0xFF065F46)),
                                modifier = Modifier.weight(1f).clickable { onNavigateTab(ScreenTab.STOCK) },
                                testTag = "stat_low_stock"
                            )
                        }
                    }
                }
            }

            // Current Cash Balance Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Slate800, RoundedCornerShape(14.dp))
                        .clickable { onOpenOpeningCash() }
                        .testTag("banner_current_cash"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(EmeraldPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "দোকানের ক্যাশ ব্যালেন্স",
                                    fontSize = 12.sp,
                                    color = Slate400
                                )
                                Text(
                                    text = "৳ %,.0f".format(currentCash),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                            }
                        }

                        Text(
                            text = "ব্যালেন্স পরিবর্তন >",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate400
                        )
                    }
                }
            }

            if (lowStockCount > 0) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CoralPink.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .clickable { onNavigateTab(ScreenTab.STOCK) }
                            .testTag("banner_low_stock_alert"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CoralPink.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CoralPink.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.WarningAmber,
                                        contentDescription = null,
                                        tint = CoralPink,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "কম স্টক আইটেম: $lowStockCount টি পণ্য",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "রি-অর্ডার প্রয়োজন • স্টক চেক করতে ট্যাপ করুন",
                                        fontSize = 11.sp,
                                        color = Slate400
                                    )
                                }
                            }

                            Text(
                                text = "স্টক দেখুন >",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CoralPink
                            )
                        }
                    }
                }
            }

            val expiredCount = products.count { it.isExpired() }
            val expiringSoonCount = products.count { it.getExpiryStatus() == ExpiryStatus.EXPIRING_SOON }
            if (expiredCount > 0 || expiringSoonCount > 0) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CoralPink.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .clickable { onNavigateTab(ScreenTab.STOCK) }
                            .testTag("banner_expiry_alert"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CoralPink.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CoralPink.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.WarningAmber,
                                        contentDescription = null,
                                        tint = CoralPink,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (expiredCount > 0) "মেয়াদ উত্তীর্ণ কসমেটিক্স: $expiredCount টি" else "মেয়াদ সন্নিকটে: $expiringSoonCount টি পণ্য",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "স্টক অপশন থেকে মেয়াদোত্তীর্ণ পণ্য চেক করুন",
                                        fontSize = 11.sp,
                                        color = Slate400
                                    )
                                }
                            }

                            Text(
                                text = "চেক করুন >",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CoralPink
                            )
                        }
                    }
                }
            }

            // Quick Actions Section
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "কুইক অ্যাকশন",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }

            // Quick Action Buttons
            item {
                if (isTablet) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionButton(
                            icon = Icons.Default.PointOfSale,
                            label = "নতুন বিক্রি",
                            iconColor = EmeraldPrimary,
                            modifier = Modifier.weight(1f),
                            testTag = "btn_quick_pos",
                            onClick = { onNavigateTab(ScreenTab.POS) }
                        )

                        QuickActionButton(
                            icon = Icons.Default.ReceiptLong,
                            label = "খরচ এন্ট্রি",
                            iconColor = CoralPink,
                            modifier = Modifier.weight(1f),
                            testTag = "btn_quick_add_expense",
                            onClick = onOpenAddExpense
                        )

                        QuickActionButton(
                            icon = Icons.Default.HistoryEdu,
                            label = "খরচের হিসাব",
                            iconColor = AmberOrange,
                            modifier = Modifier.weight(1f),
                            testTag = "btn_quick_expense_history",
                            onClick = onOpenExpenseHistory
                        )

                        QuickActionButton(
                            icon = Icons.Default.Payments,
                            label = "বাকি আদায়",
                            iconColor = EmeraldPrimary,
                            modifier = Modifier.weight(1f),
                            testTag = "btn_quick_collect_due",
                            onClick = onOpenCollectDue
                        )

                        QuickActionButton(
                            icon = Icons.Default.AddBox,
                            label = "নতুন পণ্য",
                            iconColor = EmeraldPrimary,
                            modifier = Modifier.weight(1f),
                            testTag = "btn_quick_add_product",
                            onClick = onOpenAddProduct
                        )

                        QuickActionButton(
                            icon = Icons.Default.ShoppingBag,
                            label = "মাল কেনা",
                            iconColor = AmberOrange,
                            modifier = Modifier.weight(1f),
                            testTag = "btn_quick_purchase",
                            onClick = onOpenPurchaseStock
                        )
                    }
                } else if (isSmallMobile) {
                    // Small Mobile: 3 rows of 2 columns
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QuickActionButton(
                                icon = Icons.Default.PointOfSale,
                                label = "নতুন বিক্রি",
                                iconColor = EmeraldPrimary,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_quick_pos",
                                onClick = { onNavigateTab(ScreenTab.POS) }
                            )
                            QuickActionButton(
                                icon = Icons.Default.ReceiptLong,
                                label = "খরচ এন্ট্রি",
                                iconColor = CoralPink,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_quick_add_expense",
                                onClick = onOpenAddExpense
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QuickActionButton(
                                icon = Icons.Default.HistoryEdu,
                                label = "খরচের হিসাব",
                                iconColor = AmberOrange,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_quick_expense_history",
                                onClick = onOpenExpenseHistory
                            )
                            QuickActionButton(
                                icon = Icons.Default.Payments,
                                label = "বাকি আদায়",
                                iconColor = EmeraldPrimary,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_quick_collect_due",
                                onClick = onOpenCollectDue
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QuickActionButton(
                                icon = Icons.Default.AddBox,
                                label = "নতুন পণ্য",
                                iconColor = EmeraldPrimary,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_quick_add_product",
                                onClick = onOpenAddProduct
                            )
                            QuickActionButton(
                                icon = Icons.Default.ShoppingBag,
                                label = "মাল কেনা",
                                iconColor = AmberOrange,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_quick_purchase",
                                onClick = onOpenPurchaseStock
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            QuickActionButton(
                                icon = Icons.Default.PointOfSale,
                                label = "নতুন বিক্রি",
                                iconColor = EmeraldPrimary,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_quick_pos",
                                onClick = { onNavigateTab(ScreenTab.POS) }
                            )

                            QuickActionButton(
                                icon = Icons.Default.ReceiptLong,
                                label = "খরচ এন্ট্রি",
                                iconColor = CoralPink,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_quick_add_expense",
                                onClick = onOpenAddExpense
                            )

                            QuickActionButton(
                                icon = Icons.Default.HistoryEdu,
                                label = "খরচের হিসাব",
                                iconColor = AmberOrange,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_quick_expense_history",
                                onClick = onOpenExpenseHistory
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            QuickActionButton(
                                icon = Icons.Default.Payments,
                                label = "বাকি আদায়",
                                iconColor = EmeraldPrimary,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_quick_collect_due",
                                onClick = onOpenCollectDue
                            )

                            QuickActionButton(
                                icon = Icons.Default.AddBox,
                                label = "নতুন পণ্য",
                                iconColor = EmeraldPrimary,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_quick_add_product",
                                onClick = onOpenAddProduct
                            )

                            QuickActionButton(
                                icon = Icons.Default.ShoppingBag,
                                label = "মাল কেনা",
                                iconColor = AmberOrange,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_quick_purchase",
                                onClick = onOpenPurchaseStock
                            )
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun StatCard(
    title: String,
    amount: String,
    subtitle: String,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Card(
        modifier = modifier
            .border(1.dp, Slate800, RoundedCornerShape(14.dp))
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            gradientColors.first().copy(alpha = 0.25f),
                            Slate900
                        )
                    )
                )
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = Slate400,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = amount,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Slate400,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    iconColor: Color,
    modifier: Modifier = Modifier,
    testTag: String = "",
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .border(1.dp, Slate800, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1
            )
        }
    }
}
