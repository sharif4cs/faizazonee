package com.example.ui.responsive

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Detailed screen breakpoints for modern responsive Android design.
 * Handles 320dp Small Mobile through 1200dp+ Large Desktop / POS monitors.
 */
enum class ScreenBreakpoint {
    COMPACT_SMALL,   // 320dp - 359dp (Compact phones, Fold outer display)
    COMPACT_NORMAL,  // 360dp - 479dp (Standard modern smartphones)
    COMPACT_LARGE,   // 480dp - 599dp (Large smartphones / Phablets)
    MEDIUM,          // 600dp - 839dp (7-8" Tablets, Foldables unfolded)
    EXPANDED,        // 840dp - 1199dp (10-12" Tablets, Desktop mode)
    EXPANDED_LARGE   // 1200dp+ (Large Screens, POS Dual Display, 14"+ Displays)
}

enum class NavigationDisplayType {
    BOTTOM_BAR,
    NAVIGATION_RAIL,
    PERMANENT_DRAWER
}

/**
 * Encapsulates current responsive state with convenient queries and tokens.
 */
data class ResponsiveState(
    val width: Dp,
    val height: Dp,
    val isLandscape: Boolean
) {
    val breakpoint: ScreenBreakpoint = when {
        width < 360.dp -> ScreenBreakpoint.COMPACT_SMALL
        width < 480.dp -> ScreenBreakpoint.COMPACT_NORMAL
        width < 600.dp -> ScreenBreakpoint.COMPACT_LARGE
        width < 840.dp -> ScreenBreakpoint.MEDIUM
        width < 1200.dp -> ScreenBreakpoint.EXPANDED
        else -> ScreenBreakpoint.EXPANDED_LARGE
    }

    // High-level categorical checks
    val isSmallMobile: Boolean get() = breakpoint == ScreenBreakpoint.COMPACT_SMALL
    val isCompact: Boolean get() = width < 600.dp
    val isMedium: Boolean get() = width in 600.dp..839.dp
    val isExpanded: Boolean get() = width >= 840.dp
    val isTablet: Boolean get() = isMedium
    val isDesktop: Boolean get() = isExpanded
    val isTabletOrLarger: Boolean get() = width >= 600.dp

    // Navigation decision
    val navigationType: NavigationDisplayType get() = when {
        width < 600.dp -> NavigationDisplayType.BOTTOM_BAR
        width < 840.dp -> NavigationDisplayType.NAVIGATION_RAIL
        else -> NavigationDisplayType.PERMANENT_DRAWER
    }

    // Recommended content margins & horizontal padding
    val screenHorizontalPadding: Dp get() = when (breakpoint) {
        ScreenBreakpoint.COMPACT_SMALL -> 10.dp
        ScreenBreakpoint.COMPACT_NORMAL -> 14.dp
        ScreenBreakpoint.COMPACT_LARGE -> 16.dp
        ScreenBreakpoint.MEDIUM -> 20.dp
        ScreenBreakpoint.EXPANDED -> 24.dp
        ScreenBreakpoint.EXPANDED_LARGE -> 32.dp
    }

    // Recommended Dashboard KPI Grid columns
    val dashboardKpiColumns: Int get() = when {
        width < 360.dp -> 1
        width < 600.dp -> 2
        width < 960.dp -> 3
        width < 1300.dp -> 4
        else -> 6
    }

    // Recommended Product / Inventory Grid columns
    val productGridColumns: Int get() = when {
        width < 360.dp -> 1
        width < 600.dp -> 1
        width < 840.dp -> 2
        width < 1200.dp -> 3
        else -> 4
    }

    // POS Layout Mode: Two-column split on tablets and larger
    val isPosSplitLayout: Boolean get() = width >= 720.dp

    // POS Product area weight in split layout
    val posProductAreaWeight: Float get() = if (width >= 1100.dp) 0.62f else 0.58f
    val posCartAreaWeight: Float get() = 1f - posProductAreaWeight

    // Form columns (Add Product, Customer etc.)
    val formColumns: Int get() = when {
        width < 600.dp -> 1
        width < 960.dp -> 2
        else -> 3
    }

    // Recommended Dialog Max Width
    val dialogMaxWidth: Dp get() = when {
        width < 360.dp -> width - 16.dp
        width < 600.dp -> width - 24.dp
        width < 840.dp -> 540.dp
        else -> 680.dp
    }
}

/**
 * Standard Design Dimension tokens for consistent padding, spacing, and radius.
 */
object ResponsiveDimens {
    val spacingXxs = 2.dp
    val spacingXs = 4.dp
    val spacingSm = 8.dp
    val spacingMd = 12.dp
    val spacingLg = 16.dp
    val spacingXl = 20.dp
    val spacingXxl = 24.dp
    val spacingXxxl = 32.dp

    val cardRadiusSmall = 8.dp
    val cardRadiusMedium = 12.dp
    val cardRadiusLarge = 16.dp
    val cardRadiusXLarge = 20.dp

    val minTouchTarget = 48.dp
}

val LocalResponsiveState = compositionLocalOf<ResponsiveState> {
    ResponsiveState(width = 360.dp, height = 800.dp, isLandscape = false)
}

/**
 * Root responsive container that computes window constraints and provides LocalResponsiveState.
 */
@Composable
fun ProvideResponsiveLayout(
    modifier: Modifier = Modifier,
    content: @Composable (ResponsiveState) -> Unit
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val state = remember(maxWidth, maxHeight) {
            ResponsiveState(
                width = maxWidth,
                height = maxHeight,
                isLandscape = maxWidth > maxHeight
            )
        }

        CompositionLocalProvider(LocalResponsiveState provides state) {
            content(state)
        }
    }
}
