package com.turismap.app.screens.solicitud

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.theme.FondoBlancoAdaptable
import com.turismap.app.theme.GrisBorde
import com.turismap.app.theme.GrisTextoSecundario
import com.turismap.app.theme.TerracotaTextoAdaptable
import com.turismap.app.theme.TextoPrincipalOscuro
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.btn_reintentar
import turismap.shared.generated.resources.comercio_btn_cambiar_plan
import turismap.shared.generated.resources.comercio_btn_enviar_solicitud
import turismap.shared.generated.resources.comercio_btn_volver_planes
import turismap.shared.generated.resources.comercio_plan_desc_basico
import turismap.shared.generated.resources.comercio_plan_desc_crecimiento
import turismap.shared.generated.resources.comercio_plan_desc_vip
import turismap.shared.generated.resources.comercio_plan_seleccionado_fallback

private val VerdeAcentoOscuro = Color(0xFF6FCF97)
private val VerdeAcentoClaro = Color(0xFF1B4934)
private val AmbarAcentoOscuro = Color(0xFFE9C46A)
private val AmbarAcentoClaro = Color(0xFF9A6700)

private fun formatearPrecioCompacto(precioRaw: String): String {
    val partes = precioRaw.split("/").map(String::trim)
    val montoYMoneda = partes.firstOrNull().orEmpty()
    val periodicidad = partes.getOrNull(1)?.lowercase().orEmpty()
    val tokens = montoYMoneda.split(" ").filter(String::isNotBlank)
    val montoCompacto = if (tokens.size == 2 && tokens[1].equals("USD", ignoreCase = true)) {
        "$${tokens[0]}"
    } else {
        montoYMoneda
    }
    return if (periodicidad.isNotBlank()) "$montoCompacto / $periodicidad" else montoCompacto
}

@Composable
internal fun TarjetaResumenPlan(
    planNombre: String,
    planPrecio: String?,
    esPremium: Boolean,
    esSocio: Boolean,
    habilitarCambiar: Boolean,
    onCambiarPlan: () -> Unit
) {
    val esModoOscuro = isSystemInDarkTheme()
    val precioCompacto = remember(planPrecio) { planPrecio?.let(::formatearPrecioCompacto) }
    val colorAcento = when {
        esPremium -> if (esModoOscuro) AmbarAcentoOscuro else AmbarAcentoClaro
        esSocio -> if (esModoOscuro) VerdeAcentoOscuro else VerdeAcentoClaro
        else -> TextoPrincipalOscuro
    }
    val colorBorde = if (esPremium || esSocio) {
        colorAcento.copy(alpha = if (esModoOscuro) 0.45f else 0.35f)
    } else {
        GrisBorde
    }
    val colorPillFondo = if (esPremium || esSocio) {
        colorAcento.copy(alpha = if (esModoOscuro) 0.16f else 0.11f)
    } else {
        GrisBorde.copy(alpha = if (esModoOscuro) 0.55f else 0.60f)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = FondoBlancoAdaptable,
        border = BorderStroke(1.dp, colorBorde)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = planNombre.ifBlank { stringResource(Res.string.comercio_plan_seleccionado_fallback) },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextoPrincipalOscuro,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(
                        when {
                            esPremium -> Res.string.comercio_plan_desc_vip
                            esSocio -> Res.string.comercio_plan_desc_crecimiento
                            else -> Res.string.comercio_plan_desc_basico
                        }
                    ),
                    fontSize = 12.5.sp,
                    color = GrisTextoSecundario,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                precioCompacto?.let { precio ->
                    Surface(shape = RoundedCornerShape(20.dp), color = colorPillFondo) {
                        Text(
                            text = precio,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = colorAcento,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                Text(
                    text = stringResource(Res.string.comercio_btn_cambiar_plan),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (habilitarCambiar) TerracotaTextoAdaptable else GrisTextoSecundario,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = habilitarCambiar, role = Role.Button, onClick = onCambiarPlan)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
internal fun TarjetaSeccionSolicitud(
    titulo: String,
    icono: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    val colorAcento = if (isSystemInDarkTheme()) VerdeAcentoOscuro else VerdeAcentoClaro
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = FondoBlancoAdaptable,
        border = BorderStroke(1.dp, GrisBorde)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = icono, contentDescription = null, tint = colorAcento, modifier = Modifier.size(18.dp))
                Text(text = titulo, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextoPrincipalOscuro)
            }
            HorizontalDivider(color = GrisBorde.copy(alpha = 0.65f), thickness = 1.dp)
            content()
        }
    }
}

@Composable
internal fun CampoSelectorSolicitud(
    valorMostrar: String,
    etiqueta: String,
    descripcionBoton: String,
    iconoInicio: ImageVector,
    habilitado: Boolean,
    error: String?,
    onAbrir: () -> Unit
) {
    val textoColor = if (habilitado) TextoPrincipalOscuro else GrisTextoSecundario
    val bordeColor = if (error != null) MaterialTheme.colorScheme.error else GrisBorde
    val iconoColor = if (error != null) MaterialTheme.colorScheme.error else GrisTextoSecundario

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable(enabled = habilitado, role = Role.Button, onClickLabel = descripcionBoton, onClick = onAbrir)
        ) {
            OutlinedTextField(
                value = valorMostrar,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                singleLine = true,
                label = { Text(etiqueta) },
                leadingIcon = { Icon(iconoInicio, contentDescription = null, modifier = Modifier.size(20.dp)) },
                trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = descripcionBoton, modifier = Modifier.size(22.dp)) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = textoColor,
                    disabledBorderColor = bordeColor,
                    disabledLabelColor = if (error != null) MaterialTheme.colorScheme.error else GrisTextoSecundario,
                    disabledLeadingIconColor = iconoColor,
                    disabledTrailingIconColor = if (habilitado) TextoPrincipalOscuro else GrisTextoSecundario,
                    disabledContainerColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
        }
    }
}

