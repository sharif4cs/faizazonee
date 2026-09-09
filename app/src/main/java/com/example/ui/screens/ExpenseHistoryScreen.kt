package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Expense
import com.example.ui.AccountBalances
import com.example.ui.theme.AmberOrange
import com.example.ui.theme.CoralPink
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import androidx.compose.material3.TextButton
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ExpenseHistoryScreen(
    expenses: List<Expense>,
    accountBalances: AccountBalances,
    onBack: () -> Unit,
    onOpenAddExpense: () -> Unit,
    onEditExpense: (Expense) -> Unit,
    onDeleteExpense: (Expense) -> Unit,
    onOpenManageBalances: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("সব") }
    var selectedMethodFilter by remember { mutableStateOf("সব") }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }

    val categories = listOf("সব", "দোকান ভাড়া", "বিদ্যুৎ বিল", "কর্মচারীর বেতন", "ইন্টারনেট", "চা-নাস্তা", "যাতায়াত", "প্যাকেজিং", "Marketing / Ads", "অন্যান্য")
    val methods = listOf("সব", "নগদ ক্যাশ", "bKash", "Nagad", "ব্যাংক")

    val filteredExpenses = remember(expenses, searchQuery, selectedCategoryFilter, selectedMethodFilter) {
        expenses.filter { exp ->
            val matchesSearch = searchQuery.isBlank() ||
                    exp.title.contains(searchQuery, ignoreCase = true) ||
                    exp.category.contains(searchQuery, ignoreCase = true) ||
                    exp.customCategory.contains(searchQuery, ignoreCase = true) ||
                    exp.note.contains(searchQuery, ignoreCase = true) ||
                    exp.paymentMethod.contains(searchQuery, ignoreCase = true)

            val matchesCategory = selectedCategoryFilter == "সব" ||
                    (selectedCategoryFilter == "অন্যান্য" && (exp.category == "অন্যান্য" || !categories.contains(exp.category))) ||
                    exp.category == selectedCategoryFilter

            val matchesMethod = selectedMethodFilter == "সব" ||
                    (selectedMethodFilter == "নগদ ক্যাশ" && exp.paymentMethod.startsWith("নগদ")) ||
                    (selectedMethodFilter == "bKash" && exp.paymentMethod.contains("bKash", ignoreCase = true)) ||
                    (selectedMethodFilter == "Nagad" && exp.paymentMethod.contains("Nagad", ignoreCase = true)) ||
                    (selectedMethodFilter == "ব্যাংক" && exp.paymentMethod.contains("ব্যাংক"))

            matchesSearch && matchesCategory && matchesMethod
        }
    }

    val totalExpenseAmount = remember(filteredExpenses) { filteredExpenses.sumOf { it.amount } }
    val cashExpenseAmount = remember(filteredExpenses) {
        filteredExpenses.filter { it.paymentMethod.startsWith("নগদ") || it.paymentMethod.contains("Cash", ignoreCase = true) }.sumOf { it.amount }
    }
    val digitalExpenseAmount = remember(filteredExpenses) { totalExpenseAmount - cashExpenseAmount }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        val screenWidth = maxWidth
        val isTablet = screenWidth >= 600.dp
        val isCompact = screenWidth < 380.dp
        val horizontalPadding = if (isCompact) 10.dp else 16.dp

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isTablet) Modifier.widthIn(max = 840.dp)
                        else Modifier.fillMaxWidth()
                    )
                    .padding(horizontal = horizontalPadding)
                    .testTag("screen_expense_history")
            ) {
                // Top Header
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
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
                                    .size(42.dp)
                                    .background(Slate900, CircleShape)
                                    .border(1.dp, Slate800, CircleShape)
                                    .testTag("button_back_expense_history")
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text(
                                    text = "দোকানের খরচ বিবরণী",
                                    fontSize = if (isCompact) 17.sp else 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "হিসাব ও একাউন্ট লেনদেন",
                                    fontSize = 12.sp,
                                    color = Slate400
                                )
                            }
                        }

                        Button(
                            onClick = onOpenAddExpense,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .heightIn(min = 44.dp)
                                .testTag("button_add_new_expense")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("নতুন খরচ", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                // Live Account Balances Bar
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .border(1.dp, Slate800, RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(if (isCompact) 10.dp else 14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "বর্তমান একাউন্ট ব্যালেন্স",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "পরিবর্তন করুন",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary,
                                    modifier = Modifier
                                        .clickable { onOpenManageBalances() }
                                        .padding(4.dp)
                                        .testTag("button_edit_balances_from_history")
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (screenWidth < 420.dp) {
                                // 2x2 Grid for compact & portrait phones so balances fit comfortably without clipping
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AccountBalanceCompactCard(
                                        title = "ক্যাশ",
                                        icon = Icons.Default.Payments,
                                        balance = accountBalances.cash,
                                        color = EmeraldPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    AccountBalanceCompactCard(
                                        title = "bKash",
                                        icon = Icons.Default.PhoneAndroid,
                                        balance = accountBalances.bkash,
                                        color = Color(0xFFE2136E),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AccountBalanceCompactCard(
                                        title = "Nagad",
                                        icon = Icons.Default.PhoneAndroid,
                                        balance = accountBalances.nagad,
                                        color = Color(0xFFF7941D),
                                        modifier = Modifier.weight(1f)
                                    )
                                    AccountBalanceCompactCard(
                                        title = "ব্যাংক",
                                        icon = Icons.Default.AccountBalance,
                                        balance = accountBalances.bank,
                                        color = Color(0xFF3B82F6),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            } else {
                                // 4 in a row for tablets and wide screens
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AccountBalanceCompactCard(
                                        title = "ক্যাশ",
                                        icon = Icons.Default.Payments,
                                        balance = accountBalances.cash,
                                        color = EmeraldPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    AccountBalanceCompactCard(
                                        title = "bKash",
                                        icon = Icons.Default.PhoneAndroid,
                                        balance = accountBalances.bkash,
                                        color = Color(0xFFE2136E),
                                        modifier = Modifier.weight(1f)
                                    )
                                    AccountBalanceCompactCard(
                                        title = "Nagad",
                                        icon = Icons.Default.PhoneAndroid,
                                        balance = accountBalances.nagad,
                                        color = Color(0xFFF7941D),
                                        modifier = Modifier.weight(1f)
                                    )
                                    AccountBalanceCompactCard(
                                        title = "ব্যাংক",
                                        icon = Icons.Default.AccountBalance,
                                        balance = accountBalances.bank,
                                        color = Color(0xFF3B82F6),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Summary Metric Cards
                item {
                    if (screenWidth < 360.dp) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ExpenseSummaryMetricCard(
                                    title = "মোট খরচ",
                                    amount = totalExpenseAmount,
                                    subtitle = "${filteredExpenses.size} টি এন্ট্রি",
                                    color = CoralPink,
                                    modifier = Modifier.weight(1f)
                                )
                                ExpenseSummaryMetricCard(
                                    title = "ক্যাশ খরচ",
                                    amount = cashExpenseAmount,
                                    subtitle = "ক্যাশ থেকে বাদ",
                                    color = EmeraldPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            ExpenseSummaryMetricCard(
                                title = "ডিজিটাল / ব্যাংক",
                                amount = digitalExpenseAmount,
                                subtitle = "অনলাইন/ব্যাংক",
                                color = AmberOrange,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ExpenseSummaryMetricCard(
                                title = "মোট খরচ",
                                amount = totalExpenseAmount,
                                subtitle = "${filteredExpenses.size} টি এন্ট্রি",
                                color = CoralPink,
                                modifier = Modifier.weight(1f)
                            )
                            ExpenseSummaryMetricCard(
                                title = "ক্যাশ খরচ",
                                amount = cashExpenseAmount,
                                subtitle = "ক্যাশ থেকে বাদ",
                                color = EmeraldPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            ExpenseSummaryMetricCard(
                                title = "ডিজিটাল / ব্যাংক",
                                amount = digitalExpenseAmount,
                                subtitle = "অনলাইন/ব্যাংক",
                                color = AmberOrange,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

        // Search bar
        item {
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("খরচ খুঁজুন (যেমন: ভাড়া, নাস্তা, বিকাশ...)", fontSize = 13.sp, color = Slate400) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Slate400, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Slate400, modifier = Modifier.size(16.dp))
                        }
                    }
                },
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
                modifier = Modifier.fillMaxWidth().testTag("input_search_expenses")
            )
        }

        // Filter chips: Category
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) EmeraldPrimary else Slate900,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) EmeraldPrimary else Slate800,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedCategoryFilter = cat }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else TextSecondary
                        )
                    }
                }
            }
        }

        // Filter chips: Payment Method
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                methods.forEach { met ->
                    val isSelected = selectedMethodFilter == met
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) Slate800 else Slate900,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) EmeraldPrimary else Slate800,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedMethodFilter = met }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = met,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) EmeraldPrimary else Slate400
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Expense Items List
        if (filteredExpenses.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                        .border(1.dp, Slate800, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Slate850, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = Slate400,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "কোনো খরচের রেকর্ড পাওয়া যায়নি",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "আপনার দোকানের যাবতীয় খরচ এন্ট্রি করে নির্ভুল হিসাব রাখুন",
                            fontSize = 12.sp,
                            color = Slate400,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onOpenAddExpense,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("নতুন খরচ যোগ করুন", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        } else {
            items(filteredExpenses, key = { it.id }) { exp ->
                val methodColor = when {
                    exp.paymentMethod.startsWith("নগদ") -> EmeraldPrimary
                    exp.paymentMethod.contains("bKash", ignoreCase = true) -> Color(0xFFE2136E)
                    exp.paymentMethod.contains("Nagad", ignoreCase = true) -> Color(0xFFF7941D)
                    exp.paymentMethod.contains("ব্যাংক") -> Color(0xFF3B82F6)
                    else -> EmeraldPrimary
                }

                val categoryIcon = when (exp.category) {
                    "দোকান ভাড়া" -> "🏢"
                    "বিদ্যুৎ বিল" -> "⚡"
                    "কর্মচারীর বেতন" -> "👥"
                    "ইন্টারনেট" -> "🌐"
                    "চা-নাস্তা" -> "☕"
                    "যাতায়াত" -> "🚗"
                    "প্যাকেজিং" -> "📦"
                    "Marketing / Ads" -> "📢"
                    else -> "📝"
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                        .testTag("item_expense_${exp.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(Slate850, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = categoryIcon, fontSize = 18.sp)
                                }
                                Column {
                                    Text(
                                        text = if (exp.category == "অন্যান্য" && exp.customCategory.isNotBlank()) exp.customCategory else exp.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = exp.dateString,
                                            fontSize = 11.sp,
                                            color = Slate400
                                        )
                                        Text("•", fontSize = 10.sp, color = Slate600)
                                        Text(
                                            text = exp.category,
                                            fontSize = 11.sp,
                                            color = Slate400
                                        )
                                    }
                                }
                            }

                            // Amount
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "- ৳ %,.0f".format(exp.amount),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CoralPink
                                )

                                // Payment method badge
                                Box(
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .background(methodColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .border(0.5.dp, methodColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = exp.paymentMethod.replace(" (ক্যাশ ব্যালেন্স থেকে কমবে)", ""),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = methodColor
                                    )
                                }
                            }
                        }

                        // Note if present
                        if (exp.note.isNotBlank() && exp.note != exp.title) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "নোট: ${exp.note}",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Slate850, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Action Buttons (Edit & Delete)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { onEditExpense(exp) },
                                modifier = Modifier.size(44.dp).testTag("button_edit_expense_${exp.id}")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Slate400, modifier = Modifier.size(18.dp))
                            }
                            IconButton(
                                onClick = { expenseToDelete = exp },
                                modifier = Modifier.size(44.dp).testTag("button_delete_expense_${exp.id}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CoralPink.copy(alpha = 0.85f), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
            } // LazyColumn
        } // Box
    } // BoxWithConstraints

    // Delete Confirmation Dialog
    expenseToDelete?.let { exp ->
        Dialog(onDismissRequest = { expenseToDelete = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CoralPink.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .testTag("dialog_delete_expense_confirm")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "খরচ মুছে ফেলতে চান?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "“${exp.title}” (৳ %,.0f) মুছে ফেলা হলে স্বয়ংক্রিয়ভাবে ব্যালেন্স পুনঃসমন্বয় করা হবে এবং ব্যালেন্সে টাকা ফেরত যোগ হবে।".format(exp.amount),
                        fontSize = 13.sp,
                        color = Slate400
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { expenseToDelete = null },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Text("বাতিল", fontSize = 13.sp, color = Slate400)
                        }
                        Button(
                            onClick = {
                                onDeleteExpense(exp)
                                expenseToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CoralPink),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(44.dp).testTag("button_confirm_delete_expense")
                        ) {
                            Text("মুছে ফেলুন", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountBalanceCompactCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    balance: Double,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Slate850, RoundedCornerShape(10.dp))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
                Text(title, fontSize = 11.sp, color = Slate400)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "৳ %,.0f".format(balance),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun ExpenseSummaryMetricCard(
    title: String,
    amount: Double,
    subtitle: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        modifier = modifier.border(1.dp, Slate800, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, fontSize = 11.sp, color = Slate400)
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "৳ %,.0f".format(amount),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(subtitle, fontSize = 10.sp, color = TextSecondary)
        }
    }
}
