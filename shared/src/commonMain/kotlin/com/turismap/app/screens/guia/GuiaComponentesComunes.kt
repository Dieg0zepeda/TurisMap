package com.turismap.app.screens.guia

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Hotel
import androidx.compose.material.icons.rounded.Landscape
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.guia_badge_socio_premium
import turismap.shared.generated.resources.guia_badge_socio_turismap

internal fun iconoCategoriaComercio(categoria: String?): ImageVector {
    val cat = normalizarTexto(categoria)
    return when {
        cat.contains("restauran") || cat.contains("cafe") || cat.contains("comida") || cat.contains("gastronom") ->
            Icons.Rounded.Restaurant
        cat.contains("hotel") || cat.contains("alojam") || cat.contains("hosped") || cat.contains("posada") || cat.contains("cabana") ->
            Icons.Rounded.Hotel
        cat.contains("artesan") || cat.contains("souvenir") || cat.contains("tienda") ->
            Icons.Rounded.Storefront
        cat.contains("atractiv") || cat.contains("experienc") || cat.contains("tour") || cat.contains("aventura") ->
            Icons.Rounded.Landscape
        else -> Icons.Rounded.Place
    }
}

@Composable
internal fun PlaceholderImagenCategoria(
    categoria: String?,
    paleta: GuiaPaleta,
    modifier: Modifier = Modifier,
    iconoSize: Dp = 32.dp,
    desplazamientoInferior: Dp = 0.dp
) {
    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                colors = listOf(paleta.placeholderInicio, paleta.placeholderFin)
            )
        ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = desplazamientoInferior)
                .size(iconoSize + 24.dp)
                .background(paleta.placeholderIcono.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconoCategoriaComercio(categoria),
                contentDescription = null,
                tint = paleta.placeholderIcono,
                modifier = Modifier.size(iconoSize)
            )
        }
    }
}

@Composable
internal fun BadgePlanComercio(
    planInt: Int,
    paleta: GuiaPaleta,
    compacto: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (planInt != 3 && planInt != 2) return

    val esPremium = planInt == 3
    val fondo = if (esPremium) paleta.ambarFondo else paleta.socioFondo
    val textoColor = if (esPremium) paleta.ambarTexto else paleta.socioTexto
    val acentoColor = if (esPremium) paleta.ambarAcento else paleta.socioAcento
    val bordeColor = if (esPremium) {
        paleta.ambarAcento.copy(alpha = 0.38f)
    } else {
        paleta.bordeEnfocado.copy(alpha = if (paleta.esOscuro) 0.45f else 0.25f)
    }
    val icono = if (esPremium) Icons.Rounded.WorkspacePremium else Icons.Rounded.Verified
    val etiquetaRes = if (esPremium) Res.string.guia_badge_socio_premium else Res.string.guia_badge_socio_turismap

    Surface(
        modifier = modifier,
        color = fondo,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(0.8.dp, bordeColor),
        shadowElevation = if (compacto) 0.dp else 1.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                horizontal = if (compacto) 8.dp else 9.dp,
                vertical = if (compacto) 3.dp else 4.dp
            )
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = acentoColor,
                modifier = Modifier.size(if (compacto) 12.dp else 13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(etiquetaRes),
                color = textoColor,
                fontSize = if (compacto) 10.sp else 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

internal fun formatearSubtituloComercio(municipio: String, categoria: String): String =
    listOf(municipio.trim(), categoria.trim())
        .filter { it.isNotBlank() }
        .joinToString(" · ")

internal fun formatearPrecioComercio(precio: Double): String {
    val entero = precio.toInt()
    return if (precio == entero.toDouble()) {
        "$$entero"
    } else {
        val partes = precio.toString().split(".")
        "$${partes[0]}.${partes.getOrElse(1) { "00" }.padEnd(2, '0').take(2)}"
    }
}

internal fun formatearCalificacionComercio(calificacion: Double?): String? {
    if (calificacion == null || calificacion <= 0.0) return null
    val entero = calificacion.toInt()
    val dec = ((calificacion - entero) * 10).toInt()
    return "$entero.$dec"
}
