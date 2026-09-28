package com.turismap.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.theme.*

// Modelos de datos para la pantalla Explorar
data class PuebloDestacado(
    val nombre: String,
    val departamento: String,
    val atraccionPrincipal: String
)

data class ExperienciaRuta(
    val titulo: String,
    val ubicacion: String,
    val duracion: String,
    val calificacion: String,
    val etiqueta: String
)

@Composable
fun ExplorarScreen() {
    val pueblos = remember {
        listOf(
            PuebloDestacado("Juayúa", "Sonsonate", "Cascadas Los Chorros"),
            PuebloDestacado("Ataco", "Ahuachapán", "Murales y Telares"),
            PuebloDestacado("Apaneca", "Ahuachapán", "Café de Altura y Laberinto"),
            PuebloDestacado("Nahuizalco", "Sonsonate", "Artesanías de Mimbre"),
            PuebloDestacado("Salcoatitán", "Sonsonate", "Plaza de la Yuca")
        )
    }

    val experiencias = remember {
        listOf(
            ExperienciaRuta(
                titulo = "Tour del Café y Catanas",
                ubicacion = "Apaneca · Finca San Antonio",
                duracion = "3 horas",
                calificacion = "4.9",
                etiqueta = "Aventura"
            ),
            ExperienciaRuta(
                titulo = "Ruta de los Murales Iluminados",
                ubicacion = "Concepción de Ataco · Centro",
                duracion = "1.5 horas",
                calificacion = "4.8",
                etiqueta = "Cultura"
            ),
            ExperienciaRuta(
                titulo = "Caminata Cascada Los Chorros de la Calera",
                ubicacion = "Juayúa · Sendero Ecológico",
                duracion = "2 horas",
                calificacion = "4.9",
                etiqueta = "Naturaleza"
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(GrisFondo)
            .padding(bottom = 24.dp)
    ) {
        // 1. Header superior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TURISMAP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = VerdePrimario,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Explorar la Ruta",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A202C)
                )
            }

            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notificaciones",
                        tint = VerdePrimario,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Banner Hero Destacado
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = VerdePrimario)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "EVENTO DE TEMPORADA · CA-8 KM 82",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Festival Gastronómico de Juayúa",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Prueba la gastronomía tradicional salvadoreña al aire libre en la plaza central este fin de semana.",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TerracotaAcento),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Ver detalles de ruta",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Sección "Pueblos de la Cordillera"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PUEBLOS DE LA CORDILLERA",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GrisTextoSecundario,
                letterSpacing = 1.sp
            )
            Text(
                text = "5 Destinos",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = VerdePrimario
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(pueblos) { pueblo ->
                Surface(
                    modifier = Modifier
                        .width(170.dp)
                        .clickable { }
                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = VerdePrimario,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = pueblo.nombre,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A202C)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = pueblo.departamento,
                            fontSize = 11.sp,
                            color = GrisTextoSecundario
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = pueblo.atraccionPrincipal,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TerracotaAcento,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        // 4. Sección "Rutas y Experiencias Recomendadas"
        Text(
            text = "EXPERIENCIAS RECOMENDADAS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = GrisTextoSecundario,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            experiencias.forEach { item ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(VerdePrimario.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = VerdePrimario,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.titulo,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A202C)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${item.ubicacion} · ${item.duracion}",
                                fontSize = 11.sp,
                                color = GrisTextoSecundario
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = TerracotaAcento,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = item.calificacion,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2D3748)
                            )
                        }
                    }
                }
            }
        }
    }
}