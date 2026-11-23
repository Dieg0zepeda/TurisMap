package com.turismap.app.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val MAX_LADO_IMAGEN_PX = 1280
private const val CALIDAD_JPEG = 82
private const val CARPETA_TEMPORAL_UPLOADS = "turismap_uploads"

@Composable
actual fun rememberSelectorImagen(onImagenSeleccionada: (String) -> Unit): () -> Unit {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    val callbackActual by rememberUpdatedState(onImagenSeleccionada)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val rutaTemporal = try {
                withContext(Dispatchers.IO) {
                    procesarImagenSeleccionada(context, uri)
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                null
            }
            if (!rutaTemporal.isNullOrBlank()) {
                callbackActual(rutaTemporal)
            }
        }
    }

    return {
        launcher.launch("image/*")
    }
}

private fun procesarImagenSeleccionada(context: Context, contentUri: Uri): String? {
    val resolver = context.contentResolver

    val gradosRotacion = try {
        resolver.openInputStream(contentUri)?.use { input ->
            when (
                ExifInterface(input).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            ) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f
    } catch (_: Exception) {
        0f
    }

    val opcionesDimensiones = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    resolver.openInputStream(contentUri)?.use { input ->
        BitmapFactory.decodeStream(input, null, opcionesDimensiones)
    } ?: return null

    if (opcionesDimensiones.outWidth <= 0 || opcionesDimensiones.outHeight <= 0) {
        return null
    }

    val opcionesDecodificacion = BitmapFactory.Options().apply {
        inSampleSize = calcularInSampleSize(
            ancho = opcionesDimensiones.outWidth,
            alto = opcionesDimensiones.outHeight,
            maxLado = MAX_LADO_IMAGEN_PX
        )
    }

    val bitmapOriginal = resolver.openInputStream(contentUri)?.use { input ->
        BitmapFactory.decodeStream(input, null, opcionesDecodificacion)
    } ?: return null

    var bitmapTrabajo = bitmapOriginal
    try {
        if (gradosRotacion != 0f) {
            val matriz = Matrix().apply { postRotate(gradosRotacion) }
            val rotado = Bitmap.createBitmap(
                bitmapTrabajo,
                0,
                0,
                bitmapTrabajo.width,
                bitmapTrabajo.height,
                matriz,
                true
            )
            if (rotado !== bitmapTrabajo) {
                bitmapTrabajo.recycle()
                bitmapTrabajo = rotado
            }
        }

        val ladoMayor = max(bitmapTrabajo.width, bitmapTrabajo.height)
        if (ladoMayor > MAX_LADO_IMAGEN_PX) {
            val escala = MAX_LADO_IMAGEN_PX.toFloat() / ladoMayor.toFloat()
            val nuevoAncho = (bitmapTrabajo.width * escala).roundToInt().coerceAtLeast(1)
            val nuevoAlto = (bitmapTrabajo.height * escala).roundToInt().coerceAtLeast(1)
            val redimensionado = Bitmap.createScaledBitmap(bitmapTrabajo, nuevoAncho, nuevoAlto, true)
            if (redimensionado !== bitmapTrabajo) {
                bitmapTrabajo.recycle()
                bitmapTrabajo = redimensionado
            }
        }

        val carpetaTemporal = File(context.cacheDir, CARPETA_TEMPORAL_UPLOADS).apply {
            if (!exists()) mkdirs()
        }
        // Limpiar temporales previos para evitar crecimiento indefinido en disco
        carpetaTemporal.listFiles()?.forEach { archivoPrevio ->
            if (archivoPrevio.isFile) {
                archivoPrevio.delete()
            }
        }

        val archivoTemporal = File(carpetaTemporal, "upload_${System.currentTimeMillis()}.jpg")
        FileOutputStream(archivoTemporal).use { output ->
            val comprimido = bitmapTrabajo.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, output)
            if (!comprimido) {
                archivoTemporal.delete()
                return null
            }
            output.flush()
        }

        return Uri.fromFile(archivoTemporal).toString()
    } finally {
        if (!bitmapTrabajo.isRecycled) {
            bitmapTrabajo.recycle()
        }
    }
}

private fun calcularInSampleSize(ancho: Int, alto: Int, maxLado: Int): Int {
    var inSampleSize = 1
    while ((ancho / (inSampleSize * 2)) >= maxLado || (alto / (inSampleSize * 2)) >= maxLado) {
        inSampleSize *= 2
    }
    return inSampleSize
}
