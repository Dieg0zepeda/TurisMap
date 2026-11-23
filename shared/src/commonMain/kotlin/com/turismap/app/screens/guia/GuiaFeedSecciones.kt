package com.turismap.app.screens.guia

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.components.RemoteImage
import com.turismap.app.data.repository.Comercio
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.auth_ruta_flores
import turismap.shared.generated.resources.btn_reintentar
import turismap.shared.generated.resources.guia_encabezado_municipio
import turismap.shared.generated.resources.guia_encabezado_todos
import turismap.shared.generated.resources.guia_error_conectar
import turismap.shared.generated.resources.guia_estado_vacio
import turismap.shared.generated.resources.guia_seccion_destacadas
import turismap.shared.generated.resources.guia_total_lugares

internal fun Modifier.shimmerEffect(esOscuro: Boolean = false): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val widthPx = if (size.width > 0) size.width.toFloat() else 300f
    val startOffsetX by transition.animateFloat(
        initialValue = -2f * widthPx,
        targetValue = 2f * widthPx,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing)
        ),
        label = "shimmer_x"
    )
    val base = if (esOscuro) Color(0xFF2A2E2C) else Color(0xFFE5E7EB)
    val brillo = if (esOscuro) Color(0xFF383E3B) else Color(0xFFF3F4F6)

    background(
        brush = Brush.linearGradient(
            colors = listOf(base, brillo, base),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    ).onGloballyPositioned { size = it.size }
}

@Composable
internal fun SeccionExperienciasDestacadas(
    comerciosDestacados: List<Comercio>,
    resolverUbicacion: (Comercio) -> String,
    resolverCategoria: (String) -> String,
    paleta: GuiaPaleta,
    onComercioClick: (String) -> Unit
) {
    val municipioPredeterminado = stringResource(Res.string.auth_ruta_flores)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = paleta.ambarAcento,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(Res.string.guia_seccion_destacadas),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = paleta.airbnbGray,
                letterSpacing = 0.7.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(comerciosDestacados, key = { "dest_${it.id}" }) { destacado ->
                val muniNombre = resolverUbicacion(destacado).ifBlank { municipioPredeterminado }
                val categoriaNombre = resolverCategoria(destacado.categoria)
                TarjetaMiniDestacada(
                    comercio = destacado,
                    municipio = muniNombre,
                    categoriaEtiqueta = categoriaNombre,
                    paleta = paleta,
                    onClick = { onComercioClick(destacado.id) }
                )
            }
        }
    }
}

@Composable
internal fun EncabezadoFeedGuia(
    municipioSeleccionado: String?,
    totalLugares: Int,
    paleta: GuiaPaleta
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (municipioSeleccionado == null) {
                stringResource(Res.string.guia_encabezado_todos)
            } else {
                stringResource(Res.string.guia_encabezado_municipio, municipioSeleccionado)
            },
            fontSize = 16.5.sp,
            fontWeight = FontWeight.Bold,
            color = paleta.airbnbBlack,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(Res.string.guia_total_lugares, totalLugares),
            fontSize = 12.5.sp,
            color = paleta.airbnbGray,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
internal fun EstadoErrorGuia(
    errorCarga: String?,
    paleta: GuiaPaleta,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = errorCarga ?: stringResource(Res.string.guia_error_conectar),
            color = paleta.airbnbGray,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onReintentar,
            colors = ButtonDefaults.buttonColors(containerColor = paleta.filtroActivoFondo)
        ) {
            Text(stringResource(Res.string.btn_reintentar), color = Color.White)
        }
    }
}

@Composable
internal fun EstadoVacioGuia(
    paleta: GuiaPaleta,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(Res.string.guia_estado_vacio),
            color = paleta.airbnbGray,
            fontSize = 14.5.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
internal fun TarjetaMiniDestacada(
    comercio: Comercio,
    municipio: String,
    categoriaEtiqueta: String,
    paleta: GuiaPaleta,
    onClick: () -> Unit
) {
    val urlPortada = comercio.imagenUrls.firstOrNull {
        it.startsWith("http://") || it.startsWith("https://")
    }
    val subtitulo = formatearSubtituloComercio(municipio, categoriaEtiqueta)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = paleta.superficieTarjeta),
        border = BorderStroke(0.8.dp, paleta.airbnbBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .width(248.dp)
            .height(148.dp)
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (urlPortada != null) {
                RemoteImage(
                    url = urlPortada,
                    contentDescription = comercio.nombre,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                PlaceholderImagenCategoria(
                    categoria = comercio.categoria,
                    paleta = paleta,
                    modifier = Modifier.fillMaxSize(),
                    iconoSize = 30.dp,
                    desplazamientoInferior = 26.dp
                )
            }

            // Gradiente inferior oscuro para legibilidad consistente sobre imagen o placeholder
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Black.copy(alpha = 0.08f),
                                0.42f to Color.Transparent,
                                1.0f to Color.Black.copy(alpha = if (urlPortada != null) 0.80f else 0.68f)
                            )
                        )
                    )
            )

            BadgePlanComercio(
                planInt = comercio.planInt,
                paleta = paleta,
                compacto = true,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = comercio.nombre,
                    color = Color.White,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitulo.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitulo,
                        color = Color.White.copy(alpha = 0.88f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
internal fun TarjetaEsqueletoFeed(esOscuro: Boolean = false) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(144.dp)
                .shimmerEffect(esOscuro)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.58f)
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect(esOscuro)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.38f)
                .height(13.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect(esOscuro)
        )
    }
}
