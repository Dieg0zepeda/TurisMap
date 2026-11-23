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
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.btn_cancelar
import turismap.shared.generated.resources.btn_reintentar
import turismap.shared.generated.resources.comercio_btn_aplicar_horario
import turismap.shared.generated.resources.comercio_btn_cambiar_ubicacion_mapa
import turismap.shared.generated.resources.comercio_btn_enviar_solicitud
import turismap.shared.generated.resources.comercio_btn_horario_manual
import turismap.shared.generated.resources.comercio_btn_horario_por_dias
import turismap.shared.generated.resources.comercio_btn_solicitud_enviada
import turismap.shared.generated.resources.comercio_btn_ubicar_mapa
import turismap.shared.generated.resources.comercio_btn_volver_planes
import turismap.shared.generated.resources.comercio_cd_seleccionar_categoria
import turismap.shared.generated.resources.comercio_cd_seleccionar_ubicacion
import turismap.shared.generated.resources.comercio_coordenadas_guardadas
import turismap.shared.generated.resources.comercio_dia_domingo
import turismap.shared.generated.resources.comercio_dia_jueves
import turismap.shared.generated.resources.comercio_dia_lunes
import turismap.shared.generated.resources.comercio_dia_martes
import turismap.shared.generated.resources.comercio_dia_miercoles
import turismap.shared.generated.resources.comercio_dia_sabado
import turismap.shared.generated.resources.comercio_dia_viernes
import turismap.shared.generated.resources.comercio_error_cargar_categorias
import turismap.shared.generated.resources.comercio_error_cargar_plan
import turismap.shared.generated.resources.comercio_error_cargar_ubicaciones
import turismap.shared.generated.resources.comercio_error_enviar_solicitud
import turismap.shared.generated.resources.comercio_error_permisos_coleccion
import turismap.shared.generated.resources.comercio_error_plan_no_disponible
import turismap.shared.generated.resources.comercio_error_seleccionar_ubicacion
import turismap.shared.generated.resources.comercio_error_seleccionar_ubicacion_exacta
import turismap.shared.generated.resources.comercio_error_sin_categorias
import turismap.shared.generated.resources.comercio_error_sin_ubicacion_para_mapa
import turismap.shared.generated.resources.comercio_error_sin_ubicaciones
import turismap.shared.generated.resources.comercio_error_sitio_web_https
import turismap.shared.generated.resources.comercio_error_telefono_digitos
import turismap.shared.generated.resources.comercio_horario_abre
import turismap.shared.generated.resources.comercio_horario_cierra
import turismap.shared.generated.resources.comercio_horario_conector_a
import turismap.shared.generated.resources.comercio_label_categoria
import turismap.shared.generated.resources.comercio_label_correo
import turismap.shared.generated.resources.comercio_label_descripcion
import turismap.shared.generated.resources.comercio_label_direccion
import turismap.shared.generated.resources.comercio_label_horario_atencion
import turismap.shared.generated.resources.comercio_label_horario_manual
import turismap.shared.generated.resources.comercio_label_nombre_comercio
import turismap.shared.generated.resources.comercio_label_nombre_completo
import turismap.shared.generated.resources.comercio_label_promocion
import turismap.shared.generated.resources.comercio_label_sitio_web
import turismap.shared.generated.resources.comercio_label_telefono
import turismap.shared.generated.resources.comercio_label_ubicacion_pueblo
import turismap.shared.generated.resources.comercio_plan_desc_basico
import turismap.shared.generated.resources.comercio_plan_desc_crecimiento
import turismap.shared.generated.resources.comercio_plan_desc_vip
import turismap.shared.generated.resources.comercio_plan_seleccionado_fallback
import turismap.shared.generated.resources.comercio_seccion_beneficio_vip
import turismap.shared.generated.resources.comercio_seccion_contacto
import turismap.shared.generated.resources.comercio_seccion_datos_comercio
import turismap.shared.generated.resources.comercio_seccion_operacion_redes
import turismap.shared.generated.resources.comercio_solicitud_exito
import turismap.shared.generated.resources.comercio_titulo_registrar
import turismap.shared.generated.resources.planes_cd_volver

data class HorarioDia(
    val apertura: String = "",
    val cierre: String = ""
)

