package com.turismap.app.screens.detalleexperiencia

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.data.repository.Resena
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.btn_reintentar
import turismap.shared.generated.resources.detalle_exp_btn_actualizar_resena
import turismap.shared.generated.resources.detalle_exp_btn_publicar_resena
import turismap.shared.generated.resources.detalle_exp_cd_calificacion_estrella
import turismap.shared.generated.resources.detalle_exp_resena_comentario_label
import turismap.shared.generated.resources.detalle_exp_resena_editar
import turismap.shared.generated.resources.detalle_exp_resena_escribir
import turismap.shared.generated.resources.detalle_exp_resena_fallback
import turismap.shared.generated.resources.detalle_exp_resena_guardada_exito
import turismap.shared.generated.resources.detalle_exp_resena_iniciar_sesion
import turismap.shared.generated.resources.detalle_exp_resena_propio_comercio
import turismap.shared.generated.resources.detalle_exp_resenas_cargando
import turismap.shared.generated.resources.detalle_exp_resenas_titulo
import turismap.shared.generated.resources.detalle_exp_resenas_vacio

@Composable
internal fun SeccionResenasExperiencia(
    resenas: List<Resena>,
    cargandoResenas: Boolean,
    errorResenas: String?,
    tieneUsuarioAutenticado: Boolean,
    esPropietario: Boolean,
    tieneResenaPropia: Boolean,
    calificacion: Int,
    comentario: String,
    enviando: Boolean,
    mensaje: String?,
    resenaGuardadaExito: Boolean,
    paleta: DetalleExperienciaPaleta,
    onCalificacionChange: (Int) -> Unit,
    onComentarioChange: (String) -> Unit,
    onPublicarResena: () -> Unit,
    onReintentarResenas: () -> Unit
) {
    val puedeResenar = tieneUsuarioAutenticado && !esPropietario

    Spacer(Modifier.height(20.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = paleta.superficieTarjeta),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, paleta.borde),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(Res.string.detalle_exp_resenas_titulo),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = paleta.textoPrincipal
            )
            Spacer(Modifier.height(8.dp))

            when {
                resenas.isEmpty() && cargandoResenas -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = paleta.primario,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = stringResource(Res.string.detalle_exp_resenas_cargando),
                            color = paleta.textoSecundario,
                            fontSize = 13.sp
                        )
                    }
                }
                resenas.isEmpty() && errorResenas != null -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = errorResenas,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp
                        )
                        OutlinedButton(
                            onClick = onReintentarResenas,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, paleta.borde),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = paleta.superficieContenedor,
                                contentColor = paleta.acentoVerdeTexto
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.btn_reintentar),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                resenas.isEmpty() -> {
                    Text(
                        text = stringResource(Res.string.detalle_exp_resenas_vacio),
                        color = paleta.textoSecundario,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        resenas.forEach { resena ->
                            TarjetaResenaIndividual(resena = resena, paleta = paleta)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(thickness = 1.dp, color = paleta.borde)
            Spacer(Modifier.height(14.dp))

            Text(
                text = stringResource(
                    when {
                        !tieneUsuarioAutenticado -> Res.string.detalle_exp_resena_iniciar_sesion
                        esPropietario -> Res.string.detalle_exp_resena_propio_comercio
                        tieneResenaPropia -> Res.string.detalle_exp_resena_editar
                        else -> Res.string.detalle_exp_resena_escribir
                    }
                ),
                fontWeight = FontWeight.SemiBold,
                color = paleta.textoPrincipal,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                (1..5).forEach { valor ->
                    val seleccionada = valor <= calificacion
                    IconButton(
                        onClick = { onCalificacionChange(valor) },
                        enabled = puedeResenar,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (seleccionada) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                            contentDescription = stringResource(
                                Res.string.detalle_exp_cd_calificacion_estrella,
                                valor
                            ),
                            tint = if (seleccionada) {
                                paleta.ambarAcento
                            } else {
                                paleta.textoSecundario.copy(alpha = if (puedeResenar) 0.7f else 0.4f)
                            },
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            OutlinedTextField(
                value = comentario,
                onValueChange = onComentarioChange,
                label = { Text(stringResource(Res.string.detalle_exp_resena_comentario_label)) },
                supportingText = if (puedeResenar) {
                    {
                        Text(
                            text = "${comentario.length} / $MAX_COMENTARIO_RESENA",
                            color = paleta.textoSecundario,
                            fontSize = 11.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    null
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = puedeResenar,
                minLines = 2,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = paleta.textoPrincipal,
                    unfocusedTextColor = paleta.textoPrincipal,
                    disabledTextColor = paleta.textoSecundario,
                    focusedContainerColor = paleta.superficieContenedor,
                    unfocusedContainerColor = paleta.superficieContenedor,
                    disabledContainerColor = paleta.superficieContenedor.copy(alpha = 0.6f),
                    focusedBorderColor = paleta.primario,
                    unfocusedBorderColor = paleta.borde,
                    disabledBorderColor = paleta.borde.copy(alpha = 0.6f),
                    focusedLabelColor = paleta.acentoVerdeTexto,
                    unfocusedLabelColor = paleta.textoSecundario,
                    cursorColor = paleta.primario
                )
            )

            mensaje?.let { textoError ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = textoError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }

            if (resenaGuardadaExito) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.detalle_exp_resena_guardada_exito),
                    color = paleta.acentoVerdeTexto,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.5.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            val botonHabilitado = puedeResenar && !enviando
            Button(
                onClick = onPublicarResena,
                enabled = botonHabilitado,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (botonHabilitado) paleta.primario else paleta.borde
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = paleta.contenedorPrimario,
                    contentColor = paleta.sobreContenedorPrimario,
                    disabledContainerColor = paleta.superficieContenedor,
                    disabledContentColor = paleta.textoSecundario
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                )
            ) {
                if (enviando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = paleta.sobreContenedorPrimario,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(
                            if (tieneResenaPropia) {
                                Res.string.detalle_exp_btn_actualizar_resena
                            } else {
                                Res.string.detalle_exp_btn_publicar_resena
                            }
                        ),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaResenaIndividual(
    resena: Resena,
    paleta: DetalleExperienciaPaleta
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = paleta.superficieContenedor,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, paleta.borde)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val estrellas = resena.calificacion.coerceIn(0, 5)
                (1..5).forEach { indice ->
                    val activa = indice <= estrellas
                    Icon(
                        imageVector = if (activa) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                        contentDescription = null,
                        tint = if (activa) paleta.ambarAcento else paleta.textoSecundario.copy(alpha = 0.45f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = resena.comentario.ifBlank {
                    stringResource(Res.string.detalle_exp_resena_fallback)
                },
                color = paleta.textoPrincipal,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
    }
}
