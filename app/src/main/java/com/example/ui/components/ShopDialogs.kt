package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Customer
import com.example.data.model.Expense
import com.example.data.model.Product
import com.example.data.model.ShopProfile
import com.example.ui.AccountBalances
import com.example.ui.CompletedSaleSummary
import com.example.ui.theme.*

class ProductSizeEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    initialSize: String = "",
    initialStock: String = ""
) {
    var size by mutableStateOf(initialSize)
    var stock by mutableStateOf(initialStock)
}

private fun parseVariantsToEntries(variants: String): List<ProductSizeEntry> {
    if (variants.isBlank() || variants == "স্ট্যান্ডার্ড" || variants == "Standard") return emptyList()
    val entries = mutableListOf<ProductSizeEntry>()
    val tokens = variants.split("|", ",", ";").map { it.trim() }.filter { it.isNotBlank() }
    for (token in tokens) {
        if (token.contains(":")) {
            val parts = token.split(":")
            val s = parts[0].trim()
            val qty = parts.getOrNull(1)?.filter { it.isDigit() } ?: ""
            entries.add(ProductSizeEntry(initialSize = s, initialStock = qty))
        } else if (token.contains("(") && token.contains(")")) {
            val s = token.substringBefore("(").trim()
            val qty = token.substringAfter("(").substringBefore(")").filter { it.isDigit() }
            entries.add(ProductSizeEntry(initialSize = s, initialStock = qty))
        } else {
            entries.add(ProductSizeEntry(initialSize = token, initialStock = ""))
        }
    }
    return entries
}

