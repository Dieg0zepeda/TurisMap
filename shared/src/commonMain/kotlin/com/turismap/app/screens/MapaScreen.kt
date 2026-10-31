package com.turismap.app.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.MapaTuristico
import com.turismap.app.components.RemoteImage
import com.turismap.app.data.models.CategoriaLugar
import com.turismap.app.data.models.Coordenada
import com.turismap.app.data.models.Lugar
import com.turismap.app.data.repository.Comercio
import com.turismap.app.data.repository.ComercioRepository
import com.turismap.app.data.repository.LugaresRepository
import kotlin.math.*
import kotlinx.coroutines.CancellationException

import androidx.compose.foundation.isSystemInDarkTheme
import com.turismap.app.theme.*

private val ColorAirbnbPrimario = Color(0xFFFF385C)
private val ColorAirbnbOscuro: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFF7F7F7) else Color(0xFF1E1E1E)
private val ColorAirbnbGris: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFA0A0A0) else Color(0xFF717171)
private val ColorAirbnbFondo: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF2C2C2C) else Color(0xFFF7F7F8)
private val ColorAirbnbBorde: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF404040) else Color(0xFFEBEBEB)

// Cálculo geodésico de distancia y tiempo estimado
private fun calcularDistanciaYTiempo(
    latDestino: Double,
    lngDestino: Double,
    latOrigen: Double = 13.9214, // Ahuachapán
    lngOrigen: Double = -89.8450
): Pair<String, String> {
    if (latDestino == 0.0 || lngDestino == 0.0) {
        return Pair("~10 min", "4.5 km")
    }
    val dLat = (latDestino - latOrigen) * PI / 180.0
    val dLng = (lngDestino - lngOrigen) * PI / 180.0
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(latOrigen * PI / 180.0) * cos(latDestino * PI / 180.0) *
            sin(dLng / 2) * sin(dLng / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    val distanciaKm = 6371.0 * c

    val tiempoMinutos = (distanciaKm / 35.0 * 60).toInt().coerceAtLeast(4)
    val entero = distanciaKm.toInt()
    val decimal = ((distanciaKm - entero) * 10).toInt()

    return Pair("$tiempoMinutos min", "$entero.$decimal km")
}

private fun resolverCoordenadasComercio(
    comercio: Comercio,
    municipiosMap: Map<String, Lugar>,
    indice: Int
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
            else -> { lat = 13.9214; lng = -89.8450 }
        }
    }

    val dispersLat = (indice % 3 - 1) * 0.0015
    val dispersLng = ((indice / 3) % 3 - 1) * 0.0015
    return Coordenada(latitud = lat + dispersLat, longitud = lng + dispersLng)
}

