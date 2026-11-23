package com.turismap.app.screens.notificaciones

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.data.models.AccionNotificacion
import com.turismap.app.data.models.Notificacion
import com.turismap.app.data.models.TipoNotificacion
import dev.gitlive.firebase.firestore.Timestamp
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.btn_reintentar
import turismap.shared.generated.resources.notificaciones_badge_aprobada
import turismap.shared.generated.resources.notificaciones_badge_aviso
import turismap.shared.generated.resources.notificaciones_badge_rechazada
import turismap.shared.generated.resources.notificaciones_btn_ver_comercio
import turismap.shared.generated.resources.notificaciones_btn_ver_planes
import turismap.shared.generated.resources.notificaciones_cd_no_leida
import turismap.shared.generated.resources.notificaciones_mensaje_aprobada_default
import turismap.shared.generated.resources.notificaciones_mensaje_default
import turismap.shared.generated.resources.notificaciones_mensaje_rechazada_default
import turismap.shared.generated.resources.notificaciones_negocio_fallback
import turismap.shared.generated.resources.notificaciones_tiempo_ahora
import turismap.shared.generated.resources.notificaciones_tiempo_dias
import turismap.shared.generated.resources.notificaciones_tiempo_horas
import turismap.shared.generated.resources.notificaciones_tiempo_minutos
import turismap.shared.generated.resources.notificaciones_titulo_aprobada_default
import turismap.shared.generated.resources.notificaciones_titulo_default
import turismap.shared.generated.resources.notificaciones_titulo_rechazada_default
import turismap.shared.generated.resources.notificaciones_vacio_descripcion
import turismap.shared.generated.resources.notificaciones_vacio_titulo
import kotlin.time.Clock

