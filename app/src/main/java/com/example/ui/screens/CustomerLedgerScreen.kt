package com.example.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.ui.CustomerFilter
import com.example.ui.theme.AmberOrange
import com.example.ui.theme.CoralPink
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.RoyalBlue
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
import com.example.ui.theme.VioletPurple

@Composable
fun CustomerLedgerScreen(
    customers: List<Customer>,
    searchQuery: String,
    filter: CustomerFilter,
    onSearchChange: (String) -> Unit,
    onFilterSelect: (CustomerFilter) -> Unit,
    onCustomerClick: (Customer) -> Unit,
    onOpenAddCustomer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCount = customers.size
    val dueCount = customers.count { it.currentDue > 0 }
    val paidCount = customers.count { it.currentDue <= 0 }

    val filteredCustomers = customers.filter { customer ->
        val matchesQuery = searchQuery.isBlank() ||
                customer.name.contains(searchQuery, ignoreCase = true) ||
                customer.phone.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (filter) {
            CustomerFilter.ALL -> true
            CustomerFilter.HAS_DUE -> customer.currentDue > 0
            CustomerFilter.PAID -> customer.currentDue <= 0
        }
        matchesQuery && matchesFilter
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .testTag("screen_customer_ledger")
    ) {
        val screenWidth = maxWidth
        val isSmallMobile = screenWidth < 360.dp
        val isTablet = screenWidth >= 640.dp
        val isDesktop = screenWidth >= 960.dp
        val gridColumns = if (isDesktop) 3 else if (isTablet) 2 else 1

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 1200.dp)
                .align(Alignment.TopCenter)
                .padding(horizontal = if (isTablet) 20.dp else if (isSmallMobile) 10.dp else 14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "কাস্টমার ও বাকি খাতা",
                        fontSize = if (isTablet) 22.sp else if (isSmallMobile) 17.sp else 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "মোট $totalCount জন • $dueCount জনের বাকি আছে",
                        fontSize = if (isSmallMobile) 11.sp else 12.sp,
                        color = Slate400
                    )
                }

                IconButton(
                    onClick = onOpenAddCustomer,
                    modifier = Modifier
                        .size(if (isSmallMobile) 36.dp else 42.dp)
                        .background(EmeraldPrimary, CircleShape)
                        .testTag("button_add_customer_icon")
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Customer",
                        tint = Color.White,
                        modifier = Modifier.size(if (isSmallMobile) 20.dp else 24.dp)
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = {
                    Text(
                        "কাস্টমারের নাম বা মোবাইল নম্বর খুঁজুন...",
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
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Slate900,
                    unfocusedContainerColor = Slate900,
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = Slate700,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search_customer")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Tabs: সব (12), বাকি আছে (10), পরিশোধিত (2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterTabChip(
                    label = "সব ($totalCount)",
                    isSelected = filter == CustomerFilter.ALL,
                    onClick = { onFilterSelect(CustomerFilter.ALL) },
                    modifier = Modifier.weight(1f),
                    testTag = "filter_tab_all"
                )
                FilterTabChip(
                    label = "বাকি আছে ($dueCount)",
                    isSelected = filter == CustomerFilter.HAS_DUE,
                    onClick = { onFilterSelect(CustomerFilter.HAS_DUE) },
                    modifier = Modifier.weight(1f),
                    testTag = "filter_tab_due"
                )
                FilterTabChip(
                    label = "পরিশোধিত ($paidCount)",
                    isSelected = filter == CustomerFilter.PAID,
                    onClick = { onFilterSelect(CustomerFilter.PAID) },
                    modifier = Modifier.weight(1f),
                    testTag = "filter_tab_paid"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Responsive Customer Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp)
            ) {
                items(filteredCustomers, key = { it.id }) { customer ->
                    CustomerLedgerCard(
                        customer = customer,
                        onClick = { onCustomerClick(customer) }
                    )
                }

                if (filteredCustomers.isEmpty()) {
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
                                        .background(AmberOrange.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.People,
                                        contentDescription = null,
                                        tint = AmberOrange,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank() || filter != CustomerFilter.ALL) "কোনো কাস্টমার খুঁজে পাওয়া যায়নি" else "বাকি খাতায় কোনো কাস্টমার নেই",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank() || filter != CustomerFilter.ALL) "অন্য নাম বা ফিল্টার চেক করে দেখুন" else "উপরের '+ কাস্টমার' বাটনে ক্লিক করে আপনার আসল কাস্টমারের হিসাব শুরু করুন",
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
    }
}

@Composable
fun FilterTabChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) EmeraldPrimary else Slate900)
            .border(
                1.dp,
                if (isSelected) EmeraldPrimary else Slate700,
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Slate950 else Slate300
        )
    }
}

@Composable
fun CustomerLedgerCard(
    customer: Customer,
    onClick: () -> Unit
) {
    val initial = customer.name.firstOrNull()?.toString() ?: "C"
    val avatarColor = when (customer.id.toInt() % 4) {
        0 -> VioletPurple
        1 -> CoralPink
        2 -> RoyalBlue
        else -> AmberOrange
    }

    val cardBorder = if (customer.currentDue > 0) {
        CoralPink.copy(alpha = 0.4f)
    } else {
        Slate700
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("customer_card_${customer.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(avatarColor.copy(alpha = 0.2f))
                    .border(1.dp, avatarColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = avatarColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = customer.phone,
                    fontSize = 12.sp,
                    color = Slate400,
                    modifier = Modifier.padding(top = 1.dp)
                )
                if (customer.lastSaleDate.isNotBlank()) {
                    Text(
                        text = "শেষ বিক্রি: ${customer.lastSaleDate}",
                        fontSize = 11.sp,
                        color = Slate600,
                        modifier = Modifier.padding(top = 1.dp)
                    )
                }
            }

            // Due tag
            if (customer.currentDue > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CoralPink.copy(alpha = 0.15f))
                        .border(1.dp, CoralPink.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "বাকি: ৳ ${"%,.0f".format(customer.currentDue)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoralPink
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(EmeraldPrimary.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "পরিশোধিত ✓",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                }
            }
        }
    }
}
