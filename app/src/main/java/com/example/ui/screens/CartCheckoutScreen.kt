package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.ui.theme.TextSecondary

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

    var showCustomerPicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .testTag("screen_cart_checkout")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("button_back_cart")) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Text(
                    text = "কার্ট ও চেকআউট",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
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
                    Text("সব মুছুন", color = CoralPink, fontSize = 13.sp)
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
        ) {
            // Cart Items
            items(cartItems) { item ->
                CartItemRow(
                    item = item,
                    onIncrease = { onUpdateQuantity(item, 1) },
                    onDecrease = { onUpdateQuantity(item, -1) },
                    onDelete = { onRemoveItem(item) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (cartItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "কার্টে কোনো পণ্য নেই। POS থেকে পণ্য যোগ করুন।",
                            color = Slate400,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                item {
                    Spacer(modifier = Modifier.height(14.dp))

                    // ক্রেতার তথ্য (ঐচ্ছিক)
                    Text(
                        text = "ক্রেতার তথ্য (ঐচ্ছিক)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Slate800, RoundedCornerShape(10.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
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
                                        tint = Slate400,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (selectedCustomer != null) selectedCustomer.name else "কাস্টমার নির্বাচন করুন",
                                        fontSize = 13.sp,
                                        color = if (selectedCustomer != null) EmeraldPrimary else Slate400,
                                        fontWeight = if (selectedCustomer != null) FontWeight.Bold else FontWeight.Normal
                                    )
                                }

                                Text(
                                    text = if (showCustomerPicker) "বন্ধ করুন" else "খুঁজুন / বাছাই",
                                    fontSize = 12.sp,
                                    color = EmeraldPrimary,
                                    modifier = Modifier
                                        .clickable { showCustomerPicker = !showCustomerPicker }
                                        .padding(4.dp)
                                )
                            }

                            if (showCustomerPicker) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "বিদ্যমান কাস্টমার তালিকা:",
                                    fontSize = 11.sp,
                                    color = Slate400
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                allCustomers.take(5).forEach { cust ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onSelectCustomer(cust)
                                                showCustomerPicker = false
                                            }
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = cust.name, fontSize = 13.sp, color = TextPrimary)
                                        Text(
                                            text = if (cust.currentDue > 0) "বাকি: ৳ ${"%,.0f".format(cust.currentDue)}" else "পরিশোধিত",
                                            fontSize = 11.sp,
                                            color = if (cust.currentDue > 0) AmberOrange else EmeraldPrimary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = customerName,
                                onValueChange = onCustomerNameChange,
                                placeholder = { Text("ক্রেতার নাম / মোবাইল নম্বর", color = Slate600, fontSize = 13.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Slate850,
                                    unfocusedContainerColor = Slate850,
                                    focusedBorderColor = EmeraldPrimary,
                                    unfocusedBorderColor = Slate700,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_checkout_customer")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // বিক্রির ধরন (ক্যাশ / বাকি toggle)
                    Text(
                        text = "বিক্রির ধরন",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Slate900)
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
                            Text(
                                text = "ক্যাশ",
                                fontSize = 14.sp,
                                fontWeight = if (isCash) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCash) Color.White else Slate400
                            )
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
                            Text(
                                text = "বাকি",
                                fontSize = 14.sp,
                                fontWeight = if (!isCash) FontWeight.Bold else FontWeight.Medium,
                                color = if (!isCash) Color.White else Slate400
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ডিসকাউন্ট Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ডিসকাউন্ট",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("৳", color = EmeraldPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            OutlinedTextField(
                                value = if (discount == 0.0) "0" else discount.toInt().toString(),
                                onValueChange = {
                                    val d = it.toDoubleOrNull() ?: 0.0
                                    onDiscountChange(d)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Slate900,
                                    unfocusedContainerColor = Slate900,
                                    focusedBorderColor = EmeraldPrimary,
                                    unfocusedBorderColor = Slate800,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier
                                    .width(90.dp)
                                    .height(48.dp)
                                    .testTag("input_checkout_discount")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Calculation Summary Card
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            SummaryLine("মোট টাকা", "৳ ${"%,.0f".format(totalSubtotal)}")
                            SummaryLine("ডিসকাউন্ট", "৳ ${"%,.0f".format(discount)}")
                            SummaryLine(
                                "পরিশোধিত",
                                if (isCash) "৳ ${"%,.0f".format(netTotal)}" else "৳ ${"%,.0f".format(parsedPaid)}"
                            )
                            SummaryLine(
                                if (isCash) "ফেরত টাকা" else "বাকি টাকা",
                                if (isCash) "৳ ${"%,.0f".format(changeAmount)}" else "৳ ${"%,.0f".format(dueAmount)}",
                                isHighlight = true,
                                highlightColor = if (!isCash && dueAmount > 0) AmberOrange else EmeraldPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        // Complete Sale Action Button
        if (cartItems.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
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
                    Text(
                        text = "বিক্রি সম্পন্ন করুন ✓",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
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
            .border(1.dp, Slate800, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate800),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Checkroom,
                    contentDescription = item.product.name,
                    tint = Slate400,
                    modifier = Modifier.size(24.dp)
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
                    color = Slate400,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            // Quantity Stepper [-] qty [+]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Slate800)
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
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary)
                        .clickable(onClick = onIncrease),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Increase",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Delete Icon
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = CoralPink,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Line Total
            Text(
                text = "৳ ${"%,.0f".format(lineTotal)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
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
            fontSize = 14.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlight) highlightColor else TextPrimary
        )
    }
}
