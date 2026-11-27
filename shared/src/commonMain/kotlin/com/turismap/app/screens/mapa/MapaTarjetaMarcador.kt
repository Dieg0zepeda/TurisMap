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
import com.turismap.app.theme.FondoBlancoAdaptable
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.mapa_atractivo_acceso_publico
import turismap.shared.generated.resources.mapa_atractivo_sin_descripcion
import turismap.shared.generated.resources.mapa_btn_como_llegar
import turismap.shared.generated.resources.mapa_btn_ver_comercio
import turismap.shared.generated.resources.mapa_btn_ver_destino
import turismap.shared.generated.resources.mapa_calificacion_resenas
import turismap.shared.generated.resources.mapa_cd_cerrar_detalles

@Composable
internal fun TarjetaMarcadorMapa(
    marcadorSeleccionado: MarcadorMapa?,
    municipiosMap: Map<String, Lugar>,
    categoriasMap: Map<String, String> = emptyMap(),
    alturaMaximaTarjeta: Dp,
    paleta: MapaPaleta,
    onCerrar: () -> Unit,
    onAbrirGoogleMaps: (url: String) -> Unit,
    onAbrirDetalle: (Lugar) -> Unit,
    onComercioClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = marcadorSeleccionado != null,
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        marcadorSeleccionado?.let { marcador ->
            val imagenUrl = when (marcador) {
                is MarcadorMapa.Comercio -> marcador.comercio.imagenUrls.firstOrNull().orEmpty()
                is MarcadorMapa.Destino -> marcador.lugar.imagenPortadaUrl
                is MarcadorMapa.AtractivoPublico -> marcador.atractivo.imagenPrincipal
            }
            val colorEtiqueta = when (marcador) {
                is MarcadorMapa.AtractivoPublico -> colorCategoriaAtractivo(marcador.categoria)
                is MarcadorMapa.Comercio,
                is MarcadorMapa.Destino -> paleta.turismapPrimario
            }
            val textoEtiqueta = when (marcador) {
                is MarcadorMapa.Comercio -> categoriasMap[marcador.comercio.categoria.trim()]
                    ?.takeIf(String::isNotBlank)
                    ?: marcador.etiquetaCategoria.ifBlank {
                        marcador.comercio.categoria.replaceFirstChar { c -> c.uppercase() }
                    }

                is MarcadorMapa.Destino -> stringResource(marcador.lugar.categoriaResuelta.etiquetaRes)
                is MarcadorMapa.AtractivoPublico -> stringResource(marcador.categoria.etiquetaRes)
            }
            val direccion = when (marcador) {
                is MarcadorMapa.Comercio -> {
                    val municipio = municipiosMap[marcador.comercio.lugarId.trim()]?.nombre
                        ?: marcador.nombreMunicipio.ifBlank { null }
                    listOf(marcador.comercio.direccion, municipio.orEmpty())
                        .filter(String::isNotBlank)
                        .distinct()
                        .joinToString(" · ")
                }

                is MarcadorMapa.Destino -> listOf(
                    marcador.lugar.ubicacionCorta,
                    marcador.lugar.ubicacionLarga
                ).filter(String::isNotBlank).distinct().joinToString(" · ")

                is MarcadorMapa.AtractivoPublico -> listOf(
                    marcador.atractivo.municipio,
                    stringResource(Res.string.mapa_atractivo_acceso_publico)
                ).filter(String::isNotBlank).distinct().joinToString(" · ")
            }
            val descripcion = when (marcador) {
                is MarcadorMapa.Comercio -> marcador.comercio.descripcion
                is MarcadorMapa.Destino -> marcador.lugar.historia
                is MarcadorMapa.AtractivoPublico -> marcador.atractivo.descripcion.ifBlank {
                    stringResource(Res.string.mapa_atractivo_sin_descripcion)
                }
            }

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
                                    contentDescription = marcador.titulo,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = textoEtiqueta,
                                color = colorEtiqueta,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = marcador.titulo,
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
                                contentDescription = stringResource(Res.string.mapa_cd_cerrar_detalles),
                                tint = paleta.airbnbGris
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    if (direccion.isNotBlank()) {
                        Text(direccion, fontSize = 13.sp, color = paleta.airbnbGris)
                    }

                    if (marcador is MarcadorMapa.Comercio &&
                        (marcador.comercio.calificacionPromedio ?: 0.0) > 0.0
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB800),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(
                                    Res.string.mapa_calificacion_resenas,
                                    marcador.comercio.calificacionPromedio.toString(),
                                    marcador.comercio.cantidadResenas ?: 0
                                ),
                                fontSize = 13.sp,
                                color = paleta.airbnbGris
                            )
                        }
                    }

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
                            onClick = {
                                onAbrirGoogleMaps(urlGoogleMapsMarcador(marcador, municipiosMap))
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = paleta.turismapPrimario,
                                contentColor = paleta.sobrePrimario
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = null,
                                tint = paleta.sobrePrimario,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(Res.string.mapa_btn_como_llegar),
                                color = paleta.sobrePrimario
                            )
                        }

                        when (marcador) {
                            is MarcadorMapa.Comercio -> {
                                OutlinedButton(
                                    onClick = { onComercioClick(marcador.comercio.id) },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, paleta.airbnbBorde),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(Res.string.mapa_btn_ver_comercio))
                                }
                            }

                            is MarcadorMapa.Destino -> {
                                Button(
                                    onClick = { onAbrirDetalle(marcador.lugar) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = paleta.airbnbOscuro),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = stringResource(Res.string.mapa_btn_ver_destino),
                                        color = FondoBlancoAdaptable
                                    )
                                }
                            }

                            is MarcadorMapa.AtractivoPublico -> {
                                // Sin botón de detalle de municipio: la experiencia completa de galería y ruta se añade en Bloque 3
                            }
                        }
                    }
                }
            }
        }
    }
}
