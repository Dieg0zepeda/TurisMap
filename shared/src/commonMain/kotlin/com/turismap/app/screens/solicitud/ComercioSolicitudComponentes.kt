package com.turismap.app.screens.solicitud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.btn_reintentar
import turismap.shared.generated.resources.comercio_btn_aplicar_horario
import turismap.shared.generated.resources.comercio_btn_horario_manual
import turismap.shared.generated.resources.comercio_btn_horario_por_dias
import turismap.shared.generated.resources.comercio_horario_abre
import turismap.shared.generated.resources.comercio_horario_cierra
import turismap.shared.generated.resources.comercio_horario_conector_a
import turismap.shared.generated.resources.comercio_label_horario_atencion
import turismap.shared.generated.resources.comercio_label_horario_manual
import turismap.shared.generated.resources.comercio_plan_desc_basico
import turismap.shared.generated.resources.comercio_plan_desc_crecimiento
import turismap.shared.generated.resources.comercio_plan_desc_vip
import turismap.shared.generated.resources.comercio_plan_seleccionado_fallback

@Composable
internal fun TarjetaResumenPlan(
    planNombre: String,
    planPrecio: String?,
    esPremium: Boolean,
    esSocio: Boolean,
    cargandoPlan: Boolean,
    planError: String?,
    onReintentar: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                esPremium -> Color(0xFFFEF3C7)
                esSocio -> Color(0xFFEEF2FF)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = planNombre.ifBlank { stringResource(Res.string.comercio_plan_seleccionado_fallback) },
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = when {
                        esPremium -> Color(0xFF92400E)
                        esSocio -> Color(0xFF3730A3)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                planPrecio?.let {
                    Text(
                        text = it,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = when {
                            esPremium -> Color(0xFFB45309)
                            esSocio -> Color(0xFF4F46E5)
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(
                    when {
                        esPremium -> Res.string.comercio_plan_desc_vip
                        esSocio -> Res.string.comercio_plan_desc_crecimiento
                        else -> Res.string.comercio_plan_desc_basico
                    }
                ),
                fontSize = 12.5.sp,
                color = Color(0xFF475569)
            )
        }
    }

    if (cargandoPlan) {
        CircularProgressIndicator(modifier = Modifier.padding(vertical = 4.dp))
    } else {
        planError?.let { errorPlan ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(errorPlan, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onReintentar) {
                    Text(stringResource(Res.string.btn_reintentar))
                }
            }
        }
    }
}

@Composable
internal fun <T> SelectorDropdownSolicitud(
    valorMostrar: String,
    etiqueta: String,
    descripcionBoton: String,
    expandido: Boolean,
    cargando: Boolean,
    error: String?,
    opciones: List<T>,
    etiquetaOpcion: (T) -> String,
    onAbrir: () -> Unit,
    onCerrar: () -> Unit,
    onSeleccionar: (T) -> Unit
) {
    Column {
        Box {
            OutlinedTextField(
                value = valorMostrar,
                onValueChange = {},
                readOnly = true,
                label = { Text(etiqueta) },
                trailingIcon = {
                    IconButton(
                        onClick = onAbrir,
                        enabled = !cargando && error == null && opciones.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = descripcionBoton
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            DropdownMenu(
                expanded = expandido,
                onDismissRequest = onCerrar,
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(etiquetaOpcion(opcion)) },
                        onClick = { onSeleccionar(opcion) }
                    )
                }
            }
        }
        if (cargando) {
            CircularProgressIndicator(modifier = Modifier.padding(8.dp))
        } else {
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 4.dp))
            }
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
    Column {
        Text(
            stringResource(Res.string.comercio_label_horario_atencion),
            style = MaterialTheme.typography.labelLarge
        )
        TextButton(onClick = onToggleModo) {
            Text(
                stringResource(
                    if (mostrarHorarioDetallado) {
                        Res.string.comercio_btn_horario_manual
                    } else {
                        Res.string.comercio_btn_horario_por_dias
                    }
                )
            )
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
                                onCheckedChange = { checked -> onToggleDia(dia, checked) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(dia.etiquetaRes))
                        }
                        if (seleccionado) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = horarioDia.apertura,
                                    onValueChange = { nuevoValor ->
                                        onActualizarHorarioDia(dia, horarioDia.copy(apertura = nuevoValor.take(12)))
                                    },
                                    label = { Text(stringResource(Res.string.comercio_horario_abre)) },
                                    modifier = Modifier.width(90.dp),
                                    singleLine = true
                                )
                                Text(stringResource(Res.string.comercio_horario_conector_a))
                                OutlinedTextField(
                                    value = horarioDia.cierre,
                                    onValueChange = { nuevoValor ->
                                        onActualizarHorarioDia(dia, horarioDia.copy(cierre = nuevoValor.take(12)))
                                    },
                                    label = { Text(stringResource(Res.string.comercio_horario_cierra)) },
                                    modifier = Modifier.width(90.dp),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onAplicarHorario,
                enabled = diasSeleccionados.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.comercio_btn_aplicar_horario))
            }
        } else {
            CampoSolicitud(
                etiqueta = stringResource(Res.string.comercio_label_horario_manual),
                valor = horario,
                alCambiar = onHorarioManualChange
            )
        }
    }
}

@Composable
internal fun CampoSolicitud(
    etiqueta: String,
    valor: String,
    alCambiar: (String) -> Unit,
    multilinea: Boolean = false,
    soloLectura: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    esError: Boolean = false,
    textoAyuda: String? = null
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        modifier = Modifier.fillMaxWidth(),
        readOnly = soloLectura,
        singleLine = !multilinea,
        minLines = if (multilinea) 3 else 1,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        isError = esError,
        supportingText = if (textoAyuda != null) {
            { Text(textoAyuda, color = MaterialTheme.colorScheme.error) }
        } else {
            null
        }
    )
}
