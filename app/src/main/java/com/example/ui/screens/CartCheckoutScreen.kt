package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.repository.SaleCartItem
import com.example.ui.theme.AmberOrange
import com.example.ui.theme.CoralPink
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.TextPrimary
import kotlin.math.roundToInt

@Composable
fun CartCheckoutScreen(
    cartItems: List<SaleCartItem>,
    customerName: String,
    customerPhone: String,
    selectedCustomer: Customer?,
    isCash: Boolean,
    discount: Double,
    paidAmountStr: String,
    allCustomers: List<Customer>,
    onBack: () -> Unit,
    onClearCart: () -> Unit,
    onUpdateQuantity: (SaleCartItem, Int) -> Unit,
    onRemoveItem: (SaleCartItem) -> Unit,
    onCustomerNameChange: (String) -> Unit,
    onCustomerPhoneChange: (String) -> Unit,
    onSelectCustomer: (Customer?) -> Unit,
    onIsCashChange: (Boolean) -> Unit,
    onDiscountChange: (Double) -> Unit,
    onPaidAmountChange: (String) -> Unit,
    onCompleteSale: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSubtotal = cartItems.sumOf { it.product.sellingPrice * it.quantity }
    val netTotal = (totalSubtotal - discount).coerceAtLeast(0.0)
    val parsedPaid = paidAmountStr.toDoubleOrNull() ?: if (isCash) netTotal else 0.0
    val changeAmount = (parsedPaid - netTotal).coerceAtLeast(0.0)
    val dueAmount = (netTotal - parsedPaid).coerceAtLeast(0.0)
    val totalItemCount = cartItems.sumOf { it.quantity }

    var showCustomerPicker by remember { mutableStateOf(false) }
    var customerSearchQuery by remember { mutableStateOf("") }

    // Discount text input state for smooth editing
    var discountInputText by remember(discount) {
        mutableStateOf(if (discount == 0.0) "" else if (discount == discount.toInt().toDouble()) discount.toInt().toString() else discount.toString())
    }

    // Filter customers for the picker
    val filteredPickerCustomers = remember(allCustomers, customerSearchQuery) {
        if (customerSearchQuery.isBlank()) {
            allCustomers.take(8)
        } else {
            allCustomers.filter {
                it.name.contains(customerSearchQuery, ignoreCase = true) ||
                        it.phone.contains(customerSearchQuery)
            }.take(8)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .testTag("screen_cart_checkout")
    ) {
        val screenWidth = maxWidth
        val isTablet = screenWidth >= 720.dp

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 1200.dp)
                .align(Alignment.TopCenter),
            containerColor = Slate950,
            topBar = {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (isTablet) 20.dp else 12.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Slate900)
                                .testTag("button_back_cart")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "কার্ট ও চেকআউট",
                                    fontSize = if (isTablet) 20.sp else 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                if (totalItemCount > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = EmeraldPrimary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "$totalItemCount টি পণ্য",
                                            fontSize = 12.sp,
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "পণ্য যাচাই ও চূড়ান্ত বিল সম্পন্ন করুন",
                                fontSize = 11.sp,
                                color = Slate400
                            )
                        }
                    }

                    if (cartItems.isNotEmpty()) {
                        TextButton(
                            onClick = onClearCart,
                            modifier = Modifier.testTag("button_clear_cart")
                        ) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Clear",
                                tint = CoralPink,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("সব মুছুন", color = CoralPink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            bottomBar = {
                // Mobile Sticky Bottom Action Button
                if (!isTablet && cartItems.isNotEmpty()) {
                    Surface(
                        color = Slate900,
                        tonalElevation = 8.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                        modifier = Modifier.navigationBarsPadding()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Button(
                                onClick = onCompleteSale,
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("button_complete_sale")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "বিক্রি সম্পন্ন করুন ✓",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate950
                                    )
                                    Text(
                                        text = "৳ ${"%,.0f".format(netTotal)}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate950
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            if (cartItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Slate900),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Checkroom,
                                contentDescription = null,
                                tint = Slate600,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Text(
                            text = "কার্টে কোনো পণ্য নেই",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "বিক্রি শুরু করতে POS স্ক্রিন থেকে পণ্য যোগ করুন।",
                            fontSize = 13.sp,
                            color = Slate400
                        )
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("POS এ ফিরে যান", color = Color.White)
                        }
                    }
                }
            } else if (isTablet) {
                // ==========================================
                // TABLET / DESKTOP TWO-COLUMN SPLIT LAYOUT
                // ==========================================
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Left Column: Items + Customer Info + Sale Type
                    LazyColumn(
                        modifier = Modifier.weight(1.15f),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        item {
                            Text(
                                text = "অর্ডারের পণ্যসমূহ (${cartItems.size} টি আইটেম)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate400
                            )
                        }

                        items(cartItems, key = { "${it.product.id}_${it.variant}" }) { item ->
                            CartItemRow(
                                item = item,
                                onIncrease = { onUpdateQuantity(item, 1) },
                                onDecrease = { onUpdateQuantity(item, -1) },
                                onDelete = { onRemoveItem(item) }
                            )
                        }

                        item {
                            CustomerInfoCard(
                                customerName = customerName,
                                customerPhone = customerPhone,
                                selectedCustomer = selectedCustomer,
                                allCustomers = filteredPickerCustomers,
                                showPicker = showCustomerPicker,
                                searchQuery = customerSearchQuery,
                                isTablet = true,
                                onTogglePicker = { showCustomerPicker = !showCustomerPicker },
                                onSearchQueryChange = { customerSearchQuery = it },
                                onCustomerNameChange = onCustomerNameChange,
                                onCustomerPhoneChange = onCustomerPhoneChange,
                                onSelectCustomer = {
                                    onSelectCustomer(it)
                                    if (it != null) {
                                        onCustomerNameChange(it.name)
                                        onCustomerPhoneChange(it.phone)
                                    }
                                    showCustomerPicker = false
                                },
                                onClearSelectedCustomer = {
                                    onSelectCustomer(null)
                                    onCustomerNameChange("")
                                    onCustomerPhoneChange("")
                                }
                            )
                        }

                        item {
                            SaleTypeCard(
                                isCash = isCash,
                                onIsCashChange = onIsCashChange
                            )
                        }
                    }

                    // Right Column: Discount + Payment Breakdown + Complete Sale Button
                    LazyColumn(
                        modifier = Modifier.weight(0.85f),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        item {
                            DiscountCard(
                                discount = discount,
                                discountInputText = discountInputText,
                                totalSubtotal = totalSubtotal,
                                onDiscountTextChange = { text ->
                                    if (text.all { it.isDigit() || it == '.' }) {
                                        discountInputText = text
                                        val parsed = text.toDoubleOrNull() ?: 0.0
                                        onDiscountChange(parsed)
                                    }
                                },
                                onApplyPreset = { presetValue ->
                                    onDiscountChange(presetValue)
                                    discountInputText = if (presetValue == 0.0) "" else presetValue.toInt().toString()
                                }
                            )
                        }

                        item {
                            PaymentDetailsCard(
                                isCash = isCash,
                                netTotal = netTotal,
                                paidAmountStr = paidAmountStr,
                                parsedPaid = parsedPaid,
                                changeAmount = changeAmount,
                                dueAmount = dueAmount,
                                onPaidAmountChange = onPaidAmountChange
                            )
                        }

                        item {
                            BillSummaryCard(
                                totalSubtotal = totalSubtotal,
                                discount = discount,
                                netTotal = netTotal,
                                parsedPaid = parsedPaid,
                                changeAmount = changeAmount,
                                dueAmount = dueAmount,
                                isCash = isCash
                            )
                        }

                        item {
                            Button(
                                onClick = onCompleteSale,
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("button_complete_sale")
                            ) {
                                Text(
                                    text = "বিক্রি সম্পন্ন করুন ✓ • ৳ ${"%,.0f".format(netTotal)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate950
                                )
                            }
                        }
                    }
                }
            } else {
                // ==========================================
                // MOBILE COMPACT SINGLE COLUMN LAYOUT (LAZY COLUMN)
                // ==========================================
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 28.dp)
                ) {
                    item {
                        Text(
                            text = "কার্টের পণ্য (${cartItems.size} প্রকার)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate400
                        )
                    }

                    items(cartItems, key = { "${it.product.id}_${it.variant}" }) { item ->
                        CartItemRow(
                            item = item,
                            onIncrease = { onUpdateQuantity(item, 1) },
                            onDecrease = { onUpdateQuantity(item, -1) },
                            onDelete = { onRemoveItem(item) }
                        )
                    }

                    item {
                        CustomerInfoCard(
                            customerName = customerName,
                            customerPhone = customerPhone,
                            selectedCustomer = selectedCustomer,
                            allCustomers = filteredPickerCustomers,
                            showPicker = showCustomerPicker,
                            searchQuery = customerSearchQuery,
                            isTablet = false,
                            onTogglePicker = { showCustomerPicker = !showCustomerPicker },
                            onSearchQueryChange = { customerSearchQuery = it },
                            onCustomerNameChange = onCustomerNameChange,
                            onCustomerPhoneChange = onCustomerPhoneChange,
                            onSelectCustomer = {
                                onSelectCustomer(it)
                                if (it != null) {
                                    onCustomerNameChange(it.name)
                                    onCustomerPhoneChange(it.phone)
                                }
                                showCustomerPicker = false
                            },
                            onClearSelectedCustomer = {
                                onSelectCustomer(null)
                                onCustomerNameChange("")
                                onCustomerPhoneChange("")
                            }
                        )
                    }

                    item {
                        SaleTypeCard(
                            isCash = isCash,
                            onIsCashChange = onIsCashChange
                        )
                    }

                    item {
                        DiscountCard(
                            discount = discount,
                            discountInputText = discountInputText,
                            totalSubtotal = totalSubtotal,
                            onDiscountTextChange = { text ->
                                if (text.all { it.isDigit() || it == '.' }) {
                                    discountInputText = text
                                    val parsed = text.toDoubleOrNull() ?: 0.0
                                    onDiscountChange(parsed)
                                }
                            },
                            onApplyPreset = { presetValue ->
                                onDiscountChange(presetValue)
                                discountInputText = if (presetValue == 0.0) "" else presetValue.toInt().toString()
                            }
                        )
                    }

                    item {
                        PaymentDetailsCard(
                            isCash = isCash,
                            netTotal = netTotal,
                            paidAmountStr = paidAmountStr,
                            parsedPaid = parsedPaid,
                            changeAmount = changeAmount,
                            dueAmount = dueAmount,
                            onPaidAmountChange = onPaidAmountChange
                        )
                    }

                    item {
                        BillSummaryCard(
                            totalSubtotal = totalSubtotal,
                            discount = discount,
                            netTotal = netTotal,
                            parsedPaid = parsedPaid,
                            changeAmount = changeAmount,
                            dueAmount = dueAmount,
                            isCash = isCash
                        )
                    }
                }
            }
        }
    }
}

