package com.turismap.app.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.data.models.formatearBadgeNotificaciones
import com.turismap.app.theme.FondoBlancoAdaptable
import com.turismap.app.theme.GrisBorde
import com.turismap.app.theme.TerracotaAcento
import com.turismap.app.theme.VerdeTextoAdaptable
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.notificaciones_cd_abrir
import turismap.shared.generated.resources.notificaciones_cd_abrir_con_pendientes

enum class VarianteBotonNotificaciones {
    ESTANDAR,
    SOBRE_IMAGEN
}

@Composable
fun BotonNotificaciones(
    cantidadNoLeidas: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variante: VarianteBotonNotificaciones = VarianteBotonNotificaciones.ESTANDAR
) {
    val textoBadge = formatearBadgeNotificaciones(cantidadNoLeidas)
    val descripcion = if (cantidadNoLeidas > 0) {
        stringResource(Res.string.notificaciones_cd_abrir_con_pendientes, cantidadNoLeidas)
    } else {
        stringResource(Res.string.notificaciones_cd_abrir)
    }

    val esSobreImagen = variante == VarianteBotonNotificaciones.SOBRE_IMAGEN
    val colorFondo = if (esSobreImagen) Color.Black.copy(alpha = 0.4f) else FondoBlancoAdaptable
    val borde = if (esSobreImagen) null else BorderStroke(1.dp, GrisBorde)
    val colorIcono = if (esSobreImagen) Color.White else VerdeTextoAdaptable
    val tamanoBoton = if (esSobreImagen) 36.dp else 42.dp
    val tamanoIcono = if (esSobreImagen) 20.dp else 22.dp

    BadgedBox(
        modifier = modifier,
        badge = {
            if (textoBadge != null) {
                Badge(
                    containerColor = TerracotaAcento,
                    contentColor = Color.White
                ) {
                    Text(
                        text = textoBadge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(tamanoBoton),
            shape = CircleShape,
            color = colorFondo,
            border = borde
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (cantidadNoLeidas > 0) {
                        Icons.Filled.Notifications
                    } else {
                        Icons.Outlined.Notifications
                    },
                    contentDescription = descripcion,
                    tint = colorIcono,
                    modifier = Modifier.size(tamanoIcono)
                )
            }
        }
    }
}
