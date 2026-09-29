package com.turismap.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.theme.*

@Composable
fun PerfilScreen() {
    // Variable única: cualquier nombre que pongas aquí calculará sus iniciales automáticamente
    val nombreUsuario = "marvin figueria"
    val iniciales = remember(nombreUsuario) {
        nombreUsuario
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercase() }
            .joinToString("")
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
                    text = "Perfil Turismap",
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

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Ficha de usuario y avatar
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(VerdePrimario),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iniciales,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = nombreUsuario,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A202C)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Explorador de Montaña · 14 Pueblos Visitados",
                fontSize = 13.sp,
                color = GrisTextoSecundario
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(50),
                color = VerdePrimario.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = VerdePrimario,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Guía Local TurisMap · Nivel 4",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = VerdePrimario
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Métricas (Favoritos / Rutas / Reseñas)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricaItem(numero = "28", etiqueta = "FAVORITOS")
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(GrisBorde)
                )
                MetricaItem(numero = "12", etiqueta = "RUTAS CA-8")
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(GrisBorde)
                )
                MetricaItem(numero = "9", etiqueta = "RESEÑAS")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Banner TurisMap Negocios
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = VerdePrimario)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "TurisMap Negocios",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Promociona tu negocio en la Ruta",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "¿Tienes un hotel, café, restaurante o taller en la CA-8? Llega a más de 45,000 viajeros mensuales en busca de experiencias auténticas.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TerracotaAcento),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Registrar mi comercio",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5. Sección de viajes y guardados
        Text(
            text = "VIAJES Y GUARDADOS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = GrisTextoSecundario,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde)
        ) {
            Column {
                OpcionPerfilFila(
                    icono = Icons.Default.Favorite,
                    titulo = "Mis Favoritos",
                    subtitulo = "Cabañas, miradores y cafeterías",
                    valorExtra = "28"
                )
                HorizontalDivider(color = GrisBorde, thickness = 0.8.dp)
                OpcionPerfilFila(
                    icono = Icons.Default.Place,
                    titulo = "Historial de Rutas",
                    subtitulo = "Tours CA-8 realizados y mapa offline",
                    valorExtra = null
                )
            }
        }
    }
}

// Subcomponentes auxiliares del perfil
@Composable
private fun MetricaItem(numero: String, etiqueta: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = numero,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A202C)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = etiqueta,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = GrisTextoSecundario,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun OpcionPerfilFila(
    icono: ImageVector,
    titulo: String,
    subtitulo: String,
    valorExtra: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(38.dp),
            shape = RoundedCornerShape(10.dp),
            color = GrisFondo
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = TerracotaAcento,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A202C)
            )
            Text(
                text = subtitulo,
                fontSize = 12.sp,
                color = GrisTextoSecundario
            )
        }

        if (valorExtra != null) {
            Text(
                text = valorExtra,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = GrisTextoSecundario
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = GrisTextoSecundario,
            modifier = Modifier.size(18.dp)
        )
    }
}