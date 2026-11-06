package com.turismap.app.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

import androidx.compose.foundation.isSystemInDarkTheme
import com.turismap.app.theme.*

// Paleta corporativa corregida
private val ColorTurismapPrimario = Color(0xFF1B5E20)
private val ColorAirbnbOscuro: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFF7F7F7) else Color(0xFF1E1E1E)
private val ColorAirbnbGris: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFA0A0A0) else Color(0xFF717171)
private val ColorAirbnbFondo: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF2C2C2C) else Color(0xFFF7F7F8)
private val ColorAirbnbBorde: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF404040) else Color(0xFFEBEBEB)

private fun resolverCoordenadasComercio(
    comercio: Comercio,
    municipiosMap: Map<String, Lugar>
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
            else -> { lat = 0.0; lng = 0.0 }
        }
    }

    return Coordenada(latitud = lat, longitud = lng)
}

private fun codificarParametroMaps(valor: String): String =
    valor.encodeToByteArray().joinToString("") { byte ->
        val caracter = byte.toInt() and 0xFF
        if (caracter in 'a'.code..'z'.code ||
            caracter in 'A'.code..'Z'.code ||
            caracter in '0'.code..'9'.code ||
            caracter == '-'.code || caracter == '_'.code ||
            caracter == '.'.code || caracter == '~'.code
        ) {
            caracter.toChar().toString()
        } else {
            "%" + caracter.toString(16).uppercase().padStart(2, '0')
        }
    }

private fun urlGoogleMaps(comercio: Comercio?, lugar: Lugar, municipio: String?): String {
    val destino = if (comercio == null) {
        listOf(lugar.nombre, lugar.ubicacionCorta, "El Salvador")
    } else {
        listOf(comercio.nombre, comercio.direccion, municipio, "El Salvador")
    }.filterNotNull().filter(String::isNotBlank).joinToString(", ")

    return "https://www.google.com/maps/dir/?api=1&destination=${codificarParametroMaps(destino)}"
}

@Composable
private fun LeyendaMarcador(inicial: String, etiqueta: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .border(1.dp, Color.White, CircleShape)
                .background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = inicial,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(etiqueta, color = ColorAirbnbOscuro, fontSize = 11.sp)
    }
}

