package com.turismap.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.screens.solicitud.CampoSolicitud
import com.turismap.app.screens.solicitud.SeccionHorarioSolicitud
import com.turismap.app.screens.solicitud.SelectorDropdownSolicitud
import com.turismap.app.screens.solicitud.TarjetaResumenPlan
import com.turismap.app.screens.solicitud.rememberComercioSolicitudState
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.btn_cancelar
import turismap.shared.generated.resources.comercio_btn_cambiar_ubicacion_mapa
import turismap.shared.generated.resources.comercio_btn_enviar_solicitud
import turismap.shared.generated.resources.comercio_btn_solicitud_enviada
import turismap.shared.generated.resources.comercio_btn_ubicar_mapa
import turismap.shared.generated.resources.comercio_btn_volver_planes
import turismap.shared.generated.resources.comercio_cd_seleccionar_categoria
import turismap.shared.generated.resources.comercio_cd_seleccionar_ubicacion
import turismap.shared.generated.resources.comercio_coordenadas_guardadas
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
import turismap.shared.generated.resources.comercio_seccion_datos_comercio
import turismap.shared.generated.resources.comercio_seccion_operacion_redes
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
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.comercio_titulo_registrar)) },
                    navigationIcon = {
                        IconButton(onClick = onBack, enabled = !state.enviando) {
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
                TarjetaResumenPlan(
                    planNombre = state.planNombre,
                    planPrecio = state.planPrecio,
                    esPremium = state.esPremium,
                    esSocio = state.esSocio,
                    cargandoPlan = state.cargandoPlan,
                    planError = state.planError,
                    onReintentar = state::reintentarCarga
                )

                Text(stringResource(Res.string.comercio_seccion_contacto), fontWeight = FontWeight.Bold)
                CampoSolicitud(
                    etiqueta = stringResource(Res.string.comercio_label_nombre_completo),
                    valor = state.nombreCompleto,
                    alCambiar = { state.nombreCompleto = it.take(120) }
                )
                CampoSolicitud(
                    etiqueta = stringResource(Res.string.comercio_label_correo),
                    valor = state.email,
                    alCambiar = {},
                    soloLectura = true
                )

                Text(stringResource(Res.string.comercio_seccion_datos_comercio), fontWeight = FontWeight.Bold)
                CampoSolicitud(
                    etiqueta = stringResource(Res.string.comercio_label_nombre_comercio),
                    valor = state.nombreNegocio,
                    alCambiar = { state.nombreNegocio = it.take(120) }
                )

                SelectorDropdownSolicitud(
                    valorMostrar = state.lugarSeleccionado?.nombre.orEmpty(),
                    etiqueta = stringResource(Res.string.comercio_label_ubicacion_pueblo),
                    descripcionBoton = stringResource(Res.string.comercio_cd_seleccionar_ubicacion),
                    expandido = state.selectorMunicipioAbierto,
                    cargando = state.cargandoLugares,
                    error = state.lugaresError,
                    opciones = state.lugares,
                    etiquetaOpcion = { it.nombre },
                    onAbrir = { state.selectorMunicipioAbierto = true },
                    onCerrar = { state.selectorMunicipioAbierto = false },
                    onSeleccionar = { lugar ->
                        state.lugarSeleccionado = lugar
                        state.coordenadaSeleccionada = null
                        state.selectorMunicipioAbierto = false
                        state.seleccionandoUbicacion = true
                    }
                )

                state.lugarSeleccionado?.let {
                    Button(
                        onClick = { state.seleccionandoUbicacion = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            stringResource(
                                if (state.coordenadaSeleccionada == null) {
                                    Res.string.comercio_btn_ubicar_mapa
                                } else {
                                    Res.string.comercio_btn_cambiar_ubicacion_mapa
                                }
                            )
                        )
                    }
                    state.coordenadaSeleccionada?.let { coordenada ->
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

                SelectorDropdownSolicitud(
                    valorMostrar = state.categoria,
                    etiqueta = stringResource(Res.string.comercio_label_categoria),
                    descripcionBoton = stringResource(Res.string.comercio_cd_seleccionar_categoria),
                    expandido = state.selectorCategoriaAbierto,
                    cargando = state.cargandoCategorias,
                    error = state.categoriasError,
                    opciones = state.categorias,
                    etiquetaOpcion = { it.etiqueta },
                    onAbrir = { state.selectorCategoriaAbierto = true },
                    onCerrar = { state.selectorCategoriaAbierto = false },
                    onSeleccionar = { opcion ->
                        state.categoria = opcion.etiqueta
                        state.categoriaId = opcion.id
                        state.selectorCategoriaAbierto = false
                    }
                )

                CampoSolicitud(
                    etiqueta = "${stringResource(Res.string.comercio_label_descripcion)} (${state.descripcion.length}/1800)",
                    valor = state.descripcion,
                    alCambiar = { state.descripcion = it.take(1800) },
                    multilinea = true
                )
                CampoSolicitud(
                    etiqueta = stringResource(Res.string.comercio_label_direccion),
                    valor = state.direccion,
                    alCambiar = { state.direccion = it.take(300) }
                )
                CampoSolicitud(
                    etiqueta = stringResource(Res.string.comercio_label_telefono),
                    valor = state.telefono,
                    alCambiar = { input ->
                        state.telefono = input.filter { it.isDigit() || it in "+- ()" }.take(20)
                    },
                    keyboardType = KeyboardType.Phone,
                    esError = state.telefonoInvalido,
                    textoAyuda = if (state.telefonoInvalido) errorTelefonoDigitos else null
                )

                if (state.esSocio) {
                    Text(stringResource(Res.string.comercio_seccion_operacion_redes), fontWeight = FontWeight.Bold)
                    CampoSolicitud(
                        etiqueta = stringResource(Res.string.comercio_label_sitio_web),
                        valor = state.sitioWeb,
                        alCambiar = { state.sitioWeb = it.take(200) },
                        keyboardType = KeyboardType.Uri,
                        esError = state.sitioWebInvalido,
                        textoAyuda = if (state.sitioWebInvalido) errorSitioWebHttps else null
                    )

                    SeccionHorarioSolicitud(
                        horario = state.horario,
                        mostrarHorarioDetallado = state.mostrarHorarioDetallado,
                        diasSeleccionados = state.diasSeleccionados,
                        horariosPorDia = state.horariosPorDia,
                        onToggleModo = { state.mostrarHorarioDetallado = !state.mostrarHorarioDetallado },
                        onHorarioManualChange = { state.horario = it.take(200) },
                        onToggleDia = { dia, checked ->
                            state.diasSeleccionados = if (checked) {
                                state.diasSeleccionados + dia
                            } else {
                                state.diasSeleccionados - dia
                            }
                        },
                        onActualizarHorarioDia = { dia, horarioDia ->
                            state.horariosPorDia = state.horariosPorDia + (dia to horarioDia)
                        },
                        onAplicarHorario = state::aplicarHorarioPorDias
                    )
                }

                if (state.esPremium) {
                    Text(stringResource(Res.string.comercio_seccion_beneficio_vip), fontWeight = FontWeight.Bold)
                    CampoSolicitud(
                        etiqueta = stringResource(Res.string.comercio_label_promocion),
                        valor = state.promocionEspecial,
                        alCambiar = { state.promocionEspecial = it.take(150) }
                    )
                }

                state.errorEnvio?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                if (state.solicitudEnviada) {
                    Text(
                        stringResource(Res.string.comercio_solicitud_exito),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        enabled = state.puedeEnviar,
                        onClick = state::enviarSolicitud
                    ) {
                        if (state.enviando) {
                            CircularProgressIndicator()
                        } else {
                            Text(
                                stringResource(
                                    if (state.solicitudEnviada) {
                                        Res.string.comercio_btn_solicitud_enviada
                                    } else {
                                        Res.string.comercio_btn_enviar_solicitud
                                    }
                                )
                            )
                        }
                    }

                    TextButton(
                        onClick = if (state.solicitudEnviada) onSubmitted else onBack,
                        enabled = !state.enviando
                    ) {
                        Text(
                            stringResource(
                                if (state.solicitudEnviada) Res.string.comercio_btn_volver_planes else Res.string.btn_cancelar
                            )
                        )
                    }
                }
            }
        }
    }
}