/**
 * Customer Information Card with separate Name and Phone Number inputs
 * plus an optional customer search/picker from database.
 */
@Composable
fun CustomerInfoCard(
    customerName: String,
    customerPhone: String,
    selectedCustomer: Customer?,
    allCustomers: List<Customer>,
    showPicker: Boolean,
    searchQuery: String,
    isTablet: Boolean,
    onTogglePicker: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCustomerNameChange: (String) -> Unit,
    onCustomerPhoneChange: (String) -> Unit,
    onSelectCustomer: (Customer?) -> Unit,
    onClearSelectedCustomer: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header with search button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Customer",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "ক্রেতার তথ্য (ঐচ্ছিক)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                TextButton(
                    onClick = onTogglePicker,
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showPicker) "তালিকা লুকান" else "তালিকা থেকে বাছাই",
                        fontSize = 12.sp,
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Selected Customer Chip (if selected)
            if (selectedCustomer != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(EmeraldPrimary.copy(alpha = 0.12f))
                        .border(1.dp, EmeraldPrimary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = "সংরক্ষিত কাস্টমার: ${selectedCustomer.name}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "ফোন: ${selectedCustomer.phone.ifBlank { "নেই" }} • পূর্বের বাকি: ৳ ${"%,.0f".format(selectedCustomer.currentDue)}",
                                fontSize = 11.sp,
                                color = if (selectedCustomer.currentDue > 0) AmberOrange else Slate400
                            )
                        }
                    }

                    IconButton(
                        onClick = onClearSelectedCustomer,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Deselect",
                            tint = CoralPink,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Customer Picker Dropdown / List
            if (showPicker) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate850)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("নাম বা মোবাইল দিয়ে খুঁজুন...", color = Slate600, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Slate400, modifier = Modifier.size(16.dp))
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Slate900,
                            unfocusedContainerColor = Slate900,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (allCustomers.isEmpty()) {
                        Text(
                            text = "কোনো কাস্টমার পাওয়া যায়নি",
                            fontSize = 12.sp,
                            color = Slate400,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    } else {
                        allCustomers.forEach { cust ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { onSelectCustomer(cust) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = cust.name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                    Text(text = cust.phone, fontSize = 11.sp, color = Slate400)
                                }
                                Text(
                                    text = if (cust.currentDue > 0) "বাকি: ৳ ${"%,.0f".format(cust.currentDue)}" else "পরিশোধিত",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (cust.currentDue > 0) AmberOrange else EmeraldPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Name & Phone Input Fields
            if (isTablet) {
                // Tablet side-by-side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = onCustomerNameChange,
                        label = { Text("ক্রেতার নাম", fontSize = 12.sp) },
                        placeholder = { Text("যেমন: মো: রফিক", color = Slate600, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = "Name", tint = Slate400, modifier = Modifier.size(18.dp))
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Slate850,
                            unfocusedContainerColor = Slate850,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = EmeraldPrimary,
                            unfocusedLabelColor = Slate400
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_checkout_customer")
                    )

                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = onCustomerPhoneChange,
                        label = { Text("মোবাইল নম্বর", fontSize = 12.sp) },
                        placeholder = { Text("01XXXXXXXXX", color = Slate600, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = "Phone", tint = Slate400, modifier = Modifier.size(18.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Slate850,
                            unfocusedContainerColor = Slate850,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = EmeraldPrimary,
                            unfocusedLabelColor = Slate400
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_checkout_phone")
                    )
                }
            } else {
                // Mobile stacked
                OutlinedTextField(
                    value = customerName,
                    onValueChange = onCustomerNameChange,
                    label = { Text("ক্রেতার নাম", fontSize = 12.sp) },
                    placeholder = { Text("যেমন: মো: রফিক", color = Slate600, fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = "Name", tint = Slate400, modifier = Modifier.size(18.dp))
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Slate850,
                        unfocusedContainerColor = Slate850,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = EmeraldPrimary,
                        unfocusedLabelColor = Slate400
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_checkout_customer")
                )

                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = onCustomerPhoneChange,
                    label = { Text("মোবাইল নম্বর", fontSize = 12.sp) },
                    placeholder = { Text("01XXXXXXXXX", color = Slate600, fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = "Phone", tint = Slate400, modifier = Modifier.size(18.dp))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Slate850,
                        unfocusedContainerColor = Slate850,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = EmeraldPrimary,
                        unfocusedLabelColor = Slate400
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_checkout_phone")
                )
            }
        }
    }
}

/**
 * Sale Type Card (Cash vs Due toggle)
 */
@Composable
fun SaleTypeCard(
    isCash: Boolean,
    onIsCashChange: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "বিক্রির ধরন",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Slate850)
                    .border(1.dp, Slate800, RoundedCornerShape(10.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isCash) EmeraldPrimary else Color.Transparent)
                        .clickable { onIsCashChange(true) }
                        .padding(vertical = 10.dp)
                        .testTag("toggle_sale_cash"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Payments,
                            contentDescription = null,
                            tint = if (isCash) Color.White else Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "ক্যাশ (নগদ)",
                            fontSize = 14.sp,
                            fontWeight = if (isCash) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCash) Color.White else Slate400
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isCash) AmberOrange else Color.Transparent)
                        .clickable { onIsCashChange(false) }
                        .padding(vertical = 10.dp)
                        .testTag("toggle_sale_due"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = null,
                            tint = if (!isCash) Color(0xFF451A03) else Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "বাকি (খাতা)",
                            fontSize = 14.sp,
                            fontWeight = if (!isCash) FontWeight.Bold else FontWeight.Medium,
                            color = if (!isCash) Color(0xFF451A03) else Slate400
                        )
                    }
                }
            }
        }
    }
}

