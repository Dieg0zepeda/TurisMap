package com.turismap.app.screens.guia

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.turismap.app.data.repository.Comercio
import com.turismap.app.data.repository.ComercioRepository
import com.turismap.app.data.repository.EstadoPublicados
import com.turismap.app.data.repository.LugaresRepository
import com.turismap.app.data.repository.UsuarioRepository
import com.turismap.app.screens.mapa.normalizarTextoMapa
import com.turismap.app.theme.TerracotaAcento
import com.turismap.app.theme.VerdePrimario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.guia_categoria_alojamientos
import turismap.shared.generated.resources.guia_categoria_artesanias
import turismap.shared.generated.resources.guia_categoria_experiencias
import turismap.shared.generated.resources.guia_categoria_restaurantes
import turismap.shared.generated.resources.guia_categoria_todo
import turismap.shared.generated.resources.guia_error_actualizar_favorito
import turismap.shared.generated.resources.guia_error_cargar_comercios

internal data class GuiaPaleta(
    val esOscuro: Boolean,
    val airbnbBlack: Color,
    val airbnbGray: Color,
    val airbnbLightGray: Color,
    val airbnbBorder: Color,
    val superficieTarjeta: Color,
    val airbnbPrimary: Color = Color(0xFFFF385C),
    val verdeTurisMap: Color = VerdePrimario,
    val filtroActivoFondo: Color,
    val filtroActivoContenido: Color,
    val ambarFondo: Color,
    val ambarTexto: Color,
    val ambarAcento: Color,
    val socioFondo: Color,
    val socioTexto: Color,
    val socioAcento: Color,
    val placeholderInicio: Color,
    val placeholderFin: Color,
    val placeholderIcono: Color
)

@Composable
internal fun rememberGuiaPaleta(esOscuro: Boolean): GuiaPaleta =
    remember(esOscuro) {
        GuiaPaleta(
            esOscuro = esOscuro,
            airbnbBlack = if (esOscuro) Color(0xFFF5F6F5) else Color(0xFF1E2220),
            airbnbGray = if (esOscuro) Color(0xFFA0A7A3) else Color(0xFF6B7280),
            airbnbLightGray = if (esOscuro) Color(0xFF262A28) else Color(0xFFF3F5F4),
            airbnbBorder = if (esOscuro) Color(0xFF353B38) else Color(0xFFE5E7EB),
            superficieTarjeta = if (esOscuro) Color(0xFF1E2120) else Color.White,
            filtroActivoFondo = if (esOscuro) Color(0xFF2A7D5E) else VerdePrimario,
            filtroActivoContenido = if (esOscuro) Color(0xFF6EE7B7) else VerdePrimario,
            ambarFondo = if (esOscuro) Color(0xFF3B2C12).copy(alpha = 0.92f) else Color(0xFFFEF3C7).copy(alpha = 0.95f),
            ambarTexto = if (esOscuro) Color(0xFFFDE68A) else Color(0xFF92400E),
            ambarAcento = if (esOscuro) Color(0xFFFBBF24) else Color(0xFFD97706),
            socioFondo = if (esOscuro) Color(0xFF2B2725).copy(alpha = 0.92f) else Color(0xFFF7F4F1).copy(alpha = 0.95f),
            socioTexto = if (esOscuro) Color(0xFFE5E7EB) else Color(0xFF374151),
            socioAcento = if (esOscuro) Color(0xFFE28B73) else TerracotaAcento,
            placeholderInicio = if (esOscuro) Color(0xFF28302C) else Color(0xFFEDEAE4),
            placeholderFin = if (esOscuro) Color(0xFF1B221F) else Color(0xFFDCE4DF),
            placeholderIcono = if (esOscuro) Color(0xFF8FA399) else Color(0xFF64746C)
        )
    }

internal fun normalizarTexto(txt: String?): String =
    if (txt.isNullOrBlank()) "" else normalizarTextoMapa(txt)

internal const val CATEGORIA_FILTRO_TODOS = "Todos"

internal data class CategoriaFiltro(
    val id: String,
    val etiquetaRes: StringResource
)

internal val categoriasFiltros: List<CategoriaFiltro> by lazy {
    listOf(
        CategoriaFiltro(CATEGORIA_FILTRO_TODOS, Res.string.guia_categoria_todo),
        CategoriaFiltro("restaurante", Res.string.guia_categoria_restaurantes),
        CategoriaFiltro("hotel", Res.string.guia_categoria_alojamientos),
        CategoriaFiltro("artesania", Res.string.guia_categoria_artesanias),
        CategoriaFiltro("atractivo", Res.string.guia_categoria_experiencias)
    )
}

internal val municipiosRutaRespaldo: List<String> = listOf(
    "Nahuizalco",
    "Salcoatitán",
    "Juayúa",
    "Apaneca",
    "Concepción de Ataco",
    "Ahuachapán",
    "El Refugio",
    "Tacuba"
)