@Composable
fun MapaScreen(
    onAbrirDetalle: (Lugar) -> Unit = {},
    onComercioClick: (String) -> Unit = {}
) {
    val uriHandler = LocalUriHandler.current

    var textoBusqueda: String by remember { mutableStateOf("") }
    var categoriaSeleccionada: String by remember { mutableStateOf("Todos") }

    var todosLosComercios: List<Comercio> by remember { mutableStateOf(emptyList()) }
    var municipiosMap: Map<String, Lugar> by remember { mutableStateOf(emptyMap()) }
    var comercioSeleccionado: Comercio? by remember { mutableStateOf(null) }

    // MODO RUTA INTERNO DE TURISMAP
    var comercioEnRutaActiva: Comercio? by remember { mutableStateOf<Comercio?>(null) }

    var cargando: Boolean by remember { mutableStateOf(true) }
    var errorCarga: String? by remember { mutableStateOf(null) }
    var intentoCarga: Int by remember { mutableStateOf(0) }

    LaunchedEffect(intentoCarga) {
        cargando = true
        errorCarga = null
        try {
            val comerciosDb = ComercioRepository.obtenerPublicados()
            val lugaresDb = try {
                LugaresRepository.obtenerTodos().associateBy { it.id.trim() }
            } catch (e: Exception) {
                emptyMap()
            }

            todosLosComercios = comerciosDb
            municipiosMap = lugaresDb
            comercioSeleccionado = comerciosDb.firstOrNull()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            errorCarga = error.message ?: "No se pudieron cargar los datos del mapa."
        } finally {
            cargando = false
        }
    }

    val comerciosFiltrados = remember(textoBusqueda, categoriaSeleccionada, todosLosComercios) {
        todosLosComercios.filter { com ->
            val coincideCategoria = categoriaSeleccionada.equals("Todos", ignoreCase = true) ||
                    com.categoria.equals(categoriaSeleccionada, ignoreCase = true)
            val coincideBusqueda = textoBusqueda.isBlank() ||
                    com.nombre.contains(textoBusqueda, ignoreCase = true) ||
                    com.direccion.contains(textoBusqueda, ignoreCase = true) ||
                    com.categoria.contains(textoBusqueda, ignoreCase = true)

            coincideCategoria && coincideBusqueda
        }
    }

    val lugaresParaElMapa = remember(comerciosFiltrados, municipiosMap) {
        comerciosFiltrados.mapIndexed { index, com ->
            val coords = resolverCoordenadasComercio(com, municipiosMap, index)
            Lugar(
                id = com.id,
                nombre = com.nombre,
                subtitulo = com.categoria.replaceFirstChar { it.uppercase() },
                categoria = CategoriaLugar.TODOS,
                ubicacionCoordenadas = coords,
                ubicacionCorta = com.direccion,
                ubicacionLarga = com.direccion,
                calificacion = com.calificacionPromedio ?: 4.9,
                cantidadResenas = com.cantidadResenas ?: 12,
                historia = com.descripcion,
                imagenPortadaUrl = com.imagenUrls.firstOrNull() ?: ""
            )
        }
    }

    val categoriasPildoras = listOf("Todos", "restaurante", "hotel", "artesania", "atractivo")

    Box(modifier = Modifier.fillMaxSize()) {
        // MAPA TURÍSTICO
        MapaTuristico(
            lugares = lugaresParaElMapa,
            onLugarClick = { lugarTocado ->
                comercioSeleccionado = todosLosComercios.find { it.id == lugarTocado.id }
            },
            modifier = Modifier.fillMaxSize()
        )

        // MODO NAVEGACIÓN ACTIVA (PANEL SUPERIOR TIPO UBER / WAZE)
        if (comercioEnRutaActiva != null) {
            val c = comercioEnRutaActiva!!
            val coords = resolverCoordenadasComercio(c, municipiosMap, 0)
            val (tiempo, dist) = calcularDistanciaYTiempo(coords.latitud, coords.longitud)

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = ColorAirbnbOscuro,
                shadowElevation = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(ColorAirbnbPrimario),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Rumbo a ${c.nombre}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "🚗 Llegada estimada en $tiempo ($dist)",
                            color = Color(0xFFB0B0B0),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { 
                            val url = "https://www.google.com/maps/dir/?api=1&destination=${coords.latitud},${coords.longitud}"
                            try { uriHandler.openUri(url) } catch (e: Exception) {}
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorAirbnbPrimario),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Maps", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Botón para finalizar ruta
                    Button(
                        onClick = { comercioEnRutaActiva = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Salir", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        } else {
            // PANEL HABITUAL: Header + Buscador + Filtros
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                // Header con "TURISMAP" y Avatar limpio (sin el "CH")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TURISMAP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = VerdeTextoAdaptable,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Mapa",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorAirbnbOscuro
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = FondoBlancoAdaptable,
                            shadowElevation = 3.dp,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notificaciones",
                                    tint = ColorAirbnbOscuro,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Avatar de usuario limpio
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF1B5E20),
                            shadowElevation = 3.dp,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Perfil",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Buscador flotante
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .shadow(4.dp, RoundedCornerShape(30.dp))
                            .clip(RoundedCornerShape(30.dp))
                            .background(FondoBlancoAdaptable)
                            .border(1.dp, ColorAirbnbBorde, RoundedCornerShape(30.dp))
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = ColorAirbnbOscuro,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        TextField(
                            value = textoBusqueda,
                            onValueChange = { textoBusqueda = it },
                            placeholder = {
                                Text(
                                    "Buscar cafés, hoteles, tiendas...",
                                    color = ColorAirbnbGris,
                                    fontSize = 13.sp
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

                        if (textoBusqueda.isNotBlank()) {
                            IconButton(
                                onClick = { textoBusqueda = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpiar",
                                    tint = ColorAirbnbGris,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Píldoras de Categoría
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categoriasPildoras) { cat ->
                        val isSelected = cat.equals(categoriaSeleccionada, ignoreCase = true)
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .clickable { categoriaSeleccionada = cat },
                            color = if (isSelected) ColorAirbnbOscuro else FondoBlancoAdaptable,
                            shape = RoundedCornerShape(24.dp),
                            border = if (!isSelected) BorderStroke(1.dp, ColorAirbnbBorde) else null,
                            shadowElevation = if (isSelected) 3.dp else 1.dp
                        ) {
                            Text(
                                text = cat.replaceFirstChar { it.uppercase() },
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) FondoBlancoAdaptable else ColorAirbnbOscuro,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // TARJETA FLOTANTE (ESTILO AIRBNB ARREGLADO)
        // ==========================================
        AnimatedVisibility(
            visible = comercioSeleccionado != null && comercioEnRutaActiva == null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            comercioSeleccionado?.let { comercio ->
                val coords = resolverCoordenadasComercio(comercio, municipiosMap, 0)
                val (tiempoViaje, distanciaKm) = calcularDistanciaYTiempo(coords.latitud, coords.longitud)

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = FondoBlancoAdaptable),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    border = BorderStroke(1.dp, ColorAirbnbBorde),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Ta'anga (Foto)
                            Box(
                                modifier = Modifier
                                    .size(86.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(ColorAirbnbFondo)
                            ) {
                                RemoteImage(
                                    url = comercio.imagenUrls.firstOrNull() ?: "",
                                    contentDescription = comercio.nombre,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Marandu (Info)
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = ColorAirbnbOscuro.copy(alpha = 0.08f)
                                    ) {
                                        Text(
                                            text = comercio.categoria.replaceFirstChar { it.uppercase() },
                                            color = ColorAirbnbOscuro,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { comercioSeleccionado = null },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Cerrar",
                                            tint = ColorAirbnbGris,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = comercio.nombre,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorAirbnbOscuro,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB800),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${comercio.calificacionPromedio ?: 4.9} · ${comercio.direccion.ifBlank { "Ruta de las Flores" }}",
                                        fontSize = 12.sp,
                                        color = ColorAirbnbGris,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Tiempo ha Distancia
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFE8F5E9)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Place,
                                            contentDescription = null,
                                            tint = Color(0xFF2E7D32),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "$tiempoViaje ($distanciaKm)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = ColorAirbnbBorde, thickness = 0.8.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // UMI BOTÓN OÑEMOHENDA PORÃVA
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (comercio.telefono.isNotBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        val telLimpio = comercio.telefono.filter { it.isDigit() }
                                        if (telLimpio.isNotBlank()) {
                                            try { uriHandler.openUri("tel:$telLimpio") } catch (_: Exception) {}
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, ColorAirbnbBorde),
                                    contentPadding = PaddingValues(10.dp),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = "Llamar",
                                        tint = ColorAirbnbOscuro,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Botón "Iniciar ruta" (Modo interno TurisMap)
                            Button(
                                onClick = {
                                    comercioEnRutaActiva = comercio
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ColorAirbnbPrimario),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 11.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Iniciar ruta",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            // Botón "Detalles"
                            Button(
                                onClick = {
                                    val id = comercio.id
                                    onComercioClick(id)
                                    onAbrirDetalle(
                                        Lugar(id = id, nombre = comercio.nombre, ubicacionCorta = comercio.direccion)
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ColorAirbnbOscuro),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 11.dp)
                            ) {
                                Text(
                                    text = "Detalles",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FondoBlancoAdaptable
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}