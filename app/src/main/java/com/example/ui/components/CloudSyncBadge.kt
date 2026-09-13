package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sync.SyncState
import com.example.sync.SyncStatus
import com.example.ui.theme.AmberOrange
import com.example.ui.theme.CoralPink
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun CloudSyncBadge(
    syncStatus: SyncStatus,
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val (bgColor, borderColor, contentColor, icon, text) = when (syncStatus.state) {
        SyncState.SYNCED -> Quintuple(
            EmeraldPrimary.copy(alpha = 0.12f),
            EmeraldPrimary.copy(alpha = 0.4f),
            EmeraldPrimary,
            Icons.Default.CloudDone,
            "সিঙ্ক হয়েছে"
        )
        SyncState.SYNCING -> Quintuple(
            Color(0xFF0284C7).copy(alpha = 0.15f),
            Color(0xFF38BDF8).copy(alpha = 0.5f),
            Color(0xFF38BDF8),
            Icons.Default.Sync,
            "সিঙ্ক হচ্ছে..."
        )
        SyncState.OFFLINE -> Quintuple(
            AmberOrange.copy(alpha = 0.12f),
            AmberOrange.copy(alpha = 0.4f),
            AmberOrange,
            Icons.Default.CloudOff,
            "অফলাইন"
        )
        SyncState.FAILED -> Quintuple(
            CoralPink.copy(alpha = 0.15f),
            CoralPink.copy(alpha = 0.5f),
            CoralPink,
            Icons.Default.ErrorOutline,
            "সিঙ্ক ব্যর্থ"
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onSyncClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("badge_cloud_sync"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        val iconModifier = Modifier.size(16.dp).let {
            if (syncStatus.state == SyncState.SYNCING) it.rotate(rotation) else it
        }

        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = contentColor,
            modifier = iconModifier
        )

        if (showLabel) {
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}

private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
