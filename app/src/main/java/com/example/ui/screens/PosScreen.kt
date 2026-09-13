package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.model.Product
import com.example.data.repository.SaleCartItem
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

@Composable
fun PosScreen(
    products: List<Product>,
    cartItems: List<SaleCartItem>,
    searchQuery: String,
    selectedCategory: String,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onAddToCart: (Product, String) -> Unit,
    onOpenCart: () -> Unit,
    // Optional Checkout callbacks for tablet/desktop split view:
    customerName: String = "",
    customerPhone: String = "",
    selectedCustomer: Customer? = null,
    isCash: Boolean = true,
    discount: Double = 0.0,
    paidAmountStr: String = "",
    allCustomers: List<Customer> = emptyList(),
    onClearCart: () -> Unit = {},
    onUpdateQuantity: (SaleCartItem, Int) -> Unit = { _, _ -> },
    onRemoveCartItem: (SaleCartItem) -> Unit = {},
    onCustomerNameChange: (String) -> Unit = {},
    onCustomerPhoneChange: (String) -> Unit = {},
    onSelectCustomer: (Customer?) -> Unit = {},
    onIsCashChange: (Boolean) -> Unit = {},
    onDiscountChange: (Double) -> Unit = {},
    onPaidAmountChange: (String) -> Unit = {},
    onCompleteSale: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val baseCategories = listOf("সব", "মেকআপ", "স্কিন কেয়ার", "হেয়ার কেয়ার", "সুগন্ধি", "বডি কেয়ার")
    val categories = remember(products) {
        val cats = baseCategories.toMutableList()
        products.forEach { p ->
            val simpleCat = p.category.substringBefore(" (").trim()
            if (simpleCat.isNotBlank() && simpleCat !in cats) {
                cats.add(simpleCat)
            }
        }
        cats
    }

    val filteredProducts = products.filter { product ->
        val matchesQuery = searchQuery.isBlank() ||
                product.name.contains(searchQuery, ignoreCase = true) ||
                product.sku.contains(searchQuery, ignoreCase = true) ||
                product.barcode.contains(searchQuery, ignoreCase = true) ||
                product.brand.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "সব" || product.category.contains(selectedCategory, ignoreCase = true)
        matchesQuery && matchesCategory
    }

    val totalCartItemsCount = cartItems.sumOf { it.quantity }
    val totalCartPrice = cartItems.sumOf { it.product.sellingPrice * it.quantity }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .testTag("screen_pos")
    ) {
        val screenWidth = maxWidth
        // Tablet / Desktop split layout active at 768dp+
        val isSplitLayout = screenWidth >= 768.dp
        val isDesktop = screenWidth >= 1100.dp
        val isSmallMobile = screenWidth < 360.dp

        if (isSplitLayout) {
            // =========================================================================
            // TABLET / DESKTOP TWO-COLUMN SPLIT LAYOUT (User Requirement 5)
            // Left (58%-62%): Products Grid, Search, Categories
            // Right (38%-42%): Embedded Live Cart, Customer, Payment, Checkout
            // =========================================================================
            val productWeight = if (isDesktop) 0.62f else 0.58f
            val cartWeight = 1f - productWeight
            val gridColumns = if (isDesktop) 3 else 2

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // LEFT: Product Grid & Search
                Column(
                    modifier = Modifier
                        .weight(productWeight)
                        .fillMaxHeight()
                ) {
                    PosHeaderAndSearch(
                        searchQuery = searchQuery,
                        onSearchChange = onSearchChange,
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategorySelect = onCategorySelect,
                        isCompact = false
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(gridColumns),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredProducts, key = { it.id }) { product ->
                            ProductPosCard(
                                product = product,
                                isSmall = false,
                                onAddToCart = { variant -> onAddToCart(product, variant) }
                            )
                        }

                        if (filteredProducts.isEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                EmptyProductsPlaceholder()
                            }
                        }
                    }
                }

                // RIGHT: Embedded Cart & Quick Checkout Panel
                Card(
                    modifier = Modifier
                        .weight(cartWeight)
                        .fillMaxHeight()
                        .border(1.dp, Slate800, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900)
                ) {
                    PosSideCartPanel(
                        cartItems = cartItems,
                        customerName = customerName,
                        customerPhone = customerPhone,
                        selectedCustomer = selectedCustomer,
                        isCash = isCash,
                        discount = discount,
                        paidAmountStr = paidAmountStr,
                        allCustomers = allCustomers,
                        onClearCart = onClearCart,
                        onUpdateQuantity = onUpdateQuantity,
                        onRemoveCartItem = onRemoveCartItem,
                        onCustomerNameChange = onCustomerNameChange,
                        onCustomerPhoneChange = onCustomerPhoneChange,
                        onSelectCustomer = onSelectCustomer,
                        onIsCashChange = onIsCashChange,
                        onDiscountChange = onDiscountChange,
                        onPaidAmountChange = onPaidAmountChange,
                        onCompleteSale = onCompleteSale
                    )
                }
            }
        } else {
            // =========================================================================
            // MOBILE STACKED LAYOUT (User Requirement 5)
            // Top: Search + Barcode Scanner
            // Middle: Categories + Products (1 column on small/normal mobile)
            // Bottom: Cart Summary + Checkout button
            // =========================================================================
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = if (isSmallMobile) 10.dp else 14.dp)
                ) {
                    PosHeaderAndSearch(
                        searchQuery = searchQuery,
                        onSearchChange = onSearchChange,
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategorySelect = onCategorySelect,
                        isCompact = isSmallMobile
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = if (cartItems.isNotEmpty()) 80.dp else 16.dp)
                    ) {
                        items(filteredProducts, key = { it.id }) { product ->
                            ProductPosCard(
                                product = product,
                                isSmall = isSmallMobile,
                                onAddToCart = { variant -> onAddToCart(product, variant) }
                            )
                        }

                        if (filteredProducts.isEmpty()) {
                            item {
                                EmptyProductsPlaceholder()
                            }
                        }
                    }
                }

                // Floating Bottom Cart Bar for Mobile
                if (cartItems.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Slate900),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, EmeraldPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .testTag("floating_cart_bar")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = CoralPink,
                                                contentColor = Color.White
                                            ) {
                                                Text(
                                                    text = totalCartItemsCount.toString(),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.ShoppingCart,
                                            contentDescription = "Cart",
                                            tint = EmeraldPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = "$totalCartItemsCount টি পণ্য",
                                            fontSize = 11.sp,
                                            color = Slate400
                                        )
                                        Text(
                                            text = "৳ ${"%,.0f".format(totalCartPrice)}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }
                                }

                                Button(
                                    onClick = onOpenCart,
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("button_view_cart")
                                ) {
                                    Text(
                                        text = "বিল সম্পন্ন করুন →",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PosHeaderAndSearch(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    categories: List<String>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    isCompact: Boolean
) {
    // Top Bar
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (isCompact) 8.dp else 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "বিক্রি (POS)",
                fontSize = if (isCompact) 18.sp else 21.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "দ্রুত পয়েন্ট অফ সেল ও ক্যাশ ড্রয়ার",
                fontSize = if (isCompact) 10.5.sp else 11.5.sp,
                color = Slate400
            )
        }

        IconButton(
            onClick = { /* barcode scan */ },
            modifier = Modifier
                .size(if (isCompact) 36.dp else 40.dp)
                .background(Slate850, RoundedCornerShape(10.dp))
                .border(1.dp, Slate800, RoundedCornerShape(10.dp))
                .testTag("button_scan_barcode")
        ) {
            Icon(
                Icons.Default.QrCodeScanner,
                contentDescription = "Scan Barcode",
                tint = EmeraldPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }

    // Search Field
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        placeholder = {
            Text(
                "পণ্যের নাম / Barcode স্ক্যান করুন...",
                color = Slate600,
                fontSize = if (isCompact) 12.sp else 13.sp
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
            .fillMaxWidth()
            .testTag("input_search_pos")
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Categories LazyRow
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(categories) { category ->
            val isSelected = selectedCategory == category
            Surface(
                onClick = { onCategorySelect(category) },
                shape = RoundedCornerShape(18.dp),
                color = if (isSelected) EmeraldPrimary else Slate900,
                border = BorderStroke(1.dp, if (isSelected) EmeraldPrimary else Slate800),
                modifier = Modifier.testTag("chip_category_$category")
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Slate300
                    )
                }
            }
        }
    }
}

/**
 * Tablet / Desktop Right Side Embedded Cart Panel
 */
@Composable
private fun PosSideCartPanel(
    cartItems: List<SaleCartItem>,
    customerName: String,
    customerPhone: String,
    selectedCustomer: Customer?,
    isCash: Boolean,
    discount: Double,
    paidAmountStr: String,
    allCustomers: List<Customer>,
    onClearCart: () -> Unit,
    onUpdateQuantity: (SaleCartItem, Int) -> Unit,
    onRemoveCartItem: (SaleCartItem) -> Unit,
    onCustomerNameChange: (String) -> Unit,
    onCustomerPhoneChange: (String) -> Unit,
    onSelectCustomer: (Customer?) -> Unit,
    onIsCashChange: (Boolean) -> Unit,
    onDiscountChange: (Double) -> Unit,
    onPaidAmountChange: (String) -> Unit,
    onCompleteSale: () -> Unit
) {
    val totalSubtotal = cartItems.sumOf { it.product.sellingPrice * it.quantity }
    val netTotal = (totalSubtotal - discount).coerceAtLeast(0.0)
    val parsedPaid = paidAmountStr.toDoubleOrNull() ?: if (isCash) netTotal else 0.0
    val dueAmount = (netTotal - parsedPaid).coerceAtLeast(0.0)

    var showCustomerPicker by remember { mutableStateOf(false) }
    var customerSearchQuery by remember { mutableStateOf("") }
    var discountText by remember(discount) {
        mutableStateOf(if (discount == 0.0) "" else if (discount == discount.toInt().toDouble()) discount.toInt().toString() else discount.toString())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "অর্ডার কার্ট (${cartItems.sumOf { it.quantity }})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            if (cartItems.isNotEmpty()) {
                IconButton(
                    onClick = onClearCart,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear",
                        tint = CoralPink,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = Slate800, thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))

        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Slate850),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Checkroom,
                            contentDescription = null,
                            tint = Slate600,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text = "কার্ট খালি রয়েছে",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate400
                    )
                    Text(
                        text = "বাম পাশের তালিকা থেকে পণ্য নির্বাচন করুন",
                        fontSize = 12.sp,
                        color = Slate600
                    )
                }
            }
        } else {
            // Scrollable Cart Items and Quick Inputs
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cartItems, key = { "${it.product.id}_${it.variant}" }) { item ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate950),
                        border = BorderStroke(1.dp, Slate800),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.product.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "৳ ${"%,.0f".format(item.product.sellingPrice)} × ${item.quantity}",
                                    fontSize = 11.5.sp,
                                    color = Slate400
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = { onUpdateQuantity(item, -1) },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(Slate850, CircleShape)
                                ) {
                                    Icon(
                                        Icons.Default.Remove,
                                        contentDescription = "Decrease",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                Text(
                                    text = item.quantity.toString(),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )

                                IconButton(
                                    onClick = { onUpdateQuantity(item, 1) },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(EmeraldPrimary.copy(alpha = 0.2f), CircleShape)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Increase",
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onRemoveCartItem(item) },
                                    modifier = Modifier
                                        .padding(start = 2.dp)
                                        .size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = CoralPink,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Customer Selection Pill
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate950),
                        border = BorderStroke(1.dp, Slate800),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (selectedCustomer != null) selectedCustomer.name else if (customerName.isNotBlank()) customerName else "খুচরা ক্রেতা (Retail)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                }

                                Text(
                                    text = if (showCustomerPicker) "বন্ধ করুন" else "কাস্টমার খুঁজুন",
                                    fontSize = 11.sp,
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { showCustomerPicker = !showCustomerPicker }
                                )
                            }

                            if (showCustomerPicker) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = customerSearchQuery,
                                    onValueChange = { customerSearchQuery = it },
                                    placeholder = { Text("নাম বা ফোন নম্বর...", fontSize = 11.sp, color = Slate600) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Slate900,
                                        unfocusedContainerColor = Slate900,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                val pickerList = allCustomers.filter {
                                    customerSearchQuery.isBlank() || it.name.contains(customerSearchQuery, true) || it.phone.contains(customerSearchQuery)
                                }.take(4)

                                pickerList.forEach { cust ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onSelectCustomer(cust)
                                                onCustomerNameChange(cust.name)
                                                onCustomerPhoneChange(cust.phone)
                                                showCustomerPicker = false
                                            }
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(cust.name, fontSize = 12.sp, color = TextPrimary)
                                        Text(if (cust.currentDue > 0) "বাকি: ৳${cust.currentDue.toInt()}" else "পরিশোধিত", fontSize = 11.sp, color = if (cust.currentDue > 0) AmberOrange else EmeraldPrimary)
                                    }
                                }
                            }
                        }
                    }
                }

                // Payment Mode Toggle: Cash vs Baki
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = { onIsCashChange(true) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isCash) EmeraldPrimary else Slate950,
                            border = BorderStroke(1.dp, if (isCash) EmeraldPrimary else Slate800),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "নগদ ক্যাশ",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCash) Color.White else Slate400
                                )
                            }
                        }

                        Surface(
                            onClick = { onIsCashChange(false) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (!isCash) AmberOrange else Slate950,
                            border = BorderStroke(1.dp, if (!isCash) AmberOrange else Slate800),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "বাকি খাতা",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isCash) Slate950 else Slate400
                                )
                            }
                        }
                    }
                }

                // Discount Input
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ডিসকাউন্ট (৳ ছাড়):", fontSize = 12.sp, color = Slate400)
                        OutlinedTextField(
                            value = discountText,
                            onValueChange = { input ->
                                val clean = input.filter { it.isDigit() || it == '.' }
                                discountText = clean
                                onDiscountChange(clean.toDoubleOrNull() ?: 0.0)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            placeholder = { Text("0", fontSize = 12.sp, color = Slate600) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Slate950,
                                unfocusedContainerColor = Slate950,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.width(110.dp)
                        )
                    }
                }
            }

            // Bottom Sticky Checkout Bar
            Column(modifier = Modifier.padding(top = 10.dp)) {
                HorizontalDivider(color = Slate800, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("সর্বমোট বিল:", fontSize = 13.sp, color = Slate400)
                    Text("৳ ${"%,.0f".format(netTotal)}", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                }

                if (!isCash && dueAmount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("বাকিতে যুক্ত হবে:", fontSize = 12.sp, color = AmberOrange)
                        Text("৳ ${"%,.0f".format(dueAmount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AmberOrange)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onCompleteSale,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("button_pos_split_complete_sale")
                ) {
                    Text(
                        text = "বিক্রয় সম্পন্ন করুন ✓",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ProductPosCard(
    product: Product,
    isSmall: Boolean = false,
    onAddToCart: (String) -> Unit
) {
    val isExpired = product.isExpired()
    val isOutOfStock = product.stockQuantity <= 0
    val canAdd = !isExpired && !isOutOfStock

    val variant = if (product.sizesOrVariants.isNotBlank() && product.sizesOrVariants != "স্ট্যান্ডার্ড") {
        product.sizesOrVariants
    } else "Standard"

    val borderColor = when {
        isExpired -> Color(0xFFEF4444).copy(alpha = 0.5f)
        isOutOfStock -> Color(0xFFEF4444).copy(alpha = 0.35f)
        product.stockQuantity <= product.lowStockThreshold -> AmberOrange.copy(alpha = 0.4f)
        else -> Slate800
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .testTag("product_pos_${product.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isSmall) 10.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Icon
            Box(
                modifier = Modifier
                    .size(if (isSmall) 44.dp else 50.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        when {
                            isExpired -> Color(0xFFEF4444).copy(alpha = 0.15f)
                            isOutOfStock -> Color(0xFFEF4444).copy(alpha = 0.12f)
                            else -> Slate850
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Checkroom,
                    contentDescription = product.name,
                    tint = when {
                        isExpired -> Color(0xFFEF4444)
                        isOutOfStock -> Slate600
                        else -> Slate400
                    },
                    modifier = Modifier.size(if (isSmall) 22.dp else 26.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontSize = if (isSmall) 13.5.sp else 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canAdd) TextPrimary else Slate400,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (product.brand.isNotBlank()) {
                        Text(
                            text = product.brand,
                            fontSize = 11.sp,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text("•", fontSize = 10.sp, color = Slate600)
                    }
                    Text(
                        text = product.category,
                        fontSize = 11.sp,
                        color = Slate400,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                if (isExpired) {
                    Text(
                        text = "⚠️ মেয়াদ উত্তীর্ণ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                } else if (isOutOfStock) {
                    Text(
                        text = "❌ স্টক শেষ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "স্টক: ${product.stockQuantity}",
                            fontSize = 11.sp,
                            color = if (product.stockQuantity <= product.lowStockThreshold) AmberOrange else Slate400
                        )
                        Text(
                            text = "৳ ${"%,.0f".format(product.sellingPrice)}",
                            fontSize = if (isSmall) 13.5.sp else 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }
                }
            }

            // Add '+' Button
            Surface(
                onClick = { if (canAdd) onAddToCart(variant) },
                shape = CircleShape,
                color = if (canAdd) EmeraldPrimary else Slate800,
                modifier = Modifier
                    .size(if (isSmall) 38.dp else 42.dp)
                    .testTag("button_add_${product.id}")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (isExpired) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = if (canAdd) "Add to Cart" else "Unavailable",
                        tint = if (canAdd) Color.White else Slate600,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyProductsPlaceholder() {
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(EmeraldPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Checkroom,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "কোনো পণ্য পাওয়া যায়নি",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "সার্চ ফিল্টার মুছে পুনরায় চেষ্টা করুন",
                fontSize = 12.sp,
                color = Slate400
            )
        }
    }
}