@Composable
internal fun CampoSolicitud(
    etiqueta: String,
    valor: String,
    alCambiar: (String) -> Unit,
    iconoInicio: ImageVector? = null,
    multilinea: Boolean = false,
    soloLectura: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    esError: Boolean = false,
    textoAyuda: String? = null
) {
    val esModoOscuro = isSystemInDarkTheme()
    val colorFoco = if (esModoOscuro) VerdeAcentoOscuro else VerdeAcentoClaro
    val fondoSoloLectura = if (soloLectura) GrisBorde.copy(alpha = if (esModoOscuro) 0.25f else 0.35f) else Color.Transparent

    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        leadingIcon = iconoInicio?.let { icono ->
            { Icon(imageVector = icono, contentDescription = null, modifier = Modifier.size(20.dp)) }
        },
        trailingIcon = if (soloLectura) {
            { Icon(Icons.Default.Lock, contentDescription = null, tint = GrisTextoSecundario, modifier = Modifier.size(16.dp)) }
        } else null,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        readOnly = soloLectura,
        singleLine = !multilinea,
        minLines = if (multilinea) 3 else 1,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        isError = esError,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colorFoco,
            unfocusedBorderColor = GrisBorde,
            focusedLabelColor = colorFoco,
            unfocusedLabelColor = GrisTextoSecundario,
            focusedLeadingIconColor = colorFoco,
            unfocusedLeadingIconColor = GrisTextoSecundario,
            focusedContainerColor = fondoSoloLectura,
            unfocusedContainerColor = fondoSoloLectura,
            focusedTextColor = TextoPrincipalOscuro,
            unfocusedTextColor = if (soloLectura) GrisTextoSecundario else TextoPrincipalOscuro
        ),
        supportingText = textoAyuda?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
    )
}

@Composable
internal fun BarraInferiorSolicitud(
    enviando: Boolean,
    solicitudEnviada: Boolean,
    puedeEnviar: Boolean,
    onEnviar: () -> Unit,
    onVolverPlanes: () -> Unit
) {
    val colorBotonActivo = if (isSystemInDarkTheme()) Color(0xFF2E7D57) else VerdeAcentoClaro
    Surface(color = FondoBlancoAdaptable, shadowElevation = 8.dp) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(color = GrisBorde, thickness = 1.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = if (solicitudEnviada) onVolverPlanes else onEnviar,
                    enabled = if (solicitudEnviada) !enviando else puedeEnviar,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorBotonActivo,
                        contentColor = Color.White,
                        disabledContainerColor = GrisBorde,
                        disabledContentColor = GrisTextoSecundario
                    )
                ) {
                    if (enviando) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp, color = Color.White)
                    } else {
                        Text(
                            text = stringResource(
                                if (solicitudEnviada) Res.string.comercio_btn_volver_planes else Res.string.comercio_btn_enviar_solicitud
                            ),
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun BannerErrorCargaSolicitud(mensaje: String, onReintentar: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            Text(mensaje, color = MaterialTheme.colorScheme.error, fontSize = 13.sp, modifier = Modifier.weight(1f))
            TextButton(onClick = onReintentar) {
                Text(stringResource(Res.string.btn_reintentar), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
internal fun BannerEstadoSolicitud(mensaje: String, esExito: Boolean) {
    val esModoOscuro = isSystemInDarkTheme()
    val color = if (esExito) {
        if (esModoOscuro) VerdeAcentoOscuro else VerdeAcentoClaro
    } else {
        MaterialTheme.colorScheme.error
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = if (esModoOscuro) 0.15f else 0.10f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = if (esExito) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = mensaje,
                color = color,
                fontSize = 13.5.sp,
                fontWeight = if (esExito) FontWeight.SemiBold else FontWeight.Normal,
                lineHeight = 19.sp
            )
        }
    }
}
