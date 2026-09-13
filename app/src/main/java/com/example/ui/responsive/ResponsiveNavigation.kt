package com.example.ui.responsive

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShopProfile
import com.example.ui.ScreenTab
import com.example.ui.theme.CoralPink
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.TextPrimary

data class NavItemData(
    val tab: ScreenTab,
    val titleBangla: String,
    val titleEnglish: String,
    val icon: ImageVector,
    val testTag: String
)

val defaultNavItems = listOf(
    NavItemData(ScreenTab.DASHBOARD, "ড্যাশবোর্ড", "Dashboard", Icons.Default.Dashboard, "nav_dashboard"),
    NavItemData(ScreenTab.POS, "বিক্রি (POS)", "POS", Icons.Default.ShoppingCart, "nav_pos"),
    NavItemData(ScreenTab.STOCK, "স্টক", "Stock", Icons.Default.Inventory2, "nav_stock"),
    NavItemData(ScreenTab.BAKI, "বাকি", "Baki", Icons.Default.PeopleAlt, "nav_baki"),
    NavItemData(ScreenTab.MENU, "মেনু", "Menu", Icons.Default.Menu, "nav_menu")
)

/**
 * Tablet Navigation Rail (600dp - 840dp)
 * Compact vertical bar with icons, badge, and quick actions.
 */
@Composable
fun ShopNavigationRail(
    currentTab: ScreenTab,
    cartItemCount: Int,
    onTabSelected: (ScreenTab) -> Unit,
    onLockApp: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier
            .fillMaxHeight()
            .border(width = 1.dp, color = Slate850, shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
            .windowInsetsPadding(WindowInsets.statusBars),
        containerColor = Slate900,
        contentColor = Slate300,
        header = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(EmeraldPrimary.copy(alpha = 0.15f))
                    .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = "Shop",
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                defaultNavItems.forEach { item ->
                    val isSelected = currentTab == item.tab
                    NavigationRailItem(
                        selected = isSelected,
                        onClick = { onTabSelected(item.tab) },
                        icon = {
                            if (item.tab == ScreenTab.POS && cartItemCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = CoralPink,
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
                                    Icon(item.icon, contentDescription = item.titleBangla)
                                }
                            } else {
                                Icon(item.icon, contentDescription = item.titleBangla)
                            }
                        },
                        label = {
                            Text(
                                text = item.titleBangla,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = EmeraldPrimary,
                            selectedTextColor = EmeraldPrimary,
                            indicatorColor = EmeraldPrimary.copy(alpha = 0.16f),
                            unselectedIconColor = Slate400,
                            unselectedTextColor = Slate400
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }

            // Bottom Quick Action
            Box(
                modifier = Modifier
                    .padding(bottom = 16.dp)
            ) {
                IconButton(
                    onClick = onLockApp,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Slate850)
                        .border(1.dp, Slate700, CircleShape)
                        .testTag("rail_button_lock")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Desktop Permanent Side Navigation Drawer (840dp+)
 * Rich side drawer with shop identity, category sections, active pills, and footer actions.
 */
@Composable
fun ShopPermanentSideNavigation(
    shopProfile: ShopProfile,
    currentTab: ScreenTab,
    cartItemCount: Int,
    onTabSelected: (ScreenTab) -> Unit,
    onOpenReports: () -> Unit = {},
    onOpenExpenses: () -> Unit = {},
    onLockApp: () -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(260.dp)
            .background(Slate900)
            .border(width = 1.dp, color = Slate800, shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Shop Branding Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(EmeraldPrimary.copy(alpha = 0.18f))
                            .border(1.dp, EmeraldPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = shopProfile.shopName.ifBlank { "ফাইজা স্টোর" },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary)
                            )
                            Text(
                                text = if (shopProfile.ownerName.isNotBlank()) shopProfile.ownerName else "অফলাইন POS প্রস্তুত",
                                fontSize = 11.5.sp,
                                color = Slate400,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                HorizontalDivider(color = Slate800, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Section: Main Operations
                Text(
                    text = "প্রধান মেনু (OPERATIONS)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate400,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)
                )

                defaultNavItems.forEach { item ->
                    val isSelected = currentTab == item.tab
                    SideNavItemRow(
                        item = item,
                        isSelected = isSelected,
                        cartCount = if (item.tab == ScreenTab.POS) cartItemCount else 0,
                        onClick = { onTabSelected(item.tab) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section: Financials & Reports
                Text(
                    text = "হিসাব ও অ্যানালিটিক্স",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate400,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)
                )

                SideNavActionRow(
                    title = "রিপোর্ট ও লাভ-ক্ষতি",
                    subtitle = "বিক্রয় অ্যানালিটিক্স",
                    icon = Icons.Default.Assessment,
                    onClick = onOpenReports
                )

                SideNavActionRow(
                    title = "দৈনিক খরচের খাতা",
                    subtitle = "ভাউচার ও হিসাব",
                    icon = Icons.Default.Payments,
                    onClick = onOpenExpenses
                )
            }

            // Bottom Footer Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate800, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Slate950)
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
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "পিন লক",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "নিরাপত্তা সুরক্ষা",
                                fontSize = 10.sp,
                                color = Slate400
                            )
                        }
                    }

                    IconButton(
                        onClick = onLockApp,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Slate850, RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Lock",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SideNavItemRow(
    item: NavItemData,
    isSelected: Boolean,
    cartCount: Int,
    onClick: () -> Unit
) {
    val bg = if (isSelected) EmeraldPrimary.copy(alpha = 0.16f) else Color.Transparent
    val border = if (isSelected) EmeraldPrimary.copy(alpha = 0.4f) else Color.Transparent
    val textAndIconColor = if (isSelected) EmeraldPrimary else Slate300

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.5.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag(item.testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.titleBangla,
                tint = textAndIconColor,
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = item.titleBangla,
                fontSize = 13.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textAndIconColor
            )
        }

        if (cartCount > 0) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(CoralPink)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$cartCount",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun SideNavActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.5.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Slate850),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Slate300,
                modifier = Modifier.size(17.dp)
            )
        }

        Column {
            Text(
                text = title,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                color = Slate300
            )
            Text(
                text = subtitle,
                fontSize = 10.5.sp,
                color = Slate400
            )
        }
    }
}
