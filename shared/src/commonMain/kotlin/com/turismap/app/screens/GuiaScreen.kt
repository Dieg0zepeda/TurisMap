package com.turismap.app.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.components.RemoteImage
import com.turismap.app.data.repository.Comercio
import com.turismap.app.data.repository.ComercioRepository
import com.turismap.app.data.repository.LugaresRepository
import com.turismap.app.data.repository.UsuarioRepository
import com.turismap.app.theme.*
import kotlinx.coroutines.launch

private val AirbnbBlack: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFF7F7F7) else Color(0xFF222222)
private val AirbnbGray: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFA0A0A0) else Color(0xFF717171)
private val AirbnbLightGray: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF2C2C2C) else Color(0xFFF7F7F7)
private val AirbnbBorder: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF404040) else Color(0xFFEBEBEB)
private val AirbnbPrimary = Color(0xFFFF385C)
private val VerdeTurisMap = Color(0xFF10B981)
private val VerdeOscuroTurisMap = Color(0xFF047857)

private fun normalizarTexto(txt: String?): String {
    if (txt.isNullOrBlank()) return ""
    return txt.lowercase()
        .replace("á", "a")
        .replace("é", "e")
        .replace("í", "i")
        .replace("ó", "o")
        .replace("ú", "u")
        .trim()
}

private data class CategoriaFiltro(val id: String, val etiqueta: String, val icono: String)

private val categoriasFiltros = listOf(
    CategoriaFiltro("Todos", "Todo", "🧭"),
    CategoriaFiltro("restaurante", "Restaurantes", "☕"),
    CategoriaFiltro("hotel", "Alojamientos", "🏨"),
    CategoriaFiltro("artesania", "Artesanías", "🏺"),
    CategoriaFiltro("atractivo", "Experiencias", "🎈")
)

private val municipiosRuta = listOf(
    "Todos",
    "Nahuizalco",
    "Salcoatitán",
    "Juayúa",
    "Apaneca",
    "Concepción de Ataco",
    "Ahuachapán"
)

