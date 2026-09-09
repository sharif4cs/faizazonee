package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import android.widget.Toast
import com.example.data.model.Product
import com.example.ui.theme.AmberOrange
import com.example.ui.theme.CoralPink
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

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
    modifier: Modifier = Modifier
) {
    val categories = listOf("সব", "T-Shirt", "Shirt", "Pants", "কম স্টক")

    val totalPieces = if (products.isNotEmpty()) products.sumOf { it.stockQuantity } else 376
    val totalCost = if (products.isNotEmpty()) products.sumOf { it.purchasePrice * it.stockQuantity } else 181400.0
    val totalSale = if (products.isNotEmpty()) products.sumOf { it.sellingPrice * it.stockQuantity } else 291400.0
    val grossProfit = (totalSale - totalCost).coerceAtLeast(0.0)
    val lowStockCount = if (products.isNotEmpty()) products.count { it.stockQuantity <= it.lowStockThreshold || it.isLowStockAlert || it.stockQuantity <= 10 } else 2

    val filteredProducts = products.filter { product ->
        val matchesQuery = searchQuery.isBlank() ||
                product.name.contains(searchQuery, ignoreCase = true) ||
                product.sku.contains(searchQuery, ignoreCase = true)
        val matchesCategory = when (selectedCategory) {
            "সব" -> true
            "Low Stock", "কম স্টক" -> product.stockQuantity <= product.lowStockThreshold || product.isLowStockAlert || product.stockQuantity <= 10
            else -> product.category.equals(selectedCategory, ignoreCase = true)
        }
        matchesQuery && matchesCategory
    }

    val context = LocalContext.current
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var selectedProductForManagement by remember { mutableStateOf<Product?>(null) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .testTag("screen_stock")
    ) {
        val screenWidth = maxWidth
        val isTablet = screenWidth >= 640.dp
        val isDesktop = screenWidth >= 960.dp
        val gridColumns = if (isDesktop) 3 else if (isTablet) 2 else 1

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 1200.dp)
                .align(Alignment.TopCenter)
                .padding(horizontal = if (isTablet) 20.dp else 14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "স্টক ব্যবস্থাপনা",
                        fontSize = if (isTablet) 22.sp else 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "মজুদ পণ্য ও মূল্যের হিসাব",
                        fontSize = 11.5.sp,
                        color = Slate400
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate900)
                        .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "মোট পণ্য: ${products.size} টি | $totalPieces পিস",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldPrimary
                    )
                }
            }

            // Stock Valuation & Metrics Overview (Responsive KPI Cards)
            if (isTablet) {
                // Tablet/Desktop: All 4 in a single responsive row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StockKpiCard(
                        title = "স্টক ক্রয়মূল্য (Cost)",
                        amount = "৳ " + "%,.0f".format(totalCost),
                        subtitle = "$totalPieces টি মোট পিস",
                        icon = Icons.Default.Inventory2,
                        accentColor = Slate400,
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_stock_cost"
                    )

                    StockKpiCard(
                        title = "স্টক বিক্রয়মূল্য (Sale)",
                        amount = "৳ " + "%,.0f".format(totalSale),
                        subtitle = "সম্ভাব্য মোট বিক্রয়",
                        icon = Icons.Default.TrendingUp,
                        accentColor = EmeraldPrimary,
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_stock_sale"
                    )

                    StockKpiCard(
                        title = "সম্ভাব্য মোট গ্রস লাভ",
                        amount = "৳ " + "%,.0f".format(grossProfit),
                        subtitle = "বিক্রয়মূল্য - ক্রয়মূল্য",
                        icon = Icons.Default.MonetizationOn,
                        accentColor = EmeraldPrimary,
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_stock_gross_profit"
                    )

                    val isLowStockSelected = selectedCategory == "কম স্টক" || selectedCategory == "Low Stock"
                    StockKpiCard(
                        title = "কম স্টক আইটেম",
                        amount = "$lowStockCount",
                        subtitle = "রি-অর্ডার প্রয়োজন",
                        icon = Icons.Default.WarningAmber,
                        accentColor = if (lowStockCount > 0) CoralPink else Slate400,
                        badgeText = if (lowStockCount > 0) "রি-অর্ডার" else null,
                        isSelected = isLowStockSelected,
                        onClick = {
                            if (isLowStockSelected) {
                                onCategorySelect("সব")
                            } else {
                                onCategorySelect("কম স্টক")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_stock_low_stock"
                    )
                }
            } else {
                // Phone: 2 rows of 2
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StockKpiCard(
                            title = "স্টক ক্রয়মূল্য (Cost)",
                            amount = "৳ " + "%,.0f".format(totalCost),
                            subtitle = "$totalPieces টি মোট পিস",
                            icon = Icons.Default.Inventory2,
                            accentColor = Slate400,
                            modifier = Modifier.weight(1f),
                            testTag = "kpi_stock_cost"
                        )

                        StockKpiCard(
                            title = "স্টক বিক্রয়মূল্য (Sale)",
                            amount = "৳ " + "%,.0f".format(totalSale),
                            subtitle = "সম্ভাব্য মোট বিক্রয়",
                            icon = Icons.Default.TrendingUp,
                            accentColor = EmeraldPrimary,
                            modifier = Modifier.weight(1f),
                            testTag = "kpi_stock_sale"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StockKpiCard(
                            title = "সম্ভাব্য মোট গ্রস লাভ",
                            amount = "৳ " + "%,.0f".format(grossProfit),
                            subtitle = "বিক্রয়মূল্য - ক্রয়মূল্য",
                            icon = Icons.Default.MonetizationOn,
                            accentColor = EmeraldPrimary,
                            modifier = Modifier.weight(1f),
                            testTag = "kpi_stock_gross_profit"
                        )

                        val isLowStockSelected = selectedCategory == "কম স্টক" || selectedCategory == "Low Stock"
                        StockKpiCard(
                            title = "কম স্টক আইটেম",
                            amount = "$lowStockCount",
                            subtitle = "রি-অর্ডার প্রয়োজন",
                            icon = Icons.Default.WarningAmber,
                            accentColor = if (lowStockCount > 0) CoralPink else Slate400,
                            badgeText = if (lowStockCount > 0) "রি-অর্ডার" else null,
                            isSelected = isLowStockSelected,
                            onClick = {
                                if (isLowStockSelected) {
                                    onCategorySelect("সব")
                                } else {
                                    onCategorySelect("কম স্টক")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "kpi_stock_low_stock"
                        )
                    }
                }
            }

            // Search Bar + Filter
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
                            "পণ্যের নাম, SKU বা বারকোড...",
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
                            IconButton(
                                onClick = { onSearchChange("") },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Slate400,
                                    modifier = Modifier.size(16.dp)
                                )
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
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_search_stock")
                )

                IconButton(
                    onClick = { /* filter action */ },
                    modifier = Modifier
                        .size(50.dp)
                        .background(Slate900, RoundedCornerShape(12.dp))
                        .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = Slate400,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category Chips Row (Refined, uniform styling)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    Surface(
                        onClick = { onCategorySelect(category) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) EmeraldPrimary else Slate900,
                        border = BorderStroke(1.dp, if (isSelected) EmeraldPrimary else Slate800),
                        modifier = Modifier.testTag("stock_chip_$category")
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category,
                                fontSize = 12.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Slate300
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Responsive Product Inventory Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 84.dp)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    StockItemCard(
                        product = product,
                        onClick = {
                            selectedProductForManagement = product
                            onProductClick(product)
                        },
                        onEditClick = {
                            onEditProduct(product)
                        },
                        onDeleteClick = {
                            productToDelete = product
                        }
                    )
                }

                if (filteredProducts.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Slate900),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Slate800, RoundedCornerShape(16.dp))
                                .padding(top = 24.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
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
                                    text = if (searchQuery.isNotBlank() || selectedCategory != "সব") "নাম বা ক্যাটাগরি ফিল্টার চেক করুন" else "নিচের '+ নতুন পণ্য' বাটনে চাপ দিয়ে আপনার দোকানের আসল প্রোডাক্ট যোগ করুন",
                                    fontSize = 13.sp,
                                    color = Slate400,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom + নতুন পণ্য Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Button(
                onClick = onOpenAddProduct,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(24.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("button_add_stock_product")
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "নতুন পণ্য",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Product Management Dialog (Direct Stock Count Adjustment, Full Edit & Delete Option)
        if (selectedProductForManagement != null) {
            ProductManagementDialog(
                product = selectedProductForManagement!!,
                onDismiss = { selectedProductForManagement = null },
                onUpdateStock = { newQuantity ->
                    onUpdateStockQuantity(selectedProductForManagement!!, newQuantity)
                    selectedProductForManagement = null
                    Toast.makeText(context, "স্টক সফলভাবে আপডেট করা হয়েছে", Toast.LENGTH_SHORT).show()
                },
                onEditRequest = {
                    val prod = selectedProductForManagement!!
                    selectedProductForManagement = null
                    onEditProduct(prod)
                },
                onDeleteRequest = {
                    val prod = selectedProductForManagement!!
                    selectedProductForManagement = null
                    productToDelete = prod
                }
            )
        }

        // Delete Product Confirmation Dialog
        if (productToDelete != null) {
            val prod = productToDelete!!
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
                        text = "পণ্য ডিলিট / রিমুভ করবেন?",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "আপনি কি নিশ্চিতভাবে এই পণ্যটি স্টক থেকে সম্পূর্ণ মুছে ফেলতে চান?",
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
                                Text(
                                    text = prod.name,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "SKU: ${prod.sku} • বর্তমান স্টক: ${prod.stockQuantity} pcs",
                                    color = Slate400,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteProduct(prod)
                            productToDelete = null
                            Toast.makeText(context, "${prod.name} স্টক থেকে রিমুভ করা হয়েছে", Toast.LENGTH_SHORT).show()
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

@Composable
fun StockItemCard(
    product: Product,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLowStock = product.stockQuantity <= product.lowStockThreshold || product.isLowStockAlert || product.stockQuantity <= 10

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isLowStock) CoralPink.copy(alpha = 0.4f) else Slate800,
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .testTag("stock_card_${product.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Main Top Row: Thumbnail + Details + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail with contextual icon
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when {
                        product.category.contains("Shirt", ignoreCase = true) || product.category.contains("Pant", ignoreCase = true) -> Icons.Default.Checkroom
                        product.category.contains("Bag", ignoreCase = true) || product.category.contains("Shoe", ignoreCase = true) -> Icons.Default.ShoppingBag
                        else -> Icons.Default.Inventory2
                    }
                    Icon(
                        icon,
                        contentDescription = product.name,
                        tint = if (isLowStock) AmberOrange else Slate300,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Product Information
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Title
                    Text(
                        text = product.name,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // SKU & Category
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "SKU: ${product.sku}",
                            fontSize = 11.sp,
                            color = Slate400
                        )

                        if (product.category.isNotBlank() && product.category != "সব") {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF1E293B))
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = product.category,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Slate300
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Stock Quantity Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isLowStock) CoralPink.copy(alpha = 0.15f) else EmeraldPrimary.copy(alpha = 0.14f))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isLowStock) "⚠️ স্টক: ${product.stockQuantity} pcs" else "✓ স্টক: ${product.stockQuantity} pcs",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLowStock) CoralPink else EmeraldPrimary
                            )
                        }

                        if (product.sizesOrVariants.isNotBlank() && product.sizesOrVariants != "স্ট্যান্ডার্ড") {
                            Text(
                                text = "(${product.sizesOrVariants})",
                                fontSize = 10.5.sp,
                                color = Slate400,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Actions: Distinct, comfortably padded Edit & Delete buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quick Edit Button
                    Surface(
                        onClick = onEditClick,
                        shape = RoundedCornerShape(9.dp),
                        color = EmeraldPrimary.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("button_edit_product_${product.id}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "পণ্য এডিট করুন",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    // Quick Delete Button
                    Surface(
                        onClick = onDeleteClick,
                        shape = RoundedCornerShape(9.dp),
                        color = CoralPink.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, CoralPink.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("button_delete_product_${product.id}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "পণ্য রিমুভ করুন",
                                tint = CoralPink,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Slate800.copy(alpha = 0.8f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Pricing & Financial Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Selling Price
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "বিক্রয়: ",
                            fontSize = 11.5.sp,
                            color = Slate400
                        )
                        Text(
                            text = "৳ ${"%,.0f".format(product.sellingPrice)}",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }

                    Text(text = "•", color = Slate700, fontSize = 12.sp)

                    // Purchase Price
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ক্রয়: ",
                            fontSize = 11.5.sp,
                            color = Slate400
                        )
                        Text(
                            text = "৳ ${"%,.0f".format(product.purchasePrice)}",
                            fontSize = 12.sp,
                            color = Slate300
                        )
                    }
                }

                val profit = (product.sellingPrice - product.purchasePrice).coerceAtLeast(0.0)
                if (profit > 0) {
                    Text(
                        text = "লাভ: ৳ ${"%,.0f".format(profit)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AmberOrange
                    )
                } else {
                    Text(
                        text = "ম্যানেজ করতে ট্যাপ করুন ↗",
                        fontSize = 10.5.sp,
                        color = Slate400
                    )
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
        modifier = modifier
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) AmberOrange else Slate800,
                shape = RoundedCornerShape(12.dp)
            )
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
                        fontSize = 11.5.sp,
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
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoralPink
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = amount,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (accentColor == CoralPink && amount != "0") CoralPink else TextPrimary,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

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

@Composable
fun ProductManagementDialog(
    product: Product,
    onDismiss: () -> Unit,
    onUpdateStock: (Int) -> Unit,
    onEditRequest: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    var quantityInput by remember { mutableStateOf(product.stockQuantity.toString()) }
    val currentQty = quantityInput.toIntOrNull() ?: 0

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .border(1.dp, Slate800, RoundedCornerShape(16.dp))
                .testTag("dialog_manage_product")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "পণ্য ব্যবস্থাপনা ও রিমুভ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "SKU: ${product.sku} • ${product.category}",
                            fontSize = 11.5.sp,
                            color = Slate400
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Product Card Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate850)
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = product.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "ক্রয়মূল্য: ৳ ${"%,.0f".format(product.purchasePrice)}",
                                fontSize = 12.sp,
                                color = Slate400
                            )
                            Text(
                                text = "বিক্রয়মূল্য: ৳ ${"%,.0f".format(product.sellingPrice)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldPrimary
                            )
                        }
                        if (product.sizesOrVariants.isNotBlank()) {
                            Text(
                                text = "সাইজ / ভ্যারিয়েন্ট: ${product.sizesOrVariants}",
                                fontSize = 11.5.sp,
                                color = Slate400,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stock Adjustment Section
                Text(
                    text = "স্টক সংখ্যা পরিবর্তন / কমানো:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Decrease button
                    IconButton(
                        onClick = {
                            val newQ = (currentQty - 1).coerceAtLeast(0)
                            quantityInput = newQ.toString()
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate800)
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Minus",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Direct input field
                    OutlinedTextField(
                        value = quantityInput,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                quantityInput = input
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = Slate800,
                            unfocusedContainerColor = Slate800
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("input_adjust_stock_quantity")
                    )

                    // Increase button
                    IconButton(
                        onClick = {
                            val newQ = currentQty + 1
                            quantityInput = newQ.toString()
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate800)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Plus",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Quick reduce/add chips
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(-10, -5, -1, 5, 10).forEach { delta ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (delta < 0) Slate800 else EmeraldPrimary.copy(alpha = 0.15f))
                                .clickable {
                                    val newQ = (currentQty + delta).coerceAtLeast(0)
                                    quantityInput = newQ.toString()
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (delta > 0) "+$delta" else "$delta",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (delta < 0) Slate400 else EmeraldPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val finalQty = quantityInput.toIntOrNull() ?: 0
                        onUpdateStock(finalQty)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("button_save_stock_adjustment")
                ) {
                    Text(
                        text = "স্টক সংখ্যা সংরক্ষণ করুন",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Slate800, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Edit Product All Details Button
                OutlinedButton(
                    onClick = onEditRequest,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = EmeraldPrimary.copy(alpha = 0.08f),
                        contentColor = EmeraldPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("button_request_edit_product")
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "পণ্যের সকল তথ্য সম্পাদনা করুন (Edit All)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Delete Product Button
                OutlinedButton(
                    onClick = onDeleteRequest,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = CoralPink.copy(alpha = 0.08f),
                        contentColor = CoralPink
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CoralPink.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("button_request_delete_product")
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = CoralPink,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "স্টক থেকে এই পণ্যটি মুছে ফেলুন (Delete)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