@Composable
fun AddProductDialog(
    editingProduct: Product? = null,
    onDismiss: () -> Unit,
    onConfirm: (
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
    ) -> Unit
) {
    var name by remember { mutableStateOf(editingProduct?.name ?: "") }
    var category by remember { mutableStateOf(editingProduct?.category ?: "Shirt") }
    var brand by remember { mutableStateOf(editingProduct?.brand ?: "") }
    var sku by remember { mutableStateOf(editingProduct?.sku ?: "") }
    var buyPrice by remember {
        mutableStateOf(
            if (editingProduct != null && editingProduct.purchasePrice > 0) "%.0f".format(editingProduct.purchasePrice) else ""
        )
    }
    var sellPrice by remember {
        mutableStateOf(
            if (editingProduct != null && editingProduct.sellingPrice > 0) "%.0f".format(editingProduct.sellingPrice) else ""
        )
    }
    var lowStockLimit by remember { mutableStateOf((editingProduct?.lowStockThreshold ?: 5).toString()) }
    var note by remember { mutableStateOf(editingProduct?.note ?: "") }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryText by remember { mutableStateOf("") }

    val categories = remember {
        val baseList = mutableListOf(
            "Shirt",
            "T-Shirt",
            "Pants",
            "Panjabi",
            "Polo Shirt",
            "Kids",
            "Borkha",
            "Hijab",
            "অন্যান্য"
        )
        if (editingProduct != null && editingProduct.category !in baseList && editingProduct.category.isNotBlank()) {
            baseList.add(0, editingProduct.category)
        }
        mutableStateListOf(*baseList.toTypedArray())
    }

    val sizeEntries = remember {
        val list = mutableStateListOf<ProductSizeEntry>()
        if (editingProduct != null) {
            val parsed = parseVariantsToEntries(editingProduct.sizesOrVariants)
            list.addAll(parsed)
        }
        list
    }
    val quickSizes = listOf("S", "M", "L", "XL", "XXL", "XXXL")

    // Dynamically calculate total stock by summing all non-blank stock entries
    val sizeTotalStock = sizeEntries.sumOf { it.stock.toIntOrNull() ?: 0 }
    val totalStock = if (sizeEntries.isNotEmpty()) sizeTotalStock else (editingProduct?.stockQuantity ?: 0)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101725)),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp)
                .padding(horizontal = 8.dp, vertical = 10.dp)
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
                .testTag("dialog_add_product")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header: Title & Close Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (editingProduct != null) "পণ্য তথ্য সম্পাদনা (Edit Product)" else "নতুন পণ্য যোগ করুন",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("button_close_add_product")
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Row 1: Product Name (Left) & Category with "+ নতুন ক্যাটাগরি" (Right)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1.35f)) {
                            Text(
                                text = "পণ্যের নাম (Product Name)*:",
                                fontSize = 11.sp,
                                color = Slate400,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                            SleekInputField(
                                value = name,
                                onValueChange = { name = it },
                                placeholder = "যেমন: Formal Cotton Shirt",
                                testTag = "input_product_name"
                            )
                        }

                        CategorySelectorField(
                            category = category,
                            onSelectCategory = { category = it },
                            onOpenNewCategory = {
                                newCategoryText = ""
                                showNewCategoryDialog = true
                            },
                            categories = categories,
                            expanded = categoryDropdownExpanded,
                            onExpandedChange = { categoryDropdownExpanded = it },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 2: Brand / Company (Left) & SKU / Barcode (Right)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ব্র্যান্ড / কোম্পানি:",
                                fontSize = 11.sp,
                                color = Slate400,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                            SleekInputField(
                                value = brand,
                                onValueChange = { brand = it },
                                placeholder = "যেমন: Faiza Fashion",
                                testTag = "input_product_brand"
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SKU / বারকোড:",
                                fontSize = 11.sp,
                                color = Slate400,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                            SleekInputField(
                                value = sku,
                                onValueChange = { sku = it },
                                placeholder = "যেমন: SHT-001",
                                testTag = "input_product_sku"
                            )
                        }
                    }

                    // Row 3: Purchase Price, Selling Price & Low Stock Limit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(0.9f)) {
                            Text(
                                text = "ক্রয়মূল্য",
                                fontSize = 11.sp,
                                color = Slate400,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                            SleekInputField(
                                value = buyPrice,
                                onValueChange = { buyPrice = it },
                                placeholder = "যেমন: 350",
                                keyboardType = KeyboardType.Number,
                                testTag = "input_buy_price"
                            )
                        }

                        Column(modifier = Modifier.weight(1.35f)) {
                            Text(
                                text = "সম্ভাব্য বিক্রয়মূল্য (Selling Price ৳):",
                                fontSize = 10.5.sp,
                                color = Slate400,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                            SleekInputField(
                                value = sellPrice,
                                onValueChange = { sellPrice = it },
                                placeholder = "যেমন: 550 বা 750",
                                keyboardType = KeyboardType.Number,
                                testTag = "input_sell_price"
                            )
                        }

                        Column(modifier = Modifier.weight(0.95f)) {
                            Text(
                                text = "কম স্টক এলার্ট সীমা:",
                                fontSize = 10.5.sp,
                                color = Slate400,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                            SleekInputField(
                                value = lowStockLimit,
                                onValueChange = { lowStockLimit = it },
                                placeholder = "5",
                                keyboardType = KeyboardType.Number,
                                testTag = "input_low_stock_limit"
                            )
                        }
                    }

                    // Size & Stock Container Box (Green Accent Border)
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1322)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF00B87C), RoundedCornerShape(10.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            // Header: Title & "+ সাইজ যোগ করুন" button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "সাইজ ও স্টক (Size & Stock)*:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "প্রতিটি সাইজের স্টক আলাদাভাবে সংরক্ষণ হবে।",
                                        fontSize = 10.sp,
                                        color = Slate400,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Button(
                                    onClick = {
                                        sizeEntries.add(ProductSizeEntry(initialSize = "", initialStock = "0"))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B87C)),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                    modifier = Modifier
                                        .height(30.dp)
                                        .testTag("button_add_size_row")
                                ) {
                                    Text(
                                        text = "+ সাইজ যোগ করুন",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Quick sizes row: "কুইক সাইজ যোগ: + S  + M  + L  + XL  + XXL  + XXXL"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = "কুইক সাইজ যোগ:",
                                    fontSize = 11.sp,
                                    color = Slate400,
                                    modifier = Modifier.padding(end = 2.dp)
                                )

                                quickSizes.forEach { qSize ->
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF141E30), RoundedCornerShape(5.dp))
                                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(5.dp))
                                            .clickable {
                                                val existing = sizeEntries.indexOfFirst { it.size.equals(qSize, ignoreCase = true) }
                                                if (existing == -1) {
                                                    sizeEntries.add(ProductSizeEntry(initialSize = qSize, initialStock = "0"))
                                                }
                                            }
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                            .testTag("button_quick_size_$qSize")
                                    ) {
                                        Text(
                                            text = "+ $qSize",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Sizes Table Header Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF111A2C), RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "সাইজ (Size)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Slate300,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "প্রারম্ভিক স্টক (Initial Stock)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Slate300,
                                    modifier = Modifier.weight(1.3f)
                                )
                                Text(
                                    text = "মুছুন",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Slate300,
                                    modifier = Modifier.width(36.dp),
                                    textAlign = TextAlign.Center
                                )
                            }

                            // Size Items
                            if (sizeEntries.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "কোনো সাইজ যোগ করা হয়নি। উপরের কুইক সাইজ বা '+ সাইজ যোগ করুন' বাটনে চাপুন।",
                                        fontSize = 11.sp,
                                        color = Slate500,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.height(6.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    sizeEntries.forEachIndexed { index, entry ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Size input
                                            SleekInputField(
                                                value = entry.size,
                                                onValueChange = { entry.size = it },
                                                placeholder = "যেমন: M",
                                                modifier = Modifier.weight(1f),
                                                testTag = "input_size_row_$index"
                                            )

                                            // Stock input
                                            SleekInputField(
                                                value = entry.stock,
                                                onValueChange = { entry.stock = it.filter { ch -> ch.isDigit() } },
                                                placeholder = "0",
                                                keyboardType = KeyboardType.Number,
                                                modifier = Modifier.weight(1.3f),
                                                testTag = "input_stock_row_$index"
                                            )

                                            // Delete row button
                                            IconButton(
                                                onClick = { sizeEntries.remove(entry) },
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .testTag("button_delete_size_row_$index")
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Remove Size",
                                                    tint = Color(0xFFEF4444),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Bottom Right: "মোট স্টক (Total Stock): 0 pcs"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "মোট স্টক (Total Stock): ",
                                    fontSize = 11.sp,
                                    color = Slate400
                                )
                                Text(
                                    text = "$totalStock pcs",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00B87C)
                                )
                            }
                        }
                    }

                    // Note / Description Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "নোট / বিবরণ:",
                            fontSize = 11.sp,
                            color = Slate400,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                        SleekInputField(
                            value = note,
                            onValueChange = { note = it },
                            placeholder = "কাপড়ের বিবরণ ইত্যাদি",
                            testTag = "input_product_note"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Footer Buttons: [বাতিল]  [সংরক্ষণ করুন]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(6.dp))
                            .testTag("button_cancel_add_product")
                    ) {
                        Text(
                            text = "বাতিল",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate300
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val validSizes = sizeEntries.filter { it.size.isNotBlank() }
                                val formattedVariants = if (validSizes.isNotEmpty()) {
                                    validSizes.joinToString(" | ") { "${it.size}:${it.stock.toIntOrNull() ?: 0}" }
                                } else {
                                    editingProduct?.sizesOrVariants?.takeIf { it.isNotBlank() } ?: "স্ট্যান্ডার্ড"
                                }
                                val calculatedStock = if (validSizes.isNotEmpty()) {
                                    validSizes.sumOf { it.stock.toIntOrNull() ?: 0 }
                                } else {
                                    editingProduct?.stockQuantity ?: 0
                                }

                                onConfirm(
                                    name,
                                    category,
                                    sku,
                                    buyPrice.toDoubleOrNull() ?: 0.0,
                                    sellPrice.toDoubleOrNull() ?: 0.0,
                                    calculatedStock,
                                    formattedVariants,
                                    brand,
                                    lowStockLimit.toIntOrNull() ?: 5,
                                    note
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B87C)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("button_save_product")
                    ) {
                        Text(
                            text = if (editingProduct != null) "আপডেট সংরক্ষণ করুন" else "সংরক্ষণ করুন",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    // Modal dialog to add a new category
    if (showNewCategoryDialog) {
        Dialog(onDismissRequest = { showNewCategoryDialog = false }) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101725)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "নতুন ক্যাটাগরি যোগ করুন",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SleekInputField(
                        value = newCategoryText,
                        onValueChange = { newCategoryText = it },
                        placeholder = "যেমন: Formal Shirt, পাঞ্জাবি"
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { showNewCategoryDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(6.dp))
                        ) {
                            Text("বাতিল", color = Slate300, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val trimmed = newCategoryText.trim()
                                if (trimmed.isNotBlank()) {
                                    if (!categories.contains(trimmed)) {
                                        categories.add(0, trimmed)
                                    }
                                    category = trimmed
                                    showNewCategoryDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B87C)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("যোগ করুন", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// Backward compatibility overload
@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String, sku: String, buy: Double, sell: Double, stock: Int, variants: String) -> Unit
) {
    AddProductDialog(
        onDismiss = onDismiss,
        onConfirm = { name, category, sku, buy, sell, stock, variants, _, _, _ ->
            onConfirm(name, category, sku, buy, sell, stock, variants)
        }
    )
}

@Composable
private fun CategorySelectorField(
    category: String,
    onSelectCategory: (String) -> Unit,
    onOpenNewCategory: () -> Unit,
    categories: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ক্যাটাগরি",
                fontSize = 11.sp,
                color = Slate400,
                modifier = Modifier.padding(bottom = 3.dp)
            )

            Box(
                modifier = Modifier
                    .border(1.dp, Color(0xFF00B87C), RoundedCornerShape(10.dp))
                    .clickable { onOpenNewCategory() }
                    .padding(horizontal = 6.dp, vertical = 1.dp)
                    .testTag("button_add_new_category")
            ) {
                Text(
                    text = "+ নতুন ক্যাটাগরি",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00B87C)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                .border(1.dp, if (expanded) Color(0xFF00B87C) else Color(0xFF1E293B), RoundedCornerShape(6.dp))
                .clickable { onExpandedChange(!expanded) }
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.ifBlank { "Shirt" },
                    fontSize = 12.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Dropdown",
                    tint = Slate400,
                    modifier = Modifier.size(18.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) },
                modifier = Modifier
                    .background(Color(0xFF101725))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(6.dp))
            ) {
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                color = if (cat == category) Color(0xFF00B87C) else TextPrimary,
                                fontWeight = if (cat == category) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onSelectCategory(cat)
                            onExpandedChange(false)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SleekInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = TextStyle(
            color = Color.White,
            fontSize = 12.sp
        ),
        cursorBrush = SolidColor(Color(0xFF00B87C)),
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
            .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp)
            .testTag(testTag),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = Color(0xFF64748B),
                        fontSize = 11.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun ProductFieldItem(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Slate400,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(bottom = 3.dp)
        )
        SleekInputField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            keyboardType = keyboardType,
            testTag = testTag
        )
    }
}

