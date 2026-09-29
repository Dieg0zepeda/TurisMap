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
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.theme.*

// Modelo de datos local para los destinos de la guía
data class ItemGuia(
    val id: String,
    val titulo: String,
    val subtitulo: String,
    val categoriaTag: String,
    val calificacion: String,
    val precio: String?,
    val textoBoton: String?
)

@Composable
fun GuiaScreen() {
    var categoriaFiltro by remember { mutableStateOf("Todos") }
    val categorias = listOf("Todos", "Restaurantes", "Hoteles", "Cafés")

    // Datos basados en el prototipo de Figma
    val lugaresAtaco = remember {
        listOf(
            ItemGuia(
                id = "carmen",
                titulo = "Hotel El Carmen",
                subtitulo = "Ataco · A 3 min del parque",
                categoriaTag = "Hoteles & Cabañas",
                calificacion = "4.9 (280)",
                precio = "$75 / noche",
                textoBoton = "Reservar"
            ),
            ItemGuia(
                id = "rustico",
                titulo = "D'Rústico Restaurante",
                subtitulo = "Famoso por postres y moka",
                categoriaTag = "Restaurantes",
                calificacion = "4.8 (195)",
                precio = "$$ · Tradicional",
                textoBoton = null
            )
        )
    }

    val lugaresApaneca = remember {
        listOf(
            ItemGuia(
                id = "albania_guia",
                titulo = "Finca Café Albania",
                subtitulo = "Laberinto y tirolesa extrema",
                categoriaTag = "Aventura & Café",
                calificacion = "4.9 (420)",
                precio = "$5 / entrada",
                textoBoton = "Explorar"
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
        // 1. Header superior Guía
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
                    text = "Guía Turismap",
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

        // 2. Buscador específico de la guía
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(50.dp)
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(14.dp)),
            color = Color.White,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = GrisTextoSecundario,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Buscar en la guía (hoteles, café, tel...",
                    fontSize = 14.sp,
                    color = GrisTextoSecundario
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Chips de categorías
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categorias) { categoria ->
                val isSelected = categoria == categoriaFiltro
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { categoriaFiltro = categoria },
                    color = if (isSelected) VerdePrimario else Color.White,
                    shape = RoundedCornerShape(50),
                    border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, GrisBorde) else null,
                    shadowElevation = if (isSelected) 3.dp else 1.dp
                ) {
                    Text(
                        text = categoria,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color(0xFF2D3748),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Bloque Concepción de Ataco
        SeccionGuiaEncabezado(
            titulo = "Concepción de Ataco",
            subtitulo = "Pueblo de murales, telares y café gourmet"
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(lugaresAtaco) { item ->
                TarjetaGuiaItem(item = item)
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        // 5. Bloque Apaneca
        SeccionGuiaEncabezado(
            titulo = "Apaneca",
            subtitulo = "La cumbre más fresca de la cordillera · 1,470 msnm"
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(lugaresApaneca) { item ->
                TarjetaGuiaItem(item = item)
            }
        }
    }
}

// ================= COMPONENTES REUTILIZABLES DE LA GUÍA =================

@Composable
private fun SeccionGuiaEncabezado(titulo: String, subtitulo: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A202C)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitulo,
                fontSize = 12.sp,
                color = GrisTextoSecundario
            )
        }

        Text(
            text = "Ver todos >",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = VerdePrimario,
            modifier = Modifier.clickable { }
        )
    }
}

@Composable
private fun TarjetaGuiaItem(item: ItemGuia) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Contenedor visual simulador de imagen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(VerdePrimario.copy(alpha = 0.85f)),
                contentAlignment = Alignment.TopEnd
            ) {
                // Badge de categoría sobre la imagen
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = item.categoriaTag,
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                // Botón de favorito flotante
                Surface(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(30.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.9f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = TerracotaAcento,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Cuerpo informativo de la tarjeta
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = item.titulo,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A202C),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = item.subtitulo,
                    fontSize = 11.sp,
                    color = GrisTextoSecundario,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = TerracotaAcento,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = item.calificacion,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2D3748)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Fila de precio y acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item.precio?.let {
                        Text(
                            text = it,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A202C)
                        )
                    }

                    item.textoBoton?.let { botonTexto ->
                        Button(
                            onClick = { },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TerracotaAcento),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(
                                text = botonTexto,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}