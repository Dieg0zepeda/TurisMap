package com.turismap.app.screens.detalleexperiencia

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.components.RemoteImage
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.detalle_exp_badge_contador_fotos
import turismap.shared.generated.resources.detalle_exp_badge_foto_principal
import turismap.shared.generated.resources.detalle_exp_badge_galeria_fotos
import turismap.shared.generated.resources.detalle_exp_badge_premium_fotos
import turismap.shared.generated.resources.detalle_exp_cd_volver
import turismap.shared.generated.resources.detalle_exp_sin_resenas
import turismap.shared.generated.resources.detalle_exp_una_resena
import turismap.shared.generated.resources.detalle_exp_varias_resenas
import turismap.shared.generated.resources.guia_badge_socio_premium
import turismap.shared.generated.resources.guia_badge_socio_turismap
import turismap.shared.generated.resources.guia_calificacion_nuevo

@Composable
internal fun CabeceraMultimediaExperiencia(
    datosUi: DetalleExperienciaDatosUi,
    esFavorito: Boolean,
    favoritoEnProceso: Boolean,
    paleta: DetalleExperienciaPaleta,
    onVolver: () -> Unit,
    onToggleFavorito: () -> Unit
) {
    val datos = datosUi.comercio
    val fotos = datosUi.fotos

    Box(Modifier.fillMaxWidth().height(260.dp)) {
        if (fotos.isNotEmpty()) {
            val pagerState = rememberPagerState(pageCount = { fotos.size })

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { pagina ->
                RemoteImage(
                    url = fotos[pagina],
                    contentDescription = datos.nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.5f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
            )

            // PUNTOS INDICADORES ESTILO INSTAGRAM (Aparecen si tiene 2, 4 u 8 fotos)
            if (fotos.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
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
                                .background(
                                    if (esActivo) Color.White else Color.White.copy(alpha = 0.45f)
                                )
                        )
                    }
                }
            }

            // Insignia inferior descriptiva de fotos
            Row(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when {
                    datosUi.esPremium -> {
                        Surface(
                            color = Color.Black.copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, paleta.doradoCorona),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("👑", fontSize = 13.sp)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    stringResource(
                                        Res.string.detalle_exp_badge_premium_fotos,
                                        pagerState.currentPage + 1,
                                        fotos.size
                                    ),
                                    color = paleta.doradoCorona,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                    datosUi.esSocio -> {
                        Surface(
                            color = Color.Black.copy(alpha = 0.75f),
                            border = BorderStroke(1.dp, paleta.verdeTurisMap.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "📷 ${stringResource(Res.string.detalle_exp_badge_galeria_fotos, pagerState.currentPage + 1, fotos.size)}",
                                color = paleta.verdeTurisMap,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                    else -> {
                        Surface(
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                if (fotos.size > 1) {
                                    "📷 ${stringResource(Res.string.detalle_exp_badge_contador_fotos, pagerState.currentPage + 1, fotos.size)}"
                                } else {
                                    "📷 ${stringResource(Res.string.detalle_exp_badge_foto_principal)}"
                                },
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(if (paleta.esOscuro) Color(0xFF1E293B) else Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                Text(datosUi.categoriaEtiqueta, color = paleta.verdeTurisMap, fontWeight = FontWeight.Bold)
            }
        }

        // Botones superiores
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onVolver,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = stringResource(Res.string.detalle_exp_cd_volver),
                        tint = Color.White
                    )
                }

                if (datosUi.esPremium) {
                    Surface(
                        color = paleta.verdeTurisMap,
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text("👑", fontSize = 12.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                stringResource(Res.string.guia_badge_socio_premium),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                } else if (datosUi.esSocio) {
                    Surface(color = paleta.verdeTurisMap, shape = RoundedCornerShape(20.dp)) {
                        Text(
                            "✓ ${stringResource(Res.string.guia_badge_socio_turismap)}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            if (!datosUi.esBasico) {
                IconButton(
                    onClick = onToggleFavorito,
                    enabled = !favoritoEnProceso,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                ) {
                    Text(if (esFavorito) "❤️" else "🤍", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
internal fun EncabezadoComercio(
    datosUi: DetalleExperienciaDatosUi,
    paleta: DetalleExperienciaPaleta
) {
    val datos = datosUi.comercio
    val promedioTexto = datosUi.promedioNumericoTexto ?: stringResource(Res.string.guia_calificacion_nuevo)
    val resenasTexto = when (datosUi.totalResenas) {
        0 -> stringResource(Res.string.detalle_exp_sin_resenas)
        1 -> stringResource(Res.string.detalle_exp_una_resena)
        else -> stringResource(Res.string.detalle_exp_varias_resenas, datosUi.totalResenas)
    }

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("★", color = paleta.verdeTurisMap, fontSize = 15.sp)
            Spacer(Modifier.width(4.dp))
            Text(
                "$promedioTexto · $resenasTexto · ${datosUi.categoriaEtiqueta}",
                color = paleta.colorTextoSecundario,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    Spacer(Modifier.height(6.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            datos.nombre,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = paleta.colorTextoPrincipal,
            modifier = Modifier.weight(1f, fill = false)
        )
        Spacer(Modifier.width(8.dp))
        if (datosUi.esPremium) {
            Text("👑", fontSize = 22.sp)
        } else if (datosUi.esSocio) {
            Text("✓", color = paleta.verdeTurisMap, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}
