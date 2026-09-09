package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShopProfile
import com.example.ui.ScreenTab
import com.example.ui.theme.CoralPink
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.TextPrimary

@Composable
fun MenuScreen(
    shopProfile: ShopProfile,
    onNavigateTab: (ScreenTab) -> Unit,
    onOpenShopInfo: () -> Unit,
    onOpenReports: () -> Unit,
    onResetDemoData: () -> Unit,
    onLogout: () -> Unit = {},
    onChangePin: (currentPin: String, newPin: String) -> Boolean = { _, _ -> true },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showClearDataConfirmDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var currentPinInput by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var pinErrorMsg by remember { mutableStateOf<String?>(null) }

    if (showChangePinDialog) {
        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            containerColor = Slate900,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "সিকিউরিটি পিন পরিবর্তন",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "দোকানের হিসাব সুরক্ষিত রাখতে আপনার বর্তমান ও নতুন পিন দিন।",
                        color = Slate400,
                        fontSize = 12.5.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (pinErrorMsg != null) {
                        Text(
                            text = pinErrorMsg!!,
                            color = CoralPink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    OutlinedTextField(
                        value = currentPinInput,
                        onValueChange = {
                            currentPinInput = it
                            pinErrorMsg = null
                        },
                        label = { Text("বর্তমান পিন") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = Slate800
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = {
                            newPinInput = it
                            pinErrorMsg = null
                        },
                        label = { Text("নতুন পিন (কমপক্ষে ৪-৫ ডিজিট)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = Slate800
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = confirmPinInput,
                        onValueChange = {
                            confirmPinInput = it
                            pinErrorMsg = null
                        },
                        label = { Text("নতুন পিন পুনরায় লিখুন") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = Slate800
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (currentPinInput.isBlank()) {
                            pinErrorMsg = "বর্তমান পিন দিন"
                            return@Button
                        }
                        if (newPinInput.length < 4) {
                            pinErrorMsg = "নতুন পিন কমপক্ষে ৪ ডিজিট হতে হবে"
                            return@Button
                        }
                        if (newPinInput != confirmPinInput) {
                            pinErrorMsg = "নতুন পিন দুটি মেলেনি!"
                            return@Button
                        }
                        val success = onChangePin(currentPinInput, newPinInput)
                        if (success) {
                            showChangePinDialog = false
                            currentPinInput = ""
                            newPinInput = ""
                            confirmPinInput = ""
                            Toast.makeText(context, "সিকিউরিটি পিন সফলভাবে আপডেট করা হয়েছে!", Toast.LENGTH_SHORT).show()
                        } else {
                            pinErrorMsg = "বর্তমান পিন সঠিক নয়! অনুগ্রহ করে যাচাই করুন।"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) {
                    Text("বাতিল", color = Slate400)
                }
            }
        )
    }

    if (showClearDataConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataConfirmDialog = false },
            containerColor = Slate900,
            title = {
                Text(
                    text = "সব তথ্য মুছে ফেলবেন?",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "দোকানের সমস্ত বিক্রি, খরচ, পণ্য ও বাকি খাতার তথ্য স্থায়ীভাবে মুছে যাবে। আপনি কি একদম নতুনভাবে শুরু করতে চান?",
                    color = Slate400,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearDataConfirmDialog = false
                        onResetDemoData()
                        Toast.makeText(context, "সব তথ্য মুছে ডেমো রিসেট করা হয়েছে", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPink)
                ) {
                    Text("হ্যাঁ, সব মুছুন", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataConfirmDialog = false }) {
                    Text("বাতিল", color = Slate400)
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 900.dp)
        ) {
            // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "মেনু ও সেটিংস",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Shop Profile Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Slate800, RoundedCornerShape(14.dp))
                        .clickable { onOpenShopInfo() }
                        .testTag("menu_shop_profile_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary.copy(alpha = 0.2f))
                                .border(1.dp, EmeraldPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Store,
                                contentDescription = "Shop",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = shopProfile.shopName.ifBlank { "আমার দোকান" },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (shopProfile.ownerName.isNotBlank()) "প্রোপ্রাইটর: ${shopProfile.ownerName}" else "দোকানের তথ্য এডিট করতে চাপুন",
                                fontSize = 12.sp,
                                color = Slate400
                            )
                            if (shopProfile.phone.isNotBlank()) {
                                Text(
                                    text = shopProfile.phone,
                                    fontSize = 11.sp,
                                    color = EmeraldPrimary
                                )
                            }
                        }

                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Edit",
                            tint = Slate400,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "ব্যবস্থাপনা ও স্টক",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate400,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            item {
                MenuItemRow(
                    icon = Icons.Default.Category,
                    title = "পণ্য ক্যাটাগরি",
                    onClick = { onNavigateTab(ScreenTab.STOCK) },
                    testTag = "menu_categories"
                )
            }

            item {
                MenuItemRow(
                    icon = Icons.Default.ColorLens,
                    title = "সাইজ / কালার",
                    onClick = { onNavigateTab(ScreenTab.STOCK) },
                    testTag = "menu_sizes"
                )
            }

            item {
                MenuItemRow(
                    icon = Icons.Default.Scale,
                    title = "ইউনিট ব্যবস্থাপনা",
                    onClick = { onNavigateTab(ScreenTab.STOCK) },
                    testTag = "menu_units"
                )
            }

            item {
                MenuItemRow(
                    icon = Icons.Default.Inventory2,
                    title = "স্টক ব্যবস্থাপনা",
                    onClick = { onNavigateTab(ScreenTab.STOCK) },
                    testTag = "menu_stock_nav"
                )
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "হিসাব ও রিপোর্ট",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate400,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            item {
                MenuItemRow(
                    icon = Icons.Default.Assessment,
                    title = "রিপোর্ট (লাভ-ক্ষতি)",
                    onClick = onOpenReports,
                    testTag = "menu_reports"
                )
            }

            item {
                MenuItemRow(
                    icon = Icons.Default.CloudDownload,
                    title = "ডাটা ব্যাকআপ",
                    onClick = {
                        Toast.makeText(context, "ডাটা ব্যাকআপ সফলভাবে প্রস্তুত হয়েছে", Toast.LENGTH_SHORT).show()
                    },
                    testTag = "menu_backup"
                )
            }

            item {
                MenuItemRow(
                    icon = Icons.Default.Settings,
                    title = "সেটিংস",
                    onClick = onOpenShopInfo,
                    testTag = "menu_settings"
                )
            }

            item {
                MenuItemRow(
                    icon = Icons.Default.Lock,
                    title = "সিকিউরিটি পিন পরিবর্তন (Change PIN)",
                    tintColor = EmeraldPrimary,
                    onClick = { showChangePinDialog = true },
                    testTag = "menu_change_pin"
                )
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "ডাটা রিসেট",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate400,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            item {
                MenuItemRow(
                    icon = Icons.Default.DeleteSweep,
                    title = "সব ডাটা মুছে নতুন শুরু",
                    tintColor = CoralPink,
                    onClick = { showClearDataConfirmDialog = true },
                    testTag = "menu_clear_all_data"
                )
            }

            item {
                MenuItemRow(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    title = "লগ আউট",
                    tintColor = CoralPink,
                    onClick = {
                        onLogout()
                        Toast.makeText(context, "লগ আউট সম্পন্ন হয়েছে", Toast.LENGTH_SHORT).show()
                    },
                    testTag = "menu_logout"
                )
            }
        }
    }
}
}

@Composable
fun MenuItemRow(
    icon: ImageVector,
    title: String,
    tintColor: Color = TextPrimary,
    onClick: () -> Unit,
    testTag: String = ""
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (tintColor == CoralPink) CoralPink.copy(alpha = 0.15f) else Slate800),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (tintColor == CoralPink) CoralPink else EmeraldPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = tintColor,
                modifier = Modifier.weight(1f)
            )

            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Slate400,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
