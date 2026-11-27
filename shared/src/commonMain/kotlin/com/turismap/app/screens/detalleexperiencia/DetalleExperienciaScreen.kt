package com.turismap.app.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.turismap.app.screens.detalleexperiencia.BarraInferiorExperiencia
import com.turismap.app.screens.detalleexperiencia.CabeceraMultimediaExperiencia
import com.turismap.app.screens.detalleexperiencia.DetalleExperienciaDatosUi
import com.turismap.app.screens.detalleexperiencia.EncabezadoComercio
import com.turismap.app.screens.detalleexperiencia.FilaAccionesRapidasComercio
import com.turismap.app.screens.detalleexperiencia.FranjaSocioDestacado
import com.turismap.app.screens.detalleexperiencia.GaleriaMiniaturas
import com.turismap.app.screens.detalleexperiencia.SeccionResenasExperiencia
import com.turismap.app.screens.detalleexperiencia.TarjetaAcercaYHorario
import com.turismap.app.screens.detalleexperiencia.TarjetaUbicacionComercio
import com.turismap.app.screens.detalleexperiencia.rememberDetalleExperienciaPaleta
import com.turismap.app.screens.detalleexperiencia.rememberDetalleExperienciaState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.btn_reintentar

@Composable
fun DetalleExperienciaScreen(
    comercioId: String,
    onVolver: () -> Unit,
    onVerEnMapa: ((lat: Double, lng: Double, zoom: Float) -> Unit)? = null
) {
    val paleta = rememberDetalleExperienciaPaleta()
    val (state, esFavorito) = rememberDetalleExperienciaState(comercioId)
    val uriHandler = LocalUriHandler.current
    val uiScope = rememberCoroutineScope()

    Column(
        Modifier
            .fillMaxSize()
            .background(paleta.fondoPantalla)
            .imePadding()
    ) {
        when {
            state.cargando && state.comercio == null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(paleta.fondoPantalla),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = paleta.primario)
            }
            state.error != null && state.comercio == null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(paleta.fondoPantalla)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(state.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = state::reintentar,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, paleta.primario),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = paleta.contenedorPrimario,
                        contentColor = paleta.sobreContenedorPrimario
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(stringResource(Res.string.btn_reintentar))
                }
            }
            state.comercio != null -> {
                val comercioActual = state.comercio!!
                val datosUi = remember(
                    comercioActual,
                    state.resenas,
                    state.usuarioActualUid,
                    state.categoriasMap,
                    state.municipiosMap
                ) {
                    DetalleExperienciaDatosUi.desde(
                        datos = comercioActual,
                        resenas = state.resenas,
                        usuarioActualUid = state.usuarioActualUid,
                        categoriasMap = state.categoriasMap,
                        municipiosMap = state.municipiosMap
                    )
                }
                val pagerState = rememberPagerState(
                    pageCount = { datosUi.fotos.size.coerceAtLeast(1) }
                )

                Box(Modifier.fillMaxSize()) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 96.dp)
                    ) {
                        // 1. Hero fotográfico limpio (solo Atrás, Favorito y contador 1 / N)
                        CabeceraMultimediaExperiencia(
                            datosUi = datosUi,
                            pagerState = pagerState,
                            esFavorito = esFavorito,
                            favoritoEnProceso = comercioId in state.favoritosEnProceso,
                            paleta = paleta,
                            onVolver = onVolver,
                            onToggleFavorito = { state.toggleFavorito(esFavorito) }
                        )

                        Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                            // 2. Encabezado limpio (badge sobrio + categoría, título, metadatos)
                            EncabezadoComercio(datosUi = datosUi, paleta = paleta)

                            // 3. Fila única de acciones rápidas (WhatsApp, Llamar, Web / Redes)
                            FilaAccionesRapidasComercio(
                                datosUi = datosUi,
                                paleta = paleta,
                                onAbrirWhatsapp = {
                                    if (datosUi.mostrarWhatsapp) {
                                        state.abrirUriSeguro(uriHandler, datosUi.whatsappUrl)
                                    }
                                },
                                onLlamar = {
                                    if (datosUi.mostrarLlamar) {
                                        state.abrirUriSeguro(uriHandler, datosUi.telefonoUrl)
                                    }
                                },
                                onAbrirSitioWeb = {
                                    if (datosUi.mostrarSitioWeb) {
                                        datosUi.sitioWebUrl?.let { url ->
                                            state.abrirUriSeguro(uriHandler, url)
                                        }
                                    }
                                }
                            )

                            // 4. Tarjeta unificada "Acerca de este lugar" + Servicios + Horario
                            TarjetaAcercaYHorario(datosUi = datosUi, paleta = paleta)

                            // 5. Distintivo discreto de socio destacado (solo planes superiores)
                            if (datosUi.esPremium) {
                                FranjaSocioDestacado(paleta = paleta)
                            }

                            // 6. Ubicación en la ruta + Ver en Mapa (secundario)
                            TarjetaUbicacionComercio(
                                datosUi = datosUi,
                                paleta = paleta,
                                onVerEnMapa = {
                                    val coord = datosUi.coordenadaMapa
                                    if (onVerEnMapa != null && coord != null) {
                                        onVerEnMapa(coord.latitud, coord.longitud, datosUi.zoomMapa)
                                    } else {
                                        state.abrirUriSeguro(uriHandler, datosUi.mapsUrl)
                                    }
                                }
                            )

                            // 7. Galería de fotos condicional sincronizada con el hero
                            if (datosUi.fotos.size > 1) {
                                GaleriaMiniaturas(
                                    fotos = datosUi.fotos,
                                    indiceActivo = pagerState.currentPage,
                                    paleta = paleta,
                                    onSeleccionarFoto = { indice ->
                                        if (indice in datosUi.fotos.indices) {
                                            uiScope.launch {
                                                pagerState.animateScrollToPage(indice)
                                            }
                                        }
                                    }
                                )
                            }

                            // 8. Reseñas de visitantes
                            SeccionResenasExperiencia(
                                resenas = state.resenas,
                                cargandoResenas = state.cargandoResenas,
                                errorResenas = state.errorResenas,
                                tieneUsuarioAutenticado = state.tieneUsuarioAutenticado,
                                esPropietario = datosUi.esPropietario,
                                tieneResenaPropia = state.tieneResenaPropia,
                                calificacion = state.calificacion,
                                comentario = state.comentario,
                                enviando = state.enviando,
                                mensaje = state.mensaje,
                                resenaGuardadaExito = state.resenaGuardadaExito,
                                paleta = paleta,
                                onCalificacionChange = state::actualizarCalificacion,
                                onComentarioChange = state::actualizarComentario,
                                onPublicarResena = { state.publicarResena(datosUi.comercio.id) },
                                onReintentarResenas = state::reintentarResenas
                            )
                        }
                    }

                    // 9. Barra inferior fija con el único CTA primario sólido (Iniciar ruta)
                    BarraInferiorExperiencia(
                        datosUi = datosUi,
                        paleta = paleta,
                        onIniciarRuta = { state.abrirUriSeguro(uriHandler, datosUi.mapsUrl) },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )

                    SnackbarHost(
                        hostState = state.snackbarHostState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 84.dp)
                    )
                }
            }
        }
    }
}
