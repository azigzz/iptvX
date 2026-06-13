package com.iptvx.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterDeviceRequest(
    val deviceId: String,
    val model: String?,
    val manufacturer: String?,
    val androidVersion: String?,
    val appVersion: String?
)

@Serializable
data class RegisterDeviceResponse(
    val virtualMac: String,
    val pairingCode: String,
    val pairingExpiresAt: String? = null,
    val panelUrl: String,
    val paired: Boolean = false,
    val deviceToken: String? = null
)

@Serializable
data class DeviceAuthRequest(
    val deviceId: String,
    val token: String
)

@Serializable
data class SyncResponse(
    val device: SyncDevice,
    val settings: DeviceSettingsDto,
    val playlists: List<PlaylistConfig>
)

@Serializable
data class SyncDevice(
    val id: String,
    val virtualMac: String,
    val pairedAt: String? = null
)

@Serializable
data class DeviceSettingsDto(
    val deviceId: String? = null,
    val parentalPinHash: String? = null,
    val theme: String = "DARK",
    val bufferMode: String = "MEDIUM",
    val performanceMode: Boolean = true
)

@Serializable
data class PlaylistConfig(
    val id: String,
    val name: String,
    val type: PlaylistType,
    val serverUrl: String? = null,
    val username: String? = null,
    val password: String? = null,
    val m3uUrl: String? = null,
    val epgUrl: String? = null,
    val enabled: Boolean = true,
    val updatedAt: String? = null
)

@Serializable
enum class PlaylistType {
    @SerialName("XTREAM")
    XTREAM,

    @SerialName("M3U")
    M3U
}

enum class ContentType {
    LIVE,
    MOVIE,
    SERIES
}

data class M3uChannel(
    val name: String,
    val url: String,
    val tvgId: String? = null,
    val tvgName: String? = null,
    val tvgLogo: String? = null,
    val groupTitle: String? = null,
    val catchup: String? = null,
    val rawAttributes: Map<String, String> = emptyMap()
)

data class XtreamCategory(
    val id: String,
    val name: String
)

data class XtreamStream(
    val id: String,
    val name: String,
    val categoryId: String?,
    val streamIcon: String?,
    val containerExtension: String?,
    val directUrl: String?
)

data class EpgProgramme(
    val channelId: String,
    val channelName: String?,
    val title: String,
    val description: String?,
    val startUtc: Long,
    val endUtc: Long
)

data class PlaybackItem(
    val id: String,
    val title: String,
    val url: String,
    val playlistId: String,
    val contentType: ContentType,
    val logoUrl: String? = null,
    val category: String? = null
)
