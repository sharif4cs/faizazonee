package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.security.AuthResult
import com.example.ui.theme.AmberOrange
import com.example.ui.theme.CoralPink
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.TextPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onAuthenticateOnline: suspend (phone: String, pin: String) -> AuthResult,
    checkNetworkOnline: () -> Boolean,
    initialPhone: String = "01798113899",
    shopName: String = "ফাইজা স্টোর",
    ownerName: String = "মোঃ শরিফ"
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    var phone by remember { mutableStateOf(initialPhone) }
    var pin by remember { mutableStateOf("") }
    var pinVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var lockoutSeconds by remember { mutableLongStateOf(0L) }
    var isDeviceOnline by remember { mutableStateOf(checkNetworkOnline()) }

    // Countdown timer for lockout
    LaunchedEffect(lockoutSeconds) {
        if (lockoutSeconds > 0) {
            delay(1000L)
            lockoutSeconds -= 1
        }
    }

    // Periodic network check
    LaunchedEffect(Unit) {
        while (true) {
            isDeviceOnline = checkNetworkOnline()
            delay(4000L)
        }
    }

    fun executeLogin() {
        if (isLoading || lockoutSeconds > 0) return

        errorMessage = null
        val cleanPhone = phone.trim()
        val cleanPin = pin.trim()

        if (cleanPhone.isEmpty()) {
            errorMessage = "দয়া করে নিবন্ধিত মোবাইল নম্বরটি লিখুন"
            return
        }
        if (cleanPin.isEmpty()) {
            errorMessage = "দয়া করে ৫ ডিজিটের গোপন পিন দিন"
            return
        }

        focusManager.clearFocus()
        isLoading = true

        coroutineScope.launch {
            val result = onAuthenticateOnline(cleanPhone, cleanPin)
            isLoading = false
            when (result) {
                is AuthResult.Success -> {
                    Toast.makeText(context, "অনলাইন যাচাইকরণ সফল! স্বাগতম $shopName", Toast.LENGTH_SHORT).show()
                    onLoginSuccess()
                }
                is AuthResult.OfflineError -> {
                    isDeviceOnline = false
                    errorMessage = "অনলাইন ভেরিফিকেশন ব্যর্থ! ইন্টারনেট চালু করে আবার চেষ্টা করুন।"
                }
                is AuthResult.LockedOut -> {
                    lockoutSeconds = result.remainingSeconds
                    errorMessage = "ভুল পিন দেয়ার কারণে সাময়িক লক! ${result.remainingSeconds} সেকেন্ড পর চেষ্টা করুন।"
                }
                is AuthResult.Error -> {
                    errorMessage = result.message
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        val isTablet = maxWidth >= 600.dp

        // Atmospheric glowing gradient background elements
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            EmeraldPrimary.copy(alpha = 0.14f),
                            Color.Transparent
                        ),
                        radius = 800f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Main Secure Card Frame
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp)
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                if (isDeviceOnline) EmeraldPrimary.copy(alpha = 0.5f) else AmberOrange.copy(alpha = 0.5f),
                                Slate800
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .testTag("card_login_container"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.96f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (isTablet) 32.dp else 22.dp, vertical = 26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Online Security Status Badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isDeviceOnline) EmeraldPrimary.copy(alpha = 0.15f) else AmberOrange.copy(alpha = 0.15f)
                            )
                            .border(
                                1.dp,
                                if (isDeviceOnline) EmeraldPrimary.copy(alpha = 0.4f) else AmberOrange.copy(alpha = 0.4f),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isDeviceOnline) Icons.Default.VerifiedUser else Icons.Default.WifiOff,
                            contentDescription = "Online Security Status",
                            tint = if (isDeviceOnline) EmeraldPrimary else AmberOrange,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isDeviceOnline) "অনলাইন ক্লাউড সিকিউরিটি সক্রিয়" else "অফলাইন - ইন্টারনেট সংযোগ প্রয়োজন",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDeviceOnline) EmeraldPrimary else AmberOrange
                        )
                        if (!isDeviceOnline) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retry Connection",
                                tint = AmberOrange,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { isDeviceOnline = checkNetworkOnline() }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Shop Brand Icon Header
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        EmeraldPrimary.copy(alpha = 0.25f),
                                        Slate800
                                    )
                                )
                            )
                            .border(1.5.dp, EmeraldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = "Shop Brand",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = shopName.ifBlank { "ফাইজা স্টোর" },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "অনলাইন ভেরিফিকেশন ও শপ মালিক সুরক্ষা",
                        fontSize = 12.5.sp,
                        color = Slate400
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Error banner
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut()
                    ) {
                        if (errorMessage != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                                    .background(CoralPink.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                    .border(1.dp, CoralPink.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (lockoutSeconds > 0) Icons.Default.LockClock else Icons.Default.Info,
                                        contentDescription = "Error",
                                        tint = CoralPink,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = errorMessage!!,
                                        color = Color(0xFFFF8096),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    // Field 1: Phone / Username
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "রেজিস্টার্ড মোবাইল নম্বর",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate200
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        LoginCustomInput(
                            value = phone,
                            onValueChange = {
                                phone = it
                                if (errorMessage != null) errorMessage = null
                            },
                            placeholder = "01798113899",
                            leadingIcon = Icons.Default.Phone,
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next,
                            onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                            testTag = "login_input_phone"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Field 2: Password / PIN (Masked with dots, no hint or reveal)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "গোপন সিকিউরিটি পিন (PIN)",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate200
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        LoginCustomInput(
                            value = pin,
                            onValueChange = {
                                if (it.length <= 10) {
                                    pin = it
                                    if (errorMessage != null) errorMessage = null
                                }
                            },
                            placeholder = "•••••",
                            leadingIcon = Icons.Default.Lock,
                            keyboardType = KeyboardType.NumberPassword,
                            isPassword = true,
                            passwordVisible = pinVisible,
                            onTogglePasswordVisibility = { pinVisible = !pinVisible },
                            imeAction = ImeAction.Done,
                            onImeAction = { executeLogin() },
                            testTag = "login_input_pin"
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Lockout countdown display if blocked
                    if (lockoutSeconds > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .background(AmberOrange.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .border(1.dp, AmberOrange.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LockClock,
                                    contentDescription = "Locked",
                                    tint = AmberOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "লগইন লক: $lockoutSeconds সেকেন্ড অপেক্ষা করুন",
                                    color = AmberOrange,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Login Action Button with online verification spinner
                    Button(
                        onClick = { executeLogin() },
                        enabled = !isLoading && lockoutSeconds == 0L,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            disabledContainerColor = Slate800
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("button_login_submit")
                    ) {
                        if (isLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "অনলাইনে যাচাই করা হচ্ছে...",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Login,
                                    contentDescription = "Login",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "অনলাইন যাচাই করে প্রবেশ করুন",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Online Protected Footnote
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate850, RoundedCornerShape(10.dp))
                            .border(1.dp, Slate800, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Protected",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "উচ্চ-নিরাপত্তা এনক্রিপশন ও সুরক্ষা",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "সঠিক পিন ছাড়া কোনো অননুমোদিত ব্যক্তি সিস্টেমে প্রবেশ করতে পারবে না।",
                                    fontSize = 10.5.sp,
                                    color = Slate400,
                                    lineHeight = 14.sp
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
private fun LoginCustomInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePasswordVisibility: () -> Unit = {},
    imeAction: ImeAction = ImeAction.Default,
    onImeAction: () -> Unit = {},
    testTag: String = ""
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onAny = { onImeAction() }
        ),
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        cursorBrush = SolidColor(EmeraldPrimary),
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .background(Slate950, RoundedCornerShape(10.dp))
            .border(1.dp, Slate800, RoundedCornerShape(10.dp))
            .testTag(testTag),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Slate400,
                            fontSize = 13.5.sp
                        )
                    }
                    innerTextField()
                }

                if (isPassword) {
                    IconButton(
                        onClick = onTogglePasswordVisibility,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide PIN" else "Show PIN",
                            tint = Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    )
}
