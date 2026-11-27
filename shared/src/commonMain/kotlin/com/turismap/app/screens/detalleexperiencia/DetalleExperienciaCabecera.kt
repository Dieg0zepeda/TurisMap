package com.turismap.app.screens.detalleexperiencia

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.components.RemoteImage
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.detalle_exp_cd_volver
import turismap.shared.generated.resources.detalle_lugar_cd_guardar_favorito
import turismap.shared.generated.resources.detalle_lugar_cd_quitar_favorito

@Composable
internal fun CabeceraMultimediaExperiencia(
    datosUi: DetalleExperienciaDatosUi,
    pagerState: PagerState,
    esFavorito: Boolean,
    favoritoEnProceso: Boolean,
    paleta: DetalleExperienciaPaleta,
    onVolver: () -> Unit,
    onToggleFavorito: () -> Unit
) {
    val datos = datosUi.comercio
    val fotos = datosUi.fotos

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(264.dp)
            .background(paleta.superficieContenedor)
    ) {
        if (fotos.isNotEmpty()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { pagina ->
                val fotoUrl = fotos.getOrNull(pagina)
                if (fotoUrl != null) {
                    RemoteImage(
                        url = fotoUrl,
                        contentDescription = datos.nombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Scrim superior funcional para legibilidad de los botones flotantes
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent)
                        )
                    )
            )

            // Scrim inferior funcional y contador discreto (solo si hay más de 1 foto)
            if (fotos.size > 1) {
                val paginaVisible = (pagerState.currentPage + 1).coerceIn(1, fotos.size)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.42f))
                            )
                        )
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(14.dp),
                    color = Color.Black.copy(alpha = 0.62f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.14f))
                ) {
                    Text(
                        text = "$paginaVisible / ${fotos.size}",
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(paleta.superficieTarjeta)
                        .border(1.dp, paleta.borde, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Storefront,
                        contentDescription = null,
                        tint = paleta.textoSecundario,
                        modifier = Modifier.size(26.dp)
                    )
                }
                if (datosUi.categoriaEtiqueta.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = datosUi.categoriaEtiqueta,
                        color = paleta.textoSecundario,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Controles flotantes superiores: únicamente Atrás (izq.) y Favorito (der.)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BotonCircularHero(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(Res.string.detalle_exp_cd_volver),
                tint = Color.White,
                onClick = onVolver
            )

            BotonCircularHero(
                icon = if (esFavorito) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = stringResource(
                    if (esFavorito) {
                        Res.string.detalle_lugar_cd_quitar_favorito
                    } else {
                        Res.string.detalle_lugar_cd_guardar_favorito
                    }
                ),
                tint = if (esFavorito) paleta.terracota else Color.White,
                enabled = !favoritoEnProceso,
                onClick = onToggleFavorito
            )
        }
    }
}

@Composable
private fun BotonCircularHero(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.56f))
            .border(0.8.dp, Color.White.copy(alpha = 0.14f), CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}
