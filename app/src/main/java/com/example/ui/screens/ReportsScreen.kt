package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Expense
import com.example.data.model.Sale
import com.example.ui.theme.AmberOrange
import com.example.ui.theme.CoralPink
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.TextPrimary
import kotlin.math.max

@Composable
fun ReportsScreen(
    sales: List<Sale>,
    expenses: List<Expense>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("আজ") }
    val tabs = listOf("আজ", "এই সপ্তাহ", "এই মাস", "সব সময়")

    val totalSales = sales.sumOf { it.subtotal - it.discount }
    val totalExpenses = expenses.sumOf { it.amount }
    val grossProfit = totalSales * 0.30 // estimated 30% gross margin or profit
    val netProfit = grossProfit - totalExpenses

    val cashSales = sales.sumOf { it.paidAmount }
    val dueSales = sales.sumOf { it.dueAmount }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 960.dp)
        ) {
            // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("btn_reports_back")
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Text(
                text = "রিপোর্ট ও লাভ-ক্ষতি",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Time Filter Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEach { tab ->
                val isSelected = selectedTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) EmeraldPrimary else Slate900)
                        .border(1.dp, if (isSelected) EmeraldPrimary else Slate800, RoundedCornerShape(10.dp))
                        .clickable { selectedTab = tab }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Slate950 else Slate400
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Stat Cards Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ReportStatBox(
                        title = "মোট বিক্রি",
                        value = "৳ %,.0f".format(totalSales),
                        subtitle = "${sales.size} টি বিক্রি",
                        color = EmeraldPrimary,
                        modifier = Modifier.weight(1f),
                        testTag = "stat_total_sales"
                    )
                    ReportStatBox(
                        title = "মোট খরচ",
                        value = "৳ %,.0f".format(totalExpenses),
                        subtitle = "${expenses.size} টি খরচ",
                        color = CoralPink,
                        modifier = Modifier.weight(1f),
                        testTag = "stat_total_expenses"
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ReportStatBox(
                        title = "মোট লাভ (গ্রস)",
                        value = "৳ %,.0f".format(grossProfit),
                        subtitle = "আনুমানিক ৩০% মার্জিন",
                        color = TextPrimary,
                        modifier = Modifier.weight(1f),
                        testTag = "stat_gross_profit"
                    )
                    ReportStatBox(
                        title = "নেট লাভ",
                        value = "৳ %,.0f".format(netProfit),
                        subtitle = if (netProfit >= 0.0) "লাভের দিকে" else "লোকসান",
                        color = if (netProfit >= 0.0) EmeraldPrimary else CoralPink,
                        modifier = Modifier.weight(1f),
                        testTag = "stat_net_profit"
                    )
                }
            }

            // Sales Trend Mini Chart Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "বিক্রির ট্রেন্ড",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Hourly / trend bars
                        val sampleData = listOf(350f, 650f, 1200f, 800f, 1500f, 2100f, 950f, 1400f)
                        val maxVal = sampleData.maxOrNull() ?: 1f

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                        ) {
                            val barWidth = 24.dp.toPx()
                            val spacing = (size.width - (sampleData.size * barWidth)) / (sampleData.size + 1)

                            sampleData.forEachIndexed { index, valData ->
                                val barHeight = (valData / maxVal) * (size.height * 0.8f)
                                val x = spacing + index * (barWidth + spacing)
                                val y = size.height - barHeight

                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(EmeraldPrimary, Color(0xFF047857))
                                    ),
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(6f, 6f)
                                )
                            }
                        }
                    }
                }
            }

            // Cash vs Due Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "নগদ ও বাকি বিক্রির অনুপাত",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("নগদ আদায়", fontSize = 12.sp, color = Slate400)
                                Text("৳ %,.0f".format(cashSales), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("বাকি বিক্রি", fontSize = 12.sp, color = Slate400)
                                Text("৳ %,.0f".format(dueSales), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AmberOrange)
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
fun ReportStatBox(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Card(
        modifier = modifier
            .border(1.dp, Slate800, RoundedCornerShape(12.dp))
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = Slate400
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Slate400
            )
        }
    }
}
