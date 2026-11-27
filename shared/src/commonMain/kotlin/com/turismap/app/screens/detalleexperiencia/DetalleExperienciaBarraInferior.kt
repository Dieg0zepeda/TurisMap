package com.turismap.app.screens.detalleexperiencia

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.detalle_exp_barra_label_categoria
import turismap.shared.generated.resources.detalle_exp_barra_label_horario
import turismap.shared.generated.resources.detalle_exp_barra_label_precio
import turismap.shared.generated.resources.detalle_exp_barra_label_ubicacion
import turismap.shared.generated.resources.detalle_exp_btn_iniciar_ruta

@Composable
internal fun BarraInferiorExperiencia(
    datosUi: DetalleExperienciaDatosUi,
    paleta: DetalleExperienciaPaleta,
    onIniciarRuta: () -> Unit,
    modifier: Modifier = Modifier
) {
    val etiquetaIzquierdaRes = when {
        datosUi.horarioLimpio.isNotBlank() -> Res.string.detalle_exp_barra_label_horario
        datosUi.municipioEtiqueta.isNotBlank() -> Res.string.detalle_exp_barra_label_ubicacion
        datosUi.precioTexto != null -> Res.string.detalle_exp_barra_label_precio
        datosUi.categoriaEtiqueta.isNotBlank() -> Res.string.detalle_exp_barra_label_categoria
        else -> null
    }
    val valorIzquierdo = when {
        datosUi.horarioLimpio.isNotBlank() -> datosUi.horarioLimpio
        datosUi.municipioEtiqueta.isNotBlank() -> datosUi.municipioEtiqueta
        datosUi.precioTexto != null -> datosUi.precioTexto
        datosUi.categoriaEtiqueta.isNotBlank() -> datosUi.categoriaEtiqueta
        else -> ""
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = paleta.superficieBarraInferior,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(thickness = 1.dp, color = paleta.borde)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = if (etiquetaIzquierdaRes != null) {
                    Arrangement.SpaceBetween
                } else {
                    Arrangement.End
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (etiquetaIzquierdaRes != null && valorIzquierdo.isNotBlank()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    ) {
                        Text(
                            text = stringResource(etiquetaIzquierdaRes),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = paleta.textoSecundario,
                            maxLines = 1
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = valorIzquierdo,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = paleta.textoPrincipal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Único botón primario sólido de toda la pantalla
                Button(
                    onClick = onIniciarRuta,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = paleta.primario,
                        contentColor = paleta.sobrePrimario
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Navigation,
                        contentDescription = null,
                        tint = paleta.sobrePrimario,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(Res.string.detalle_exp_btn_iniciar_ruta),
                        color = paleta.sobrePrimario,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
