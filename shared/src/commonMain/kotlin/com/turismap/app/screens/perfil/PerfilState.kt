package com.turismap.app.screens.perfil

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.turismap.app.data.repository.AutenticacionRepository
import com.turismap.app.data.repository.ComercioRepository
import com.turismap.app.data.repository.LugaresRepository
import com.turismap.app.data.repository.UsuarioBD
import com.turismap.app.data.repository.UsuarioRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.perfil_actualizado
import turismap.shared.generated.resources.perfil_cargando
import turismap.shared.generated.resources.perfil_correo_no_disponible
import turismap.shared.generated.resources.perfil_error_actualizar
import turismap.shared.generated.resources.perfil_error_cargar
import turismap.shared.generated.resources.perfil_error_cargar_favoritos
import turismap.shared.generated.resources.perfil_error_cargar_favoritos_generico
import turismap.shared.generated.resources.perfil_error_con_detalle
import turismap.shared.generated.resources.perfil_error_generico
import turismap.shared.generated.resources.perfil_error_sin_sesion
import turismap.shared.generated.resources.perfil_favorito_eliminado
import turismap.shared.generated.resources.perfil_foto_actualizada
import turismap.shared.generated.resources.perfil_usuario_predeterminado

internal const val TURISMAP_WEB_URL = "https://urldeprueba.com"

internal enum class CategoriaFavorito {
    TODOS,
    DESTINOS,
    COMERCIOS
}

internal data class ItemFavoritoUi(
    val id: String,
    val nombre: String,
    val categoria: String = "",
    val categoriaRes: StringResource? = null,
    val imagenUrl: String,
    val calificacion: Double,
    val esComercio: Boolean
)

internal data class PerfilPaleta(
    val verdeEsmeralda: Color = Color(0xFF1B4934),
    val terracota: Color = Color(0xFFD96B52),
    val fondoApp: Color,
    val fondoTarjeta: Color,
    val colorTextoPrincipal: Color,
    val colorTextoSecundario: Color,
    val colorDivisor: Color
)

@Composable
internal fun rememberPerfilPaleta(): PerfilPaleta {
    val colorScheme = MaterialTheme.colorScheme
    return remember(colorScheme) {
        PerfilPaleta(
            fondoApp = colorScheme.background,
            fondoTarjeta = colorScheme.surface,
            colorTextoPrincipal = colorScheme.onBackground,
            colorTextoSecundario = colorScheme.onSurfaceVariant,
            colorDivisor = colorScheme.outlineVariant
        )
    }
}

internal class PerfilState(
    private val coroutineScope: CoroutineScope,
    val snackbarHostState: SnackbarHostState
) {
    var usuarioBD by mutableStateOf<UsuarioBD?>(null)
    var estaCargando by mutableStateOf(true)
    var visitas by mutableStateOf(0)
    var errorPerfil by mutableStateOf<String?>(null)

    // Control del modal de favoritos y filtro por categoría
    var mostrarVentanaFavoritos by mutableStateOf(false)
    var cargandoFavoritos by mutableStateOf(false)
    var listaFavoritosDetallados by mutableStateOf<List<ItemFavoritoUi>>(emptyList())
    var categoriaFavoritoSeleccionada by mutableStateOf(CategoriaFavorito.TODOS)

    var perfilPublico by mutableStateOf(true)
    var compartirUbicacion by mutableStateOf(false)

    var mostrarDialogoEditar by mutableStateOf(false)
    var mostrarDialogoPrivacidad by mutableStateOf(false)
    var cerrandoSesion by mutableStateOf(false)
    var tipoFotoSeleccionada by mutableStateOf("perfil")

    val cantDestinos: Int by derivedStateOf {
        listaFavoritosDetallados.count { !it.esComercio }
    }

    val cantComercios: Int by derivedStateOf {
        listaFavoritosDetallados.count { it.esComercio }
    }

    val favoritosFiltrados: List<ItemFavoritoUi> by derivedStateOf {
        when (categoriaFavoritoSeleccionada) {
            CategoriaFavorito.DESTINOS -> listaFavoritosDetallados.filter { !it.esComercio }
            CategoriaFavorito.COMERCIOS -> listaFavoritosDetallados.filter { it.esComercio }
            CategoriaFavorito.TODOS -> listaFavoritosDetallados
        }
    }

    @Composable
    fun obtenerNombreMostrar(): String =
        usuarioBD?.nombre?.takeIf { it.isNotBlank() }
            ?: if (estaCargando) {
                stringResource(Res.string.perfil_cargando)
            } else {
                stringResource(Res.string.perfil_usuario_predeterminado)
            }

    @Composable
    fun obtenerCorreoMostrar(): String =
        usuarioBD?.email?.takeIf { it.isNotBlank() }
            ?: AutenticacionRepository.usuarioActual?.email?.takeIf { it.isNotBlank() }
            ?: if (estaCargando) {
                stringResource(Res.string.perfil_cargando)
            } else {
                stringResource(Res.string.perfil_correo_no_disponible)
            }

    val cantidadFavoritos: String
        get() = (usuarioBD?.favoritos?.size ?: 0).toString()

    fun actualizarFoto(uriLocal: String) {
        coroutineScope.launch {
            try {
                UsuarioRepository.actualizarFoto(tipoFotoSeleccionada == "perfil", uriLocal)
                snackbarHostState.showSnackbar(getString(Res.string.perfil_foto_actualizada))
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                val detalle = error.message?.takeIf { it.isNotBlank() }
                val mensaje = if (detalle != null) {
                    getString(Res.string.perfil_error_con_detalle, detalle)
                } else {
                    getString(Res.string.perfil_error_generico)
                }
                snackbarHostState.showSnackbar(mensaje)
            }
        }
    }

    fun quitarFavorito(itemId: String) {
        coroutineScope.launch {
            try {
                UsuarioRepository.cambiarFavorito(itemId, false)
                snackbarHostState.showSnackbar(getString(Res.string.perfil_favorito_eliminado))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(e.message ?: getString(Res.string.perfil_error_actualizar))
            }
        }
    }

    fun guardarPerfil(nombre: String, telefono: String) {
        coroutineScope.launch {
            try {
                UsuarioRepository.actualizarPerfil(nombre.trim(), telefono.trim())
                mostrarDialogoEditar = false
                snackbarHostState.showSnackbar(getString(Res.string.perfil_actualizado))
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                snackbarHostState.showSnackbar(error.message ?: getString(Res.string.perfil_error_actualizar))
            }
        }
    }

    fun cerrarSesion(onCerrarSesionClick: () -> Unit) {
        if (!cerrandoSesion) {
            cerrandoSesion = true
            coroutineScope.launch {
                try {
                    AutenticacionRepository.cerrarSesion()
                    onCerrarSesionClick()
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    snackbarHostState.showSnackbar(error.message ?: getString(Res.string.perfil_error_generico))
                } finally {
                    cerrandoSesion = false
                }
            }
        }
    }

    fun mostrarMensaje(mensaje: String) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(mensaje)
        }
    }

    fun mostrarMensaje(recurso: StringResource) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(getString(recurso))
        }
    }

    fun mostrarMensajeOError(mensaje: String?, fallback: StringResource) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(mensaje ?: getString(fallback))
        }
    }
}

