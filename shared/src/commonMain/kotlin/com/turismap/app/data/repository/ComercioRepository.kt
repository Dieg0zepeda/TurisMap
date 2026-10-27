package com.turismap.app.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.where
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Comercio(
    val id: String = "",
    val propietarioUid: String = "",
    val lugarId: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val direccion: String = "",
    val telefono: String = "",
    val sitioWeb: String = "",
    val horario: String = "",
    val precioDesde: Double? = null,
    val servicios: List<String> = emptyList(),
    val imagenUrls: List<String> = emptyList(),
    val estado: String = "pendiente",
    val destacado: Boolean = false,
    @SerialName("calificacionPromedio") val calificacionPromedio: Double? = 0.0,
    @SerialName("cantidadResenas") val cantidadResenas: Int? = 0,
    val creadoEn: Timestamp? = null,
    val actualizadoEn: Timestamp? = null
)

@Serializable
data class Resena(
    val tipoDestino: String = "comercio",
    val destinoId: String = "",
    val usuarioUid: String = "",
    val calificacion: Int = 0,
    val comentario: String = "",
    val creadoEn: Timestamp? = null,
    val actualizadoEn: Timestamp? = null
)

object ComercioRepository {
    private val firestore by lazy { Firebase.firestore }

    suspend fun obtenerPublicados(): List<Comercio> =
        firestore.collection("comercios")
            .where { "estado" equalTo "publicado" }
            .get()
            .documents
            .map { documento ->
                documento.data(Comercio.serializer()).copy(id = documento.id)
            }

    suspend fun obtenerDestacados(): List<Comercio> =
        firestore.collection("comercios")
            .where { "estado" equalTo "publicado" }
            .get()
            .documents
            .map { documento ->
                documento.data(Comercio.serializer()).copy(id = documento.id)
            }
            .filter { it.destacado }

    suspend fun obtenerPorId(id: String): Comercio? {
        val documento = firestore.collection("comercios").document(id).get()
        return if (documento.exists) documento.data(Comercio.serializer()).copy(id = documento.id) else null
    }

    suspend fun enviarSolicitud(
        lugarId: String,
        nombre: String,
        descripcion: String,
        categoria: String,
        direccion: String,
        telefono: String,
        sitioWeb: String,
        horario: String,
        precioDesde: Double?,
        servicios: List<String>,
        imagenUrls: List<String>
    ) {
        val uid = Firebase.auth.currentUser?.uid
            ?: throw IllegalStateException("Inicia sesión para registrar un comercio.")
        val ahoraServidor = FieldValue.serverTimestamp
        firestore.collection("comercios").add(
            mapOf(
                "propietarioUid" to uid,
                "lugarId" to lugarId,
                "nombre" to nombre,
                "descripcion" to descripcion,
                "categoria" to categoria,
                "direccion" to direccion,
                "telefono" to telefono,
                "sitioWeb" to sitioWeb,
                "horario" to horario,
                "precioDesde" to precioDesde,
                "servicios" to servicios,
                "imagenUrls" to imagenUrls,
                "calificacionPromedio" to 0.0,
                "cantidadResenas" to 0,
                "destacado" to false,
                "estado" to "pendiente",
                "creadoEn" to ahoraServidor,
                "actualizadoEn" to ahoraServidor
            )
        )
    }

    suspend fun obtenerResenas(comercioId: String): List<Resena> =
        firestore.collection("resenas")
            .where {
                "tipoDestino" equalTo "comercio"
                "destinoId" equalTo comercioId
            }
            .get()
            .documents
            .map { it.data(Resena.serializer()) }

    suspend fun enviarResena(comercioId: String, calificacion: Int, comentario: String) {
        val uid = Firebase.auth.currentUser?.uid
            ?: throw IllegalStateException("Inicia sesión para publicar una reseña.")
        require(calificacion in 1..5) { "La calificación debe estar entre 1 y 5." }
        val referencia = firestore.collection("resenas").document("comercio_${comercioId}_$uid")
        val existente = referencia.get()
        val ahoraServidor = FieldValue.serverTimestamp

        if (existente.exists) {
            referencia.update(
                "calificacion" to calificacion,
                "comentario" to comentario.trim(),
                "actualizadoEn" to ahoraServidor
            )
        } else {
            referencia.set(
                mapOf(
                    "tipoDestino" to "comercio",
                    "destinoId" to comercioId,
                    "usuarioUid" to uid,
                    "calificacion" to calificacion,
                    "comentario" to comentario.trim(),
                    "creadoEn" to ahoraServidor,
                    "actualizadoEn" to ahoraServidor
                )
            )
        }
    }
}
