package com.turismap.app.screens.solicitud

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.theme.FondoBlancoAdaptable
import com.turismap.app.theme.GrisBorde
import com.turismap.app.theme.GrisFondo
import com.turismap.app.theme.GrisTextoSecundario
import com.turismap.app.theme.TextoPrincipalOscuro
import com.turismap.app.theme.VerdePrimario
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.btn_cerrar
import turismap.shared.generated.resources.comercio_horario_abre
import turismap.shared.generated.resources.comercio_horario_atajo_finde
import turismap.shared.generated.resources.comercio_horario_atajo_semana
import turismap.shared.generated.resources.comercio_horario_atajo_todos
import turismap.shared.generated.resources.comercio_horario_bloque_titulo
import turismap.shared.generated.resources.comercio_horario_btn_agregar
import turismap.shared.generated.resources.comercio_horario_btn_guardar
import turismap.shared.generated.resources.comercio_horario_btn_limpiar
import turismap.shared.generated.resources.comercio_horario_cd_eliminar_bloque
import turismap.shared.generated.resources.comercio_horario_cierra
import turismap.shared.generated.resources.comercio_horario_dialogo_abre
import turismap.shared.generated.resources.comercio_horario_dialogo_cierra
import turismap.shared.generated.resources.comercio_horario_error_horas_iguales
import turismap.shared.generated.resources.comercio_label_horario_atencion

private val VerdeHorarioOscuro = Color(0xFF6FCF97)
private val VerdeHorarioClaro = Color(0xFF1B4934)
private val TextoSobreVerdeOscuro = Color(0xFF0B251B)

private val DIAS_SEMANA_LABORAL = setOf(
    DiaSemanaSolicitud.LUNES,
    DiaSemanaSolicitud.MARTES,
    DiaSemanaSolicitud.MIERCOLES,
    DiaSemanaSolicitud.JUEVES,
    DiaSemanaSolicitud.VIERNES
)
private val DIAS_FIN_SEMANA = setOf(DiaSemanaSolicitud.SABADO, DiaSemanaSolicitud.DOMINGO)
private val TODOS_LOS_DIAS = DiaSemanaSolicitud.entries.toSet()

private enum class CampoHoraObjetivo { APERTURA, CIERRE }
private data class EdicionHoraActiva(val bloqueId: Int, val campo: CampoHoraObjetivo)
private data class AtajoDias(val etiquetaRes: StringResource, val dias: Set<DiaSemanaSolicitud>)

