package com.kontak.app.platform

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Le contexte applicatif est fourni une seule fois par MainActivity au
 * démarrage — nécessaire car openUrl() est une fonction "expect" sans
 * paramètre de contexte (pour rester identique côté iOS).
 */
object AndroidAppContext {
    lateinit var context: Context
}

actual fun openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    AndroidAppContext.context.startActivity(intent)
}
