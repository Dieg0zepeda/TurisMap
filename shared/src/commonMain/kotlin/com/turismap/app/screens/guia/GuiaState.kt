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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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

internal data class CategoriaFiltro(val id: String, val etiqueta: String, val icono: String)

internal val categoriasFiltros = listOf(
    CategoriaFiltro("Todos", "Todo", "🧭"),
    CategoriaFiltro("restaurante", "Restaurantes", "☕"),
    CategoriaFiltro("hotel", "Alojamientos", "🏨"),
    CategoriaFiltro("artesania", "Artesanías", "🏺"),
    CategoriaFiltro("atractivo", "Experiencias", "🎈")
)

internal val municipiosRuta = listOf(
    "Todos",
    "Nahuizalco",
    "Salcoatitán",
    "Juayúa",
    "Apaneca",
    "Concepción de Ataco",
    "Ahuachapán"
)

internal fun coincideConCategoria(categoriaComercio: String?, filtroId: String): Boolean {
    if (filtroId.equals("Todos", ignoreCase = true)) return true
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
    var categoriaSeleccionada by mutableStateOf("Todos")
    var municipioSeleccionado by mutableStateOf("Todos")

    var listaComercios by mutableStateOf<List<Comercio>>(emptyList())
    var mapaMunicipios by mutableStateOf<Map<String, String>>(emptyMap())
    var cargando by mutableStateOf(cargandoInicial)
    var errorCarga by mutableStateOf<String?>(null)

    // Filtrado reactivo de comercios
    val comerciosFiltrados: List<Comercio> by derivedStateOf {
        listaComercios.filter { item ->
            val coincideCat = coincideConCategoria(item.categoria, categoriaSeleccionada)

            val nombreMuni = mapaMunicipios[item.lugarId] ?: item.direccion
            val coincideMuni = if (municipioSeleccionado.equals("Todos", ignoreCase = true)) true
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
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(e.message ?: "No se pudo actualizar favoritos.")
            }
        }
    }

    fun reintentarCarga() {
        scope.launch {
            cargando = true
            errorCarga = null
            try {
                listaComercios = ComercioRepository.obtenerPublicados()
            } catch (e: Exception) {
                errorCarga = e.message
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
                } catch (e: Exception) {
                    emptyMap()
                }

                state.listaComercios = comerciosCargados
                state.mapaMunicipios = municipiosCargados
            } catch (e: Exception) {
                state.errorCarga = e.message ?: "No se pudieron obtener los comercios."
            } finally {
                state.cargando = false
            }
        }
    }

    return state to favoritosUsuario
}