private val ATAJOS_DIAS = listOf(
    AtajoDias(Res.string.comercio_horario_atajo_todos, TODOS_LOS_DIAS),
    AtajoDias(Res.string.comercio_horario_atajo_semana, DIAS_SEMANA_LABORAL),
    AtajoDias(Res.string.comercio_horario_atajo_finde, DIAS_FIN_SEMANA)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HorarioAtencionModalSheet(
    bloquesIniciales: List<BloqueHorario>,
    tieneHorarioGuardado: Boolean,
    onDismiss: () -> Unit,
    onGuardar: (List<BloqueHorario>) -> Unit,
    onLimpiar: () -> Unit
) {
    val esModoOscuro = isSystemInDarkTheme()
    val colorAcento = if (esModoOscuro) VerdeHorarioOscuro else VerdeHorarioClaro
    val colorBotonGuardar = if (esModoOscuro) Color(0xFF2E7D57) else VerdeHorarioClaro
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var bloques by remember {
        mutableStateOf(
            bloquesIniciales.ifEmpty { listOf(BloqueHorario(id = 1, dias = DIAS_SEMANA_LABORAL)) }
        )
    }
    var siguienteId by remember { mutableIntStateOf((bloques.maxOfOrNull { it.id } ?: 1) + 1) }
    var edicionHora by remember { mutableStateOf<EdicionHoraActiva?>(null) }

    val diasAsignados = bloques.flatMap { it.dias }.toSet()
    val quedanDiasLibres = diasAsignados.size < DiaSemanaSolicitud.entries.size
    val puedeGuardar = bloques.isNotEmpty() && bloques.all { it.esValido }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FondoBlancoAdaptable,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = GrisBorde) }
    ) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
            // Cabecera coherente con SelectorModalSheet
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = colorAcento, modifier = Modifier.size(22.dp))
                    Text(
                        text = stringResource(Res.string.comercio_label_horario_atencion),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoPrincipalOscuro,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(Res.string.btn_cerrar), tint = GrisTextoSecundario)
                }
            }

            HorizontalDivider(color = GrisBorde, thickness = 1.dp)

            // Bloques horarios configurables
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                bloques.forEachIndexed { indice, bloque ->
                    val diasOcupadosOtros = bloques.filter { it.id != bloque.id }.flatMap { it.dias }.toSet()
                    TarjetaBloqueHorario(
                        indice = indice,
                        bloque = bloque,
                        diasOcupadosOtros = diasOcupadosOtros,
                        puedeEliminar = bloques.size > 1,
                        colorAcento = colorAcento,
                        esModoOscuro = esModoOscuro,
                        onEliminar = { bloques = bloques.filterNot { it.id == bloque.id } },
                        onActualizarDias = { nuevosDias ->
                            bloques = bloques.map { if (it.id == bloque.id) it.copy(dias = nuevosDias) else it }
                        },
                        onAbrirHora = { campo -> edicionHora = EdicionHoraActiva(bloque.id, campo) }
                    )
                }

                if (quedanDiasLibres) {
                    OutlinedButton(
                        onClick = {
                            bloques = bloques + BloqueHorario(id = siguienteId, dias = emptySet())
                            siguienteId++
                        },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, colorAcento.copy(alpha = 0.55f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colorAcento)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(Res.string.comercio_horario_btn_agregar), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            HorizontalDivider(color = GrisBorde, thickness = 1.dp)

            // Pie fijo con acción principal y opción discreta de limpiar
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Button(
                    onClick = { onGuardar(bloques) },
                    enabled = puedeGuardar,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorBotonGuardar,
                        contentColor = Color.White,
                        disabledContainerColor = GrisBorde,
                        disabledContentColor = GrisTextoSecundario
                    )
                ) {
                    Text(stringResource(Res.string.comercio_horario_btn_guardar), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                if (tieneHorarioGuardado) {
                    TextButton(onClick = onLimpiar) {
                        Text(
                            text = stringResource(Res.string.comercio_horario_btn_limpiar),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = GrisTextoSecundario
                        )
                    }
                }
            }
        }
    }

    edicionHora?.let { edicion ->
        val bloqueObjetivo = bloques.firstOrNull { it.id == edicion.bloqueId }
        if (bloqueObjetivo != null) {
            key(edicion) {
                val esApertura = edicion.campo == CampoHoraObjetivo.APERTURA
                DialogoSelectorHora(
                    titulo = stringResource(
                        if (esApertura) Res.string.comercio_horario_dialogo_abre else Res.string.comercio_horario_dialogo_cierra
                    ),
                    horaInicial = if (esApertura) bloqueObjetivo.horaAbre else bloqueObjetivo.horaCierra,
                    minutoInicial = if (esApertura) bloqueObjetivo.minutoAbre else bloqueObjetivo.minutoCierra,
                    colorAcento = colorAcento,
                    esModoOscuro = esModoOscuro,
                    onDismiss = { edicionHora = null },
                    onConfirmar = { nuevaHora, nuevoMinuto ->
                        bloques = bloques.map { actual ->
                            if (actual.id != bloqueObjetivo.id) actual
                            else if (esApertura) actual.copy(horaAbre = nuevaHora, minutoAbre = nuevoMinuto)
                            else actual.copy(horaCierra = nuevaHora, minutoCierra = nuevoMinuto)
                        }
                        edicionHora = null
                    }
                )
            }
        }
    }
}

