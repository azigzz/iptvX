package com.iptvx.app.data

import java.security.MessageDigest

fun shortNumericDeviceId(deviceId: String): String {
    if (deviceId.isBlank()) return "..."
    val bytes = MessageDigest.getInstance("SHA-256").digest(deviceId.toByteArray())
    val value =
        ((bytes[0].toLong() and 0xff) shl 24) or
            ((bytes[1].toLong() and 0xff) shl 16) or
            ((bytes[2].toLong() and 0xff) shl 8) or
            (bytes[3].toLong() and 0xff)
    return ((value % 90_000L) + 10_000L).toString()
}
