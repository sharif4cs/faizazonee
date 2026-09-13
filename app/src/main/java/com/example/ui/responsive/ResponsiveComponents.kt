package com.example.ui.responsive

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.TextPrimary

/**
 * Responsive Top App Bar with adaptive font sizing and action buttons.
 */
@Composable
fun ResponsiveTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val responsive = LocalResponsiveState.current
    val titleSize = when {
        responsive.isSmallMobile -> 17.sp
        responsive.isCompact -> 19.sp
        else -> 22.sp
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = responsive.screenHorizontalPadding,
                vertical = if (responsive.isSmallMobile) 8.dp else 12.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(if (responsive.isSmallMobile) 36.dp else 40.dp)
                        .background(Slate850, RoundedCornerShape(10.dp))
                        .border(1.dp, Slate800, RoundedCornerShape(10.dp))
                        .testTag("button_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    fontSize = titleSize,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        fontSize = if (responsive.isSmallMobile) 10.5.sp else 11.5.sp,
                        color = Slate400,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (actions != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                actions()
            }
        }
    }
}

/**
 * Responsive Card with adaptive padding, border and radius.
 */
@Composable
fun ResponsiveCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(14.dp),
    colors: CardColors = CardDefaults.cardColors(containerColor = Slate900),
    border: BorderStroke? = BorderStroke(1.dp, Slate800),
    elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = colors,
        border = border,
        elevation = elevation,
        content = content
    )
}

/**
 * Responsive Search Bar with quick scanner button and clean placeholders.
 */
@Composable
fun ResponsiveSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "সার্চ করুন...",
    onScanClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    testTag: String = "input_search"
) {
    val responsive = LocalResponsiveState.current

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = Slate600,
                    fontSize = if (responsive.isSmallMobile) 12.sp else 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = Slate400,
                            modifier = Modifier.size(18.dp)
                        )
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
            modifier = Modifier
                .weight(1f)
                .testTag(testTag)
        )

        if (onScanClick != null) {
            IconButton(
                onClick = onScanClick,
                modifier = Modifier
                    .size(if (responsive.isSmallMobile) 46.dp else 52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate900)
                    .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                    .testTag("button_barcode_scan")
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Scan Barcode",
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * Responsive Modal Dialog:
 * Features:
 * - Automatically fits mobile screen width safely without overflowing.
 * - Caps max width on tablets and desktop for optimum readability.
 * - Caps max height with vertical scrolling for scrollable body.
 * - Uses imePadding() so keyboard never hides inputs or submit buttons.
 * - Sticky header and sticky bottom action buttons.
 */
@Composable
fun ResponsiveDialog(
    onDismissRequest: () -> Unit,
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    maxWidth: Dp = 640.dp,
    dismissible: Boolean = true,
    confirmButton: (@Composable () -> Unit)? = null,
    dismissButton: (@Composable () -> Unit)? = null,
    testTag: String = "responsive_dialog",
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = dismissible,
            dismissOnBackPress = dismissible
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            val screenWidth = maxWidth
            val dialogTargetWidth = if (screenWidth < 600.dp) screenWidth else minOf(screenWidth * 0.9f, 680.dp)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                modifier = modifier
                    .width(dialogTargetWidth)
                    .widthIn(max = 680.dp)
                    .heightIn(max = maxHeight * 0.92f)
                    .border(1.dp, Slate800, RoundedCornerShape(16.dp))
                    .testTag(testTag)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Sticky Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!subtitle.isNullOrBlank()) {
                                Text(
                                    text = subtitle,
                                    fontSize = 11.5.sp,
                                    color = Slate400,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (dismissible) {
                            IconButton(
                                onClick = onDismissRequest,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("dialog_button_close")
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Slate400,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Slate800, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Scrollable Body
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        content()
                    }

                    // Sticky Action Buttons
                    if (confirmButton != null || dismissButton != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Slate800, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (dismissButton != null) {
                                dismissButton()
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            if (confirmButton != null) {
                                confirmButton()
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Helper to display either 2 items side by side (if tablet/desktop) or stacked (if mobile).
 */
@Composable
fun ResponsiveRowOrColumn(
    modifier: Modifier = Modifier,
    spacing: Dp = 10.dp,
    content: @Composable (isWide: Boolean) -> Unit
) {
    val responsive = LocalResponsiveState.current
    val isWide = responsive.width >= 560.dp

    if (isWide) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            content(true)
        }
    } else {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            content(false)
        }
    }
}
