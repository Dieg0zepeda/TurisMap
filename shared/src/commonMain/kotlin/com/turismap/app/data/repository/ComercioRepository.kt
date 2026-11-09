package com.turismap.app.data.repository

import com.turismap.app.data.models.CategoriaComercio
import com.turismap.app.data.models.Coordenada
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.where
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
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
    val nivelPlan: Int = 1, // 1 = Básico, 2 = Socio, 3 = Premium
    val planId: String? = null,
    val plan: String? = null,
    val tipoPlan: String? = null,
    @SerialName("calificacionPromedio") val calificacionPromedio: Double? = 0.0,
    @SerialName("cantidadResenas") val cantidadResenas: Int? = 0,
    val creadoEn: Timestamp? = null,
    val actualizadoEn: Timestamp? = null
) {
    // Identificador unificado tolerante a cualquier nombre de campo en Firestore
    val planClave: String
        get() = listOfNotNull(planId, plan, tipoPlan).joinToString(" ").lowercase().trim()

    // 1 = Básico, 2 = Socio, 3 = Premium
    val planInt: Int
        get() = when {
            planClave.contains("premium") || planClave.contains("25") || nivelPlan == 3 -> 3
            planClave.contains("socio") || planClave.contains("turismap") || planClave.contains("15") || nivelPlan == 2 -> 2
            else -> 1
        }

    val esNivelPremium: Boolean
        get() = planInt == 3

    val esNivelSocio: Boolean
        get() = planInt == 2

    val esNivelBasico: Boolean
        get() = planInt == 1

    // Límite de fotografías por suscripción: 2 (Básico), 4 (Socio), 8 (Premium)
    val maxFotosPermitidas: Int
        get() = when (planInt) {
            3 -> 8
            2 -> 4
            else -> 2
        }
}

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
    val precio: Double? = null,
    val moneda: String = "",
    val periodicidad: String = "",
    val orden: Int = 0,
    val activo: Boolean = true,
    val id: String? = null,
    @Transient val documentId: String = ""
) {
    fun precioFormateado(): String? {
        val valor = precio?.takeIf { it.isFinite() && it >= 0.0 } ?: return null
        val valorTexto = valor.toString()
        val precioTexto = if (valorTexto.endsWith(".0")) valorTexto.dropLast(2) else valorTexto
        val importe = listOf(precioTexto, moneda.trim())
            .filter(String::isNotBlank)
            .joinToString(" ")
        return listOf(importe, periodicidad.trim())
            .filter(String::isNotBlank)
            .joinToString(" / ")
            .ifBlank { null }
    }
}

object ComercioRepository {
    private val firestore by lazy { Firebase.firestore }

    suspend fun obtenerPlanesDisponibles(): List<PlanComercio> =
        firestore.collection("planes_comercio")
            .where { "activo" equalTo true }
            .get()
            .documents
            .map { documento ->
                documento.data(PlanComercio.serializer()).copy(documentId = documento.id)
            }
            .sortedBy(PlanComercio::orden)

    suspend fun obtenerPlanDisponible(id: String): PlanComercio? {
        val documento = firestore.collection("planes_comercio").document(id).get()
        return if (documento.exists) {
            documento.data(PlanComercio.serializer()).copy(documentId = documento.id)
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
        planId: String,
        coordenadas: Coordenada
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
        require(coordenadas.latitud.isFinite() && coordenadas.latitud in -90.0..90.0) {
            "La latitud debe estar entre -90 y 90."
        }
        require(coordenadas.longitud.isFinite() && coordenadas.longitud in -180.0..180.0) {
            "La longitud debe estar entre -180 y 180."
        }

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
                "coordenadas" to mapOf(
                    "latitud" to coordenadas.latitud,
                    "longitud" to coordenadas.longitud
                ),
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