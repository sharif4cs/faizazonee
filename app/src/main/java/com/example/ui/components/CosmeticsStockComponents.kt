package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ExpiryStatus
import com.example.data.model.Product
import com.example.data.model.ProductStockStatus
import com.example.data.model.StockTransaction
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

val COSMETICS_CATEGORIES = listOf(
    "মেকআপ (Makeup)",
    "স্কিন কেয়ার (Skincare)",
    "হেয়ার কেয়ার (Hair Care)",
    "বডি কেয়ার (Body Care)",
    "সুগন্ধি / পারফিউম (Fragrance)",
    "নেইল কেয়ার (Nail Care)",
    "এক্সেসরিজ (Accessories)",
    "জুয়েলারি (Jewelry)",
    "পোশাক / ক্লথিং (Clothing)",
    "অন্যান্য (Other)"
)

val PRODUCT_UNITS = listOf(
    "পিস (Piece)",
    "বক্স (Box)",
    "বোতল (Bottle)",
    "টিউব (Tube)",
    "জার (Jar)",
    "ড্রপার (Dropper)",
    "সেট (Set)",
    "প্যাক (Pack)",
    "কৌটা (Container)"
)

/**
 * Modern, comprehensive Cosmetics Product Add/Edit Dialog.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditCosmeticsProductDialog(
    editingProduct: Product? = null,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        category: String,
        sku: String,
        buyPrice: Double,
        sellPrice: Double,
        stock: Int,
        variants: String,
        brand: String,
        lowStockLimit: Int,
        note: String,
        barcode: String,
        unit: String,
        wholesalePrice: Double,
        discount: Double,
        taxRate: Double,
        openingStock: Int,
        supplierName: String,
        supplierPhone: String,
        batchNumber: String,
        manufacturingDate: String,
        expiryDate: String
    ) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: বেসিক, 1: মূল্য ও স্টক, 2: ব্যাচ ও মেয়াদ, 3: সাপ্লায়ার
    val tabTitles = listOf("সাধারণ তথ্য", "মূল্য ও স্টক", "ব্যাচ ও মেয়াদ", "সাপ্লায়ার")

    // Form fields
    var name by remember { mutableStateOf(editingProduct?.name ?: "") }
    var category by remember { mutableStateOf(editingProduct?.category ?: "মেকআপ (Makeup)") }
    var brand by remember { mutableStateOf(editingProduct?.brand ?: "") }
    var sku by remember { mutableStateOf(editingProduct?.sku ?: "") }
    var barcode by remember { mutableStateOf(editingProduct?.barcode ?: "") }
    var unit by remember { mutableStateOf(editingProduct?.unit ?: "পিস") }
    var variant by remember { mutableStateOf(editingProduct?.sizesOrVariants ?: "Standard") }

    var buyPriceStr by remember {
        mutableStateOf(if (editingProduct != null && editingProduct.purchasePrice > 0) "%.0f".format(editingProduct.purchasePrice) else "")
    }
    var sellPriceStr by remember {
        mutableStateOf(if (editingProduct != null && editingProduct.sellingPrice > 0) "%.0f".format(editingProduct.sellingPrice) else "")
    }
    var wholesalePriceStr by remember {
        mutableStateOf(if (editingProduct != null && editingProduct.wholesalePrice > 0) "%.0f".format(editingProduct.wholesalePrice) else "")
    }
    var stockStr by remember {
        mutableStateOf(if (editingProduct != null) editingProduct.stockQuantity.toString() else "")
    }
    var lowStockLimitStr by remember {
        mutableStateOf((editingProduct?.lowStockThreshold ?: 5).toString())
    }
    var discountStr by remember {
        mutableStateOf(if (editingProduct != null && editingProduct.discount > 0) "%.0f".format(editingProduct.discount) else "")
    }
    var taxRateStr by remember {
        mutableStateOf(if (editingProduct != null && editingProduct.taxRate > 0) "%.0f".format(editingProduct.taxRate) else "")
    }

    var batchNumber by remember { mutableStateOf(editingProduct?.batchNumber ?: "") }
    var mfgDate by remember { mutableStateOf(editingProduct?.manufacturingDate ?: "") }
    var expiryDate by remember { mutableStateOf(editingProduct?.expiryDate ?: "") }

    var supplierName by remember { mutableStateOf(editingProduct?.supplierName ?: "") }
    var supplierPhone by remember { mutableStateOf(editingProduct?.supplierPhone ?: "") }
    var note by remember { mutableStateOf(editingProduct?.note ?: "") }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var unitDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Real-time calculations
    val buyVal = buyPriceStr.toDoubleOrNull() ?: 0.0
    val sellVal = sellPriceStr.toDoubleOrNull() ?: 0.0
    val profitVal = (sellVal - buyVal).coerceAtLeast(0.0)
    val marginPercent = if (buyVal > 0) ((profitVal / buyVal) * 100) else 0.0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = BorderStroke(1.dp, Slate800),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .widthIn(max = 640.dp)
                .fillMaxHeight(0.92f)
                .padding(vertical = 12.dp)
                .testTag("dialog_add_edit_cosmetics_product")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Title Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EmeraldPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (editingProduct == null) Icons.Default.Add else Icons.Default.Edit,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (editingProduct == null) "নতুন কসমেটিক্স পণ্য যোগ" else "পণ্য সম্পাদনা করুন",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "প্রসাধনী পণ্য, ব্যাচ ও এক্সপায়ারির বিস্তারিত হিসাব",
                                fontSize = 11.sp,
                                color = Slate400
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sub-tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate850)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) EmeraldPrimary else Color.Transparent)
                                .clickable { selectedTab = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Slate950 else Slate300,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        when (selectedTab) {
                            0 -> {
                                // Basic Info
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = name,
                                        onValueChange = { name = it; errorMessage = null },
                                        label = { Text("পণ্যের নাম (Product Name) *") },
                                        placeholder = { Text("যেমন: মেবেলিন ফিট মি ফাউন্ডেশন") },
                                        modifier = Modifier.fillMaxWidth().testTag("input_product_name"),
                                        singleLine = true,
                                        colors = outlinedFieldColors()
                                    )

                                    // Category Dropdown
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = category,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("ক্যাটাগরি (Category)") },
                                            trailingIcon = {
                                                IconButton(onClick = { categoryDropdownExpanded = true }) {
                                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Slate400)
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth().clickable { categoryDropdownExpanded = true },
                                            colors = outlinedFieldColors()
                                        )
                                        DropdownMenu(
                                            expanded = categoryDropdownExpanded,
                                            onDismissRequest = { categoryDropdownExpanded = false },
                                            modifier = Modifier.background(Slate850)
                                        ) {
                                            COSMETICS_CATEGORIES.forEach { cat ->
                                                DropdownMenuItem(
                                                    text = { Text(cat, color = TextPrimary) },
                                                    onClick = {
                                                        category = cat.substringBefore(" (")
                                                        categoryDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedTextField(
                                            value = brand,
                                            onValueChange = { brand = it },
                                            label = { Text("ব্র্যান্ড (Brand)") },
                                            placeholder = { Text("Maybelline, MAC, CeraVe") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            colors = outlinedFieldColors()
                                        )

                                        // Unit Dropdown
                                        Box(modifier = Modifier.weight(1f)) {
                                            OutlinedTextField(
                                                value = unit,
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("একক (Unit)") },
                                                trailingIcon = {
                                                    IconButton(onClick = { unitDropdownExpanded = true }) {
                                                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Slate400)
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth().clickable { unitDropdownExpanded = true },
                                                colors = outlinedFieldColors()
                                            )
                                            DropdownMenu(
                                                expanded = unitDropdownExpanded,
                                                onDismissRequest = { unitDropdownExpanded = false },
                                                modifier = Modifier.background(Slate850)
                                            ) {
                                                PRODUCT_UNITS.forEach { u ->
                                                    DropdownMenuItem(
                                                        text = { Text(u, color = TextPrimary) },
                                                        onClick = {
                                                            unit = u.substringBefore(" (")
                                                            unitDropdownExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedTextField(
                                            value = sku,
                                            onValueChange = { sku = it },
                                            label = { Text("SKU কোড (ঐচ্ছিক)") },
                                            placeholder = { Text("MB-128") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            colors = outlinedFieldColors()
                                        )

                                        OutlinedTextField(
                                            value = variant,
                                            onValueChange = { variant = it },
                                            label = { Text("শেড / ভলিউম / সাইজ") },
                                            placeholder = { Text("Shade 128 / 50ml") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            colors = outlinedFieldColors()
                                        )
                                    }

                                    // Barcode Field with Generate Action
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = barcode,
                                            onValueChange = { barcode = it },
                                            label = { Text("বারকোড (Barcode EAN/UPC)") },
                                            placeholder = { Text("8901234567890") },
                                            modifier = Modifier.weight(1f).testTag("input_product_barcode"),
                                            singleLine = true,
                                            colors = outlinedFieldColors()
                                        )
                                        Button(
                                            onClick = {
                                                val prefix = "890"
                                                val time = System.currentTimeMillis().toString().takeLast(9)
                                                val base = prefix + time
                                                var sum = 0
                                                for (i in 0 until 12) {
                                                    val digit = base[i] - '0'
                                                    sum += if (i % 2 == 0) digit else digit * 3
                                                }
                                                val checkDigit = (10 - (sum % 10)) % 10
                                                barcode = "$base$checkDigit"
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.height(54.dp).padding(top = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Autorenew, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("জেনারেট", fontSize = 11.5.sp, color = EmeraldPrimary)
                                        }
                                    }
                                }
                            }

                            1 -> {
                                // Pricing & Stock
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedTextField(
                                            value = buyPriceStr,
                                            onValueChange = { buyPriceStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                            label = { Text("ক্রয়মূল্য (Buy Price ৳) *") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f).testTag("input_buy_price"),
                                            singleLine = true,
                                            colors = outlinedFieldColors()
                                        )
                                        OutlinedTextField(
                                            value = sellPriceStr,
                                            onValueChange = { sellPriceStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                            label = { Text("বিক্রয়মূল্য (Sell Price ৳) *") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f).testTag("input_sell_price"),
                                            singleLine = true,
                                            colors = outlinedFieldColors()
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedTextField(
                                            value = wholesalePriceStr,
                                            onValueChange = { wholesalePriceStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                            label = { Text("পাইকারি মূল্য (Wholesale ৳)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            colors = outlinedFieldColors()
                                        )
                                        OutlinedTextField(
                                            value = discountStr,
                                            onValueChange = { discountStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                            label = { Text("ডিফল্ট ডিসকাউন্ট (৳)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            colors = outlinedFieldColors()
                                        )
                                    }

                                    // Margin / Profit Card Preview
                                    Card(
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = Slate850),
                                        border = BorderStroke(1.dp, Slate800),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceAround,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("প্রতি ইউনিটে লাভ", fontSize = 11.sp, color = Slate400)
                                                Text(
                                                    "৳${"%.1f".format(profitVal)}",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (profitVal > 0) EmeraldPrimary else AmberOrange
                                                )
                                            }
                                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Slate700))
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("প্রফিট মার্জিন", fontSize = 11.sp, color = Slate400)
                                                Text(
                                                    "${"%.1f".format(marginPercent)}%",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (marginPercent >= 20) EmeraldPrimary else Slate300
                                                )
                                            }
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedTextField(
                                            value = stockStr,
                                            onValueChange = { stockStr = it.filter { ch -> ch.isDigit() } },
                                            label = { Text("প্রারম্ভিক স্টক (Opening Stock) *") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f).testTag("input_opening_stock"),
                                            singleLine = true,
                                            colors = outlinedFieldColors()
                                        )
                                        OutlinedTextField(
                                            value = lowStockLimitStr,
                                            onValueChange = { lowStockLimitStr = it.filter { ch -> ch.isDigit() } },
                                            label = { Text("কম স্টক সতর্কতা লিমিট") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            colors = outlinedFieldColors()
                                        )
                                    }
                                }
                            }

                            2 -> {
                                // Batch & Expiry
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = batchNumber,
                                        onValueChange = { batchNumber = it },
                                        label = { Text("ব্যাচ নম্বর (Batch Number)") },
                                        placeholder = { Text("যেমন: BATCH-2025A / LOT-991") },
                                        modifier = Modifier.fillMaxWidth().testTag("input_batch_number"),
                                        singleLine = true,
                                        colors = outlinedFieldColors()
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedTextField(
                                            value = mfgDate,
                                            onValueChange = { mfgDate = it },
                                            label = { Text("উৎপাদনের তারিখ (Mfg Date)") },
                                            placeholder = { Text("YYYY-MM-DD") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            colors = outlinedFieldColors()
                                        )
                                        OutlinedTextField(
                                            value = expiryDate,
                                            onValueChange = { expiryDate = it },
                                            label = { Text("মেয়াদ উত্তীর্ণ (Expiry Date)") },
                                            placeholder = { Text("YYYY-MM-DD") },
                                            modifier = Modifier.weight(1f).testTag("input_expiry_date"),
                                            singleLine = true,
                                            colors = outlinedFieldColors()
                                        )
                                    }

                                    // Quick Expiry Presets
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text("মেয়াদ দ্রুত নির্বাচন করুন:", fontSize = 11.5.sp, color = Slate400)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            val presets = listOf(
                                                Pair("+৬ মাস", 6),
                                                Pair("+১ বছর", 12),
                                                Pair("+২ বছর", 24),
                                                Pair("+৩ বছর", 36)
                                            )
                                            presets.forEach { (label, months) ->
                                                OutlinedButton(
                                                    onClick = {
                                                        val cal = Calendar.getInstance()
                                                        cal.add(Calendar.MONTH, months)
                                                        expiryDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(1.dp, Slate700),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300),
                                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                                ) {
                                                    Text(label, fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }

                                    // Expiry Info Box
                                    if (expiryDate.isNotBlank()) {
                                        val tempProduct = Product(
                                            name = name,
                                            sku = sku,
                                            category = category,
                                            purchasePrice = buyVal,
                                            sellingPrice = sellVal,
                                            stockQuantity = stockStr.toIntOrNull() ?: 0,
                                            expiryDate = expiryDate
                                        )
                                        val days = tempProduct.daysUntilExpiry()
                                        val status = tempProduct.getExpiryStatus()

                                        Card(
                                            shape = RoundedCornerShape(10.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = when (status) {
                                                    ExpiryStatus.EXPIRED -> CoralPink.copy(alpha = 0.15f)
                                                    ExpiryStatus.EXPIRING_SOON -> AmberOrange.copy(alpha = 0.15f)
                                                    ExpiryStatus.SAFE -> EmeraldPrimary.copy(alpha = 0.15f)
                                                    ExpiryStatus.NOT_SET -> Slate800
                                                }
                                            ),
                                            border = BorderStroke(
                                                1.dp,
                                                when (status) {
                                                    ExpiryStatus.EXPIRED -> CoralPink
                                                    ExpiryStatus.EXPIRING_SOON -> AmberOrange
                                                    ExpiryStatus.SAFE -> EmeraldPrimary
                                                    ExpiryStatus.NOT_SET -> Slate700
                                                }
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = when (status) {
                                                        ExpiryStatus.EXPIRED -> Icons.Default.ErrorOutline
                                                        ExpiryStatus.EXPIRING_SOON -> Icons.Default.WarningAmber
                                                        ExpiryStatus.SAFE -> Icons.Default.CheckCircle
                                                        ExpiryStatus.NOT_SET -> Icons.Default.Event
                                                    },
                                                    contentDescription = null,
                                                    tint = when (status) {
                                                        ExpiryStatus.EXPIRED -> CoralPink
                                                        ExpiryStatus.EXPIRING_SOON -> AmberOrange
                                                        ExpiryStatus.SAFE -> EmeraldPrimary
                                                        ExpiryStatus.NOT_SET -> Slate400
                                                    },
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = when (status) {
                                                        ExpiryStatus.EXPIRED -> "⚠️ মেয়াদ উত্তীর্ণ! (${-(days ?: 0)} দিন পূর্বে মেয়াদ শেষ হয়েছে)"
                                                        ExpiryStatus.EXPIRING_SOON -> "⏳ মেয়াদ সন্নিকটে! (আর মাত্র $days দিন বাকি)"
                                                        ExpiryStatus.SAFE -> "✅ মেয়াদ সুরক্ষিত (আর $days দিন বাকি আছে)"
                                                        ExpiryStatus.NOT_SET -> "তারিখ ফরম্যাট সঠিক নয় (ব্যবহার করুন: YYYY-MM-DD)"
                                                    },
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextPrimary
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            3 -> {
                                // Supplier & Note
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = supplierName,
                                        onValueChange = { supplierName = it },
                                        label = { Text("সরবরাহকারী / সাপ্লায়ারের নাম") },
                                        placeholder = { Text("যেমন: গ্লোবাল কসমেটিক্স ইম্পোর্ট") },
                                        modifier = Modifier.fillMaxWidth().testTag("input_supplier_name"),
                                        singleLine = true,
                                        colors = outlinedFieldColors()
                                    )

                                    OutlinedTextField(
                                        value = supplierPhone,
                                        onValueChange = { supplierPhone = it },
                                        label = { Text("সাপ্লায়ারের মোবাইল নম্বর") },
                                        placeholder = { Text("017XXXXXXXX") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        colors = outlinedFieldColors()
                                    )

                                    OutlinedTextField(
                                        value = note,
                                        onValueChange = { note = it },
                                        label = { Text("অতিরিক্ত মন্তব্য / নোট") },
                                        placeholder = { Text("যেমন: অরিজিনাল ইউএসএ সংস্করণ, সীমিত স্টক") },
                                        modifier = Modifier.fillMaxWidth().height(90.dp),
                                        colors = outlinedFieldColors()
                                    )
                                }
                            }
                        }

                        // Error Banner if validation fails
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage ?: "",
                                color = CoralPink,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Footer Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল", color = Slate400, fontSize = 14.sp)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (selectedTab > 0) {
                            OutlinedButton(
                                onClick = { selectedTab-- },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Slate700),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300)
                            ) {
                                Text("পূর্ববর্তী")
                            }
                        }

                        if (selectedTab < tabTitles.lastIndex) {
                            Button(
                                onClick = {
                                    if (selectedTab == 0 && name.isBlank()) {
                                        errorMessage = "পণ্যের নাম প্রদান করা বাধ্যতামূলক!"
                                    } else {
                                        errorMessage = null
                                        selectedTab++
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("পরবর্তী", color = TextPrimary)
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (name.isBlank()) {
                                        errorMessage = "পণ্যের নাম প্রদান করা বাধ্যতামূলক!"
                                        selectedTab = 0
                                        return@Button
                                    }
                                    val buyP = buyPriceStr.toDoubleOrNull() ?: 0.0
                                    val sellP = sellPriceStr.toDoubleOrNull() ?: 0.0
                                    val stk = stockStr.toIntOrNull() ?: 0
                                    val lowLimit = lowStockLimitStr.toIntOrNull() ?: 5
                                    val wholesaleP = wholesalePriceStr.toDoubleOrNull() ?: 0.0
                                    val disc = discountStr.toDoubleOrNull() ?: 0.0
                                    val taxR = taxRateStr.toDoubleOrNull() ?: 0.0

                                    onConfirm(
                                        name.trim(),
                                        category.trim(),
                                        sku.trim(),
                                        buyP,
                                        sellP,
                                        stk,
                                        variant.trim(),
                                        brand.trim(),
                                        lowLimit,
                                        note.trim(),
                                        barcode.trim(),
                                        unit.trim(),
                                        wholesaleP,
                                        disc,
                                        taxR,
                                        stk,
                                        supplierName.trim(),
                                        supplierPhone.trim(),
                                        batchNumber.trim(),
                                        mfgDate.trim(),
                                        expiryDate.trim()
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("button_save_cosmetics_product")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Slate950, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (editingProduct == null) "পণ্য সংরক্ষণ করুন" else "আপডেট সংরক্ষণ করুন",
                                    color = Slate950,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Product Detail Modal with rich stock actions.
 */
