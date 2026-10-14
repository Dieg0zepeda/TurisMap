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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.components.RemoteImage
import com.turismap.app.data.models.Lugar
import com.turismap.app.data.repository.Comercio
import com.turismap.app.data.repository.ComercioRepository
import com.turismap.app.data.repository.LugaresRepository
import com.turismap.app.theme.*
import kotlinx.coroutines.CancellationException

@Composable
fun ExplorarScreen(
    onLugarClick: (Lugar) -> Unit = {},
    onComercioClick: (String) -> Unit = {}
) {
    // Estados reactivos para la carga asíncrona
    var lugares by remember { mutableStateOf<List<Lugar>>(emptyList()) }
    var lugarDestacado by remember { mutableStateOf<Lugar?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var errorCarga by remember { mutableStateOf<String?>(null) }
    var intentoCarga by remember { mutableStateOf(0) }
    var experienciasDestacadas by remember { mutableStateOf<List<Comercio>>(emptyList()) }

    LaunchedEffect(intentoCarga) {
        cargando = true
        errorCarga = null
        try {
            lugares = LugaresRepository.obtenerTodos()
            lugarDestacado = lugares.firstOrNull { it.id == "ataco" } ?: lugares.firstOrNull()
            experienciasDestacadas = ComercioRepository.obtenerDestacados()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            errorCarga = error.message ?: "No se pudieron cargar los destinos."
        } finally {
            cargando = false
        }
    }

    // Renderizado condicional basado en el estado de carga
    if (cargando) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = VerdePrimario)
        }
    } else if (errorCarga != null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(errorCarga ?: "No se pudieron cargar los destinos.", color = Color(0xFF1A202C))
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { intentoCarga++ }) {
                Text("Reintentar")
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(GrisFondo)
                .padding(bottom = 24.dp)
        ) {
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

            if (lugarDestacado != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .padding(horizontal = 16.dp)
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp))
                        .clickable { onLugarClick(lugarDestacado!!) },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = VerdePrimario)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // INTEGRACIÓN REMOTE IMAGE: Fondo de Lugar Destacado
                        val urlImagenDestacado = lugarDestacado!!.atractivos.firstOrNull { it.imagenUrl.isNotEmpty() }?.imagenUrl ?: ""
                        if (urlImagenDestacado.isNotEmpty()) {
                            RemoteImage(
                                url = urlImagenDestacado,
                                contentDescription = "Fondo de ${lugarDestacado!!.nombre}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Gradiente oscuro para que el texto resalte
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                        startY = 300f
                                    )
                                )
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
                                text = "Descubre ${lugarDestacado!!.nombre.split(" ").last()}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = lugarDestacado!!.nombre,
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

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
                items(lugares) { lugar ->
                    Card(
                        modifier = Modifier
                            .width(140.dp)
                            .height(180.dp)
                            .clickable { onLugarClick(lugar) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                            ) {
                                // INTEGRACIÓN REMOTE IMAGE: Miniatura de la lista
                                val urlImagen = lugar.atractivos.firstOrNull { it.imagenUrl.isNotEmpty() }?.imagenUrl ?: ""
                                if (urlImagen.isNotEmpty()) {
                                    RemoteImage(
                                        url = urlImagen,
                                        contentDescription = lugar.nombre,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Surface(
                                    modifier = Modifier.padding(8.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.White.copy(alpha = 0.9f)
                                ) {
                                    val etiqueta = lugar.atractivos.firstOrNull()?.etiqueta ?: "Destino"
                                    Text(
                                        text = etiqueta,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VerdePrimario,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = lugar.nombre,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A202C),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = lugar.subtitulo,
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

            Text(
                text = "Experiencias Destacadas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A202C),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (experienciasDestacadas.isEmpty()) {
                Text(
                    "Aún no hay experiencias destacadas publicadas.",
                    color = GrisTextoSecundario,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    experienciasDestacadas.forEach { experiencia ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                onComercioClick(experiencia.id)
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                experiencia.imagenUrls.firstOrNull { it.startsWith("https://") }?.let { url ->
                                    RemoteImage(
                                        url = url,
                                        contentDescription = experiencia.nombre,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxWidth().height(150.dp)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                }
                                Text(experiencia.nombre, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text(experiencia.descripcion, color = GrisTextoSecundario)
                                experiencia.precioDesde?.let {
                                    Spacer(Modifier.height(8.dp))
                                    Text("Desde $it", color = TerracotaAcento, fontWeight = FontWeight.SemiBold)
                                }
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = { onComercioClick(experiencia.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = VerdePrimario)
                                ) { Text("Ver detalle") }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // Espacio para BottomBar
        }
    }
}