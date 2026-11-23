package com.turismap.app.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest

private val imageLoaderLock = Any()

@Volatile
private var imageLoaderCompartido: ImageLoader? = null

private fun obtenerImageLoaderCompartido(context: Context): ImageLoader {
    imageLoaderCompartido?.let { return it }
    return synchronized(imageLoaderLock) {
        imageLoaderCompartido?.let { return@synchronized it }
        val appContext = context.applicationContext
        ImageLoader.Builder(appContext)
            .memoryCache {
                MemoryCache.Builder(appContext)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(appContext.cacheDir.resolve("turismap_image_cache"))
                    .maxSizeBytes(64L * 1024L * 1024L)
                    .build()
            }
            // Ignora cabeceras restrictivas (ej. Firebase Storage) para reutilizar caché de disco y memoria
            .respectCacheHeaders(false)
            .crossfade(false)
            .build()
            .also { creado ->
                imageLoaderCompartido = creado
            }
    }
}

@Composable
actual fun RemoteImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale
) {
    val appContext = LocalContext.current.applicationContext
    val urlLimpia = url.trim()
    val imageLoader = remember(appContext) {
        obtenerImageLoaderCompartido(appContext)
    }
    val imageRequest = remember(appContext, urlLimpia) {
        ImageRequest.Builder(appContext)
            .data(urlLimpia)
            .memoryCacheKey(urlLimpia)
            .diskCacheKey(urlLimpia)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    AsyncImage(
        model = imageRequest,
        contentDescription = contentDescription,
        imageLoader = imageLoader,
        modifier = modifier,
        contentScale = contentScale
    )
}
