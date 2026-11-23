package com.turismap.app.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.screens.solicitud.*
import com.turismap.app.theme.GrisBorde
import com.turismap.app.theme.GrisTextoSecundario
import com.turismap.app.theme.TextoPrincipalOscuro
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.comercio_cargando_formulario
import turismap.shared.generated.resources.comercio_cd_seleccionar_categoria
import turismap.shared.generated.resources.comercio_cd_seleccionar_ubicacion
import turismap.shared.generated.resources.comercio_error_sin_ubicacion_para_mapa
import turismap.shared.generated.resources.comercio_error_sitio_web_https
import turismap.shared.generated.resources.comercio_error_telefono_digitos
import turismap.shared.generated.resources.comercio_label_categoria
import turismap.shared.generated.resources.comercio_label_correo
import turismap.shared.generated.resources.comercio_label_descripcion
import turismap.shared.generated.resources.comercio_label_direccion
import turismap.shared.generated.resources.comercio_label_nombre_comercio
import turismap.shared.generated.resources.comercio_label_nombre_completo
import turismap.shared.generated.resources.comercio_label_promocion
import turismap.shared.generated.resources.comercio_label_sitio_web
import turismap.shared.generated.resources.comercio_label_telefono
import turismap.shared.generated.resources.comercio_label_ubicacion_pueblo
import turismap.shared.generated.resources.comercio_seccion_beneficio_vip
import turismap.shared.generated.resources.comercio_seccion_contacto
import turismap.shared.generated.resources.comercio_seccion_info_negocio
import turismap.shared.generated.resources.comercio_seccion_ubicacion_horario
import turismap.shared.generated.resources.comercio_sheet_categoria_titulo
import turismap.shared.generated.resources.comercio_sheet_municipio_titulo
import turismap.shared.generated.resources.comercio_solicitud_exito
import turismap.shared.generated.resources.comercio_titulo_registrar
import turismap.shared.generated.resources.planes_cd_volver

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ComercioSolicitudScreen(
    nombreCompletoInicial: String,
    emailInicial: String,
    planId: String,
    onBack: () -> Unit,
    onSubmitted: () -> Unit
) {
    val state = rememberComercioSolicitudState(
        nombreCompletoInicial = nombreCompletoInicial,
        emailInicial = emailInicial,
        planId = planId
    )
    val errorSitioWebHttps = stringResource(Res.string.comercio_error_sitio_web_https)
    val errorTelefonoDigitos = stringResource(Res.string.comercio_error_telefono_digitos)
    val esModoOscuro = isSystemInDarkTheme()
    val colorAcento = if (esModoOscuro) Color(0xFF6FCF97) else Color(0xFF1B4934)
    val cargandoInicial = state.cargandoPlan || state.cargandoLugares || state.cargandoCategorias

    val lugarParaMapa = state.lugarSeleccionado
    if (state.seleccionandoUbicacion && lugarParaMapa != null) {
        SeleccionarUbicacionComercioScreen(
            coordenadaInicial = lugarParaMapa.ubicacionCoordenadas,
            coordenadaGuardada = state.coordenadaSeleccionada,
            onBack = { state.seleccionandoUbicacion = false },
            onGuardar = { coordenada ->
                state.coordenadaSeleccionada = coordenada
                state.seleccionandoUbicacion = false
            }
        )
    } else {
        LaunchedEffect(state.seleccionandoUbicacion, lugarParaMapa) {
            if (state.seleccionandoUbicacion && lugarParaMapa == null) {
                state.seleccionandoUbicacion = false
                state.errorEnvio = getString(Res.string.comercio_error_sin_ubicacion_para_mapa)
            }
        }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(stringResource(Res.string.comercio_titulo_registrar), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack, enabled = !state.enviando) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.planes_cd_volver))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = TextoPrincipalOscuro,
                        navigationIconContentColor = TextoPrincipalOscuro
                    )
                )
            },
            bottomBar = {
                if (!cargandoInicial) {
                    BarraInferiorSolicitud(
                        enviando = state.enviando,
                        solicitudEnviada = state.solicitudEnviada,
                        puedeEnviar = state.puedeEnviar,
                        onEnviar = state::enviarSolicitud,
                        onVolverPlanes = onSubmitted
                    )
                }
            }
        ) { paddingValues ->
            if (cargandoInicial) {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator(color = colorAcento)
                        Text(stringResource(Res.string.comercio_cargando_formulario), fontSize = 14.sp, color = GrisTextoSecundario)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TarjetaResumenPlan(
                        planNombre = state.planNombre,
                        planPrecio = state.planPrecio,
                        esPremium = state.esPremium,
                        esSocio = state.esSocio,
                        habilitarCambiar = !state.enviando,
                        onCambiarPlan = onBack
                    )

                    state.planError?.let { errorPlan ->
                        BannerErrorCargaSolicitud(mensaje = errorPlan, onReintentar = state::reintentarCarga)
                    }

                    // 1. Datos de contacto
                    TarjetaSeccionSolicitud(
                        titulo = stringResource(Res.string.comercio_seccion_contacto),
                        icono = Icons.Default.Person
                    ) {
                        CampoSolicitud(
                            etiqueta = stringResource(Res.string.comercio_label_nombre_completo),
                            valor = state.nombreCompleto,
                            alCambiar = { state.nombreCompleto = it.take(120) },
                            iconoInicio = Icons.Default.Person
                        )
                        CampoSolicitud(
                            etiqueta = stringResource(Res.string.comercio_label_correo),
                            valor = state.email,
                            alCambiar = {},
                            iconoInicio = Icons.Default.Email,
                            soloLectura = true
                        )
                    }

                    // 2. Información del negocio
                    TarjetaSeccionSolicitud(
                        titulo = stringResource(Res.string.comercio_seccion_info_negocio),
                        icono = Icons.Default.Storefront
                    ) {
                        CampoSolicitud(
                            etiqueta = stringResource(Res.string.comercio_label_nombre_comercio),
                            valor = state.nombreNegocio,
                            alCambiar = { state.nombreNegocio = it.take(120) },
                            iconoInicio = Icons.Default.Storefront
                        )
                        CampoSelectorSolicitud(
                            valorMostrar = state.categoria,
                            etiqueta = stringResource(Res.string.comercio_label_categoria),
                            descripcionBoton = stringResource(Res.string.comercio_cd_seleccionar_categoria),
                            iconoInicio = Icons.Default.Category,
                            habilitado = state.categoriasError == null && state.categorias.isNotEmpty(),
                            error = state.categoriasError,
                            onAbrir = { state.selectorCategoriaAbierto = true }
                        )
                        CampoSolicitud(
                            etiqueta = "${stringResource(Res.string.comercio_label_descripcion)} (${state.descripcion.length}/1800)",
                            valor = state.descripcion,
                            alCambiar = { state.descripcion = it.take(1800) },
                            iconoInicio = Icons.Default.Description,
                            multilinea = true
                        )
                        CampoSolicitud(
                            etiqueta = stringResource(Res.string.comercio_label_telefono),
                            valor = state.telefono,
                            alCambiar = { input ->
                                state.telefono = input.filter { it.isDigit() || it in "+- ()" }.take(20)
                            },
                            iconoInicio = Icons.Default.Phone,
                            keyboardType = KeyboardType.Phone,
                            esError = state.telefonoInvalido,
                            textoAyuda = if (state.telefonoInvalido) errorTelefonoDigitos else null
                        )
                        if (state.esSocio) {
                            CampoSolicitud(
                                etiqueta = stringResource(Res.string.comercio_label_sitio_web),
                                valor = state.sitioWeb,
                                alCambiar = { state.sitioWeb = it.take(200) },
                                iconoInicio = Icons.Default.Language,
                                keyboardType = KeyboardType.Uri,
                                esError = state.sitioWebInvalido,
                                textoAyuda = if (state.sitioWebInvalido) errorSitioWebHttps else null
                            )
                        }
                        if (state.esPremium) {
                            HorizontalDivider(color = GrisBorde.copy(alpha = 0.65f))
                            Text(
                                text = stringResource(Res.string.comercio_seccion_beneficio_vip),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (esModoOscuro) Color(0xFFE9C46A) else Color(0xFF9A6700)
                            )
                            CampoSolicitud(
                                etiqueta = stringResource(Res.string.comercio_label_promocion),
                                valor = state.promocionEspecial,
                                alCambiar = { state.promocionEspecial = it.take(150) },
                                iconoInicio = Icons.Default.LocalOffer
                            )
                        }
                    }

                    // 3. Ubicación y horario
                    TarjetaSeccionSolicitud(
                        titulo = stringResource(Res.string.comercio_seccion_ubicacion_horario),
                        icono = Icons.Default.LocationOn
                    ) {
                        CampoSelectorSolicitud(
                            valorMostrar = state.lugarSeleccionado?.nombre.orEmpty(),
                            etiqueta = stringResource(Res.string.comercio_label_ubicacion_pueblo),
                            descripcionBoton = stringResource(Res.string.comercio_cd_seleccionar_ubicacion),
                            iconoInicio = Icons.Default.LocationCity,
                            habilitado = state.lugaresError == null && state.lugares.isNotEmpty(),
                            error = state.lugaresError,
                            onAbrir = { state.selectorMunicipioAbierto = true }
                        )
                        state.lugarSeleccionado?.let {
                            BloqueUbicacionMapa(
                                coordenadaSeleccionada = state.coordenadaSeleccionada,
                                onAbrirMapa = { state.seleccionandoUbicacion = true }
                            )
                        }
                        CampoSolicitud(
                            etiqueta = stringResource(Res.string.comercio_label_direccion),
                            valor = state.direccion,
                            alCambiar = { state.direccion = it.take(300) },
                            iconoInicio = Icons.Default.Place
                        )
                        if (state.esSocio) {
                            HorizontalDivider(color = GrisBorde.copy(alpha = 0.65f))
                            SeccionHorarioSolicitud(
                                horario = state.horario,
                                mostrarHorarioDetallado = state.mostrarHorarioDetallado,
                                diasSeleccionados = state.diasSeleccionados,
                                horariosPorDia = state.horariosPorDia,
                                onToggleModo = { state.mostrarHorarioDetallado = !state.mostrarHorarioDetallado },
                                onHorarioManualChange = { state.horario = it.take(200) },
                                onToggleDia = { dia, checked ->
                                    state.diasSeleccionados = if (checked) state.diasSeleccionados + dia else state.diasSeleccionados - dia
                                },
                                onActualizarHorarioDia = { dia, horarioDia ->
                                    state.horariosPorDia = state.horariosPorDia + (dia to horarioDia)
                                },
                                onAplicarHorario = state::aplicarHorarioPorDias
                            )
                        }
                    }

                    state.errorEnvio?.let { BannerEstadoSolicitud(mensaje = it, esExito = false) }
                    if (state.solicitudEnviada) {
                        BannerEstadoSolicitud(mensaje = stringResource(Res.string.comercio_solicitud_exito), esExito = true)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        if (state.selectorMunicipioAbierto) {
            SelectorModalSheet(
                titulo = stringResource(Res.string.comercio_sheet_municipio_titulo),
                iconoOpcion = Icons.Default.LocationCity,
                opciones = state.lugares,
                opcionSeleccionada = { it.id == state.lugarSeleccionado?.id },
                etiquetaOpcion = { it.nombre },
                claveOpcion = { it.id },
                onDismiss = { state.selectorMunicipioAbierto = false },
                onSeleccionar = { lugar ->
                    state.lugarSeleccionado = lugar
                    state.coordenadaSeleccionada = null
                    state.selectorMunicipioAbierto = false
                    state.seleccionandoUbicacion = true
                }
            )
        }

        if (state.selectorCategoriaAbierto) {
            SelectorModalSheet(
                titulo = stringResource(Res.string.comercio_sheet_categoria_titulo),
                iconoOpcion = Icons.Default.Category,
                opciones = state.categorias,
                opcionSeleccionada = { it.id == state.categoriaId },
                etiquetaOpcion = { it.etiqueta },
                claveOpcion = { it.id },
                onDismiss = { state.selectorCategoriaAbierto = false },
                onSeleccionar = { opcion ->
                    state.categoria = opcion.etiqueta
                    state.categoriaId = opcion.id
                    state.selectorCategoriaAbierto = false
                }
            )
        }
    }
}
