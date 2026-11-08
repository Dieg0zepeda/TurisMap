package com.turismap.app.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.components.RemoteImage
import com.turismap.app.data.repository.AutenticacionRepository
import com.turismap.app.data.repository.Comercio
import com.turismap.app.data.repository.ComercioRepository
import com.turismap.app.data.repository.Resena
import com.turismap.app.data.repository.UsuarioRepository
import com.turismap.app.theme.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun DetalleExperienciaScreen(comercioId: String, onVolver: () -> Unit) {
    val esOscuro = isSystemInDarkTheme()

    val colorFondoPantalla = if (esOscuro) Color(0xFF0B0F17) else Color(0xFFF8FAFC)
    val colorTextoPrincipal = if (esOscuro) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val colorTextoSecundario = if (esOscuro) Color(0xFF94A3B8) else Color(0xFF64748B)
    val colorBordeSuave = if (esOscuro) Color.White.copy(alpha = 0.08f) else Color(0xFFE2E8F0)
    val colorBarraInferior = if (esOscuro) Color(0xFF0F172A) else Color.White
    val colorFondoInput = if (esOscuro) Color(0xFF1E293B) else Color(0xFFF1F5F9)

    var comercio by remember(comercioId) { mutableStateOf<Comercio?>(null) }
    var resenas by remember(comercioId) { mutableStateOf<List<Resena>>(emptyList()) }
    var cargando by remember(comercioId) { mutableStateOf(true) }
    var error by remember(comercioId) { mutableStateOf<String?>(null) }
    var intento by remember(comercioId) { mutableIntStateOf(0) }
    var calificacion by remember { mutableIntStateOf(0) }
    var comentario by remember { mutableStateOf("") }
    var enviando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var mostrarModalMenu by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    // Observación en tiempo real del usuario desde Firestore
    val usuarioActual by UsuarioRepository.observarUsuarioActual().collectAsState(initial = null)
    val esFavorito = remember(usuarioActual?.favoritos, comercioId) {
        usuarioActual?.favoritos?.any { it == comercioId || it == "comercio:$comercioId" } == true
    }

    LaunchedEffect(comercioId, intento) {
        cargando = true
        error = null
        try {
            comercio = ComercioRepository.obtenerPorId(comercioId)
                ?.takeIf { it.estado == "publicado" }
                ?: throw IllegalStateException("Este comercio no está disponible.")
            resenas = ComercioRepository.obtenerResenas(comercioId)
            val propia = resenas.firstOrNull {
                it.usuarioUid == AutenticacionRepository.usuarioActual?.uid
            }
            calificacion = propia?.calificacion ?: 0
            comentario = propia?.comentario.orEmpty()
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (excepcion: Exception) {
            error = excepcion.message ?: "No se pudo cargar el comercio."
        } finally {
            cargando = false
        }
    }

    if (mostrarModalMenu && comercio != null) {
        AlertDialog(
            onDismissRequest = { mostrarModalMenu = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📖", fontSize = 22.sp)
                    Text("Menú Digital y Catálogo", fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Establecimiento: ${comercio!!.nombre}",
                        fontWeight = FontWeight.Bold,
                        color = colorTextoPrincipal
                    )
                    Text(
                        "Como beneficio de Socio Premium, este comercio cuenta con carta digitalizada en alta resolución. Aquí se cargará el visor de platillos o catálogo.",
                        color = colorTextoSecundario,
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { mostrarModalMenu = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                ) {
                    Text("Cerrar", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = if (esOscuro) Color(0xFF1E1B18) else Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(colorFondoPantalla)
            .imePadding()
    ) {
        when {
            cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VerdePrimario)
            }
            error != null -> Column(
                Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
                Button(onClick = { intento++ }) { Text("Reintentar") }
            }
            comercio != null -> {
                val datos = comercio!!
                val esPropietario = datos.propietarioUid == AutenticacionRepository.usuarioActual?.uid

                val planNombre = (datos.tipoPlan ?: "").lowercase()
                val esPremium = planNombre.contains("premium") || planNombre.contains("25")
                val esSocio = !esPremium && (planNombre.contains("socio") || planNombre.contains("15"))
                val esBasico = !esPremium && !esSocio

                val colorAcento = when {
                    esPremium -> if (esOscuro) Color(0xFFF59E0B) else Color(0xFFD97706)
                    esSocio -> if (esOscuro) Color(0xFF10B981) else Color(0xFF059669)
                    else -> if (esOscuro) Color(0xFF38BDF8) else Color(0xFF0284C7)
                }

                val fondoCardPlan = when {
                    esPremium -> if (esOscuro) Color(0xFF1A1408) else Color(0xFFFFFBEB)
                    esSocio -> if (esOscuro) Color(0xFF062217) else Color(0xFFF0FDF4)
                    else -> if (esOscuro) Color(0xFF111827) else Color.White
                }

                val fotosReales = datos.imagenUrls.filter { it.startsWith("http://") || it.startsWith("https://") }
                val fotos = if (esBasico) fotosReales.take(1) else fotosReales

                val telefonoLimpio = datos.telefono.filter { it.isDigit() }
                val whatsappUrl = if (telefonoLimpio.startsWith("503")) "https://wa.me/$telefonoLimpio" else "https://wa.me/503$telefonoLimpio"
                val direccionConsulta = datos.direccion.ifBlank { "${datos.nombre}, ${datos.lugarId}" }
                val mapsUrl = "https://www.google.com/maps/search/?api=1&query=" + direccionConsulta.replace(" ", "+")

                val precioTexto = if (datos.precioDesde != null && datos.precioDesde > 0) {
                    val pStr = datos.precioDesde.toString()
                    if (pStr.contains(".")) {
                        val partes = pStr.split(".")
                        "$${partes[0]}.${partes[1].padEnd(2, '0').take(2)}"
                    } else "$$pStr.00"
                } else "$3.00"

                val promedio = if (resenas.isEmpty()) 4.9 else resenas.map { it.calificacion }.average()

                Box(Modifier.fillMaxSize()) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 90.dp)
                    ) {
                        // 1. Zona Multimedia / Portada
                        Box(Modifier.fillMaxWidth().height(250.dp)) {
                            if (fotos.isNotEmpty()) {
                                val pagerState = rememberPagerState(pageCount = { fotos.size })

                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier.fillMaxSize()
                                ) { pagina ->
                                    RemoteImage(
                                        url = fotos[pagina],
                                        contentDescription = datos.nombre,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent, Color.Black.copy(alpha = 0.75f))
                                            )
                                        )
                                )

                                Row(
                                    Modifier
                                        .align(Alignment.BottomStart)
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    when {
                                        esPremium -> {
                                            Surface(color = Color.Black.copy(alpha = 0.75f), shape = RoundedCornerShape(8.dp)) {
                                                Text(
                                                    "📷 Galería Ilimitada (${fotos.size}+ HD)",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                            Surface(color = Color(0xFFF59E0B), shape = RoundedCornerShape(8.dp)) {
                                                Text(
                                                    "🏷️ 10% dto. TurisMap",
                                                    color = Color.Black,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                        esSocio -> {
                                            Surface(color = Color.Black.copy(alpha = 0.75f), shape = RoundedCornerShape(8.dp)) {
                                                Text(
                                                    "📷 Galería de ${fotos.size} fotos (${pagerState.currentPage + 1} de ${fotos.size})",
                                                    color = Color(0xFF34D399),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                        else -> {
                                            Surface(color = Color.Black.copy(alpha = 0.75f), shape = RoundedCornerShape(8.dp)) {
                                                Text(
                                                    "📷 Foto de Portada (1 foto)",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    Modifier.fillMaxSize().background(if (esOscuro) Color(0xFF1E293B) else Color(0xFFE2E8F0)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(datos.categoria, color = colorAcento, fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    IconButton(
                                        onClick = onVolver,
                                        modifier = Modifier.size(36.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                                    }

                                    if (esPremium) {
                                        Surface(color = Color(0xFFF59E0B), shape = RoundedCornerShape(20.dp)) {
                                            Text(
                                                "👑 Favorito VIP",
                                                color = Color.Black,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                            )
                                        }
                                    } else if (esSocio) {
                                        Surface(color = Color(0xFF10B981), shape = RoundedCornerShape(20.dp)) {
                                            Text(
                                                "★ Favorito de la Ruta",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (!esBasico) {
                                        IconButton(
                                            onClick = {
                                                scope.launch {
                                                    try {
                                                        UsuarioRepository.cambiarFavorito(comercioId, !esFavorito)
                                                    } catch (e: Exception) {
                                                        mensaje = e.message ?: "No se pudo actualizar favoritos."
                                                    }
                                                }
                                            },
                                            modifier = Modifier.size(36.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                        ) {
                                            Text(if (esFavorito) "❤️" else "🤍", fontSize = 16.sp)
                                        }
                                    }
                                    IconButton(
                                        onClick = onVolver,
                                        modifier = Modifier.size(36.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                                    }
                                }
                            }
                        }

                        // 2. Encabezado y Título
                        Column(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("★", color = colorAcento, fontSize = 15.sp)
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "${kotlin.math.round(promedio * 10) / 10} · ${resenas.size.coerceAtLeast(14)} reseñas · ${datos.categoria}",
                                        color = colorTextoSecundario,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                if (esPremium) {
                                    Surface(
                                        color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            "EXCLUSIVO",
                                            color = if (esOscuro) Color(0xFFFBBF24) else Color(0xFFB45309),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(4.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    datos.nombre,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colorTextoPrincipal
                                )
                                Spacer(Modifier.width(6.dp))
                                if (esPremium) {
                                    Text("👑", fontSize = 18.sp)
                                } else if (esSocio) {
                                    Text("✓", color = Color(0xFF10B981), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            // 3. Tarjeta: Ubicación
                            Card(
                                Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = fondoCardPlan),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (esPremium) Color(0xFFF59E0B).copy(alpha = 0.4f) else colorBordeSuave
                                )
                            ) {
                                Column(Modifier.padding(14.dp)) {
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "UBICACIÓN EN LA RUTA",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 1.sp,
                                            color = colorTextoSecundario
                                        )
                                        if (esPremium) {
                                            Text(
                                                "A 5 min de tu trayecto",
                                                color = if (esOscuro) Color(0xFF10B981) else Color(0xFF047857),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(6.dp))

                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("📍", fontSize = 16.sp)
                                            Text(
                                                datos.direccion.ifBlank { "Ruta de las Flores, ${datos.lugarId}" },
                                                color = colorTextoPrincipal,
                                                fontSize = 12.5.sp,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Spacer(Modifier.width(8.dp))

                                        Button(
                                            onClick = { uriHandler.openUri(mapsUrl) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = when {
                                                    esPremium -> Color(0xFFF59E0B)
                                                    esSocio -> Color(0xFF10B981)
                                                    else -> Color(0xFF0284C7)
                                                }
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                when {
                                                    esPremium -> "◆ Abrir Ruta GPS"
                                                    esSocio -> "🗺️ Ver en Mapa"
                                                    else -> "↱ Ruta"
                                                },
                                                color = if (esPremium) Color.Black else Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.5.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // 4. Conectores Directos
                            if (esSocio) {
                                Spacer(Modifier.height(16.dp))
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("ENLACES DIRECTOS DE CONTACTO", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = colorTextoSecundario)
                                    Text("BENEFICIO SOCIO", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981))
                                }

                                Spacer(Modifier.height(8.dp))

                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { if (telefonoLimpio.isNotBlank()) uriHandler.openUri(whatsappUrl) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("💬 WhatsApp", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            if (datos.sitioWeb.isNotBlank()) {
                                                val url = if (datos.sitioWeb.startsWith("http")) datos.sitioWeb else "https://${datos.sitioWeb}"
                                                uriHandler.openUri(url)
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, colorBordeSuave),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("🌐 Redes / Web", color = colorTextoPrincipal, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    }
                                }
                            } else if (esPremium) {
                                Spacer(Modifier.height(16.dp))
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("CONECTORES VIP DIRECTOS", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = colorTextoSecundario)
                                    Text("BENEFICIO PREMIUM", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFD97706))
                                }

                                Spacer(Modifier.height(8.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { if (telefonoLimpio.isNotBlank()) uriHandler.openUri(whatsappUrl) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (esOscuro) Color(0xFF261E10) else Color(0xFFFEF3C7)
                                        ),
                                        border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("💬", fontSize = 14.sp)
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    "WhatsApp Concierge 24/7",
                                                    color = if (esOscuro) Color(0xFFFBBF24) else Color(0xFFB45309),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text("›", color = Color(0xFFF59E0B), fontSize = 18.sp)
                                        }
                                    }

                                    Button(
                                        onClick = { mostrarModalMenu = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("📖", fontSize = 14.sp)
                                                Spacer(Modifier.width(8.dp))
                                                Text("Ver Menú Digital y Catálogo", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                                            }
                                            Text("↗", color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // 5. Acerca de este lugar
                            Spacer(Modifier.height(18.dp))
                            Text("ACERCA DE ESTE LUGAR", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = colorTextoSecundario, letterSpacing = 1.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                datos.descripcion.ifBlank { "Un espacio acogedor en la Ruta de las Flores preparado para atenderte." },
                                color = colorTextoPrincipal.copy(alpha = 0.9f),
                                fontSize = 13.5.sp,
                                lineHeight = 21.sp
                            )

                            // 6. Servicios y Comodidades
                            if (datos.servicios.isNotEmpty()) {
                                Spacer(Modifier.height(18.dp))
                                Text("SERVICIOS Y COMODIDADES", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = colorTextoSecundario, letterSpacing = 1.sp)
                                Spacer(Modifier.height(8.dp))

                                if (esBasico) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        datos.servicios.forEach { s ->
                                            Text("• $s", color = colorTextoPrincipal, fontSize = 13.sp)
                                        }
                                    }
                                } else {
                                    Row(
                                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        datos.servicios.forEach { servicio ->
                                            Surface(
                                                color = if (esPremium) {
                                                    if (esOscuro) Color(0xFF261E10) else Color(0xFFFEF3C7)
                                                } else {
                                                    if (esOscuro) Color(0xFF0F261E) else Color(0xFFDCFCE7)
                                                },
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (esPremium) Color(0xFFF59E0B).copy(alpha = 0.5f) else Color(0xFF10B981).copy(alpha = 0.5f)
                                                ),
                                                shape = RoundedCornerShape(20.dp)
                                            ) {
                                                Text(
                                                    "✦ $servicio",
                                                    color = if (esPremium) {
                                                        if (esOscuro) Color(0xFFFBBF24) else Color(0xFFB45309)
                                                    } else {
                                                        if (esOscuro) Color(0xFF34D399) else Color(0xFF047857)
                                                    },
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 7. Horario
                            Spacer(Modifier.height(18.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(8.dp).background(Color(0xFF10B981), CircleShape))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        if (esPremium) "Abierto todos los días" else "Abierto ahora",
                                        color = if (esOscuro) Color(0xFF10B981) else Color(0xFF047857),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    )
                                }
                                Text(
                                    datos.horario.ifBlank { "Lun a Dom: 7:00 AM - 8:00 PM" },
                                    color = colorTextoSecundario,
                                    fontSize = 12.5.sp
                                )
                            }

                            // Banner Promocional Premium
                            if (esPremium) {
                                Spacer(Modifier.height(16.dp))
                                Card(
                                    Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (esOscuro) Color(0xFF261D0C) else Color(0xFFFFFBEB)
                                    ),
                                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text("🎁", fontSize = 20.sp)
                                        Column {
                                            Text(
                                                "Promoción activa de la semana con TurisMap",
                                                color = if (esOscuro) Color(0xFFFBBF24) else Color(0xFFB45309),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp
                                            )
                                            Text(
                                                "Muestra tu app al ordenar y recibe cortesía especial del local.",
                                                color = colorTextoPrincipal.copy(alpha = 0.8f),
                                                fontSize = 11.5.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Galería Miniaturas Socio
                            if (esSocio && fotos.size > 1) {
                                Spacer(Modifier.height(18.dp))
                                Text("VISTA DE GALERÍA (${fotos.size} FOTOS)", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = colorTextoSecundario, letterSpacing = 1.sp)
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    fotos.take(5).forEach { f ->
                                        RemoteImage(
                                            url = f,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp))
                                        )
                                    }
                                }
                            }

                            // 8. Reseñas
                            Spacer(Modifier.height(28.dp))
                            Text("Reseñas de Turistas", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colorTextoPrincipal)
                            Spacer(Modifier.height(6.dp))

                            resenas.forEach { r ->
                                Card(
                                    Modifier.fillMaxWidth().padding(top = 8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (esOscuro) Color(0xFF1E293B).copy(alpha = 0.6f) else Color(0xFFF1F5F9)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, colorBordeSuave)
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text("★".repeat(r.calificacion), color = colorAcento, fontSize = 13.sp)
                                        Spacer(Modifier.height(4.dp))
                                        Text(r.comentario.ifBlank { "Excelente lugar recomendado." }, color = colorTextoPrincipal, fontSize = 13.sp)
                                    }
                                }
                            }

                            Spacer(Modifier.height(20.dp))
                            Text(
                                if (AutenticacionRepository.usuarioActual == null) "Inicia sesión para reseñar"
                                else if (esPropietario) "No puedes reseñar tu propio comercio"
                                else "Escribe tu reseña",
                                fontWeight = FontWeight.Bold,
                                color = colorTextoPrincipal,
                                fontSize = 14.sp
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                (1..5).forEach { valor ->
                                    TextButton(
                                        onClick = { calificacion = valor },
                                        enabled = AutenticacionRepository.usuarioActual != null && !esPropietario
                                    ) {
                                        Text(
                                            if (valor <= calificacion) "★" else "☆",
                                            color = colorAcento,
                                            fontSize = 24.sp
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = comentario,
                                onValueChange = { comentario = it.take(1000) },
                                label = { Text("Comentario (opcional)") },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = AutenticacionRepository.usuarioActual != null && !esPropietario,
                                minLines = 2,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = colorTextoPrincipal,
                                    unfocusedTextColor = colorTextoPrincipal,
                                    focusedContainerColor = colorFondoInput,
                                    unfocusedContainerColor = colorFondoInput,
                                    focusedBorderColor = colorAcento,
                                    unfocusedBorderColor = colorBordeSuave
                                )
                            )

                            mensaje?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }

                            Button(
                                onClick = {
                                    if (calificacion !in 1..5) {
                                        mensaje = "Selecciona una calificación de 1 a 5."
                                    } else {
                                        enviando = true
                                        mensaje = null
                                        scope.launch {
                                            try {
                                                ComercioRepository.enviarResena(datos.id, calificacion, comentario)
                                                intento++
                                            } catch (cancelacion: CancellationException) {
                                                throw cancelacion
                                            } catch (ex: Exception) {
                                                mensaje = ex.message ?: "No se pudo guardar la reseña."
                                            } finally {
                                                enviando = false
                                            }
                                        }
                                    }
                                },
                                enabled = AutenticacionRepository.usuarioActual != null && !esPropietario && !enviando,
                                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colorAcento)
                            ) {
                                if (enviando) {
                                    CircularProgressIndicator(Modifier.size(18.dp), color = Color.White)
                                } else {
                                    Text("Publicar reseña", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 9. Barra Inferior Fija Adaptativa
                    Surface(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(),
                        color = colorBarraInferior,
                        shadowElevation = 16.dp,
                        border = BorderStroke(1.dp, colorBordeSuave)
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    if (esPremium) "Mejor Valorado" else "por consumo",
                                    fontSize = 11.sp,
                                    color = colorTextoSecundario
                                )
                                Text(
                                    "Desde $precioTexto USD",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colorTextoPrincipal
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (datos.telefono.isNotBlank()) {
                                    IconButton(
                                        onClick = { uriHandler.openUri("tel:${datos.telefono.filter { it.isDigit() || it == '+' }}") },
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(if (esOscuro) Color(0xFF1E293B) else Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                                            .border(1.dp, colorBordeSuave, RoundedCornerShape(10.dp))
                                    ) {
                                        Text("📞", fontSize = 16.sp)
                                    }
                                }

                                if (esSocio && telefonoLimpio.isNotBlank()) {
                                    IconButton(
                                        onClick = { uriHandler.openUri(whatsappUrl) },
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    ) {
                                        Text("💬", fontSize = 16.sp)
                                    }
                                }

                                Button(
                                    onClick = { uriHandler.openUri(mapsUrl) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when {
                                            esPremium -> Color(0xFFF59E0B)
                                            esSocio -> Color(0xFF10B981)
                                            else -> Color(0xFF0284C7)
                                        }
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        when {
                                            esPremium -> "📍 Iniciar ruta VIP"
                                            esSocio -> "📍 Iniciar ruta"
                                            else -> "📍 Iniciar ruta"
                                        },
                                        color = if (esPremium) Color.Black else Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp
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