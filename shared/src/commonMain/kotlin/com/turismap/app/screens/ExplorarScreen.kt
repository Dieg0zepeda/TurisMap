package com.turismap.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
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

    // Filtra la lista para no duplicar el destino que ya está en el banner superior
    val destinosSinDestacado = remember(lugares, lugarDestacado) {
        lugares.filter { it.id != lugarDestacado?.id }
    }

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
            Text(errorCarga ?: "No se pudieron cargar los destinos.", color = TextoPrincipalOscuro)
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
            // Cabecera limpia sin icono de notificaciones
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "TURISMAP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = VerdeTextoAdaptable,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Explorar la Ruta",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrincipalOscuro
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Banner superior: Destino Destacado
            if (lugarDestacado != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 280.dp)
                        .padding(horizontal = 16.dp)
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp))
                        .clickable { onLugarClick(lugarDestacado!!) },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = VerdePrimario)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val urlImagenDestacado = lugarDestacado!!.atractivos.firstOrNull { it.imagenUrl.isNotEmpty() }?.imagenUrl ?: ""
                        if (urlImagenDestacado.isNotEmpty()) {
                            RemoteImage(
                                url = urlImagenDestacado,
                                contentDescription = "Fondo de ${lugarDestacado!!.nombre}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
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
                color = TextoPrincipalOscuro,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Carrusel horizontal sin el elemento repetido
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(destinosSinDestacado, key = { it.id }) { lugar ->
                    Card(
                        modifier = Modifier
                            .width(140.dp)
                            .clickable { onLugarClick(lugar) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = FondoBlancoAdaptable),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                            ) {
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
                                    color = FondoBlancoAdaptable.copy(alpha = 0.9f)
                                ) {
                                    val etiqueta = lugar.atractivos.firstOrNull()?.etiqueta ?: "Destino"
                                    Text(
                                        text = etiqueta,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VerdeTextoAdaptable,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = lugar.nombre,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextoPrincipalOscuro
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = lugar.subtitulo,
                                    fontSize = 12.sp,
                                    color = GrisTextoSecundario
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
                color = TextoPrincipalOscuro,
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    experienciasDestacadas.forEach { experiencia ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onComercioClick(experiencia.id)
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = FondoBlancoAdaptable)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                experiencia.imagenUrls.firstOrNull { it.startsWith("https://") }?.let { url ->
                                    RemoteImage(
                                        url = url,
                                        contentDescription = experiencia.nombre,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                }
                                Text(
                                    text = experiencia.nombre,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextoPrincipalOscuro
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = experiencia.descripcion,
                                    color = GrisTextoSecundario
                                )
                                experiencia.precioDesde?.let {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "Desde $it",
                                        color = TerracotaAcento,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = { onComercioClick(experiencia.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = VerdePrimario)
                                ) {
                                    Text("Ver detalle")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}