@Composable
fun ProductDetailModal(
    product: Product,
    onDismiss: () -> Unit,
    onRestock: () -> Unit,
    onAdjustStock: () -> Unit,
    onViewHistory: () -> Unit,
    onViewBarcode: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val expiryStatus = product.getExpiryStatus()
    val stockStatus = product.getStockStatus()
    val daysUntilExp = product.daysUntilExpiry()
    val profit = (product.sellingPrice - product.purchasePrice).coerceAtLeast(0.0)
    val margin = if (product.purchasePrice > 0) ((profit / product.purchasePrice) * 100) else 0.0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = BorderStroke(1.dp, Slate800),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 580.dp)
                .padding(vertical = 12.dp)
                .testTag("modal_product_detail")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (product.brand.isNotBlank()) {
                                Text(product.brand, fontSize = 12.sp, color = EmeraldPrimary, fontWeight = FontWeight.SemiBold)
                                Text(" • ", fontSize = 12.sp, color = Slate600)
                            }
                            Text(product.category, fontSize = 12.sp, color = Slate400)
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Stock Badge
                    val stockBg = when (stockStatus) {
                        ProductStockStatus.OUT_OF_STOCK -> CoralPink.copy(alpha = 0.18f)
                        ProductStockStatus.LOW_STOCK -> AmberOrange.copy(alpha = 0.18f)
                        ProductStockStatus.IN_STOCK -> EmeraldPrimary.copy(alpha = 0.18f)
                    }
                    val stockFg = when (stockStatus) {
                        ProductStockStatus.OUT_OF_STOCK -> CoralPink
                        ProductStockStatus.LOW_STOCK -> AmberOrange
                        ProductStockStatus.IN_STOCK -> EmeraldPrimary
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(stockBg)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = when (stockStatus) {
                                ProductStockStatus.OUT_OF_STOCK -> "আউট অব স্টক (০)"
                                ProductStockStatus.LOW_STOCK -> "কম স্টক: ${product.stockQuantity} ${product.unit}"
                                ProductStockStatus.IN_STOCK -> "মজুদ: ${product.stockQuantity} ${product.unit}"
                            },
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = stockFg
                        )
                    }

                    // Expiry Badge
                    val expBg = when (expiryStatus) {
                        ExpiryStatus.EXPIRED -> CoralPink.copy(alpha = 0.18f)
                        ExpiryStatus.EXPIRING_SOON -> AmberOrange.copy(alpha = 0.18f)
                        ExpiryStatus.SAFE -> EmeraldPrimary.copy(alpha = 0.18f)
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
                            .clip(RoundedCornerShape(8.dp))
                            .background(expBg)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = when (expiryStatus) {
                                ExpiryStatus.EXPIRED -> "⚠️ মেয়াদ শেষ (${-(daysUntilExp ?: 0)} দিন আগে)"
                                ExpiryStatus.EXPIRING_SOON -> "⏳ মেয়াদ সন্নিকটে ($daysUntilExp দিন বাকি)"
                                ExpiryStatus.SAFE -> "✅ মেয়াদ নিরাপদ ($daysUntilExp দিন)"
                                ExpiryStatus.NOT_SET -> "মেয়াদ তথ্য নেই"
                            },
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = expFg
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Pricing Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate850),
                    border = BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("ক্রয়মূল্য", fontSize = 11.sp, color = Slate400)
                                Text("৳${"%.0f".format(product.purchasePrice)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Column {
                                Text("বিক্রয়মূল্য", fontSize = 11.sp, color = Slate400)
                                Text("৳${"%.0f".format(product.sellingPrice)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }
                            Column {
                                Text("মুনাফা", fontSize = 11.sp, color = Slate400)
                                Text("৳${"%.0f".format(profit)} (${"%.0f".format(margin)}%)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Key Attribute Grid
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate850),
                    border = BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetailRow("বারকোড:", product.barcode.ifBlank { "নেই" })
                        DetailRow("SKU কোড:", product.sku.ifBlank { "নেই" })
                        DetailRow("ভ্যারিয়েন্ট / সাইজ:", product.sizesOrVariants)
                        DetailRow("ব্যাচ নম্বর:", product.batchNumber.ifBlank { "উল্লেখ নেই" })
                        DetailRow("উৎপাদন তারিখ:", product.manufacturingDate.ifBlank { "উল্লেখ নেই" })
                        DetailRow("মেয়াদ উত্তীর্ণ তারিখ:", product.expiryDate.ifBlank { "নির্ধারিত নেই" })
                        DetailRow("সাপ্লায়ার:", product.supplierName.ifBlank { "উল্লেখ নেই" })
                        if (product.supplierPhone.isNotBlank()) {
                            DetailRow("সাপ্লায়ার মোবাইল:", product.supplierPhone)
                        }
                        if (product.note.isNotBlank()) {
                            DetailRow("নোট:", product.note)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons Grid
                Text("স্টক ও পণ্য পরিচালনা:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate300)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRestock,
                        modifier = Modifier.weight(1f).testTag("btn_modal_restock"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Slate950, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("রিস্টক", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onAdjustStock,
                        modifier = Modifier.weight(1f).testTag("btn_modal_adjust"),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("অ্যাডজাস্ট", color = TextPrimary, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onViewHistory,
                        modifier = Modifier.weight(1f).testTag("btn_modal_history"),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("হিস্ট্রি", color = TextPrimary, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onViewBarcode,
                        modifier = Modifier.weight(1f).testTag("btn_modal_barcode"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Slate700),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300)
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("বারকোড", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f).testTag("btn_modal_edit"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Slate700),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("সম্পাদনা", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f).testTag("btn_modal_delete"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CoralPink.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralPink)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ডিলিট", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 11.5.sp, color = Slate400)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

/**
 * Restock / Purchase In Dialog with Cash Expense integration.
 */
@Composable
fun CosmeticsRestockDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (
        quantity: Int,
        buyPrice: Double,
        supplierName: String,
        supplierPhone: String,
        batchNumber: String,
        mfgDate: String,
        expiryDate: String,
        isCashPaid: Boolean,
        note: String
    ) -> Unit
) {
    var quantityStr by remember { mutableStateOf("") }
    var buyPriceStr by remember { mutableStateOf("%.0f".format(product.purchasePrice)) }
    var supplierName by remember { mutableStateOf(product.supplierName) }
    var supplierPhone by remember { mutableStateOf(product.supplierPhone) }
    var batchNumber by remember { mutableStateOf(product.batchNumber) }
    var mfgDate by remember { mutableStateOf(product.manufacturingDate) }
    var expiryDate by remember { mutableStateOf(product.expiryDate) }
    var isCashPaid by remember { mutableStateOf(true) }
    var note by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val qty = quantityStr.toIntOrNull() ?: 0
    val price = buyPriceStr.toDoubleOrNull() ?: 0.0
    val totalCost = qty * price

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = BorderStroke(1.dp, Slate800),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 520.dp)
                .padding(vertical = 12.dp)
                .testTag("dialog_cosmetics_restock")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("স্টক ইন / রিস্টক", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(product.name, fontSize = 12.sp, color = EmeraldPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Current stock display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate850)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("বর্তমান মজুদ:", fontSize = 12.sp, color = Slate400)
                        Text(
                            "${product.stockQuantity} ${product.unit} (নতুন ব্যালেন্স: ${product.stockQuantity + qty} ${product.unit})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it.filter { ch -> ch.isDigit() }; errorMsg = null },
                        label = { Text("রিস্টক পরিমাণ (${product.unit}) *") },
                        placeholder = { Text("যেমন: ২০") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_restock_qty"),
                        singleLine = true,
                        colors = outlinedFieldColors()
                    )
                    OutlinedTextField(
                        value = buyPriceStr,
                        onValueChange = { buyPriceStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("একক ক্রয়মূল্য (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = outlinedFieldColors()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = batchNumber,
                        onValueChange = { batchNumber = it },
                        label = { Text("নতুন ব্যাচ নম্বর") },
                        placeholder = { Text("BATCH-2026") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = outlinedFieldColors()
                    )
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { expiryDate = it },
                        label = { Text("মেয়াদ উত্তীর্ণ তারিখ") },
                        placeholder = { Text("YYYY-MM-DD") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = outlinedFieldColors()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = supplierName,
                    onValueChange = { supplierName = it },
                    label = { Text("সরবরাহকারী / সাপ্লায়ারের নাম") },
                    placeholder = { Text("সাপ্লায়ারের নাম") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = outlinedFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Cash Expense Option
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate850),
                    border = BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ক্যাশ ড্রয়ার থেকে খরচ", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("মোট ৳${"%.0f".format(totalCost)} টাকা খরচ হিসেবে এন্ট্রি হবে", fontSize = 11.sp, color = Slate400)
                        }
                        Switch(
                            checked = isCashPaid,
                            onCheckedChange = { isCashPaid = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                        )
                    }
                }

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMsg ?: "", color = CoralPink, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল", color = Slate400)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (qty <= 0) {
                                errorMsg = "সঠিক পরিমাণ লিখুন!"
                                return@Button
                            }
                            onConfirm(
                                qty,
                                price,
                                supplierName.trim(),
                                supplierPhone.trim(),
                                batchNumber.trim(),
                                mfgDate.trim(),
                                expiryDate.trim(),
                                isCashPaid,
                                note.trim()
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_confirm_restock")
                    ) {
                        Text("রিস্টক নিশ্চিত করুন", color = Slate950, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Stock Adjustment Dialog (Damaged, Lost, Expired, Inventory Count Correction).
 */
@Composable
fun StockAdjustmentDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (
        adjustmentType: String,
        quantity: Int,
        reason: String,
        note: String
    ) -> Unit
) {
    val adjustmentTypes = listOf(
        Pair("DAMAGED", "নষ্ট পণ্য (Damaged)"),
        Pair("LOST", "হারিয়ে যাওয়া (Lost)"),
        Pair("EXPIRED", "মেয়াদ উত্তীর্ণ অপসরণ (Expired)"),
        Pair("STOCK_OUT", "অন্যান্য হ্রাস (- Stock Out)"),
        Pair("STOCK_IN", "স্টক বৃদ্ধি (+ Stock In)"),
        Pair("CORRECTION", "হিসাব সংশোধন (Correction)")
    )

    var selectedType by remember { mutableStateOf("DAMAGED") }
    var quantityStr by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val qty = quantityStr.toIntOrNull() ?: 0
    val isDeduction = selectedType in listOf("DAMAGED", "LOST", "EXPIRED", "STOCK_OUT")
    val resultingStock = if (isDeduction) (product.stockQuantity - qty).coerceAtLeast(0) else (product.stockQuantity + qty)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = BorderStroke(1.dp, Slate800),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 500.dp)
                .padding(vertical = 12.dp)
                .testTag("dialog_stock_adjustment")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("স্টক অ্যাডজাস্টমেন্ট", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(product.name, fontSize = 12.sp, color = AmberOrange, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Balance preview
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate850),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("বর্তমান মজুদ", fontSize = 11.sp, color = Slate400)
                            Text("${product.stockQuantity} ${product.unit}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Icon(Icons.Default.Tune, contentDescription = null, tint = Slate400)
                        Column(horizontalAlignment = Alignment.End) {
                            Text("সংশোধিত ব্যালেন্স", fontSize = 11.sp, color = Slate400)
                            Text(
                                "$resultingStock ${product.unit}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDeduction) AmberOrange else EmeraldPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("অ্যাডজাস্টমেন্টের কারণ নির্বাচন করুন:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate300)
                Spacer(modifier = Modifier.height(6.dp))

                // Adjustment Type Selector
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    adjustmentTypes.forEach { (typeCode, typeLabel) ->
                        val isSelected = selectedType == typeCode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Slate850 else Color.Transparent)
                                .clickable { selectedType = typeCode }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedType = typeCode },
                                colors = RadioButtonDefaults.colors(selectedColor = EmeraldPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = typeLabel,
                                fontSize = 12.5.sp,
                                color = if (isSelected) TextPrimary else Slate400,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = quantityStr,
                    onValueChange = { quantityStr = it.filter { ch -> ch.isDigit() }; errorMsg = null },
                    label = { Text("সংশোধিত পরিমাণ (${product.unit}) *") },
                    placeholder = { Text("যেমন: ২") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("input_adjust_qty"),
                    singleLine = true,
                    colors = outlinedFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("সংক্ষিপ্ত বিবরণ (Optional)") },
                    placeholder = { Text("যেমন: ভাঙা বোতল পাওয়া গেছে / ডিসপ্লে ক্ষতি") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = outlinedFieldColors()
                )

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMsg ?: "", color = CoralPink, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল", color = Slate400)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (qty <= 0) {
                                errorMsg = "সঠিক পরিমাণ লিখুন!"
                                return@Button
                            }
                            onConfirm(selectedType, qty, reason.trim(), note.trim())
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberOrange),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_confirm_adjust")
                    ) {
                        Text("সমন্বয় সম্পন্ন করুন", color = Slate950, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Barcode Vector Display & Print Preview Dialog.
 */
@Composable
fun BarcodeViewDialog(
    product: Product,
    shopName: String = "কসমেটিক্স শপ",
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val code = product.barcode.ifBlank { product.sku.ifBlank { "890000000000" } }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = BorderStroke(1.dp, Slate800),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 440.dp)
                .padding(vertical = 16.dp)
                .testTag("dialog_barcode_view")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("বারকোড লেবেল", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Realistic Printable Barcode Sticker Preview (White Card with crisp black bars)
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = shopName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = product.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (product.sizesOrVariants.isNotBlank() && product.sizesOrVariants != "Standard") {
                            Text(
                                text = product.sizesOrVariants,
                                fontSize = 11.sp,
                                color = Color(0xFF4B5563)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Render Canvas Barcode
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val barCount = 58
                                val barWidth = size.width / (barCount * 1.5f)
                                val seed = code.hashCode()
                                val random = java.util.Random(seed.toLong())

                                var currentX = (size.width - (barCount * barWidth * 1.3f)) / 2f
                                for (i in 0 until barCount) {
                                    val isBar = if (i < 3 || i > barCount - 4 || i in 28..30) {
                                        true
                                    } else {
                                        random.nextBoolean()
                                    }
                                    if (isBar) {
                                        val thickness = if (random.nextFloat() > 0.6f) barWidth * 1.6f else barWidth
                                        drawRect(
                                            color = Color.Black,
                                            topLeft = Offset(currentX, 0f),
                                            size = Size(thickness, size.height)
                                        )
                                        currentX += thickness + (barWidth * 0.4f)
                                    } else {
                                        currentX += barWidth * 1.2f
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Barcode digits
                        Text(
                            text = code,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.Black,
                            letterSpacing = 3.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Price tag
                        Text(
                            text = "মূল্য: ৳${"%.0f".format(product.sellingPrice)} (ভ্যাট সহ)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF047857)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(code))
                            Toast.makeText(context, "বারকোড কপি হয়েছে: $code", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Slate700),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("কোড কপি")
                    }

                    Button(
                        onClick = {
                            Toast.makeText(context, "প্রিন্ট কমান্ড পাঠানো হয়েছে!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("প্রিন্ট লেবেল", color = Slate950, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Bulk Product Import Dialog with CSV parsing and Demo Cosmetics loading.
 */
@Composable
fun BulkImportDialog(
    onDismiss: () -> Unit,
    onImportProducts: (List<Product>) -> Unit
) {
    val sampleCosmeticsCsv = """
মেবেলিন ফিট মি ফাউন্ডেশন, মেকআপ, 720, 1050, 25, Maybelline, 8901001, 2027-05-31, BATCH-1, বোতল
লরিয়েল হেয়ার সিরাম, হেয়ার কেয়ার, 640, 920, 18, L'Oreal, 8901002, 2026-12-31, BATCH-2, বোতল
সেরাভে ফেসিয়াল ওয়াশ, স্কিন কেয়ার, 1150, 1600, 14, CeraVe, 8901003, 2027-02-28, BATCH-3, বোতল
ম্যাক রুবি উ লিপস্টিক, মেকআপ, 1800, 2450, 10, MAC, 8901004, 2026-11-15, BATCH-4, পিস
গার্নিয়ার মাইসেলার ওয়াটার, স্কিন কেয়ার, 520, 780, 20, Garnier, 8901005, 2026-10-30, BATCH-5, বোতল
ল্যাকমে আইকনিক কাজল, মেকআপ, 290, 450, 30, Lakme, 8901006, 2026-09-20, BATCH-6, পিস
ট্রেসমে কেরাটিন শ্যাম্পু, হেয়ার কেয়ার, 480, 720, 15, Tresemme, 8901007, 2026-08-15, BATCH-7, বোতল
হুদা বিউটি মিনি প্যালেট, মেকআপ, 2200, 3100, 8, Huda Beauty, 8901008, 2027-06-30, BATCH-8, পিস
""".trimIndent()

    var csvText by remember { mutableStateOf("") }
    var parsedProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    var parseError by remember { mutableStateOf<String?>(null) }

    fun parseCsv(text: String): List<Product> {
        val list = mutableListOf<Product>()
        val lines = text.lines().filter { it.isNotBlank() }
        for (line in lines) {
            val tokens = line.split(",").map { it.trim() }
            if (tokens.size >= 5) {
                val pName = tokens.getOrNull(0) ?: ""
                val pCat = tokens.getOrNull(1) ?: "মেকআপ (Makeup)"
                val pBuy = tokens.getOrNull(2)?.toDoubleOrNull() ?: 0.0
                val pSell = tokens.getOrNull(3)?.toDoubleOrNull() ?: 0.0
                val pStock = tokens.getOrNull(4)?.toIntOrNull() ?: 0
                val pBrand = tokens.getOrNull(5) ?: ""
                val pBarcode = tokens.getOrNull(6) ?: ""
                val pExpiry = tokens.getOrNull(7) ?: ""
                val pBatch = tokens.getOrNull(8) ?: ""
                val pUnit = tokens.getOrNull(9) ?: "পিস"

                if (pName.isNotBlank()) {
                    list.add(
                        Product(
                            name = pName,
                            category = pCat,
                            sku = "SKU-${System.currentTimeMillis() % 10000}-${list.size}",
                            purchasePrice = pBuy,
                            sellingPrice = pSell,
                            stockQuantity = pStock,
                            brand = pBrand,
                            barcode = pBarcode,
                            expiryDate = pExpiry,
                            batchNumber = pBatch,
                            unit = pUnit
                        )
                    )
                }
            }
        }
        return list
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = BorderStroke(1.dp, Slate800),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .widthIn(max = 640.dp)
                .fillMaxHeight(0.90f)
                .padding(vertical = 12.dp)
                .testTag("dialog_bulk_import")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("বাল্ক পণ্য ইম্পোর্ট (Bulk Import)", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("CSV টেক্সট থেকে এক ক্লিকে বহু পণ্য যুক্ত করুন", fontSize = 11.sp, color = Slate400)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // One-click demo loader button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            csvText = sampleCosmeticsCsv
                            parsedProducts = parseCsv(sampleCosmeticsCsv)
                            parseError = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("btn_load_sample_cosmetics")
                    ) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("নমুনা প্রসাধনী ডেটা লোড করুন (Demo Data)", color = EmeraldPrimary, fontSize = 11.5.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Format Guide
                Text(
                    text = "ফরম্যাট: নাম, ক্যাটাগরি, ক্রয়মূল্য, বিক্রয়মূল্য, স্টক, ব্র্যান্ড, বারকোড, মেয়াদ (YYYY-MM-DD), ব্যাচ, একক",
                    fontSize = 10.5.sp,
                    color = Slate400
                )

                Spacer(modifier = Modifier.height(6.dp))

                // CSV Text input
                OutlinedTextField(
                    value = csvText,
                    onValueChange = {
                        csvText = it
                        parsedProducts = parseCsv(it)
                        parseError = null
                    },
                    placeholder = { Text("এখানে CSV টেক্সট পেস্ট করুন...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .testTag("input_csv_text"),
                    colors = outlinedFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Preview summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "শনাক্তকৃত পণ্য: ${parsedProducts.size} টি",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (parsedProducts.isNotEmpty()) EmeraldPrimary else Slate400
                    )
                    if (parsedProducts.isNotEmpty()) {
                        Text(
                            text = "মোট স্টক: ${parsedProducts.sumOf { it.stockQuantity }} পিস",
                            fontSize = 12.sp,
                            color = Slate300
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Parsed Table Preview
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate850)
                        .padding(8.dp)
                ) {
                    if (parsedProducts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("ডেটা প্রবেশ করান বা উপরের নমুনা বাটনে ক্লিক করুন", color = Slate500, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(parsedProducts) { p ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Slate900)
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(p.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text("${p.category} • ${p.brand}", fontSize = 10.5.sp, color = Slate400)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("ক্রয়: ৳${"%.0f".format(p.purchasePrice)} | বিক্রয়: ৳${"%.0f".format(p.sellingPrice)}", fontSize = 11.sp, color = TextPrimary)
                                        Text("স্টক: ${p.stockQuantity} ${p.unit}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = EmeraldPrimary)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Footer Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল", color = Slate400)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (parsedProducts.isEmpty()) {
                                parseError = "কোনো বৈধ পণ্য পাওয়া যায়নি!"
                                return@Button
                            }
                            onImportProducts(parsedProducts)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        enabled = parsedProducts.isNotEmpty(),
                        modifier = Modifier.testTag("btn_confirm_bulk_import")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Slate950, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${parsedProducts.size}টি পণ্য ইম্পোর্ট সম্পন্ন করুন", color = Slate950, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Stock Transaction History Dialog for a specific product or all products.
 */
@Composable
fun StockHistoryDialog(
    transactions: List<StockTransaction>,
    productName: String? = null,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = BorderStroke(1.dp, Slate800),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .widthIn(max = 600.dp)
                .fillMaxHeight(0.85f)
                .padding(vertical = 14.dp)
                .testTag("dialog_stock_history")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("স্টক মুভমেন্ট হিস্ট্রি", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(
                            text = productName ?: "সকল পণ্যের স্টক লেনদেন রেকর্ড",
                            fontSize = 11.5.sp,
                            color = EmeraldPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (transactions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("কোনো লেনদেন রেকর্ড পাওয়া যায়নি", color = Slate500, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(transactions) { tx ->
                            val isAddition = tx.quantityChange > 0
                            val badgeColor = when (tx.type) {
                                "PURCHASE", "INITIAL_IMPORT" -> EmeraldPrimary
                                "SALE" -> Color(0xFF60A5FA) // Sky blue
                                "DAMAGED", "EXPIRED", "LOST" -> CoralPink
                                else -> AmberOrange
                            }

                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Slate850),
                                border = BorderStroke(1.dp, Slate800),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(badgeColor.copy(alpha = 0.2f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = when (tx.type) {
                                                        "PURCHASE" -> "রিস্টক / ক্রয়"
                                                        "SALE" -> "বিক্রি"
                                                        "INITIAL_IMPORT" -> "ইম্পোর্ট"
                                                        "DAMAGED" -> "নষ্ট"
                                                        "LOST" -> "হারানো"
                                                        "EXPIRED" -> "মেয়াদ উত্তীর্ণ"
                                                        else -> tx.type
                                                    },
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = badgeColor
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(tx.dateString, fontSize = 11.sp, color = Slate400)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(tx.productName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        if (tx.reason.isNotBlank()) {
                                            Text(tx.reason, fontSize = 11.sp, color = Slate400)
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = if (isAddition) "+${tx.quantityChange}" else "${tx.quantityChange}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isAddition) EmeraldPrimary else CoralPink
                                        )
                                        Text("অবশিষ্ট: ${tx.balanceAfter}", fontSize = 11.sp, color = Slate400)
                                    }
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
fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = EmeraldPrimary,
    unfocusedBorderColor = Slate700,
    focusedLabelColor = EmeraldPrimary,
    unfocusedLabelColor = Slate400,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = EmeraldPrimary,
    focusedContainerColor = Slate850,
    unfocusedContainerColor = Slate850
)
