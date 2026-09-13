package com.example.sync

enum class SyncState {
    SYNCED,
    SYNCING,
    OFFLINE,
    FAILED
}

data class SyncStatus(
    val state: SyncState = SyncState.SYNCED,
    val pendingCount: Int = 0,
    val lastSyncTime: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
) {
    val displayLabel: String
        get() = when (state) {
            SyncState.SYNCED -> "সব সিঙ্ক হয়েছে"
            SyncState.SYNCING -> "ক্লাউডে সিঙ্ক হচ্ছে..."
            SyncState.OFFLINE -> "অফলাইন মোড"
            SyncState.FAILED -> "সিঙ্ক ব্যর্থ হয়েছে"
        }
}