@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, address: String, initialDue: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var initialDue by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            modifier = Modifier.fillMaxWidth().testTag("dialog_add_customer")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "নতুন কাস্টমার যোগ করুন",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                CustomTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "কাস্টমারের নাম (Customer Name)",
                    placeholder = "যেমন: হাসান মাহমুদ",
                    testTag = "input_customer_name"
                )

                CustomTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "মোবাইল নম্বর (Phone)",
                    placeholder = "017XXXXXXXX",
                    keyboardType = KeyboardType.Phone,
                    testTag = "input_customer_phone"
                )

                CustomTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "ঠিকানা (ঐচ্ছিক)",
                    placeholder = "মিরপুর, ঢাকা",
                    testTag = "input_customer_address"
                )

                CustomTextField(
                    value = initialDue,
                    onValueChange = { initialDue = it },
                    label = "পূর্বের বাকি (যদি থাকে ৳)",
                    placeholder = "0",
                    keyboardType = KeyboardType.Number,
                    testTag = "input_customer_initial_due"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onConfirm(
                                name,
                                phone,
                                address,
                                initialDue.toDoubleOrNull() ?: 0.0
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("button_save_customer")
                ) {
                    Text("কাস্টমার যুক্ত করুন ✓", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

val EXPENSE_CATEGORIES = listOf(
    "দোকান ভাড়া",
    "বিদ্যুৎ বিল",
    "কর্মচারীর বেতন",
    "ইন্টারনেট",
    "চা-নাস্তা ও আপ্যায়ন",
    "মেরামত ও রক্ষণাবেক্ষণ",
    "পরিবহন / ডেলিভারি",
    "প্যাকেজিং",
    "বিজ্ঞাপন / মার্কেটিং",
    "অন্যান্য"
)

val PAYMENT_METHOD_OPTIONS = listOf(
    "নগদ ক্যাশ (ক্যাশ ব্যালেন্স থেকে কমবে)",
    "bKash",
    "Nagad",
    "ব্যাংক"
)

private fun parseLocalizedAmount(input: String): Double? {
    val clean = input.trim()
        .replace('০', '0')
        .replace('১', '1')
        .replace('২', '2')
        .replace('৩', '3')
        .replace('৪', '4')
        .replace('৫', '5')
        .replace('৬', '6')
        .replace('৭', '7')
        .replace('৮', '8')
        .replace('৯', '9')
        .replace(",", "")
    return clean.toDoubleOrNull()
}

val QUICK_EXPENSE_CATEGORIES = listOf(
    "চা-নাস্তা ও আপ্যায়ন",
    "দোকান ভাড়া",
    "বিদ্যুৎ বিল",
    "কর্মচারীর বেতন",
    "পরিবহন / ডেলিভারি",
    "প্যাকেজিং",
    "ইন্টারনেট",
    "অন্যান্য"
)

@Composable
fun ExpenseEntryDialog(
    editingExpense: Expense? = null,
    accountBalances: AccountBalances,
    onDismiss: () -> Unit,
    onConfirm: (category: String, customCategory: String, amount: Double, paymentMethod: String, note: String) -> Unit,
    onOpenManageBalances: () -> Unit = {}
) {
    var selectedCategory by remember {
        mutableStateOf(
            editingExpense?.let {
                if (EXPENSE_CATEGORIES.contains(it.category)) it.category else "অন্যান্য"
            } ?: "চা-নাস্তা ও আপ্যায়ন"
        )
    }

    var customCategory by remember {
        mutableStateOf(
            editingExpense?.let {
                if (it.category == "অন্যান্য") it.customCategory.ifBlank { it.title }
                else if (!EXPENSE_CATEGORIES.contains(it.category)) it.category
                else ""
            } ?: ""
        )
    }

    var amountText by remember {
        mutableStateOf(
            editingExpense?.let { if (it.amount == 0.0) "" else "%,.0f".format(it.amount).replace(",", "") } ?: ""
        )
    }

    var selectedPaymentMethod by remember {
        mutableStateOf(
            editingExpense?.let { exp ->
                when {
                    exp.paymentMethod.startsWith("নগদ") || exp.paymentMethod.contains("Cash", ignoreCase = true) ->
                        "নগদ ক্যাশ (ক্যাশ ব্যালেন্স থেকে কমবে)"
                    exp.paymentMethod.contains("bKash", ignoreCase = true) || exp.paymentMethod.contains("বিকাশ") ->
                        "bKash"
                    exp.paymentMethod.contains("Nagad", ignoreCase = true) || exp.paymentMethod.contains("নগদ") ->
                        "Nagad"
                    exp.paymentMethod.contains("ব্যাংক") || exp.paymentMethod.contains("Bank", ignoreCase = true) ->
                        "ব্যাংক"
                    else -> "নগদ ক্যাশ (ক্যাশ ব্যালেন্স থেকে কমবে)"
                }
            } ?: "নগদ ক্যাশ (ক্যাশ ব্যালেন্স থেকে কমবে)"
        )
    }

    var noteText by remember {
        mutableStateOf(editingExpense?.note ?: "")
    }

    var isCategoryDropdownOpen by remember { mutableStateOf(false) }

    val baseAvailable = when {
        selectedPaymentMethod.startsWith("নগদ") -> accountBalances.cash
        selectedPaymentMethod.equals("bKash", ignoreCase = true) -> accountBalances.bkash
        selectedPaymentMethod.equals("Nagad", ignoreCase = true) -> accountBalances.nagad
        selectedPaymentMethod.equals("ব্যাংক", ignoreCase = true) -> accountBalances.bank
        else -> accountBalances.cash
    }

    val effectiveAvailable = if (editingExpense != null && editingExpense.paymentMethod == selectedPaymentMethod) {
        baseAvailable + editingExpense.amount
    } else {
        baseAvailable
    }

    val parsedAmount = parseLocalizedAmount(amountText)
    val isAmountValid = parsedAmount != null && parsedAmount > 0.0
    val isBalanceSufficient = parsedAmount == null || parsedAmount <= effectiveAvailable
    val isCategoryValid = if (selectedCategory == "অন্যান্য") customCategory.isNotBlank() else true
    val canSave = isAmountValid && isBalanceSufficient && isCategoryValid

    val warningMessage = when {
        selectedPaymentMethod.startsWith("নগদ") -> "ক্যাশ ব্যালেন্স পর্যাপ্ত নেই।"
        selectedPaymentMethod.equals("bKash", ignoreCase = true) -> "bKash ব্যালেন্স পর্যাপ্ত নেই।"
        selectedPaymentMethod.equals("Nagad", ignoreCase = true) -> "Nagad ব্যালেন্স পর্যাপ্ত নেই।"
        selectedPaymentMethod.equals("ব্যাংক", ignoreCase = true) -> "ব্যাংক ব্যালেন্স পর্যাপ্ত নেই।"
        else -> "ব্যালেন্স পর্যাপ্ত নেই।"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .systemBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            val screenWidth = maxWidth
            val screenHeight = maxHeight
            val isTablet = screenWidth >= 600.dp
            val isLandscape = screenWidth > screenHeight && screenHeight < 560.dp
            val isWideScreen = isTablet || isLandscape || screenWidth >= 580.dp
            val isCompact = screenWidth < 380.dp
            val isShortHeight = screenHeight < 500.dp
            val cardPadding = if (isCompact) 12.dp else 16.dp

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                modifier = Modifier
                    .then(
                        if (screenWidth >= 820.dp) Modifier.width(760.dp)
                        else if (isTablet) Modifier.width(680.dp)
                        else if (isLandscape) Modifier.width(620.dp)
                        else Modifier.fillMaxWidth()
                    )
                    .heightIn(max = screenHeight - (if (isShortHeight) 10.dp else 24.dp))
                    .border(1.5.dp, EmeraldPrimary.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                    .testTag("dialog_add_expense")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header Bar (fixed top)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate850)
                            .padding(horizontal = cardPadding, vertical = if (isShortHeight) 8.dp else 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(EmeraldPrimary.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (editingExpense != null) "খরচ সম্পাদনা" else "দোকানের নতুন খরচ এন্ট্রি",
                                    fontSize = if (isCompact) 16.sp else 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "সঠিক খাত ও পেমেন্ট মেথড নির্বাচন করুন",
                                    fontSize = 11.sp,
                                    color = Slate400
                                )
                            }
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Slate800, CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400, modifier = Modifier.size(16.dp))
                        }
                    }

                    Divider(color = Slate800, thickness = 1.dp)

                    // Responsive Form Content
                    if (isWideScreen) {
                        // 2-Column Responsive Layout for Tablet & Landscape
                        Row(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .padding(horizontal = cardPadding, vertical = if (isShortHeight) 8.dp else 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Left Column: Category & Payment Method
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                // Category Selection
                                ExpenseCategorySelectorSection(
                                    selectedCategory = selectedCategory,
                                    isDropdownOpen = isCategoryDropdownOpen,
                                    customCategory = customCategory,
                                    onToggleDropdown = { isCategoryDropdownOpen = !isCategoryDropdownOpen },
                                    onSelectCategory = {
                                        selectedCategory = it
                                        isCategoryDropdownOpen = false
                                    },
                                    onCustomCategoryChange = { customCategory = it }
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Payment Method Selection
                                ExpensePaymentMethodSection(
                                    selectedPaymentMethod = selectedPaymentMethod,
                                    accountBalances = accountBalances,
                                    isCompact = isCompact,
                                    useGrid = true,
                                    onSelectMethod = { selectedPaymentMethod = it }
                                )
                            }

                            // Right Column: Amount, Quick Add, Balance Feedback, Note
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                // Amount & Quick Chips
                                ExpenseAmountSection(
                                    amountText = amountText,
                                    onAmountChange = { amountText = it },
                                    isAmountValid = isAmountValid,
                                    parsedAmount = parsedAmount,
                                    isBalanceSufficient = isBalanceSufficient,
                                    effectiveAvailable = effectiveAvailable,
                                    warningMessage = warningMessage,
                                    onOpenManageBalances = onOpenManageBalances
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Note
                                CustomTextField(
                                    value = noteText,
                                    onValueChange = { noteText = it },
                                    label = "বিবরণ / নোট (ঐচ্ছিক)",
                                    placeholder = "যেমন: কর্মচারীর লাঞ্চ ও চা বিল",
                                    testTag = "input_expense_note"
                                )
                            }
                        }
                    } else {
                        // Single Column Layout for Standard & Compact Portrait Phones
                        Column(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = cardPadding, vertical = 12.dp)
                        ) {
                            // Section 1: খরচের খাত
                            ExpenseCategorySelectorSection(
                                selectedCategory = selectedCategory,
                                isDropdownOpen = isCategoryDropdownOpen,
                                customCategory = customCategory,
                                onToggleDropdown = { isCategoryDropdownOpen = !isCategoryDropdownOpen },
                                onSelectCategory = {
                                    selectedCategory = it
                                    isCategoryDropdownOpen = false
                                },
                                onCustomCategoryChange = { customCategory = it }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Section 2: পরিশোধের মাধ্যম
                            val useGrid = screenWidth >= 340.dp
                            ExpensePaymentMethodSection(
                                selectedPaymentMethod = selectedPaymentMethod,
                                accountBalances = accountBalances,
                                isCompact = isCompact,
                                useGrid = useGrid,
                                onSelectMethod = { selectedPaymentMethod = it }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Section 3: টাকার পরিমাণ & ব্যালেন্স প্রিভিউ
                            ExpenseAmountSection(
                                amountText = amountText,
                                onAmountChange = { amountText = it },
                                isAmountValid = isAmountValid,
                                parsedAmount = parsedAmount,
                                isBalanceSufficient = isBalanceSufficient,
                                effectiveAvailable = effectiveAvailable,
                                warningMessage = warningMessage,
                                onOpenManageBalances = onOpenManageBalances
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Section 4: বিবরণ / নোট
                            CustomTextField(
                                value = noteText,
                                onValueChange = { noteText = it },
                                label = "বিবরণ / নোট (ঐচ্ছিক)",
                                placeholder = "যেমন: কর্মচারীর লাঞ্চ ও চা বিল",
                                testTag = "input_expense_note"
                            )
                        }
                    }

                    Divider(color = Slate800, thickness = 1.dp)

                    // Footer Action Buttons (Fixed Bottom)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate850)
                            .padding(horizontal = cardPadding, vertical = if (isShortHeight) 8.dp else 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("button_cancel_expense")
                        ) {
                            Text("বাতিল", fontSize = 14.sp, color = Slate300)
                        }

                        Button(
                            onClick = {
                                if (canSave && parsedAmount != null) {
                                    onConfirm(
                                        selectedCategory,
                                        customCategory,
                                        parsedAmount,
                                        selectedPaymentMethod,
                                        noteText
                                    )
                                }
                            },
                            enabled = canSave,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                disabledContainerColor = Slate800
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.4f)
                                .height(48.dp)
                                .testTag("button_save_expense")
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (canSave) Color.White else Slate500
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (editingExpense != null) "আপডেট করুন" else "খরচ সংরক্ষণ করুন",
                                fontSize = if (isCompact) 13.sp else 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (canSave) Color.White else Slate500
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseCategorySelectorSection(
    selectedCategory: String,
    isDropdownOpen: Boolean,
    customCategory: String,
    onToggleDropdown: () -> Unit,
    onSelectCategory: (String) -> Unit,
    onCustomCategoryChange: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "খরচের খাত *",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "ক্লিক করে পরিবর্তন করুন",
                fontSize = 11.sp,
                color = Slate400
            )
        }
        Spacer(modifier = Modifier.height(6.dp))

        // Selected Category Card / Dropdown Trigger
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Slate850),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (isDropdownOpen) EmeraldPrimary else Slate800, RoundedCornerShape(10.dp))
                .clickable { onToggleDropdown() }
                .testTag("dropdown_expense_category")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = getExpenseCategoryEmoji(selectedCategory),
                        fontSize = 18.sp
                    )
                    Text(
                        text = selectedCategory,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldPrimary
                    )
                }
                Icon(
                    imageVector = if (isDropdownOpen) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown",
                    tint = EmeraldPrimary
                )
            }
        }

        // Dropdown options list
        if (isDropdownOpen) {
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Slate850),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate700, RoundedCornerShape(10.dp))
            ) {
                Column(
                    modifier = Modifier
                        .heightIn(max = 200.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(4.dp)
                ) {
                    EXPENSE_CATEGORIES.forEach { cat ->
                        val isSelected = cat == selectedCategory
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectCategory(cat) }
                                .background(
                                    if (isSelected) EmeraldPrimary.copy(alpha = 0.15f) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = getExpenseCategoryEmoji(cat),
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = cat,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) EmeraldPrimary else TextPrimary
                                )
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelectCategory(cat) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = EmeraldPrimary,
                                    unselectedColor = Slate500
                                ),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick Category Chips (for instant 1-tap selection)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            QUICK_EXPENSE_CATEGORIES.forEach { cat ->
                val isSelected = cat == selectedCategory
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) EmeraldPrimary.copy(alpha = 0.2f) else Slate850,
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (isSelected) EmeraldPrimary else Slate800,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelectCategory(cat) }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = getExpenseCategoryEmoji(cat), fontSize = 11.sp)
                        Text(
                            text = cat,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) EmeraldPrimary else Slate300
                        )
                    }
                }
            }
        }

        // If "অন্যান্য" is selected, show input field
        if (selectedCategory == "অন্যান্য") {
            Spacer(modifier = Modifier.height(8.dp))
            CustomTextField(
                value = customCategory,
                onValueChange = onCustomCategoryChange,
                label = "খরচের খাত লিখুন *",
                placeholder = "যেমন: অফিস স্টেশনারি বা মেরামত",
                testTag = "input_custom_expense_category"
            )
        }
    }
}

