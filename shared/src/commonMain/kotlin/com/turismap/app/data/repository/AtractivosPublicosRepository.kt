package com.turismap.app.data.repository

import com.turismap.app.data.models.AtractivoPublico
import com.turismap.app.data.models.CategoriaAtractivoPublico
import com.turismap.app.data.models.Coordenada
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.GeoPoint
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable

@Serializable
private data class AtractivoCoordenadaMapaNumerica(
    val latitud: Double? = null,
    val longitud: Double? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
private data class AtractivoCoordenadaMapaTexto(
    val latitud: String? = null,
    val longitud: String? = null,
    val lat: String? = null,
    val lng: String? = null,
    val latitude: String? = null,
    val longitude: String? = null
)

@Serializable
private data class AtractivoPublicoFirestoreDto(
    val nombre: String? = null,
    val categoria: String? = null,
    val descripcion: String? = null,
    val municipio: String? = null,
    val lugarId: String? = null,
    val activo: Boolean? = null
)

/**
 * Caché en memoria por sesión para la colección pública `atractivos_publicos`.
 */
internal object AtractivosPublicosCache {
    val mutex = Mutex()
    private val listaFlow = MutableStateFlow<List<AtractivoPublico>?>(null)
    private val porIdFlow = MutableStateFlow<Map<String, AtractivoPublico>>(emptyMap())
    private val usandoRespaldoFlow = MutableStateFlow(false)

    fun obtenerLista(): List<AtractivoPublico>? = listaFlow.value

    fun obtenerPorId(id: String): AtractivoPublico? = porIdFlow.value[id.trim()]

    fun usandoRespaldo(): Boolean = usandoRespaldoFlow.value

    fun guardarLista(atractivos: List<AtractivoPublico>, esRespaldo: Boolean) {
        val copia = atractivos.toList()
        listaFlow.value = copia
        porIdFlow.value = copia.associateBy { it.id.trim() }
        usandoRespaldoFlow.value = esRespaldo
    }

    fun guardarAtractivo(atractivo: AtractivoPublico) {
        val clave = atractivo.id.trim()
        if (clave.isEmpty()) return
        porIdFlow.value = porIdFlow.value + (clave to atractivo)
        val listaActual = listaFlow.value ?: return
        val indice = listaActual.indexOfFirst { it.id.trim() == clave }
        listaFlow.value = if (indice >= 0) {
            listaActual.toMutableList().apply { this[indice] = atractivo }
        } else {
            listaActual + atractivo
        }
    }

    fun limpiar() {
        listaFlow.value = null
        porIdFlow.value = emptyMap()
        usandoRespaldoFlow.value = false
    }
}

object AtractivosPublicosRepository {
    private const val COLECCION_ATRACTIVOS_PUBLICOS = "atractivos_publicos"
    private val firestore by lazy { Firebase.firestore }

    /**
     * Catálogo seed temporal de respaldo con 5 atractivos naturales reales de la Ruta de las Flores
     * (uno por cada categoría). Se usa ÚNICAMENTE cuando `atractivos_publicos` en Firestore está
     * vacía o cuando aún no se han desplegado los permisos de lectura para la colección.
     */
    val catalogoSeedRespaldo: List<AtractivoPublico> = listOf(
        AtractivoPublico(
            id = "seed_cordillera_apaneca",
            nombre = "Cordillera de Apaneca",
            categoria = CategoriaAtractivoPublico.MONTANA.claveFirestore,
            descripcion = "Cadena montañosa de bosque nebuloso y cafetales de altura que atraviesa el corazón de la Ruta de las Flores, ideal para caminatas entre brisa fresca y paisajes volcánicos.",
            imagenes = emptyList(),
            coordenadas = Coordenada(latitud = 13.8689, longitud = -89.8117),
            municipio = "Apaneca",
            lugarId = "apaneca",
            activo = true
        ),
        AtractivoPublico(
            id = "seed_mirador_la_cruz_ataco",
            nombre = "Mirador de La Cruz",
            categoria = CategoriaAtractivoPublico.MIRADOR.claveFirestore,
            descripcion = "Mirador público emblemático en la parte alta de Concepción de Ataco con vistas panorámicas hacia el casco urbano, cafetales y los valles de la cordillera.",
            imagenes = emptyList(),
            coordenadas = Coordenada(latitud = 13.8744, longitud = -89.8453),
            municipio = "Concepción de Ataco",
            lugarId = "ataco",
            activo = true
        ),
        AtractivoPublico(
            id = "seed_laguna_verde_apaneca",
            nombre = "Laguna Verde",
            categoria = CategoriaAtractivoPublico.LAGUNA.claveFirestore,
            descripcion = "Laguna cratérica de aguas verdosas ubicada a más de 1,600 metros de altitud, rodeada de cipreses, senderos naturales y neblina de montaña.",
            imagenes = emptyList(),
            coordenadas = Coordenada(latitud = 13.8922, longitud = -89.7875),
            municipio = "Apaneca",
            lugarId = "apaneca",
            activo = true
        ),
        AtractivoPublico(
            id = "seed_chorros_la_calera_juayua",
            nombre = "Río y Chorros de la Calera",
            categoria = CategoriaAtractivoPublico.RIO.claveFirestore,
            descripcion = "Cañón natural en Juayúa formado por aguas cristalinas de montaña, caídas de agua y pozas frescas accesibles por senderos rodeados de vegetación.",
            imagenes = emptyList(),
            coordenadas = Coordenada(latitud = 13.8547, longitud = -89.7381),
            municipio = "Juayúa",
            lugarId = "juayua",
            activo = true
        ),
        AtractivoPublico(
            id = "seed_cerro_el_aguila_juayua",
            nombre = "Cerro El Águila",
            categoria = CategoriaAtractivoPublico.CERRO.claveFirestore,
            descripcion = "Cumbre emblemática de más de 2,000 metros sobre el nivel del mar, muy valorada para senderismo entre bosques de pino, ciprés y miradores naturales.",
            imagenes = emptyList(),
            coordenadas = Coordenada(latitud = 13.8925, longitud = -89.6967),
            municipio = "Juayúa",
            lugarId = "juayua",
            activo = true
        )
    )

    val atractivosEnCache: List<AtractivoPublico>?
        get() = AtractivosPublicosCache.obtenerLista()

    val usandoCatalogoRespaldo: Boolean
        get() = AtractivosPublicosCache.usandoRespaldo()

    fun obtenerEnCachePorId(id: String): AtractivoPublico? =
        AtractivosPublicosCache.obtenerPorId(id)

    suspend fun obtenerActivos(forzarRecarga: Boolean = false): List<AtractivoPublico> {
        if (!forzarRecarga) {
            AtractivosPublicosCache.obtenerLista()?.let { return it }
        }
        return AtractivosPublicosCache.mutex.withLock {
            if (!forzarRecarga) {
                AtractivosPublicosCache.obtenerLista()?.let { return@withLock it }
            }
            try {
                val documentos = firestore.collection(COLECCION_ATRACTIVOS_PUBLICOS)
                    .where { "activo" equalTo true }
                    .get()
                    .documents

                val atractivos = documentos
                    .mapNotNull { documento -> documento.aAtractivoPublicoSeguro() }
                    .filter { it.activo && it.nombre.isNotBlank() }

                if (atractivos.isNotEmpty()) {
                    AtractivosPublicosCache.guardarLista(atractivos, esRespaldo = false)
                    atractivos
                } else {
                    AtractivosPublicosCache.guardarLista(catalogoSeedRespaldo, esRespaldo = true)
                    catalogoSeedRespaldo
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                if (esErrorPermisosFirestore(error)) {
                    AtractivosPublicosCache.guardarLista(catalogoSeedRespaldo, esRespaldo = true)
                    catalogoSeedRespaldo
                } else {
                    throw error
                }
            }
        }
    }

    suspend fun obtenerPorId(id: String, forzarRecarga: Boolean = false): AtractivoPublico? {
        val idLimpio = id.trim()
        if (idLimpio.isEmpty()) return null
        if (!forzarRecarga) {
            AtractivosPublicosCache.obtenerPorId(idLimpio)?.let { return it }
        }
        return try {
            val documento = firestore.collection(COLECCION_ATRACTIVOS_PUBLICOS)
                .document(idLimpio)
                .get()
            val atractivo = if (documento.exists) {
                documento.aAtractivoPublicoSeguro()?.takeIf { it.activo && it.nombre.isNotBlank() }
            } else {
                null
            }
            if (atractivo != null) {
                AtractivosPublicosCache.guardarAtractivo(atractivo)
                atractivo
            } else {
                catalogoSeedRespaldo.firstOrNull { it.id == idLimpio }
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            if (esErrorPermisosFirestore(error)) {
                catalogoSeedRespaldo.firstOrNull { it.id == idLimpio }
            } else {
                throw error
            }
        }
    }

    fun limpiarCache() {
        AtractivosPublicosCache.limpiar()
    }

    private fun esErrorPermisosFirestore(error: Throwable): Boolean {
        val mensajeCompleto = generateSequence(error as Throwable?) { it.cause }
            .mapNotNull(Throwable::message)
            .joinToString(" ")
        return mensajeCompleto.contains("PERMISSION_DENIED", ignoreCase = true) ||
            mensajeCompleto.contains("permission-denied", ignoreCase = true) ||
            mensajeCompleto.contains("Missing or insufficient permissions", ignoreCase = true)
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
            ?: leerCampoSeguro<String>(campo)
                ?.trim()
                ?.replace(",", ".")
                ?.toDoubleOrNull()
                ?.takeIf(Double::isFinite)

    private fun DocumentSnapshot.extraerCoordenadaSegura(campo: String): Coordenada? {
        leerCampoSeguro<AtractivoCoordenadaMapaNumerica>(campo)?.let { mapa ->
            val lat = mapa.latitud ?: mapa.lat ?: mapa.latitude
            val lng = mapa.longitud ?: mapa.lng ?: mapa.longitude
            if (lat != null && lng != null) {
                Coordenada(latitud = lat, longitud = lng)
                    .takeIf(::esCoordenadaValida)
                    ?.let { return it }
            }
        }
        leerCampoSeguro<GeoPoint>(campo)?.let { punto ->
            Coordenada(latitud = punto.latitude, longitud = punto.longitude)
                .takeIf(::esCoordenadaValida)
                ?.let { return it }
        }
        leerCampoSeguro<AtractivoCoordenadaMapaTexto>(campo)?.let { mapa ->
            val lat = (mapa.latitud ?: mapa.lat ?: mapa.latitude)
                ?.trim()
                ?.replace(",", ".")
                ?.toDoubleOrNull()
            val lng = (mapa.longitud ?: mapa.lng ?: mapa.longitude)
                ?.trim()
                ?.replace(",", ".")
                ?.toDoubleOrNull()
            if (lat != null && lng != null) {
                Coordenada(latitud = lat, longitud = lng)
                    .takeIf(::esCoordenadaValida)
                    ?.let { return it }
            }
        }
        leerCampoSeguro<String>(campo)?.let { texto ->
            parsearCoordenadaDesdeTexto(texto)?.let { return it }
        }
        return null
    }

    private fun DocumentSnapshot.leerListaTextosSegura(campo: String): List<String> {
        val lista = leerCampoSeguro<List<String?>>(campo)
            ?.mapNotNull { it?.trim()?.takeIf(String::isNotEmpty) }
        if (!lista.isNullOrEmpty()) return lista
        return leerCampoSeguro<String>(campo)
            ?.trim()
            ?.takeIf(String::isNotEmpty)
            ?.let(::listOf)
            .orEmpty()
    }

    private fun DocumentSnapshot.extraerImagenesSeguras(): List<String> {
        val imagenes = leerListaTextosSegura("imagenes")
        val imagenUrls = leerListaTextosSegura("imagenUrls")
        val imagenUrl = leerCampoSeguro<String>("imagenUrl")
            ?.trim()
            ?.takeIf(String::isNotEmpty)
        val imagenPortadaUrl = leerCampoSeguro<String>("imagenPortadaUrl")
            ?.trim()
            ?.takeIf(String::isNotEmpty)

        return buildList {
            addAll(imagenes)
            addAll(imagenUrls)
            if (imagenUrl != null) add(imagenUrl)
            if (imagenPortadaUrl != null) add(imagenPortadaUrl)
        }.distinct()
    }

    private fun DocumentSnapshot.aAtractivoPublico(): AtractivoPublico {
        val dto = data(AtractivoPublicoFirestoreDto.serializer())
        val coordenada = extraerCoordenadaSegura("coordenadas")
            ?: extraerCoordenadaSegura("ubicacionCoordenadas")
            ?: run {
                val lat = leerDoubleSeguro("latitud") ?: leerDoubleSeguro("lat")
                val lng = leerDoubleSeguro("longitud") ?: leerDoubleSeguro("lng")
                if (lat != null && lng != null) {
                    Coordenada(latitud = lat, longitud = lng).takeIf(::esCoordenadaValida)
                } else {
                    null
                }
            }
            ?: Coordenada()

        val categoriaNormalizada = CategoriaAtractivoPublico.desdeTexto(dto.categoria).claveFirestore

        return AtractivoPublico(
            id = id,
            nombre = dto.nombre.orEmpty().trim(),
            categoria = categoriaNormalizada,
            descripcion = dto.descripcion.orEmpty().trim(),
            imagenes = extraerImagenesSeguras(),
            coordenadas = coordenada,
            municipio = dto.municipio.orEmpty().trim(),
            lugarId = dto.lugarId.orEmpty().trim(),
            activo = dto.activo ?: true
        )
    }

    private fun DocumentSnapshot.aAtractivoPublicoSeguro(): AtractivoPublico? =
        try {
            aAtractivoPublico()
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            null
        }
}
