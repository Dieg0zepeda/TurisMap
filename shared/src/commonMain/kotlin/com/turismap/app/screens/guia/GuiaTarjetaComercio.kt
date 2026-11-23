package com.turismap.app.screens.guia

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Star
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.components.RemoteImage
import com.turismap.app.data.repository.Comercio
import com.turismap.app.theme.ObsidianaFondo
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.detalle_exp_precio_desde_usd
import turismap.shared.generated.resources.guia_calificacion_nuevo
import turismap.shared.generated.resources.guia_cd_favorito

@Composable
internal fun TarjetaComercioGuia(
    comercio: Comercio,
    municipio: String,
    categoriaEtiqueta: String,
    esFavorito: Boolean,
    favoritoEnProceso: Boolean,
    paleta: GuiaPaleta,
    onToggleFavorito: () -> Unit,
    onClick: () -> Unit
) {
    // Respeta el tope exacto según suscripción (2 Básico / 4 Socio / 8 Premium)
    val fotos = comercio.imagenUrls
        .filter { it.startsWith("http://") || it.startsWith("https://") }
        .take(comercio.maxFotosPermitidas)

    val calificacion = formatearCalificacionComercio(comercio.calificacionPromedio)
        ?: stringResource(Res.string.guia_calificacion_nuevo)
    val municipioLimpio = municipio.trim()
    val categoriaLimpia = categoriaEtiqueta.trim()

    val precioDesdeValido = comercio.precioDesde?.takeIf { it > 0.0 }
    val detalleBreve = when {
        precioDesdeValido != null ->
            stringResource(Res.string.detalle_exp_precio_desde_usd, formatearPrecioComercio(precioDesdeValido))
        comercio.horario.isNotBlank() -> comercio.horario.trim()
        comercio.descripcion.isNotBlank() -> comercio.descripcion.trim()
        else -> null
    }
    val bordeTarjeta = when (comercio.planInt) {
        3 -> paleta.ambarAcento.copy(alpha = if (paleta.esOscuro) 0.38f else 0.32f)
        2 -> paleta.bordeEnfocado.copy(alpha = if (paleta.esOscuro) 0.40f else 0.28f)
        else -> paleta.airbnbBorder
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = paleta.superficieTarjeta),
        border = BorderStroke(1.dp, bordeTarjeta),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column {
            // Cabecera panorámica compacta de 144.dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(144.dp)
            ) {
                if (fotos.size > 1) {
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

                    GradienteSuperiorTarjeta(modifier = Modifier.align(Alignment.TopCenter))

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                            .background(ObsidianaFondo.copy(alpha = 0.56f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(fotos.size) { index ->
                            val esActivo = pagerState.currentPage == index
                            Box(
                                modifier = Modifier
                                    .size(if (esActivo) 6.dp else 4.5.dp)
                                    .clip(CircleShape)
                                    .background(if (esActivo) Color.White else Color.White.copy(alpha = 0.45f))
                            )
                        }
                    }
                } else if (fotos.size == 1) {
                    RemoteImage(
                        url = fotos.first(),
                        contentDescription = comercio.nombre,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    GradienteSuperiorTarjeta(modifier = Modifier.align(Alignment.TopCenter))
                } else {
                    PlaceholderImagenCategoria(
                        categoria = comercio.categoria,
                        paleta = paleta,
                        modifier = Modifier.fillMaxSize(),
                        iconoSize = 34.dp
                    )
                }

                // Badge de plan en esquina superior izquierda + Favorito en esquina superior derecha
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (comercio.planInt == 3 || comercio.planInt == 2) {
                        BadgePlanComercio(
                            planInt = comercio.planInt,
                            paleta = paleta
                        )
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(ObsidianaFondo.copy(alpha = 0.58f))
                            .border(0.8.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                            .clickable(enabled = !favoritoEnProceso, onClick = onToggleFavorito),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (esFavorito) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = stringResource(Res.string.guia_cd_favorito),
                            tint = if (esFavorito) paleta.airbnbPrimary else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Bloque de texto compacto y escaneable
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // Fila 1: Nombre + Calificación a la derecha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = comercio.nombre,
                        color = paleta.airbnbBlack,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = paleta.ambarAcento,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = calificacion,
                            color = paleta.airbnbBlack,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }

                // Fila 2: Etiqueta de categoría tonal salvia + municipio legible
                if (categoriaLimpia.isNotEmpty() || municipioLimpio.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (categoriaLimpia.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = paleta.etiquetaFondo,
                                border = BorderStroke(0.8.dp, paleta.etiquetaBorde)
                            ) {
                                Text(
                                    text = categoriaLimpia,
                                    color = paleta.etiquetaTexto,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        if (municipioLimpio.isNotEmpty()) {
                            Text(
                                text = municipioLimpio,
                                color = paleta.airbnbGray,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                    }
                }

                // Fila 3 (solo si tiene dato): precio o detalle breve discreto en 1 línea
                if (!detalleBreve.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = detalleBreve,
                        color = if (precioDesdeValido != null) {
                            paleta.airbnbBlack.copy(alpha = 0.88f)
                        } else {
                            paleta.airbnbGray
                        },
                        fontSize = 12.5.sp,
                        fontWeight = if (precioDesdeValido != null) FontWeight.Medium else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun GradienteSuperiorTarjeta(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(
                Brush.verticalGradient(
                    listOf(ObsidianaFondo.copy(alpha = 0.44f), Color.Transparent)
                )
            )
    )
}
