package com.turismap.app.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.FileOutputStream

@Composable
actual fun rememberSelectorImagen(onImagenSeleccionada: (String) -> Unit): () -> Unit {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { contentUri ->
            try {
                val inputStream = context.contentResolver.openInputStream(contentUri)
                // Guardamos en filesDir (permanente) en lugar de cacheDir
                val archivoPermanente = File(context.filesDir, "img_perfil_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(archivoPermanente)

                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                // Retornamos la ruta local file:// lista para Coil y Firestore
                onImagenSeleccionada(Uri.fromFile(archivoPermanente).toString())
            } catch (e: Exception) {
                e.printStackTrace()
                onImagenSeleccionada(contentUri.toString())
            }
        }
    }

    return {
        launcher.launch("image/*")
    }
}