package com.iptvx.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.iptvx.app.BuildConfig
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.iptvDataStore by preferencesDataStore("iptvx_preferences")

data class AppPreferencesState(
    val deviceId: String,
    val deviceToken: String?,
    val virtualMac: String?,
    val pairingCode: String?,
    val panelUrl: String,
    val paired: Boolean,
    val performanceMode: Boolean
)

class AppPreferences(private val context: Context) {
    private val deviceIdKey = stringPreferencesKey("device_id")
    private val deviceTokenKey = stringPreferencesKey("device_token")
    private val virtualMacKey = stringPreferencesKey("virtual_mac")
    private val pairingCodeKey = stringPreferencesKey("pairing_code")
    private val panelUrlKey = stringPreferencesKey("panel_url")
    private val pairedKey = booleanPreferencesKey("paired")
    private val performanceModeKey = booleanPreferencesKey("performance_mode")

    val state: Flow<AppPreferencesState> = context.iptvDataStore.data.map { prefs ->
        val existingDeviceId = prefs[deviceIdKey].orEmpty()
        AppPreferencesState(
            deviceId = existingDeviceId,
            deviceToken = prefs[deviceTokenKey],
            virtualMac = prefs[virtualMacKey],
            pairingCode = prefs[pairingCodeKey],
            panelUrl = normalizedPanelUrl(prefs[panelUrlKey]),
            paired = prefs[pairedKey] ?: false,
            performanceMode = prefs[performanceModeKey] ?: true
        )
    }

    suspend fun ensureDeviceId(): String {
        val current = context.iptvDataStore.data.first()[deviceIdKey]
        if (!current.isNullOrBlank()) return current
        val generated = UUID.randomUUID().toString()
        context.iptvDataStore.edit { it[deviceIdKey] = generated }
        return generated
    }

    suspend fun savePanelUrl(url: String) {
        context.iptvDataStore.edit { it[panelUrlKey] = url.trim().trimEnd('/') }
    }

    suspend fun saveRegistration(
        virtualMac: String,
        pairingCode: String,
        panelUrl: String,
        paired: Boolean,
        deviceToken: String?
    ) {
        context.iptvDataStore.edit { prefs ->
            prefs[virtualMacKey] = virtualMac
            prefs[pairingCodeKey] = pairingCode
            prefs[panelUrlKey] = panelUrl.trim().trimEnd('/')
            prefs[pairedKey] = paired
            if (!deviceToken.isNullOrBlank()) prefs[deviceTokenKey] = deviceToken
        }
    }

    suspend fun markPaired() {
        context.iptvDataStore.edit { it[pairedKey] = true }
    }

    suspend fun savePerformanceMode(enabled: Boolean) {
        context.iptvDataStore.edit { it[performanceModeKey] = enabled }
    }

    private fun normalizedPanelUrl(savedUrl: String?): String {
        val url = savedUrl?.trim()?.trimEnd('/').orEmpty()
        return if (url.isBlank() || url == "http://10.0.2.2:3000" || url == "http://localhost:3000") {
            BuildConfig.DEFAULT_PANEL_URL
        } else {
            url
        }
    }
}
