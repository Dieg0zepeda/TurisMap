package com.turismap.app.screens.solicitud

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.data.models.Coordenada
import com.turismap.app.theme.GrisBorde
import com.turismap.app.theme.GrisTextoSecundario
import com.turismap.app.theme.TerracotaTextoAdaptable
import com.turismap.app.theme.TextoPrincipalOscuro
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.comercio_btn_aplicar_horario
import turismap.shared.generated.resources.comercio_btn_cambiar_ubicacion_mapa
import turismap.shared.generated.resources.comercio_btn_horario_manual
import turismap.shared.generated.resources.comercio_btn_horario_por_dias
import turismap.shared.generated.resources.comercio_btn_ubicar_mapa
import turismap.shared.generated.resources.comercio_coordenadas_guardadas
import turismap.shared.generated.resources.comercio_horario_abre
import turismap.shared.generated.resources.comercio_horario_cierra
import turismap.shared.generated.resources.comercio_horario_conector_a
import turismap.shared.generated.resources.comercio_label_horario_atencion
import turismap.shared.generated.resources.comercio_label_horario_manual
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
internal fun SeccionHorarioSolicitud(
    horario: String,
    mostrarHorarioDetallado: Boolean,
    diasSeleccionados: Set<DiaSemanaSolicitud>,
    horariosPorDia: Map<DiaSemanaSolicitud, HorarioDia>,
    onToggleModo: () -> Unit,
    onHorarioManualChange: (String) -> Unit,
    onToggleDia: (DiaSemanaSolicitud, Boolean) -> Unit,
    onActualizarHorarioDia: (DiaSemanaSolicitud, HorarioDia) -> Unit,
    onAplicarHorario: () -> Unit
) {
    val esModoOscuro = isSystemInDarkTheme()
    val colorVerde = if (esModoOscuro) VerdeMapaOscuro else VerdeMapaClaro

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.comercio_label_horario_atencion),
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextoPrincipalOscuro
            )
            TextButton(onClick = onToggleModo) {
                Text(
                    text = stringResource(
                        if (mostrarHorarioDetallado) {
                            Res.string.comercio_btn_horario_manual
                        } else {
                            Res.string.comercio_btn_horario_por_dias
                        }
                    ),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorVerde
                )
            }
        }

        if (mostrarHorarioDetallado) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DiaSemanaSolicitud.entries.forEach { dia ->
                    val seleccionado = diasSeleccionados.contains(dia)
                    val horarioDia = horariosPorDia[dia] ?: HorarioDia()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = seleccionado,
                                onCheckedChange = { checked -> onToggleDia(dia, checked) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = if (esModoOscuro) Color(0xFF2E7D57) else VerdeMapaClaro,
                                    checkmarkColor = Color.White
                                )
                            )
                            Text(stringResource(dia.etiquetaRes), fontSize = 13.5.sp, color = TextoPrincipalOscuro)
                        }
                        if (seleccionado) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = horarioDia.apertura,
                                    onValueChange = { nuevo ->
                                        onActualizarHorarioDia(dia, horarioDia.copy(apertura = nuevo.take(12)))
                                    },
                                    label = { Text(stringResource(Res.string.comercio_horario_abre), fontSize = 11.sp) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.width(88.dp),
                                    singleLine = true
                                )
                                Text(
                                    text = stringResource(Res.string.comercio_horario_conector_a),
                                    fontSize = 12.sp,
                                    color = GrisTextoSecundario
                                )
                                OutlinedTextField(
                                    value = horarioDia.cierre,
                                    onValueChange = { nuevo ->
                                        onActualizarHorarioDia(dia, horarioDia.copy(cierre = nuevo.take(12)))
                                    },
                                    label = { Text(stringResource(Res.string.comercio_horario_cierra), fontSize = 11.sp) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.width(88.dp),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onAplicarHorario,
                enabled = diasSeleccionados.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (esModoOscuro) Color(0xFF2E7D57) else VerdeMapaClaro,
                    contentColor = Color.White
                )
            ) { Text(stringResource(Res.string.comercio_btn_aplicar_horario), fontWeight = FontWeight.SemiBold) }
        } else {
            CampoSolicitud(
                etiqueta = stringResource(Res.string.comercio_label_horario_manual),
                valor = horario,
                alCambiar = onHorarioManualChange,
                iconoInicio = Icons.Default.Schedule
            )
        }
    }
}
