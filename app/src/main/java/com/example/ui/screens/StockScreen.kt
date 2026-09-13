package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpiryStatus
import com.example.data.model.Product
import com.example.data.model.ProductStockStatus
import com.example.data.model.StockTransaction
import com.example.ui.components.BarcodeViewDialog
import com.example.ui.components.BulkImportDialog
import com.example.ui.components.CosmeticsRestockDialog
import com.example.ui.components.ProductDetailModal
import com.example.ui.components.StockAdjustmentDialog
import com.example.ui.components.StockHistoryDialog
import com.example.ui.theme.*

enum class ProductSortOption(val title: String) {
    NAME_ASC("নাম (A - Z)"),
    STOCK_ASC("স্টক (কম থেকে বেশি)"),
    STOCK_DESC("স্টক (বেশি থেকে কম)"),
    PRICE_DESC("বিক্রয়মূল্য (উচ্চ থেকে নিম্ন)"),
    EXPIRY_SOON("মেয়াদ (নিকটবর্তী)")
}

@Composable
fun StockScreen(
    products: List<Product>,
    searchQuery: String,
    selectedCategory: String,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onOpenAddProduct: () -> Unit,
    onProductClick: (Product) -> Unit = {},
    onEditProduct: (Product) -> Unit = {},
    onDeleteProduct: (Product) -> Unit = {},
    onUpdateStockQuantity: (Product, Int) -> Unit = { _, _ -> },
    stockTransactions: List<StockTransaction> = emptyList(),
    onRestock: ((Product, Int, Double, String, String, String, String, String, Boolean, String) -> Unit)? = null,
    onAdjustStock: ((Product, String, Int, String, String) -> Unit)? = null,
    onBulkImport: ((List<Product>) -> Unit)? = null,
    shopName: String = "কসমেটিক্স শপ",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Base Cosmetics Category Chips
    val baseCategories = listOf(
        "সব",
        "মেকআপ",
        "স্কিন কেয়ার",
        "হেয়ার কেয়ার",
        "বডি কেয়ার",
        "সুগন্ধি",
        "কম স্টক",
        "মেয়াদ উত্তীর্ণ"
    )
    val dynamicCategories = remember(products) {
        val cats = baseCategories.toMutableList()
        products.forEach { p ->
            val simpleCat = p.category.substringBefore(" (").trim()
            if (simpleCat.isNotBlank() && simpleCat !in cats) {
                cats.add(simpleCat)
            }
        }
        cats
    }

    // Sorting state
    var selectedSort by remember { mutableStateOf(ProductSortOption.NAME_ASC) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    // Dialog & Modal states
    var detailProduct by remember { mutableStateOf<Product?>(null) }
    var restockProduct by remember { mutableStateOf<Product?>(null) }
    var adjustProduct by remember { mutableStateOf<Product?>(null) }
    var barcodeProduct by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var showBulkImportDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var historyFilterProduct by remember { mutableStateOf<Product?>(null) }

    // Metric Calculations
    val totalPieces = products.sumOf { it.stockQuantity }
    val totalCost = products.sumOf { it.purchasePrice * it.stockQuantity }
    val totalRetail = products.sumOf { it.sellingPrice * it.stockQuantity }
    val grossProfit = (totalRetail - totalCost).coerceAtLeast(0.0)
    val lowStockCount = products.count { it.stockQuantity <= it.lowStockThreshold || it.isLowStockAlert || it.stockQuantity <= 5 }
    val expiredCount = products.count { it.isExpired() }
    val expiringSoonCount = products.count { it.getExpiryStatus() == ExpiryStatus.EXPIRING_SOON }

    // Filter & Sort Products
    val filteredProducts = remember(products, searchQuery, selectedCategory, selectedSort) {
        products.filter { product ->
            val matchesQuery = searchQuery.isBlank() ||
                    product.name.contains(searchQuery, ignoreCase = true) ||
                    product.sku.contains(searchQuery, ignoreCase = true) ||
                    product.barcode.contains(searchQuery, ignoreCase = true) ||
                    product.brand.contains(searchQuery, ignoreCase = true) ||
                    product.supplierName.contains(searchQuery, ignoreCase = true)

            val matchesCategory = when (selectedCategory) {
                "সব" -> true
                "কম স্টক" -> product.stockQuantity <= product.lowStockThreshold || product.isLowStockAlert || product.stockQuantity <= 5
                "মেয়াদ উত্তীর্ণ" -> product.isExpired()
                "মেয়াদ সন্নিকটে" -> product.getExpiryStatus() == ExpiryStatus.EXPIRING_SOON
                else -> product.category.contains(selectedCategory, ignoreCase = true)
            }
            matchesQuery && matchesCategory
        }.let { list ->
            when (selectedSort) {
                ProductSortOption.NAME_ASC -> list.sortedBy { it.name.lowercase() }
                ProductSortOption.STOCK_ASC -> list.sortedBy { it.stockQuantity }
                ProductSortOption.STOCK_DESC -> list.sortedByDescending { it.stockQuantity }
                ProductSortOption.PRICE_DESC -> list.sortedByDescending { it.sellingPrice }
                ProductSortOption.EXPIRY_SOON -> list.sortedBy { (it.daysUntilExpiry() ?: 999999).toLong() }
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .testTag("screen_stock")
    ) {
        val screenWidth = maxWidth
        val isSmallMobile = screenWidth < 360.dp
        val isTablet = screenWidth >= 640.dp
        val isDesktop = screenWidth >= 960.dp
        val gridColumns = if (isDesktop) 3 else if (isTablet) 2 else 1

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 1240.dp)
                .align(Alignment.TopCenter)
                .padding(horizontal = if (isTablet) 20.dp else if (isSmallMobile) 8.dp else 12.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = if (isSmallMobile) "স্টক ব্যবস্থাপনা" else "প্রসাধনী পণ্য ও স্টক ব্যবস্থাপনা",
                        fontSize = if (isTablet) 22.sp else if (isSmallMobile) 16.sp else 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isSmallMobile) "Inventory & Stock Tracking" else "Cosmetics Inventory, Batch, Expiry & Stock Tracking",
                        fontSize = if (isSmallMobile) 10.5.sp else 11.5.sp,
                        color = Slate400,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Header Action Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Bulk Import Button
                    OutlinedButton(
                        onClick = { showBulkImportDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Slate700),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300),
                        contentPadding = PaddingValues(horizontal = if (isSmallMobile) 8.dp else 12.dp, vertical = 0.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("btn_open_bulk_import")
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = "Bulk Import", modifier = Modifier.size(16.dp))
                        if (isTablet) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("বাল্ক ইম্পোর্ট", fontSize = 12.sp)
                        }
                    }

                    // Stock History Button
                    OutlinedButton(
                        onClick = {
                            historyFilterProduct = null
                            showHistoryDialog = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Slate700),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300),
                        contentPadding = PaddingValues(horizontal = if (isSmallMobile) 8.dp else 12.dp, vertical = 0.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("btn_open_stock_history")
                    ) {
                        Icon(Icons.Default.History, contentDescription = "Stock History", modifier = Modifier.size(16.dp))
                        if (isTablet) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("স্টক হিস্ট্রি", fontSize = 12.sp)
                        }
                    }

                    // Add Product Button
                    Button(
                        onClick = onOpenAddProduct,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = if (isSmallMobile) 10.dp else 12.dp, vertical = 0.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("btn_top_add_product")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Slate950, modifier = Modifier.size(16.dp))
                        if (!isSmallMobile) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isTablet) "+ নতুন পণ্য যোগ" else "পণ্য যোগ",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate950
                            )
                        }
                    }
                }
            }

            // Valuation & Metric KPI Overview Cards
            if (isTablet) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StockKpiCard(
                        title = "মোট ইনভেন্টরি ক্রয়মূল্য",
                        amount = "৳ %,.0f".format(totalCost),
                        subtitle = "${products.size}টি পণ্যে $totalPieces পিস স্টক",
                        icon = Icons.Default.Inventory2,
                        accentColor = Slate300,
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_stock_cost"
                    )
                    StockKpiCard(
                        title = "সম্ভাব্য বিক্রয়মূল্য",
                        amount = "৳ %,.0f".format(totalRetail),
                        subtitle = "প্রত্যাশিত মোট সেলস",
                        icon = Icons.Default.TrendingUp,
                        accentColor = EmeraldPrimary,
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_stock_retail"
                    )
                    StockKpiCard(
                        title = "সম্ভাব্য মোট লাভ",
                        amount = "৳ %,.0f".format(grossProfit),
                        subtitle = "গ্রস প্রফিট মার্জিন",
                        icon = Icons.Default.MonetizationOn,
                        accentColor = EmeraldPrimary,
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_stock_profit"
                    )
                    StockKpiCard(
                        title = "কম স্টক এলার্ট",
                        amount = "$lowStockCount টি",
                        subtitle = "রি-স্টক প্রয়োজন",
                        icon = Icons.Default.WarningAmber,
                        accentColor = if (lowStockCount > 0) AmberOrange else Slate400,
                        badgeText = if (lowStockCount > 0) "রি-অর্ডার" else null,
                        isSelected = selectedCategory == "কম স্টক",
                        onClick = {
                            if (selectedCategory == "কম স্টক") onCategorySelect("সব") else onCategorySelect("কম স্টক")
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_stock_low"
                    )
                    StockKpiCard(
                        title = "মেয়াদ উত্তীর্ণ / আসন্ন",
                        amount = "${expiredCount + expiringSoonCount} টি",
                        subtitle = "মেয়াদ সতর্কবার্তা",
                        icon = Icons.Default.ErrorOutline,
                        accentColor = if (expiredCount > 0) CoralPink else AmberOrange,
                        badgeText = if (expiredCount > 0) "$expiredCount শেষ" else null,
                        isSelected = selectedCategory == "মেয়াদ উত্তীর্ণ",
                        onClick = {
                            if (selectedCategory == "মেয়াদ উত্তীর্ণ") onCategorySelect("সব") else onCategorySelect("মেয়াদ উত্তীর্ণ")
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_stock_expiry"
                    )
                }
            } else {
                // Mobile layout: 2-column KPI grid
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StockKpiCard(
                            title = "স্টক ক্রয়মূল্য",
                            amount = "৳ %,.0f".format(totalCost),
                            subtitle = "$totalPieces পিস স্টক",
                            icon = Icons.Default.Inventory2,
                            accentColor = Slate300,
                            modifier = Modifier.weight(1f)
                        )
                        StockKpiCard(
                            title = "সম্ভাব্য বিক্রয়মূল্য",
                            amount = "৳ %,.0f".format(totalRetail),
                            subtitle = "লাভ: ৳ %,.0f".format(grossProfit),
                            icon = Icons.Default.TrendingUp,
                            accentColor = EmeraldPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StockKpiCard(
                            title = "কম স্টক",
                            amount = "$lowStockCount টি",
                            subtitle = "রি-অর্ডার প্রয়োজন",
                            icon = Icons.Default.WarningAmber,
                            accentColor = if (lowStockCount > 0) AmberOrange else Slate400,
                            isSelected = selectedCategory == "কম স্টক",
                            onClick = {
                                if (selectedCategory == "কম স্টক") onCategorySelect("সব") else onCategorySelect("কম স্টক")
                            },
                            modifier = Modifier.weight(1f)
                        )
                        StockKpiCard(
                            title = "মেয়াদ সতর্কবার্তা",
                            amount = "${expiredCount + expiringSoonCount} টি",
                            subtitle = if (expiredCount > 0) "$expiredCount টি শেষ!" else "সতর্কতা",
                            icon = Icons.Default.ErrorOutline,
                            accentColor = if (expiredCount > 0) CoralPink else AmberOrange,
                            isSelected = selectedCategory == "মেয়াদ উত্তীর্ণ",
                            onClick = {
                                if (selectedCategory == "মেয়াদ উত্তীর্ণ") onCategorySelect("সব") else onCategorySelect("মেয়াদ উত্তীর্ণ")
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Search Bar & Sort Dropdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = {
                        Text(
                            "পণ্য, SKU, বারকোড বা ব্র্যান্ড খুঁজুন...",
                            color = Slate600,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Slate400,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = if (searchQuery.isNotBlank()) {
                        {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Slate400, modifier = Modifier.size(16.dp))
                            }
                        }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Slate900,
                        unfocusedContainerColor = Slate900,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = Slate800,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.weight(1f).testTag("input_search_stock")
                )

                // Sort Dropdown
                Box {
                    IconButton(
                        onClick = { sortMenuExpanded = true },
                        modifier = Modifier
                            .size(50.dp)
                            .background(Slate900, RoundedCornerShape(12.dp))
                            .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                            .testTag("btn_sort_stock")
                    ) {
                        Icon(
                            Icons.Default.Sort,
                            contentDescription = "Sort Products",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false },
                        modifier = Modifier.background(Slate850)
                    ) {
                        ProductSortOption.values().forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option.title,
                                        color = if (selectedSort == option) EmeraldPrimary else TextPrimary,
                                        fontWeight = if (selectedSort == option) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    selectedSort = option
                                    sortMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                items(dynamicCategories) { category ->
                    val isSelected = selectedCategory == category
                    Surface(
                        onClick = { onCategorySelect(category) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) EmeraldPrimary else Slate900,
                        border = BorderStroke(1.dp, if (isSelected) EmeraldPrimary else Slate800),
                        modifier = Modifier.testTag("stock_chip_$category")
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Slate950 else Slate300
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Product Inventory Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 20.dp)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    CosmeticsProductCard(
                        product = product,
                        onClick = {
                            detailProduct = product
                            onProductClick(product)
                        },
                        onRestock = {
                            restockProduct = product
                        },
                        onAdjust = {
                            adjustProduct = product
                        },
                        onBarcode = {
                            barcodeProduct = product
                        },
                        onEdit = {
                            onEditProduct(product)
                        },
                        onDelete = {
                            productToDelete = product
                        }
                    )
                }

                if (filteredProducts.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Slate900),
                            border = BorderStroke(1.dp, Slate800),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(EmeraldPrimary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Inventory2,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank() || selectedCategory != "সব") "কোনো পণ্য খুঁজে পাওয়া যায়নি" else "স্টকে কোনো পণ্য নেই",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank() || selectedCategory != "সব") "অন্য নাম বা ক্যাটাগরি দিয়ে সার্চ করে দেখুন" else "উপরের '+ নতুন পণ্য যোগ' বা 'বাল্ক ইম্পোর্ট' দিয়ে আপনার কসমেটিক্স সামগ্রী যোগ করুন",
                                    fontSize = 13.sp,
                                    color = Slate400,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Modals & Dialogs
        detailProduct?.let { prod ->
            ProductDetailModal(
                product = prod,
                onDismiss = { detailProduct = null },
                onRestock = {
                    detailProduct = null
                    restockProduct = prod
                },
                onAdjustStock = {
                    detailProduct = null
                    adjustProduct = prod
                },
                onViewHistory = {
                    detailProduct = null
                    historyFilterProduct = prod
                    showHistoryDialog = true
                },
                onViewBarcode = {
                    detailProduct = null
                    barcodeProduct = prod
                },
                onEdit = {
                    detailProduct = null
                    onEditProduct(prod)
                },
                onDelete = {
                    detailProduct = null
                    productToDelete = prod
                }
            )
        }

        restockProduct?.let { prod ->
            CosmeticsRestockDialog(
                product = prod,
                onDismiss = { restockProduct = null },
                onConfirm = { qty, price, sName, sPhone, batch, mfg, exp, cash, note ->
                    onRestock?.invoke(prod, qty, price, sName, sPhone, batch, mfg, exp, cash, note)
                    restockProduct = null
                    Toast.makeText(context, "${prod.name} রিস্টক সম্পন্ন হয়েছে (+${qty} ${prod.unit})", Toast.LENGTH_SHORT).show()
                }
            )
        }

        adjustProduct?.let { prod ->
            StockAdjustmentDialog(
                product = prod,
                onDismiss = { adjustProduct = null },
                onConfirm = { type, qty, reason, note ->
                    onAdjustStock?.invoke(prod, type, qty, reason, note)
                    adjustProduct = null
                    Toast.makeText(context, "স্টক সমন্বয় আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
                }
            )
        }

        barcodeProduct?.let { prod ->
            BarcodeViewDialog(
                product = prod,
                shopName = shopName,
                onDismiss = { barcodeProduct = null }
            )
        }

        if (showBulkImportDialog) {
            BulkImportDialog(
                onDismiss = { showBulkImportDialog = false },
                onImportProducts = { importedList ->
                    onBulkImport?.invoke(importedList)
                    Toast.makeText(context, "${importedList.size}টি প্রসাধনী পণ্য সফলভাবে ইম্পোর্ট করা হয়েছে!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showHistoryDialog) {
            val transactionsToShow = if (historyFilterProduct != null) {
                stockTransactions.filter { it.productId == historyFilterProduct!!.id }
            } else {
                stockTransactions
            }
            StockHistoryDialog(
                transactions = transactionsToShow,
                productName = historyFilterProduct?.name,
                onDismiss = {
                    showHistoryDialog = false
                    historyFilterProduct = null
                }
            )
        }

        productToDelete?.let { prod ->
            AlertDialog(
                onDismissRequest = { productToDelete = null },
                containerColor = Slate900,
                icon = {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = CoralPink,
                        modifier = Modifier.size(28.dp)
                    )
                },
                title = {
                    Text(
                        text = "পণ্য ডিলিট করবেন?",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "আপনি কি নিশ্চিতভাবে এই পণ্যটি স্টক তালিকা থেকে সম্পূর্ণ মুছে ফেলতে চান?",
                            color = Slate400,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Slate800)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(prod.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("SKU: ${prod.sku} • বর্তমান মজুদ: ${prod.stockQuantity} ${prod.unit}", color = Slate400, fontSize = 12.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteProduct(prod)
                            productToDelete = null
                            Toast.makeText(context, "${prod.name} মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPink),
                        modifier = Modifier.testTag("button_confirm_delete_product")
                    ) {
                        Text("মুছে ফেলুন", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { productToDelete = null },
                        modifier = Modifier.testTag("button_cancel_delete_product")
                    ) {
                        Text("বাতিল", color = Slate400)
                    }
                }
            )
        }
    }
}

/**
 * Modern, Rich Cosmetics Product Card
 */
@Composable
fun CosmeticsProductCard(
    product: Product,
    onClick: () -> Unit,
    onRestock: () -> Unit,
    onAdjust: () -> Unit,
    onBarcode: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stockStatus = product.getStockStatus()
    val expiryStatus = product.getExpiryStatus()
    val daysUntilExp = product.daysUntilExpiry()
    val profit = (product.sellingPrice - product.purchasePrice).coerceAtLeast(0.0)

    val cardBorderColor = when {
        expiryStatus == ExpiryStatus.EXPIRED -> CoralPink.copy(alpha = 0.6f)
        stockStatus == ProductStockStatus.OUT_OF_STOCK -> CoralPink.copy(alpha = 0.5f)
        stockStatus == ProductStockStatus.LOW_STOCK -> AmberOrange.copy(alpha = 0.5f)
        else -> Slate700
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = BorderStroke(1.dp, cardBorderColor),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("stock_card_${product.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Top row: Avatar + Name + Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Product Icon Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when {
                                expiryStatus == ExpiryStatus.EXPIRED -> CoralPink.copy(alpha = 0.15f)
                                stockStatus == ProductStockStatus.LOW_STOCK -> AmberOrange.copy(alpha = 0.15f)
                                else -> EmeraldPrimary.copy(alpha = 0.15f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            product.category.contains("সুগন্ধি") || product.category.contains("Fragrance") -> Icons.Default.Spa
                            else -> Icons.Default.Inventory2
                        },
                        contentDescription = null,
                        tint = when {
                            expiryStatus == ExpiryStatus.EXPIRED -> CoralPink
                            stockStatus == ProductStockStatus.LOW_STOCK -> AmberOrange
                            else -> EmeraldPrimary
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (product.brand.isNotBlank()) {
                            Text(product.brand, fontSize = 11.sp, color = EmeraldPrimary, fontWeight = FontWeight.SemiBold)
                            Text("•", fontSize = 10.sp, color = Slate600)
                        }
                        Text(product.category, fontSize = 11.sp, color = Slate400, maxLines = 1)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Badges Row: Stock Badge + Expiry Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stock Pill
                val stockBg = when (stockStatus) {
                    ProductStockStatus.OUT_OF_STOCK -> CoralPink.copy(alpha = 0.18f)
                    ProductStockStatus.LOW_STOCK -> AmberOrange.copy(alpha = 0.18f)
                    ProductStockStatus.IN_STOCK -> EmeraldPrimary.copy(alpha = 0.15f)
                }
                val stockFg = when (stockStatus) {
                    ProductStockStatus.OUT_OF_STOCK -> CoralPink
                    ProductStockStatus.LOW_STOCK -> AmberOrange
                    ProductStockStatus.IN_STOCK -> EmeraldPrimary
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(stockBg)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = when (stockStatus) {
                            ProductStockStatus.OUT_OF_STOCK -> "আউট অব স্টক"
                            ProductStockStatus.LOW_STOCK -> "কম: ${product.stockQuantity} ${product.unit}"
                            ProductStockStatus.IN_STOCK -> "স্টক: ${product.stockQuantity} ${product.unit}"
                        },
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = stockFg
                    )
                }

                // Expiry Pill
                if (product.expiryDate.isNotBlank()) {
                    val expBg = when (expiryStatus) {
                        ExpiryStatus.EXPIRED -> CoralPink.copy(alpha = 0.18f)
                        ExpiryStatus.EXPIRING_SOON -> AmberOrange.copy(alpha = 0.18f)
                        ExpiryStatus.SAFE -> Slate800
                        ExpiryStatus.NOT_SET -> Slate800
                    }
                    val expFg = when (expiryStatus) {
                        ExpiryStatus.EXPIRED -> CoralPink
                        ExpiryStatus.EXPIRING_SOON -> AmberOrange
                        ExpiryStatus.SAFE -> EmeraldPrimary
                        ExpiryStatus.NOT_SET -> Slate400
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(expBg)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = when (expiryStatus) {
                                ExpiryStatus.EXPIRED -> "⚠️ মেয়াদ শেষ"
                                ExpiryStatus.EXPIRING_SOON -> "⏳ $daysUntilExp দিন বাকি"
                                ExpiryStatus.SAFE -> "✓ মেয়াদ নিরাপদ"
                                ExpiryStatus.NOT_SET -> "তারিখ চেক"
                            },
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = expFg
                        )
                    }
                }

                if (product.barcode.isNotBlank()) {
                    Text(
                        text = "UPC: ${product.barcode.takeLast(6)}",
                        fontSize = 9.5.sp,
                        color = Slate400
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Slate800.copy(alpha = 0.7f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Pricing & Profit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("বিক্রয়: ", fontSize = 11.sp, color = Slate400)
                    Text("৳%,.0f".format(product.sellingPrice), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                    Text("•", fontSize = 10.sp, color = Slate700)
                    Text("ক্রয়: ৳%,.0f".format(product.purchasePrice), fontSize = 11.sp, color = Slate400)
                }

                if (profit > 0) {
                    Text("লাভ: ৳%,.0f".format(profit), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AmberOrange)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Quick Restock Button
                Surface(
                    onClick = onRestock,
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldPrimary.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f).height(34.dp).testTag("btn_card_restock_${product.id}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("রিস্টক", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                    }
                }

                // Adjust Stock Button
                Surface(
                    onClick = onAdjust,
                    shape = RoundedCornerShape(8.dp),
                    color = Slate800,
                    border = BorderStroke(1.dp, Slate600),
                    modifier = Modifier.weight(1f).height(34.dp).testTag("btn_card_adjust_${product.id}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = Slate200, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("অ্যাডজাস্ট", fontSize = 11.sp, color = Slate200)
                    }
                }

                // Barcode Action
                Surface(
                    onClick = onBarcode,
                    shape = RoundedCornerShape(8.dp),
                    color = Slate800,
                    border = BorderStroke(1.dp, Slate600),
                    modifier = Modifier.size(34.dp).testTag("btn_card_barcode_${product.id}")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.QrCode, contentDescription = "Barcode", tint = Slate200, modifier = Modifier.size(15.dp))
                    }
                }

                // Edit Action
                Surface(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    color = Slate800,
                    border = BorderStroke(1.dp, Slate600),
                    modifier = Modifier.size(34.dp).testTag("btn_card_edit_${product.id}")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Slate200, modifier = Modifier.size(14.dp))
                    }
                }

                // Delete Action
                Surface(
                    onClick = onDelete,
                    shape = RoundedCornerShape(8.dp),
                    color = CoralPink.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, CoralPink.copy(alpha = 0.5f)),
                    modifier = Modifier.size(34.dp).testTag("btn_card_delete_${product.id}")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CoralPink, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StockKpiCard(
    title: String,
    amount: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    badgeText: String? = null,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    testTag: String = ""
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) AmberOrange else Slate800
        ),
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 11.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate400,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (badgeText != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CoralPink.copy(alpha = 0.18f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoralPink
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = amount,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )

            Text(
                text = subtitle,
                fontSize = 10.5.sp,
                color = Slate400,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
