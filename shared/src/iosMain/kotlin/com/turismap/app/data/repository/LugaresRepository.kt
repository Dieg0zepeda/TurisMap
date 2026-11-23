package com.turismap.app.data.repository

import com.turismap.app.data.models.Atractivo
import com.turismap.app.data.models.CategoriaLugar
import com.turismap.app.data.models.Coordenada
import com.turismap.app.data.models.Lugar
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable

private val firestore by lazy { Firebase.firestore }

@Serializable
private data class CoordenadaFirestoreDto(
    val latitud: Double? = null,
    val longitud: Double? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    fun aCoordenada(): Coordenada? {
        val latFinal = (latitud ?: lat ?: latitude)?.takeIf(Double::isFinite) ?: return null
        val lngFinal = (longitud ?: lng ?: longitude)?.takeIf(Double::isFinite) ?: return null
        return Coordenada(latitud = latFinal, longitud = lngFinal)
    }
}

@Serializable
private data class AtractivoFirestoreDto(
    val id: String? = null,
    val etiqueta: String? = null,
    val etiquetaColorHex: Long? = null,
    val titulo: String? = null,
    val descripcion: String? = null,
    val accion: String? = null,
    val imagenUrl: String? = null,
    val ubicacionCoordenadas: CoordenadaFirestoreDto? = null,
    val coordenadas: CoordenadaFirestoreDto? = null
) {
    fun aAtractivo(): Atractivo = Atractivo(
        id = id.orEmpty(),
        etiqueta = etiqueta.orEmpty(),
        etiquetaColorHex = etiquetaColorHex ?: 0L,
        titulo = titulo.orEmpty(),
        descripcion = descripcion.orEmpty(),
        accion = accion.orEmpty(),
        imagenUrl = imagenUrl.orEmpty(),
        ubicacionCoordenadas = ubicacionCoordenadas?.aCoordenada() ?: coordenadas?.aCoordenada()
    )
}

@Serializable
private data class LugarFirestoreDto(
    val id: String? = null,
    val nombre: String? = null,
    val subtitulo: String? = null,
    val categoria: String? = null,
    val ubicacionCoordenadas: CoordenadaFirestoreDto? = null,
    val coordenadas: CoordenadaFirestoreDto? = null,
    val ubicacionCorta: String? = null,
    val ubicacionLarga: String? = null,
    val elevacion: String? = null,
    val clima: String? = null,
    val calificacion: Double? = null,
    val cantidadResenas: Int? = null,
    val historia: String? = null,
    val imagenPortadaUrl: String? = null,
    val mejorTemporadaTitulo: String? = null,
    val mejorTemporadaDesc: String? = null,
    val atractivos: List<AtractivoFirestoreDto?>? = null
) {
    fun aLugar(documentId: String): Lugar = Lugar(
        id = documentId,
        nombre = nombre.orEmpty(),
        subtitulo = subtitulo.orEmpty(),
        categoria = categoria?.takeIf(String::isNotBlank) ?: CategoriaLugar.TODOS.name,
        ubicacionCoordenadas = ubicacionCoordenadas?.aCoordenada()
            ?: coordenadas?.aCoordenada()
            ?: Coordenada(),
        ubicacionCorta = ubicacionCorta.orEmpty(),
        ubicacionLarga = ubicacionLarga.orEmpty(),
        elevacion = elevacion.orEmpty(),
        clima = clima.orEmpty(),
        calificacion = calificacion?.takeIf(Double::isFinite) ?: 0.0,
        cantidadResenas = cantidadResenas ?: 0,
        historia = historia.orEmpty(),
        imagenPortadaUrl = imagenPortadaUrl.orEmpty(),
        mejorTemporadaTitulo = mejorTemporadaTitulo.orEmpty(),
        mejorTemporadaDesc = mejorTemporadaDesc.orEmpty(),
        atractivos = atractivos.orEmpty().mapNotNull { it?.aAtractivo() }
    )
}

private fun DocumentSnapshot.aLugarSeguro(): Lugar? {
    if (!exists) return null
    return try {
        data(LugarFirestoreDto.serializer()).aLugar(id)
    } catch (error: CancellationException) {
        throw error
    } catch (_: Exception) {
        null
    }
}

actual object LugaresRepository {
    actual suspend fun obtenerTodos(): List<Lugar> {
        try {
            return firestore.collection("lugares").get().documents.mapNotNull { document ->
                document.aLugarSeguro()
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            throw IllegalStateException("No se pudieron cargar los lugares desde Firestore.", error)
        }
    }

    actual suspend fun obtenerPorId(id: String): Lugar? {
        try {
            val document = firestore.collection("lugares").document(id).get()
            return document.aLugarSeguro()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            throw IllegalStateException("No se pudo cargar el lugar '$id' desde Firestore.", error)
        }
    }
}
