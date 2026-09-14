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

/**
 * Base de l'API — les mêmes routes que celles utilisées par le site web
 * (kontaks.vercel.app). Aucune clé Supabase ici : tout passe par notre
 * backend, exactement comme sur le site.
 */
const val API_BASE_URL = "https://kontaks.vercel.app"

/** Fourni par chaque plateforme : Android → OkHttp, iOS → Darwin (NSURLSession). */
expect val kontakHttpEngine: HttpClientEngineFactory<*>

/**
 * Client HTTP partagé. HttpCookies conserve automatiquement le cookie de
 * session signé renvoyé par /api/auth/login ou /api/auth/session (POST) —
 * l'équivalent natif de `credentials: 'include'` côté web.
 */
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