@Composable
private fun ExpensePaymentMethodSection(
    selectedPaymentMethod: String,
    accountBalances: AccountBalances,
    isCompact: Boolean,
    useGrid: Boolean,
    onSelectMethod: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "পরিশোধের মাধ্যম (Payment Method) *",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "ব্যালেন্স থেকে কমবে",
                fontSize = 11.sp,
                color = Slate400
            )
        }
        Spacer(modifier = Modifier.height(6.dp))

        if (useGrid) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExpenseMethodOptionCard(
                    method = PAYMENT_METHOD_OPTIONS[0],
                    isSelected = selectedPaymentMethod == PAYMENT_METHOD_OPTIONS[0],
                    balance = accountBalances.cash,
                    isCompact = isCompact,
                    modifier = Modifier.weight(1f),
                    onSelect = { onSelectMethod(PAYMENT_METHOD_OPTIONS[0]) }
                )
                ExpenseMethodOptionCard(
                    method = PAYMENT_METHOD_OPTIONS[1],
                    isSelected = selectedPaymentMethod == PAYMENT_METHOD_OPTIONS[1],
                    balance = accountBalances.bkash,
                    isCompact = isCompact,
                    modifier = Modifier.weight(1f),
                    onSelect = { onSelectMethod(PAYMENT_METHOD_OPTIONS[1]) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExpenseMethodOptionCard(
                    method = PAYMENT_METHOD_OPTIONS[2],
                    isSelected = selectedPaymentMethod == PAYMENT_METHOD_OPTIONS[2],
                    balance = accountBalances.nagad,
                    isCompact = isCompact,
                    modifier = Modifier.weight(1f),
                    onSelect = { onSelectMethod(PAYMENT_METHOD_OPTIONS[2]) }
                )
                ExpenseMethodOptionCard(
                    method = PAYMENT_METHOD_OPTIONS[3],
                    isSelected = selectedPaymentMethod == PAYMENT_METHOD_OPTIONS[3],
                    balance = accountBalances.bank,
                    isCompact = isCompact,
                    modifier = Modifier.weight(1f),
                    onSelect = { onSelectMethod(PAYMENT_METHOD_OPTIONS[3]) }
                )
            }
        } else {
            PAYMENT_METHOD_OPTIONS.forEach { method ->
                val bal = when {
                    method.startsWith("নগদ") -> accountBalances.cash
                    method.equals("bKash", ignoreCase = true) -> accountBalances.bkash
                    method.equals("Nagad", ignoreCase = true) -> accountBalances.nagad
                    method.equals("ব্যাংক", ignoreCase = true) -> accountBalances.bank
                    else -> accountBalances.cash
                }
                ExpenseMethodOptionCard(
                    method = method,
                    isSelected = selectedPaymentMethod == method,
                    balance = bal,
                    isCompact = isCompact,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.5.dp),
                    onSelect = { onSelectMethod(method) }
                )
            }
        }
    }
}

