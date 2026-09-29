package com.turismap.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.theme.*

@Composable
fun DetalleExperienciaScreen(onVolver: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onVolver,
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = GrisFondo
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Volver",
                        tint = VerdePrimario,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = "Detalle De Experiencia",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A202C)
            )

            Spacer(modifier = Modifier.size(40.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(VerdePrimario),
            contentAlignment = Alignment.BottomStart
        ) {
            Surface(
                modifier = Modifier.padding(16.dp),
                shape = RoundedCornerShape(50),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Text(
                    text = "Abierto hoy · 7:30 AM - 6:00 PM",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Finca & Café Las Nubes",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A202C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Concepción de Ataco, Ahuachapán · Km 92",
                fontSize = 14.sp,
                color = GrisTextoSecundario
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Calificación: 4.9 (420 opiniones) · Excelente",
                fontSize = 13.sp,
                color = TerracotaAcento,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "INFORMACIÓN CLAVE PARA TU VISITA",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GrisTextoSecundario,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AmenidadBadge(titulo = "Tarjetas y Chivo", subtitulo = "Aceptado", modifier = Modifier.weight(1f))
                AmenidadBadge(titulo = "Parqueo Privado", subtitulo = "Gratuito", modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AmenidadBadge(titulo = "Pet Friendly", subtitulo = "Áreas exteriores", modifier = Modifier.weight(1f))
                AmenidadBadge(titulo = "Clima 16°C", subtitulo = "Llevar abrigo", modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun AmenidadBadge(titulo: String, subtitulo: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = GrisFondo,
        border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = titulo, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2D3748))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitulo, fontSize = 11.sp, color = GrisTextoSecundario)
        }
    }
}