@Composable
private fun TarjetaBloqueHorario(
    indice: Int,
    bloque: BloqueHorario,
    diasOcupadosOtros: Set<DiaSemanaSolicitud>,
    puedeEliminar: Boolean,
    colorAcento: Color,
    esModoOscuro: Boolean,
    onEliminar: () -> Unit,
    onActualizarDias: (Set<DiaSemanaSolicitud>) -> Unit,
    onAbrirHora: (CampoHoraObjetivo) -> Unit
) {
    val atajosAplicables = ATAJOS_DIAS.filter { atajo -> atajo.dias.none { it in diasOcupadosOtros } }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = GrisFondo,
        border = BorderStroke(1.dp, GrisBorde)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.comercio_horario_bloque_titulo, indice + 1),
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrincipalOscuro
                )
                if (puedeEliminar) {
                    IconButton(onClick = onEliminar, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = stringResource(Res.string.comercio_horario_cd_eliminar_bloque),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            if (atajosAplicables.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    atajosAplicables.forEach { atajo ->
                        val activo = bloque.dias == atajo.dias
                        Surface(
                            onClick = { onActualizarDias(atajo.dias) },
                            shape = RoundedCornerShape(20.dp),
                            color = if (activo) colorAcento.copy(alpha = if (esModoOscuro) 0.20f else 0.12f) else FondoBlancoAdaptable,
                            border = BorderStroke(1.dp, if (activo) colorAcento else GrisBorde)
                        ) {
                            Text(
                                text = stringResource(atajo.etiquetaRes),
                                fontSize = 12.sp,
                                fontWeight = if (activo) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (activo) colorAcento else GrisTextoSecundario,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // 7 chips circulares de días ordenados de Lunes a Domingo
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                DiaSemanaSolicitud.entries.forEach { dia ->
                    val seleccionado = dia in bloque.dias
                    val ocupadoPorOtro = dia in diasOcupadosOtros
                    val nombreDia = stringResource(dia.etiquetaRes)
                    val fondoChip = when {
                        seleccionado -> if (esModoOscuro) VerdeHorarioOscuro else VerdePrimario
                        ocupadoPorOtro -> GrisBorde.copy(alpha = if (esModoOscuro) 0.30f else 0.45f)
                        else -> FondoBlancoAdaptable
                    }
                    val textoChip = when {
                        seleccionado -> if (esModoOscuro) TextoSobreVerdeOscuro else Color.White
                        ocupadoPorOtro -> GrisTextoSecundario.copy(alpha = 0.40f)
                        else -> TextoPrincipalOscuro
                    }

                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Surface(
                            onClick = { onActualizarDias(if (seleccionado) bloque.dias - dia else bloque.dias + dia) },
                            enabled = !ocupadoPorOtro,
                            shape = CircleShape,
                            color = fondoChip,
                            border = if (!seleccionado && !ocupadoPorOtro) BorderStroke(1.dp, GrisBorde) else null,
                            modifier = Modifier.size(36.dp).semantics { contentDescription = nombreDia }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = stringResource(dia.inicialRes),
                                    fontSize = 13.sp,
                                    fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium,
                                    color = textoChip
                                )
                            }
                        }
                    }
                }
            }

            // Pills táctiles de apertura y cierre
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PillHoraBloque(
                    etiqueta = stringResource(Res.string.comercio_horario_abre),
                    horaTexto = formatearHora12h(bloque.horaAbre, bloque.minutoAbre),
                    esError = bloque.horasIguales,
                    colorAcento = colorAcento,
                    onClick = { onAbrirHora(CampoHoraObjetivo.APERTURA) },
                    modifier = Modifier.weight(1f)
                )
                PillHoraBloque(
                    etiqueta = stringResource(Res.string.comercio_horario_cierra),
                    horaTexto = formatearHora12h(bloque.horaCierra, bloque.minutoCierra),
                    esError = bloque.horasIguales,
                    colorAcento = colorAcento,
                    onClick = { onAbrirHora(CampoHoraObjetivo.CIERRE) },
                    modifier = Modifier.weight(1f)
                )
            }

            if (bloque.horasIguales) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(15.dp))
                    Text(
                        text = stringResource(Res.string.comercio_horario_error_horas_iguales),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
