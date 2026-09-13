package com.example.security

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
    data class LockedOut(val remainingSeconds: Long) : AuthResult()
    object OfflineError : AuthResult()
}

class AuthManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("secure_shop_auth_prefs", Context.MODE_PRIVATE)

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()
    }

    companion object {
        private const val KEY_PHONE = "auth_secure_phone"
        private const val KEY_PIN_HASH = "auth_secure_pin_hash"
        private const val KEY_FAILED_ATTEMPTS = "auth_failed_attempts"
        private const val KEY_LOCKOUT_UNTIL = "auth_lockout_until"
        private const val KEY_SESSION_TOKEN = "auth_session_active"

        const val DEFAULT_REAL_PHONE = "01798113899"
        const val DEFAULT_REAL_PIN = "88990"
        private const val SALT = "faiza_shop_secure_salt_2026"
    }

    init {
        // Initialize default owner credentials if not already stored
        if (!prefs.contains(KEY_PHONE)) {
            prefs.edit()
                .putString(KEY_PHONE, normalizePhone(DEFAULT_REAL_PHONE))
                .putString(KEY_PIN_HASH, hashPin(DEFAULT_REAL_PIN))
                .apply()
        }
    }

    fun isUserLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_SESSION_TOKEN, false)
    }

    fun setLoggedIn(loggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_SESSION_TOKEN, loggedIn).apply()
    }

    fun getRegisteredPhone(): String {
        return prefs.getString(KEY_PHONE, DEFAULT_REAL_PHONE) ?: DEFAULT_REAL_PHONE
    }

    /**
     * Check if device is connected to active internet
     */
    fun isNetworkConnected(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Real online handshake to verify online server connectivity
     */
    suspend fun verifyOnlineServerReachability(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://www.google.com/generate_204")
                .head()
                .build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful || response.code == 204
            }
        } catch (_: Exception) {
            // Fallback second check
            try {
                val fallbackRequest = Request.Builder()
                    .url("https://httpbin.org/status/200")
                    .head()
                    .build()
                client.newCall(fallbackRequest).execute().use { response ->
                    response.isSuccessful
                }
            } catch (_: Exception) {
                false
            }
        }
    }

    /**
     * Authenticate with online check and brute-force prevention
     */
    suspend fun authenticateOnline(inputPhone: String, inputPin: String): AuthResult = withContext(Dispatchers.IO) {
        val currentTime = System.currentTimeMillis()
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)

        // Check if currently locked out
        if (currentTime < lockoutUntil) {
            val remainingSec = (lockoutUntil - currentTime) / 1000
            return@withContext AuthResult.LockedOut(remainingSec)
        }

        // Online Network Verification
        val isOnline = verifyOnlineServerReachability() || isNetworkConnected()
        if (!isOnline) {
            return@withContext AuthResult.OfflineError
        }

        val storedPhone = prefs.getString(KEY_PHONE, normalizePhone(DEFAULT_REAL_PHONE)) ?: normalizePhone(DEFAULT_REAL_PHONE)
        val storedPinHash = prefs.getString(KEY_PIN_HASH, hashPin(DEFAULT_REAL_PIN)) ?: hashPin(DEFAULT_REAL_PIN)

        val cleanPhone = normalizePhone(inputPhone)
        val inputPinHash = hashPin(inputPin.trim())

        if (cleanPhone == storedPhone && inputPinHash == storedPinHash) {
            // Success: Reset failed attempts
            prefs.edit()
                .putInt(KEY_FAILED_ATTEMPTS, 0)
                .putLong(KEY_LOCKOUT_UNTIL, 0L)
                .putBoolean(KEY_SESSION_TOKEN, true)
                .putString("last_verified_pin", inputPin.trim())
                .apply()
            AuthResult.Success
        } else {
            // Failure: Increment failed attempts
            val failedAttempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
            val editor = prefs.edit().putInt(KEY_FAILED_ATTEMPTS, failedAttempts)

            if (failedAttempts >= 5) {
                // Lock out for 3 minutes
                val lockDuration = 3 * 60 * 1000L
                editor.putLong(KEY_LOCKOUT_UNTIL, currentTime + lockDuration)
                editor.apply()
                AuthResult.LockedOut(lockDuration / 1000)
            } else if (failedAttempts >= 3) {
                // Lock out for 45 seconds
                val lockDuration = 45 * 1000L
                editor.putLong(KEY_LOCKOUT_UNTIL, currentTime + lockDuration)
                editor.apply()
                AuthResult.LockedOut(lockDuration / 1000)
            } else {
                editor.apply()
                val remainingTries = 5 - failedAttempts
                AuthResult.Error("মোবাইল নম্বর বা পিন সঠিক নয়! আর $remainingTries বার সুযোগ পাবেন।")
            }
        }
    }

    /**
     * Change owner PIN securely
     */
    fun updateOwnerPin(currentPin: String, newPin: String): Boolean {
        val storedPinHash = prefs.getString(KEY_PIN_HASH, hashPin(DEFAULT_REAL_PIN)) ?: hashPin(DEFAULT_REAL_PIN)
        if (hashPin(currentPin.trim()) != storedPinHash) {
            return false
        }
        prefs.edit()
            .putString(KEY_PIN_HASH, hashPin(newPin.trim()))
            .putString("last_verified_pin", newPin.trim())
            .apply()
        return true
    }

    fun getLastVerifiedPin(): String = prefs.getString("last_verified_pin", DEFAULT_REAL_PIN) ?: DEFAULT_REAL_PIN

    private fun normalizePhone(phone: String): String {
        return phone.replace(" ", "")
            .replace("-", "")
            .removePrefix("+88")
            .trim()
    }

    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest("$pin$SALT".toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
