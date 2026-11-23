package com.turismap.app.screens.perfil

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.turismap.app.components.BotonNotificaciones
import com.turismap.app.components.RemoteImage
import com.turismap.app.components.VarianteBotonNotificaciones
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.perfil_cd_cambiar_portada
import turismap.shared.generated.resources.perfil_cd_foto_perfil
import turismap.shared.generated.resources.perfil_cd_foto_portada
import turismap.shared.generated.resources.perfil_editar_titulo

internal val ALTURA_CABECERA_PERFIL = 260.dp
internal val SOLAPAMIENTO_CORTINA_PERFIL = 20.dp
internal val ALTURA_ESPACIADOR_PERFIL = ALTURA_CABECERA_PERFIL - SOLAPAMIENTO_CORTINA_PERFIL
internal val ALTURA_BARRA_COMPACTA_PERFIL = 56.dp
internal const val UMBRAL_BARRA_COMPACTA_PERFIL = 0.70f

private const val FACTOR_PARALAJE_CABECERA = 0.45f
private const val ESCALA_MINIMA_CABECERA = 0.88f
private const val VELOCIDAD_FADE_CABECERA = 1.25f

private fun calcularAlphaCabecera(collapseProgress: Float): Float =
    1f - (collapseProgress * VELOCIDAD_FADE_CABECERA).coerceIn(0f, 1f)

@Composable
internal fun FondoCabeceraPerfilParalaje(
    fotoPortada: String?,
    paleta: PerfilPaleta,
    scrollOffsetProvider: () -> Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationY = -scrollOffsetProvider() * FACTOR_PARALAJE_CABECERA
            }
            .background(paleta.verdeEsmeralda)
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(ALTURA_CABECERA_PERFIL)
        )

        fotoPortada?.takeIf { it.isNotBlank() }?.let { url ->
            RemoteImage(
                url = url,
                contentDescription = stringResource(Res.string.perfil_cd_foto_portada),
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.28f),
                            Color.Black.copy(alpha = 0.40f),
                            Color.Black.copy(alpha = 0.68f)
                        )
                    )
                )
        )
    }
}

@Composable
internal fun ContenidoCabeceraPerfilParalaje(
    fotoPerfil: String?,
    nombreMostrar: String,
    correoMostrar: String,
    errorPerfil: String?,
    cantidadNotificacionesNoLeidas: Int,
    paleta: PerfilPaleta,
    scrollOffsetProvider: () -> Float,
    collapseProgressProvider: () -> Float,
    onAbrirNotificacionesClick: () -> Unit,
    onCambiarPortadaClick: () -> Unit,
    onCambiarFotoPerfilClick: () -> Unit,
    onEditarPerfilClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                // El scroll desplaza -offset; compensar +offset * 0.55f mantiene el bloque
                // sincronizado exactamente a -offset * 0.45f junto al fondo en paralaje.
                translationY = scrollOffsetProvider() * (1f - FACTOR_PARALAJE_CABECERA)
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onCambiarPortadaClick
            )
            .statusBarsPadding()
            .height(ALTURA_ESPACIADOR_PERFIL)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .graphicsLayer {
                    alpha = calcularAlphaCabecera(collapseProgressProvider())
                }
        ) {
            BotonNotificaciones(
                cantidadNoLeidas = cantidadNotificacionesNoLeidas,
                onClick = onAbrirNotificacionesClick,
                variante = VarianteBotonNotificaciones.SOBRE_IMAGEN,
                modifier = Modifier.align(Alignment.TopStart)
            )

            IconButton(
                onClick = onCambiarPortadaClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = stringResource(Res.string.perfil_cd_cambiar_portada),
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 10.dp)
                .graphicsLayer {
                    val progress = collapseProgressProvider()
                    val escala = lerp(1f, ESCALA_MINIMA_CABECERA, progress)
                    scaleX = escala
                    scaleY = escala
                    alpha = calcularAlphaCabecera(progress)
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(96.dp)) {
                AvatarUsuarioPerfil(
                    fotoPerfil = fotoPerfil,
                    tamano = 96.dp,
                    tamanoIconoPlaceholder = 52.dp,
                    grosorBorde = 3.dp,
                    colorBorde = Color.White.copy(alpha = 0.92f),
                    onClick = onCambiarFotoPerfilClick
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-2).dp, y = (-2).dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(paleta.terracota, CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                        .clickable(onClick = onEditarPerfilClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(Res.string.perfil_editar_titulo),
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = nombreMostrar,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Text(
                text = correoMostrar,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.88f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )

            errorPerfil?.let { mensajeError ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.92f),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Text(
                        text = mensajeError,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun BarraSuperiorCompactaPerfil(
    visible: Boolean,
    fotoPerfil: String?,
    nombreMostrar: String,
    cantidadNotificacionesNoLeidas: Int,
    paleta: PerfilPaleta,
    onAbrirNotificacionesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val barraAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "barraCompactaPerfilAlpha"
    )

    if (visible || barraAlpha > 0.01f) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .graphicsLayer { alpha = barraAlpha }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                ),
            color = paleta.fondoTarjeta.copy(alpha = 0.96f),
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ALTURA_BARRA_COMPACTA_PERFIL)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarUsuarioPerfil(
                        fotoPerfil = fotoPerfil,
                        tamano = 34.dp,
                        tamanoIconoPlaceholder = 20.dp,
                        grosorBorde = 1.5.dp,
                        colorBorde = paleta.colorDivisor
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = nombreMostrar,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = paleta.colorTextoPrincipal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    BotonNotificaciones(
                        cantidadNoLeidas = cantidadNotificacionesNoLeidas,
                        onClick = onAbrirNotificacionesClick,
                        variante = VarianteBotonNotificaciones.ESTANDAR
                    )
                }

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = paleta.colorDivisor.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@Composable
private fun AvatarUsuarioPerfil(
    fotoPerfil: String?,
    tamano: Dp,
    tamanoIconoPlaceholder: Dp,
    grosorBorde: Dp,
    colorBorde: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val cdFotoPerfil = stringResource(Res.string.perfil_cd_foto_perfil)
    Box(
        modifier = modifier
            .size(tamano)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(grosorBorde, colorBorde, CircleShape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!fotoPerfil.isNullOrBlank()) {
            RemoteImage(
                url = fotoPerfil,
                contentDescription = cdFotoPerfil,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = cdFotoPerfil,
                modifier = Modifier.size(tamanoIconoPlaceholder),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