private enum class DiaSemanaSolicitud(
    val nombrePersistencia: String
) {
    LUNES("Lunes"),
    MARTES("Martes"),
    MIERCOLES("Miércoles"),
    JUEVES("Jueves"),
    VIERNES("Viernes"),
    SABADO("Sábado"),
    DOMINGO("Domingo");

    val etiquetaRes: StringResource
        get() = when (this) {
            LUNES -> Res.string.comercio_dia_lunes
            MARTES -> Res.string.comercio_dia_martes
            MIERCOLES -> Res.string.comercio_dia_miercoles
            JUEVES -> Res.string.comercio_dia_jueves
            VIERNES -> Res.string.comercio_dia_viernes
            SABADO -> Res.string.comercio_dia_sabado
            DOMINGO -> Res.string.comercio_dia_domingo
        }
}

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
    var diasSeleccionados by remember { mutableStateOf<Set<DiaSemanaSolicitud>>(emptySet()) }
    var horariosPorDia: Map<DiaSemanaSolicitud, HorarioDia> by remember { mutableStateOf(emptyMap()) }
    var solicitudEnviada by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val errorSitioWebHttps = stringResource(Res.string.comercio_error_sitio_web_https)
    val errorTelefonoDigitos = stringResource(Res.string.comercio_error_telefono_digitos)

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
                getString(Res.string.comercio_error_sin_ubicaciones)
            } else {
                null
            }
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (error: Exception) {
            lugaresError = mensajeErrorCarga(
                error = error,
                coleccion = "lugares",
                mensajePredeterminadoRes = Res.string.comercio_error_cargar_ubicaciones
            )
        } finally {
            cargandoLugares = false
        }

        try {
            categorias = ComercioRepository.obtenerCategorias()
            categoriasError = if (categorias.isEmpty()) {
                getString(Res.string.comercio_error_sin_categorias)
            } else {
                null
            }
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (error: Exception) {
            categoriasError = mensajeErrorCarga(
                error = error,
                coleccion = "categorias_comercio",
                mensajePredeterminadoRes = Res.string.comercio_error_cargar_categorias
            )
        } finally {
            cargandoCategorias = false
        }

        try {
            val plan = ComercioRepository.obtenerPlanDisponible(planId)
            planNombre = plan?.nombre.orEmpty()
            planPrecio = plan?.precioFormateado()
            planError = if (plan == null) getString(Res.string.comercio_error_plan_no_disponible) else null
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (error: Exception) {
            planError = mensajeErrorCarga(
                error = error,
                coleccion = "planes_comercio",
                mensajePredeterminadoRes = Res.string.comercio_error_cargar_plan
            )
        } finally {
            cargandoPlan = false
        }
    }

    val lugarParaMapa = lugarSeleccionado
    if (seleccionandoUbicacion && lugarParaMapa != null) {
        SeleccionarUbicacionComercioScreen(
            coordenadaInicial = lugarParaMapa.ubicacionCoordenadas,
            coordenadaGuardada = coordenadaSeleccionada,
            onBack = { seleccionandoUbicacion = false },
            onGuardar = { coordenada ->
                coordenadaSeleccionada = coordenada
                seleccionandoUbicacion = false
            }
        )
    } else {
        LaunchedEffect(seleccionandoUbicacion, lugarParaMapa) {
            if (seleccionandoUbicacion && lugarParaMapa == null) {
                seleccionandoUbicacion = false
                errorEnvio = getString(Res.string.comercio_error_sin_ubicacion_para_mapa)
            }
        }
        Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.comercio_titulo_registrar)) },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !enviando) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.planes_cd_volver)
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
                            text = planNombre.ifBlank { stringResource(Res.string.comercio_plan_seleccionado_fallback) },
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
                        text = stringResource(
                            when {
                                esPremium -> Res.string.comercio_plan_desc_vip
                                esSocio -> Res.string.comercio_plan_desc_crecimiento
                                else -> Res.string.comercio_plan_desc_basico
                            }
                        ),
                        fontSize = 12.5.sp,
                        color = Color(0xFF475569)
                    )
                }
            }

            if (cargandoPlan) {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 4.dp))
            } else {
                planError?.let { errorPlan ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(errorPlan, color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = { intentoCarga++ }) {
                            Text(stringResource(Res.string.btn_reintentar))
                        }
                    }
                }
            }

            Text(stringResource(Res.string.comercio_seccion_contacto), fontWeight = FontWeight.Bold)
            CampoSolicitud(
                etiqueta = stringResource(Res.string.comercio_label_nombre_completo),
                valor = nombreCompleto,
                alCambiar = { nombreCompleto = it.take(120) }
            )
            CampoSolicitud(
                etiqueta = stringResource(Res.string.comercio_label_correo),
                valor = email,
                alCambiar = {},
                soloLectura = true
            )

            Text(stringResource(Res.string.comercio_seccion_datos_comercio), fontWeight = FontWeight.Bold)
            CampoSolicitud(
                etiqueta = stringResource(Res.string.comercio_label_nombre_comercio),
                valor = nombreNegocio,
                alCambiar = { nombreNegocio = it.take(120) }
            )

            Column {
                Box {
                    OutlinedTextField(
                        value = lugarSeleccionado?.nombre.orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(Res.string.comercio_label_ubicacion_pueblo)) },
                        trailingIcon = {
                            IconButton(
                                onClick = { selectorMunicipioAbierto = true },
                                enabled = !cargandoLugares && lugaresError == null && lugares.isNotEmpty()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = stringResource(Res.string.comercio_cd_seleccionar_ubicacion)
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
                        stringResource(
                            if (coordenadaSeleccionada == null) {
                                Res.string.comercio_btn_ubicar_mapa
                            } else {
                                Res.string.comercio_btn_cambiar_ubicacion_mapa
                            }
                        )
                    )
                }
                coordenadaSeleccionada?.let { coordenada ->
                    Text(
                        stringResource(
                            Res.string.comercio_coordenadas_guardadas,
                            coordenada.latitud.toString(),
                            coordenada.longitud.toString()
                        ),
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
                        label = { Text(stringResource(Res.string.comercio_label_categoria)) },
                        trailingIcon = {
                            IconButton(
                                onClick = { selectorCategoriaAbierto = true },
                                enabled = !cargandoCategorias && categoriasError == null && categorias.isNotEmpty()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = stringResource(Res.string.comercio_cd_seleccionar_categoria)
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
                etiqueta = "${stringResource(Res.string.comercio_label_descripcion)} (${descripcion.length}/1800)",
                valor = descripcion,
                alCambiar = { descripcion = it.take(1800) },
                multilinea = true
            )
            CampoSolicitud(
                etiqueta = stringResource(Res.string.comercio_label_direccion),
                valor = direccion,
                alCambiar = { direccion = it.take(300) }
            )
            CampoSolicitud(
                etiqueta = stringResource(Res.string.comercio_label_telefono),
                valor = telefono,
                alCambiar = { input ->
                    telefono = input.filter { it.isDigit() || it in "+- ()" }.take(20)
                },
                keyboardType = KeyboardType.Phone,
                esError = telefonoInvalido,
                textoAyuda = if (telefonoInvalido) errorTelefonoDigitos else null
            )

            if (esSocio) {
                Text(stringResource(Res.string.comercio_seccion_operacion_redes), fontWeight = FontWeight.Bold)
                CampoSolicitud(
                    etiqueta = stringResource(Res.string.comercio_label_sitio_web),
                    valor = sitioWeb,
                    alCambiar = { sitioWeb = it.take(200) },
                    keyboardType = KeyboardType.Uri,
                    esError = sitioWebInvalido,
                    textoAyuda = if (sitioWebInvalido) errorSitioWebHttps else null
                )

                Column {
                    Text(
                        stringResource(Res.string.comercio_label_horario_atencion),
                        style = MaterialTheme.typography.labelLarge
                    )
                    TextButton(onClick = { mostrarHorarioDetallado = !mostrarHorarioDetallado }) {
                        Text(
                            stringResource(
                                if (mostrarHorarioDetallado) {
                                    Res.string.comercio_btn_horario_manual
                                } else {
                                    Res.string.comercio_btn_horario_por_dias
                                }
                            )
                        )
                    }
                    if (mostrarHorarioDetallado) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DiaSemanaSolicitud.entries.forEach { dia ->
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
                                        Text(stringResource(dia.etiquetaRes))
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
                                                label = { Text(stringResource(Res.string.comercio_horario_abre)) },
                                                modifier = Modifier.width(90.dp),
                                                singleLine = true
                                            )
                                            Text(stringResource(Res.string.comercio_horario_conector_a))
                                            OutlinedTextField(
                                                value = horarioDia.cierre,
                                                onValueChange = { nuevoValor ->
                                                    horariosPorDia = horariosPorDia + (dia to horarioDia.copy(cierre = nuevoValor.take(12)))
                                                },
                                                label = { Text(stringResource(Res.string.comercio_horario_cierra)) },
                                                modifier = Modifier.width(90.dp),
                                                singleLine = true
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val diasOrdenados = DiaSemanaSolicitud.entries.filter { it in diasSeleccionados }
                                horario = diasOrdenados.joinToString(", ") { dia ->
                                    val horarioDia = horariosPorDia[dia] ?: HorarioDia()
                                    val abre = horarioDia.apertura.trim().ifBlank { "8:00 AM" }
                                    val cierra = horarioDia.cierre.trim().ifBlank { "5:00 PM" }
                                    "${dia.nombrePersistencia}: $abre a $cierra"
                                }
                                mostrarHorarioDetallado = false
                            },
                            enabled = diasSeleccionados.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(Res.string.comercio_btn_aplicar_horario))
                        }
                    } else {
                        CampoSolicitud(
                            etiqueta = stringResource(Res.string.comercio_label_horario_manual),
                            valor = horario,
                            alCambiar = { horario = it.take(200) }
                        )
                    }
                }
            }

            if (esPremium) {
                Text(stringResource(Res.string.comercio_seccion_beneficio_vip), fontWeight = FontWeight.Bold)
                CampoSolicitud(
                    etiqueta = stringResource(Res.string.comercio_label_promocion),
                    valor = promocionEspecial,
                    alCambiar = { promocionEspecial = it.take(150) }
                )
            }

            errorEnvio?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            if (solicitudEnviada) {
                Text(
                    stringResource(Res.string.comercio_solicitud_exito),
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
                            errorEnvio = errorSitioWebHttps
                        } else {
                            enviando = true
                            errorEnvio = null
                            scope.launch {
                                try {
                                    val lugar = lugarSeleccionado
                                        ?: throw IllegalStateException(
                                            getString(Res.string.comercio_error_seleccionar_ubicacion)
                                        )

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
                                            ?: throw IllegalStateException(
                                                getString(Res.string.comercio_error_seleccionar_ubicacion_exacta)
                                            )
                                    )
                                    solicitudEnviada = true
                                } catch (cancelacion: CancellationException) {
                                    throw cancelacion
                                } catch (error: Exception) {
                                    errorEnvio = error.message
                                        ?: getString(Res.string.comercio_error_enviar_solicitud)
                                } finally {
                                    enviando = false
                                }
                            }
                        }
                    }
                ) {
                    if (enviando) CircularProgressIndicator()
                    else Text(
                        stringResource(
                            if (solicitudEnviada) {
                                Res.string.comercio_btn_solicitud_enviada
                            } else {
                                Res.string.comercio_btn_enviar_solicitud
                            }
                        )
                    )
                }

                TextButton(onClick = if (solicitudEnviada) onSubmitted else onBack, enabled = !enviando) {
                    Text(
                        stringResource(
                            if (solicitudEnviada) Res.string.comercio_btn_volver_planes else Res.string.btn_cancelar
                        )
                    )
                }
            }
        }
    }
    }
}

private suspend fun mensajeErrorCarga(
    error: Exception,
    coleccion: String,
    mensajePredeterminadoRes: StringResource
): String {
    val detalle = generateSequence(error as Throwable?) { it.cause }
        .mapNotNull(Throwable::message)
        .joinToString(" ")
    return if (
        detalle.contains("PERMISSION_DENIED", ignoreCase = true) ||
        detalle.contains("missing or insufficient permissions", ignoreCase = true)
    ) {
        getString(Res.string.comercio_error_permisos_coleccion, coleccion)
    } else {
        error.message?.takeIf(String::isNotBlank) ?: getString(mensajePredeterminadoRes)
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
        } else {
            null
        }
    )
}