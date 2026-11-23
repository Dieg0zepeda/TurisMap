package com.turismap.app.screens.guia

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.turismap.app.data.repository.Comercio
import com.turismap.app.data.repository.ComercioRepository
import com.turismap.app.data.repository.LugaresRepository
import com.turismap.app.data.repository.UsuarioRepository
import com.turismap.app.theme.TerracotaAcento
import com.turismap.app.theme.VerdePrimario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
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

internal fun normalizarTexto(txt: String?): String {
    if (txt.isNullOrBlank()) return ""
    return txt.lowercase()
        .replace("á", "a")
        .replace("é", "e")
        .replace("í", "i")
        .replace("ó", "o")
        .replace("ú", "u")
        .trim()
}

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

internal val municipiosRuta: List<String?> = listOf(
    null,
    "Nahuizalco",
    "Salcoatitán",
    "Juayúa",
    "Apaneca",
    "Concepción de Ataco",
    "Ahuachapán"
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
    cargandoInicial: Boolean,
    private val scope: CoroutineScope,
    val snackbarHostState: SnackbarHostState
) {
    var searchQuery by mutableStateOf("")
    var categoriaSeleccionada by mutableStateOf(CATEGORIA_FILTRO_TODOS)
    var municipioSeleccionado by mutableStateOf<String?>(null)
    var mostrarSelectorPueblo by mutableStateOf(false)

    var listaComercios by mutableStateOf<List<Comercio>>(emptyList())
    var mapaMunicipios by mutableStateOf<Map<String, String>>(emptyMap())
    var cargando by mutableStateOf(cargandoInicial)
    var errorCarga by mutableStateOf<String?>(null)

    // Filtrado reactivo de comercios
    val comerciosFiltrados: List<Comercio> by derivedStateOf {
        listaComercios.filter { item ->
            val coincideCat = coincideConCategoria(item.categoria, categoriaSeleccionada)

            val nombreMuni = mapaMunicipios[item.lugarId] ?: item.direccion
            val coincideMuni = if (municipioSeleccionado == null) true
            else normalizarTexto(nombreMuni).contains(normalizarTexto(municipioSeleccionado))

            val q = normalizarTexto(searchQuery)
            val coincideBusqueda = q.isBlank() ||
                normalizarTexto(item.nombre).contains(q) ||
                normalizarTexto(item.direccion).contains(q) ||
                normalizarTexto(item.descripcion).contains(q) ||
                normalizarTexto(nombreMuni).contains(q)

            coincideCat && coincideMuni && coincideBusqueda
        }.sortedByDescending { it.planInt }
    }

    // Experiencias destacadas para el carrusel superior (Socios y Premium)
    val comerciosDestacados: List<Comercio> by derivedStateOf {
        comerciosFiltrados.filter { it.planInt >= 2 }
    }

    fun toggleFavorito(idComercio: String, guardar: Boolean) {
        scope.launch {
            try {
                UsuarioRepository.cambiarFavorito(idComercio, guardar)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(
                    e.message ?: getString(Res.string.guia_error_actualizar_favorito)
                )
            }
        }
    }

    fun reintentarCarga() {
        scope.launch {
            cargando = true
            errorCarga = null
            try {
                listaComercios = ComercioRepository.obtenerPublicados()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errorCarga = e.message ?: getString(Res.string.guia_error_cargar_comercios)
            } finally {
                cargando = false
            }
        }
    }
}

@Composable
internal fun rememberGuiaState(comerciosExternos: List<Comercio>?): Pair<GuiaState, Set<String>> {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val state = remember {
        GuiaState(
            cargandoInicial = comerciosExternos == null,
            scope = coroutineScope,
            snackbarHostState = snackbarHostState
        )
    }

    val usuarioActual by UsuarioRepository.observarUsuarioActual().collectAsState(initial = null)
    val favoritosUsuario = remember(usuarioActual?.favoritos) {
        usuarioActual?.favoritos?.toSet() ?: emptySet()
    }

    LaunchedEffect(comerciosExternos) {
        if (comerciosExternos != null) {
            state.listaComercios = comerciosExternos
            state.cargando = false
        } else {
            state.cargando = true
            state.errorCarga = null
            try {
                val comerciosCargados = ComercioRepository.obtenerPublicados()
                val municipiosCargados = try {
                    LugaresRepository.obtenerTodos().associate { it.id to it.nombre }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    emptyMap()
                }

                state.listaComercios = comerciosCargados
                state.mapaMunicipios = municipiosCargados
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                state.errorCarga = e.message ?: getString(Res.string.guia_error_cargar_comercios)
            } finally {
                state.cargando = false
            }
        }
    }

    return state to favoritosUsuario
}
