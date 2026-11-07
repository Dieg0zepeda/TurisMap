package com.turismap.app.components

import dev.gitlive.firebase.storage.File
import platform.Foundation.NSURL

actual fun getFirebaseFile(uri: String): File? {
    return try {
        // En iOS, usa NSURL (nos servirá para cuando implementemos la galería del iPhone)
        File(NSURL(string = uri))
    } catch (e: Exception) {
        null
    }
}