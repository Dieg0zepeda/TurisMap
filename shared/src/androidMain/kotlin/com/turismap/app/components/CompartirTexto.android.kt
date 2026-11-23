package com.turismap.app.components

import android.app.Activity
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberCompartirTexto(): (texto: String, asunto: String) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { texto, asunto ->
            val textoLimpio = texto.trim()
            if (textoLimpio.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, textoLimpio)
                    if (asunto.isNotBlank()) {
                        putExtra(Intent.EXTRA_SUBJECT, asunto.trim())
                    }
                }
                val chooser = Intent.createChooser(intent, asunto.trim().ifBlank { null }).apply {
                    if (context !is Activity) {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                }
                try {
                    context.startActivity(chooser)
                } catch (_: Exception) {
                    // Sin aplicación compatible para compartir
                }
            }
        }
    }
}
