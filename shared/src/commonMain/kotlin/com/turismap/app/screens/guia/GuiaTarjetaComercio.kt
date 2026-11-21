package com.turismap.app.screens.guia

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.components.RemoteImage
import com.turismap.app.data.repository.Comercio
import com.turismap.app.theme.FondoBlancoAdaptable

// ---------------------------------------------------------
// TARJETA PRINCIPAL CON LÍMITE DE FOTOS (2 BÁSICO / 4 SOCIO / 8 PREMIUM)
// ---------------------------------------------------------
@Composable
internal fun TarjetaComercioInstagram(
    comercio: Comercio,
    municipio: String,
    esFavorito: Boolean,
    paleta: GuiaPaleta,
    onToggleFavorito: () -> Unit,
    onClick: () -> Unit,
    onAccionContacto: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

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
            color = if (esPremium) paleta.verdeTurisMap.copy(alpha = 0.7f) else paleta.airbnbBorder
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
                            .background(paleta.airbnbLightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(comercio.categoria, color = paleta.airbnbGray, fontWeight = FontWeight.Bold)
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
                            color = paleta.verdeTurisMap,
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
                            border = BorderStroke(1.dp, paleta.verdeTurisMap),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    "✓ Socio TurisMap",
                                    color = paleta.verdeTurisMap,
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
                            tint = if (esFavorito) paleta.airbnbPrimary else Color.White,
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
                            color = paleta.airbnbBlack,
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
                            tint = paleta.verdeTurisMap,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = calificacion,
                            color = paleta.airbnbBlack,
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
                        color = paleta.airbnbGray,
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
                        color = paleta.airbnbBlack,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (comercio.telefono.isNotBlank()) {
                        val telLimpio = comercio.telefono.filter { it.isDigit() }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (esSocio || esPremium) {
                                Surface(
                                    color = paleta.verdeTurisMap.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable {
                                        val url = if (telLimpio.startsWith("503")) "https://wa.me/$telLimpio" else "https://wa.me/503$telLimpio"
                                        try { uriHandler.openUri(url) } catch (e: Exception) { onAccionContacto() }
                                    }
                                ) {
                                    Text(
                                        text = "💬 WhatsApp",
                                        color = paleta.verdeOscuroTurisMap,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Surface(
                                color = if (paleta.esOscuro) Color(0xFF2C2C2C) else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable { onAccionContacto() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Llamar",
                                    tint = paleta.airbnbBlack,
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
