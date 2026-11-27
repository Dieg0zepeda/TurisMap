package com.turismap.app.screens.mapa

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.turismap.app.EstadoCamaraMapa
import com.turismap.app.MapaCamaraCache
import com.turismap.app.data.models.AtractivoPublico
import com.turismap.app.data.models.CategoriaAtractivoPublico
import com.turismap.app.data.models.Lugar
import com.turismap.app.data.repository.Comercio
import com.turismap.app.data.repository.esCoordenadaValida
import com.turismap.app.theme.BosqueSobrePrimario
import com.turismap.app.theme.PinoBorde
import com.turismap.app.theme.PinoContenedor
import com.turismap.app.theme.TextoClaroBosque
import com.turismap.app.theme.TextoSecundarioBosque
import com.turismap.app.theme.VerdeBosqueMarca
import com.turismap.app.theme.VerdeSalviaOscuro
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.mapa_capa_atractivos_subtitulo
import turismap.shared.generated.resources.mapa_capa_atractivos_titulo
import turismap.shared.generated.resources.mapa_capa_comercios_subtitulo
import turismap.shared.generated.resources.mapa_capa_comercios_titulo
import turismap.shared.generated.resources.mapa_capa_destinos_subtitulo
import turismap.shared.generated.resources.mapa_capa_destinos_titulo
import turismap.shared.generated.resources.mapa_capa_todos_subtitulo
import turismap.shared.generated.resources.mapa_capa_todos_titulo
import turismap.shared.generated.resources.mapa_filtro_atractivos
import turismap.shared.generated.resources.mapa_filtro_comercios
import turismap.shared.generated.resources.mapa_filtro_destinos
import turismap.shared.generated.resources.mapa_filtro_todos

internal enum class FiltroMarcadoresMapa {
    TODOS,
    DESTINOS,
    COMERCIOS,
    ATRACTIVOS;

    val etiquetaRes: StringResource
        get() = when (this) {
            TODOS -> Res.string.mapa_filtro_todos
            DESTINOS -> Res.string.mapa_filtro_destinos
            COMERCIOS -> Res.string.mapa_filtro_comercios
            ATRACTIVOS -> Res.string.mapa_filtro_atractivos
        }

    val tituloCapaRes: StringResource
        get() = when (this) {
            TODOS -> Res.string.mapa_capa_todos_titulo
            DESTINOS -> Res.string.mapa_capa_destinos_titulo
            COMERCIOS -> Res.string.mapa_capa_comercios_titulo
            ATRACTIVOS -> Res.string.mapa_capa_atractivos_titulo
        }

    val subtituloCapaRes: StringResource
        get() = when (this) {
            TODOS -> Res.string.mapa_capa_todos_subtitulo
            DESTINOS -> Res.string.mapa_capa_destinos_subtitulo
            COMERCIOS -> Res.string.mapa_capa_comercios_subtitulo
            ATRACTIVOS -> Res.string.mapa_capa_atractivos_subtitulo
        }
}

// Paleta corporativa TurisMap
internal data class MapaPaleta(
    val esOscuro: Boolean,
    val turismapPrimario: Color,
    val sobrePrimario: Color,
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
            turismapPrimario = if (esOscuro) VerdeSalviaOscuro else VerdeBosqueMarca,
            sobrePrimario = if (esOscuro) BosqueSobrePrimario else Color.White,
            airbnbOscuro = if (esOscuro) TextoClaroBosque else Color(0xFF1E1E1E),
            airbnbGris = if (esOscuro) TextoSecundarioBosque else Color(0xFF717171),
            airbnbFondo = if (esOscuro) PinoContenedor else Color(0xFFF7F7F8),
            airbnbBorde = if (esOscuro) PinoBorde else Color(0xFFEBEBEB)
        )
    }

private const val MAX_SUGERENCIAS_BUSQUEDA = 8
private const val ZOOM_SELECCION_BUSQUEDA = 17.5f
private const val ZOOM_SELECCION_BUSQUEDA_ALT = 17.5001f

