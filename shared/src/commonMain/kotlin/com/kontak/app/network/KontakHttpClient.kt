package com.kontak.app.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

const val API_BASE_URL = "https://kontaks.vercel.app"

expect val kontakHttpEngine: HttpClientEngineFactory<*>

val kontakHttpClient: HttpClient by lazy {
    HttpClient(kontakHttpEngine) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(HttpCookies)
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }
}