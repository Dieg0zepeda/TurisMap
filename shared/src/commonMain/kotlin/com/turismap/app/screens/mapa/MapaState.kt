package com.turismap.app.screens.mapa

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.turismap.app.data.models.CategoriaLugar
import com.turismap.app.data.models.Coordenada
import com.turismap.app.data.models.Lugar
import com.turismap.app.data.repository.Comercio
import com.turismap.app.data.repository.ComercioRepository
import com.turismap.app.data.repository.LugaresRepository
import com.turismap.app.data.repository.esCoordenadaValida
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin

// Paleta corporativa TurisMap
internal data class MapaPaleta(
    val esOscuro: Boolean,
    val turismapPrimario: Color = Color(0xFF1B5E20),
    val turismapAcento: Color = Color(0xFFF59E0B), // Ámbar / Dorado
    val airbnbOscuro: Color,
    val airbnbGris: Color,
    val airbnbFondo: Color,
    val airbnbBorde: Color
)

@Composable
internal fun rememberMapaPaleta(esOscuro: Boolean): MapaPaleta =
    remember(esOscuro) {
        MapaPaleta(
            esOscuro = esOscuro,
            airbnbOscuro = if (esOscuro) Color(0xFFF7F7F7) else Color(0xFF1E1E1E),
            airbnbGris = if (esOscuro) Color(0xFFA0A0A0) else Color(0xFF717171),
            airbnbFondo = if (esOscuro) Color(0xFF2C2C2C) else Color(0xFFF7F7F8),
            airbnbBorde = if (esOscuro) Color(0xFF404040) else Color(0xFFEBEBEB)
        )
    }

private const val PRECISION_COLISION_COORDENADAS = 20_000.0 // ~5.5 m
private const val RADIO_BASE_ANILLO_GRADOS = 0.00055 // ~61 m
private const val INCREMENTO_RADIO_ANILLO_GRADOS = 0.00050 // ~55 m por anillo adicional
private const val CAPACIDAD_BASE_ANILLO = 8

private fun normalizarTextoMapa(texto: String): String =
    texto.trim().lowercase()
        .replace('á', 'a')
        .replace('é', 'e')
        .replace('í', 'i')
        .replace('ó', 'o')
        .replace('ú', 'u')
        .replace('ü', 'u')
        .replace('ñ', 'n')

private fun esMarcadorComercio(lugar: Lugar): Boolean =
    lugar.id.startsWith("comercio:")

internal fun buscarMunicipioDeComercio(
    comercio: Comercio,
    municipiosMap: Map<String, Lugar>
): Lugar? {
    val claveExacta = comercio.lugarId.trim()
    if (claveExacta.isEmpty()) return null

    municipiosMap[claveExacta]?.let { return it }

    val claveNormalizada = normalizarTextoMapa(claveExacta)
    return municipiosMap.values.firstOrNull { lugar ->
        val idNorm = normalizarTextoMapa(lugar.id)
        val nombreNorm = normalizarTextoMapa(lugar.nombre)
        (idNorm.isNotEmpty() && (idNorm == claveNormalizada || claveNormalizada.contains(idNorm))) ||
            (nombreNorm.isNotEmpty() && (nombreNorm == claveNormalizada || nombreNorm.contains(claveNormalizada)))
    }
}

