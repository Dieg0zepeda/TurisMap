package com.turismap.app.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
private val ColorPremium = Color(0xFFD4AF37)
private val ColorDestacado = Color(0xFF1B5E20)

val Comercio.planInt: Int
    get() {
        val plan = (tipoPlan ?: "").lowercase()
        return when {
            plan.contains("premium") || plan.contains("25") -> 3
            plan.contains("socio") || plan.contains("15") -> 2
            else -> 1
        }
    }

private fun formatearCalificacion(cal: Double?): String {
    if (cal == null || cal <= 0.0) return "4.9"
    val entero = cal.toInt()
    val decimal = ((cal - entero) * 10).toInt()
    return "$entero.$decimal"
}

private data class CategoriaFiltro(val id: String, val etiqueta: String, val icono: String)

private val categoriasFiltros = listOf(
    CategoriaFiltro("Todos", "Todo", "🧭"),
    CategoriaFiltro("restaurante", "Restaurantes", "☕"),
    CategoriaFiltro("hotel", "Alojamientos", "🏨"),
    CategoriaFiltro("artesania", "Artesanías", "🏺"),
    CategoriaFiltro("atractivo", "Experiencias", "🎈")
)

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

    var listaComercios: List<Comercio> by remember { mutableStateOf(emptyList()) }
    var mapaMunicipios: Map<String, String> by remember { mutableStateOf(emptyMap()) }
    var cargando by remember { mutableStateOf(comerciosExternos == null) }
    var errorCarga by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // 1. Observación reactiva de favoritos del usuario autenticado
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

    val comerciosFiltrados = remember(searchQuery, categoriaSeleccionada, listaComercios) {
        listaComercios.filter { item ->
            val coincideCategoria = categoriaSeleccionada.equals("Todos", ignoreCase = true) ||
                    item.categoria.equals(categoriaSeleccionada, ignoreCase = true)
            val coincideBusqueda = searchQuery.isBlank() ||
                    item.nombre.contains(searchQuery, ignoreCase = true) ||
                    item.direccion.contains(searchQuery, ignoreCase = true) ||
                    item.descripcion.contains(searchQuery, ignoreCase = true)
            coincideCategoria && coincideBusqueda
        }
    }

    val gruposPorMunicipio = remember(comerciosFiltrados, mapaMunicipios) {
        comerciosFiltrados
            .sortedByDescending { it.planInt }
            .groupBy { item ->
                val nombre = mapaMunicipios[item.lugarId]
                if (!nombre.isNullOrBlank()) nombre else "Ruta de las Flores"
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingValues)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(32.dp),
                        ambientColor = Color.Black.copy(alpha = 0.08f),
                        spotColor = Color.Black.copy(alpha = 0.12f)
                    )
                    .clip(RoundedCornerShape(32.dp))
                    .background(FondoBlancoAdaptable)
                    .border(0.8.dp, AirbnbBorder, RoundedCornerShape(32.dp))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = AirbnbBlack,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Buscar restaurantes, hoteles o tiendas...",
                            color = AirbnbBlack,
                            fontSize = 15.sp,
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

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            items(categoriasFiltros) { cat ->
                val activa = cat.id.equals(categoriaSeleccionada, ignoreCase = true)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (activa) AirbnbLightGray else FondoBlancoAdaptable)
                        .border(
                            width = if (activa) 1.5.dp else 1.dp,
                            color = if (activa) AirbnbBlack else AirbnbBorder,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .clickable { categoriaSeleccionada = cat.id }
                        .padding(horizontal = 16.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = cat.icono, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = cat.etiqueta,
                        color = AirbnbBlack,
                        fontSize = 13.sp,
                        fontWeight = if (activa) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        HorizontalDivider(color = AirbnbBorder, thickness = 0.8.dp)

        Box(modifier = Modifier.fillMaxSize()) {
            if (cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AirbnbPrimary
                )
            } else if (errorCarga != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
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
                        colors = ButtonDefaults.buttonColors(containerColor = AirbnbBlack)
                    ) {
                        Text("Reintentar")
                    }
                }
            } else if (comerciosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron negocios para esta búsqueda.",
                        color = AirbnbGray,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(28.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    gruposPorMunicipio.forEach { (nombreMunicipio, comerciosEnMunicipio) ->
                        item(key = nombreMunicipio) {
                            SeccionMunicipioAirbnb(
                                titulo = "Lugares en $nombreMunicipio",
                                comercios = comerciosEnMunicipio,
                                favoritosIds = favoritosUsuario,
                                onComercioClick = { id -> onComercioClick(id) },
                                onActionClick = onLlamarComercio,
                                onToggleFavorito = onToggleFavorito
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

@Composable
fun SeccionMunicipioAirbnb(
    titulo: String,
    comercios: List<Comercio>,
    favoritosIds: Set<String>,
    onComercioClick: (String) -> Unit,
    onActionClick: (String) -> Unit,
    onToggleFavorito: (String, Boolean) -> Unit
) {
    val uriHandler = LocalUriHandler.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = titulo,
                modifier = Modifier.weight(1f),
                color = AirbnbBlack,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(comercios, key = { it.id }) { comercio ->
                val esFavorito = favoritosIds.contains(comercio.id) || favoritosIds.contains("comercio:${comercio.id}")

                when (comercio.planInt) {
                    3 -> TarjetaPremium(
                        comercio = comercio,
                        esFavorito = esFavorito,
                        onToggleFavorito = { onToggleFavorito(comercio.id, !esFavorito) },
                        onClick = { onComercioClick(comercio.id) },
                        onQuickActionClick = {
                            val tel = comercio.telefono.filter { it.isDigit() }
                            if (tel.isNotBlank()) {
                                val url = if (tel.startsWith("503")) "https://wa.me/$tel" else "https://wa.me/503$tel"
                                try {
                                    uriHandler.openUri(url)
                                } catch (e: Exception) {
                                    onActionClick(comercio.telefono)
                                }
                            }
                        }
                    )
                    2 -> TarjetaEstandar(
                        comercio = comercio,
                        esFavorito = esFavorito,
                        onToggleFavorito = { onToggleFavorito(comercio.id, !esFavorito) },
                        onClick = { onComercioClick(comercio.id) }
                    )
                    else -> TarjetaBasica(
                        comercio = comercio,
                        onClick = { onComercioClick(comercio.id) }
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------
// PLAN 3: SOCIO PREMIUM ($25) - Tarjeta VIP
// ---------------------------------------------------------
@Composable
fun TarjetaPremium(
    comercio: Comercio,
    esFavorito: Boolean,
    onToggleFavorito: () -> Unit,
    onClick: () -> Unit,
    onQuickActionClick: () -> Unit
) {
    val precio = comercio.precioDesde?.takeIf { it > 0.0 }?.let { "$$it USD" } ?: "Consultar precio"
    val calificacion = formatearCalificacion(comercio.calificacionPromedio)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = FondoBlancoAdaptable),
        border = BorderStroke(1.5.dp, ColorPremium.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier.width(280.dp).clickable(onClick = onClick)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                RemoteImage(
                    url = comercio.imagenUrls.firstOrNull() ?: "",
                    contentDescription = comercio.nombre,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Insignia VIP
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .align(Alignment.TopStart)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👑", fontSize = 12.sp)
                        Spacer(Modifier.width(4.dp))
                        Text("Favorito de la Ruta", color = ColorPremium, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Botón Favorito reactivo
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .size(34.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                        .clickable { onToggleFavorito() }
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (esFavorito) AirbnbPrimary else AirbnbBlack,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Tag Fotos HD
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Fotos HD", color = Color.White, fontSize = 10.sp)
                    }
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(comercio.nombre, color = AirbnbBlack, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.Star, contentDescription = null, tint = AirbnbBlack, modifier = Modifier.size(14.dp))
                    Text(calificacion, color = AirbnbBlack, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Text("${comercio.categoria} · ${comercio.cantidadResenas ?: 0} reseñas", color = AirbnbGray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(precio, color = AirbnbBlack, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onQuickActionClick,
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AirbnbBlack),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("WhatsApp / Contacto", fontSize = 13.sp)
                }
            }
        }
    }
}

// ---------------------------------------------------------
// PLAN 2: SOCIO TURISMAP ($15) - Tarjeta Estándar
// ---------------------------------------------------------
@Composable
fun TarjetaEstandar(
    comercio: Comercio,
    esFavorito: Boolean,
    onToggleFavorito: () -> Unit,
    onClick: () -> Unit
) {
    val precio = comercio.precioDesde?.takeIf { it > 0.0 }?.let { "$$it USD" } ?: "Consultar precio"
    val calificacion = formatearCalificacion(comercio.calificacionPromedio)

    Column(
        modifier = Modifier.width(220.dp).clip(RoundedCornerShape(18.dp)).clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(18.dp)).background(AirbnbLightGray)) {
            RemoteImage(
                url = comercio.imagenUrls.firstOrNull() ?: "",
                contentDescription = comercio.nombre,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Badge Verde Destacado
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, start = 12.dp)
                    .background(ColorDestacado, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .align(Alignment.TopStart)
            ) {
                Text("★ Destacado", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // Botón Favorito reactivo
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, end = 10.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .clickable { onToggleFavorito() }
                    .align(Alignment.TopEnd),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorito",
                    tint = if (esFavorito) AirbnbPrimary else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(Color(0xFF4CAF50), CircleShape))
            Spacer(Modifier.width(6.dp))
            Text(comercio.nombre, color = AirbnbBlack, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Text("$precio por consumo", color = AirbnbGray, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = null, tint = AirbnbBlack, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(calificacion, color = AirbnbBlack, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(" · ${comercio.cantidadResenas ?: 0} reseñas", color = AirbnbGray, fontSize = 12.sp)
        }
    }
}

// ---------------------------------------------------------
// PLAN 1: BÁSICO ($5) - Tarjeta Simple
// ---------------------------------------------------------
@Composable
fun TarjetaBasica(comercio: Comercio, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = FondoBlancoAdaptable),
        border = BorderStroke(1.dp, AirbnbBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.width(260.dp).clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)).background(AirbnbLightGray)) {
                RemoteImage(
                    url = comercio.imagenUrls.firstOrNull() ?: "",
                    contentDescription = comercio.nombre,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(comercio.nombre, color = AirbnbBlack, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(comercio.categoria, color = AirbnbGray, fontSize = 12.sp)
            }
        }
    }
}