package com.turismap.app.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.turismap.app.data.models.Lugar
import com.turismap.app.data.repository.ComercioRepository
import com.turismap.app.data.repository.LugaresRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun ComercioSolicitudDialog(onDismiss: () -> Unit, onSubmitted: () -> Unit) {
    var lugares by remember { mutableStateOf<List<Lugar>>(emptyList()) }
    var cargandoLugares by remember { mutableStateOf(true) }
    var lugarSeleccionado by remember { mutableStateOf<Lugar?>(null) }
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var sitioWeb by remember { mutableStateOf("") }
    var horario by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var servicios by remember { mutableStateOf("") }
    var imagenUrl by remember { mutableStateOf("") }
    var lugaresError by remember { mutableStateOf<String?>(null) }
    var errorEnvio by remember { mutableStateOf<String?>(null) }
    var enviando by remember { mutableStateOf(false) }
    var selectorAbierto by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            lugares = LugaresRepository.obtenerTodos()
            lugaresError = if (lugares.isEmpty()) "No hay municipios disponibles para asociar el comercio." else null
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (error: Exception) {
            lugaresError = error.message ?: "No se pudieron cargar los municipios."
        } finally {
            cargandoLugares = false
        }
    }

    AlertDialog(
        onDismissRequest = { if (!enviando) onDismiss() },
        title = { Text("Solicitar registro de comercio") },
        text = {
            Column(
                Modifier.fillMaxWidth().heightIn(max = 520.dp).verticalScroll(rememberScrollState())
            ) {
                when {
                    cargandoLugares -> CircularProgressIndicator()
                    lugaresError != null -> Text(lugaresError.orEmpty())
                    else -> {
                        OutlinedTextField(
                            value = lugarSeleccionado?.nombre.orEmpty(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Municipio") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextButton(onClick = { selectorAbierto = true }) {
                            Text(if (lugarSeleccionado == null) "Elegir municipio" else "Cambiar municipio")
                        }
                        DropdownMenu(
                            expanded = selectorAbierto,
                            onDismissRequest = { selectorAbierto = false }
                        ) {
                            lugares.forEach { lugar ->
                                DropdownMenuItem(
                                    text = { Text(lugar.nombre) },
                                    onClick = {
                                        lugarSeleccionado = lugar
                                        selectorAbierto = false
                                    }
                                )
                            }
                        }
                    }
                }
                CampoSolicitud("Nombre del comercio", nombre, { nombre = it })
                CampoSolicitud("Categoría (restaurante, hotel...)", categoria, { categoria = it })
                CampoSolicitud("Descripción", descripcion, { descripcion = it }, multilinea = true)
                CampoSolicitud("Dirección", direccion, { direccion = it })
                CampoSolicitud("Teléfono", telefono, { telefono = it })
                CampoSolicitud("Sitio web (opcional)", sitioWeb, { sitioWeb = it })
                CampoSolicitud("Horario (opcional)", horario, { horario = it })
                CampoSolicitud("Precio desde (opcional)", precio, { precio = it })
                CampoSolicitud("Servicios separados por coma", servicios, { servicios = it })
                CampoSolicitud("URL HTTPS de imagen pública (opcional)", imagenUrl, { imagenUrl = it })
                Text("No se suben imágenes: la URL debe ser pública y comenzar con https://.")
                errorEnvio?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(
                enabled = !enviando && !cargandoLugares && lugarSeleccionado != null &&
                    nombre.isNotBlank() && categoria.isNotBlank() && descripcion.isNotBlank(),
                onClick = {
                    val precioValor = precio.trim().takeIf(String::isNotEmpty)?.toDoubleOrNull()
                    if (precio.isNotBlank() && (precioValor == null || precioValor < 0)) {
                        errorEnvio = "El precio debe ser un número igual o mayor que cero."
                    } else if (imagenUrl.isNotBlank() && !imagenUrl.startsWith("https://", ignoreCase = true)) {
                        errorEnvio = "La imagen debe usar una URL HTTPS pública."
                    } else if (sitioWeb.isNotBlank() && !sitioWeb.startsWith("https://", ignoreCase = true)) {
                        errorEnvio = "El sitio web debe comenzar con https://."
                    } else {
                        enviando = true
                        errorEnvio = null
                        scope.launch {
                            try {
                                ComercioRepository.enviarSolicitud(
                                    lugarId = lugarSeleccionado!!.id,
                                    nombre = nombre.trim(),
                                    descripcion = descripcion.trim(),
                                    categoria = categoria.trim(),
                                    direccion = direccion.trim(),
                                    telefono = telefono.trim(),
                                    sitioWeb = sitioWeb.trim(),
                                    horario = horario.trim(),
                                    precioDesde = precioValor,
                                    servicios = servicios.split(",").map(String::trim).filter(String::isNotEmpty),
                                    imagenUrls = imagenUrl.trim().takeIf(String::isNotEmpty)?.let(::listOf) ?: emptyList()
                                )
                                onSubmitted()
                            } catch (cancelacion: CancellationException) {
                                throw cancelacion
                            } catch (error: Exception) {
                                errorEnvio = error.message ?: "No se pudo enviar la solicitud."
                            } finally {
                                enviando = false
                            }
                        }
                    }
                }
            ) {
                if (enviando) CircularProgressIndicator()
                else Text("Enviar solicitud")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !enviando) { Text("Cancelar") } }
    )
}

@Composable
private fun CampoSolicitud(
    etiqueta: String,
    valor: String,
    alCambiar: (String) -> Unit,
    multilinea: Boolean = false
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = !multilinea,
        minLines = if (multilinea) 3 else 1
    )
}
