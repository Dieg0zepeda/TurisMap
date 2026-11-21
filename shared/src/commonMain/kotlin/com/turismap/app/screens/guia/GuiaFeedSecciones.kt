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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.components.RemoteImage
import com.turismap.app.data.repository.Comercio
import com.turismap.app.theme.FondoBlancoAdaptable

// Shimmer sin errores de sintaxis
internal fun Modifier.shimmerEffect(): Modifier = composed {
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

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFE0E0E0),
                Color(0xFFF5F5F5),
                Color(0xFFE0E0E0)
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    ).onGloballyPositioned { size = it.size }
}

@Composable
internal fun SeccionExperienciasDestacadas(
    comerciosDestacados: List<Comercio>,
    mapaMunicipios: Map<String, String>,
    paleta: GuiaPaleta,
    onComercioClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("👑", fontSize = 16.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                text = "EXPERIENCIAS DESTACADAS",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = paleta.verdeOscuroTurisMap,
                letterSpacing = 0.5.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(comerciosDestacados, key = { "dest_${it.id}" }) { destacado ->
                val muniNombre = mapaMunicipios[destacado.lugarId] ?: "Ruta de las Flores"
                TarjetaMiniDestacada(
                    comercio = destacado,
                    municipio = muniNombre,
                    paleta = paleta,
                    onClick = { onComercioClick(destacado.id) }
                )
            }
        }
    }
}

@Composable
internal fun EncabezadoFeedGuia(
    municipioSeleccionado: String,
    totalLugares: Int,
    paleta: GuiaPaleta
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (municipioSeleccionado == "Todos") "Guía de Comercios" else "Comercios en $municipioSeleccionado",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = paleta.airbnbBlack
        )
        Text(
            text = "$totalLugares lugares",
            fontSize = 12.sp,
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
            text = errorCarga ?: "Error al conectar",
            color = paleta.airbnbGray,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onReintentar,
            colors = ButtonDefaults.buttonColors(containerColor = paleta.verdeTurisMap)
        ) {
            Text("Reintentar", color = Color.White)
        }
    }
}

@Composable
internal fun EstadoVacioGuia(paleta: GuiaPaleta) {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No se encontraron negocios con los filtros seleccionados.",
            color = paleta.airbnbGray,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ---------------------------------------------------------
// TARJETA DEL CARRUSEL SUPERIOR DESTACADO
// ---------------------------------------------------------
@Composable
internal fun TarjetaMiniDestacada(
    comercio: Comercio,
    municipio: String,
    paleta: GuiaPaleta,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FondoBlancoAdaptable),
        border = BorderStroke(1.dp, paleta.verdeTurisMap.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                RemoteImage(
                    url = comercio.imagenUrls.firstOrNull() ?: "",
                    contentDescription = comercio.nombre,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (comercio.planInt == 3) {
                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Text("👑 Socio Premium", color = paleta.verdeTurisMap, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (comercio.planInt == 2) {
                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Text("✓ Socio TurisMap", color = paleta.verdeTurisMap, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = comercio.nombre,
                    color = paleta.airbnbBlack,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "$municipio · ${comercio.categoria}",
                    color = paleta.airbnbGray,
                    fontSize = 11.5.sp,
                    maxLines = 1
                )
            }
        }
    }
}

// ---------------------------------------------------------
// ESQUELETO DE CARGA
// ---------------------------------------------------------
@Composable
internal fun TarjetaEsqueletoFeed() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
    }
}