@Composable
private fun ExpenseAmountSection(
    amountText: String,
    onAmountChange: (String) -> Unit,
    isAmountValid: Boolean,
    parsedAmount: Double?,
    isBalanceSufficient: Boolean,
    effectiveAvailable: Double,
    warningMessage: String,
    onOpenManageBalances: () -> Unit
) {
    Column {
        CustomTextField(
            value = amountText,
            onValueChange = onAmountChange,
            label = "টাকার পরিমাণ (৳) *",
            placeholder = "যেমন: ১০০০ বা 1000",
            keyboardType = KeyboardType.Number,
            testTag = "input_expense_amount"
        )

        // Quick Amount Addition Chips
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(50, 100, 200, 500, 1000, 2000, 5000).forEach { amt ->
                Box(
                    modifier = Modifier
                        .background(Slate850, RoundedCornerShape(8.dp))
                        .border(1.dp, Slate700, RoundedCornerShape(8.dp))
                        .clickable {
                            val currentAmt = parseLocalizedAmount(amountText) ?: 0.0
                            val newAmt = if (amountText.isBlank() || currentAmt == 0.0) amt.toDouble() else currentAmt + amt
                            onAmountChange("%.0f".format(newAmt))
                        }
                        .padding(horizontal = 9.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "+৳$amt",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldPrimary
                    )
                }
            }
        }

        // Real-time Balance Feedback
        Spacer(modifier = Modifier.height(8.dp))
        if (!isBalanceSufficient) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CoralPink.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    .border(1.dp, CoralPink.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = "Warning",
                        tint = CoralPink,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = warningMessage,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoralPink
                        )
                        Text(
                            text = "বর্তমান ব্যালেন্স: ৳ ${"%,.0f".format(effectiveAvailable)}",
                            fontSize = 11.sp,
                            color = Slate300
                        )
                    }
                }
                Text(
                    text = "+ ব্যালেন্স সেট",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary,
                    modifier = Modifier
                        .clickable { onOpenManageBalances() }
                        .padding(4.dp)
                )
            }
        } else if (isAmountValid && parsedAmount != null) {
            val remaining = effectiveAvailable - parsedAmount
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EmeraldPrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "OK",
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "খরচ শেষে অবশিষ্ট ব্যালেন্স থাকবে: ৳ ${"%,.0f".format(remaining)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = EmeraldPrimary
                )
            }
        }
    }
}

private fun getExpenseCategoryEmoji(cat: String): String = when (cat) {
    "দোকান ভাড়া" -> "🏢"
    "বিদ্যুৎ বিল" -> "⚡"
    "কর্মচারীর বেতন" -> "👥"
    "ইন্টারনেট" -> "🌐"
    "চা-নাস্তা ও আপ্যায়ন" -> "☕"
    "মেরামত ও রক্ষণাবেক্ষণ" -> "🔧"
    "পরিবহন / ডেলিভারি" -> "🚚"
    "প্যাকেজিং" -> "📦"
    "বিজ্ঞাপন / মার্কেটিং" -> "📢"
    else -> "📝"
}

