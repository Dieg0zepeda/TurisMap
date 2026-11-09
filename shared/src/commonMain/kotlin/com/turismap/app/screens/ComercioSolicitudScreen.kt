package com.turismap.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.data.models.CategoriaComercio
import com.turismap.app.data.models.Coordenada
import com.turismap.app.data.models.Lugar
import com.turismap.app.data.repository.ComercioRepository
import com.turismap.app.data.repository.LugaresRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

data class HorarioDia(
    val apertura: String = "",
    val cierre: String = ""
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ComercioSolicitudScreen(
    nombreCompletoInicial: String,
    emailInicial: String,
    planId: String,
    onBack: () -> Unit,
    onSubmitted: () -> Unit
) {
    val planIdNormalizado = remember(planId) { planId.trim().lowercase() }
    val esSocio = remember(planIdNormalizado) {
        planIdNormalizado.contains("socio") || planIdNormalizado.contains("premium")
    }
    val esPremium = remember(planIdNormalizado) {
        planIdNormalizado.contains("premium")
    }

    var lugares by remember { mutableStateOf<List<Lugar>>(emptyList()) }
    var cargandoLugares by remember { mutableStateOf(true) }
    var lugarSeleccionado by remember { mutableStateOf<Lugar?>(null) }
    var coordenadaSeleccionada by remember { mutableStateOf<Coordenada?>(null) }
    var seleccionandoUbicacion by remember { mutableStateOf(false) }
    var nombreCompleto by remember { mutableStateOf(nombreCompletoInicial.take(120)) }
    val email = emailInicial.trim()
    var nombreNegocio by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var categoriaId by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var sitioWeb by remember { mutableStateOf("") }
    var horario by remember { mutableStateOf("") }
    var promocionEspecial by remember { mutableStateOf("") }

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
    var horariosPorDia: Map<String, HorarioDia> by remember { mutableStateOf(emptyMap()) }
    var solicitudEnviada by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val digitosTelefono = remember(telefono) { telefono.count { it.isDigit() } }
    val telefonoInvalido = telefono.isNotBlank() && digitosTelefono < 8
    val sitioWebInvalido = esSocio && sitioWeb.isNotBlank() && !sitioWeb.trim().startsWith("https://", ignoreCase = true)

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
                "No hay categorías activas en categorias_comercio."
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
                        Icon(
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        esPremium -> Color(0xFFFEF3C7)
                        esSocio -> Color(0xFFEEF2FF)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = planNombre.ifBlank { "Plan Seleccionado" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = when {
                                esPremium -> Color(0xFF92400E)
                                esSocio -> Color(0xFF3730A3)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        planPrecio?.let {
                            Text(
                                text = it,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = when {
                                    esPremium -> Color(0xFFB45309)
                                    esSocio -> Color(0xFF4F46E5)
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when {
                            esPremium -> "Plan VIP: Incluye presencia en inicio, redes, horarios y promoción destacada."
                            esSocio -> "Plan Crecimiento: Incluye enlaces a redes sociales y horarios de atención."
                            else -> "Plan Básico: Presencia esencial en el directorio y mapa turístico."
                        },
                        fontSize = 12.5.sp,
                        color = Color(0xFF475569)
                    )
                }
            }

            Text("Tus datos de contacto", fontWeight = FontWeight.Bold)
            CampoSolicitud(
                etiqueta = "Nombre completo",
                valor = nombreCompleto,
                alCambiar = { nombreCompleto = it.take(120) }
            )
            CampoSolicitud(
                etiqueta = "Correo electrónico",
                valor = email,
                alCambiar = {},
                soloLectura = true
            )

            Text("Datos del comercio", fontWeight = FontWeight.Bold)
            CampoSolicitud(
                etiqueta = "Nombre del comercio",
                valor = nombreNegocio,
                alCambiar = { nombreNegocio = it.take(120) }
            )

            Column {
                Box {
                    OutlinedTextField(
                        value = lugarSeleccionado?.nombre.orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Ubicación del negocio (Pueblo)") },
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

            lugarSeleccionado?.let {
                Button(
                    onClick = { seleccionandoUbicacion = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (coordenadaSeleccionada == null) "Ubicar negocio en el mapa"
                        else "Cambiar ubicación en el mapa"
                    )
                }
                coordenadaSeleccionada?.let { coordenada ->
                    Text(
                        "Coordenadas guardadas: ${coordenada.latitud}, ${coordenada.longitud}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
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
                etiqueta = "Descripción breve (${descripcion.length}/1800)",
                valor = descripcion,
                alCambiar = { descripcion = it.take(1800) },
                multilinea = true
            )
            CampoSolicitud(
                etiqueta = "Dirección física",
                valor = direccion,
                alCambiar = { direccion = it.take(300) }
            )
            CampoSolicitud(
                etiqueta = "Teléfono de contacto (ej. 7000-0000)",
                valor = telefono,
                alCambiar = { input ->
                    telefono = input.filter { it.isDigit() || it in "+- ()" }.take(20)
                },
                keyboardType = KeyboardType.Phone,
                esError = telefonoInvalido,
                textoAyuda = if (telefonoInvalido) "Ingresa un número válido de al menos 8 dígitos" else null
            )

            if (esSocio) {
                Text("Detalles de Operación y Redes", fontWeight = FontWeight.Bold)
                CampoSolicitud(
                    etiqueta = "Sitio web o Instagram (ej. https://...)",
                    valor = sitioWeb,
                    alCambiar = { sitioWeb = it.take(200) },
                    keyboardType = KeyboardType.Uri,
                    esError = sitioWebInvalido,
                    textoAyuda = if (sitioWebInvalido) "El enlace debe comenzar con https://" else null
                )

                Column {
                    Text("Horario de atención", style = MaterialTheme.typography.labelLarge)
                    TextButton(onClick = { mostrarHorarioDetallado = !mostrarHorarioDetallado }) {
                        Text(if (mostrarHorarioDetallado) "Escribir horario manual" else "Configurar horario por días")
                    }
                    if (mostrarHorarioDetallado) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo").forEach { dia ->
                                val seleccionado = diasSeleccionados.contains(dia)
                                val horarioDia = horariosPorDia[dia] ?: HorarioDia()
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = seleccionado,
                                            onCheckedChange = { checked ->
                                                diasSeleccionados = if (checked) diasSeleccionados + dia else diasSeleccionados - dia
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(dia)
                                    }
                                    if (seleccionado) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = horarioDia.apertura,
                                                onValueChange = { nuevoValor ->
                                                    horariosPorDia = horariosPorDia + (dia to horarioDia.copy(apertura = nuevoValor.take(12)))
                                                },
                                                label = { Text("Abre") },
                                                modifier = Modifier.width(90.dp),
                                                singleLine = true
                                            )
                                            Text("a")
                                            OutlinedTextField(
                                                value = horarioDia.cierre,
                                                onValueChange = { nuevoValor ->
                                                    horariosPorDia = horariosPorDia + (dia to horarioDia.copy(cierre = nuevoValor.take(12)))
                                                },
                                                label = { Text("Cierra") },
                                                modifier = Modifier.width(90.dp),
                                                singleLine = true
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        LaunchedEffect(diasSeleccionados, horariosPorDia) {
                            horario = if (diasSeleccionados.isEmpty()) {
                                ""
                            } else {
                                diasSeleccionados.joinToString(", ") { dia ->
                                    val horarioDia = horariosPorDia[dia] ?: HorarioDia()
                                    val abre = horarioDia.apertura.trim().ifBlank { "8:00 AM" }
                                    val cierra = horarioDia.cierre.trim().ifBlank { "5:00 PM" }
                                    "$dia: $abre a $cierra"
                                }
                            }
                        }
                    } else {
                        CampoSolicitud(
                            etiqueta = "Horario (ej. Lun a Dom 8:00 AM - 6:00 PM)",
                            valor = horario,
                            alCambiar = { horario = it.take(200) }
                        )
                    }
                }
            }

            if (esPremium) {
                Text("Beneficio VIP TurisMap", fontWeight = FontWeight.Bold)
                CampoSolicitud(
                    etiqueta = "Promoción exclusiva para turistas (ej. 10% en café)",
                    valor = promocionEspecial,
                    alCambiar = { promocionEspecial = it.take(150) }
                )
            }

            errorEnvio?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            if (solicitudEnviada) {
                Text(
                    "Solicitud enviada con éxito. Será revisada y publicada desde el panel administrativo.",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
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
                            digitosTelefono >= 8 &&
                            !sitioWebInvalido &&
                            !solicitudEnviada,
                    onClick = {
                        val webLimpia = sitioWeb.trim()
                        if (esSocio && webLimpia.isNotBlank() && !webLimpia.startsWith("https://", ignoreCase = true)) {
                            errorEnvio = "El sitio web debe comenzar con https://."
                        } else {
                            enviando = true
                            errorEnvio = null
                            scope.launch {
                                try {
                                    val lugar = lugarSeleccionado ?: throw IllegalStateException("Selecciona una ubicación.")

                                    val descripcionFinal = if (esPremium && promocionEspecial.isNotBlank()) {
                                        "${descripcion.trim()}\n\n[PROMO TURISMAP]: ${promocionEspecial.trim()}"
                                    } else {
                                        descripcion.trim()
                                    }.take(2000)

                                    ComercioRepository.enviarSolicitudComercio(
                                        nombreCompleto = nombreCompleto.trim().take(120),
                                        email = email,
                                        municipio = lugar.nombre.trim().take(100),
                                        nombreNegocio = nombreNegocio.trim().take(120),
                                        categoria = categoriaId,
                                        descripcion = descripcionFinal,
                                        direccion = direccion.trim().take(300),
                                        telefono = telefono.trim().take(40),
                                        sitioWeb = if (esSocio) webLimpia else "",
                                        horario = if (esSocio) horario.trim() else "",
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
    soloLectura: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    esError: Boolean = false,
    textoAyuda: String? = null
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        modifier = Modifier.fillMaxWidth(),
        readOnly = soloLectura,
        singleLine = !multilinea,
        minLines = if (multilinea) 3 else 1,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        isError = esError,
        supportingText = if (textoAyuda != null) {
            { Text(textoAyuda, color = MaterialTheme.colorScheme.error) }
        } else null
    )
}