@Composable
fun MapaScreen(
    onAbrirDetalle: (Lugar) -> Unit = {},
    onComercioClick: (String) -> Unit = {}
) {
    val uriHandler = LocalUriHandler.current

    var textoBusqueda: String by remember { mutableStateOf("") }
    var filtroTipoMarcadores by remember { mutableStateOf("Destinos") }

    var lugaresTuristicos: List<Lugar> by remember { mutableStateOf(emptyList()) }
    var todosLosComercios: List<Comercio> by remember { mutableStateOf(emptyList()) }
    var municipiosMap: Map<String, Lugar> by remember { mutableStateOf(emptyMap()) }
    var lugarSeleccionado: Lugar? by remember { mutableStateOf(null) }

    var cargando: Boolean by remember { mutableStateOf(true) }
    var errorCarga: String? by remember { mutableStateOf(null) }
    var intentoCarga: Int by remember { mutableStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(intentoCarga) {
        cargando = true
        errorCarga = null
        val erroresCarga = mutableListOf<String>()
        try {
            lugaresTuristicos = LugaresRepository.obtenerTodos()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            erroresCarga += error.message ?: "No se pudieron cargar los destinos turísticos."
        }
        try {
            todosLosComercios = ComercioRepository.obtenerPublicados()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            erroresCarga += error.message ?: "No se pudieron cargar los comercios publicados."
        }
        municipiosMap = lugaresTuristicos.associateBy { it.id.trim() }
        errorCarga = erroresCarga.takeIf { it.isNotEmpty() }?.joinToString("\n")
        if (lugaresTuristicos.isEmpty() && todosLosComercios.isEmpty() && errorCarga == null) {
            errorCarga = "No hay destinos turísticos ni comercios publicados para mostrar."
        }
        cargando = false
    }

    LaunchedEffect(errorCarga) {
        errorCarga?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
        }
    }

    val comerciosFiltrados = remember(textoBusqueda, todosLosComercios) {
        todosLosComercios.filter { comercio ->
            val coincideBusqueda = textoBusqueda.isBlank() ||
                    comercio.nombre.contains(textoBusqueda, ignoreCase = true) ||
                    comercio.direccion.contains(textoBusqueda, ignoreCase = true) ||
                    comercio.categoria.contains(textoBusqueda, ignoreCase = true) ||
                    comercio.descripcion.contains(textoBusqueda, ignoreCase = true)

            coincideBusqueda
        }
    }

    val lugaresFiltrados = remember(textoBusqueda, lugaresTuristicos) {
        lugaresTuristicos.filter { lugar ->
            val coincideBusqueda = textoBusqueda.isBlank() ||
                    lugar.nombre.contains(textoBusqueda, ignoreCase = true) ||
                    lugar.ubicacionCorta.contains(textoBusqueda, ignoreCase = true) ||
                    lugar.historia.contains(textoBusqueda, ignoreCase = true)

            coincideBusqueda
        }
    }

    val lugaresParaElMapa = remember(
        lugaresFiltrados,
        comerciosFiltrados,
        municipiosMap,
        filtroTipoMarcadores
    ) {
        val lugaresDestino = if (filtroTipoMarcadores != "Comercios") lugaresFiltrados else emptyList()
        val lugaresComerciales = if (filtroTipoMarcadores != "Destinos") {
            comerciosFiltrados.map { comercio ->
                val municipio = municipiosMap[comercio.lugarId.trim()]
                Lugar(
                    id = "comercio:${comercio.id}",
                    nombre = comercio.nombre,
                    subtitulo = comercio.categoria.replaceFirstChar { it.uppercase() },
                    categoria = CategoriaLugar.TODOS,
                    ubicacionCoordenadas = resolverCoordenadasComercio(comercio, municipiosMap),
                    ubicacionCorta = comercio.direccion,
                    ubicacionLarga = municipio?.nombre.orEmpty(),
                    calificacion = comercio.calificacionPromedio ?: 0.0,
                    cantidadResenas = comercio.cantidadResenas ?: 0,
                    historia = comercio.descripcion,
                    imagenPortadaUrl = comercio.imagenUrls.firstOrNull().orEmpty()
                )
            }
        } else {
            emptyList()
        }
        (lugaresDestino + lugaresComerciales).filter {
            it.ubicacionCoordenadas.latitud != 0.0 && it.ubicacionCoordenadas.longitud != 0.0
        }
    }

    val comercioSeleccionado = remember(lugarSeleccionado, todosLosComercios) {
        lugarSeleccionado?.id
            ?.takeIf { it.startsWith("comercio:") }
            ?.removePrefix("comercio:")
            ?.let { id -> todosLosComercios.firstOrNull { it.id == id } }
    }

    val filtrosMarcadores = listOf(
        "Destinos" to lugaresFiltrados.size,
        "Comercios" to comerciosFiltrados.size,
        "Todos" to lugaresFiltrados.size + comerciosFiltrados.size
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val alturaMaximaTarjeta = maxHeight * 0.65f

        MapaTuristico(
            lugares = lugaresParaElMapa,
            onLugarClick = { lugarTocado -> lugarSeleccionado = lugarTocado },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        ) {
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .heightIn(min = 50.dp)
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
                    onValueChange = {
                        textoBusqueda = it
                        lugarSeleccionado = null
                    },
                    placeholder = {
                        Text(
                            "Buscar destinos y comercios...",
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
                        onClick = {
                            textoBusqueda = ""
                            lugarSeleccionado = null
                        },
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LeyendaMarcador(
                    inicial = "D",
                    etiqueta = "Destino",
                    color = Color(0xFF1B4934)
                )
                Text(
                    text = "Comercios: restaurantes, tiendas, hoteles y más",
                    color = ColorAirbnbOscuro,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtrosMarcadores) { (filtro, cantidad) ->
                    val seleccionado = filtroTipoMarcadores == filtro
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .clickable {
                                filtroTipoMarcadores = filtro
                                lugarSeleccionado = null
                            },
                        color = if (seleccionado) ColorAirbnbOscuro else FondoBlancoAdaptable,
                        shape = RoundedCornerShape(24.dp),
                        border = if (!seleccionado) BorderStroke(1.dp, ColorAirbnbBorde) else null,
                        shadowElevation = if (seleccionado) 3.dp else 1.dp
                    ) {
                        Text(
                            text = "$filtro ($cantidad)",
                            fontSize = 13.sp,
                            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium,
                            color = if (seleccionado) FondoBlancoAdaptable else ColorAirbnbOscuro,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                        )
                    }
                }

            }
        }
        AnimatedVisibility(
            visible = lugarSeleccionado != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            lugarSeleccionado?.let { lugar ->
                val comercio = comercioSeleccionado
                val municipio = comercio?.let { municipiosMap[it.lugarId.trim()]?.nombre }
                    ?: lugar.ubicacionLarga
                val imagenUrl = comercio?.imagenUrls?.firstOrNull().orEmpty()
                    .ifBlank { lugar.imagenPortadaUrl }
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = FondoBlancoAdaptable),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    border = BorderStroke(1.dp, ColorAirbnbBorde),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = alturaMaximaTarjeta)
                            .verticalScroll(rememberScrollState())
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            if (imagenUrl.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(ColorAirbnbFondo)
                                ) {
                                    RemoteImage(
                                        url = imagenUrl,
                                        contentDescription = lugar.nombre,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = comercio?.categoria?.replaceFirstChar { it.uppercase() }
                                        ?: lugar.categoria.etiqueta,
                                    color = ColorTurismapPrimario, // Tono verde corporativo
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = lugar.nombre,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorAirbnbOscuro
                                )
                            }
                            IconButton(
                                onClick = { lugarSeleccionado = null },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar detalles",
                                    tint = ColorAirbnbGris
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        val direccion = listOf(lugar.ubicacionCorta, municipio)
                            .filter(String::isNotBlank)
                            .distinct()
                            .joinToString(" · ")
                        if (direccion.isNotBlank()) {
                            Text(direccion, fontSize = 13.sp, color = ColorAirbnbGris)
                        }
                        if (comercio != null) {
                            Text(
                                "El marcador indica una ubicación aproximada dentro del municipio.",
                                fontSize = 12.sp,
                                color = ColorAirbnbGris
                            )
                        }
                        if (comercio != null && (comercio.calificacionPromedio ?: 0.0) > 0.0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB800),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${comercio.calificacionPromedio} · ${comercio.cantidadResenas ?: 0} reseñas",
                                    fontSize = 13.sp,
                                    color = ColorAirbnbGris
                                )
                            }
                        }

                        val descripcion = comercio?.descripcion?.takeIf(String::isNotBlank)
                            ?: lugar.historia
                        if (descripcion.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(descripcion, fontSize = 14.sp, color = ColorAirbnbGris)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = ColorAirbnbBorde, thickness = 0.8.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    try {
                                        uriHandler.openUri(urlGoogleMaps(comercio, lugar, municipio))
                                    } catch (error: Exception) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                error.message ?: "No se pudo abrir Google Maps."
                                            )
                                        }
                                    }

                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ColorTurismapPrimario), // Tono verde corporativo
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cómo llegar con Google Maps", color = Color.White)
                            }

                            if (comercio != null) {
                                OutlinedButton(
                                    onClick = { onComercioClick(comercio.id) },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, ColorAirbnbBorde),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Ver detalles del comercio")
                                }
                            } else {
                                Button(
                                    onClick = { onAbrirDetalle(lugar) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ColorAirbnbOscuro),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Ver detalles del destino", color = FondoBlancoAdaptable)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = ColorTurismapPrimario
            )
        } else if (lugaresParaElMapa.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = errorCarga ?: "No hay destinos ni comercios con ubicación para mostrar.",
                    color = ColorAirbnbOscuro
                )
                if (errorCarga != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { intentoCarga++ }) {
                        Text("Reintentar")
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 150.dp)
        )
    }
}