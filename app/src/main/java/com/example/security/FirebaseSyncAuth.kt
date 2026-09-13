package com.example.security

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseSyncAuth(private val context: Context) {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val prefs = context.getSharedPreferences("firebase_sync_auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "FirebaseSyncAuth"
        private const val KEY_CACHED_SHOP_ID = "cached_shop_id"
        private const val KEY_CACHED_UID = "cached_user_uid"
        private const val KEY_LAST_PHONE = "cached_user_phone"
        private const val DOMAIN_SUFFIX = "@faizahisab.app"
    }

    val currentUid: String
        get() = auth.currentUser?.uid ?: prefs.getString(KEY_CACHED_UID, "") ?: ""

    val currentShopId: String
        get() = prefs.getString(KEY_CACHED_SHOP_ID, "") ?: ""

    fun isLocallyAuthenticated(): Boolean {
        return currentUid.isNotBlank() && currentShopId.isNotBlank()
    }

    suspend fun authenticateWithFirebase(phone: String, pin: String): Result<String> = withContext(Dispatchers.IO) {
        val cleanPhone = normalizePhone(phone)
        val email = "u_${cleanPhone}$DOMAIN_SUFFIX"
        // Ensure password is at least 6 characters for Firebase Auth requirements
        val password = "Pin@${pin.trim()}#secure2026"

        try {
            var user: FirebaseUser? = null
            try {
                val signInResult = auth.signInWithEmailAndPassword(email, password).await()
                user = signInResult.user
                Log.d(TAG, "Firebase signIn success for UID: ${user?.uid}")
            } catch (e: Exception) {
                Log.w(TAG, "signIn failed, attempting to register: ${e.message}")
                try {
                    val createResult = auth.createUserWithEmailAndPassword(email, password).await()
                    user = createResult.user
                    Log.d(TAG, "Firebase createUser success for UID: ${user?.uid}")
                } catch (createEx: Exception) {
                    // If user already exists or transient network error
                    Log.e(TAG, "Both signIn and create failed: ${createEx.message}")
                    // If offline, check if we have cached UID
                    if (isLocallyAuthenticated()) {
                        Log.i(TAG, "Falling back to cached local session: $currentShopId")
                        return@withContext Result.success(currentShopId)
                    }
                    return@withContext Result.failure(createEx)
                }
            }

            val uid = user?.uid ?: return@withContext Result.failure(Exception("User UID is null"))
            prefs.edit().putString(KEY_CACHED_UID, uid).putString(KEY_LAST_PHONE, cleanPhone).apply()

            // Resolve or retrieve Shop/Business ID from Firestore
            val shopId = resolveShopIdForUser(uid, cleanPhone)
            prefs.edit().putString(KEY_CACHED_SHOP_ID, shopId).apply()

            Result.success(shopId)
        } catch (e: Exception) {
            Log.e(TAG, "Authentication error: ${e.message}")
            if (isLocallyAuthenticated()) {
                Result.success(currentShopId)
            } else {
                Result.failure(e)
            }
        }
    }

    private suspend fun resolveShopIdForUser(uid: String, phone: String): String {
        return try {
            val userDocRef = firestore.collection("users").document(uid)
            val snapshot = userDocRef.get().await()

            if (snapshot.exists()) {
                val existingShopId = snapshot.getString("shopId")
                if (!existingShopId.isNullOrBlank()) {
                    return existingShopId
                }
            }

            // Shop ID is unique per business / owner account.
            // When user logs in on another device with same account, they get this exact same shopId!
            val generatedShopId = "shop_${phone.ifBlank { uid }}"
            val userData = mapOf(
                "uid" to uid,
                "phone" to phone,
                "shopId" to generatedShopId,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
            userDocRef.set(userData, SetOptions.merge()).await()
            generatedShopId
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving shopId from Firestore: ${e.message}")
            val fallback = prefs.getString(KEY_CACHED_SHOP_ID, "") ?: ""
            if (fallback.isNotBlank()) fallback else "shop_${phone.ifBlank { uid }}"
        }
    }

    fun signOut() {
        try {
            auth.signOut()
        } catch (_: Exception) {}
        prefs.edit()
            .remove(KEY_CACHED_UID)
            .remove(KEY_CACHED_SHOP_ID)
            .apply()
    }

    private fun normalizePhone(phone: String): String {
        return phone.replace(" ", "")
            .replace("-", "")
            .removePrefix("+88")
            .trim()
    }
}