@Composable
private fun ExpenseMethodOptionCard(
    method: String,
    isSelected: Boolean,
    balance: Double,
    isCompact: Boolean,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit
) {
    val methodIcon = when {
        method.startsWith("নগদ") -> Icons.Default.Payments
        method.equals("bKash", ignoreCase = true) -> Icons.Default.PhoneAndroid
        method.equals("Nagad", ignoreCase = true) -> Icons.Default.PhoneAndroid
        method.equals("ব্যাংক", ignoreCase = true) -> Icons.Default.AccountBalance
        else -> Icons.Default.AttachMoney
    }
    val methodColor = when {
        method.startsWith("নগদ") -> EmeraldPrimary
        method.equals("bKash", ignoreCase = true) -> Color(0xFFE2136E)
        method.equals("Nagad", ignoreCase = true) -> Color(0xFFF7941D)
        method.equals("ব্যাংক", ignoreCase = true) -> Color(0xFF3B82F6)
        else -> EmeraldPrimary
    }
    val shortTitle = when {
        method.startsWith("নগদ") -> "নগদ ক্যাশ"
        else -> method
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) methodColor.copy(alpha = 0.14f) else Slate850
        ),
        modifier = modifier
            .heightIn(min = 48.dp)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) methodColor else Slate800,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onSelect() }
            .testTag("method_option_${method.take(5)}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isCompact) 8.dp else 10.dp, vertical = if (isCompact) 8.dp else 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isCompact) 26.dp else 28.dp)
                        .background(methodColor.copy(alpha = 0.18f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = methodIcon,
                        contentDescription = method,
                        tint = methodColor,
                        modifier = Modifier.size(if (isCompact) 14.dp else 16.dp)
                    )
                }
                Text(
                    text = shortTitle,
                    fontSize = if (isCompact) 11.sp else 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) TextPrimary else Slate300
                )
            }

            // Balance Badge
            Box(
                modifier = Modifier
                    .background(Slate900, RoundedCornerShape(6.dp))
                    .border(1.dp, if (isSelected) methodColor.copy(alpha = 0.4f) else Slate800, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.5.dp)
            ) {
                Text(
                    text = "৳ ${"%,.0f".format(balance)}",
                    fontSize = if (isCompact) 10.sp else 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (balance > 0) methodColor else CoralPink
                )
            }
        }
    }
}

@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, amount: Double) -> Unit
) {
    ExpenseEntryDialog(
        editingExpense = null,
        accountBalances = AccountBalances(cash = 999999.0, bkash = 999999.0, nagad = 999999.0, bank = 999999.0),
        onDismiss = onDismiss,
        onConfirm = { cat, custom, amt, _, _ ->
            val title = if (cat == "অন্যান্য") custom else cat
            onConfirm(title, cat, amt)
        }
    )
}

