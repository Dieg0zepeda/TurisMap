package com.turismap.app.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIPasteboard

@Composable
actual fun rememberCompartirTexto(): (texto: String, asunto: String) -> Unit = remember {
    { texto, _ ->
        val textoLimpio = texto.trim()
        if (textoLimpio.isNotEmpty()) {
            try {
                val rootController = UIApplication.sharedApplication.keyWindow?.rootViewController
                if (rootController != null) {
                    val activityController = UIActivityViewController(
                        activityItems = listOf(textoLimpio),
                        applicationActivities = null
                    )
                    activityController.popoverPresentationController?.sourceView = rootController.view
                    rootController.presentViewController(
                        activityController,
                        animated = true,
                        completion = null
                    )
                } else {
                    UIPasteboard.generalPasteboard.string = textoLimpio
                }
            } catch (_: Exception) {
                try {
                    UIPasteboard.generalPasteboard.string = textoLimpio
                } catch (_: Exception) {
                    // Fallback seguro
                }
            }
        }
    }
}
