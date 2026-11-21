package com.turismap.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.turismap.app.components.rememberSelectorImagen
import com.turismap.app.screens.perfil.CabeceraPerfil
import com.turismap.app.screens.perfil.DialogoEditarPerfil
import com.turismap.app.screens.perfil.DialogoIdiomaPerfil
import com.turismap.app.screens.perfil.DialogoPrivacidadPerfil
import com.turismap.app.screens.perfil.EncabezadoDatosUsuario
import com.turismap.app.screens.perfil.ModalFavoritosPerfil
import com.turismap.app.screens.perfil.SeccionConfiguracionPerfil
import com.turismap.app.screens.perfil.SeccionEstadisticasPerfil
import com.turismap.app.screens.perfil.TURISMAP_WEB_URL
import com.turismap.app.screens.perfil.TarjetaTurisMapNegocios
import com.turismap.app.screens.perfil.rememberPerfilPaleta
import com.turismap.app.screens.perfil.rememberPerfilState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    paddingValues: PaddingValues = PaddingValues(0.dp),
    onCerrarSesionClick: () -> Unit = {},
    onVerPlanesClick: () -> Unit = {},
    onComercioClick: (String) -> Unit = {},
    onLugarClick: (String) -> Unit = {}
) {
    val paleta = rememberPerfilPaleta()
    val state = rememberPerfilState()
    val uriHandler = LocalUriHandler.current

    val abrirGaleriaNativa = rememberSelectorImagen { uriLocal ->
        state.actualizarFoto(uriLocal)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(paleta.fondoApp)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            CabeceraPerfil(
                fotoPortada = state.usuarioBD?.fotoPortada,
                fotoPerfil = state.usuarioBD?.fotoPerfil,
                paleta = paleta,
                onCambiarPortadaClick = {
                    state.tipoFotoSeleccionada = "portada"
                    abrirGaleriaNativa()
                },
                onCambiarFotoPerfilClick = {
                    state.tipoFotoSeleccionada = "perfil"
                    abrirGaleriaNativa()
                },
                onEditarPerfilClick = { state.mostrarDialogoEditar = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            EncabezadoDatosUsuario(
                nombreMostrar = state.nombreMostrar,
                correoMostrar = state.correoMostrar,
                errorPerfil = state.errorPerfil,
                paleta = paleta
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Métricas
            SeccionEstadisticasPerfil(
                cantidadFavoritos = state.cantidadFavoritos,
                visitas = state.visitas,
                paleta = paleta,
                onFavoritosClick = { state.mostrarVentanaFavoritos = true }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Tarjeta TurisMap Negocios
            TarjetaTurisMapNegocios(
                paleta = paleta,
                onVerPlanesClick = onVerPlanesClick,
                onVerEnWebClick = {
                    try {
                        uriHandler.openUri(TURISMAP_WEB_URL)
                    } catch (error: Exception) {
                        state.mostrarMensaje(error.message ?: "No se pudo abrir.")
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Configuración
            SeccionConfiguracionPerfil(
                idiomaSeleccionado = state.idiomaSeleccionado,
                cerrandoSesion = state.cerrandoSesion,
                paleta = paleta,
                onIdiomaClick = { state.mostrarDialogoIdioma = true },
                onPrivacidadClick = { state.mostrarDialogoPrivacidad = true },
                onCerrarSesionClick = { state.cerrarSesion(onCerrarSesionClick) }
            )

            Spacer(modifier = Modifier.height(40.dp))
        }

        SnackbarHost(hostState = state.snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
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
            paleta = paleta,
            onDismiss = { state.mostrarVentanaFavoritos = false },
            onSeleccionarCategoria = { state.categoriaFavoritoSeleccionada = it },
            onItemClick = { item ->
                state.mostrarVentanaFavoritos = false
                if (item.esComercio) onComercioClick(item.id)
                else onLugarClick(item.id)
            },
            onQuitarFavoritoClick = { itemId -> state.quitarFavorito(itemId) }
        )
    }

    if (state.mostrarDialogoPrivacidad) {
        DialogoPrivacidadPerfil(
            perfilPublico = state.perfilPublico,
            compartirUbicacion = state.compartirUbicacion,
            paleta = paleta,
            onPerfilPublicoChange = { state.perfilPublico = it },
            onCompartirUbicacionChange = { state.compartirUbicacion = it },
            onCambiarContrasenaClick = { state.mostrarMensaje("Te enviaremos un correo.") },
            onDismiss = { state.mostrarDialogoPrivacidad = false }
        )
    }

    if (state.mostrarDialogoEditar) {
        DialogoEditarPerfil(
            nombreInicial = state.nombreMostrar,
            telefonoInicial = state.usuarioBD?.telefono.orEmpty(),
            paleta = paleta,
            onGuardarClick = { nombre, telefono -> state.guardarPerfil(nombre, telefono) },
            onDismiss = { state.mostrarDialogoEditar = false }
        )
    }

    if (state.mostrarDialogoIdioma) {
        DialogoIdiomaPerfil(
            idiomaSeleccionado = state.idiomaSeleccionado,
            paleta = paleta,
            onSeleccionarIdioma = { state.idiomaSeleccionado = it },
            onDismiss = { state.mostrarDialogoIdioma = false }
        )
    }
}
