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
import com.turismap.app.components.rememberCompartirTexto
import com.turismap.app.data.models.Atractivo
import com.turismap.app.data.models.Lugar
import com.turismap.app.data.repository.Comercio
import com.turismap.app.data.repository.ComercioRepository
import com.turismap.app.screens.mapa.normalizarTextoMapa
import com.turismap.app.theme.*
import kotlinx.coroutines.CancellationException
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.detalle_lugar_atractivos_destacados
import turismap.shared.generated.resources.detalle_lugar_atractivos_titulo
import turismap.shared.generated.resources.detalle_lugar_btn_explorar_guia
import turismap.shared.generated.resources.detalle_lugar_btn_ver_guia
import turismap.shared.generated.resources.detalle_lugar_cd_atras
import turismap.shared.generated.resources.detalle_lugar_cd_compartir
import turismap.shared.generated.resources.detalle_lugar_cd_fondo
import turismap.shared.generated.resources.detalle_lugar_cd_guardar_favorito
import turismap.shared.generated.resources.detalle_lugar_cd_quitar_favorito
import turismap.shared.generated.resources.detalle_lugar_compartir_texto
import turismap.shared.generated.resources.detalle_lugar_directorio_alojamientos
import turismap.shared.generated.resources.detalle_lugar_directorio_artesanias
import turismap.shared.generated.resources.detalle_lugar_directorio_badge
import turismap.shared.generated.resources.detalle_lugar_directorio_cafeterias
import turismap.shared.generated.resources.detalle_lugar_directorio_cargando
import turismap.shared.generated.resources.detalle_lugar_directorio_desc_con_comercios
import turismap.shared.generated.resources.detalle_lugar_directorio_desc_sin_comercios
import turismap.shared.generated.resources.detalle_lugar_directorio_guias
import turismap.shared.generated.resources.detalle_lugar_directorio_titulo
import turismap.shared.generated.resources.detalle_lugar_historia_titulo

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
    val compartirTexto = rememberCompartirTexto()
    val textoCompartirBase = stringResource(Res.string.detalle_lugar_compartir_texto, lugar.nombre)

    LaunchedEffect(lugar.id, lugar.nombre) {
        cargandoComercios = true
        try {
            val candidatos = ComercioRepository.obtenerPublicadosPorLugar(
                lugarId = lugar.id,
                nombreLugar = lugar.nombre
            )
            val idNorm = normalizarTextoMapa(lugar.id)
            val nombreNorm = normalizarTextoMapa(lugar.nombre)

            comerciosDelLugar = candidatos.filter { comercio ->
                val refsComercio = listOf(comercio.lugarId, comercio.municipio)
                    .map(::normalizarTextoMapa)
                    .filter(String::isNotEmpty)
                refsComercio.any { ref ->
                    (idNorm.isNotEmpty() && (ref == idNorm || ref.contains(idNorm) || idNorm.contains(ref))) ||
                        (nombreNorm.isNotEmpty() && (ref == nombreNorm || ref.contains(nombreNorm) || nombreNorm.contains(ref)))
                }
            }
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (_: Exception) {
            comerciosDelLugar = emptyList()
        } finally {
            cargandoComercios = false
        }
    }

    // 2. Conteo seguro normalizado (incluye tildes y ñ -> n)
    val totalComercios = comerciosDelLugar.size
    val cantCafes = comerciosDelLugar.count {
        val cat = normalizarTextoMapa(it.categoria)
        cat.contains("restaurante") || cat.contains("cafe") || cat.contains("comida") || cat.contains("gastronom")
    }
    val cantAlojamientos = comerciosDelLugar.count {
        val cat = normalizarTextoMapa(it.categoria)
        cat.contains("hotel") || cat.contains("alojamiento") || cat.contains("hostal") || cat.contains("cabana") || cat.contains("hosped")
    }
    val cantArtesanias = comerciosDelLugar.count {
        val cat = normalizarTextoMapa(it.categoria)
        cat.contains("artesania") || cat.contains("tienda") || cat.contains("souvenir")
    }
    val cantExperiencias = comerciosDelLugar.count {
        val cat = normalizarTextoMapa(it.categoria)
        cat.contains("atractivo") || cat.contains("experiencia") || cat.contains("tour") || cat.contains("aventura")
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
                    contentDescription = stringResource(Res.string.detalle_lugar_cd_fondo, lugar.nombre),
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
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = stringResource(Res.string.detalle_lugar_cd_atras),
                        modifier = Modifier.padding(8.dp),
                        tint = TextoPrincipalOscuro
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        onClick = {
                            val textoCompleto = listOf(
                                textoCompartirBase,
                                lugar.subtitulo.trim()
                            ).filter(String::isNotBlank).joinToString(" — ")
                            compartirTexto(textoCompleto, lugar.nombre)
                        },
                        shape = CircleShape,
                        color = FondoBlancoAdaptable.copy(alpha = 0.9f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = stringResource(Res.string.detalle_lugar_cd_compartir),
                            modifier = Modifier.padding(10.dp),
                            tint = TextoPrincipalOscuro
                        )
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
                            contentDescription = stringResource(
                                if (favoritoGuardado) {
                                    Res.string.detalle_lugar_cd_quitar_favorito
                                } else {
                                    Res.string.detalle_lugar_cd_guardar_favorito
                                }
                            ),
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
                    Text(
                        text = stringResource(Res.string.detalle_lugar_historia_titulo),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoPrincipalOscuro
                    )
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
                            text = stringResource(Res.string.detalle_lugar_atractivos_titulo),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextoPrincipalOscuro,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = stringResource(Res.string.detalle_lugar_atractivos_destacados, listaAtractivos.size),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = VerdeTextoAdaptable
                        )
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
                        text = stringResource(Res.string.detalle_lugar_directorio_badge),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(Res.string.detalle_lugar_directorio_titulo, lugar.nombre),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (cargandoComercios) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(Res.string.detalle_lugar_directorio_cargando, lugar.nombre),
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    } else {
                        Text(
                            text = if (totalComercios > 0) {
                                stringResource(
                                    Res.string.detalle_lugar_directorio_desc_con_comercios,
                                    totalComercios,
                                    lugar.nombre
                                )
                            } else {
                                stringResource(
                                    Res.string.detalle_lugar_directorio_desc_sin_comercios,
                                    lugar.nombre
                                )
                            },
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            DirectorioIconoGen(
                                icono = Icons.Default.LocalCafe,
                                texto = stringResource(Res.string.detalle_lugar_directorio_cafeterias, cantCafes),
                                modifier = Modifier.weight(1f)
                            )
                            DirectorioIconoGen(
                                icono = Icons.Default.Home,
                                texto = stringResource(Res.string.detalle_lugar_directorio_alojamientos, cantAlojamientos),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            DirectorioIconoGen(
                                icono = Icons.Default.Palette,
                                texto = stringResource(Res.string.detalle_lugar_directorio_artesanias, cantArtesanias),
                                modifier = Modifier.weight(1f)
                            )
                            DirectorioIconoGen(
                                icono = Icons.Default.DirectionsWalk,
                                texto = stringResource(Res.string.detalle_lugar_directorio_guias, cantExperiencias),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onIrAGuia,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TerracotaAcento),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(
                                if (totalComercios > 0) {
                                    Res.string.detalle_lugar_btn_ver_guia
                                } else {
                                    Res.string.detalle_lugar_btn_explorar_guia
                                }
                            ),
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
        val hex = atractivo.etiquetaColorHex
        if (hex == 0L) {
            Color(0xFF1B4934)
        } else {
            try {
                val hexConAlpha = if ((hex and 0xFF000000L) == 0L) hex or 0xFF000000L else hex
                Color(hexConAlpha)
            } catch (_: Exception) {
                Color(0xFF1B4934)
            }
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