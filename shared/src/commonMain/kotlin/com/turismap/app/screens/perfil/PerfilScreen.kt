package com.turismap.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.turismap.app.components.rememberSelectorImagen
import com.turismap.app.screens.perfil.ALTURA_BARRA_COMPACTA_PERFIL
import com.turismap.app.screens.perfil.ALTURA_ESPACIADOR_PERFIL
import com.turismap.app.screens.perfil.BarraSuperiorCompactaPerfil
import com.turismap.app.screens.perfil.ContenidoCabeceraPerfilParalaje
import com.turismap.app.screens.perfil.DialogoEditarPerfil
import com.turismap.app.screens.perfil.FondoCabeceraPerfilParalaje
import com.turismap.app.screens.perfil.ModalFavoritosPerfil
import com.turismap.app.screens.perfil.SeccionConfiguracionPerfil
import com.turismap.app.screens.perfil.SeccionEstadisticasPerfil
import com.turismap.app.screens.perfil.TarjetaTurisMapNegocios
import com.turismap.app.screens.perfil.UMBRAL_BARRA_COMPACTA_PERFIL
import com.turismap.app.screens.perfil.rememberPerfilPaleta
import com.turismap.app.screens.perfil.rememberPerfilState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    paddingValues: PaddingValues = PaddingValues(0.dp),
    cantidadNotificacionesNoLeidas: Int = 0,
    onAbrirNotificaciones: () -> Unit = {},
    onCerrarSesionClick: () -> Unit = {},
    onVerPlanesClick: () -> Unit = {},
    onComercioClick: (String) -> Unit = {},
    onLugarClick: (String) -> Unit = {}
) {
    val paleta = rememberPerfilPaleta()
    val state = rememberPerfilState()
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    val distanciaColapsoPx = remember(density) {
        with(density) { (ALTURA_ESPACIADOR_PERFIL - ALTURA_BARRA_COMPACTA_PERFIL).toPx() }
    }

    val collapseProgressState = remember(distanciaColapsoPx) {
        derivedStateOf {
            if (distanciaColapsoPx > 0f) {
                (scrollState.value / distanciaColapsoPx).coerceIn(0f, 1f)
            } else {
                0f
            }
        }
    }

    val mostrarBarraCompacta by remember(collapseProgressState) {
        derivedStateOf { collapseProgressState.value >= UMBRAL_BARRA_COMPACTA_PERFIL }
    }

    val nombreMostrar = state.obtenerNombreMostrar()
    val correoMostrar = state.obtenerCorreoMostrar()

    val abrirGaleriaNativa = rememberSelectorImagen { uriLocal ->
        state.actualizarFoto(uriLocal)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(paleta.fondoApp)
            .padding(paddingValues)
    ) {
        val alturaMinimaCortina = (maxHeight - ALTURA_BARRA_COMPACTA_PERFIL).coerceAtLeast(0.dp)

        // Capa 1 (Fondo): banner de portada con desplazamiento en paralaje en GPU
        FondoCabeceraPerfilParalaje(
            fotoPortada = state.usuarioBD?.fotoPortada,
            paleta = paleta,
            scrollOffsetProvider = { scrollState.value.toFloat() },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Capa 2 (Frontal): scroll vertical con cabecera interactiva + cortina superpuesta
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            ContenidoCabeceraPerfilParalaje(
                fotoPerfil = state.usuarioBD?.fotoPerfil,
                nombreMostrar = nombreMostrar,
                correoMostrar = correoMostrar,
                errorPerfil = state.errorPerfil,
                cantidadNotificacionesNoLeidas = cantidadNotificacionesNoLeidas,
                paleta = paleta,
                scrollOffsetProvider = { scrollState.value.toFloat() },
                collapseProgressProvider = { collapseProgressState.value },
                onAbrirNotificacionesClick = onAbrirNotificaciones,
                onCambiarPortadaClick = {
                    state.tipoFotoSeleccionada = "portada"
                    abrirGaleriaNativa()
                },
                onCambiarFotoPerfilClick = {
                    state.tipoFotoSeleccionada = "perfil"
                    abrirGaleriaNativa()
                },
                onEditarPerfilClick = { state.mostrarDialogoEditar = true },
                modifier = Modifier.zIndex(0f)
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = alturaMinimaCortina)
                    .zIndex(1f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = paleta.fondoApp,
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                ) {
                    // Métricas
                    SeccionEstadisticasPerfil(
                        cantidadFavoritos = state.cantidadFavoritos,
                        visitas = state.visitas,
                        paleta = paleta,
                        onFavoritosClick = { state.mostrarVentanaFavoritos = true }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Tarjeta TurisMap Negocios
                    TarjetaTurisMapNegocios(
                        paleta = paleta,
                        onVerPlanesClick = onVerPlanesClick
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Cuenta y seguridad + cierre de sesión
                    SeccionConfiguracionPerfil(
                        enviandoRecuperacion = state.enviandoRecuperacion,
                        cerrandoSesion = state.cerrandoSesion,
                        paleta = paleta,
                        onCambiarContrasenaClick = { state.enviarRestablecimientoContrasena() },
                        onCerrarSesionClick = { state.cerrarSesion(onCerrarSesionClick) }
                    )

                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }

        // Capa 3 (Superior fija): barra compacta sticky con fade-in al superar ~70% de colapso
        BarraSuperiorCompactaPerfil(
            visible = mostrarBarraCompacta,
            fotoPerfil = state.usuarioBD?.fotoPerfil,
            nombreMostrar = nombreMostrar,
            cantidadNotificacionesNoLeidas = cantidadNotificacionesNoLeidas,
            paleta = paleta,
            onAbrirNotificacionesClick = onAbrirNotificaciones,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        SnackbarHost(
            hostState = state.snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Modal Bottom Sheet: Mis Favoritos con Pestañas / Chips
    if (state.mostrarVentanaFavoritos) {
        ModalFavoritosPerfil(
            totalFavoritos = state.listaFavoritosDetallados.size,
            cantDestinos = state.cantDestinos,
            cantComercios = state.cantComercios,
            categoriaSeleccionada = state.categoriaFavoritoSeleccionada,
            cargandoFavoritos = state.cargandoFavoritos,
            favoritosFiltrados = state.favoritosFiltrados,
            favoritosEnProceso = state.favoritosEnProceso,
            paleta = paleta,
            onDismiss = { state.mostrarVentanaFavoritos = false },
            onSeleccionarCategoria = { state.categoriaFavoritoSeleccionada = it },
            onItemClick = { item ->
                state.mostrarVentanaFavoritos = false
                if (item.esComercio) onComercioClick(item.id)
                else onLugarClick(item.id)
            },
            onQuitarFavoritoClick = { item -> state.quitarFavorito(item) }
        )
    }

    if (state.mostrarDialogoEditar) {
        DialogoEditarPerfil(
            nombreInicial = state.usuarioBD?.nombre?.takeIf { it.isNotBlank() } ?: nombreMostrar,
            guardandoPerfil = state.guardandoPerfil,
            paleta = paleta,
            onGuardarClick = { nombre -> state.guardarPerfil(nombre) },
            onDismiss = { if (!state.guardandoPerfil) state.mostrarDialogoEditar = false }
        )
    }
}
