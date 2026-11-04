package com.turismap.app.data.repository

import com.turismap.app.data.models.CategoriaComercio
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.where
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.uuid.Uuid

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

@Serializable
data class PlanComercio(
    val nombre: String = "",
    val descripcion: String = "",
    val beneficios: List<String> = emptyList(),
    val orden: Int = 0,
    val activo: Boolean = true,
    val id: String = ""
)

object ComercioRepository {
    private val firestore by lazy { Firebase.firestore }

    suspend fun obtenerPlanesDisponibles(): List<PlanComercio> =
        firestore.collection("planes_comercio")
            .where { "activo" equalTo true }
            .get()
            .documents
            .map { documento ->
                documento.data(PlanComercio.serializer()).copy(id = documento.id)
            }
            .sortedBy(PlanComercio::orden)

    suspend fun obtenerPlanDisponible(id: String): PlanComercio? {
        val documento = firestore.collection("planes_comercio").document(id).get()
        return if (documento.exists) {
            documento.data(PlanComercio.serializer()).copy(id = documento.id)
                .takeIf(PlanComercio::activo)
        } else {
            null
        }
    }

    suspend fun obtenerCategorias(): List<CategoriaComercio> =
        firestore.collection("categorias_comercio")
            .where { "activo" equalTo true }
            .get()
            .documents
            .map { documento ->
                documento.data(CategoriaComercio.serializer()).copy(id = documento.id)
            }
            .filter { it.etiqueta.isNotBlank() }
            .sortedBy(CategoriaComercio::orden)

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

    suspend fun enviarSolicitudComercio(
        nombreCompleto: String,
        email: String,
        municipio: String,
        nombreNegocio: String,
        categoria: String,
        descripcion: String,
        direccion: String,
        telefono: String,
        sitioWeb: String,
        horario: String,
        planId: String
    ) {
        val usuario = Firebase.auth.currentUser
            ?: throw IllegalStateException("Inicia sesión para enviar una solicitud.")
        val correoAutenticado = usuario.email?.trim()
            ?.takeIf(String::isNotBlank)
            ?: throw IllegalStateException("La sesión actual no tiene un correo electrónico asociado.")
        require(email.trim().equals(correoAutenticado, ignoreCase = true)) {
            "El correo debe coincidir con el de la sesión actual."
        }
        require(planId.isNotBlank()) { "Selecciona un plan disponible." }
        require(nombreCompleto.trim().length in 1..120) { "El nombre completo debe tener entre 1 y 120 caracteres." }
        require(municipio.trim().length in 1..100) { "La ubicación debe tener entre 1 y 100 caracteres." }
        require(nombreNegocio.trim().length in 1..120) { "El nombre del negocio debe tener entre 1 y 120 caracteres." }
        require(categoria.isNotBlank()) { "Selecciona una categoría disponible." }
        require(descripcion.trim().length in 1..2000) { "La descripción debe tener entre 1 y 2000 caracteres." }
        require(direccion.trim().length in 1..300) { "La dirección debe tener entre 1 y 300 caracteres." }
        require(telefono.trim().length in 1..40) { "El teléfono debe tener entre 1 y 40 caracteres." }

        val referencia = firestore.collection("solicitudes_comercio").document(Uuid.random().toString())
        referencia.set(
            mapOf(
                "id" to referencia.id,
                "propietarioUid" to usuario.uid,
                "nombreCompleto" to nombreCompleto.trim(),
                "email" to correoAutenticado,
                "municipio" to municipio.trim(),
                "nombreNegocio" to nombreNegocio.trim(),
                "categoria" to categoria.trim(),
                "descripcion" to descripcion.trim(),
                "direccion" to direccion.trim(),
                "telefono" to telefono.trim(),
                "sitioWeb" to sitioWeb.trim(),
                "horario" to horario.trim(),
                "planId" to planId,
                "estado" to "PENDIENTE",
                "fechaRegistro" to Clock.System.now().toString()
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
