package com.turismap.app.screens.mapa

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.components.RemoteImage
import com.turismap.app.data.models.Lugar
import com.turismap.app.data.repository.Comercio
import com.turismap.app.theme.FondoBlancoAdaptable

@Composable
internal fun TarjetaMarcadorMapa(
    lugarSeleccionado: Lugar?,
    comercioSeleccionado: Comercio?,
    municipiosMap: Map<String, Lugar>,
    alturaMaximaTarjeta: Dp,
    paleta: MapaPaleta,
    onCerrar: () -> Unit,
    onAbrirGoogleMaps: (url: String) -> Unit,
    onAbrirDetalle: (Lugar) -> Unit,
    onComercioClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = lugarSeleccionado != null,
        modifier = modifier
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
                border = BorderStroke(1.dp, paleta.airbnbBorde),
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
                                    .background(paleta.airbnbFondo)
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
                                color = paleta.turismapPrimario,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = lugar.nombre,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = paleta.airbnbOscuro
                            )
                        }
                        IconButton(
                            onClick = onCerrar,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar detalles",
                                tint = paleta.airbnbGris
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    val direccion = listOf(lugar.ubicacionCorta, municipio)
                        .filter(String::isNotBlank)
                        .distinct()
                        .joinToString(" · ")
                    if (direccion.isNotBlank()) {
                        Text(direccion, fontSize = 13.sp, color = paleta.airbnbGris)
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
                                color = paleta.airbnbGris
                            )
                        }
                    }

                    val descripcion = comercio?.descripcion?.takeIf(String::isNotBlank)
                        ?: lugar.historia
                    if (descripcion.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(descripcion, fontSize = 14.sp, color = paleta.airbnbGris)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = paleta.airbnbBorde, thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onAbrirGoogleMaps(urlGoogleMaps(comercio, lugar, municipio)) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = paleta.turismapPrimario),
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
                                border = BorderStroke(1.dp, paleta.airbnbBorde),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Ver detalles del comercio")
                            }
                        } else {
                            Button(
                                onClick = { onAbrirDetalle(lugar) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = paleta.airbnbOscuro),
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
}
