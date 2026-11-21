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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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

internal fun resolverCoordenadasComercio(
    comercio: Comercio,
    municipiosMap: Map<String, Lugar>
): Coordenada {
    val municipio = municipiosMap[comercio.lugarId.trim()]
    var lat = municipio?.ubicacionCoordenadas?.latitud ?: 0.0
    var lng = municipio?.ubicacionCoordenadas?.longitud ?: 0.0

    if (lat == 0.0 || lng == 0.0) {
        val ref = (municipio?.nombre ?: "") + " " + comercio.direccion
        when {
            ref.contains("Refugio", ignoreCase = true) -> { lat = 13.9744; lng = -89.7719 }
            ref.contains("Apaneca", ignoreCase = true) -> { lat = 13.8569; lng = -89.8028 }
            ref.contains("Ataco", ignoreCase = true) -> { lat = 13.8703; lng = -89.8486 }
            ref.contains("Juayúa", ignoreCase = true) || ref.contains("Juayua", ignoreCase = true) -> { lat = 13.8417; lng = -89.7456 }
            ref.contains("Salcoatitán", ignoreCase = true) || ref.contains("Salcoatitan", ignoreCase = true) -> { lat = 13.8306; lng = -89.7547 }
            ref.contains("Nahuizalco", ignoreCase = true) -> { lat = 13.7778; lng = -89.7361 }
            else -> { lat = 0.0; lng = 0.0 }
        }
    }

    return Coordenada(latitud = lat, longitud = lng)
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
                val municipio = municipiosMap[comercio.lugarId.trim()]
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
        (lugaresDestino + lugaresComerciales).filter {
            it.ubicacionCoordenadas.latitud != 0.0 && it.ubicacionCoordenadas.longitud != 0.0
        }
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
