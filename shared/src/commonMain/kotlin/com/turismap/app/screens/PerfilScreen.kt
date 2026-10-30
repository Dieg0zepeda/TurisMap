package com.turismap.app.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.turismap.app.components.RemoteImage
import com.turismap.app.data.repository.AutenticacionRepository
import com.turismap.app.data.repository.UsuarioRepository
import com.turismap.app.data.repository.UsuarioBD
import kotlinx.coroutines.CancellationException
import com.turismap.app.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    paddingValues: PaddingValues = PaddingValues(0.dp),
    onCerrarSesionClick: () -> Unit = {}
) {
    // --- VARIABLES DE APARIENCIA ---
    val verdeEsmeralda = Color(0xFF1B4934)
    val terracota = Color(0xFFD96B52)
    val fondoApp = MaterialTheme.colorScheme.background
    val fondoTarjeta = MaterialTheme.colorScheme.surface
    val colorTextoPrincipal = MaterialTheme.colorScheme.onBackground
    val colorTextoSecundario = MaterialTheme.colorScheme.onSurfaceVariant
    val colorDivisor = MaterialTheme.colorScheme.outlineVariant

    // --- CONEXIÓN A FIREBASE (REAL) ---
    var usuarioBD by remember { mutableStateOf<UsuarioBD?>(null) }
    var estaCargando by remember { mutableStateOf(true) }
    var visitas by remember { mutableStateOf(0) }
    var errorPerfil by remember { mutableStateOf<String?>(null) }

    // Extraemos el ID dinámico de la sesión actual
    val idUsuarioActual = UsuarioRepository.obtenerIdUsuarioActual()

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
                usuarioBD = UsuarioRepository.obtenerUsuarioPorId(idUsuarioActual)
                    ?: throw IllegalStateException("No se encontró el perfil del usuario.")
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

    val nombreMostrar = usuarioBD?.nombre?.takeIf { it.isNotBlank() }
        ?: if (estaCargando) "Cargando..." else "Usuario"
    val correoMostrar = usuarioBD?.email?.takeIf { it.isNotBlank() }
        ?: AutenticacionRepository.usuarioActual?.email
        ?: if (estaCargando) "Cargando..." else "Correo no disponible"
    val cantidadFavoritos = usuarioBD?.favoritos?.size?.toString() ?: "0"

    // Variables para las fotos

    // --- VARIABLES INTERACTIVAS ---
    var idiomaSeleccionado by remember { mutableStateOf("Español") }
    var notificacionesActivas by remember { mutableStateOf(true) }
    var perfilPublico by remember { mutableStateOf(true) }
    var compartirUbicacion by remember { mutableStateOf(false) }

    var mostrarDialogoEditar by remember { mutableStateOf(false) }
    var mostrarDialogoIdioma by remember { mutableStateOf(false) }
    var mostrarDialogoPrivacidad by remember { mutableStateOf(false) }
    var mostrarOpcionesFoto by remember { mutableStateOf(false) }
    var mostrarFormularioComercio by remember { mutableStateOf(false) }
    var urlFotoTemporal by remember { mutableStateOf("") }
    var tipoFotoSeleccionada by remember { mutableStateOf("perfil") } // "perfil" o "portada"
    var cerrandoSesion by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(fondoApp)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // --- 1. ENCABEZADO (FOTOS) ---
            Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                // FOTO DE PORTADA
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(verdeEsmeralda)
                        .clickable {
                            tipoFotoSeleccionada = "portada"
                            urlFotoTemporal = usuarioBD?.fotoPortada.orEmpty()
                            mostrarOpcionesFoto = true
                        }
                ) {
                    usuarioBD?.fotoPortada?.takeIf { it.startsWith("https://", true) }?.let { url ->
                        RemoteImage(
                            url = url,
                            contentDescription = "Foto de portada",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    IconButton(onClick = {
                        tipoFotoSeleccionada = "portada"
                        urlFotoTemporal = usuarioBD?.fotoPortada.orEmpty()
                        mostrarOpcionesFoto = true
                    }, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(Color.Black.copy(alpha = 0.4f), CircleShape).size(36.dp)) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = "Cambiar Portada", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }

                // FOTO DE PERFIL
                Box(modifier = Modifier.size(130.dp).align(Alignment.BottomCenter)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(4.dp, fondoApp, CircleShape)
                            .clickable {
                                tipoFotoSeleccionada = "perfil"
                                urlFotoTemporal = usuarioBD?.fotoPerfil.orEmpty()
                                mostrarOpcionesFoto = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val fotoPerfil = usuarioBD?.fotoPerfil?.takeIf { it.startsWith("https://", true) }
                        if (fotoPerfil != null) {
                            RemoteImage(
                                url = fotoPerfil,
                                contentDescription = "Foto de perfil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(Icons.Default.Person, contentDescription = "Foto de perfil", modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    // Botón flotante para editar nombre
                    Box(modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-4).dp, y = (-4).dp).size(40.dp).background(terracota, CircleShape).border(2.dp, fondoApp, CircleShape).clickable { mostrarDialogoEditar = true }, contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar Perfil", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. INFO DEL USUARIO ---
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = nombreMostrar, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colorTextoPrincipal, modifier = Modifier.padding(horizontal = 16.dp))
                Text(text = correoMostrar, fontSize = 14.sp, color = colorTextoSecundario, modifier = Modifier.padding(top = 4.dp))
                errorPerfil?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 3. ESTADÍSTICAS ---
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                ItemEstadistica(valor = cantidadFavoritos, etiqueta = "Favoritos", colorEtiqueta = TerracotaTextoAdaptable, colorTexto = colorTextoPrincipal)
                ItemEstadistica(valor = visitas.toString(), etiqueta = "Visitas", colorEtiqueta = colorTextoPrincipal, colorTexto = colorTextoPrincipal)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 4. Banner TurisMap Negocios ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = verdeEsmeralda)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Text(
                        text = "TurisMap Negocios",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                    text = "Promociona tu negocio en la Ruta",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                    text = "¿Tienes un hotel, café, restaurante o taller en la CA-8? Llega a más de 45,000 viajeros mensuales en busca de experiencias auténticas.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { mostrarFormularioComercio = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = terracota),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                        text = "Registrar mi comercio",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 5. SECCIONES DE CONFIGURACIÓN ---
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                Text("PERSONALIZACIÓN", fontSize = 12.sp, color = colorTextoSecundario, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
                CardOpciones(fondoTarjeta) {
                    ItemMenuOpcion(icono = Icons.Default.Public, titulo = "Idioma", subtitulo = idiomaSeleccionado, colorTexto = colorTextoPrincipal, colorIcono = colorTextoPrincipal) { mostrarDialogoIdioma = true }
                }

                Text("ALERTAS", fontSize = 12.sp, color = colorTextoSecundario, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp, top = 8.dp))
                CardOpciones(fondoTarjeta) {
                    Row(modifier = Modifier.fillMaxWidth().clickable { notificacionesActivas = !notificacionesActivas }.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = colorTextoPrincipal, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Notificaciones", fontSize = 16.sp, color = colorTextoPrincipal, fontWeight = FontWeight.Medium)
                            Text(if (notificacionesActivas) "Activadas" else "Desactivadas", fontSize = 12.sp, color = colorTextoSecundario)
                        }
                        Switch(checked = notificacionesActivas, onCheckedChange = { notificacionesActivas = it }, colors = SwitchDefaults.colors(checkedThumbColor = VerdeTextoAdaptable, checkedTrackColor = VerdeTextoAdaptable.copy(alpha = 0.5f)))
                    }
                }

                Text("PRIVACIDAD & SEGURIDAD", fontSize = 12.sp, color = colorTextoSecundario, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp, top = 8.dp))
                CardOpciones(fondoTarjeta) {
                    ItemMenuOpcion(icono = Icons.Default.Security, titulo = "Ajustes de Privacidad", subtitulo = "Contraseña y visibilidad", colorTexto = colorTextoPrincipal, colorIcono = colorTextoPrincipal) { mostrarDialogoPrivacidad = true }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- BOTÓN CERRAR SESIÓN REAL ---
                OutlinedButton(
                    onClick = {
                        if (!cerrandoSesion) {
                            cerrandoSesion = true
                            coroutineScope.launch {
                                try {
                                    AutenticacionRepository.cerrarSesion()
                                    onCerrarSesionClick()
                                } catch (error: CancellationException) {
                                    throw error
                                } catch (error: Exception) {
                                    snackbarHostState.showSnackbar(error.message ?: "No se pudo cerrar la sesión.")
                                } finally {
                                    cerrandoSesion = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TerracotaTextoAdaptable),
                    border = BorderStroke(2.dp, TerracotaTextoAdaptable), shape = RoundedCornerShape(16.dp),
                    enabled = !cerrandoSesion
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // --- DIÁLOGOS EMERGENTES ---

    if (mostrarOpcionesFoto) {
        val tituloDialogo = if (tipoFotoSeleccionada == "perfil") "Actualizar Foto de Perfil" else "Actualizar Foto de Portada"
        AlertDialog(
            onDismissRequest = { mostrarOpcionesFoto = false },
            containerColor = fondoTarjeta,
            title = { Text(tituloDialogo, fontWeight = FontWeight.Bold, color = colorTextoPrincipal) },
            text = {
                Column {
                    Text("Usa una URL HTTPS pública. La app no sube archivos ni utiliza Firebase Storage.")
                    OutlinedTextField(
                        value = urlFotoTemporal,
                        onValueChange = { urlFotoTemporal = it },
                        label = { Text("URL de imagen") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        try {
                            UsuarioRepository.actualizarFoto(tipoFotoSeleccionada == "perfil", urlFotoTemporal)
                            usuarioBD = if (tipoFotoSeleccionada == "perfil") {
                                usuarioBD?.copy(fotoPerfil = urlFotoTemporal.trim())
                            } else {
                                usuarioBD?.copy(fotoPortada = urlFotoTemporal.trim())
                            }
                            mostrarOpcionesFoto = false
                            snackbarHostState.showSnackbar("Imagen actualizada.")
                        } catch (error: CancellationException) {
                            throw error
                        } catch (error: Exception) {
                            snackbarHostState.showSnackbar(error.message ?: "No se pudo actualizar la imagen.")
                        }
                    }
                }) { Text("Guardar", color = verdeEsmeralda) }
            },
            dismissButton = { TextButton(onClick = { mostrarOpcionesFoto = false }) { Text("Cancelar", color = TerracotaTextoAdaptable) } }
        )
    }

    if (mostrarFormularioComercio) {
        ComercioSolicitudDialog(
            onDismiss = { mostrarFormularioComercio = false },
            onSubmitted = {
                mostrarFormularioComercio = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Solicitud enviada. Un administrador debe revisarla antes de publicarse.")
                }
            }
        )
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
        AlertDialog(
            onDismissRequest = { mostrarDialogoEditar = false },
            containerColor = fondoTarjeta,
            title = { Text("Editar Perfil", fontWeight = FontWeight.Bold, color = colorTextoPrincipal) },
            text = {
                Column {
                    OutlinedTextField(value = tempNombre, onValueChange = { tempNombre = it }, label = { Text("Nombre Completo") }, modifier = Modifier.fillMaxWidth(), singleLine = true, textStyle = LocalTextStyle.current.copy(color = colorTextoPrincipal))
                }
            },
            confirmButton = {
                Button(onClick = {
                    coroutineScope.launch {
                        try {
                            UsuarioRepository.actualizarPerfil(tempNombre.trim())
                            usuarioBD = usuarioBD?.copy(nombre = tempNombre.trim())
                            mostrarDialogoEditar = false
                            snackbarHostState.showSnackbar("Perfil actualizado.")
                        } catch (error: CancellationException) {
                            throw error
                        } catch (error: Exception) {
                            snackbarHostState.showSnackbar(error.message ?: "No se pudo actualizar el perfil.")
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

// ==========================================
// --- COMPONENTES AUXILIARES PRIVADOS ---
// ==========================================

@Composable
private fun ItemEstadistica(valor: String, etiqueta: String, colorEtiqueta: Color, colorTexto: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
            if (subtitulo.isNotEmpty()) Text(text = subtitulo, fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
    }
}