internal class MapaState(
    latInicial: Double?,
    lngInicial: Double?,
    zoomInicial: Float?,
    private val uidActual: String?,
    lugaresIniciales: List<Lugar>?,
    comerciosIniciales: List<Comercio>?,
    atractivosIniciales: List<AtractivoPublico>?,
    categoriasIniciales: Map<String, String>,
    val camaraGuardada: EstadoCamaraMapa?,
    private val textoBusquedaState: MutableState<String>,
    private val filtroNombreState: MutableState<String>,
    private val categoriaAtractivoNombreState: MutableState<String?>,
    private val scope: CoroutineScope,
    val snackbarHostState: SnackbarHostState
) {
    var textoBusqueda: String
        get() = textoBusquedaState.value
        set(value) { textoBusquedaState.value = value }

    var filtroTipoMarcadores: FiltroMarcadoresMapa
        get() = FiltroMarcadoresMapa.entries.firstOrNull { it.name == filtroNombreState.value }
            ?: FiltroMarcadoresMapa.TODOS
        set(value) { filtroNombreState.value = value.name }

    var categoriaAtractivoSeleccionada: CategoriaAtractivoPublico?
        get() = categoriaAtractivoNombreState.value?.let { nombre ->
            CategoriaAtractivoPublico.entries.firstOrNull { it.name == nombre }
        }
        set(value) { categoriaAtractivoNombreState.value = value?.name }

    var mostrarSugerencias: Boolean by mutableStateOf(false)
    var mostrarMenuWaze: Boolean by mutableStateOf(false)

    var lugaresTuristicos: List<Lugar> by mutableStateOf(lugaresIniciales.orEmpty())
    var todosLosComercios: List<Comercio> by mutableStateOf(comerciosIniciales.orEmpty())
    var atractivosPublicos: List<AtractivoPublico> by mutableStateOf(atractivosIniciales.orEmpty())
    var municipiosMap: Map<String, Lugar> by mutableStateOf(
        lugaresIniciales.orEmpty().associateBy { it.id.trim() }
    )
    var categoriasMap: Map<String, String> by mutableStateOf(categoriasIniciales)
    var marcadorSeleccionado: MarcadorMapa? by mutableStateOf(null)

    var camaraLatObjetivo: Double? by mutableStateOf(latInicial)
    var camaraLngObjetivo: Double? by mutableStateOf(lngInicial)
    var camaraZoomObjetivo: Float? by mutableStateOf(zoomInicial)

    var lugaresCargados: Boolean by mutableStateOf(lugaresIniciales != null)
    var comerciosCargados: Boolean by mutableStateOf(comerciosIniciales != null)
    var atractivosCargados: Boolean by mutableStateOf(atractivosIniciales != null)
    var errorLugares: String? by mutableStateOf(null)
    var errorComercios: String? by mutableStateOf(null)
    var errorAtractivos: String? by mutableStateOf(null)
    var intentoCarga: Int by mutableIntStateOf(0)

    fun guardarEstadoCamara(estado: EstadoCamaraMapa) {
        MapaCamaraCache.guardar(uidActual, estado)
    }

    val cargando: Boolean by derivedStateOf {
        !lugaresCargados || !comerciosCargados || !atractivosCargados
    }

    val errorCargaDetalle: String? by derivedStateOf {
        listOfNotNull(errorLugares, errorComercios, errorAtractivos)
            .takeIf { it.isNotEmpty() }
            ?.joinToString("\n")
    }

    val sinDatosPublicados: Boolean by derivedStateOf {
        !cargando &&
            errorCargaDetalle == null &&
            lugaresTuristicos.isEmpty() &&
            todosLosComercios.isEmpty() &&
            atractivosPublicos.isEmpty()
    }

    val hayErrorOEstadoVacioCarga: Boolean
        get() = errorCargaDetalle != null || sinDatosPublicados

    fun etiquetaCategoria(categoriaId: String): String =
        categoriasMap[categoriaId.trim()]?.takeIf(String::isNotBlank)
            ?: categoriaId.trim().replaceFirstChar { it.uppercase() }

    private fun comercioAMarcador(comercio: Comercio): MarcadorMapa.Comercio {
        val municipio = buscarMunicipioDeComercio(comercio, municipiosMap)
        val nombreMunicipio = municipio?.nombre?.ifBlank { null } ?: comercio.municipio
        return MarcadorMapa.Comercio(
            comercio = comercio,
            etiquetaCategoria = etiquetaCategoria(comercio.categoria),
            nombreMunicipio = nombreMunicipio,
            coordenadas = resolverCoordenadasComercio(comercio, municipiosMap)
        )
    }

    private val marcadoresComercio: List<MarcadorMapa.Comercio> by derivedStateOf {
        todosLosComercios.map(::comercioAMarcador)
    }

    private val marcadoresComercioPorId: Map<String, MarcadorMapa.Comercio> by derivedStateOf {
        marcadoresComercio.associateBy { it.idUnico }
    }

    val comerciosFiltrados: List<Comercio> by derivedStateOf {
        if (textoBusqueda.isBlank()) {
            todosLosComercios
        } else {
            todosLosComercios.filter { comercio ->
                val nombreMunicipio = buscarMunicipioDeComercio(comercio, municipiosMap)?.nombre.orEmpty()
                coincideBusquedaMapa(
                    consulta = textoBusqueda,
                    comercio.nombre,
                    comercio.direccion,
                    comercio.categoria,
                    etiquetaCategoria(comercio.categoria),
                    comercio.descripcion,
                    comercio.municipio,
                    comercio.lugarId,
                    nombreMunicipio
                )
            }
        }
    }

    val lugaresFiltrados: List<Lugar> by derivedStateOf {
        if (textoBusqueda.isBlank()) {
            lugaresTuristicos
        } else {
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
    }

    val atractivosPorBusqueda: List<AtractivoPublico> by derivedStateOf {
        filtrarAtractivosPublicos(
            atractivos = atractivosPublicos,
            consulta = textoBusqueda
        )
    }

    val conteoAtractivosPorCategoria: Map<CategoriaAtractivoPublico, Int> by derivedStateOf {
        CategoriaAtractivoPublico.entries.associateWith { categoria ->
            atractivosPorBusqueda.count { it.categoriaResuelta == categoria }
        }
    }

    val atractivosFiltrados: List<AtractivoPublico> by derivedStateOf {
        filtrarAtractivosPublicos(
            atractivos = atractivosPorBusqueda,
            categoriaSeleccionada = categoriaAtractivoSeleccionada
        )
    }

    val resultadosBusqueda: List<MarcadorMapa> by derivedStateOf {
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
                    if (relevancia == Int.MAX_VALUE) null else relevancia to MarcadorMapa.Destino(lugar)
                }

            val comerciosCoincidentes = todosLosComercios
                .mapNotNull { comercio ->
                    val marcador = marcadoresComercioPorId["comercio:${comercio.id}"] ?: comercioAMarcador(comercio)
                    if (!esCoordenadaValida(marcador.coordenadas)) return@mapNotNull null
                    val nombreMunicipio = buscarMunicipioDeComercio(comercio, municipiosMap)?.nombre.orEmpty()
                    val relevancia = relevanciaBusquedaMapa(
                        consulta = consulta,
                        nombre = comercio.nombre,
                        comercio.categoria,
                        etiquetaCategoria(comercio.categoria),
                        comercio.direccion,
                        comercio.municipio,
                        nombreMunicipio,
                        comercio.lugarId,
                        comercio.descripcion
                    )
                    if (relevancia == Int.MAX_VALUE) null else relevancia to marcador
                }

            val atractivosCoincidentes = atractivosPublicos
                .filter { esCoordenadaValida(it.coordenadas) }
                .mapNotNull { atractivo ->
                    val relevancia = relevanciaBusquedaMapa(
                        consulta = consulta,
                        nombre = atractivo.nombre,
                        atractivo.categoria,
                        palabrasClaveCategoriaAtractivo(atractivo.categoriaResuelta),
                        atractivo.municipio,
                        atractivo.lugarId,
                        atractivo.descripcion
                    )
                    if (relevancia == Int.MAX_VALUE) {
                        null
                    } else {
                        relevancia to MarcadorMapa.AtractivoPublico(atractivo)
                    }
                }

            (comerciosCoincidentes + atractivosCoincidentes + destinosCoincidentes)
                .sortedWith(
                    compareBy<Pair<Int, MarcadorMapa>> { it.first }
                        .thenBy { prioridadTipoMarcador(it.second) }
                        .thenBy { normalizarTextoMapa(it.second.titulo) }
                )
                .map { it.second }
                .take(MAX_SUGERENCIAS_BUSQUEDA)
        }
    }

    val marcadoresParaElMapa: List<MarcadorMapa> by derivedStateOf {
        val destinos = lugaresFiltrados.map { MarcadorMapa.Destino(it) }
        val comercios = if (textoBusqueda.isBlank()) {
            marcadoresComercio
        } else {
            comerciosFiltrados.map { comercio ->
                marcadoresComercioPorId["comercio:${comercio.id}"] ?: comercioAMarcador(comercio)
            }
        }
        val atractivos = atractivosFiltrados.map { MarcadorMapa.AtractivoPublico(it) }

        val visiblesPorCapa = filtrarMarcadoresPorCapa(
            destinos = destinos,
            comercios = comercios,
            atractivos = atractivos,
            filtroCapa = filtroTipoMarcadores,
            categoriaAtractivo = categoriaAtractivoSeleccionada
        )
        val conUbicacionValida = visiblesPorCapa.filter {
            esCoordenadaValida(it.coordenadas)
        }
        separarCoordenadasSuperpuestasMarcadores(conUbicacionValida)
    }

    fun comerciosConCoordenadaPropiaParaAutoseleccion(): List<MarcadorMapa.Comercio> =
        marcadoresComercio.filter { it.comercio.coordenadaPropiaValida() != null }

    private fun asegurarFiltroParaMarcador(marcador: MarcadorMapa) {
        when (marcador) {
            is MarcadorMapa.Comercio -> {
                if (filtroTipoMarcadores != FiltroMarcadoresMapa.TODOS &&
                    filtroTipoMarcadores != FiltroMarcadoresMapa.COMERCIOS
                ) {
                    filtroTipoMarcadores = FiltroMarcadoresMapa.TODOS
                    categoriaAtractivoSeleccionada = null
                }
            }

            is MarcadorMapa.Destino -> {
                if (filtroTipoMarcadores != FiltroMarcadoresMapa.TODOS &&
                    filtroTipoMarcadores != FiltroMarcadoresMapa.DESTINOS
                ) {
                    filtroTipoMarcadores = FiltroMarcadoresMapa.TODOS
                    categoriaAtractivoSeleccionada = null
                }
            }

            is MarcadorMapa.AtractivoPublico -> {
                if (filtroTipoMarcadores != FiltroMarcadoresMapa.TODOS &&
                    filtroTipoMarcadores != FiltroMarcadoresMapa.ATRACTIVOS
                ) {
                    filtroTipoMarcadores = FiltroMarcadoresMapa.TODOS
                    categoriaAtractivoSeleccionada = null
                } else if (categoriaAtractivoSeleccionada != null &&
                    categoriaAtractivoSeleccionada != marcador.categoria
                ) {
                    categoriaAtractivoSeleccionada = null
                }
            }
        }
    }

    fun aplicarAutoseleccion(marcador: MarcadorMapa) {
        asegurarFiltroParaMarcador(marcador)
        val marcadorEnMapa = marcadoresParaElMapa.firstOrNull { it.idUnico == marcador.idUnico } ?: marcador
        marcadorSeleccionado = marcadorEnMapa
    }

    private fun prioridadTipoMarcador(marcador: MarcadorMapa): Int = when (marcador) {
        is MarcadorMapa.Comercio -> 0
        is MarcadorMapa.AtractivoPublico -> 1
        is MarcadorMapa.Destino -> 2
    }

    fun actualizarBusqueda(nuevoTexto: String) {
        textoBusqueda = nuevoTexto
        mostrarSugerencias = nuevoTexto.isNotBlank()
        marcadorSeleccionado = null
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

    fun seleccionarResultadoBusqueda(marcador: MarcadorMapa) {
        asegurarFiltroParaMarcador(marcador)

        textoBusqueda = marcador.titulo
        mostrarSugerencias = false

        val marcadorEnMapa = marcadoresParaElMapa.firstOrNull { it.idUnico == marcador.idUnico } ?: marcador
        marcadorSeleccionado = marcadorEnMapa
        camaraLatObjetivo = marcadorEnMapa.coordenadas.latitud
        camaraLngObjetivo = marcadorEnMapa.coordenadas.longitud
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

    fun seleccionarMarcadorEnMapa(marcador: MarcadorMapa) {
        mostrarSugerencias = false
        marcadorSeleccionado = marcador
    }

    fun limpiarBusqueda() {
        textoBusqueda = ""
        mostrarSugerencias = false
        marcadorSeleccionado = null
        camaraLatObjetivo = null
        camaraLngObjetivo = null
        camaraZoomObjetivo = null
    }

    fun seleccionarFiltro(filtro: FiltroMarcadoresMapa) {
        filtroTipoMarcadores = filtro
        categoriaAtractivoSeleccionada = null
        marcadorSeleccionado = null
        mostrarSugerencias = false
        mostrarMenuWaze = false
    }

    fun seleccionarCategoriaAtractivo(categoria: CategoriaAtractivoPublico?) {
        filtroTipoMarcadores = FiltroMarcadoresMapa.ATRACTIVOS
        categoriaAtractivoSeleccionada = if (categoria != null && categoria == categoriaAtractivoSeleccionada) {
            null
        } else {
            categoria
        }
        marcadorSeleccionado = null
        mostrarSugerencias = false
        mostrarMenuWaze = false
    }

    fun reintentarCarga() {
        intentoCarga++
    }

    fun mostrarMensajeError(mensaje: String?, fallbackRes: StringResource) {
        scope.launch {
            snackbarHostState.showSnackbar(
                mensaje?.takeIf { it.isNotBlank() } ?: getString(fallbackRes)
            )
        }
    }
}
