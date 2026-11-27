package com.turismap.app.screens.guia

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.theme.PinoContenedorAlto
import com.turismap.app.theme.PinoSuperficie
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.btn_reintentar
import turismap.shared.generated.resources.guia_encabezado_municipio
import turismap.shared.generated.resources.guia_encabezado_todos
import turismap.shared.generated.resources.guia_error_conectar
import turismap.shared.generated.resources.guia_estado_vacio
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
    val base = if (esOscuro) PinoSuperficie else Color(0xFFE5E7EB)
    val brillo = if (esOscuro) PinoContenedorAlto else Color(0xFFF3F4F6)

    background(
        brush = Brush.linearGradient(
            colors = listOf(base, brillo, base),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    ).onGloballyPositioned { size = it.size }
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
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(stringResource(Res.string.btn_reintentar))
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
