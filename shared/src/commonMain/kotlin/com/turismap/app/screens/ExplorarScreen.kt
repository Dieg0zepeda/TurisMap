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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.theme.*

// --- Modelos de datos ---
data class Pueblo(val nombre: String, val descripcion: String, val etiqueta: String)
data class ExperienciaCard(val titulo: String, val descripcion: String, val precio: String)

@Composable
fun ExplorarScreen(
    onPuebloClick: (Pueblo) -> Unit = {}
) {
    val pueblos = remember {
        listOf(
            Pueblo("Apaneca", "1,450 msnm", "Elevación"),
            Pueblo("Juayúa", "Cascadas de la Calera y feria gastronómica.", "Naturaleza"),
            Pueblo("Salcoatitán", "Cuna de la yuca frita y el café artesanal.", "Gastronomía"),
            Pueblo("Nahuizalco", "Mercado nocturno y artesanías de mimbre.", "Cultura")
        )
    }

    val experienciasDestacadas = remember {
        listOf(
            ExperienciaCard(
                "Finca San Antonio El Portezuelo",
                "Balcón panorámico con vistas al volcán de Izalco.",
                "Desde $4.50 / persona"
            ),
            ExperienciaCard(
                "Café Entre Nubes y Mirador",
                "Café de altura rodeado de neblina y pinos.",
                "Desde $3.50 / persona"
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
        // --- 1. Header superior (Se mantiene igual, está excelente) ---
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

        // --- 2. Tarjeta Destacada "Descubre Ataco" (Ajustada al Figma) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp) // Tarjeta alta como en el diseño
                .padding(horizontal = 16.dp)
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp)).clickable { onPuebloClick(Pueblo("Concepción de Ataco", "Destino Destacado", "Murales")) },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = VerdePrimario) // Aquí irá la imagen de fondo luego

        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Simulación del gradiente oscuro en la parte inferior para que el texto resalte
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "DESTINO DESTACADO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            letterSpacing = 0.8.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Descubre Ataco",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Concepción de Ataco",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- 3. Carrusel Horizontal "Explora la Ruta" (Ajustado al Figma) ---
        Text(
            text = "Explora la Ruta",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A202C),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(pueblos) { pueblo ->
                Card(
                    modifier = Modifier
                        .width(140.dp)
                        .height(180.dp)
                        .clickable { onPuebloClick(pueblo) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Área de la imagen superior
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .background(Color(0xFFE2E8F0)) // Simulación de imagen
                        ) {
                            // Badge superior izquierdo (Elevación / Naturaleza)
                            Surface(
                                modifier = Modifier.padding(8.dp),
                                shape = RoundedCornerShape(6.dp),
                                color = Color.White.copy(alpha = 0.9f)
                            ) {
                                Text(
                                    text = pueblo.etiqueta,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VerdePrimario,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Textos de la tarjeta
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = pueblo.nombre,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A202C)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = pueblo.descripcion,
                                fontSize = 12.sp,
                                color = GrisTextoSecundario,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- 4. Sección "Experiencias Destacadas" (Tarjetas Verticales del Figma) ---
        Text(
            text = "Experiencias Destacadas",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A202C),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            experienciasDestacadas.forEach { experiencia ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        // Área de imagen ancha
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .background(Color(0xFFE2E8F0)) // Simulación de imagen
                        ) {
                            // Botón de favoritos (corazón)
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                                    .size(32.dp),
                                shape = CircleShape,
                                color = Color.White
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.FavoriteBorder,
                                        contentDescription = "Guardar",
                                        tint = TerracotaAcento,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Contenido de la tarjeta de experiencia
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = experiencia.titulo,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A202C)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = experiencia.descripcion,
                                fontSize = 14.sp,
                                color = GrisTextoSecundario
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Fila inferior: Precio y Botón "Ver Menú"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = experiencia.precio,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TerracotaAcento
                                )

                                Button(
                                    onClick = { },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFEDF2F7), // Gris claro
                                        contentColor = VerdePrimario
                                    ),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Ver Menú",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}