/**
 * Fixed & Polished Discount Card with clean input and quick discount presets
 */
@Composable
fun DiscountCard(
    discount: Double,
    discountInputText: String,
    totalSubtotal: Double,
    onDiscountTextChange: (String) -> Unit,
    onApplyPreset: (Double) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.LocalOffer,
                        contentDescription = "Discount",
                        tint = if (discount > 0) EmeraldPrimary else Slate400,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "ডিসকাউন্ট (ছাড়)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                if (discount > 0) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CoralPink.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "- ৳ ${"%,.0f".format(discount)} ছাড়",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoralPink,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Clean Discount OutlinedTextField with ৳ prefix and clear button
            OutlinedTextField(
                value = discountInputText,
                onValueChange = onDiscountTextChange,
                label = { Text("ছাড়ের পরিমাণ (টাকায়)", fontSize = 12.sp) },
                placeholder = { Text("০", color = Slate600, fontSize = 14.sp) },
                leadingIcon = {
                    Text(
                        text = "৳",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                trailingIcon = {
                    if (discount > 0) {
                        IconButton(
                            onClick = { onApplyPreset(0.0) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear Discount",
                                tint = Slate400,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Slate850,
                    unfocusedContainerColor = Slate850,
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = Slate700,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = EmeraldPrimary,
                    unfocusedLabelColor = Slate400
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_checkout_discount")
            )

            // Quick Preset Chips (Taka & Percentages)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PresetChip(
                    label = "০ ৳",
                    isSelected = discount == 0.0,
                    onClick = { onApplyPreset(0.0) }
                )
                PresetChip(
                    label = "৫০ ৳",
                    isSelected = discount == 50.0,
                    onClick = { onApplyPreset(50.0) }
                )
                PresetChip(
                    label = "১০০ ৳",
                    isSelected = discount == 100.0,
                    onClick = { onApplyPreset(100.0) }
                )
                PresetChip(
                    label = "২০০ ৳",
                    isSelected = discount == 200.0,
                    onClick = { onApplyPreset(200.0) }
                )
                val fivePct = (totalSubtotal * 0.05).roundToInt().toDouble()
                if (fivePct > 0) {
                    PresetChip(
                        label = "৫% (৳ ${fivePct.toInt()})",
                        isSelected = discount == fivePct,
                        onClick = { onApplyPreset(fivePct) }
                    )
                }
                val tenPct = (totalSubtotal * 0.10).roundToInt().toDouble()
                if (tenPct > 0) {
                    PresetChip(
                        label = "১০% (৳ ${tenPct.toInt()})",
                        isSelected = discount == tenPct,
                        onClick = { onApplyPreset(tenPct) }
                    )
                }
            }
        }
    }
}

@Composable
fun PresetChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) EmeraldPrimary.copy(alpha = 0.2f) else Slate850)
            .border(
                1.dp,
                if (isSelected) EmeraldPrimary else Slate700,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) EmeraldPrimary else Slate400
        )
    }
}