@Composable
fun AccountBalancesDialog(
    accountBalances: AccountBalances,
    onDismiss: () -> Unit,
    onConfirm: (cash: Double, bkash: Double, nagad: Double, bank: Double) -> Unit
) {
    var cashText by remember {
        mutableStateOf(if (accountBalances.cash > 0) "%,.0f".format(accountBalances.cash).replace(",", "") else "")
    }
    var bkashText by remember {
        mutableStateOf(if (accountBalances.bkash > 0) "%,.0f".format(accountBalances.bkash).replace(",", "") else "")
    }
    var nagadText by remember {
        mutableStateOf(if (accountBalances.nagad > 0) "%,.0f".format(accountBalances.nagad).replace(",", "") else "")
    }
    var bankText by remember {
        mutableStateOf(if (accountBalances.bank > 0) "%,.0f".format(accountBalances.bank).replace(",", "") else "")
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Slate800, RoundedCornerShape(16.dp))
                .testTag("dialog_account_balances")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "একাউন্ট ও প্রারম্ভিক ব্যালেন্স",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "প্রতিটি মাধ্যমে আপনার বর্তমান ব্যালেন্স নির্ধারণ করুন",
                            fontSize = 12.sp,
                            color = Slate400,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                CustomTextField(
                    value = cashText,
                    onValueChange = { cashText = it },
                    label = "ক্যাশ ব্যালেন্স (Cash ৳)",
                    placeholder = "যেমন: 10000",
                    keyboardType = KeyboardType.Number,
                    testTag = "input_balance_cash"
                )

                CustomTextField(
                    value = bkashText,
                    onValueChange = { bkashText = it },
                    label = "বিকাশ ব্যালেন্স (bKash ৳)",
                    placeholder = "যেমন: 5000",
                    keyboardType = KeyboardType.Number,
                    testTag = "input_balance_bkash"
                )

                CustomTextField(
                    value = nagadText,
                    onValueChange = { nagadText = it },
                    label = "নগদ ব্যালেন্স (Nagad ৳)",
                    placeholder = "যেমন: 3000",
                    keyboardType = KeyboardType.Number,
                    testTag = "input_balance_nagad"
                )

                CustomTextField(
                    value = bankText,
                    onValueChange = { bankText = it },
                    label = "ব্যাংক ব্যালেন্স (Bank ৳)",
                    placeholder = "যেমন: 50000",
                    keyboardType = KeyboardType.Number,
                    testTag = "input_balance_bank"
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val c = parseLocalizedAmount(cashText) ?: 0.0
                        val bk = parseLocalizedAmount(bkashText) ?: 0.0
                        val ng = parseLocalizedAmount(nagadText) ?: 0.0
                        val bn = parseLocalizedAmount(bankText) ?: 0.0
                        onConfirm(c, bk, ng, bn)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("button_save_account_balances")
                ) {
                    Text("ব্যালেন্স সংরক্ষণ করুন", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun CollectDueDialog(
    customer: Customer?,
    allCustomers: List<Customer>,
    onDismiss: () -> Unit,
    onConfirm: (customer: Customer, amount: Double, note: String) -> Unit
) {
    var selectedCustomer by remember { mutableStateOf(customer ?: allCustomers.firstOrNull { it.currentDue > 0 }) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("বাকি আদায় (ক্যাশ)") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            modifier = Modifier.fillMaxWidth().testTag("dialog_collect_due")
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "বাকি আদায় (Collect Due)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedCustomer != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate850),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = selectedCustomer!!.name,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "মোবাইল: ${selectedCustomer!!.phone}",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "মোট বাকি: ৳ ${"%,.0f".format(selectedCustomer!!.currentDue)}",
                                fontWeight = FontWeight.Bold,
                                color = CoralPink,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                CustomTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = "জমা টাকার পরিমাণ (৳)",
                    placeholder = selectedCustomer?.currentDue?.toInt()?.toString() ?: "1000",
                    keyboardType = KeyboardType.Number,
                    testTag = "input_collect_amount"
                )

                CustomTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = "নোট বা মাধ্যম",
                    placeholder = "ক্যাশ / বিকাশ / ব্যাংক",
                    testTag = "input_collect_note"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val amt = amount.toDoubleOrNull() ?: 0.0
                        if (selectedCustomer != null && amt > 0.0) {
                            onConfirm(selectedCustomer!!, amt, note)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("button_confirm_collect_due")
                ) {
                    Text("বাকি আদায় জমা করুন ✓", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun OpeningCashDialog(
    currentCash: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    AccountBalancesDialog(
        accountBalances = AccountBalances(cash = currentCash),
        onDismiss = onDismiss,
        onConfirm = { c, _, _, _ -> onConfirm(c) }
    )
}

data class StockInVariantInfo(
    val sizeName: String,
    val stock: Int
)

private fun parseProductStockVariants(product: Product): List<StockInVariantInfo> {
    val raw = product.sizesOrVariants.trim()
    if (raw.isBlank() || raw == "স্ট্যান্ডার্ড") {
        return listOf(StockInVariantInfo(sizeName = "স্ট্যান্ডার্ড", stock = product.stockQuantity))
    }
    val delimiter = if (raw.contains("|")) "|" else ","
    val parts = raw.split(delimiter).map { it.trim() }.filter { it.isNotBlank() }
    val result = parts.map { part ->
        if (part.contains(":")) {
            val sub = part.split(":")
            val sName = sub[0].trim()
            val sStock = sub.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
            StockInVariantInfo(sizeName = sName, stock = sStock)
        } else {
            StockInVariantInfo(sizeName = part, stock = product.stockQuantity)
        }
    }
    return if (result.isNotEmpty()) result else listOf(StockInVariantInfo(sizeName = "স্ট্যান্ডার্ড", stock = product.stockQuantity))
}

@Composable
private fun StockInInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    isReadOnly: Boolean = false,
    textColor: Color = Color(0xFF0F172A),
    textWeight: FontWeight = FontWeight.Normal,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = isReadOnly,
        singleLine = true,
        textStyle = TextStyle(
            color = textColor,
            fontSize = 13.sp,
            fontWeight = textWeight
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier
            .height(42.dp)
            .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
            .testTag(testTag),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun StockInDropdownTrigger(
    displayText: String,
    isOpen: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Box(
        modifier = modifier
            .height(42.dp)
            .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
            .border(
                width = if (isOpen) 1.5.dp else 1.dp,
                color = if (isOpen) Color(0xFF00B87C) else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp)
            .testTag(testTag),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayText,
                color = Color(0xFF0F172A),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Dropdown",
                tint = Color(0xFF00B87C),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun PurchaseStockDialog(
    products: List<Product>,
    onDismiss: () -> Unit,
    onConfirm: (Product, Int) -> Unit
) {
    PurchaseStockDialog(
        products = products,
        onDismiss = onDismiss,
        onConfirm = { product, qty, _, _, _, _, _ -> onConfirm(product, qty) }
    )
}

@Composable
fun PurchaseStockDialog(
    products: List<Product>,
    onDismiss: () -> Unit,
    onConfirm: (
        product: Product,
        quantity: Int,
        selectedSize: String,
        purchasePrice: Double,
        supplier: String,
        paymentType: String,
        note: String
    ) -> Unit
) {
    var selectedProduct by remember { mutableStateOf(products.firstOrNull()) }
    var productDropdownExpanded by remember { mutableStateOf(false) }

    val variants = remember(selectedProduct) {
        selectedProduct?.let { parseProductStockVariants(it) } ?: emptyList()
    }
    var selectedVariant by remember(selectedProduct) {
        mutableStateOf(variants.firstOrNull())
    }
    var variantDropdownExpanded by remember { mutableStateOf(false) }

    var quantity by remember { mutableStateOf("10") }
    var buyPrice by remember(selectedProduct) {
        mutableStateOf(
            if ((selectedProduct?.purchasePrice ?: 0.0) > 0.0) {
                val p = selectedProduct!!.purchasePrice
                if (p % 1.0 == 0.0) p.toInt().toString() else p.toString()
            } else "450"
        )
    }

    val calculatedTotal = remember(quantity, buyPrice) {
        val q = quantity.toIntOrNull() ?: 0
        val p = buyPrice.toDoubleOrNull() ?: 0.0
        val total = (q * p).toLong()
        if (total > 0) total.toString() else "0"
    }

    var supplier by remember { mutableStateOf("") }
    val paymentOptions = remember {
        listOf(
            "নগদ পরিশোধ (Cash Out from Register)",
            "বাকি (Supplier Due)",
            "আংশিক নগদ / আংশিক বাকি"
        )
    }
    var selectedPaymentType by remember { mutableStateOf(paymentOptions[0]) }
    var paymentDropdownExpanded by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 620.dp)
                .padding(horizontal = 12.dp, vertical = 16.dp)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                .testTag("dialog_purchase_stock")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "নতুন মাল ক্রয় / স্টক ইন",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val isMobileNarrow = maxWidth < 460.dp

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // ROW 1: Product Selector & Size/Variant Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Product Selector
                            Column(modifier = Modifier.weight(1.15f)) {
                                Text(
                                    text = "পণ্য নির্বাচন করুন*:",
                                    fontSize = if (isMobileNarrow) 11.sp else 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF334155),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box {
                                    val prodTitle = if (selectedProduct != null) {
                                        "${selectedProduct!!.name}${if (selectedProduct!!.category.isNotBlank()) " (${selectedProduct!!.category})" else ""}"
                                    } else {
                                        "-- পণ্য নির্বাচন করুন --"
                                    }
                                    StockInDropdownTrigger(
                                        displayText = prodTitle,
                                        isOpen = productDropdownExpanded,
                                        onClick = { productDropdownExpanded = true },
                                        testTag = "stock_in_product_selector"
                                    )
                                    DropdownMenu(
                                        expanded = productDropdownExpanded,
                                        onDismissRequest = { productDropdownExpanded = false },
                                        modifier = Modifier
                                            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                            .widthIn(min = 240.dp, max = 320.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { productDropdownExpanded = false }
                                                .padding(horizontal = 14.dp, vertical = 9.dp)
                                        ) {
                                            Text(
                                                text = "-- পণ্য নির্বাচন করুন --",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 13.sp
                                            )
                                        }
                                        products.forEach { prod ->
                                            val isSelected = selectedProduct?.id == prod.id
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(if (isSelected) Color(0xFF2563EB) else Color.Transparent)
                                                    .clickable {
                                                        selectedProduct = prod
                                                        productDropdownExpanded = false
                                                    }
                                                    .padding(horizontal = 14.dp, vertical = 9.dp)
                                            ) {
                                                Text(
                                                    text = "${prod.name}${if (prod.category.isNotBlank()) " (${prod.category})" else ""}",
                                                    color = if (isSelected) Color.White else Color(0xFFF1F5F9),
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Size / Variant Selector
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "সাইজ / ভ্যারিয়েন্ট*:",
                                    fontSize = if (isMobileNarrow) 11.sp else 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF334155),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box {
                                    val variantTitle = if (selectedVariant != null) {
                                        "${selectedVariant!!.sizeName} (বর্তমান স্টক: ${selectedVariant!!.stock} pcs)"
                                    } else {
                                        "সাইজ নির্বাচন করুন"
                                    }
                                    StockInDropdownTrigger(
                                        displayText = variantTitle,
                                        isOpen = variantDropdownExpanded,
                                        onClick = { variantDropdownExpanded = true },
                                        testTag = "stock_in_variant_selector"
                                    )
                                    DropdownMenu(
                                        expanded = variantDropdownExpanded,
                                        onDismissRequest = { variantDropdownExpanded = false },
                                        modifier = Modifier
                                            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                            .widthIn(min = 220.dp, max = 290.dp)
                                    ) {
                                        variants.forEach { v ->
                                            val isSelected = selectedVariant?.sizeName == v.sizeName
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(if (isSelected) Color(0xFF2563EB) else Color.Transparent)
                                                    .clickable {
                                                        selectedVariant = v
                                                        variantDropdownExpanded = false
                                                    }
                                                    .padding(horizontal = 14.dp, vertical = 9.dp)
                                            ) {
                                                Text(
                                                    text = "${v.sizeName} (বর্তমান স্টক: ${v.stock} pcs)",
                                                    color = if (isSelected) Color.White else Color(0xFFF1F5F9),
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ROW 2: Quantity, Per-piece purchase price, Total purchase price
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(if (isMobileNarrow) 8.dp else 12.dp)
                        ) {
                            // Column 1: Quantity
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ক্রয় সংখ্যা (Quantity pcs)*:",
                                    fontSize = if (isMobileNarrow) 10.5.sp else 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF334155),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                StockInInputField(
                                    value = quantity,
                                    onValueChange = { quantity = it },
                                    placeholder = "10",
                                    keyboardType = KeyboardType.Number,
                                    testTag = "input_stock_in_quantity"
                                )
                            }

                            // Column 2: Per-piece Price
                            Column(modifier = Modifier.weight(1.15f)) {
                                Text(
                                    text = "প্রতি পিসের ক্রয়মূল্য (৳)*:",
                                    fontSize = if (isMobileNarrow) 10.5.sp else 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF334155),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                StockInInputField(
                                    value = buyPrice,
                                    onValueChange = { buyPrice = it },
                                    placeholder = "450",
                                    keyboardType = KeyboardType.Number,
                                    testTag = "input_stock_in_buy_price"
                                )
                            }

                            // Column 3: Total Cost (Calculated)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "মোট ক্রয়মূল্য (Total ৳):",
                                    fontSize = if (isMobileNarrow) 10.5.sp else 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF334155),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp)
                                        .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = calculatedTotal,
                                        color = Color(0xFF00B87C),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // ROW 3: Supplier & Payment Type
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(if (isMobileNarrow) 10.dp else 12.dp)
                        ) {
                            // Supplier
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "সাপ্লায়ার / মহাজন:",
                                    fontSize = if (isMobileNarrow) 11.sp else 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF334155),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                StockInInputField(
                                    value = supplier,
                                    onValueChange = { supplier = it },
                                    placeholder = "যেমন: ঢাকা হোলসেল মার্কেট",
                                    testTag = "input_stock_in_supplier"
                                )
                            }

                            // Payment Type
                            Column(modifier = Modifier.weight(1.15f)) {
                                Text(
                                    text = "পরিশোধের ধরন:",
                                    fontSize = if (isMobileNarrow) 11.sp else 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF334155),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box {
                                    StockInDropdownTrigger(
                                        displayText = selectedPaymentType,
                                        isOpen = paymentDropdownExpanded,
                                        onClick = { paymentDropdownExpanded = true },
                                        testTag = "stock_in_payment_type_selector"
                                    )
                                    DropdownMenu(
                                        expanded = paymentDropdownExpanded,
                                        onDismissRequest = { paymentDropdownExpanded = false },
                                        modifier = Modifier
                                            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                            .widthIn(min = 240.dp, max = 320.dp)
                                    ) {
                                        paymentOptions.forEach { opt ->
                                            val isSelected = selectedPaymentType == opt
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(if (isSelected) Color(0xFF2563EB) else Color.Transparent)
                                                    .clickable {
                                                        selectedPaymentType = opt
                                                        paymentDropdownExpanded = false
                                                    }
                                                    .padding(horizontal = 14.dp, vertical = 9.dp)
                                            ) {
                                                Text(
                                                    text = opt,
                                                    color = if (isSelected) Color.White else Color(0xFFF1F5F9),
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ROW 4: Description / Note
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "বিবরণ / নোট",
                                fontSize = if (isMobileNarrow) 11.sp else 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            StockInInputField(
                                value = note,
                                onValueChange = { note = it },
                                placeholder = "চালান বা মেমো নম্বর",
                                testTag = "input_stock_in_note"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ROW 5: Action Buttons (Cancel and Save)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF334155)
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("button_cancel_stock_in")
                    ) {
                        Text(
                            text = "বাতিল",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            val qty = quantity.toIntOrNull() ?: 0
                            val price = buyPrice.toDoubleOrNull() ?: 0.0
                            if (selectedProduct != null && qty > 0) {
                                onConfirm(
                                    selectedProduct!!,
                                    qty,
                                    selectedVariant?.sizeName ?: "",
                                    price,
                                    supplier.trim(),
                                    selectedPaymentType,
                                    note.trim()
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B87C)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("button_confirm_stock_in")
                    ) {
                        Text(
                            text = "স্টক ইন ও সেভ করুন",
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

@Composable
fun ShopInfoDialog(
    profile: ShopProfile,
    onDismiss: () -> Unit,
    onConfirm: (shopName: String, ownerName: String, email: String, phone: String, address: String) -> Unit
) {
    var shopName by remember { mutableStateOf(profile.shopName) }
    var ownerName by remember { mutableStateOf(profile.ownerName) }
    var email by remember { mutableStateOf(profile.email) }
    var phone by remember { mutableStateOf(profile.phone) }
    var address by remember { mutableStateOf(profile.address) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            modifier = Modifier.fillMaxWidth().testTag("dialog_shop_info")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "দোকানের তথ্য পরিবর্তন",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                CustomTextField(value = shopName, onValueChange = { shopName = it }, label = "দোকানের নাম", testTag = "input_shop_name")
                CustomTextField(value = ownerName, onValueChange = { ownerName = it }, label = "মালিকের নাম", testTag = "input_owner_name")
                CustomTextField(value = email, onValueChange = { email = it }, label = "ইমেইল", testTag = "input_shop_email")
                CustomTextField(value = phone, onValueChange = { phone = it }, label = "ফোন", testTag = "input_shop_phone")
                CustomTextField(value = address, onValueChange = { address = it }, label = "ঠিকানা", testTag = "input_shop_address")

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onConfirm(shopName, ownerName, email, phone, address)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("button_save_shop_info")
                ) {
                    Text("সংরক্ষণ করুন", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun CompletedSaleInvoiceDialog(
    summary: CompletedSaleSummary,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            modifier = Modifier.fillMaxWidth().testTag("dialog_invoice_success")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(EmeraldPrimary.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "বিক্রি সফলভাবে সম্পন্ন হয়েছে!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextPrimary
                )

                Text(
                    text = "ইনভয়েস: ${summary.invoiceNo}",
                    fontSize = 13.sp,
                    color = Slate400,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate850),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        InvoiceRow("ক্রেতার নাম", summary.customerName)
                        InvoiceRow("মোট আইটেম", "${summary.itemCount} টি")
                        InvoiceRow("উপ-মোট", "৳ ${"%,.0f".format(summary.subtotal)}")
                        if (summary.discount > 0) {
                            InvoiceRow("ডিসকাউন্ট", "- ৳ ${"%,.0f".format(summary.discount)}", CoralPink)
                        }
                        Divider(color = Slate700, modifier = Modifier.padding(vertical = 8.dp))
                        InvoiceRow("সর্বমোট", "৳ ${"%,.0f".format(summary.netTotal)}", TextPrimary, true)
                        InvoiceRow("পরিশোধিত", "৳ ${"%,.0f".format(summary.paidAmount)}", EmeraldPrimary)
                        if (summary.dueAmount > 0) {
                            InvoiceRow("বাকি", "৳ ${"%,.0f".format(summary.dueAmount)}", AmberOrange, true)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("প্রিন্ট / শেয়ার", color = Slate200)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(46.dp).testTag("button_invoice_done")
                    ) {
                        Text("ঠিক আছে", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceRow(
    title: String,
    value: String,
    valueColor: Color = TextPrimary,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontSize = 13.sp, color = Slate400)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = valueColor
        )
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Column(modifier = modifier.padding(vertical = 5.dp)) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Slate400,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Slate600, fontSize = 13.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
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
                .testTag(testTag)
        )
    }
}