internal fun coincideConCategoria(categoriaComercio: String?, filtroId: String): Boolean {
    if (filtroId.equals(CATEGORIA_FILTRO_TODOS, ignoreCase = true)) return true
    val cat = normalizarTexto(categoriaComercio)
    val f = normalizarTexto(filtroId)

    return when (f) {
        "restaurante" -> cat.contains("restauran") || cat.contains("cafe") || cat.contains("comida") || cat.contains("gastronom")
        "hotel" -> cat.contains("hotel") || cat.contains("alojam") || cat.contains("hosped") || cat.contains("posada") || cat.contains("cabana")
        "artesania" -> cat.contains("artesan") || cat.contains("souvenir") || cat.contains("tienda")
        "atractivo" -> cat.contains("atractiv") || cat.contains("experienc") || cat.contains("tour") || cat.contains("aventura")
        else -> cat.contains(f)
    }
}

internal class GuiaState(
    comerciosIniciales: List<Comercio>,
    municipiosIniciales: Map<String, String>,
    categoriasIniciales: Map<String, String>,
    cargandoInicial: Boolean,
    private val searchQueryState: MutableState<String>,
    private val categoriaSeleccionadaState: MutableState<String>,
    private val municipioSeleccionadoState: MutableState<String?>,
    private val scope: CoroutineScope,
    val snackbarHostState: SnackbarHostState
) {
    var searchQuery: String
        get() = searchQueryState.value
        set(value) { searchQueryState.value = value }

    var categoriaSeleccionada: String
        get() = categoriaSeleccionadaState.value
        set(value) { categoriaSeleccionadaState.value = value }

    var municipioSeleccionado: String?
        get() = municipioSeleccionadoState.value
        set(value) { municipioSeleccionadoState.value = value }

    var mostrarSelectorPueblo by mutableStateOf(false)

    var listaComercios by mutableStateOf(comerciosIniciales)
    var mapaMunicipios by mutableStateOf(municipiosIniciales)
    var mapaCategorias by mutableStateOf(categoriasIniciales)
    var favoritosEnProceso by mutableStateOf<Set<String>>(emptySet())
    var cargando by mutableStateOf(cargandoInicial)
    var errorCarga by mutableStateOf<String?>(null)

    val opcionesMunicipios: List<String?> by derivedStateOf {
        val dinamicos = mapaMunicipios.values
            .map(String::trim)
            .filter(String::isNotEmpty)
            .distinct()
        val base = if (dinamicos.isNotEmpty()) dinamicos else municipiosRutaRespaldo
        listOf(null) + base
    }

    fun etiquetaCategoria(categoriaId: String): String =
        mapaCategorias[categoriaId.trim()]?.takeIf(String::isNotBlank) ?: categoriaId.trim()

    fun resolverUbicacionComercio(item: Comercio): String =
        mapaMunicipios[item.lugarId.trim()]
            ?: mapaMunicipios[item.lugarId]
            ?: item.municipio.trim().ifBlank { item.direccion.trim() }

    // Filtrado reactivo de comercios con normalización de consulta/municipio fuera del bucle
    val comerciosFiltrados: List<Comercio> by derivedStateOf {
        val q = normalizarTexto(searchQuery)
        val filtroCategoria = categoriaSeleccionada
        val muniFiltroNorm = municipioSeleccionado?.let(::normalizarTexto).orEmpty()

        listaComercios.filter { item ->
            val etiquetaCat = etiquetaCategoria(item.categoria)
            val coincideCat = coincideConCategoria(item.categoria, filtroCategoria) ||
                coincideConCategoria(etiquetaCat, filtroCategoria)
            if (!coincideCat) return@filter false

            val nombreMuni = resolverUbicacionComercio(item)
            val coincideMuni = muniFiltroNorm.isEmpty() ||
                normalizarTexto(nombreMuni).contains(muniFiltroNorm)
            if (!coincideMuni) return@filter false

            q.isBlank() ||
                normalizarTexto(item.nombre).contains(q) ||
                normalizarTexto(item.direccion).contains(q) ||
                normalizarTexto(item.descripcion).contains(q) ||
                normalizarTexto(nombreMuni).contains(q) ||
                normalizarTexto(etiquetaCat).contains(q) ||
                normalizarTexto(item.categoria).contains(q)
        }.sortedByDescending { it.planInt }
    }

    // Experiencias destacadas para el carrusel superior (Socios y Premium)
    val comerciosDestacados: List<Comercio> by derivedStateOf {
        comerciosFiltrados.filter { it.planInt >= 2 }
    }

    fun toggleFavorito(idComercio: String, guardar: Boolean) {
        if (idComercio in favoritosEnProceso) return
        favoritosEnProceso = favoritosEnProceso + idComercio
        scope.launch {
            try {
                UsuarioRepository.cambiarFavorito(
                    lugarId = idComercio,
                    guardar = guardar,
                    esComercio = true
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(
                    e.message ?: getString(Res.string.guia_error_actualizar_favorito)
                )
            } finally {
                favoritosEnProceso = favoritosEnProceso - idComercio
            }
        }
    }

    suspend fun cargarCatalogosAuxiliares(forzarRecarga: Boolean = false) {
        coroutineScope {
            val tareaMunicipios = async {
                try {
                    LugaresRepository.obtenerTodos(forzarRecarga = forzarRecarga)
                        .associate { it.id.trim() to it.nombre.trim() }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    null
                }
            }
            val tareaCategorias = async {
                try {
                    ComercioRepository.obtenerCategorias(forzarRecarga = forzarRecarga)
                        .associate { it.id.trim() to it.etiqueta.trim() }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    null
                }
            }
            tareaMunicipios.await()?.let { mapaMunicipios = it }
            tareaCategorias.await()?.let { mapaCategorias = it }
        }
    }

    suspend fun cargarDatos(forzarRecarga: Boolean = false) {
        val tieneDatosVisibles = listaComercios.isNotEmpty()
        if (!tieneDatosVisibles) {
            cargando = true
        }
        errorCarga = null
        try {
            val comerciosCargados = ComercioRepository.obtenerPublicados(forzarRecarga = forzarRecarga)
            listaComercios = comerciosCargados
            cargarCatalogosAuxiliares(forzarRecarga = forzarRecarga)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (!tieneDatosVisibles) {
                errorCarga = e.message ?: getString(Res.string.guia_error_cargar_comercios)
            }
        } finally {
            cargando = false
        }
    }

    fun reintentarCarga() {
        scope.launch {
            ComercioRepository.reintentarObservacionPublicados()
            cargarDatos(forzarRecarga = true)
        }
    }
}

@Composable
internal fun rememberGuiaState(comerciosExternos: List<Comercio>?): Pair<GuiaState, Set<String>> {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val searchQueryState = rememberSaveable { mutableStateOf("") }
    val categoriaSeleccionadaState = rememberSaveable { mutableStateOf(CATEGORIA_FILTRO_TODOS) }
    val municipioSeleccionadoState = rememberSaveable { mutableStateOf<String?>(null) }

    val state = remember {
        val comerciosCache = comerciosExternos ?: ComercioRepository.publicadosEnCache
        val municipiosCache = LugaresRepository.lugaresEnCache
            ?.associate { it.id.trim() to it.nombre.trim() }
            .orEmpty()
        val categoriasCache = ComercioRepository.categoriasEnCache
            ?.associate { it.id.trim() to it.etiqueta.trim() }
            .orEmpty()
        GuiaState(
            comerciosIniciales = comerciosCache.orEmpty(),
            municipiosIniciales = municipiosCache,
            categoriasIniciales = categoriasCache,
            cargandoInicial = comerciosCache == null,
            searchQueryState = searchQueryState,
            categoriaSeleccionadaState = categoriaSeleccionadaState,
            municipioSeleccionadoState = municipioSeleccionadoState,
            scope = coroutineScope,
            snackbarHostState = snackbarHostState
        )
    }

    val usuarioActual by UsuarioRepository.usuarioActualFlow.collectAsState()
    val favoritosUsuario = remember(usuarioActual?.favoritos) {
        usuarioActual?.favoritos?.toSet() ?: emptySet()
    }

    // Catálogos auxiliares (municipios y categorías) desde caché o en segundo plano sin shimmer
    LaunchedEffect(Unit) {
        if (state.mapaMunicipios.isEmpty() || state.mapaCategorias.isEmpty()) {
            state.cargarCatalogosAuxiliares(forzarRecarga = false)
        }
    }

    // Suscripción compartida a comercios publicados (sin recrear listeners ni disparar shimmer)
    LaunchedEffect(comerciosExternos) {
        if (comerciosExternos != null) {
            state.listaComercios = comerciosExternos
            state.errorCarga = null
            state.cargando = false
            return@LaunchedEffect
        }

        ComercioRepository.publicadosStateFlow.collect { estado ->
            when (estado) {
                is EstadoPublicados.Cargando -> {
                    val cache = ComercioRepository.publicadosEnCache
                    if (cache != null) {
                        state.listaComercios = cache
                        state.cargando = false
                    } else if (state.listaComercios.isEmpty()) {
                        state.cargando = true
                    }
                }
                is EstadoPublicados.Exito -> {
                    state.listaComercios = estado.comercios
                    state.errorCarga = null
                    state.cargando = false
                }
                is EstadoPublicados.Error -> {
                    val cache = estado.comerciosEnCache
                    if (cache != null) {
                        state.listaComercios = cache
                        state.errorCarga = null
                        state.cargando = false
                    } else if (state.listaComercios.isEmpty()) {
                        state.cargarDatos(forzarRecarga = false)
                    } else {
                        state.cargando = false
                    }
                }
            }
        }
    }

    return state to favoritosUsuario
}