/**
 * Payment Details Card: handles cash received vs change, or partial advance vs due.
 */
@Composable
fun PaymentDetailsCard(
    isCash: Boolean,
    netTotal: Double,
    paidAmountStr: String,
    parsedPaid: Double,
    changeAmount: Double,
    dueAmount: Double,
    onPaidAmountChange: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (isCash) "পরিশোধ ও ফেরত হিসাব" else "বাকি ও জমা হিসাব",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            if (isCash) {
                OutlinedTextField(
                    value = paidAmountStr,
                    onValueChange = onPaidAmountChange,
                    label = { Text("গ্রহনকৃত নগদ টাকা", fontSize = 12.sp) },
                    placeholder = { Text("৳ ${"%,.0f".format(netTotal)} (ঠিক টাকা)", color = Slate600, fontSize = 13.sp) },
                    leadingIcon = {
                        Text(
                            text = "৳",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Slate850,
                        unfocusedContainerColor = Slate850,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = EmeraldPrimary,
                        unfocusedLabelColor = Slate400
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick Cash Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PresetChip(
                        label = "ঠিক টাকা (৳ ${netTotal.toInt()})",
                        isSelected = paidAmountStr.isEmpty() || paidAmountStr == netTotal.toInt().toString(),
                        onClick = { onPaidAmountChange(netTotal.toInt().toString()) }
                    )
                    val round500 = (Math.ceil(netTotal / 500.0) * 500.0).toInt()
                    if (round500 > netTotal) {
                        PresetChip(
                            label = "৳ $round500",
                            isSelected = paidAmountStr == round500.toString(),
                            onClick = { onPaidAmountChange(round500.toString()) }
                        )
                    }
                    val round1000 = (Math.ceil(netTotal / 1000.0) * 1000.0).toInt()
                    if (round1000 > netTotal && round1000 != round500) {
                        PresetChip(
                            label = "৳ $round1000",
                            isSelected = paidAmountStr == round1000.toString(),
                            onClick = { onPaidAmountChange(round1000.toString()) }
                        )
                    }
                }

                // Change Banner
                if (changeAmount > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(EmeraldPrimary.copy(alpha = 0.15f))
                            .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "গ্রাহককে ফেরত দিতে হবে",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Text(
                            text = "৳ ${"%,.0f".format(changeAmount)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }
                }
            } else {
                // Due Sale mode
                OutlinedTextField(
                    value = paidAmountStr,
                    onValueChange = onPaidAmountChange,
                    label = { Text("অগ্রিম জমা দেওয়া টাকা (যদি থাকে)", fontSize = 12.sp) },
                    placeholder = { Text("০ (সম্পূর্ণ বাকি)", color = Slate600, fontSize = 13.sp) },
                    leadingIcon = {
                        Text(
                            text = "৳",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberOrange,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Slate850,
                        unfocusedContainerColor = Slate850,
                        focusedBorderColor = AmberOrange,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = AmberOrange,
                        unfocusedLabelColor = Slate400
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Due Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AmberOrange.copy(alpha = 0.15f))
                        .border(1.dp, AmberOrange.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "বাকি খাতায় জমা হবে",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = "৳ ${"%,.0f".format(dueAmount)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberOrange
                    )
                }
            }
        }
    }
}

/**
 * Calculation Summary Card
 */
@Composable
fun BillSummaryCard(
    totalSubtotal: Double,
    discount: Double,
    netTotal: Double,
    parsedPaid: Double,
    changeAmount: Double,
    dueAmount: Double,
    isCash: Boolean
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SummaryLine("পণ্যের মোট মূল্য", "৳ ${"%,.0f".format(totalSubtotal)}")
            if (discount > 0) {
                SummaryLine("ডিসকাউন্ট (ছাড়)", "- ৳ ${"%,.0f".format(discount)}", isHighlight = true, highlightColor = CoralPink)
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = Slate800
            )
            SummaryLine("সর্বমোট প্রদেয়", "৳ ${"%,.0f".format(netTotal)}", isHighlight = true, highlightColor = TextPrimary)
            SummaryLine(
                title = if (isCash) "পরিশোধিত টাকা" else "নগদ জমা",
                value = if (isCash) "৳ ${"%,.0f".format(parsedPaid.coerceAtLeast(netTotal))}" else "৳ ${"%,.0f".format(parsedPaid)}"
            )
            if (isCash && changeAmount > 0) {
                SummaryLine("ফেরত টাকা", "৳ ${"%,.0f".format(changeAmount)}", isHighlight = true, highlightColor = EmeraldPrimary)
            } else if (!isCash && dueAmount > 0) {
                SummaryLine("বাকি টাকা", "৳ ${"%,.0f".format(dueAmount)}", isHighlight = true, highlightColor = AmberOrange)
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: SaleCartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onDelete: () -> Unit
) {
    val lineTotal = item.product.sellingPrice * item.quantity

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate700, RoundedCornerShape(12.dp))
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            val isNarrow = maxWidth < 460.dp
            if (isNarrow) {
                // Adaptive 2-row layout for mobile screens
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Slate800)
                                .border(1.dp, Slate700, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Checkroom,
                                contentDescription = item.product.name,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${item.product.name}${if (item.variant.isNotBlank() && item.variant != "ডিফল্ট") " (${item.variant})" else ""}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "একক মূল্য: ৳ ${"%,.0f".format(item.product.sellingPrice)}",
                                fontSize = 12.sp,
                                color = Slate400
                            )
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = CoralPink,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = Slate800, thickness = 0.8.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quantity Stepper [-] qty [+]
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Slate800)
                                    .border(1.dp, Slate700, CircleShape)
                                    .clickable(onClick = onDecrease),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Remove,
                                    contentDescription = "Decrease",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = item.quantity.toString(),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary)
                                    .clickable(onClick = onIncrease),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Increase",
                                    tint = Slate950,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Line Total
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "মোট:",
                                fontSize = 12.sp,
                                color = Slate400
                            )
                            Text(
                                text = "৳ ${"%,.0f".format(lineTotal)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        }
                    }
                }
            } else {
                // Wide / Tablet layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate800)
                            .border(1.dp, Slate700, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Checkroom,
                            contentDescription = item.product.name,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${item.product.name} (${item.variant})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "৳ ${"%,.0f".format(item.product.sellingPrice)} x ${item.quantity}",
                            fontSize = 12.sp,
                            color = Slate400
                        )
                    }

                    // Stepper
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Slate800)
                                .border(1.dp, Slate700, CircleShape)
                                .clickable(onClick = onDecrease),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Decrease",
                                tint = TextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = item.quantity.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary)
                                .clickable(onClick = onIncrease),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Increase",
                                tint = Slate950,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = CoralPink,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "৳ ${"%,.0f".format(lineTotal)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryLine(
    title: String,
    value: String,
    isHighlight: Boolean = false,
    highlightColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontSize = 13.sp, color = Slate400)
        Text(
            text = value,
            fontSize = if (isHighlight) 15.sp else 14.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlight) highlightColor else TextPrimary
        )
    }
}