internal fun resolverCoordenadasComercio(
    comercio: Comercio,
    municipiosMap: Map<String, Lugar>
): Coordenada {
    comercio.coordenadaPropiaValida()?.let { return it }

    val municipio = buscarMunicipioDeComercio(comercio, municipiosMap)
    val coordenadaMunicipio = municipio?.ubicacionCoordenadas
    if (coordenadaMunicipio != null && esCoordenadaValida(coordenadaMunicipio)) {
        return coordenadaMunicipio
    }

    val ref = normalizarTextoMapa(
        listOf(municipio?.nombre.orEmpty(), comercio.lugarId, comercio.direccion).joinToString(" ")
    )
    return when {
        ref.contains("refugio") -> Coordenada(13.9744, -89.7719)
        ref.contains("apaneca") -> Coordenada(13.8569, -89.8028)
        ref.contains("ataco") -> Coordenada(13.8703, -89.8486)
        ref.contains("juayua") -> Coordenada(13.8417, -89.7456)
        ref.contains("salcoatitan") -> Coordenada(13.8306, -89.7547)
        ref.contains("nahuizalco") -> Coordenada(13.7778, -89.7361)
        ref.contains("ahuachapan") -> Coordenada(13.9214, -89.8450)
        ref.contains("tacuba") -> Coordenada(13.9000, -89.9333)
        else -> Coordenada(0.0, 0.0)
    }
}

private fun desplazarEnAnillo(
    base: Coordenada,
    indiceEnGrupo: Int,
    totalEnGrupo: Int
): Coordenada {
    var restante = indiceEnGrupo
    var anillo = 0
    var capacidadAnillo = CAPACIDAD_BASE_ANILLO
    var procesadosAntes = 0

    while (restante >= capacidadAnillo) {
        restante -= capacidadAnillo
        procesadosAntes += capacidadAnillo
        anillo++
        capacidadAnillo = CAPACIDAD_BASE_ANILLO + anillo * 4
    }

    val cantidadEnEsteAnillo = minOf(capacidadAnillo, totalEnGrupo - procesadosAntes).coerceAtLeast(1)
    val radio = RADIO_BASE_ANILLO_GRADOS + anillo * INCREMENTO_RADIO_ANILLO_GRADOS
    val desfaseAngular = (PI / 4.0) + (anillo * (PI / cantidadEnEsteAnillo))
    val angulo = desfaseAngular + (2.0 * PI * restante / cantidadEnEsteAnillo)
    val factorCosLat = cos(base.latitud * PI / 180.0).coerceAtLeast(0.2)

    val nuevaLat = (base.latitud + radio * sin(angulo)).coerceIn(-90.0, 90.0)
    val nuevaLng = (base.longitud + (radio * cos(angulo)) / factorCosLat).coerceIn(-180.0, 180.0)
    return Coordenada(latitud = nuevaLat, longitud = nuevaLng)
}

internal fun separarCoordenadasSuperpuestas(lugares: List<Lugar>): List<Lugar> {
    if (lugares.size <= 1) return lugares

    val gruposPorPosicion = lugares.withIndex().groupBy { (_, lugar) ->
        Pair(
            round(lugar.ubicacionCoordenadas.latitud * PRECISION_COLISION_COORDENADAS).toLong(),
            round(lugar.ubicacionCoordenadas.longitud * PRECISION_COLISION_COORDENADAS).toLong()
        )
    }

    val resultado = lugares.toMutableList()

    gruposPorPosicion.values.forEach { grupo ->
        if (grupo.size <= 1) return@forEach

        val destinosOrdenados = grupo.filterNot { esMarcadorComercio(it.value) }.sortedBy { it.value.id }
        val comerciosOrdenados = grupo.filter { esMarcadorComercio(it.value) }.sortedBy { it.value.id }

        if (destinosOrdenados.isNotEmpty()) {
            // El destino/municipio principal permanece fijo en su coordenada original
            val centroAncla = destinosOrdenados.first().value.ubicacionCoordenadas
            val elementosADistribuir = comerciosOrdenados + destinosOrdenados.drop(1)
            elementosADistribuir.forEachIndexed { posicion, item ->
                val coordenadaSeparada = desplazarEnAnillo(
                    base = centroAncla,
                    indiceEnGrupo = posicion,
                    totalEnGrupo = elementosADistribuir.size
                )
                resultado[item.index] = item.value.copy(ubicacionCoordenadas = coordenadaSeparada)
            }
        } else if (comerciosOrdenados.size > 1) {
            val centroBase = comerciosOrdenados.first().value.ubicacionCoordenadas
            comerciosOrdenados.forEachIndexed { posicion, item ->
                val coordenadaSeparada = desplazarEnAnillo(
                    base = centroBase,
                    indiceEnGrupo = posicion,
                    totalEnGrupo = comerciosOrdenados.size
                )
                resultado[item.index] = item.value.copy(ubicacionCoordenadas = coordenadaSeparada)
            }
        }
    }

    return resultado
}

