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
    val airbnbPrimary: Color = Color(0xFFFF385C),
    val verdeTurisMap: Color = Color(0xFF10B981),
    val verdeOscuroTurisMap: Color = Color(0xFF047857)
)

@Composable
internal fun rememberGuiaPaleta(esOscuro: Boolean): GuiaPaleta =
    remember(esOscuro) {
        GuiaPaleta(
            esOscuro = esOscuro,
            airbnbBlack = if (esOscuro) Color(0xFFF7F7F7) else Color(0xFF222222),
            airbnbGray = if (esOscuro) Color(0xFFA0A0A0) else Color(0xFF717171),
            airbnbLightGray = if (esOscuro) Color(0xFF2C2C2C) else Color(0xFFF7F7F7),
            airbnbBorder = if (esOscuro) Color(0xFF404040) else Color(0xFFEBEBEB)
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
    val etiquetaRes: StringResource,
    val icono: String
)

internal val categoriasFiltros: List<CategoriaFiltro> by lazy {
    listOf(
        CategoriaFiltro(CATEGORIA_FILTRO_TODOS, Res.string.guia_categoria_todo, "🧭"),
        CategoriaFiltro("restaurante", Res.string.guia_categoria_restaurantes, "☕"),
        CategoriaFiltro("hotel", Res.string.guia_categoria_alojamientos, "🏨"),
        CategoriaFiltro("artesania", Res.string.guia_categoria_artesanias, "🏺"),
        CategoriaFiltro("atractivo", Res.string.guia_categoria_experiencias, "🎈")
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
