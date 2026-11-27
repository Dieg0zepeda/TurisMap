package com.turismap.app.screens.detalleexperiencia

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.detalle_exp_accion_llamar
import turismap.shared.generated.resources.detalle_exp_accion_web_redes
import turismap.shared.generated.resources.detalle_exp_accion_whatsapp
import turismap.shared.generated.resources.detalle_exp_una_resena
import turismap.shared.generated.resources.detalle_exp_varias_resenas
import turismap.shared.generated.resources.guia_badge_socio_premium
import turismap.shared.generated.resources.guia_badge_socio_turismap
import turismap.shared.generated.resources.guia_calificacion_nuevo

@Composable
internal fun EncabezadoComercio(
    datosUi: DetalleExperienciaDatosUi,
    paleta: DetalleExperienciaPaleta
) {
    val datos = datosUi.comercio
    val promedioTexto = datosUi.promedioNumericoTexto ?: stringResource(Res.string.guia_calificacion_nuevo)
    val resenasDetalle = when {
        datosUi.promedioNumericoTexto == null || datosUi.totalResenas <= 0 -> null
        datosUi.totalResenas == 1 -> stringResource(Res.string.detalle_exp_una_resena)
        else -> stringResource(Res.string.detalle_exp_varias_resenas, datosUi.totalResenas)
    }

    // Fila superior compacta: badge sobrio del plan (si aplica) + etiqueta de categoría
    if (datosUi.esPremium || datosUi.esSocio || datosUi.categoriaEtiqueta.isNotBlank()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (datosUi.esPremium) {
                Surface(
                    color = paleta.ambarFondo,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.8.dp, paleta.ambarBorde)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WorkspacePremium,
                            contentDescription = null,
                            tint = paleta.ambarAcento,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = stringResource(Res.string.guia_badge_socio_premium),
                            color = paleta.ambarTexto,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }
            } else if (datosUi.esSocio) {
                Surface(
                    color = paleta.contenedorPrimario,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.8.dp, paleta.primario.copy(alpha = 0.45f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Verified,
                            contentDescription = null,
                            tint = paleta.acentoVerdeTexto,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = stringResource(Res.string.guia_badge_socio_turismap),
                            color = paleta.sobreContenedorPrimario,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }
            }

            if (datosUi.categoriaEtiqueta.isNotBlank()) {
                Surface(
                    modifier = Modifier.weight(1f, fill = false),
                    color = paleta.superficieContenedor,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, paleta.borde)
                ) {
                    Text(
                        text = datosUi.categoriaEtiqueta,
                        color = paleta.textoSecundario,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
    }

    // Título limpio sin emojis
    Text(
        text = datos.nombre,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        fontWeight = FontWeight.Bold,
        color = paleta.textoPrincipal
    )

    Spacer(Modifier.height(6.dp))

    // Metadatos en una sola línea compacta: estrella + calificación/reseñas · pin + municipio
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Star,
            contentDescription = null,
            tint = paleta.ambarAcento,
            modifier = Modifier.size(15.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = promedioTexto,
            color = paleta.textoPrincipal,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
        if (resenasDetalle != null) {
            Text(
                text = " ($resenasDetalle)",
                color = paleta.textoSecundario,
                fontSize = 12.5.sp,
                maxLines = 1
            )
        }

        if (datosUi.municipioEtiqueta.isNotBlank()) {
            Text(
                text = " · ",
                color = paleta.textoSecundario,
                fontSize = 13.sp
            )
            Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = null,
                tint = paleta.textoSecundario,
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(3.dp))
            Text(
                text = datosUi.municipioEtiqueta,
                color = paleta.textoSecundario,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
        }
    }
}

@Composable
internal fun FilaAccionesRapidasComercio(
    datosUi: DetalleExperienciaDatosUi,
    paleta: DetalleExperienciaPaleta,
    onAbrirWhatsapp: () -> Unit,
    onLlamar: () -> Unit,
    onAbrirSitioWeb: () -> Unit
) {
    if (!datosUi.tieneAccionesRapidas) return

    Spacer(Modifier.height(16.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (datosUi.mostrarWhatsapp) {
            BotonAccionRapida(
                icon = Icons.Rounded.Chat,
                etiqueta = stringResource(Res.string.detalle_exp_accion_whatsapp),
                paleta = paleta,
                onClick = onAbrirWhatsapp,
                modifier = Modifier.weight(1f)
            )
        }

        if (datosUi.mostrarLlamar) {
            BotonAccionRapida(
                icon = Icons.Rounded.Call,
                etiqueta = stringResource(Res.string.detalle_exp_accion_llamar),
                paleta = paleta,
                onClick = onLlamar,
                modifier = Modifier.weight(1f)
            )
        }

        if (datosUi.mostrarSitioWeb) {
            BotonAccionRapida(
                icon = Icons.Rounded.Language,
                etiqueta = stringResource(Res.string.detalle_exp_accion_web_redes),
                paleta = paleta,
                onClick = onAbrirSitioWeb,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BotonAccionRapida(
    icon: ImageVector,
    etiqueta: String,
    paleta: DetalleExperienciaPaleta,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        color = paleta.superficieContenedor,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, paleta.borde)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = paleta.acentoVerdeTexto,
                modifier = Modifier.size(17.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = etiqueta,
                color = paleta.textoPrincipal,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
