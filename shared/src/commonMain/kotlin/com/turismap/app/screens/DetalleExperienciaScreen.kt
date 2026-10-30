package com.turismap.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.components.RemoteImage
import com.turismap.app.data.repository.AutenticacionRepository
import com.turismap.app.data.repository.Comercio
import com.turismap.app.data.repository.ComercioRepository
import com.turismap.app.data.repository.Resena
import com.turismap.app.theme.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun DetalleExperienciaScreen(comercioId: String, onVolver: () -> Unit) {
    var comercio by remember(comercioId) { mutableStateOf<Comercio?>(null) }
    var resenas by remember(comercioId) { mutableStateOf<List<Resena>>(emptyList()) }
    var cargando by remember(comercioId) { mutableStateOf(true) }
    var error by remember(comercioId) { mutableStateOf<String?>(null) }
    var intento by remember(comercioId) { mutableIntStateOf(0) }
    var calificacion by remember { mutableIntStateOf(0) }
    var comentario by remember { mutableStateOf("") }
    var enviando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(comercioId, intento) {
        cargando = true
        error = null
        try {
            comercio = ComercioRepository.obtenerPorId(comercioId)
                ?.takeIf { it.estado == "publicado" }
                ?: throw IllegalStateException("Este comercio no está disponible.")
            resenas = ComercioRepository.obtenerResenas(comercioId)
            val propia = resenas.firstOrNull {
                it.usuarioUid == AutenticacionRepository.usuarioActual?.uid
            }
            calificacion = propia?.calificacion ?: 0
            comentario = propia?.comentario.orEmpty()
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (excepcion: Exception) {
            error = excepcion.message ?: "No se pudo cargar el comercio."
        } finally {
            cargando = false
        }
    }

    Column(Modifier.fillMaxSize().background(androidx.compose.material3.MaterialTheme.colorScheme.background)) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = VerdeTextoAdaptable)
            }
            Text("Detalle del comercio", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        when {
            cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VerdePrimario)
            }
            error != null -> Column(
                Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
                Button(onClick = { intento++ }) { Text("Reintentar") }
            }
            comercio != null -> {
                val datos = comercio!!
                val esPropietario = datos.propietarioUid == AutenticacionRepository.usuarioActual?.uid
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 28.dp)
                ) {
                    val imagen = datos.imagenUrls.firstOrNull { it.startsWith("https://") }
                    if (imagen != null) {
                        RemoteImage(
                            url = imagen,
                            contentDescription = datos.nombre,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(210.dp)
                        )
                    } else {
                        Box(
                            Modifier.fillMaxWidth().height(150.dp).background(GrisFondo),
                            contentAlignment = Alignment.Center
                        ) { Text(datos.categoria, color = VerdeTextoAdaptable, fontWeight = FontWeight.SemiBold) }
                    }
                    Column(Modifier.padding(20.dp)) {
                        Text(datos.nombre, fontSize = 25.sp, fontWeight = FontWeight.Bold, color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground)
                        Text(
                            listOf(datos.categoria, datos.direccion.ifBlank { datos.lugarId })
                                .filter(String::isNotBlank).joinToString(" · "),
                            color = GrisTextoSecundario
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(datos.descripcion, color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f), lineHeight = 21.sp)
                        if (datos.precioDesde != null) {
                            Spacer(Modifier.height(12.dp))
                            Text("Desde ${datos.precioDesde}", fontWeight = FontWeight.Bold, color = TerracotaAcento)
                        }
                        if (datos.servicios.isNotEmpty()) {
                            Spacer(Modifier.height(16.dp))
                            Text("Servicios", fontWeight = FontWeight.Bold)
                            Text(datos.servicios.joinToString(" · "), color = GrisTextoSecundario)
                        }
                        listOf(
                            "Horario" to datos.horario,
                            "Teléfono" to datos.telefono,
                            "Sitio web" to datos.sitioWeb
                        ).filter { it.second.isNotBlank() }.forEach { (etiqueta, valor) ->
                            Spacer(Modifier.height(12.dp))
                            Text(etiqueta, fontWeight = FontWeight.SemiBold)
                            Text(valor, color = GrisTextoSecundario)
                        }
                        if (datos.telefono.isNotBlank() || datos.sitioWeb.isNotBlank()) {
                            Row(
                                Modifier.fillMaxWidth().padding(top = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (datos.telefono.isNotBlank()) {
                                    Button(
                                        onClick = {
                                            try {
                                                val telefonoUri = datos.telefono.filter { it.isDigit() || it == '+' }
                                                uriHandler.openUri("tel:$telefonoUri")
                                            } catch (excepcion: Exception) {
                                                mensaje = excepcion.message ?: "No se pudo abrir la aplicación de llamadas."
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Llamar") }
                                }
                                if (datos.sitioWeb.isNotBlank()) {
                                    OutlinedButton(
                                        onClick = {
                                            try {
                                                uriHandler.openUri(datos.sitioWeb)
                                            } catch (excepcion: Exception) {
                                                mensaje = excepcion.message ?: "No se pudo abrir el sitio web."
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Abrir sitio web") }
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                        val promedio = if (resenas.isEmpty()) null else resenas.map { it.calificacion }.average()
                        Text("Reseñas", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = TerracotaAcento)
                            Text(
                                if (promedio == null) "Aún no hay reseñas"
                                else "${kotlin.math.round(promedio * 10) / 10} · ${resenas.size} reseñas",
                                color = GrisTextoSecundario
                            )
                        }
                        resenas.forEach { resena ->
                            Card(
                                Modifier.fillMaxWidth().padding(top = 10.dp),
                                colors = CardDefaults.cardColors(containerColor = GrisFondo)
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text("★".repeat(resena.calificacion), color = TerracotaAcento)
                                    Text(resena.comentario.ifBlank { "Sin comentario" }, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))
                        Text(
                            if (AutenticacionRepository.usuarioActual == null) "Inicia sesión para reseñar"
                            else if (esPropietario) "No puedes reseñar tu propio comercio"
                            else "Tu reseña",
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            (1..5).forEach { valor ->
                                TextButton(
                                    onClick = { calificacion = valor },
                                    enabled = AutenticacionRepository.usuarioActual != null && !esPropietario
                                ) {
                                    Text(
                                        if (valor <= calificacion) "★" else "☆",
                                        color = TerracotaAcento,
                                        fontSize = 23.sp
                                    )
                                }
                            }
                        }
                        OutlinedTextField(
                            value = comentario,
                            onValueChange = { comentario = it.take(2000) },
                            label = { Text("Comentario (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = AutenticacionRepository.usuarioActual != null && !esPropietario,
                            minLines = 2
                        )
                        mensaje?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        Button(
                            onClick = {
                                if (calificacion !in 1..5) {
                                    mensaje = "Selecciona una calificación de 1 a 5."
                                } else {
                                    enviando = true
                                    mensaje = null
                                    scope.launch {
                                        try {
                                            ComercioRepository.enviarResena(datos.id, calificacion, comentario)
                                            intento++
                                        } catch (cancelacion: CancellationException) {
                                            throw cancelacion
                                        } catch (excepcion: Exception) {
                                            mensaje = excepcion.message ?: "No se pudo guardar la reseña."
                                        } finally {
                                            enviando = false
                                        }
                                    }
                                }
                            },
                            enabled = AutenticacionRepository.usuarioActual != null && !esPropietario && !enviando,
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TerracotaAcento)
                        ) {
                            if (enviando) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
                            else Text("Publicar reseña", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
