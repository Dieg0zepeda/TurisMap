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
import com.turismap.app.data.models.Lugar
import com.turismap.app.data.repository.Comercio
import com.turismap.app.data.repository.ComercioRepository
import com.turismap.app.data.repository.LugaresRepository
import com.turismap.app.data.repository.esCoordenadaValida
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

private const val MAX_SUGERENCIAS_BUSQUEDA = 8
private const val ZOOM_SELECCION_BUSQUEDA = 17.5f
private const val ZOOM_SELECCION_BUSQUEDA_ALT = 17.5001f

internal class MapaState(
    latInicial: Double?,
    lngInicial: Double?,
    zoomInicial: Float?,
    private val scope: CoroutineScope,
    val snackbarHostState: SnackbarHostState
) {
    var textoBusqueda: String by mutableStateOf("")
    var mostrarSugerencias: Boolean by mutableStateOf(false)
    var filtroTipoMarcadores: String by mutableStateOf("Todos") // "Todos", "Destinos", "Comercios"
    var mostrarMenuWaze: Boolean by mutableStateOf(false)

    var lugaresTuristicos: List<Lugar> by mutableStateOf(emptyList())
    var todosLosComercios: List<Comercio> by mutableStateOf(emptyList())
    var municipiosMap: Map<String, Lugar> by mutableStateOf(emptyMap())
    var lugarSeleccionado: Lugar? by mutableStateOf(null)

    var camaraLatObjetivo: Double? by mutableStateOf(latInicial)
    var camaraLngObjetivo: Double? by mutableStateOf(lngInicial)
    var camaraZoomObjetivo: Float? by mutableStateOf(zoomInicial)

    var lugaresCargados: Boolean by mutableStateOf(false)
    var comerciosCargados: Boolean by mutableStateOf(false)
    var errorLugares: String? by mutableStateOf(null)
    var errorComercios: String? by mutableStateOf(null)
    var intentoCarga: Int by mutableIntStateOf(0)

    val cargando: Boolean by derivedStateOf {
        !lugaresCargados || !comerciosCargados
    }

    val errorCarga: String? by derivedStateOf {
        val errores = listOfNotNull(errorLugares, errorComercios)
        when {
            errores.isNotEmpty() -> errores.joinToString("\n")
            !cargando && lugaresTuristicos.isEmpty() && todosLosComercios.isEmpty() ->
                "No hay destinos turísticos ni comercios publicados para mostrar."
            else -> null
        }
    }

    private fun comercioALugar(comercio: Comercio): Lugar {
        val municipio = buscarMunicipioDeComercio(comercio, municipiosMap)
        val nombreMunicipio = municipio?.nombre?.ifBlank { null } ?: comercio.municipio
        return Lugar(
            id = "comercio:${comercio.id}",
            nombre = comercio.nombre,
            subtitulo = comercio.categoria.replaceFirstChar { it.uppercase() },
            categoria = CategoriaLugar.TODOS,
            ubicacionCoordenadas = resolverCoordenadasComercio(comercio, municipiosMap),
            ubicacionCorta = comercio.direccion,
            ubicacionLarga = nombreMunicipio,
            calificacion = comercio.calificacionPromedio ?: 0.0,
            cantidadResenas = comercio.cantidadResenas ?: 0,
            historia = comercio.descripcion,
            imagenPortadaUrl = comercio.imagenUrls.firstOrNull().orEmpty()
        )
    }

    val comerciosFiltrados: List<Comercio> by derivedStateOf {
        todosLosComercios.filter { comercio ->
            val nombreMunicipio = buscarMunicipioDeComercio(comercio, municipiosMap)?.nombre.orEmpty()
            coincideBusquedaMapa(
                consulta = textoBusqueda,
                comercio.nombre,
                comercio.direccion,
                comercio.categoria,
                comercio.descripcion,
                comercio.municipio,
                comercio.lugarId,
                nombreMunicipio
            )
        }
    }

    val lugaresFiltrados: List<Lugar> by derivedStateOf {
        lugaresTuristicos.filter { lugar ->
            coincideBusquedaMapa(
                consulta = textoBusqueda,
                lugar.nombre,
                lugar.subtitulo,
                lugar.ubicacionCorta,
                lugar.ubicacionLarga,
                lugar.historia
            )
        }
    }

    val resultadosBusqueda: List<Lugar> by derivedStateOf {
        if (textoBusqueda.isBlank()) {
            emptyList()
        } else {
            val consulta = textoBusqueda
            val destinosCoincidentes = lugaresTuristicos
                .filter { esCoordenadaValida(it.ubicacionCoordenadas) }
                .mapNotNull { lugar ->
                    val relevancia = relevanciaBusquedaMapa(
                        consulta = consulta,
                        nombre = lugar.nombre,
                        lugar.subtitulo,
                        lugar.ubicacionCorta,
                        lugar.ubicacionLarga,
                        lugar.historia
                    )
                    if (relevancia == Int.MAX_VALUE) null else relevancia to lugar
                }

            val comerciosCoincidentes = todosLosComercios
                .mapNotNull { comercio ->
                    val marcador = comercioALugar(comercio)
                    if (!esCoordenadaValida(marcador.ubicacionCoordenadas)) return@mapNotNull null
                    val nombreMunicipio = buscarMunicipioDeComercio(comercio, municipiosMap)?.nombre.orEmpty()
                    val relevancia = relevanciaBusquedaMapa(
                        consulta = consulta,
                        nombre = comercio.nombre,
                        comercio.categoria,
                        comercio.direccion,
                        comercio.municipio,
                        nombreMunicipio,
                        comercio.lugarId,
                        comercio.descripcion
                    )
                    if (relevancia == Int.MAX_VALUE) null else relevancia to marcador
                }

            (comerciosCoincidentes + destinosCoincidentes)
                .sortedWith(
                    compareBy<Pair<Int, Lugar>> { it.first }
                        .thenBy { !esMarcadorComercio(it.second) }
                        .thenBy { normalizarTextoMapa(it.second.nombre) }
                )
                .map { it.second }
                .take(MAX_SUGERENCIAS_BUSQUEDA)
        }
    }

    val lugaresParaElMapa: List<Lugar> by derivedStateOf {
        val lugaresDestino = if (filtroTipoMarcadores != "Comercios") lugaresFiltrados else emptyList()
        val lugaresComerciales = if (filtroTipoMarcadores != "Destinos") {
            comerciosFiltrados.map(::comercioALugar)
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
        mostrarSugerencias = nuevoTexto.isNotBlank()
        lugarSeleccionado = null
        if (nuevoTexto.isBlank()) {
            camaraLatObjetivo = null
            camaraLngObjetivo = null
            camaraZoomObjetivo = null
        }
    }

    fun abrirSugerenciasSiHayTexto() {
        if (textoBusqueda.isNotBlank()) {
            mostrarSugerencias = true
        }
    }

    fun seleccionarResultadoBusqueda(lugar: Lugar) {
        if (esMarcadorComercio(lugar) && filtroTipoMarcadores == "Destinos") {
            filtroTipoMarcadores = "Todos"
        } else if (!esMarcadorComercio(lugar) && filtroTipoMarcadores == "Comercios") {
            filtroTipoMarcadores = "Todos"
        }

        textoBusqueda = lugar.nombre
        mostrarSugerencias = false

        val marcadorEnMapa = lugaresParaElMapa.firstOrNull { it.id == lugar.id } ?: lugar
        lugarSeleccionado = marcadorEnMapa
        camaraLatObjetivo = marcadorEnMapa.ubicacionCoordenadas.latitud
        camaraLngObjetivo = marcadorEnMapa.ubicacionCoordenadas.longitud
        camaraZoomObjetivo = if (camaraZoomObjetivo == ZOOM_SELECCION_BUSQUEDA) {
            ZOOM_SELECCION_BUSQUEDA_ALT
        } else {
            ZOOM_SELECCION_BUSQUEDA
        }
    }

    fun buscarPrimerResultado() {
        val primero = resultadosBusqueda.firstOrNull()
        if (primero != null) {
            seleccionarResultadoBusqueda(primero)
        } else {
            mostrarSugerencias = textoBusqueda.isNotBlank()
        }
    }

    fun seleccionarMarcadorEnMapa(lugar: Lugar) {
        mostrarSugerencias = false
        lugarSeleccionado = lugar
    }

    fun limpiarBusqueda() {
        textoBusqueda = ""
        mostrarSugerencias = false
        lugarSeleccionado = null
        camaraLatObjetivo = null
        camaraLngObjetivo = null
        camaraZoomObjetivo = null
    }

    fun seleccionarFiltro(filtro: String) {
        filtroTipoMarcadores = filtro
        lugarSeleccionado = null
        mostrarSugerencias = false
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
    lngInicial: Double?,
    zoomInicial: Float?
): MapaState {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val state = remember {
        MapaState(
            latInicial = latInicial,
            lngInicial = lngInicial,
            zoomInicial = zoomInicial,
            scope = coroutineScope,
            snackbarHostState = snackbarHostState
        )
    }

    LaunchedEffect(latInicial, lngInicial, zoomInicial) {
        state.camaraLatObjetivo = latInicial
        state.camaraLngObjetivo = lngInicial
        state.camaraZoomObjetivo = zoomInicial
    }

    LaunchedEffect(state.intentoCarga) {
        state.lugaresCargados = false
        state.errorLugares = null
        try {
            val lugares = LugaresRepository.obtenerTodos()
            state.lugaresTuristicos = lugares
            state.municipiosMap = lugares.associateBy { it.id.trim() }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            state.errorLugares = error.message ?: "No se pudieron cargar los destinos turísticos."
        } finally {
            state.lugaresCargados = true
        }
    }

    LaunchedEffect(state.intentoCarga) {
        state.comerciosCargados = false
        state.errorComercios = null
        try {
            ComercioRepository.observarPublicados().collect { comercios ->
                state.todosLosComercios = comercios
                state.errorComercios = null
                state.comerciosCargados = true
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            try {
                state.todosLosComercios = ComercioRepository.obtenerPublicados()
                state.errorComercios = null
            } catch (fallbackError: CancellationException) {
                throw fallbackError
            } catch (fallbackError: Exception) {
                state.errorComercios = fallbackError.message
                    ?: error.message
                    ?: "No se pudieron cargar los comercios publicados."
            } finally {
                state.comerciosCargados = true
            }
        }
    }

    // Detección y selección automática del municipio si se ingresa con coordenadas específicas
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
