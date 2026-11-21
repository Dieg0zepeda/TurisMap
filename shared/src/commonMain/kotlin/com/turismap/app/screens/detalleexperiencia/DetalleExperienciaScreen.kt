package com.turismap.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.turismap.app.screens.detalleexperiencia.BarraInferiorExperiencia
import com.turismap.app.screens.detalleexperiencia.CabeceraMultimediaExperiencia
import com.turismap.app.screens.detalleexperiencia.DetalleExperienciaDatosUi
import com.turismap.app.screens.detalleexperiencia.EncabezadoComercio
import com.turismap.app.screens.detalleexperiencia.FilaHorarioComercio
import com.turismap.app.screens.detalleexperiencia.GaleriaMiniaturas
import com.turismap.app.screens.detalleexperiencia.MenuDigitalDialog
import com.turismap.app.screens.detalleexperiencia.SeccionDescripcionYServicios
import com.turismap.app.screens.detalleexperiencia.SeccionEnlacesDirectos
import com.turismap.app.screens.detalleexperiencia.SeccionResenasExperiencia
import com.turismap.app.screens.detalleexperiencia.TarjetaExperienciasPremium
import com.turismap.app.screens.detalleexperiencia.TarjetaUbicacionComercio
import com.turismap.app.screens.detalleexperiencia.rememberDetalleExperienciaPaleta
import com.turismap.app.screens.detalleexperiencia.rememberDetalleExperienciaState

@Composable
fun DetalleExperienciaScreen(comercioId: String, onVolver: () -> Unit) {
    val paleta = rememberDetalleExperienciaPaleta(isSystemInDarkTheme())
    val (state, esFavorito) = rememberDetalleExperienciaState(comercioId)
    val uriHandler = LocalUriHandler.current

    if (state.mostrarModalMenu && state.comercio != null) {
        MenuDigitalDialog(
            nombreComercio = state.comercio!!.nombre,
            paleta = paleta,
            onDismiss = { state.mostrarModalMenu = false }
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(paleta.colorFondoPantalla)
            .imePadding()
    ) {
        when {
            state.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = paleta.verdeTurisMap)
            }
            state.error != null -> Column(
                Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(state.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = state::reintentar,
                    colors = ButtonDefaults.buttonColors(containerColor = paleta.verdeTurisMap)
                ) {
                    Text("Reintentar", color = Color.White)
                }
            }
            state.comercio != null -> {
                val datosUi = DetalleExperienciaDatosUi.desde(
                    datos = state.comercio!!,
                    resenas = state.resenas,
                    usuarioActualUid = state.usuarioActualUid
                )

                Box(Modifier.fillMaxSize()) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 90.dp)
                    ) {
                        // 1. Cabecera Multimedia con Carrusel y Puntos Instagram
                        CabeceraMultimediaExperiencia(
                            datosUi = datosUi,
                            esFavorito = esFavorito,
                            paleta = paleta,
                            onVolver = onVolver,
                            onToggleFavorito = { state.toggleFavorito(esFavorito) }
                        )

                        // 2. Contenido del Comercio
                        Column(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                            EncabezadoComercio(datosUi = datosUi, paleta = paleta)

                            // 3. Módulo Exclusivo Premium: Experiencias Destacadas
                            if (datosUi.esPremium) {
                                TarjetaExperienciasPremium(
                                    paleta = paleta,
                                    onAbrirCatalogo = { state.mostrarModalMenu = true }
                                )
                            }

                            // 4. Ubicación
                            TarjetaUbicacionComercio(
                                datosUi = datosUi,
                                paleta = paleta,
                                onAbrirMapa = { uriHandler.openUri(datosUi.mapsUrl) }
                            )

                            // 5. Canales de Contacto Directo (Socio y Premium)
                            if (datosUi.esSocio || datosUi.esPremium) {
                                SeccionEnlacesDirectos(
                                    datosUi = datosUi,
                                    paleta = paleta,
                                    onAbrirWhatsapp = {
                                        if (datosUi.telefonoLimpio.isNotBlank()) {
                                            uriHandler.openUri(datosUi.whatsappUrl)
                                        }
                                    },
                                    onAbrirSitioWeb = {
                                        datosUi.sitioWebUrl?.let(uriHandler::openUri)
                                    }
                                )
                            }

                            // 6 y 7. Descripción y Servicios
                            SeccionDescripcionYServicios(datosUi = datosUi, paleta = paleta)

                            // 8. Horario
                            FilaHorarioComercio(datosUi = datosUi, paleta = paleta)

                            // 9. Galería inferior de miniaturas
                            if (datosUi.fotos.size > 1) {
                                GaleriaMiniaturas(fotos = datosUi.fotos, paleta = paleta)
                            }

                            // 10. Reseñas reales
                            SeccionResenasExperiencia(
                                resenas = state.resenas,
                                tieneUsuarioAutenticado = state.tieneUsuarioAutenticado,
                                esPropietario = datosUi.esPropietario,
                                calificacion = state.calificacion,
                                comentario = state.comentario,
                                enviando = state.enviando,
                                mensaje = state.mensaje,
                                paleta = paleta,
                                onCalificacionChange = { state.calificacion = it },
                                onComentarioChange = state::actualizarComentario,
                                onPublicarResena = { state.publicarResena(datosUi.comercio.id) }
                            )
                        }
                    }

                    // 11. Barra Inferior Fija
                    BarraInferiorExperiencia(
                        datosUi = datosUi,
                        paleta = paleta,
                        onLlamar = { uriHandler.openUri(datosUi.telefonoUrl) },
                        onAbrirWhatsapp = { uriHandler.openUri(datosUi.whatsappUrl) },
                        onIniciarRuta = { uriHandler.openUri(datosUi.mapsUrl) },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}