@Composable
internal fun rememberPerfilState(): PerfilState {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val state = remember(coroutineScope, snackbarHostState) {
        PerfilState(coroutineScope = coroutineScope, snackbarHostState = snackbarHostState)
    }

    val idUsuarioActual = UsuarioRepository.obtenerIdUsuarioActual()

    // 1. Observación reactiva en tiempo real del usuario desde Firestore
    val usuarioBD by UsuarioRepository.observarUsuarioActual().collectAsState(initial = null)
    state.usuarioBD = usuarioBD

    LaunchedEffect(idUsuarioActual) {
        state.estaCargando = true
        state.errorPerfil = null
        try {
            if (idUsuarioActual == null) {
                state.errorPerfil = getString(Res.string.perfil_error_sin_sesion)
            } else {
                UsuarioRepository.asegurarPerfil(
                    email = AutenticacionRepository.usuarioActual?.email.orEmpty(),
                    nombre = AutenticacionRepository.usuarioActual?.displayName.orEmpty()
                )
                state.visitas = UsuarioRepository.contarVisitas(idUsuarioActual)
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            state.errorPerfil = error.message ?: getString(Res.string.perfil_error_cargar)
        } finally {
            state.estaCargando = false
        }
    }

    // Carga y mapeo reactivo de favoritos distinguiendo Lugares (Destinos) y Comercios
    LaunchedEffect(state.mostrarVentanaFavoritos, usuarioBD?.favoritos) {
        if (state.mostrarVentanaFavoritos) {
            state.cargandoFavoritos = true
            try {
                val favoritosIds = usuarioBD?.favoritos ?: emptyList()
                if (favoritosIds.isEmpty()) {
                    state.listaFavoritosDetallados = emptyList()
                } else {
                    val todosLugares = try {
                        LugaresRepository.obtenerTodos().associateBy { it.id.trim() }
                    } catch (_: Exception) {
                        emptyMap()
                    }
                    val todosComercios = try {
                        ComercioRepository.obtenerPublicados().associateBy { it.id.trim() }
                    } catch (_: Exception) {
                        emptyMap()
                    }

                    val resueltos = favoritosIds.mapNotNull { idOriginal ->
                        val idLimpio = idOriginal.trim()
                        val lugar = todosLugares[idLimpio]
                        if (lugar != null) {
                            ItemFavoritoUi(
                                id = lugar.id,
                                nombre = lugar.nombre,
                                categoriaRes = lugar.categoria.etiquetaRes,
                                imagenUrl = lugar.imagenPortadaUrl,
                                calificacion = lugar.calificacion,
                                esComercio = false
                            )
                        } else {
                            val idComercioSinPrefijo = idLimpio.removePrefix("comercio:")
                            val comercio = todosComercios[idComercioSinPrefijo] ?: todosComercios[idLimpio]
                            if (comercio != null) {
                                ItemFavoritoUi(
                                    id = comercio.id,
                                    nombre = comercio.nombre,
                                    categoria = comercio.categoria,
                                    imagenUrl = comercio.imagenUrls.firstOrNull().orEmpty(),
                                    calificacion = comercio.calificacionPromedio ?: 4.9,
                                    esComercio = true
                                )
                            } else null
                        }
                    }
                    state.listaFavoritosDetallados = resueltos
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                val detalle = error.message?.takeIf { it.isNotBlank() }
                val mensaje = if (detalle != null) {
                    getString(Res.string.perfil_error_cargar_favoritos, detalle)
                } else {
                    getString(Res.string.perfil_error_cargar_favoritos_generico)
                }
                state.snackbarHostState.showSnackbar(mensaje)
            } finally {
                state.cargandoFavoritos = false
            }
        }
    }

    return state
}
