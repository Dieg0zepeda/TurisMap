package com.turismap.app.data.repository

import com.turismap.app.data.models.CategoriaComercio
import com.turismap.app.data.models.Coordenada
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.GeoPoint
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.compose.resources.getString
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.repo_error_calificacion_rango
import turismap.shared.generated.resources.repo_error_categoria_requerida
import turismap.shared.generated.resources.repo_error_correo_no_coincide
import turismap.shared.generated.resources.repo_error_descripcion_longitud
import turismap.shared.generated.resources.repo_error_direccion_longitud
import turismap.shared.generated.resources.repo_error_latitud_rango
import turismap.shared.generated.resources.repo_error_longitud_rango
import turismap.shared.generated.resources.repo_error_nombre_completo_longitud
import turismap.shared.generated.resources.repo_error_nombre_negocio_longitud
import turismap.shared.generated.resources.repo_error_plan_requerido
import turismap.shared.generated.resources.repo_error_sesion_enviar_solicitud
import turismap.shared.generated.resources.repo_error_sesion_publicar_resena
import turismap.shared.generated.resources.repo_error_sesion_registrar_comercio
import turismap.shared.generated.resources.repo_error_sesion_sin_correo
import turismap.shared.generated.resources.repo_error_telefono_longitud
import turismap.shared.generated.resources.repo_error_ubicacion_longitud
import kotlin.time.Clock
import kotlin.uuid.Uuid

internal fun esCoordenadaValida(coordenada: Coordenada): Boolean =
    coordenada.latitud.isFinite() &&
        coordenada.longitud.isFinite() &&
        coordenada.latitud in -90.0..90.0 &&
        coordenada.longitud in -180.0..180.0 &&
        coordenada.latitud != 0.0 &&
        coordenada.longitud != 0.0

internal fun parsearCoordenadaDesdeTexto(texto: String): Coordenada? {
    val limpio = texto.trim()
    if (limpio.isEmpty()) return null
    val partes = when {
        limpio.contains(",") -> limpio.split(",")
        limpio.contains(";") -> limpio.split(";")
        else -> return null
    }
    if (partes.size != 2) return null
    val lat = partes[0].trim().toDoubleOrNull() ?: return null
    val lng = partes[1].trim().toDoubleOrNull() ?: return null
    return Coordenada(latitud = lat, longitud = lng).takeIf(::esCoordenadaValida)
}

