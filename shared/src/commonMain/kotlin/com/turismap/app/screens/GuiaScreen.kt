package com.turismap.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalUriHandler
import com.turismap.app.components.RemoteImage
import com.turismap.app.data.repository.Comercio
import com.turismap.app.data.repository.ComercioRepository
import com.turismap.app.data.repository.LugaresRepository
import kotlinx.coroutines.launch

import androidx.compose.foundation.isSystemInDarkTheme
import com.turismap.app.theme.*

// Paleta oficial de Airbnb (Modo oscuro adaptativo)
private val AirbnbBlack: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFF7F7F7) else Color(0xFF222222)
private val AirbnbGray: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFA0A0A0) else Color(0xFF717171)
private val AirbnbLightGray: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF2C2C2C) else Color(0xFFF7F7F7)
private val AirbnbBorder: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF404040) else Color(0xFFEBEBEB)
private val AirbnbPrimary = Color(0xFFFF385C)

// Formateo numérico en Kotlin puro sin depender de librerías Java
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
    var comercioSeleccionado by remember { mutableStateOf<Comercio?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var listaComercios: List<Comercio> by remember { mutableStateOf(emptyList()) }
    var mapaMunicipios: Map<String, String> by remember { mutableStateOf(emptyMap()) }
    var cargando by remember { mutableStateOf(comerciosExternos == null) }
    var errorCarga by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

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
        comerciosFiltrados.groupBy { item ->
            val nombre = mapaMunicipios[item.lugarId]
            if (!nombre.isNullOrBlank()) nombre else "Ruta de las Flores"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
            .padding(paddingValues)
    ) {
            // Buscador estilo cápsula flotante
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
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
                                "Empieza la búsqueda",
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

            // Píldoras de filtros de categoría
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

            // Contenedor principal
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
                                    onComercioClick = { comercioSeleccionado = it }
                                )
                            }
                        }
                    }
                }
        }
    }

    // Modal Bottom Sheet interactivo
    if (comercioSeleccionado != null) {
        ModalBottomSheet(
            onDismissRequest = { comercioSeleccionado = null },
            sheetState = sheetState,
            containerColor = FondoBlancoAdaptable,
            dragHandle = { BottomSheetDefaults.DragHandle(color = AirbnbBorder) }
        ) {
            VentanaDetalleAirbnb(
                comercio = comercioSeleccionado!!,
                onCerrar = { comercioSeleccionado = null },
                onLlamar = { onLlamarComercio(comercioSeleccionado!!.telefono) },
                onIniciarRuta = {
                    val c = comercioSeleccionado!!
                    val municipio = mapaMunicipios[c.lugarId.trim()] ?: ""
                    val query = "${c.nombre} ${c.direccion} $municipio El Salvador"
                        .replace(" ", "+").replace(",", "")
                    val url = "https://www.google.com/maps/dir/?api=1&destination=$query"
                    try { uriHandler.openUri(url) } catch (e: Exception) {}
                },
                onVerDetalleCompleto = {
                    val id = comercioSeleccionado!!.id
                    comercioSeleccionado = null
                    onComercioClick(id)
                }
            )
        }
    }
}

@Composable
fun SeccionMunicipioAirbnb(
    titulo: String,
    comercios: List<Comercio>,
    onComercioClick: (Comercio) -> Unit
) {
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
                color = AirbnbBlack,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
            )

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AirbnbLightGray)
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Ver todos",
                    tint = AirbnbBlack,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(comercios, key = { it.id }) { comercio ->
                TarjetaMiniAirbnb(
                    comercio = comercio,
                    onClick = { onComercioClick(comercio) }
                )
            }
        }
    }
}

