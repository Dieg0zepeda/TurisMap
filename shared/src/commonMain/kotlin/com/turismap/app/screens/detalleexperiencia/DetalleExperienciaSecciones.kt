package com.turismap.app.screens.detalleexperiencia

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import turismap.shared.generated.resources.comercio_label_horario_atencion
import turismap.shared.generated.resources.detalle_exp_acerca_titulo
import turismap.shared.generated.resources.detalle_exp_btn_ver_mapa
import turismap.shared.generated.resources.detalle_exp_descripcion_fallback
import turismap.shared.generated.resources.detalle_exp_parada_recomendada
import turismap.shared.generated.resources.detalle_exp_premium_descripcion
import turismap.shared.generated.resources.detalle_exp_premium_franja_titulo
import turismap.shared.generated.resources.detalle_exp_servicios_titulo
import turismap.shared.generated.resources.detalle_exp_ubicacion_titulo

@Composable
internal fun TarjetaAcercaYHorario(
    datosUi: DetalleExperienciaDatosUi,
    paleta: DetalleExperienciaPaleta
) {
    val datos = datosUi.comercio

    Spacer(Modifier.height(16.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = paleta.superficieTarjeta),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, paleta.borde),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(Res.string.detalle_exp_acerca_titulo),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = paleta.textoSecundario
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = datos.descripcion.ifBlank {
                    stringResource(Res.string.detalle_exp_descripcion_fallback)
                },
                color = paleta.textoPrincipal,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )

            if (datos.servicios.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = stringResource(Res.string.detalle_exp_servicios_titulo),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = paleta.textoSecundario
                )
                Spacer(Modifier.height(8.dp))

                if (datosUi.esBasico) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        datos.servicios.forEach { servicio ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = paleta.textoSecundario,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = servicio,
                                    color = paleta.textoPrincipal,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        datos.servicios.forEach { servicio ->
                            Surface(
                                color = paleta.superficieContenedor,
                                border = BorderStroke(1.dp, paleta.borde),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = paleta.acentoVerdeTexto,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        text = servicio,
                                        color = paleta.textoPrincipal,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (datosUi.horarioLimpio.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(thickness = 1.dp, color = paleta.borde)
                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = paleta.acentoVerdeTexto,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(18.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(Res.string.comercio_label_horario_atencion),
                            color = paleta.textoSecundario,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = datosUi.horarioLimpio,
                            color = paleta.textoPrincipal,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 19.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun FranjaSocioDestacado(
    paleta: DetalleExperienciaPaleta
) {
    Spacer(Modifier.height(14.dp))
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = paleta.ambarFranjaFondo,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, paleta.ambarBorde)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Rounded.WorkspacePremium,
                contentDescription = null,
                tint = paleta.ambarAcento,
                modifier = Modifier
                    .padding(top = 1.dp)
                    .size(18.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.detalle_exp_premium_franja_titulo),
                    color = paleta.ambarTexto,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(Res.string.detalle_exp_premium_descripcion),
                    color = paleta.textoSecundario,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
internal fun TarjetaUbicacionComercio(
    datosUi: DetalleExperienciaDatosUi,
    paleta: DetalleExperienciaPaleta,
    onVerEnMapa: () -> Unit
) {
    val datos = datosUi.comercio
    val direccionTexto = datos.direccion.trim().ifBlank {
        listOf("Ruta de las Flores", datosUi.municipioEtiqueta)
            .filter(String::isNotBlank)
            .joinToString(", ")
    }

    Spacer(Modifier.height(14.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = paleta.superficieTarjeta),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, paleta.borde),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.detalle_exp_ubicacion_titulo),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = paleta.textoSecundario
                )
                if (datosUi.esPremium) {
                    Text(
                        text = stringResource(Res.string.detalle_exp_parada_recomendada),
                        color = paleta.acentoVerdeTexto,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = paleta.acentoVerdeTexto,
                        modifier = Modifier
                            .padding(top = 1.dp)
                            .size(18.dp)
                    )
                    Text(
                        text = direccionTexto,
                        color = paleta.textoPrincipal,
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OutlinedButton(
                    onClick = onVerEnMapa,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, paleta.borde),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = paleta.superficieContenedor,
                        contentColor = paleta.acentoVerdeTexto
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Map,
                        contentDescription = null,
                        tint = paleta.acentoVerdeTexto,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(Res.string.detalle_exp_btn_ver_mapa),
                        color = paleta.acentoVerdeTexto,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
