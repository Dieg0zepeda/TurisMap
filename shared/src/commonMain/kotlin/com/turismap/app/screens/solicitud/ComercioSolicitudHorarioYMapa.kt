package com.turismap.app.screens.solicitud

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.turismap.app.data.models.Coordenada
import com.turismap.app.theme.FondoBlancoAdaptable
import com.turismap.app.theme.GrisBorde
import com.turismap.app.theme.GrisFondo
import com.turismap.app.theme.GrisTextoSecundario
import com.turismap.app.theme.TerracotaTextoAdaptable
import com.turismap.app.theme.TextoPrincipalOscuro
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.btn_cancelar
import turismap.shared.generated.resources.comercio_btn_cambiar_ubicacion_mapa
import turismap.shared.generated.resources.comercio_btn_ubicar_mapa
import turismap.shared.generated.resources.comercio_cd_configurar_horario
import turismap.shared.generated.resources.comercio_coordenadas_guardadas
import turismap.shared.generated.resources.comercio_horario_btn_aceptar
import turismap.shared.generated.resources.comercio_horario_placeholder
import turismap.shared.generated.resources.comercio_label_horario_atencion
import turismap.shared.generated.resources.comercio_ubicacion_guardada_badge

private val VerdeMapaOscuro = Color(0xFF6FCF97)
private val VerdeMapaClaro = Color(0xFF1B4934)

@Composable
internal fun BloqueUbicacionMapa(
    coordenadaSeleccionada: Coordenada?,
    onAbrirMapa: () -> Unit
) {
    val esModoOscuro = isSystemInDarkTheme()
    val colorVerde = if (esModoOscuro) VerdeMapaOscuro else VerdeMapaClaro

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (coordenadaSeleccionada != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = colorVerde.copy(alpha = if (esModoOscuro) 0.14f else 0.09f),
                border = BorderStroke(1.dp, colorVerde.copy(alpha = if (esModoOscuro) 0.35f else 0.25f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = colorVerde,
                        modifier = Modifier.size(18.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(Res.string.comercio_ubicacion_guardada_badge),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorVerde
                        )
                        Text(
                            text = stringResource(
                                Res.string.comercio_coordenadas_guardadas,
                                coordenadaSeleccionada.latitud.toString(),
                                coordenadaSeleccionada.longitud.toString()
                            ),
                            fontSize = 11.5.sp,
                            color = GrisTextoSecundario
                        )
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onAbrirMapa,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(
                1.dp,
                if (coordenadaSeleccionada == null) TerracotaTextoAdaptable else GrisBorde
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (coordenadaSeleccionada == null) TerracotaTextoAdaptable else TextoPrincipalOscuro
            )
        ) {
            Icon(imageVector = Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(
                    if (coordenadaSeleccionada == null) {
                        Res.string.comercio_btn_ubicar_mapa
                    } else {
                        Res.string.comercio_btn_cambiar_ubicacion_mapa
                    }
                ),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
internal fun CampoSelectorHorarioSolicitud(
    horario: String,
    habilitado: Boolean,
    onAbrir: () -> Unit
) {
    val esModoOscuro = isSystemInDarkTheme()
    val colorVerde = if (esModoOscuro) VerdeMapaOscuro else VerdeMapaClaro
    val configurado = horario.isNotBlank()
    val placeholder = stringResource(Res.string.comercio_horario_placeholder)
    val descripcionBoton = stringResource(Res.string.comercio_cd_configurar_horario)
    val valorVisible = if (configurado) horario.replace(" · ", "\n") else placeholder

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                enabled = habilitado,
                role = Role.Button,
                onClickLabel = descripcionBoton,
                onClick = onAbrir
            )
    ) {
        OutlinedTextField(
            value = valorVisible,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            singleLine = false,
            maxLines = 4,
            label = { Text(stringResource(Res.string.comercio_label_horario_atencion)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = if (configurado) Icons.Default.Edit else Icons.Default.KeyboardArrowDown,
                    contentDescription = descripcionBoton,
                    modifier = Modifier.size(if (configurado) 18.dp else 22.dp)
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = if (configurado && habilitado) TextoPrincipalOscuro else GrisTextoSecundario,
                disabledBorderColor = GrisBorde,
                disabledLabelColor = GrisTextoSecundario,
                disabledLeadingIconColor = if (configurado) colorVerde else GrisTextoSecundario,
                disabledTrailingIconColor = when {
                    !habilitado -> GrisTextoSecundario
                    configurado -> colorVerde
                    else -> TextoPrincipalOscuro
                },
                disabledContainerColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
internal fun PillHoraBloque(
    etiqueta: String,
    horaTexto: String,
    esError: Boolean,
    colorAcento: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = FondoBlancoAdaptable,
        border = BorderStroke(1.dp, if (esError) MaterialTheme.colorScheme.error else GrisBorde),
        modifier = modifier.heightIn(min = 58.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = etiqueta, fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = GrisTextoSecundario)
                Text(text = horaTexto, fontSize = 15.5.sp, fontWeight = FontWeight.Bold, color = TextoPrincipalOscuro)
            }
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = if (esError) MaterialTheme.colorScheme.error else colorAcento,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DialogoSelectorHora(
    titulo: String,
    horaInicial: Int,
    minutoInicial: Int,
    colorAcento: Color,
    esModoOscuro: Boolean,
    onDismiss: () -> Unit,
    onConfirmar: (Int, Int) -> Unit
) {
    val pickerState = rememberTimePickerState(
        initialHour = horaInicial,
        initialMinute = minutoInicial,
        is24Hour = false
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = FondoBlancoAdaptable, tonalElevation = 6.dp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = titulo,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrincipalOscuro,
                    modifier = Modifier.fillMaxWidth()
                )
                TimePicker(
                    state = pickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = GrisFondo,
                        selectorColor = colorAcento,
                        clockDialSelectedContentColor = if (esModoOscuro) Color(0xFF0B251B) else Color.White,
                        clockDialUnselectedContentColor = TextoPrincipalOscuro,
                        periodSelectorBorderColor = GrisBorde,
                        periodSelectorSelectedContainerColor = colorAcento.copy(alpha = if (esModoOscuro) 0.22f else 0.14f),
                        periodSelectorUnselectedContainerColor = Color.Transparent,
                        periodSelectorSelectedContentColor = colorAcento,
                        periodSelectorUnselectedContentColor = GrisTextoSecundario,
                        timeSelectorSelectedContainerColor = colorAcento.copy(alpha = if (esModoOscuro) 0.22f else 0.14f),
                        timeSelectorUnselectedContainerColor = GrisFondo,
                        timeSelectorSelectedContentColor = colorAcento,
                        timeSelectorUnselectedContentColor = TextoPrincipalOscuro
                    )
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(Res.string.btn_cancelar), color = GrisTextoSecundario)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { onConfirmar(pickerState.hour, pickerState.minute) }) {
                        Text(
                            text = stringResource(Res.string.comercio_horario_btn_aceptar),
                            fontWeight = FontWeight.Bold,
                            color = colorAcento
                        )
                    }
                }
            }
        }
    }
}
