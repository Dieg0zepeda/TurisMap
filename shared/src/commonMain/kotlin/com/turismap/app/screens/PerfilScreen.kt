package com.turismap.app.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.round
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import com.turismap.app.components.RemoteImage
import com.turismap.app.components.rememberSelectorImagen
import com.turismap.app.data.repository.AutenticacionRepository
import com.turismap.app.data.repository.ComercioRepository
import com.turismap.app.data.repository.LugaresRepository
import com.turismap.app.data.repository.UsuarioRepository
import com.turismap.app.theme.*

private const val TURISMAP_WEB_URL = "https://urldeprueba.com"

private data class ItemFavoritoUi(
    val id: String,
    val nombre: String,
    val categoria: String,
    val imagenUrl: String,
    val calificacion: Double,
    val esComercio: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    paddingValues: PaddingValues = PaddingValues(0.dp),
    onCerrarSesionClick: () -> Unit = {},
    onVerPlanesClick: () -> Unit = {},
    onComercioClick: (String) -> Unit = {},
    onLugarClick: (String) -> Unit = {}
) {
    val verdeEsmeralda = Color(0xFF1B4934)
    val terracota = Color(0xFFD96B52)
    val fondoApp = MaterialTheme.colorScheme.background
    val fondoTarjeta = MaterialTheme.colorScheme.surface
    val colorTextoPrincipal = MaterialTheme.colorScheme.onBackground
    val colorTextoSecundario = MaterialTheme.colorScheme.onSurfaceVariant
    val colorDivisor = MaterialTheme.colorScheme.outlineVariant
    val uriHandler = LocalUriHandler.current

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val idUsuarioActual = UsuarioRepository.obtenerIdUsuarioActual()

    val usuarioBD by UsuarioRepository.observarUsuarioActual().collectAsState(initial = null)

    var estaCargando by remember { mutableStateOf(true) }
    var visitas by remember { mutableStateOf(0) }
    var errorPerfil by remember { mutableStateOf<String?>(null) }

    var mostrarVentanaFavoritos by remember { mutableStateOf(false) }
    var cargandoFavoritos by remember { mutableStateOf(false) }
    var listaFavoritosDetallados by remember { mutableStateOf<List<ItemFavoritoUi>>(emptyList()) }
    var categoriaFavoritoSeleccionada by remember { mutableStateOf("Todos") }

    LaunchedEffect(idUsuarioActual) {
        estaCargando = true
        errorPerfil = null
        try {
            if (idUsuarioActual == null) {
                errorPerfil = "No hay una sesión autenticada."
            } else {
                UsuarioRepository.asegurarPerfil(
                    email = AutenticacionRepository.usuarioActual?.email.orEmpty(),
                    nombre = AutenticacionRepository.usuarioActual?.displayName.orEmpty()
                )
                visitas = UsuarioRepository.contarVisitas(idUsuarioActual)
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            errorPerfil = error.message ?: "No se pudo cargar el perfil."
        } finally {
            estaCargando = false
        }
    }

    LaunchedEffect(mostrarVentanaFavoritos, usuarioBD?.favoritos) {
        if (mostrarVentanaFavoritos) {
            cargandoFavoritos = true
            try {
                val favoritosIds = usuarioBD?.favoritos ?: emptyList()
                if (favoritosIds.isEmpty()) {
                    listaFavoritosDetallados = emptyList()
                } else {
                    val todosLugares = try { LugaresRepository.obtenerTodos().associateBy { it.id.trim() } } catch (_: Exception) { emptyMap() }
                    val todosComercios = try { ComercioRepository.obtenerPublicados().associateBy { it.id.trim() } } catch (_: Exception) { emptyMap() }

                    val resueltos = favoritosIds.mapNotNull { idOriginal ->
                        val idLimpio = idOriginal.trim()
                        val lugar = todosLugares[idLimpio]
                        if (lugar != null) {
                            ItemFavoritoUi(
                                id = lugar.id,
                                nombre = lugar.nombre,
                                categoria = lugar.categoria.etiqueta,
                                imagenUrl = lugar.imagenPortadaUrl,
                                calificacion = lugar.calificacion,
                                esComercio = false
                            )
                        } else {
                            val idComercioSinPrefijo = idLimpio.removePrefix("comercio:")
                            val comercio = todosComercios[idComercioSinPrefijo] ?: todosComercios[idLimpio]
                            if (comercio != null) {
                                ItemFavoritoUi(
                                    id = comercio.id,
                                    nombre = comercio.nombre,
                                    categoria = comercio.categoria,
                                    imagenUrl = comercio.imagenUrls.firstOrNull().orEmpty(),
                                    calificacion = comercio.calificacionPromedio ?: 4.9,
                                    esComercio = true
                                )
                            } else null
                        }
                    }
                    listaFavoritosDetallados = resueltos
                }
            } catch (error: Exception) {
                snackbarHostState.showSnackbar("Error al cargar favoritos: ${error.message}")
            } finally {
                cargandoFavoritos = false
            }
        }
    }

    val cantDestinos = remember(listaFavoritosDetallados) { listaFavoritosDetallados.count { !it.esComercio } }
    val cantComercios = remember(listaFavoritosDetallados) { listaFavoritosDetallados.count { it.esComercio } }

    val favoritosFiltrados = remember(listaFavoritosDetallados, categoriaFavoritoSeleccionada) {
        when (categoriaFavoritoSeleccionada) {
            "Destinos" -> listaFavoritosDetallados.filter { !it.esComercio }
            "Comercios" -> listaFavoritosDetallados.filter { it.esComercio }
            else -> listaFavoritosDetallados
        }
    }

    val nombreMostrar = usuarioBD?.nombre?.takeIf { it.isNotBlank() } ?: if (estaCargando) "Cargando..." else "Usuario"
    val correoMostrar = usuarioBD?.email?.takeIf { it.isNotBlank() } ?: AutenticacionRepository.usuarioActual?.email ?: if (estaCargando) "Cargando..." else "Correo no disponible"
    val cantidadFavoritos = (usuarioBD?.favoritos?.size ?: 0).toString()

    var idiomaSeleccionado by remember { mutableStateOf("Español") }
    var perfilPublico by remember { mutableStateOf(true) }
    var compartirUbicacion by remember { mutableStateOf(false) }

    var mostrarDialogoEditar by remember { mutableStateOf(false) }
    var mostrarDialogoIdioma by remember { mutableStateOf(false) }
    var mostrarDialogoPrivacidad by remember { mutableStateOf(false) }
    var cerrandoSesion by remember { mutableStateOf(false) }
    var tipoFotoSeleccionada by remember { mutableStateOf("perfil") }

    val abrirGaleriaNativa = rememberSelectorImagen { uriLocal ->
        coroutineScope.launch {
            try {
                UsuarioRepository.actualizarFoto(tipoFotoSeleccionada == "perfil", uriLocal)
                snackbarHostState.showSnackbar("¡Foto actualizada con éxito! ✅")
            } catch (error: Exception) {
                snackbarHostState.showSnackbar("Error: ${error.message}")
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(fondoApp)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(verdeEsmeralda)
                        .clickable {
                            tipoFotoSeleccionada = "portada"
                            abrirGaleriaNativa()
                        }
                ) {
                    usuarioBD?.fotoPortada?.takeIf { it.isNotBlank() }?.let { url ->
                        RemoteImage(
                            url = url,
                            contentDescription = "Foto de portada",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    IconButton(
                        onClick = {
                            tipoFotoSeleccionada = "portada"
                            abrirGaleriaNativa()
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = "Cambiar Portada",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Box(modifier = Modifier.size(130.dp).align(Alignment.BottomCenter)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(4.dp, fondoApp, CircleShape)
                            .clickable {
                                tipoFotoSeleccionada = "perfil"
                                abrirGaleriaNativa()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val fotoPerfil = usuarioBD?.fotoPerfil
                        if (!fotoPerfil.isNullOrBlank()) {
                            RemoteImage(
                                url = fotoPerfil,
                                contentDescription = "Foto de perfil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier.size(72.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-4).dp, y = (-4).dp)
                            .size(40.dp)
                            .background(terracota, CircleShape)
                            .border(2.dp, fondoApp, CircleShape)
                            .clickable { mostrarDialogoEditar = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar Perfil",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = nombreMostrar, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colorTextoPrincipal, modifier = Modifier.padding(horizontal = 16.dp))
                Text(text = correoMostrar, fontSize = 14.sp, color = colorTextoSecundario, modifier = Modifier.padding(top = 4.dp))
                errorPerfil?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                ItemEstadistica(
                    valor = cantidadFavoritos,
                    etiqueta = "Favoritos",
                    colorEtiqueta = TerracotaTextoAdaptable,
                    colorTexto = colorTextoPrincipal,
                    onClick = { mostrarVentanaFavoritos = true }
                )
                ItemEstadistica(
                    valor = visitas.toString(),
                    etiqueta = "Visitas",
                    colorEtiqueta = colorTextoPrincipal,
                    colorTexto = colorTextoPrincipal
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = verdeEsmeralda)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Surface(shape = RoundedCornerShape(6.dp), color = Color.White.copy(alpha = 0.15f)) {
                        Text(text = "TurisMap Negocios", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Promociona tu negocio en la Ruta", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "¿Tienes un hotel, café, restaurante o taller en la CA-8? Llega a más de 45,000 viajeros mensuales en busca de experiencias auténticas.", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f), lineHeight = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onVerPlanesClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = terracota), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)) {
                            Text(text = "Ver planes para mi negocio", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        OutlinedButton(
                            onClick = {
                                try { uriHandler.openUri(TURISMAP_WEB_URL) } catch (error: Exception) { coroutineScope.launch { snackbarHostState.showSnackbar(error.message ?: "No se pudo abrir.") } }
                            },
                            modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.75f)), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White), shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Ver en la web")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("PERSONALIZACIÓN", fontSize = 12.sp, color = colorTextoSecundario, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
                CardOpciones(fondoTarjeta) {
                    ItemMenuOpcion(icono = Icons.Default.Public, titulo = "Idioma", subtitulo = idiomaSeleccionado, colorTexto = colorTextoPrincipal, colorIcono = colorTextoPrincipal) { mostrarDialogoIdioma = true }
                }

                Text("PRIVACIDAD & SEGURIDAD", fontSize = 12.sp, color = colorTextoSecundario, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp, top = 8.dp))
                CardOpciones(fondoTarjeta) {
                    ItemMenuOpcion(icono = Icons.Default.Security, titulo = "Ajustes de Privacidad", subtitulo = "Contraseña y visibilidad", colorTexto = colorTextoPrincipal, colorIcono = colorTextoPrincipal) { mostrarDialogoPrivacidad = true }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        if (!cerrandoSesion) {
                            cerrandoSesion = true
                            coroutineScope.launch {
                                try {
                                    AutenticacionRepository.cerrarSesion()
                                    onCerrarSesionClick()
                                } catch (error: Exception) {
                                    snackbarHostState.showSnackbar(error.message ?: "Error.")
                                } finally { cerrandoSesion = false }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = TerracotaTextoAdaptable), border = BorderStroke(2.dp, TerracotaTextoAdaptable), shape = RoundedCornerShape(16.dp), enabled = !cerrandoSesion
                ) {
                    if (cerrandoSesion) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = TerracotaTextoAdaptable)
                    } else {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cerrar Sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }

    if (mostrarVentanaFavoritos) {
        ModalBottomSheet(
            onDismissRequest = { mostrarVentanaFavoritos = false },
            containerColor = fondoTarjeta,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = { BottomSheetDefaults.DragHandle(color = colorDivisor) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(horizontal = 20.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("❤️", fontSize = 22.sp)
                        Column {
                            Text("Mis Favoritos", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colorTextoPrincipal)
                            Text("${listaFavoritosDetallados.size} guardados en total", fontSize = 12.sp, color = colorTextoSecundario)
                        }
                    }
                    IconButton(onClick = { mostrarVentanaFavoritos = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = colorTextoSecundario)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Todos" to listaFavoritosDetallados.size,
                        "Destinos" to cantDestinos,
                        "Comercios" to cantComercios
                    ).forEach { (tipo, count) ->
                        val activa = categoriaFavoritoSeleccionada == tipo
                        FilterChip(
                            selected = activa,
                            onClick = { categoriaFavoritoSeleccionada = tipo },
                            label = {
                                Text(
                                    text = "$tipo ($count)",
                                    fontSize = 12.sp,
                                    fontWeight = if (activa) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = verdeEsmeralda,
                                selectedLabelColor = Color.White,
                                containerColor = fondoApp,
                                labelColor = colorTextoPrincipal
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = activa,
                                borderColor = if (activa) verdeEsmeralda else colorDivisor
                            )
                        )
                    }
                }

                HorizontalDivider(color = colorDivisor)

                when {
                    cargandoFavoritos -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = verdeEsmeralda)
                        }
                    }
                    favoritosFiltrados.isEmpty() -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                if (categoriaFavoritoSeleccionada == "Destinos") "📍" else if (categoriaFavoritoSeleccionada == "Comercios") "🏪" else "🧭",
                                fontSize = 48.sp
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = when (categoriaFavoritoSeleccionada) {
                                    "Destinos" -> "No tienes destinos guardados"
                                    "Comercios" -> "No tienes comercios guardados"
                                    else -> "Aún no tienes favoritos"
                                },
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorTextoPrincipal
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Pulsa el corazón en ${if (categoriaFavoritoSeleccionada == "Destinos") "los pueblos o destinos" else "los restaurantes y tiendas"} para agregarlos a esta lista.",
                                fontSize = 13.sp,
                                color = colorTextoSecundario,
                                lineHeight = 18.sp
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(favoritosFiltrados, key = { it.id }) { item ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            mostrarVentanaFavoritos = false
                                            if (item.esComercio) onComercioClick(item.id)
                                            else onLugarClick(item.id)
                                        },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = fondoApp),
                                    border = BorderStroke(1.dp, colorDivisor)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (item.imagenUrl.isNotBlank()) {
                                                RemoteImage(
                                                    url = item.imagenUrl,
                                                    contentDescription = item.nombre,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Text(if (item.esComercio) "🏪" else "📍", fontSize = 24.sp)
                                            }
                                        }

                                        Spacer(Modifier.width(12.dp))

                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(end = 4.dp),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = item.categoria.uppercase(),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (item.esComercio) VerdeTextoAdaptable else TerracotaTextoAdaptable,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f, fill = false)
                                                )

                                                if (item.calificacion > 0.0) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                    ) {
                                                        Text(
                                                            text = "★",
                                                            fontSize = 11.sp,
                                                            color = Color(0xFFF59E0B)
                                                        )
                                                        val califTexto = "${round(item.calificacion * 10.0) / 10.0}"
                                                        Text(
                                                            text = califTexto,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFFF59E0B),
                                                            maxLines = 1
                                                        )
                                                    }
                                                }
                                            }

                                            Text(
                                                text = item.nombre,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colorTextoPrincipal,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Text(
                                                text = if (item.esComercio) "Comercio verificado" else "Destino turístico",
                                                fontSize = 12.sp,
                                                color = colorTextoSecundario,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Spacer(Modifier.width(6.dp))

                                        IconButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    try {
                                                        UsuarioRepository.cambiarFavorito(item.id, false)
                                                        snackbarHostState.showSnackbar("Eliminado de favoritos")
                                                    } catch (e: Exception) {
                                                        snackbarHostState.showSnackbar(e.message ?: "Error al actualizar")
                                                    }
                                                }
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Favorite,
                                                contentDescription = "Quitar de favoritos",
                                                tint = terracota,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoPrivacidad) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoPrivacidad = false },
            containerColor = fondoTarjeta,
            title = { Text("Privacidad", fontWeight = FontWeight.Bold, color = colorTextoPrincipal) },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Perfil Público", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colorTextoPrincipal)
                            Text("Permite que otros vean tus guardados.", fontSize = 12.sp, color = colorTextoSecundario)
                        }
                        Switch(checked = perfilPublico, onCheckedChange = { perfilPublico = it }, colors = SwitchDefaults.colors(checkedThumbColor = VerdeTextoAdaptable, checkedTrackColor = VerdeTextoAdaptable.copy(alpha = 0.5f)))
                    }
                    HorizontalDivider(color = colorDivisor)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ubicación en Reseñas", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colorTextoPrincipal)
                            Text("Comparte de dónde es tu reseña.", fontSize = 12.sp, color = colorTextoSecundario)
                        }
                        Switch(checked = compartirUbicacion, onCheckedChange = { compartirUbicacion = it }, colors = SwitchDefaults.colors(checkedThumbColor = VerdeTextoAdaptable, checkedTrackColor = VerdeTextoAdaptable.copy(alpha = 0.5f)))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { coroutineScope.launch { snackbarHostState.showSnackbar("Te enviaremos un correo.") } }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = colorTextoPrincipal), shape = RoundedCornerShape(8.dp)) {
                        Icon(Icons.Default.LockReset, contentDescription = null, tint = fondoApp, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cambiar Contraseña", color = fondoApp)
                    }
                }
            },
            confirmButton = { TextButton(onClick = { mostrarDialogoPrivacidad = false }) { Text("Cerrar", color = VerdeTextoAdaptable, fontWeight = FontWeight.Bold) } }
        )
    }

    if (mostrarDialogoEditar) {
        var tempNombre by remember { mutableStateOf(nombreMostrar) }
        var tempTelefono by remember { mutableStateOf(usuarioBD?.telefono.orEmpty()) }
        AlertDialog(
            onDismissRequest = { mostrarDialogoEditar = false },
            containerColor = fondoTarjeta,
            title = { Text("Editar Perfil", fontWeight = FontWeight.Bold, color = colorTextoPrincipal) },
            text = {
                Column {
                    OutlinedTextField(value = tempNombre, onValueChange = { tempNombre = it }, label = { Text("Nombre Completo") }, modifier = Modifier.fillMaxWidth(), singleLine = true, textStyle = LocalTextStyle.current.copy(color = colorTextoPrincipal))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = tempTelefono, onValueChange = { tempTelefono = it }, label = { Text("Teléfono de Contacto") }, modifier = Modifier.fillMaxWidth(), singleLine = true, textStyle = LocalTextStyle.current.copy(color = colorTextoPrincipal))
                }
            },
            confirmButton = {
                Button(onClick = {
                    coroutineScope.launch {
                        try {
                            UsuarioRepository.actualizarPerfil(tempNombre.trim(), tempTelefono.trim())
                            mostrarDialogoEditar = false
                            snackbarHostState.showSnackbar("Perfil actualizado.")
                        } catch (error: Exception) {
                            snackbarHostState.showSnackbar(error.message ?: "Error al actualizar.")
                        }
                    }
                }, enabled = tempNombre.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = verdeEsmeralda)) { Text("Guardar", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { mostrarDialogoEditar = false }) { Text("Cancelar", color = TerracotaTextoAdaptable) } }
        )
    }

    if (mostrarDialogoIdioma) {
        val opcionesIdioma = listOf("Español", "English", "Français")
        AlertDialog(
            onDismissRequest = { mostrarDialogoIdioma = false },
            containerColor = fondoTarjeta,
            title = { Text("Idioma", fontWeight = FontWeight.Bold, color = colorTextoPrincipal) },
            text = {
                Column {
                    opcionesIdioma.forEach { idioma ->
                        Row(modifier = Modifier.fillMaxWidth().clickable { idiomaSeleccionado = idioma }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = (idioma == idiomaSeleccionado), onClick = { idiomaSeleccionado = idioma }, colors = RadioButtonDefaults.colors(selectedColor = VerdeTextoAdaptable))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = idioma, fontSize = 16.sp, color = colorTextoPrincipal)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { mostrarDialogoIdioma = false }) { Text("Cerrar", color = TerracotaTextoAdaptable) } }
        )
    }
}

@Composable
private fun ItemEstadistica(
    valor: String,
    etiqueta: String,
    colorEtiqueta: Color,
    colorTexto: Color,
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .then(
                if (onClick != null) Modifier.clip(RoundedCornerShape(12.dp)).clickable { onClick() }.padding(horizontal = 12.dp, vertical = 6.dp)
                else Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
    ) {
        Text(text = valor, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colorTexto)
        Text(text = etiqueta, fontSize = 12.sp, color = colorEtiqueta, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun CardOpciones(fondoTarjeta: Color, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(fondoTarjeta, RoundedCornerShape(16.dp)).padding(vertical = 4.dp), content = content)
}

@Composable
private fun ItemMenuOpcion(icono: ImageVector, titulo: String, subtitulo: String = "", colorTexto: Color, colorIcono: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icono, contentDescription = null, tint = colorIcono, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = titulo, fontSize = 16.sp, color = colorTexto, fontWeight = FontWeight.Medium)
            if (subtitulo.isNotEmpty()) Text(text = subtitulo, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}