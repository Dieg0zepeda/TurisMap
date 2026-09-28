package com.turismap.app.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.theme.*

// Descomentar al añadir imágenes:
// import androidx.compose.foundation.Image
// import androidx.compose.ui.layout.ContentScale
// import org.jetbrains.compose.resources.painterResource
// import turismap.composeapp.generated.resources.Res
// import turismap.composeapp.generated.resources.*

@Composable
fun DetalleJuayuaScreen(
    onBackClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrisFondo)
            .verticalScroll(rememberScrollState())
    ) {
        // --- 1. CABECERA (Imagen y Botones Flotantes) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color(0xFF8BA896))
        ) {
            // ---> AQUI VA LA FOTO PRINCIPAL DE JUAYÚA <---
            /*
            Image(
                painter = painterResource(Res.drawable.foto_principal_juayua),
                contentDescription = "Juayúa",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            */

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(40.dp).clickable { onBackClick() }
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Atrás", modifier = Modifier.padding(8.dp), tint = Color(0xFF1A202C))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(40.dp)) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Compartir", modifier = Modifier.padding(10.dp), tint = Color(0xFF1A202C))
                    }
                    Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(40.dp)) {
                        Icon(imageVector = Icons.Default.FavoriteBorder, contentDescription = "Guardar", modifier = Modifier.padding(10.dp), tint = Color(0xFF1A202C))
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = VerdePrimario, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Ruta de las Flores · CA-8 Km 81", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VerdePrimario)
                }
            }
        }

        // --- 2. TARJETA PRINCIPAL DE INFORMACIÓN ---
        Column(modifier = Modifier.offset(y = (-20).dp)) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = GrisTextoSecundario, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Sonsonante Norte · El Salvador", fontSize = 12.sp, color = GrisTextoSecundario)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Juayúa", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A202C))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Capital gastronómica de la cordillera y paraíso de cascadas", fontSize = 14.sp, color = GrisTextoSecundario)

                    Spacer(modifier = Modifier.height(20.dp))

                    // Estadísticas
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = GrisTextoSecundario, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "1,025 msnm", fontSize = 13.sp, color = Color(0xFF1A202C), fontWeight = FontWeight.Medium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = GrisTextoSecundario, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "21°C · Agradable", fontSize = 13.sp, color = Color(0xFF1A202C), fontWeight = FontWeight.Medium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = TerracotaAcento, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "4.9 (1,580)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(text = "📖 Historia & Esencia", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A202C))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Célebre por su legendaria Feria Gastronómica de fin de semana en torno al Parque Central y sus senderos naturales que descienden a refrescantes pozas de manantiales volcánicos.",
                        fontSize = 14.sp, lineHeight = 22.sp, color = GrisTextoSecundario
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Surface(shape = RoundedCornerShape(12.dp), color = VerdePrimario.copy(alpha = 0.05f), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.DateRange, contentDescription = null, tint = VerdePrimario)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "MEJOR TEMPORADA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = VerdePrimario)
                                Text(text = "Fines de semana · Feria culinaria activa sábado y domingo", fontSize = 13.sp, color = Color(0xFF1A202C))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Atractivos Imperdibles", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A202C))
                        Text(text = "5 Destacados", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VerdePrimario)
                    }
                    Text(text = "Lo que no te puedes perder en tu recorrido", fontSize = 13.sp, color = GrisTextoSecundario)
                }
            }

            // --- 3. CARRUSEL DE ATRACTIVOS ---
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.offset(y = (-20).dp)
            ) {
                item {
                    AtractivoJuayuaCard(
                        etiqueta = "Cascadas & Pozas",
                        titulo = "Chorros de la Calera",
                        descripcion = "Impresionantes caídas de agua turquesa entre muros de roca volcánica.",
                        accion = "Ver sendero →"
                    )
                }
                item {
                    AtractivoJuayuaCard(
                        etiqueta = "Cocina Típica",
                        titulo = "Festival Gastronómico",
                        descripcion = "Parrilladas campestres, carnes asadas y platillos exóticos al aire libre.",
                        accion = "Ver menú de feria →"
                    )
                }
                item {
                    AtractivoJuayuaCard(
                        etiqueta = "Barranquismo",
                        titulo = "Siete Cascadas",
                        descripcion = "Aventura guiada de senderismo técnico y cañonismo entre cañones verdes.",
                        accion = "Ver tour guiado →"
                    )
                }
                item {
                    AtractivoJuayuaCard(
                        etiqueta = "Patrimonio",
                        titulo = "Templo Cristo Negro",
                        descripcion = "Hermosa basílica colonial con más de un siglo de fervor e historia.",
                        accion = "Ver historia →"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 4. DIRECTORIO COMUNITARIO ---
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = VerdePrimario)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(text = "DIRECTORIO LOCAL ACTIVO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.7f), letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "¿Planeas tu visita a Juayúa?", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Descubre 45 comedores tradicionales, hoteles coloniales y guías certificados para cascadas.", fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f), lineHeight = 18.sp)

                    Spacer(modifier = Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        DirectorioIconoJuayua(icono = Icons.Default.Restaurant, texto = "24 Comedores &\nParrillas")
                        DirectorioIconoJuayua(icono = Icons.Default.Home, texto = "10 Posadas\nColoniales")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        DirectorioIconoJuayua(icono = Icons.Default.LocalCafe, texto = "6 Cafés de\nPlaza")
                        DirectorioIconoJuayua(icono = Icons.Default.DirectionsWalk, texto = "5 Guías\nSenderismo")
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TerracotaAcento),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Ver Guía de Comercio y Ocio →", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 5. UBICACIÓN Y ENTORNO ---
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Ubicación y Entorno", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A202C))
                    Text(text = "Valle de Juayúa", fontSize = 12.sp, color = GrisTextoSecundario)
                }
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE2E8F0))
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                        // ---> AQUI VA LA FOTO DEL MAPA DE JUAYUA <---
                        /*
                        Image(
                            painter = painterResource(Res.drawable.mapa_juayua),
                            contentDescription = "Mapa Juayúa",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        */
                        Surface(modifier = Modifier.fillMaxWidth().padding(12.dp), shape = RoundedCornerShape(12.dp), color = Color.White) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = TerracotaAcento, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Carretera CA-8 · Acceso directo desde Sonsonate", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1A202C))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { }, modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, VerdePrimario), colors = ButtonDefaults.outlinedButtonColors(contentColor = VerdePrimario)
                    ) {
                        Icon(imageVector = Icons.Default.Place, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Ver en Mapa", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { }, modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = TerracotaAcento)
                    ) {
                        Icon(imageVector = Icons.Default.FavoriteBorder, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Guardar Viaje", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun AtractivoJuayuaCard(etiqueta: String, titulo: String, descripcion: String, accion: String) {
    Card(
        modifier = Modifier.width(200.dp).height(240.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(110.dp).background(Color(0xFFE2E8F0))) {
                // ---> AQUI VA LA FOTO DEL ATRACTIVO DE JUAYUA <---
                /*
                Image(
                    painter = painterResource(Res.drawable.foto_atractivo_juayua),
                    contentDescription = titulo,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                */
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(10.dp).align(Alignment.TopStart)
                ) {
                    Text(text = etiqueta, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = VerdePrimario, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = titulo, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A202C))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = descripcion, fontSize = 12.sp, color = GrisTextoSecundario, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = accion, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TerracotaAcento)
            }
        }
    }
}

@Composable
fun DirectorioIconoJuayua(icono: androidx.compose.ui.graphics.vector.ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.width(130.dp)) {
        Surface(shape = RoundedCornerShape(8.dp), color = Color.White.copy(alpha = 0.15f), modifier = Modifier.size(36.dp)) {
            Icon(imageVector = icono, contentDescription = null, tint = Color.White, modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = texto, fontSize = 12.sp, color = Color.White, lineHeight = 16.sp)
    }
}