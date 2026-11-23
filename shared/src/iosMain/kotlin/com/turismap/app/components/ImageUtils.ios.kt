package com.turismap.app.components

import dev.gitlive.firebase.storage.File
import platform.Foundation.NSURL

actual fun getFirebaseFile(uri: String): File? {
    val uriLimpia = uri.trim()
    if (uriLimpia.isEmpty()) return null
    return try {
        val url = NSURL.URLWithString(uriLimpia) ?: return null
        File(url)
    } catch (_: Exception) {
        null
    }
}
