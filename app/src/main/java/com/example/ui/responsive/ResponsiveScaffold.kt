package com.example.ui.responsive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.data.model.ShopProfile
import com.example.ui.ScreenTab
import com.example.ui.SubScreen
import com.example.ui.components.ShopBottomNavigation
import com.example.ui.theme.Slate950

/**
 * Responsive Shop App Scaffold:
 * Adapts automatically between:
 * - Mobile (<600dp): Material Bottom Navigation Bar
 * - Tablet (600dp..840dp): Navigation Rail on Left + Content on Right
 * - Desktop/Wide (>840dp): Permanent Navigation Drawer on Left + Content on Right
 */
@Composable
fun ResponsiveShopScaffold(
    shopProfile: ShopProfile,
    currentTab: ScreenTab,
    activeSubScreen: SubScreen,
    cartItemCount: Int,
    onTabSelected: (ScreenTab) -> Unit,
    onOpenReports: () -> Unit = {},
    onOpenExpenses: () -> Unit = {},
    onLockApp: () -> Unit = {},
    onLogout: () -> Unit = {},
    content: @Composable (ResponsiveState) -> Unit
) {
    ProvideResponsiveLayout { responsiveState ->
        val showSideNav = activeSubScreen == SubScreen.NONE || responsiveState.isDesktop

        if (responsiveState.isCompact) {
            // Mobile Layout (<600dp)
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Slate950,
                contentWindowInsets = WindowInsets.statusBars,
                bottomBar = {
                    if (activeSubScreen == SubScreen.NONE) {
                        ShopBottomNavigation(
                            currentTab = currentTab,
                            cartItemCount = cartItemCount,
                            onTabSelected = onTabSelected
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Slate950)
                ) {
                    content(responsiveState)
                }
            }
        } else {
            // Tablet & Desktop Layout (>=600dp)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Slate950)
            ) {
                if (showSideNav) {
                    if (responsiveState.isTablet) {
                        ShopNavigationRail(
                            currentTab = currentTab,
                            cartItemCount = cartItemCount,
                            onTabSelected = onTabSelected,
                            onLockApp = onLockApp
                        )
                    } else {
                        ShopPermanentSideNavigation(
                            shopProfile = shopProfile,
                            currentTab = currentTab,
                            cartItemCount = cartItemCount,
                            onTabSelected = onTabSelected,
                            onOpenReports = onOpenReports,
                            onOpenExpenses = onOpenExpenses,
                            onLockApp = onLockApp,
                            onLogout = onLogout
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Slate950)
                ) {
                    content(responsiveState)
                }
            }
        }
    }
}
