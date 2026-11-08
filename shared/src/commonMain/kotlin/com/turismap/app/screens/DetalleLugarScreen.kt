package com.turismap.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.components.RemoteImage
import com.turismap.app.data.models.Atractivo
import com.turismap.app.data.models.Lugar
import com.turismap.app.data.repository.Comercio
import com.turismap.app.data.repository.ComercioRepository
import com.turismap.app.theme.*

@Composable
fun DetalleLugarScreen(
    lugar: Lugar,
    onBackClick: () -> Unit = {},
    favoritoGuardado: Boolean = false,
    favoritoCargando: Boolean = false,
    onToggleFavorito: () -> Unit = {},
    onIrAGuia: () -> Unit = {},
    onVerAtractivoEnMapa: (Atractivo) -> Unit = {}
) {
    // 1. Carga reactiva protegida contra nulos desde Firestore
    var comerciosDelLugar by remember(lugar.id) { mutableStateOf<List<Comercio>>(emptyList()) }
    var cargandoComercios by remember(lugar.id) { mutableStateOf(true) }

    LaunchedEffect(lugar.id) {
        cargandoComercios = true
        try {
            val todos = ComercioRepository.obtenerPublicados()
            val idLimpio = lugar.id.orEmpty().trim().lowercase()
            val nombreLimpio = lugar.nombre.orEmpty().trim().lowercase()

            comerciosDelLugar = todos.filter { comercio ->
                val comLugar = (comercio.lugarId ?: "").trim().lowercase()
                comLugar.isNotBlank() && (
                        comLugar == idLimpio ||
                                comLugar == nombreLimpio ||
                                nombreLimpio.contains(comLugar) ||
                                idLimpio.contains(comLugar)
                        )
            }
        } catch (_: Exception) {
            comerciosDelLugar = emptyList()
        } finally {
            cargandoComercios = false
        }
    }

    // 2. Conteo seguro: (it.categoria ?: "") previene NullPointerException si Firestore trae campos vacíos
    val totalComercios = comerciosDelLugar.size
    val cantCafes = comerciosDelLugar.count {
        val cat = (it.categoria ?: "").lowercase()
        cat.contains("restaurante") || cat.contains("cafe") || cat.contains("café")
    }
    val cantAlojamientos = comerciosDelLugar.count {
        val cat = (it.categoria ?: "").lowercase()
        cat.contains("hotel") || cat.contains("alojamiento") || cat.contains("hostal") || cat.contains("cabaña")
    }
    val cantArtesanias = comerciosDelLugar.count {
        val cat = (it.categoria ?: "").lowercase()
        cat.contains("artesania") || cat.contains("artesanía") || cat.contains("tienda")
    }
    val cantExperiencias = comerciosDelLugar.count {
        val cat = (it.categoria ?: "").lowercase()
        cat.contains("atractivo") || cat.contains("experiencia") || cat.contains("tour")
    }

    val listaAtractivos = remember(lugar.atractivos) { lugar.atractivos ?: emptyList() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrisFondo)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(300.dp).background(GrisBorde)
        ) {
            val urlImagen = (lugar.imagenPortadaUrl ?: "").ifEmpty {
                listaAtractivos.firstOrNull { !it.imagenUrl.isNullOrEmpty() }?.imagenUrl ?: ""
            }
            if (urlImagen.isNotEmpty()) {
                RemoteImage(
                    url = urlImagen,
                    contentDescription = "Fondo de ${lugar.nombre}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 40.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = CircleShape, color = FondoBlancoAdaptable.copy(alpha = 0.9f),
                    modifier = Modifier.size(40.dp).clickable { onBackClick() }
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", modifier = Modifier.padding(8.dp), tint = TextoPrincipalOscuro)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(shape = CircleShape, color = FondoBlancoAdaptable.copy(alpha = 0.9f), modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir", modifier = Modifier.padding(10.dp), tint = TextoPrincipalOscuro)
                    }
                    Surface(
                        onClick = onToggleFavorito,
                        enabled = !favoritoCargando,
                        shape = CircleShape,
                        color = FondoBlancoAdaptable.copy(alpha = 0.9f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            if (favoritoGuardado) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (favoritoGuardado) "Quitar de favoritos" else "Guardar en favoritos",
                            modifier = Modifier.padding(10.dp),
                            tint = if (favoritoGuardado) TerracotaAcento else TextoPrincipalOscuro
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = FondoBlancoAdaptable.copy(alpha = 0.9f),
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
                    .widthIn(max = 300.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 15.dp, vertical = 6.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = VerdeTextoAdaptable, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(9.dp))
                    Text(
                        text = lugar.ubicacionCorta.orEmpty(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = VerdeTextoAdaptable
                    )
                }
            }
        }

        Column(modifier = Modifier.offset(y = (-20).dp)) {
            Surface(
                modifier = Modifier.fillMaxWidth(), color = FondoBlancoAdaptable, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = GrisTextoSecundario, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = lugar.ubicacionLarga.orEmpty(),
                            fontSize = 12.sp,
                            color = GrisTextoSecundario,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = lugar.nombre.orEmpty(), fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = TextoPrincipalOscuro)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = lugar.subtitulo.orEmpty(), fontSize = 14.sp, color = GrisTextoSecundario)

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = GrisTextoSecundario, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = lugar.elevacion.orEmpty(), fontSize = 12.sp, color = TextoPrincipalOscuro, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        }
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = GrisTextoSecundario, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = lugar.clima.orEmpty(), fontSize = 12.sp, color = TextoPrincipalOscuro, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        }
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = TerracotaAcento, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            val resenaTexto = if (lugar.cantidadResenas > 0) "${lugar.calificacion} (${lugar.cantidadResenas})" else "${lugar.calificacion}"
                            Text(text = resenaTexto, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Text(text = "Historia & Esencia", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextoPrincipalOscuro)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = lugar.historia.orEmpty(), fontSize = 14.sp, lineHeight = 22.sp, color = GrisTextoSecundario)
                    Spacer(modifier = Modifier.height(20.dp))

                    Surface(shape = RoundedCornerShape(12.dp), color = VerdeTextoAdaptable.copy(alpha = 0.1f), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = VerdeTextoAdaptable)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = lugar.mejorTemporadaTitulo.orEmpty(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = VerdeTextoAdaptable)
                                Text(text = lugar.mejorTemporadaDesc.orEmpty(), fontSize = 13.sp, color = TextoPrincipalOscuro)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Atractivos Imperdibles",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextoPrincipalOscuro,
                            modifier = Modifier.weight(1f)
                        )
                        Text(text = "${listaAtractivos.size} Destacados", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VerdeTextoAdaptable)
                    }
                }
            }

            // Carrusel de Atractivos
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.offset(y = (-20).dp)
            ) {
                items(listaAtractivos) { atractivo ->
                    AtractivoCardBase(
                        atractivo = atractivo,
                        onVerEnMapa = { onVerAtractivoEnMapa(atractivo) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Directorio 100% conectado a Firebase Firestore
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = VerdePrimario)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "DIRECTORIO LOCAL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "¿Planeas tu visita a ${lugar.nombre}?",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (totalComercios > 0) {
                            "Explora los $totalComercios comercios y servicios verificados disponibles en ${lugar.nombre}."
                        } else {
                            "Actualmente hay 0 comercios registrados en ${lugar.nombre}. Próximamente se integrarán nuevos establecimientos a la red."
                        },
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DirectorioIconoGen(
                            icono = Icons.Default.LocalCafe,
                            texto = "$cantCafes Cafeterías y Bares",
                            modifier = Modifier.weight(1f)
                        )
                        DirectorioIconoGen(
                            icono = Icons.Default.Home,
                            texto = "$cantAlojamientos Hoteles y Cabañas",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DirectorioIconoGen(
                            icono = Icons.Default.Palette,
                            texto = "$cantArtesanias Artesanías y Tiendas",
                            modifier = Modifier.weight(1f)
                        )
                        DirectorioIconoGen(
                            icono = Icons.Default.DirectionsWalk,
                            texto = "$cantExperiencias Guías y Operadores",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onIrAGuia,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TerracotaAcento),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (totalComercios > 0) "Ver Guía de Comercio y Ocio →" else "Explorar Guía General →",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun AtractivoCardBase(
    atractivo: Atractivo,
    onVerEnMapa: () -> Unit = {}
) {
    val colorEtiqueta = remember(atractivo.etiquetaColorHex) {
        try {
            Color(atractivo.etiquetaColorHex)
        } catch (_: Exception) {
            Color(0xFF1B4934)
        }
    }

    Card(
        modifier = Modifier
            .width(200.dp)
            .heightIn(min = 240.dp)
            .clickable { onVerEnMapa() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FondoBlancoAdaptable),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(110.dp).background(GrisBorde)) {
                val imagenUrl = atractivo.imagenUrl.orEmpty()
                if (imagenUrl.isNotEmpty()) {
                    RemoteImage(
                        url = imagenUrl,
                        contentDescription = atractivo.titulo.orEmpty(),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp), color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(10.dp).align(Alignment.TopEnd)
                ) {
                    Text(
                        text = atractivo.etiqueta.orEmpty(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorEtiqueta,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = atractivo.titulo.orEmpty(), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextoPrincipalOscuro)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = atractivo.descripcion.orEmpty(), fontSize = 12.sp, color = GrisTextoSecundario, maxLines = 2)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${atractivo.accion.orEmpty()} 🗺️",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TerracotaTextoAdaptable
                    )
                }
            }
        }
    }
}

@Composable
fun DirectorioIconoGen(icono: ImageVector, texto: String, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Surface(shape = RoundedCornerShape(8.dp), color = Color.White.copy(alpha = 0.15f), modifier = Modifier.size(36.dp)) {
            Icon(icono, contentDescription = null, tint = Color.White, modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(texto, fontSize = 12.sp, color = Color.White, lineHeight = 16.sp, modifier = Modifier.weight(1f))
    }
}