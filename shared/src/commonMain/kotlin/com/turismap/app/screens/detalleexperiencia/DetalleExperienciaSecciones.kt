package com.turismap.app.screens.detalleexperiencia

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.components.RemoteImage

@Composable
internal fun TarjetaExperienciasPremium(
    paleta: DetalleExperienciaPaleta,
    onAbrirCatalogo: () -> Unit
) {
    Spacer(Modifier.height(14.dp))
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (paleta.esOscuro) Color(0xFF071F14) else Color(0xFFF0FDF4)
        ),
        border = BorderStroke(1.2.dp, paleta.verdeTurisMap),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("👑", fontSize = 16.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "EXPERIENCIAS DESTACADAS",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = paleta.verdeOscuroTurisMap
                    )
                }
                Surface(
                    color = paleta.verdeTurisMap,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "PREMIUM",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "★ Experiencia recomendada en la Ruta de las Flores:",
                color = paleta.colorTextoPrincipal,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "Este comercio cuenta con atención prioritaria, especialidades insignia y promociones exclusivas para turistas.",
                color = paleta.colorTextoSecundario,
                fontSize = 12.5.sp,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onAbrirCatalogo,
                colors = ButtonDefaults.buttonColors(containerColor = paleta.verdeTurisMap),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📖", fontSize = 14.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Ver Experiencias y Catálogo",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                }
            }
        }
    }
}

@Composable
internal fun TarjetaUbicacionComercio(
    datosUi: DetalleExperienciaDatosUi,
    paleta: DetalleExperienciaPaleta,
    onAbrirMapa: () -> Unit
) {
    val datos = datosUi.comercio

    Spacer(Modifier.height(14.dp))
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (paleta.esOscuro) Color(0xFF131B26) else Color.White
        ),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, paleta.colorBordeSuave)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "UBICACIÓN EN LA RUTA",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = paleta.colorTextoSecundario
                )
                if (datosUi.esPremium) {
                    Text(
                        "Parada Recomendada",
                        color = paleta.verdeTurisMap,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("📍", fontSize = 16.sp)
                    Text(
                        datos.direccion.ifBlank { "Ruta de las Flores, ${datos.lugarId}" },
                        color = paleta.colorTextoPrincipal,
                        fontSize = 12.5.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = onAbrirMapa,
                    colors = ButtonDefaults.buttonColors(containerColor = paleta.verdeTurisMap),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "🗺️ Ver en Mapa",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp
                    )
                }
            }
        }
    }
}

@Composable
internal fun SeccionEnlacesDirectos(
    datosUi: DetalleExperienciaDatosUi,
    paleta: DetalleExperienciaPaleta,
    onAbrirWhatsapp: () -> Unit,
    onAbrirSitioWeb: () -> Unit
) {
    Spacer(Modifier.height(16.dp))
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "ENLACES DIRECTOS",
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = paleta.colorTextoSecundario
        )
        Text(
            if (datosUi.esPremium) "CANAL PREMIUM" else "CANAL OFICIAL",
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = paleta.verdeTurisMap
        )
    }

    Spacer(Modifier.height(8.dp))

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = onAbrirWhatsapp,
            colors = ButtonDefaults.buttonColors(containerColor = paleta.verdeTurisMap),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                "💬 WhatsApp Directo",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        OutlinedButton(
            onClick = onAbrirSitioWeb,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, paleta.verdeTurisMap.copy(alpha = 0.5f)),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                "🌐 Redes / Web",
                color = paleta.colorTextoPrincipal,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
internal fun SeccionDescripcionYServicios(
    datosUi: DetalleExperienciaDatosUi,
    paleta: DetalleExperienciaPaleta
) {
    val datos = datosUi.comercio

    // 6. Descripción
    Spacer(Modifier.height(18.dp))
    Text(
        "ACERCA DE ESTE LUGAR",
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        color = paleta.colorTextoSecundario,
        letterSpacing = 1.sp
    )
    Spacer(Modifier.height(6.dp))
    Text(
        datos.descripcion.ifBlank { "Un espacio acogedor en la Ruta de las Flores preparado para atenderte." },
        color = paleta.colorTextoPrincipal.copy(alpha = 0.9f),
        fontSize = 13.5.sp,
        lineHeight = 21.sp
    )

    // 7. Servicios
    if (datos.servicios.isNotEmpty()) {
        Spacer(Modifier.height(18.dp))
        Text(
            "SERVICIOS Y COMODIDADES",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = paleta.colorTextoSecundario,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(8.dp))

        if (datosUi.esBasico) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                datos.servicios.forEach { s ->
                    Text("• $s", color = paleta.colorTextoPrincipal, fontSize = 13.sp)
                }
            }
        } else {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                datos.servicios.forEach { servicio ->
                    Surface(
                        color = if (paleta.esOscuro) Color(0xFF0F261E) else Color(0xFFDCFCE7),
                        border = BorderStroke(1.dp, paleta.verdeTurisMap.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            "✦ $servicio",
                            color = if (paleta.esOscuro) Color(0xFF34D399) else Color(0xFF047857),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun FilaHorarioComercio(
    datosUi: DetalleExperienciaDatosUi,
    paleta: DetalleExperienciaPaleta
) {
    Spacer(Modifier.height(18.dp))
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(paleta.verdeTurisMap, CircleShape))
            Spacer(Modifier.width(6.dp))
            Text(
                if (datosUi.esPremium) "Abierto todos los días" else "Abierto ahora",
                color = if (paleta.esOscuro) Color(0xFF34D399) else Color(0xFF047857),
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp
            )
        }
        Text(
            datosUi.comercio.horario.ifBlank { "Lun a Dom: 7:00 AM - 8:00 PM" },
            color = paleta.colorTextoSecundario,
            fontSize = 12.5.sp
        )
    }
}

@Composable
internal fun GaleriaMiniaturas(
    fotos: List<String>,
    paleta: DetalleExperienciaPaleta
) {
    Spacer(Modifier.height(18.dp))
    Text(
        "VISTA DE GALERÍA (${fotos.size} FOTOS)",
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        color = paleta.colorTextoSecundario,
        letterSpacing = 1.sp
    )
    Spacer(Modifier.height(8.dp))
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        fotos.forEach { f ->
            RemoteImage(
                url = f,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp))
            )
        }
    }
}