@Composable
internal fun TarjetaNotificacion(
    notificacion: Notificacion,
    paleta: NotificacionesPaleta,
    procesando: Boolean,
    onMarcarLeida: () -> Unit,
    onVerComercio: (String) -> Unit,
    onVerPlanes: () -> Unit
) {
    val noLeida = !notificacion.leida
    val tipo = notificacion.tipoResuelto

    val colorEstado = when (tipo) {
        TipoNotificacion.SOLICITUD_APROBADA -> paleta.verdeAcento
        TipoNotificacion.SOLICITUD_RECHAZADA -> paleta.terracotaTexto
        TipoNotificacion.OTRO -> paleta.textoSecundario
    }

    val iconoEstado = when (tipo) {
        TipoNotificacion.SOLICITUD_APROBADA -> Icons.Rounded.CheckCircle
        TipoNotificacion.SOLICITUD_RECHAZADA -> Icons.Rounded.Cancel
        TipoNotificacion.OTRO -> Icons.Rounded.Notifications
    }

    val etiquetaEstado = stringResource(
        when (tipo) {
            TipoNotificacion.SOLICITUD_APROBADA -> Res.string.notificaciones_badge_aprobada
            TipoNotificacion.SOLICITUD_RECHAZADA -> Res.string.notificaciones_badge_rechazada
            TipoNotificacion.OTRO -> Res.string.notificaciones_badge_aviso
        }
    )

    val nombreNegocioMostrar = notificacion.nombreNegocio
        .trim()
        .ifBlank { stringResource(Res.string.notificaciones_negocio_fallback) }

    val tituloMostrar = notificacion.titulo.trim().ifBlank {
        stringResource(
            when (tipo) {
                TipoNotificacion.SOLICITUD_APROBADA -> Res.string.notificaciones_titulo_aprobada_default
                TipoNotificacion.SOLICITUD_RECHAZADA -> Res.string.notificaciones_titulo_rechazada_default
                TipoNotificacion.OTRO -> Res.string.notificaciones_titulo_default
            }
        )
    }

    val mensajeMostrar = notificacion.mensaje.trim().ifBlank {
        when (tipo) {
            TipoNotificacion.SOLICITUD_APROBADA ->
                stringResource(Res.string.notificaciones_mensaje_aprobada_default, nombreNegocioMostrar)
            TipoNotificacion.SOLICITUD_RECHAZADA ->
                stringResource(Res.string.notificaciones_mensaje_rechazada_default, nombreNegocioMostrar)
            TipoNotificacion.OTRO ->
                stringResource(Res.string.notificaciones_mensaje_default, nombreNegocioMostrar)
        }
    }

    val textoFecha = formatearTiempoRelativo(notificacion.creadaEn)
    val cdNoLeida = stringResource(Res.string.notificaciones_cd_no_leida)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = !procesando) {
                if (noLeida) onMarcarLeida()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (noLeida) paleta.fondoTarjetaNoLeida else paleta.fondoTarjeta
        ),
        border = BorderStroke(
            width = if (noLeida) 1.2.dp else 1.dp,
            color = if (noLeida) paleta.bordeNoLeida else paleta.bordeSuave
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = colorEstado.copy(alpha = 0.14f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = iconoEstado,
                                contentDescription = null,
                                tint = colorEstado,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = etiquetaEstado,
                                color = colorEstado,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (noLeida) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(paleta.terracotaAcento)
                                .semantics { contentDescription = cdNoLeida }
                        )
                    }
                }

                if (textoFecha.isNotBlank()) {
                    Text(
                        text = textoFecha,
                        color = paleta.textoSecundario,
                        fontSize = 11.5.sp
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = tituloMostrar,
                    color = paleta.textoPrincipal,
                    fontSize = 15.5.sp,
                    fontWeight = if (noLeida) FontWeight.Bold else FontWeight.SemiBold,
                    lineHeight = 20.sp
                )

                if (notificacion.nombreNegocio.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Storefront,
                            contentDescription = null,
                            tint = paleta.textoSecundario,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = notificacion.nombreNegocio.trim(),
                            color = paleta.textoSecundario,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Text(
                    text = mensajeMostrar,
                    color = if (noLeida) paleta.textoPrincipal else paleta.textoSecundario,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp
                )
            }

            when (notificacion.accionPrincipal) {
                AccionNotificacion.VER_COMERCIO -> {
                    Button(
                        onClick = {
                            if (noLeida) onMarcarLeida()
                            onVerComercio(notificacion.comercioId.trim())
                        },
                        enabled = !procesando,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = paleta.verdeBoton,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.notificaciones_btn_ver_comercio),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                AccionNotificacion.VER_PLANES -> {
                    OutlinedButton(
                        onClick = {
                            if (noLeida) onMarcarLeida()
                            onVerPlanes()
                        },
                        enabled = !procesando,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, paleta.terracotaTexto.copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = paleta.terracotaTexto),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.notificaciones_btn_ver_planes),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                AccionNotificacion.NINGUNA -> Unit
            }
        }
    }
}

@Composable
internal fun EstadoVacioNotificaciones(
    paleta: NotificacionesPaleta,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(paleta.verdeAcento.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.NotificationsNone,
                contentDescription = null,
                tint = paleta.verdeAcento,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.notificaciones_vacio_titulo),
            color = paleta.textoPrincipal,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.notificaciones_vacio_descripcion),
            color = paleta.textoSecundario,
            fontSize = 13.5.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
internal fun EstadoErrorNotificaciones(
    mensajeError: String,
    paleta: NotificacionesPaleta,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = mensajeError,
            color = paleta.textoPrincipal,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onReintentar,
            colors = ButtonDefaults.buttonColors(
                containerColor = paleta.verdeBoton,
                contentColor = Color.White
            )
        ) {
            Text(stringResource(Res.string.btn_reintentar))
        }
    }
}

@Composable
private fun formatearTiempoRelativo(creadaEn: Timestamp?): String {
    if (creadaEn == null) return ""
    val diferenciaSegundos = (Clock.System.now().epochSeconds - creadaEn.seconds).coerceAtLeast(0L)
    return when {
        diferenciaSegundos < 60L -> stringResource(Res.string.notificaciones_tiempo_ahora)
        diferenciaSegundos < 3_600L ->
            stringResource(Res.string.notificaciones_tiempo_minutos, (diferenciaSegundos / 60L).toInt())
        diferenciaSegundos < 86_400L ->
            stringResource(Res.string.notificaciones_tiempo_horas, (diferenciaSegundos / 3_600L).toInt())
        else ->
            stringResource(Res.string.notificaciones_tiempo_dias, (diferenciaSegundos / 86_400L).toInt())
    }
}