@Serializable
private data class CoordenadaMapaNumerico(
    val latitud: Double? = null,
    val longitud: Double? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
private data class CoordenadaMapaTexto(
    val latitud: String? = null,
    val longitud: String? = null,
    val lat: String? = null,
    val lng: String? = null,
    val latitude: String? = null,
    val longitude: String? = null
)

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
    val actualizadoEn: Timestamp? = null,
    @Transient val municipio: String = "",
    @Transient val coordenadas: Coordenada? = null,
    @Transient val ubicacionCoordenadas: Coordenada? = null,
    @Transient val latitud: Double? = null,
    @Transient val longitud: Double? = null
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

    fun coordenadaPropiaValida(): Coordenada? =
        listOfNotNull(
            coordenadas,
            ubicacionCoordenadas,
            if (latitud != null && longitud != null) Coordenada(latitud, longitud) else null
        ).firstOrNull(::esCoordenadaValida)
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

    private inline fun <reified T : Any> DocumentSnapshot.leerCampoSeguro(campo: String): T? =
        try {
            get<T>(campo)
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            null
        }

    private fun DocumentSnapshot.leerDoubleSeguro(campo: String): Double? =
        leerCampoSeguro<Double>(campo)?.takeIf(Double::isFinite)
            ?: leerCampoSeguro<Long>(campo)?.toDouble()
            ?: leerCampoSeguro<Int>(campo)?.toDouble()
            ?: leerCampoSeguro<String>(campo)?.trim()?.replace(",", ".")?.toDoubleOrNull()?.takeIf(Double::isFinite)

    private fun DocumentSnapshot.extraerCoordenadaSegura(campo: String): Coordenada? {
        leerCampoSeguro<CoordenadaMapaNumerico>(campo)?.let { mapa ->
            val lat = mapa.latitud ?: mapa.lat ?: mapa.latitude
            val lng = mapa.longitud ?: mapa.lng ?: mapa.longitude
            if (lat != null && lng != null) {
                Coordenada(latitud = lat, longitud = lng).takeIf(::esCoordenadaValida)?.let { return it }
            }
        }
        leerCampoSeguro<GeoPoint>(campo)?.let { punto ->
            Coordenada(latitud = punto.latitude, longitud = punto.longitude)
                .takeIf(::esCoordenadaValida)
                ?.let { return it }
        }
        leerCampoSeguro<CoordenadaMapaTexto>(campo)?.let { mapa ->
            val lat = (mapa.latitud ?: mapa.lat ?: mapa.latitude)?.trim()?.replace(",", ".")?.toDoubleOrNull()
            val lng = (mapa.longitud ?: mapa.lng ?: mapa.longitude)?.trim()?.replace(",", ".")?.toDoubleOrNull()
            if (lat != null && lng != null) {
                Coordenada(latitud = lat, longitud = lng).takeIf(::esCoordenadaValida)?.let { return it }
            }
        }
        leerCampoSeguro<String>(campo)?.let { texto ->
            parsearCoordenadaDesdeTexto(texto)?.let { return it }
        }
        return null
    }

    private fun DocumentSnapshot.aComercio(): Comercio =
        data(Comercio.serializer()).copy(
            id = id,
            municipio = leerCampoSeguro<String>("municipio").orEmpty().trim(),
            coordenadas = extraerCoordenadaSegura("coordenadas"),
            ubicacionCoordenadas = extraerCoordenadaSegura("ubicacionCoordenadas"),
            latitud = leerDoubleSeguro("latitud"),
            longitud = leerDoubleSeguro("longitud")
        )

    fun observarPublicados(): Flow<List<Comercio>> =
        firestore.collection("comercios")
            .where { "estado" equalTo "publicado" }
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { documento ->
                    try {
                        documento.aComercio()
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Exception) {
                        null
                    }
                }
            }

    suspend fun obtenerPublicados(): List<Comercio> =
        firestore.collection("comercios")
            .where { "estado" equalTo "publicado" }
            .get()
            .documents
            .mapNotNull { documento ->
                try {
                    documento.aComercio()
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    null
                }
            }

    suspend fun obtenerDestacados(): List<Comercio> =
        firestore.collection("comercios")
            .where { "estado" equalTo "publicado" }
            .get()
            .documents
            .map { documento -> documento.aComercio() }
            .filter { it.destacado }

    suspend fun obtenerPorId(id: String): Comercio? {
        val documento = firestore.collection("comercios").document(id).get()
        return if (documento.exists) documento.aComercio() else null
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
            ?: throw IllegalStateException(getString(Res.string.repo_error_sesion_registrar_comercio))
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
            ?: throw IllegalStateException(getString(Res.string.repo_error_sesion_enviar_solicitud))
        val correoAutenticado = usuario.email?.trim()
            ?.takeIf(String::isNotBlank)
            ?: throw IllegalStateException(getString(Res.string.repo_error_sesion_sin_correo))
        require(email.trim().equals(correoAutenticado, ignoreCase = true)) {
            getString(Res.string.repo_error_correo_no_coincide)
        }
        require(planId.isNotBlank()) { getString(Res.string.repo_error_plan_requerido) }
        require(nombreCompleto.trim().length in 1..120) {
            getString(Res.string.repo_error_nombre_completo_longitud)
        }
        require(municipio.trim().length in 1..100) {
            getString(Res.string.repo_error_ubicacion_longitud)
        }
        require(nombreNegocio.trim().length in 1..120) {
            getString(Res.string.repo_error_nombre_negocio_longitud)
        }
        require(categoria.isNotBlank()) { getString(Res.string.repo_error_categoria_requerida) }
        require(descripcion.trim().length in 1..2000) {
            getString(Res.string.repo_error_descripcion_longitud)
        }
        require(direccion.trim().length in 1..300) {
            getString(Res.string.repo_error_direccion_longitud)
        }
        require(telefono.trim().length in 1..40) {
            getString(Res.string.repo_error_telefono_longitud)
        }
        require(coordenadas.latitud.isFinite() && coordenadas.latitud in -90.0..90.0) {
            getString(Res.string.repo_error_latitud_rango)
        }
        require(coordenadas.longitud.isFinite() && coordenadas.longitud in -180.0..180.0) {
            getString(Res.string.repo_error_longitud_rango)
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
            ?: throw IllegalStateException(getString(Res.string.repo_error_sesion_publicar_resena))
        require(calificacion in 1..5) { getString(Res.string.repo_error_calificacion_rango) }
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