internal fun codificarParametroMaps(valor: String): String =
    valor.encodeToByteArray().joinToString("") { byte ->
        val caracter = byte.toInt() and 0xFF
        if (caracter in 'a'.code..'z'.code ||
            caracter in 'A'.code..'Z'.code ||
            caracter in '0'.code..'9'.code ||
            caracter == '-'.code || caracter == '_'.code ||
            caracter == '.'.code || caracter == '~'.code
        ) {
            caracter.toChar().toString()
        } else {
            "%" + caracter.toString(16).uppercase().padStart(2, '0')
        }
    }

internal fun urlGoogleMaps(comercio: Comercio?, lugar: Lugar, municipio: String?): String {
    val destino = if (comercio == null) {
        listOf(lugar.nombre, lugar.ubicacionCorta, "El Salvador")
    } else {
        listOf(comercio.nombre, comercio.direccion, municipio, "El Salvador")
    }.filterNotNull().filter(String::isNotBlank).joinToString(", ")

    return "https://www.google.com/maps/dir/?api=1&destination=${codificarParametroMaps(destino)}"
}

internal class MapaState(
    private val scope: CoroutineScope,
    val snackbarHostState: SnackbarHostState
) {
    var textoBusqueda: String by mutableStateOf("")
    var filtroTipoMarcadores: String by mutableStateOf("Todos") // "Todos", "Destinos", "Comercios"
    var mostrarMenuWaze: Boolean by mutableStateOf(false)

    var lugaresTuristicos: List<Lugar> by mutableStateOf(emptyList())
    var todosLosComercios: List<Comercio> by mutableStateOf(emptyList())
    var municipiosMap: Map<String, Lugar> by mutableStateOf(emptyMap())
    var lugarSeleccionado: Lugar? by mutableStateOf(null)

    var cargando: Boolean by mutableStateOf(true)
    var errorCarga: String? by mutableStateOf(null)
    var intentoCarga: Int by mutableIntStateOf(0)

    val comerciosFiltrados: List<Comercio> by derivedStateOf {
        todosLosComercios.filter { comercio ->
            textoBusqueda.isBlank() ||
                comercio.nombre.contains(textoBusqueda, ignoreCase = true) ||
                comercio.direccion.contains(textoBusqueda, ignoreCase = true) ||
                comercio.categoria.contains(textoBusqueda, ignoreCase = true) ||
                comercio.descripcion.contains(textoBusqueda, ignoreCase = true)
        }
    }

    val lugaresFiltrados: List<Lugar> by derivedStateOf {
        lugaresTuristicos.filter { lugar ->
            textoBusqueda.isBlank() ||
                lugar.nombre.contains(textoBusqueda, ignoreCase = true) ||
                lugar.ubicacionCorta.contains(textoBusqueda, ignoreCase = true) ||
                lugar.historia.contains(textoBusqueda, ignoreCase = true)
        }
    }

    val lugaresParaElMapa: List<Lugar> by derivedStateOf {
        val lugaresDestino = if (filtroTipoMarcadores != "Comercios") lugaresFiltrados else emptyList()
        val lugaresComerciales = if (filtroTipoMarcadores != "Destinos") {
            comerciosFiltrados.map { comercio ->
                val municipio = buscarMunicipioDeComercio(comercio, municipiosMap)
                Lugar(
                    id = "comercio:${comercio.id}",
                    nombre = comercio.nombre,
                    subtitulo = comercio.categoria.replaceFirstChar { it.uppercase() },
                    categoria = CategoriaLugar.TODOS,
                    ubicacionCoordenadas = resolverCoordenadasComercio(comercio, municipiosMap),
                    ubicacionCorta = comercio.direccion,
                    ubicacionLarga = municipio?.nombre.orEmpty(),
                    calificacion = comercio.calificacionPromedio ?: 0.0,
                    cantidadResenas = comercio.cantidadResenas ?: 0,
                    historia = comercio.descripcion,
                    imagenPortadaUrl = comercio.imagenUrls.firstOrNull().orEmpty()
                )
            }
        } else {
            emptyList()
        }
        val conUbicacionValida = (lugaresDestino + lugaresComerciales).filter {
            esCoordenadaValida(it.ubicacionCoordenadas)
        }
        separarCoordenadasSuperpuestas(conUbicacionValida)
    }

    val comercioSeleccionado: Comercio? by derivedStateOf {
        lugarSeleccionado?.id
            ?.takeIf { it.startsWith("comercio:") }
            ?.removePrefix("comercio:")
            ?.let { id -> todosLosComercios.firstOrNull { it.id == id } }
    }

    fun actualizarBusqueda(nuevoTexto: String) {
        textoBusqueda = nuevoTexto
        lugarSeleccionado = null
    }

    fun limpiarBusqueda() {
        textoBusqueda = ""
        lugarSeleccionado = null
    }

    fun seleccionarFiltro(filtro: String) {
        filtroTipoMarcadores = filtro
        lugarSeleccionado = null
        mostrarMenuWaze = false
    }

    fun reintentarCarga() {
        intentoCarga++
    }

    fun mostrarMensajeError(mensaje: String) {
        scope.launch {
            snackbarHostState.showSnackbar(mensaje)
        }
    }
}

