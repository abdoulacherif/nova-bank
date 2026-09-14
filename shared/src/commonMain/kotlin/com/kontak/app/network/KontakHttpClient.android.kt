package com.kontak.app.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.android.Android

actual val kontakHttpEngine: HttpClientEngineFactory<*> = Android