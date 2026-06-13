package com.iptvx.app.data.remote

import com.iptvx.app.data.model.DeviceAuthRequest
import com.iptvx.app.data.model.RegisterDeviceRequest
import com.iptvx.app.data.model.RegisterDeviceResponse
import com.iptvx.app.data.model.SyncResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.timeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class PanelApi(
    private val client: HttpClient = defaultClient()
) {
    suspend fun register(panelUrl: String, request: RegisterDeviceRequest): RegisterDeviceResponse {
        return client.post("${panelUrl.trimEnd('/')}/api/device/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun refreshCode(panelUrl: String, deviceId: String, token: String): RegisterDeviceResponse {
        return client.post("${panelUrl.trimEnd('/')}/api/device/refresh-code") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(DeviceAuthRequest(deviceId, token))
        }.body()
    }

    suspend fun sync(panelUrl: String, deviceId: String, token: String): SyncResponse {
        return client.post("${panelUrl.trimEnd('/')}/api/device/sync") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(DeviceAuthRequest(deviceId, token))
        }.body()
    }

    suspend fun downloadText(url: String): String {
        return client.get(url) {
            timeout {
                connectTimeoutMillis = 15_000
                requestTimeoutMillis = 90_000
                socketTimeoutMillis = 60_000
            }
        }.bodyAsText()
    }

    companion object {
        fun defaultClient(): HttpClient = HttpClient(Android) {
            expectSuccess = true
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                })
            }
            install(HttpTimeout) {
                connectTimeoutMillis = 10_000
                requestTimeoutMillis = 30_000
                socketTimeoutMillis = 30_000
            }
        }
    }
}
