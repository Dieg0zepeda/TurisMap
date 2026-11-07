package com.turismap.app.components

import android.net.Uri
import dev.gitlive.firebase.storage.File

actual fun getFirebaseFile(uri: String): File? {
    return try {
        // En Android, GitLive Storage crea el File a partir de un Uri nativo
        File(Uri.parse(uri))
    } catch (e: Exception) {
        null
    }
}