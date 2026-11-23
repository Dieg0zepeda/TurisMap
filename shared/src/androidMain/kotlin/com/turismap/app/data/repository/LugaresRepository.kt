package com.turismap.app.data.repository

import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import com.turismap.app.data.models.Atractivo
import com.turismap.app.data.models.CategoriaLugar
import com.turismap.app.data.models.Coordenada
import com.turismap.app.data.models.DirectorioStats
import com.turismap.app.data.models.Lugar
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private fun Any?.comoTexto(): String = when (this) {
    is String -> this
    else -> ""
}

private fun Any?.comoDouble(): Double? = when (this) {
    is Number -> toDouble().takeIf(Double::isFinite)
    is String -> trim().replace(",", ".").toDoubleOrNull()?.takeIf(Double::isFinite)
    else -> null
}

private fun Any?.comoLong(): Long? = when (this) {
    is Number -> toLong()
    is String -> trim().removePrefix("#").removePrefix("0x").removePrefix("0X").toLongOrNull(16)
        ?: trim().toLongOrNull()
    else -> null
}

private fun Any?.comoCoordenada(): Coordenada? = when (this) {
    is GeoPoint -> Coordenada(latitud = latitude, longitud = longitude)
    is Map<*, *> -> {
        val lat = (this["latitud"] ?: this["lat"] ?: this["latitude"]).comoDouble()
        val lng = (this["longitud"] ?: this["lng"] ?: this["longitude"]).comoDouble()
        if (lat != null && lng != null) Coordenada(latitud = lat, longitud = lng) else null
    }
    is String -> parsearCoordenadaDesdeTexto(this)
    else -> null
}

private fun Any?.comoAtractivos(): List<Atractivo> {
    val lista = this as? List<*> ?: return emptyList()
    return lista.mapNotNull { elemento ->
        val mapa = elemento as? Map<*, *> ?: return@mapNotNull null
        try {
            Atractivo(
                id = mapa["id"].comoTexto(),
                etiqueta = mapa["etiqueta"].comoTexto(),
                etiquetaColorHex = mapa["etiquetaColorHex"].comoLong() ?: 0L,
                titulo = mapa["titulo"].comoTexto(),
                descripcion = mapa["descripcion"].comoTexto(),
                accion = mapa["accion"].comoTexto(),
                imagenUrl = mapa["imagenUrl"].comoTexto(),
                ubicacionCoordenadas = (mapa["ubicacionCoordenadas"] ?: mapa["coordenadas"]).comoCoordenada()
            )
        } catch (_: Exception) {
            null
        }
    }
}

private fun Any?.comoDirectorioStats(): DirectorioStats {
    val mapa = this as? Map<*, *> ?: return DirectorioStats()
    return DirectorioStats(
        tituloGeneral = mapa["tituloGeneral"].comoTexto(),
        descripcionGeneral = mapa["descripcionGeneral"].comoTexto(),
        stat1Icon = mapa["stat1Icon"].comoTexto(),
        stat1Texto = mapa["stat1Texto"].comoTexto(),
        stat2Icon = mapa["stat2Icon"].comoTexto(),
        stat2Texto = mapa["stat2Texto"].comoTexto(),
        stat3Icon = mapa["stat3Icon"].comoTexto(),
        stat3Texto = mapa["stat3Texto"].comoTexto(),
        stat4Icon = mapa["stat4Icon"].comoTexto(),
        stat4Texto = mapa["stat4Texto"].comoTexto()
    )
}

private fun DocumentSnapshot.aLugarSeguro(): Lugar? {
    if (!exists()) return null
    val datos = data ?: return null
    val categoriaCruda = datos["categoria"].comoTexto().ifBlank { CategoriaLugar.TODOS.name }
    val coordenada = (datos["ubicacionCoordenadas"] ?: datos["coordenadas"]).comoCoordenada()
        ?: run {
            val lat = datos["latitud"].comoDouble()
            val lng = datos["longitud"].comoDouble()
            if (lat != null && lng != null) Coordenada(lat, lng) else Coordenada()
        }

    return Lugar(
        id = id,
        nombre = datos["nombre"].comoTexto(),
        subtitulo = datos["subtitulo"].comoTexto(),
        categoria = categoriaCruda,
        ubicacionCoordenadas = coordenada,
        ubicacionCorta = datos["ubicacionCorta"].comoTexto(),
        ubicacionLarga = datos["ubicacionLarga"].comoTexto(),
        elevacion = datos["elevacion"].comoTexto(),
        clima = datos["clima"].comoTexto(),
        calificacion = datos["calificacion"].comoDouble() ?: 0.0,
        cantidadResenas = (datos["cantidadResenas"] as? Number)?.toInt() ?: 0,
        historia = datos["historia"].comoTexto(),
        imagenPortadaUrl = datos["imagenPortadaUrl"].comoTexto(),
        mejorTemporadaTitulo = datos["mejorTemporadaTitulo"].comoTexto(),
        mejorTemporadaDesc = datos["mejorTemporadaDesc"].comoTexto(),
        atractivos = datos["atractivos"].comoAtractivos(),
        directorioStats = datos["directorioStats"].comoDirectorioStats()
    )
}

actual object LugaresRepository {

    actual suspend fun obtenerTodos(): List<Lugar> = suspendCancellableCoroutine { continuation ->
        Firebase.firestore.collection("lugares")
            .get()
            .addOnSuccessListener { snapshot ->
                if (continuation.isActive) {
                    val lugares = snapshot.documents.mapNotNull { document ->
                        try {
                            document.aLugarSeguro()
                        } catch (_: Exception) {
                            null
                        }
                    }
                    continuation.resume(lugares)
                }
            }
            .addOnFailureListener { error ->
                if (continuation.isActive) {
                    continuation.resumeWithException(error)
                }
            }
    }

    actual suspend fun obtenerPorId(id: String): Lugar? = suspendCancellableCoroutine { continuation ->
        Firebase.firestore.collection("lugares").document(id)
            .get()
            .addOnSuccessListener { document ->
                if (continuation.isActive) {
                    val lugar = try {
                        document.aLugarSeguro()
                    } catch (_: Exception) {
                        null
                    }
                    continuation.resume(lugar)
                }
            }
            .addOnFailureListener { error ->
                if (continuation.isActive) {
                    continuation.resumeWithException(error)
                }
            }
    }
}
