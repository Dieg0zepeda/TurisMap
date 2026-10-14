package com.turismap.app.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.data.repository.Comercio
import com.turismap.app.data.repository.ComercioRepository
import com.turismap.app.components.RemoteImage
import com.turismap.app.theme.GrisBorde
import com.turismap.app.theme.GrisFondo
import com.turismap.app.theme.GrisTextoSecundario
import com.turismap.app.theme.VerdePrimario
import kotlinx.coroutines.CancellationException

@Composable
fun GuiaScreen(onComercioClick: (String) -> Unit = {}) {
    var comercios by remember { mutableStateOf<List<Comercio>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var intento by remember { mutableIntStateOf(0) }
    var categoriaSeleccionada by remember { mutableStateOf("Todos") }
    var busqueda by remember { mutableStateOf("") }

    LaunchedEffect(intento) {
        cargando = true
        error = null
        try {
            comercios = ComercioRepository.obtenerPublicados()
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (excepcion: Exception) {
            error = excepcion.message ?: "No se pudieron cargar los comercios."
        } finally {
            cargando = false
        }
    }

    val categorias = remember(comercios) {
        listOf("Todos") + comercios.map { it.categoria }.filter { it.isNotBlank() }.distinct().sorted()
    }
    val filtrados = comercios.filter { comercio ->
        (categoriaSeleccionada == "Todos" || comercio.categoria.equals(categoriaSeleccionada, true)) &&
            (busqueda.isBlank() || listOf(
                comercio.nombre, comercio.descripcion, comercio.categoria, comercio.direccion,
                comercio.lugarId
            ).any { it.contains(busqueda, ignoreCase = true) } ||
                comercio.servicios.any { it.contains(busqueda, ignoreCase = true) })
    }

    Column(
        modifier = Modifier.fillMaxSize().background(GrisFondo).padding(bottom = 24.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("TURISMAP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VerdePrimario, letterSpacing = 1.2.sp)
            Text("Guía Comercial", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A202C))
        }
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(50.dp),
            color = Color.White, shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, GrisBorde)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = GrisTextoSecundario)
                Spacer(Modifier.width(10.dp))
                BasicTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF1A202C)),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (busqueda.isEmpty()) Text("Buscar comercios...", color = GrisTextoSecundario, fontSize = 14.sp)
                        inner()
                    }
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categorias) { categoria ->
                FilterChip(
                    selected = categoria == categoriaSeleccionada,
                    onClick = { categoriaSeleccionada = categoria },
                    label = { Text(categoria) }
                )
            }
        }

        when {
            cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VerdePrimario)
            }
            error != null -> Column(
                Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
                Button(onClick = { intento++ }) { Text("Reintentar") }
            }
            filtrados.isEmpty() -> Column(
                Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.SearchOff, contentDescription = null, tint = GrisTextoSecundario)
                Spacer(Modifier.height(8.dp))
                Text(
                    if (comercios.isEmpty()) "Todavía no hay comercios publicados."
                    else "No se encontraron comercios.",
                    color = GrisTextoSecundario
                )
            }
            else -> Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtrados, key = { it.id }) { comercio ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onComercioClick(comercio.id) },
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                comercio.imagenUrls.firstOrNull { it.startsWith("https://") }?.let { url ->
                                    RemoteImage(
                                        url = url,
                                        contentDescription = comercio.nombre,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxWidth().height(150.dp)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                }
                                Text(comercio.nombre, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A202C))
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    listOf(comercio.categoria, comercio.direccion.ifBlank { comercio.lugarId })
                                        .filter(String::isNotBlank).joinToString(" · "),
                                    fontSize = 12.sp, color = GrisTextoSecundario
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(comercio.descripcion, maxLines = 2, color = Color(0xFF2D3748))
                                Spacer(Modifier.height(10.dp))
                                Text("Toca para ver información y reseñas", fontSize = 12.sp, color = GrisTextoSecundario)
                            }
                        }
                    }
                }
            }
        }
    }
}
