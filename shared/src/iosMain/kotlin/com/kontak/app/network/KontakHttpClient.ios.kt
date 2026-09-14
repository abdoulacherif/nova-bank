package com.kontak.app.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

actual val kontakHttpEngine: HttpClientEngineFactory<*> = Darwin