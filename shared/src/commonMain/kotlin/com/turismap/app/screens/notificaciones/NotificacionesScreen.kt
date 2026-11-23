package com.turismap.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.screens.notificaciones.EstadoErrorNotificaciones
import com.turismap.app.screens.notificaciones.EstadoVacioNotificaciones
import com.turismap.app.screens.notificaciones.TarjetaNotificacion
import com.turismap.app.screens.notificaciones.rememberNotificacionesPaleta
import com.turismap.app.screens.notificaciones.rememberNotificacionesState
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.notificaciones_btn_marcar_todas
import turismap.shared.generated.resources.notificaciones_cd_volver
import turismap.shared.generated.resources.notificaciones_titulo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(
    onVolver: () -> Unit,
    onVerComercio: (String) -> Unit,
    onVerPlanes: () -> Unit
) {
    val paleta = rememberNotificacionesPaleta()
    val state = rememberNotificacionesState()

    Scaffold(
        containerColor = paleta.fondoPantalla,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.notificaciones_titulo),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.notificaciones_cd_volver)
                        )
                    }
                },
                actions = {
                    if (state.cantidadNoLeidas > 0) {
                        TextButton(
                            onClick = state::marcarTodasComoLeidas,
                            enabled = !state.marcandoTodas
                        ) {
                            if (state.marcandoTodas) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = paleta.verdeAcento
                                )
                            } else {
                                Text(
                                    text = stringResource(Res.string.notificaciones_btn_marcar_todas),
                                    color = paleta.verdeAcento,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = paleta.fondoPantalla,
                    titleContentColor = paleta.textoPrincipal,
                    navigationIconContentColor = paleta.textoPrincipal
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = state.snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalDivider(
                color = paleta.bordeSuave.copy(alpha = 0.7f),
                thickness = 0.6.dp,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            when {
                state.cargando -> {
                    CircularProgressIndicator(
                        color = paleta.verdeAcento,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.errorCarga != null -> {
                    EstadoErrorNotificaciones(
                        mensajeError = state.errorCarga.orEmpty(),
                        paleta = paleta,
                        onReintentar = state::reintentarCarga
                    )
                }

                state.notificaciones.isEmpty() -> {
                    EstadoVacioNotificaciones(paleta = paleta)
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.notificaciones,
                            key = { notif -> notif.id.ifBlank { "${notif.solicitudId}_${notif.tipo}" } }
                        ) { notificacion ->
                            TarjetaNotificacion(
                                notificacion = notificacion,
                                paleta = paleta,
                                procesando = state.marcandoTodas || state.idsProcesando.contains(notificacion.id),
                                onMarcarLeida = { state.marcarComoLeida(notificacion) },
                                onVerComercio = onVerComercio,
                                onVerPlanes = onVerPlanes
                            )
                        }
                    }
                }
            }
        }
    }
}
