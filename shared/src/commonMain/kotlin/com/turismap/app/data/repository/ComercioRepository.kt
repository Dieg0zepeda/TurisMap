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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.sync.withLock
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
    val nivelPlan: Int? = null, // 1 = Básico, 2 = Socio, 3 = Premium
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
        get() {
            val nivelExplicito = nivelPlan?.takeIf { it in 1..3 }
            if (nivelExplicito != null) return nivelExplicito

            val idExacto = planId?.trim()?.lowercase().orEmpty()
            val porPlanId = when (idExacto) {
                "presencia_basica" -> 1
                "socio_turismap" -> 2
                "socio_premium" -> 3
                else -> null
            }
            if (porPlanId != null) return porPlanId

            val clave = planClave
            return when {
                clave == "socio_premium" || clave.contains("premium") -> 3
                clave == "socio_turismap" || clave.contains("socio") -> 2
                else -> 1
            }
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

@Serializable
private data class ComercioFirestoreDto(
    val id: String? = null,
    val propietarioUid: String? = null,
    val lugarId: String? = null,
    val nombre: String? = null,
    val descripcion: String? = null,
    val categoria: String? = null,
    val direccion: String? = null,
    val telefono: String? = null,
    val sitioWeb: String? = null,
    val horario: String? = null,
    val precioDesde: Double? = null,
    val servicios: List<String?>? = null,
    val imagenUrls: List<String?>? = null,
    val estado: String? = null,
    val destacado: Boolean? = null,
    val nivelPlan: Int? = null,
    val planId: String? = null,
    val plan: String? = null,
    val tipoPlan: String? = null,
    @SerialName("calificacionPromedio") val calificacionPromedio: Double? = null,
    @SerialName("cantidadResenas") val cantidadResenas: Int? = null,
    val creadoEn: Timestamp? = null,
    val actualizadoEn: Timestamp? = null
)

@Serializable
private data class PlanComercioFirestoreDto(
    val nombre: String? = null,
    val descripcion: String? = null,
    val beneficios: List<String?>? = null,
    val precio: Double? = null,
    val moneda: String? = null,
    val periodicidad: String? = null,
    val orden: Int? = null,
    val activo: Boolean? = null,
    val id: String? = null
) {
    fun aPlanComercio(documentId: String): PlanComercio = PlanComercio(
        nombre = nombre.orEmpty(),
        descripcion = descripcion.orEmpty(),
        beneficios = beneficios.orEmpty().mapNotNull { it?.trim()?.takeIf(String::isNotEmpty) },
        precio = precio?.takeIf(Double::isFinite),
        moneda = moneda.orEmpty(),
        periodicidad = periodicidad.orEmpty(),
        orden = orden ?: 0,
        activo = activo ?: true,
        id = id,
        documentId = documentId
    )
}

@Serializable
private data class CategoriaComercioFirestoreDto(
    @SerialName("id") val id: String? = null,
    val nombre: String? = null,
    val descripcion: String? = null,
    val orden: Int? = null,
    val activo: Boolean? = null
) {
    fun aCategoriaComercio(documentId: String): CategoriaComercio = CategoriaComercio(
        id = documentId,
        nombre = nombre.orEmpty(),
        descripcion = descripcion.orEmpty(),
        orden = orden ?: 0,
        activo = activo ?: true
    )
}

@Serializable
private data class ResenaFirestoreDto(
    val tipoDestino: String? = null,
    val destinoId: String? = null,
    val usuarioUid: String? = null,
    val calificacion: Int? = null,
    val comentario: String? = null,
    val creadoEn: Timestamp? = null,
    val actualizadoEn: Timestamp? = null
) {
    fun aResena(): Resena = Resena(
        tipoDestino = tipoDestino.orEmpty().ifBlank { "comercio" },
        destinoId = destinoId.orEmpty(),
        usuarioUid = usuarioUid.orEmpty(),
        calificacion = calificacion ?: 0,
        comentario = comentario.orEmpty(),
        creadoEn = creadoEn,
        actualizadoEn = actualizadoEn
    )
}

sealed class EstadoPublicados {
    data object Cargando : EstadoPublicados()
    data class Exito(val comercios: List<Comercio>) : EstadoPublicados()
    data class Error(
        val causa: Throwable,
        val comerciosEnCache: List<Comercio>? = null
    ) : EstadoPublicados()
}

object ComercioRepository {
    private val firestore by lazy { Firebase.firestore }
    private val scopeObservacion by lazy {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
    private val recargaPublicadosTrigger = MutableStateFlow(0)

    val publicadosEnCache: List<Comercio>?
        get() = ComercioCacheSesion.obtenerPublicados()

    val destacadosEnCache: List<Comercio>?
        get() = ComercioCacheSesion.obtenerDestacados()

    val categoriasEnCache: List<CategoriaComercio>?
        get() = ComercioCacheSesion.obtenerCategorias()

    fun obtenerEnCachePorId(id: String): Comercio? = ComercioCacheSesion.obtenerPorId(id)

    /**
     * Flujo único compartido de comercios publicados ligado a un único listener de Firestore.
     * Mantiene la suscripción viva 30 s tras quedar sin colectores y preserva el último valor en memoria,
     * evitando cerrar/reabrir listeners ni mostrar shimmers al cambiar entre Guía, Mapa y Perfil.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val publicadosStateFlow: StateFlow<EstadoPublicados> by lazy {
        recargaPublicadosTrigger
            .flatMapLatest {
                firestore.collection("comercios")
                    .where { "estado" equalTo "publicado" }
                    .snapshots
                    .map { snapshot ->
                        val lista = snapshot.documents.mapNotNull { documento ->
                            documento.aComercioSeguro()
                        }
                        ComercioCacheSesion.guardarPublicados(lista)
                        EstadoPublicados.Exito(lista) as EstadoPublicados
                    }
                    .catch { error ->
                        if (error is CancellationException) throw error
                        emit(
                            EstadoPublicados.Error(
                                causa = error,
                                comerciosEnCache = ComercioCacheSesion.obtenerPublicados()
                            )
                        )
                    }
            }
            .stateIn(
                scope = scopeObservacion,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 30_000L),
                initialValue = ComercioCacheSesion.obtenerPublicados()
                    ?.let { EstadoPublicados.Exito(it) }
                    ?: EstadoPublicados.Cargando
            )
    }

    fun reintentarObservacionPublicados() {
        recargaPublicadosTrigger.value += 1
    }

    private fun esErrorIndiceFaltante(error: Throwable): Boolean {
        val mensajeCompleto = generateSequence(error as Throwable?) { it.cause }
            .mapNotNull(Throwable::message)
            .joinToString(" ")
        return mensajeCompleto.contains("FAILED_PRECONDITION", ignoreCase = true) ||
            mensajeCompleto.contains("requires an index", ignoreCase = true) ||
            mensajeCompleto.contains("index", ignoreCase = true)
    }

    suspend fun obtenerPlanesDisponibles(): List<PlanComercio> =
        firestore.collection("planes_comercio")
            .where { "activo" equalTo true }
            .get()
            .documents
            .mapNotNull { documento ->
                try {
                    documento.data(PlanComercioFirestoreDto.serializer()).aPlanComercio(documento.id)
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    null
                }
            }
            .sortedBy(PlanComercio::orden)

    suspend fun obtenerPlanDisponible(id: String): PlanComercio? {
        val documento = firestore.collection("planes_comercio").document(id.trim()).get()
        if (!documento.exists) return null
        return try {
            documento.data(PlanComercioFirestoreDto.serializer())
                .aPlanComercio(documento.id)
                .takeIf(PlanComercio::activo)
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            null
        }
    }

    suspend fun obtenerCategorias(forzarRecarga: Boolean = false): List<CategoriaComercio> {
        if (!forzarRecarga) {
            ComercioCacheSesion.obtenerCategorias()?.let { return it }
        }
        return ComercioCacheSesion.mutexCategorias.withLock {
            if (!forzarRecarga) {
                ComercioCacheSesion.obtenerCategorias()?.let { return@withLock it }
            }
            val categorias = firestore.collection("categorias_comercio")
                .where { "activo" equalTo true }
                .get()
                .documents
                .mapNotNull { documento ->
                    try {
                        documento.data(CategoriaComercioFirestoreDto.serializer()).aCategoriaComercio(documento.id)
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Exception) {
                        null
                    }
                }
                .filter { it.etiqueta.isNotBlank() }
                .sortedBy(CategoriaComercio::orden)
            ComercioCacheSesion.guardarCategorias(categorias)
            categorias
        }
    }

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

    private fun DocumentSnapshot.aComercio(): Comercio {
        val dto = data(ComercioFirestoreDto.serializer())
        return Comercio(
            id = id,
            propietarioUid = dto.propietarioUid.orEmpty(),
            lugarId = dto.lugarId.orEmpty(),
            nombre = dto.nombre.orEmpty(),
            descripcion = dto.descripcion.orEmpty(),
            categoria = dto.categoria.orEmpty(),
            direccion = dto.direccion.orEmpty(),
            telefono = dto.telefono.orEmpty(),
            sitioWeb = dto.sitioWeb.orEmpty(),
            horario = dto.horario.orEmpty(),
            precioDesde = dto.precioDesde?.takeIf(Double::isFinite),
            servicios = dto.servicios.orEmpty().mapNotNull { it?.trim()?.takeIf(String::isNotEmpty) },
            imagenUrls = dto.imagenUrls.orEmpty().mapNotNull { it?.trim()?.takeIf(String::isNotEmpty) },
            estado = dto.estado.orEmpty().ifBlank { "pendiente" },
            destacado = dto.destacado ?: false,
            nivelPlan = dto.nivelPlan,
            planId = dto.planId,
            plan = dto.plan,
            tipoPlan = dto.tipoPlan,
            calificacionPromedio = dto.calificacionPromedio?.takeIf(Double::isFinite) ?: 0.0,
            cantidadResenas = dto.cantidadResenas ?: 0,
            creadoEn = dto.creadoEn,
            actualizadoEn = dto.actualizadoEn,
            municipio = leerCampoSeguro<String>("municipio").orEmpty().trim(),
            coordenadas = extraerCoordenadaSegura("coordenadas"),
            ubicacionCoordenadas = extraerCoordenadaSegura("ubicacionCoordenadas"),
            latitud = leerDoubleSeguro("latitud"),
            longitud = leerDoubleSeguro("longitud")
        )
    }

    private fun DocumentSnapshot.aComercioSeguro(): Comercio? =
        try {
            aComercio()
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            null
        }

    fun observarPublicados(): Flow<List<Comercio>> =
        publicadosStateFlow.transform { estado ->
            when (estado) {
                is EstadoPublicados.Cargando -> Unit
                is EstadoPublicados.Exito -> emit(estado.comercios)
                is EstadoPublicados.Error -> {
                    val cache = estado.comerciosEnCache
                    if (cache != null) emit(cache) else throw estado.causa
                }
            }
        }

    suspend fun obtenerPublicados(forzarRecarga: Boolean = false): List<Comercio> {
        if (!forzarRecarga) {
            ComercioCacheSesion.obtenerPublicados()?.let { return it }
        }
        return ComercioCacheSesion.mutexPublicados.withLock {
            if (!forzarRecarga) {
                ComercioCacheSesion.obtenerPublicados()?.let { return@withLock it }
            }
            val publicados = firestore.collection("comercios")
                .where { "estado" equalTo "publicado" }
                .get()
                .documents
                .mapNotNull { documento -> documento.aComercioSeguro() }
            ComercioCacheSesion.guardarPublicados(publicados)
            publicados
        }
    }

    suspend fun obtenerPublicadosPorLugar(
        lugarId: String,
        nombreLugar: String = ""
    ): List<Comercio> {
        val idLimpio = lugarId.trim()
        val nombreLimpio = nombreLugar.trim()
        if (idLimpio.isEmpty() && nombreLimpio.isEmpty()) return emptyList()

        ComercioCacheSesion.obtenerPublicados()?.let { return it }

        return try {
            val acumulados = linkedMapOf<String, Comercio>()
            if (idLimpio.isNotEmpty()) {
                firestore.collection("comercios")
                    .where { "estado" equalTo "publicado" }
                    .where { "lugarId" equalTo idLimpio }
                    .get()
                    .documents
                    .mapNotNull { it.aComercioSeguro() }
                    .forEach { acumulados[it.id] = it }
            }
            if (nombreLimpio.isNotEmpty()) {
                firestore.collection("comercios")
                    .where { "estado" equalTo "publicado" }
                    .where { "municipio" equalTo nombreLimpio }
                    .get()
                    .documents
                    .mapNotNull { it.aComercioSeguro() }
                    .forEach { acumulados[it.id] = it }
            }
            acumulados.values.toList()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            if (esErrorIndiceFaltante(error)) {
                obtenerPublicados()
            } else {
                throw error
            }
        }
    }

    suspend fun obtenerDestacados(forzarRecarga: Boolean = false): List<Comercio> {
        if (!forzarRecarga) {
            ComercioCacheSesion.obtenerDestacados()?.let { return it }
        }
        return ComercioCacheSesion.mutexDestacados.withLock {
            if (!forzarRecarga) {
                ComercioCacheSesion.obtenerDestacados()?.let { return@withLock it }
            }
            val documentos = try {
                firestore.collection("comercios")
                    .where { "estado" equalTo "publicado" }
                    .where { "destacado" equalTo true }
                    .get()
                    .documents
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                if (esErrorIndiceFaltante(error)) {
                    val publicados = obtenerPublicados(forzarRecarga = true)
                    return@withLock publicados.filter { it.destacado }
                } else {
                    throw error
                }
            }
            val destacados = documentos
                .mapNotNull { documento -> documento.aComercioSeguro() }
                .filter { it.destacado }
            ComercioCacheSesion.guardarDestacados(destacados)
            destacados
        }
    }

    suspend fun obtenerPorId(id: String): Comercio? {
        val documento = firestore.collection("comercios").document(id.trim()).get()
        val comercio = if (documento.exists) documento.aComercioSeguro() else null
        if (comercio != null) {
            ComercioCacheSesion.guardarComercio(comercio)
        }
        return comercio
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
        val planIdLimpio = planId.trim()
        require(planIdLimpio.isNotBlank()) { getString(Res.string.repo_error_plan_requerido) }
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
                "planId" to planIdLimpio,
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
            .where { "tipoDestino" equalTo "comercio" }
            .where { "destinoId" equalTo comercioId }
            .get()
            .documents
            .mapNotNull { documento ->
                try {
                    documento.data(ResenaFirestoreDto.serializer()).aResena()
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    null
                }
            }

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