@Composable
fun TarjetaMiniAirbnb(
    comercio: Comercio,
    onClick: () -> Unit
) {
    var esFavorito by remember { mutableStateOf(false) }
    val precio = comercio.precioDesde?.takeIf { it > 0.0 }?.let { "$$it USD" } ?: "Consultar precio"
    val calificacion = formatearCalificacion(comercio.calificacionPromedio)

    Column(
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(AirbnbLightGray)
        ) {
            RemoteImage(
                url = comercio.imagenUrls.firstOrNull() ?: "",
                contentDescription = comercio.nombre,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .padding(top = 12.dp, start = 12.dp)
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(FondoBlancoAdaptable)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .align(Alignment.TopStart)
            ) {
                Text(
                    text = "Favorito de la Ruta",
                    color = AirbnbBlack,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .padding(top = 10.dp, end = 10.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .clickable { esFavorito = !esFavorito }
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

        Text(
            text = comercio.nombre,
            color = AirbnbBlack,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "$precio por consumo",
            color = AirbnbGray,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = AirbnbBlack,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = calificacion,
                color = AirbnbBlack,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun VentanaDetalleAirbnb(
    comercio: Comercio,
    onCerrar: () -> Unit,
    onLlamar: () -> Unit,
    onIniciarRuta: () -> Unit,
    onVerDetalleCompleto: () -> Unit = {}
) {
    val calificacion = formatearCalificacion(comercio.calificacionPromedio)
    val resenas = comercio.cantidadResenas ?: 14
    val precio = comercio.precioDesde?.takeIf { it > 0.0 }?.let { "$$it USD" } ?: "Consultar"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
            .background(FondoBlancoAdaptable)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(AirbnbLightGray)
                ) {
                    RemoteImage(
                        url = comercio.imagenUrls.firstOrNull() ?: "",
                        contentDescription = comercio.nombre,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    IconButton(
                        onClick = onCerrar,
                        modifier = Modifier
                            .padding(16.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FondoBlancoAdaptable.copy(alpha = 0.9f))
                            .align(Alignment.TopStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = AirbnbBlack,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = comercio.nombre,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AirbnbBlack
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AirbnbBlack,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$calificacion · $resenas reseñas · ",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = AirbnbBlack
                        )
                        Text(
                            text = comercio.categoria.replaceFirstChar { it.uppercase() },
                            color = AirbnbGray,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = AirbnbBorder)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Tarjeta de Ubicación con acción de iniciar ruta
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AirbnbLightGray)
                            .border(1.dp, AirbnbBorder, RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(FondoBlancoAdaptable),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = AirbnbPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ubicación en la Ruta",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = AirbnbBlack
                            )
                            Text(
                                text = comercio.direccion.ifBlank { "Ruta de las Flores, El Salvador" },
                                fontSize = 12.sp,
                                color = AirbnbGray,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onIniciarRuta,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AirbnbBlack),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = FondoBlancoAdaptable,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Ruta",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = FondoBlancoAdaptable
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Acerca de este lugar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AirbnbBlack
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = comercio.descripcion.ifBlank { "Un espacio acogedor en la Ruta de las Flores preparado para atenderte." },
                        fontSize = 14.sp,
                        color = AirbnbGray,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (comercio.servicios.isNotEmpty()) {
                        Text(
                            text = "Servicios y comodidades",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AirbnbBlack
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            comercio.servicios.forEach { servicio ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(AirbnbBlack)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = servicio,
                                        fontSize = 14.sp,
                                        color = AirbnbBlack
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    if (comercio.horario.isNotBlank()) {
                        Text(
                            text = "Horario",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AirbnbBlack
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = comercio.horario,
                            fontSize = 14.sp,
                            color = AirbnbGray
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Barra inferior fija con botones balanceados
        HorizontalDivider(color = AirbnbBorder, thickness = 0.8.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(FondoBlancoAdaptable)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Desde $precio",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AirbnbBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "por consumo",
                    fontSize = 12.sp,
                    color = AirbnbGray
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (comercio.telefono.isNotBlank()) {
                    IconButton(
                        onClick = onLlamar,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, AirbnbBorder, RoundedCornerShape(10.dp))
                            .background(AirbnbLightGray)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Llamar",
                            tint = AirbnbBlack,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Botón Iniciar Ruta destacado
                Button(
                    onClick = onIniciarRuta,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AirbnbPrimary),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Iniciar ruta",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}