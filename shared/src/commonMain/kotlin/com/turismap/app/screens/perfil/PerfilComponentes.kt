package com.turismap.app.screens.perfil

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.theme.TerracotaTextoAdaptable
import com.turismap.app.theme.VerdeTextoAdaptable
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.perfil_btn_cerrar_sesion
import turismap.shared.generated.resources.perfil_estadistica_favoritos
import turismap.shared.generated.resources.perfil_estadistica_visitas
import turismap.shared.generated.resources.perfil_negocios_badge
import turismap.shared.generated.resources.perfil_negocios_descripcion
import turismap.shared.generated.resources.perfil_negocios_titulo
import turismap.shared.generated.resources.perfil_negocios_ver_planes
import turismap.shared.generated.resources.perfil_opcion_contrasena_subtitulo
import turismap.shared.generated.resources.perfil_privacidad_cambiar_contrasena
import turismap.shared.generated.resources.perfil_privacidad_enviando
import turismap.shared.generated.resources.perfil_seccion_cuenta_seguridad

@Composable
internal fun SeccionEstadisticasPerfil(
    cantidadFavoritos: String,
    visitas: Int,
    paleta: PerfilPaleta,
    onFavoritosClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(16.dp),
        color = paleta.fondoTarjeta,
        border = BorderStroke(1.dp, paleta.colorDivisor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItemEstadistica(
                valor = cantidadFavoritos,
                etiqueta = stringResource(Res.string.perfil_estadistica_favoritos),
                colorEtiqueta = TerracotaTextoAdaptable,
                colorTexto = paleta.colorTextoPrincipal,
                modifier = Modifier.weight(1f),
                mostrarIndicadorAccion = true,
                formaClick = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                onClick = onFavoritosClick
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(paleta.colorDivisor)
            )
            ItemEstadistica(
                valor = visitas.toString(),
                etiqueta = stringResource(Res.string.perfil_estadistica_visitas),
                colorEtiqueta = paleta.colorTextoSecundario,
                colorTexto = paleta.colorTextoPrincipal,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
internal fun TarjetaTurisMapNegocios(
    paleta: PerfilPaleta,
    onVerPlanesClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = paleta.verdeEsmeralda)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color.White.copy(alpha = 0.16f)
            ) {
                Text(
                    text = stringResource(Res.string.perfil_negocios_badge),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.perfil_negocios_titulo),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(Res.string.perfil_negocios_descripcion),
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f),
                lineHeight = 17.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onVerPlanesClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 42.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = paleta.terracota),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = stringResource(Res.string.perfil_negocios_ver_planes),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
internal fun SeccionConfiguracionPerfil(
    enviandoRecuperacion: Boolean,
    cerrandoSesion: Boolean,
    paleta: PerfilPaleta,
    onCambiarContrasenaClick: () -> Unit,
    onCerrarSesionClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = stringResource(Res.string.perfil_seccion_cuenta_seguridad),
            fontSize = 12.sp,
            color = paleta.colorTextoSecundario,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        CardOpciones(
            fondoTarjeta = paleta.fondoTarjeta,
            colorBorde = paleta.colorDivisor
        ) {
            ItemMenuOpcion(
                icono = Icons.Default.LockReset,
                titulo = stringResource(
                    if (enviandoRecuperacion) {
                        Res.string.perfil_privacidad_enviando
                    } else {
                        Res.string.perfil_privacidad_cambiar_contrasena
                    }
                ),
                subtitulo = stringResource(Res.string.perfil_opcion_contrasena_subtitulo),
                colorTexto = paleta.colorTextoPrincipal,
                colorIcono = paleta.colorTextoPrincipal,
                habilitado = !cerrandoSesion,
                cargando = enviandoRecuperacion,
                onClick = onCambiarContrasenaClick
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onCerrarSesionClick,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TerracotaTextoAdaptable),
            border = BorderStroke(1.5.dp, TerracotaTextoAdaptable),
            shape = RoundedCornerShape(16.dp),
            enabled = !cerrandoSesion
        ) {
            if (cerrandoSesion) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = TerracotaTextoAdaptable)
            } else {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(Res.string.perfil_btn_cerrar_sesion), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ItemEstadistica(
    valor: String,
    etiqueta: String,
    colorEtiqueta: Color,
    colorTexto: Color,
    modifier: Modifier = Modifier,
    mostrarIndicadorAccion: Boolean = false,
    formaClick: RoundedCornerShape = RoundedCornerShape(12.dp),
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier
                        .clip(formaClick)
                        .clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Text(
            text = valor,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = colorTexto
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = etiqueta,
                fontSize = 12.sp,
                color = colorEtiqueta,
                fontWeight = if (onClick != null) FontWeight.SemiBold else FontWeight.Medium
            )
            if (mostrarIndicadorAccion) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colorEtiqueta,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun CardOpciones(
    fondoTarjeta: Color,
    colorBorde: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = fondoTarjeta,
        border = BorderStroke(1.dp, colorBorde)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

@Composable
private fun ItemMenuOpcion(
    icono: ImageVector,
    titulo: String,
    subtitulo: String = "",
    colorTexto: Color,
    colorIcono: Color,
    habilitado: Boolean = true,
    cargando: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = habilitado && !cargando, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = colorIcono,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                fontSize = 15.sp,
                color = colorTexto,
                fontWeight = FontWeight.Medium
            )
            if (subtitulo.isNotEmpty()) {
                Text(
                    text = subtitulo,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = VerdeTextoAdaptable
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