@Composable
internal fun rememberMapaState(
    latInicial: Double?,
    lngInicial: Double?
): MapaState {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val state = remember { MapaState(coroutineScope, snackbarHostState) }

    LaunchedEffect(state.intentoCarga) {
        state.cargando = true
        state.errorCarga = null
        val erroresCarga = mutableListOf<String>()
        try {
            state.lugaresTuristicos = LugaresRepository.obtenerTodos()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            erroresCarga += error.message ?: "No se pudieron cargar los destinos turísticos."
        }
        try {
            state.todosLosComercios = ComercioRepository.obtenerPublicados()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            erroresCarga += error.message ?: "No se pudieron cargar los comercios publicados."
        }
        state.municipiosMap = state.lugaresTuristicos.associateBy { it.id.trim() }
        state.errorCarga = erroresCarga.takeIf { it.isNotEmpty() }?.joinToString("\n")
        if (state.lugaresTuristicos.isEmpty() && state.todosLosComercios.isEmpty() && state.errorCarga == null) {
            state.errorCarga = "No hay destinos turísticos ni comercios publicados para mostrar."
        }
        state.cargando = false
    }

    // 1. Detección y selección automática del municipio si se ingresa con coordenadas específicas
    LaunchedEffect(latInicial, lngInicial, state.lugaresTuristicos) {
        if (latInicial != null && lngInicial != null && state.lugaresTuristicos.isNotEmpty()) {
            val lugarCoincidente = state.lugaresTuristicos.minByOrNull { lugar ->
                val dLat = lugar.ubicacionCoordenadas.latitud - latInicial
                val dLng = lugar.ubicacionCoordenadas.longitud - lngInicial
                (dLat * dLat) + (dLng * dLng)
            }
            if (lugarCoincidente != null) {
                state.lugarSeleccionado = lugarCoincidente
            }
        }
    }

    LaunchedEffect(state.errorCarga) {
        state.errorCarga?.let { mensaje ->
            state.snackbarHostState.showSnackbar(mensaje)
        }
    }

    return state
}
