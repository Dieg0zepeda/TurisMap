package com.turismap.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.turismap.app.data.models.Coordenada
import com.turismap.app.data.models.CategoriaComercio
import com.turismap.app.data.models.Lugar
import com.turismap.app.data.repository.ComercioRepository
import com.turismap.app.data.repository.LugaresRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ComercioSolicitudScreen(
    nombreCompletoInicial: String,
    emailInicial: String,
    planId: String,
    onBack: () -> Unit,
    onSubmitted: () -> Unit
) {
    var lugares by remember { mutableStateOf<List<Lugar>>(emptyList()) }
    var cargandoLugares by remember { mutableStateOf(true) }
    var lugarSeleccionado by remember { mutableStateOf<Lugar?>(null) }
    var coordenadaSeleccionada by remember { mutableStateOf<Coordenada?>(null) }
    var seleccionandoUbicacion by remember { mutableStateOf(false) }
    var nombreCompleto by remember { mutableStateOf(nombreCompletoInicial) }
    val email = emailInicial.trim()
    var nombreNegocio by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var categoriaId by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var sitioWeb by remember { mutableStateOf("") }
    var horario by remember { mutableStateOf("") }
    var lugaresError by remember { mutableStateOf<String?>(null) }
    var errorEnvio by remember { mutableStateOf<String?>(null) }
    var enviando by remember { mutableStateOf(false) }
    var selectorMunicipioAbierto by remember { mutableStateOf(false) }
    var selectorCategoriaAbierto by remember { mutableStateOf(false) }
    var categorias by remember { mutableStateOf<List<CategoriaComercio>>(emptyList()) }
    var cargandoCategorias by remember { mutableStateOf(true) }
    var categoriasError by remember { mutableStateOf<String?>(null) }
    var planNombre by remember { mutableStateOf("") }
    var planPrecio by remember { mutableStateOf<String?>(null) }
    var cargandoPlan by remember { mutableStateOf(true) }
    var planError by remember { mutableStateOf<String?>(null) }
    var intentoCarga by remember { mutableStateOf(0) }
    var mostrarHorarioDetallado by remember { mutableStateOf(false) }
    var diasSeleccionados by remember { mutableStateOf<Set<String>>(emptySet()) }
    var horariosPorDia by remember { mutableStateOf<Map<String, Pair<String, String>>>(emptyMap()) }
    var solicitudEnviada by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(planId, intentoCarga) {
        cargandoLugares = true
        cargandoCategorias = true
        cargandoPlan = true
        lugaresError = null
        categoriasError = null
        planError = null
        planNombre = ""
        planPrecio = null

        try {
            lugares = LugaresRepository.obtenerTodos()
            lugaresError = if (lugares.isEmpty()) {
                "No hay ubicaciones disponibles en la colección lugares."
            } else {
                null
            }
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (error: Exception) {
            lugaresError = mensajeErrorCarga(
                error = error,
                coleccion = "lugares",
                mensajePredeterminado = "No se pudieron cargar las ubicaciones."
            )
        } finally {
            cargandoLugares = false
        }

        try {
            categorias = ComercioRepository.obtenerCategorias()
            categoriasError = if (categorias.isEmpty()) {
                "No hay categorías activas en categorias_comercio. Verifica que tengan activo: true y un nombre, descripción o ID."
            } else {
                null
            }
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (error: Exception) {
            categoriasError = mensajeErrorCarga(
                error = error,
                coleccion = "categorias_comercio",
                mensajePredeterminado = "No se pudieron cargar las categorías."
            )
        } finally {
            cargandoCategorias = false
        }

        try {
            val plan = ComercioRepository.obtenerPlanDisponible(planId)
            planNombre = plan?.nombre.orEmpty()
            planPrecio = plan?.precioFormateado()
            planError = if (plan == null) "El plan seleccionado ya no está disponible." else null
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (error: Exception) {
            planError = mensajeErrorCarga(
                error = error,
                coleccion = "planes_comercio",
                mensajePredeterminado = "No se pudo cargar el plan seleccionado."
            )
        } finally {
            cargandoPlan = false
        }
    }

    if (seleccionandoUbicacion) {
        val lugar = lugarSeleccionado
        if (lugar != null) {
            SeleccionarUbicacionComercioScreen(
                coordenadaInicial = lugar.ubicacionCoordenadas,
                coordenadaGuardada = coordenadaSeleccionada,
                onBack = { seleccionandoUbicacion = false },
                onGuardar = { coordenada ->
                    coordenadaSeleccionada = coordenada
                    seleccionandoUbicacion = false
                }
            )
        }
    } else Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar mi comercio") },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !enviando) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
                Text("Tus datos de contacto")
                CampoSolicitud(
                    etiqueta = "Nombre completo",
                    valor = nombreCompleto,
                    alCambiar = { nombreCompleto = it }
                )
                CampoSolicitud(
                    etiqueta = "Correo electrónico",
                    valor = email,
                    alCambiar = {},
                    soloLectura = true
                )

                Text("Datos del comercio")
                CampoSolicitud(
                    etiqueta = "Nombre del comercio",
                    valor = nombreNegocio,
                    alCambiar = { nombreNegocio = it }
                )
                Column {
                    Box {
                        OutlinedTextField(
                            value = lugarSeleccionado?.nombre.orEmpty(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Ubicación del negocio") },
                            trailingIcon = {
                                IconButton(
                                    onClick = { selectorMunicipioAbierto = true },
                                    enabled = !cargandoLugares && lugaresError == null && lugares.isNotEmpty()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Seleccionar ubicación"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = selectorMunicipioAbierto,
                            onDismissRequest = { selectorMunicipioAbierto = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            lugares.forEach { lugar ->
                                DropdownMenuItem(
                                    text = { Text(lugar.nombre) },
                                    onClick = {
                                        lugarSeleccionado = lugar
                                        coordenadaSeleccionada = null
                                        selectorMunicipioAbierto = false
                                        seleccionandoUbicacion = true
                                    }
                                )
                            }
                        }
                    }
                    if (cargandoLugares) {
                        CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                    } else {
                        lugaresError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
                lugarSeleccionado?.let { lugar ->
                    Text(
                        "Ubicación exacta",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Button(
                        onClick = { seleccionandoUbicacion = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (coordenadaSeleccionada == null) {
                                "Ubicar negocio en el mapa"
                            } else {
                                "Cambiar ubicación en el mapa"
                            }
                        )
                    }
                    coordenadaSeleccionada?.let { coordenada ->
                        Text(
                            "Coordenadas guardadas: ${coordenada.latitud}, ${coordenada.longitud}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Column {
                    Box {
                        OutlinedTextField(
                            value = categoria,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoría") },
                            trailingIcon = {
                                IconButton(
                                    onClick = { selectorCategoriaAbierto = true },
                                    enabled = !cargandoCategorias && categoriasError == null && categorias.isNotEmpty()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Seleccionar categoría"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = selectorCategoriaAbierto,
                            onDismissRequest = { selectorCategoriaAbierto = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            categorias.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion.etiqueta) },
                                    onClick = {
                                        categoria = opcion.etiqueta
                                        categoriaId = opcion.id
                                        selectorCategoriaAbierto = false
                                    }
                                )
                            }
                        }
                    }
                    if (cargandoCategorias) {
                        CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                    } else {
                        categoriasError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
                CampoSolicitud(
                    etiqueta = "Descripción breve",
                    valor = descripcion,
                    alCambiar = { descripcion = it },
                    multilinea = true
                )
                CampoSolicitud(
                    etiqueta = "Dirección",
                    valor = direccion,
                    alCambiar = { direccion = it }
                )
                CampoSolicitud(
                    etiqueta = "Teléfono",
                    valor = telefono,
                    alCambiar = { telefono = it }
                )
                CampoSolicitud(
                    etiqueta = "Sitio web (opcional, HTTPS)",
                    valor = sitioWeb,
                    alCambiar = { sitioWeb = it }
                )
                Column {
                    Text("Horario (opcional)", style = MaterialTheme.typography.labelLarge)
                    TextButton(onClick = { mostrarHorarioDetallado = !mostrarHorarioDetallado }) {
                        Text(if (mostrarHorarioDetallado) "Ocultar horario detallado" else "Configurar horario detallado")
                    }
                    if (mostrarHorarioDetallado) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo").forEach { dia ->
                                val seleccionado = diasSeleccionados.contains(dia)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = seleccionado,
                                            onCheckedChange = { checked ->
                                                diasSeleccionados = if (checked) {
                                                    diasSeleccionados + dia
                                                } else {
                                                    diasSeleccionados - dia
                                                }
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(dia)
                                    }
                                    if (seleccionado) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            OutlinedTextField(
                                                value = horariosPorDia[dia]?.first ?: "",
                                                onValueChange = { nuevoValor ->
                                                    horariosPorDia = horariosPorDia.toMutableMap().apply {
                                                        put(dia, Pair(nuevoValor, horariosPorDia[dia]?.second ?: ""))
                                                    }
                                                },
                                                label = { Text("Apertura") },
                                                modifier = Modifier.width(100.dp),
                                                singleLine = true
                                            )
                                            Text("a")
                                            OutlinedTextField(
                                                value = horariosPorDia[dia]?.second ?: "",
                                                onValueChange = { nuevoValor ->
                                                    horariosPorDia = horariosPorDia.toMutableMap().apply {
                                                        put(dia, Pair(horariosPorDia[dia]?.first ?: "", nuevoValor))
                                                    }
                                                },
                                                label = { Text("Cierre") },
                                                modifier = Modifier.width(100.dp),
                                                singleLine = true
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        // Generar string de horario automáticamente
                        LaunchedEffect(diasSeleccionados, horariosPorDia) {
                            horario = if (diasSeleccionados.isEmpty()) {
                                ""
                            } else {
                                diasSeleccionados.joinToString(", ") { dia ->
                                    val (apertura, cierre) = horariosPorDia[dia] ?: Pair("", "")
                                    "$dia: $apertura - $cierre"
                                }
                            }
                        }
                    } else {
                        CampoSolicitud(
                            etiqueta = "Horario (texto libre)",
                            valor = horario,
                            alCambiar = { horario = it },
                            multilinea = true
                        )
                    }
                }

                Text("Plan solicitado")
                when {
                    cargandoPlan -> CircularProgressIndicator()
                    planError != null -> Text(planError.orEmpty(), color = MaterialTheme.colorScheme.error)
                    else -> {
                        Text(planNombre, style = MaterialTheme.typography.titleMedium)
                        Text(
                            planPrecio ?: "Precio no disponible",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    "Esta solicitud indica interés en el plan. El equipo confirmará contigo el alcance y las condiciones.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                errorEnvio?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                if (solicitudEnviada) {
                    Text(
                        "Solicitud enviada. Un administrador debe revisarla antes de publicarse.",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        enabled = !enviando &&
                            !cargandoLugares &&
                            lugaresError == null &&
                            !cargandoCategorias &&
                            categoriasError == null &&
                            !cargandoPlan &&
                            planError == null &&
                            lugarSeleccionado != null &&
                            nombreCompleto.isNotBlank() &&
                            email.isNotBlank() &&
                            nombreNegocio.isNotBlank() &&
                            categoriaId.isNotBlank() &&
                            coordenadaSeleccionada != null &&
                            descripcion.isNotBlank() &&
                            direccion.isNotBlank() &&
                            telefono.isNotBlank() &&
                            !solicitudEnviada,
                        onClick = {
                            if (sitioWeb.isNotBlank() &&
                                !sitioWeb.startsWith("https://", ignoreCase = true)
                            ) {
                                errorEnvio = "El sitio web debe comenzar con https://."
                            } else {
                                enviando = true
                                errorEnvio = null
                                scope.launch {
                                    try {
                                        val lugar = lugarSeleccionado
                                            ?: throw IllegalStateException("Selecciona una ubicación.")
                                        ComercioRepository.enviarSolicitudComercio(
                                            nombreCompleto = nombreCompleto,
                                            email = email,
                                            municipio = lugar.nombre,
                                            nombreNegocio = nombreNegocio,
                                            categoria = categoriaId,
                                            descripcion = descripcion,
                                            direccion = direccion,
                                            telefono = telefono,
                                            sitioWeb = sitioWeb,
                                            horario = horario,
                                            planId = planId,
                                            coordenadas = coordenadaSeleccionada
                                                ?: throw IllegalStateException("Selecciona la ubicación exacta del negocio.")
                                        )
                                        solicitudEnviada = true
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
                        else Text(if (solicitudEnviada) "Solicitud enviada" else "Enviar solicitud")
                    }
                    TextButton(onClick = if (solicitudEnviada) onSubmitted else onBack, enabled = !enviando) {
                        Text(if (solicitudEnviada) "Volver a planes" else "Cancelar")
                    }
                }
                if (lugaresError != null || categoriasError != null || planError != null) {
                    TextButton(
                        onClick = { intentoCarga++ },
                        enabled = !cargandoLugares && !cargandoCategorias && !cargandoPlan && !enviando
                    ) {
                        Text("Reintentar carga")
                    }
                }
            }
        }
    }

private fun mensajeErrorCarga(
    error: Exception,
    coleccion: String,
    mensajePredeterminado: String
): String {
    val detalle = generateSequence(error as Throwable?) { it.cause }
        .mapNotNull(Throwable::message)
        .joinToString(" ")
    return if (
        detalle.contains("PERMISSION_DENIED", ignoreCase = true) ||
        detalle.contains("missing or insufficient permissions", ignoreCase = true)
    ) {
        "Firestore no permite leer $coleccion. Verifica las reglas publicadas para esta colección."
    } else {
        error.message?.takeIf(String::isNotBlank) ?: mensajePredeterminado
    }
}

@Composable
private fun CampoSolicitud(
    etiqueta: String,
    valor: String,
    alCambiar: (String) -> Unit,
    multilinea: Boolean = false,
    soloLectura: Boolean = false
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        modifier = Modifier.fillMaxWidth(),
        readOnly = soloLectura,
        singleLine = !multilinea,
        minLines = if (multilinea) 3 else 1
    )
}
