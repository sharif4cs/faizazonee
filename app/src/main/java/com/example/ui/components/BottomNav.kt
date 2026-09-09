package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ScreenTab
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate900

data class NavItem(
    val tab: ScreenTab,
    val titleBangla: String,
    val titleEnglish: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun ShopBottomNavigation(
    currentTab: ScreenTab,
    cartItemCount: Int,
    onTabSelected: (ScreenTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem(ScreenTab.DASHBOARD, "ড্যাশবোর্ড", "Dashboard", Icons.Default.Dashboard, "nav_dashboard"),
        NavItem(ScreenTab.POS, "বিক্রি (POS)", "POS", Icons.Default.ShoppingCart, "nav_pos"),
        NavItem(ScreenTab.STOCK, "স্টক", "Stock", Icons.Default.Inventory2, "nav_stock"),
        NavItem(ScreenTab.BAKI, "বাকি", "Baki", Icons.Default.PeopleAlt, "nav_baki"),
        NavItem(ScreenTab.MENU, "মেনু", "Menu", Icons.Default.Menu, "nav_menu")
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Slate900)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentTab == item.tab
                val tintColor = if (isSelected) EmeraldPrimary else Slate400

                Column(
                    modifier = Modifier
                        .testTag(item.testTag)
                        .clip(CircleShape)
                        .clickable {
                            onTabSelected(item.tab)
                        }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (item.tab == ScreenTab.POS && cartItemCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = EmeraldPrimary,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = cartItemCount.toString(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.titleBangla,
                                tint = tintColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.titleBangla,
                            tint = tintColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Text(
                        text = item.titleBangla,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = tintColor,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