private fun coincideConCategoria(categoriaComercio: String?, filtroId: String): Boolean {
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

// Shimmer sin errores de sintaxis
fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val widthPx = if (size.width > 0) size.width.toFloat() else 300f
    val startOffsetX by transition.animateFloat(
        initialValue = -2f * widthPx,
        targetValue = 2f * widthPx,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing)
        ),
        label = "shimmer_x"
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFE0E0E0),
                Color(0xFFF5F5F5),
                Color(0xFFE0E0E0)
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    ).onGloballyPositioned { size = it.size }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuiaScreen(
    paddingValues: PaddingValues = PaddingValues(0.dp),
    comerciosExternos: List<Comercio>? = null,
    onLlamarComercio: (String) -> Unit = {},
    onComercioClick: (String) -> Unit = {},
    onIniciarRuta: (Comercio) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf("Todos") }
    var municipioSeleccionado by remember { mutableStateOf("Todos") }

    var listaComercios: List<Comercio> by remember { mutableStateOf(emptyList()) }
    var mapaMunicipios: Map<String, String> by remember { mutableStateOf(emptyMap()) }
    var cargando by remember { mutableStateOf(comerciosExternos == null) }
    var errorCarga by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val usuarioActual by UsuarioRepository.observarUsuarioActual().collectAsState(initial = null)
    val favoritosUsuario = remember(usuarioActual?.favoritos) {
        usuarioActual?.favoritos?.toSet() ?: emptySet()
    }

    val onToggleFavorito: (String, Boolean) -> Unit = { idComercio, guardar ->
        coroutineScope.launch {
            try {
                UsuarioRepository.cambiarFavorito(idComercio, guardar)
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(e.message ?: "No se pudo actualizar favoritos.")
            }
        }
    }

    LaunchedEffect(comerciosExternos) {
        if (comerciosExternos != null) {
            listaComercios = comerciosExternos
            cargando = false
        } else {
            cargando = true
            errorCarga = null
            try {
                val comerciosCargados = ComercioRepository.obtenerPublicados()
                val municipiosCargados = try {
                    LugaresRepository.obtenerTodos().associate { it.id to it.nombre }
                } catch (e: Exception) {
                    emptyMap()
                }

                listaComercios = comerciosCargados
                mapaMunicipios = municipiosCargados
            } catch (e: Exception) {
                errorCarga = e.message ?: "No se pudieron obtener los comercios."
            } finally {
                cargando = false
            }
        }
    }

    // Filtrado reactivo de comercios
    val comerciosFiltrados = remember(searchQuery, categoriaSeleccionada, municipioSeleccionado, listaComercios, mapaMunicipios) {
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
    val comerciosDestacados = remember(comerciosFiltrados) {
        comerciosFiltrados.filter { it.planInt >= 2 }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingValues)
    ) {
        // 1. Buscador superior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .shadow(
                        elevation = 3.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = Color.Black.copy(alpha = 0.08f),
                        spotColor = Color.Black.copy(alpha = 0.12f)
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(FondoBlancoAdaptable)
                    .border(0.8.dp, AirbnbBorder, RoundedCornerShape(28.dp))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = VerdeTurisMap,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Buscar en la Ruta de las Flores...",
                            color = AirbnbGray,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 2. Fila 1: Filtros de Categoría
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
        ) {
            items(categoriasFiltros) { cat ->
                val activa = cat.id.equals(categoriaSeleccionada, ignoreCase = true)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (activa) VerdeTurisMap else FondoBlancoAdaptable)
                        .border(
                            width = if (activa) 1.5.dp else 1.dp,
                            color = if (activa) VerdeTurisMap else AirbnbBorder,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { categoriaSeleccionada = cat.id }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = cat.icono, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = cat.etiqueta,
                        color = if (activa) Color.White else AirbnbBlack,
                        fontSize = 12.5.sp,
                        fontWeight = if (activa) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // 3. Fila 2: Filtros de Pueblos / Municipios
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
        ) {
            items(municipiosRuta) { muni ->
                val activo = muni.equals(municipioSeleccionado, ignoreCase = true)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (activo) VerdeOscuroTurisMap.copy(alpha = 0.15f) else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (activo) VerdeTurisMap else AirbnbBorder,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { municipioSeleccionado = muni }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📍", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (muni == "Todos") "Todos los Pueblos" else muni,
                        color = if (activo) VerdeTurisMap else AirbnbGray,
                        fontSize = 12.sp,
                        fontWeight = if (activo) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        HorizontalDivider(color = AirbnbBorder, thickness = 0.8.dp)

        // 4. Feed de Comercios
        Box(modifier = Modifier.fillMaxSize()) {
            if (cargando) {
                LazyColumn(
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(3) { TarjetaEsqueletoFeed() }
                }
            } else if (errorCarga != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = errorCarga ?: "Error al conectar",
                        color = AirbnbGray,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            coroutineScope.launch {
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
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VerdeTurisMap)
                    ) {
                        Text("Reintentar", color = Color.White)
                    }
                }
            } else if (comerciosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron negocios con los filtros seleccionados.",
                        color = AirbnbGray,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Carrusel superior de Destacados
                    if (comerciosDestacados.isNotEmpty() && searchQuery.isBlank()) {
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("👑", fontSize = 16.sp)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "EXPERIENCIAS DESTACADAS",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = VerdeOscuroTurisMap,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(comerciosDestacados, key = { "dest_${it.id}" }) { destacado ->
                                        val muniNombre = mapaMunicipios[destacado.lugarId] ?: "Ruta de las Flores"
                                        TarjetaMiniDestacada(
                                            comercio = destacado,
                                            municipio = muniNombre,
                                            onClick = { onComercioClick(destacado.id) }
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            HorizontalDivider(
                                color = AirbnbBorder.copy(alpha = 0.5f),
                                thickness = 0.8.dp,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Encabezado del feed con total
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (municipioSeleccionado == "Todos") "Guía de Comercios" else "Comercios en $municipioSeleccionado",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AirbnbBlack
                            )
                            Text(
                                text = "${comerciosFiltrados.size} lugares",
                                fontSize = 12.sp,
                                color = AirbnbGray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Feed vertical continuo
                    items(comerciosFiltrados, key = { it.id }) { comercio ->
                        val muniNombre = mapaMunicipios[comercio.lugarId] ?: "Ruta de las Flores"
                        val esFavorito = favoritosUsuario.contains(comercio.id) || favoritosUsuario.contains("comercio:${comercio.id}")

                        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                            TarjetaComercioInstagram(
                                comercio = comercio,
                                municipio = muniNombre,
                                esFavorito = esFavorito,
                                onToggleFavorito = { onToggleFavorito(comercio.id, !esFavorito) },
                                onClick = { onComercioClick(comercio.id) },
                                onAccionContacto = {
                                    val tel = comercio.telefono.filter { it.isDigit() }
                                    if (tel.isNotBlank()) onLlamarComercio(comercio.telefono)
                                }
                            )
                        }
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
            )
        }
    }
}

// ---------------------------------------------------------
// TARJETA PRINCIPAL CON LÍMITE DE FOTOS (2 BÁSICO / 4 SOCIO / 8 PREMIUM)
// ---------------------------------------------------------
@Composable
fun TarjetaComercioInstagram(
    comercio: Comercio,
    municipio: String,
    esFavorito: Boolean,
    onToggleFavorito: () -> Unit,
    onClick: () -> Unit,
    onAccionContacto: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val esOscuro = isSystemInDarkTheme()

    // Respeta el tope exacto según suscripción
    val fotosReales = comercio.imagenUrls.filter { it.startsWith("http://") || it.startsWith("https://") }
    val fotos = fotosReales.take(comercio.maxFotosPermitidas)

    val esPremium = comercio.planInt == 3
    val esSocio = comercio.planInt == 2

    val precioTexto = comercio.precioDesde?.takeIf { it > 0.0 }?.let { "$$it USD" } ?: "Precios accesibles"

    val calificacion = if (comercio.calificacionPromedio != null && comercio.calificacionPromedio > 0.0) {
        val entero = comercio.calificacionPromedio.toInt()
        val dec = ((comercio.calificacionPromedio - entero) * 10).toInt()
        "$entero.$dec"
    } else "Nuevo"

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = FondoBlancoAdaptable),
        border = BorderStroke(
            width = if (esPremium) 1.5.dp else 1.dp,
            color = if (esPremium) VerdeTurisMap.copy(alpha = 0.7f) else AirbnbBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (esPremium) 4.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                if (fotos.isNotEmpty()) {
                    val pagerState = rememberPagerState(pageCount = { fotos.size })

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { pagina ->
                        RemoteImage(
                            url = fotos[pagina],
                            contentDescription = comercio.nombre,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
                                )
                            )
                    )

                    // Círculos indicadores que reflejan el número exacto de fotos permitidas
                    if (fotos.size > 1) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 10.dp)
                                .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(fotos.size) { index ->
                                val esActivo = pagerState.currentPage == index
                                Box(
                                    modifier = Modifier
                                        .size(if (esActivo) 7.dp else 5.dp)
                                        .clip(CircleShape)
                                        .background(if (esActivo) Color.White else Color.White.copy(alpha = 0.45f))
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AirbnbLightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(comercio.categoria, color = AirbnbGray, fontWeight = FontWeight.Bold)
                    }
                }

                // Insignias superiores
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (esPremium) {
                        Surface(
                            color = VerdeTurisMap,
                            shape = RoundedCornerShape(20.dp),
                            shadowElevation = 3.dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text("👑", fontSize = 12.sp)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Socio Premium",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    } else if (esSocio) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.75f),
                            border = BorderStroke(1.dp, VerdeTurisMap),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    "✓ Socio TurisMap",
                                    color = VerdeTurisMap,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                            .clickable { onToggleFavorito() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (esFavorito) AirbnbPrimary else Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            // Información inferior
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = comercio.nombre,
                            color = AirbnbBlack,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        if (esPremium) {
                            Spacer(Modifier.width(6.dp))
                            Text("👑", fontSize = 14.sp)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = VerdeTurisMap,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = calificacion,
                            color = AirbnbBlack,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📍", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$municipio · ${comercio.categoria}",
                        color = AirbnbGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = precioTexto,
                        color = AirbnbBlack,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (comercio.telefono.isNotBlank()) {
                        val telLimpio = comercio.telefono.filter { it.isDigit() }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (esSocio || esPremium) {
                                Surface(
                                    color = VerdeTurisMap.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable {
                                        val url = if (telLimpio.startsWith("503")) "https://wa.me/$telLimpio" else "https://wa.me/503$telLimpio"
                                        try { uriHandler.openUri(url) } catch (e: Exception) { onAccionContacto() }
                                    }
                                ) {
                                    Text(
                                        text = "💬 WhatsApp",
                                        color = VerdeOscuroTurisMap,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Surface(
                                color = if (esOscuro) Color(0xFF2C2C2C) else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable { onAccionContacto() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Llamar",
                                    tint = AirbnbBlack,
                                    modifier = Modifier.padding(6.dp).size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// TARJETA DEL CARRUSEL SUPERIOR DESTACADO
// ---------------------------------------------------------
@Composable
fun TarjetaMiniDestacada(
    comercio: Comercio,
    municipio: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FondoBlancoAdaptable),
        border = BorderStroke(1.dp, VerdeTurisMap.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                RemoteImage(
                    url = comercio.imagenUrls.firstOrNull() ?: "",
                    contentDescription = comercio.nombre,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (comercio.planInt == 3) {
                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Text("👑 Socio Premium", color = VerdeTurisMap, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (comercio.planInt == 2) {
                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Text("✓ Socio TurisMap", color = VerdeTurisMap, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = comercio.nombre,
                    color = AirbnbBlack,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "$municipio · ${comercio.categoria}",
                    color = AirbnbGray,
                    fontSize = 11.5.sp,
                    maxLines = 1
                )
            }
        }
    }
}

// ---------------------------------------------------------
// ESQUELETO DE CARGA
// ---------------------------------------------------------
@Composable
fun TarjetaEsqueletoFeed() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
    }
}