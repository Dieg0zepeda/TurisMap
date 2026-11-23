package com.turismap.app.screens.perfil

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
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
import com.turismap.app.theme.GrisBorde
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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
import turismap.shared.generated.resources.perfil_privacidad_enlace_enviado
import turismap.shared.generated.resources.perfil_privacidad_error_enviar_correo
import turismap.shared.generated.resources.perfil_usuario_predeterminado

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
    val colorDivisor = GrisBorde
    return remember(colorScheme, colorDivisor) {
        PerfilPaleta(
            fondoApp = colorScheme.background,
            fondoTarjeta = colorScheme.surface,
            colorTextoPrincipal = colorScheme.onBackground,
            colorTextoSecundario = colorScheme.onSurfaceVariant,
            colorDivisor = colorDivisor
        )
    }
}

internal class PerfilState(
    private val coroutineScope: CoroutineScope,
    val snackbarHostState: SnackbarHostState,
    private val usuarioBDState: State<UsuarioBD?>,
    estaCargandoInicial: Boolean,
    visitasIniciales: Int,
    favoritosIniciales: List<ItemFavoritoUi>,
    favoritosSincronizadosInicial: Boolean
) {
    val usuarioBD: UsuarioBD?
        get() = usuarioBDState.value
    var estaCargando by mutableStateOf(estaCargandoInicial)
    var visitas by mutableStateOf(visitasIniciales)
    var errorPerfil by mutableStateOf<String?>(null)

    // Control del modal de favoritos y filtro por categoría
    var mostrarVentanaFavoritos by mutableStateOf(false)
    var cargandoFavoritos by mutableStateOf(false)
    var favoritosSincronizados by mutableStateOf(favoritosSincronizadosInicial)
    var listaFavoritosDetallados by mutableStateOf(favoritosIniciales)
    var favoritosEnProceso by mutableStateOf<Set<String>>(emptySet())
    var categoriaFavoritoSeleccionada by mutableStateOf(CategoriaFavorito.TODOS)

    var mostrarDialogoEditar by mutableStateOf(false)
    var cerrandoSesion by mutableStateOf(false)
    var enviandoRecuperacion by mutableStateOf(false)
    var guardandoPerfil by mutableStateOf(false)
    var subiendoFoto by mutableStateOf(false)
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
        get() = when {
            favoritosSincronizados -> listaFavoritosDetallados.size.toString()
            usuarioBD?.favoritos?.isEmpty() == true -> "0"
            else -> "—"
        }

    fun actualizarFoto(uriLocal: String) {
        if (subiendoFoto || uriLocal.isBlank()) return
        val esPerfil = tipoFotoSeleccionada == "perfil"
        subiendoFoto = true
        coroutineScope.launch {
            try {
                UsuarioRepository.subirYActualizarFoto(perfil = esPerfil, uriLocal = uriLocal)
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
            } finally {
                subiendoFoto = false
            }
        }
    }

    fun quitarFavorito(item: ItemFavoritoUi) {
        val clave = item.idFavoritoOriginal
        if (clave in favoritosEnProceso) return
        favoritosEnProceso = favoritosEnProceso + clave
        coroutineScope.launch {
            try {
                UsuarioRepository.cambiarFavorito(
                    lugarId = item.id,
                    guardar = false,
                    esComercio = item.esComercio
                )
                listaFavoritosDetallados = listaFavoritosDetallados.filterNot {
                    it.esComercio == item.esComercio && it.id == item.id
                }
                snackbarHostState.showSnackbar(getString(Res.string.perfil_favorito_eliminado))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(e.message ?: getString(Res.string.perfil_error_actualizar))
            } finally {
                favoritosEnProceso = favoritosEnProceso - clave
            }
        }
    }

    fun enviarRestablecimientoContrasena() {
        if (!enviandoRecuperacion) {
            enviandoRecuperacion = true
            coroutineScope.launch {
                try {
                    val emailDestino = usuarioBD?.email?.takeIf { it.isNotBlank() }
                        ?: AutenticacionRepository.usuarioActual?.email.orEmpty()
                    AutenticacionRepository.enviarRestablecimientoContrasena(emailDestino)
                    snackbarHostState.showSnackbar(
                        getString(Res.string.perfil_privacidad_enlace_enviado, emailDestino)
                    )
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    snackbarHostState.showSnackbar(
                        error.message ?: getString(Res.string.perfil_privacidad_error_enviar_correo)
                    )
                } finally {
                    enviandoRecuperacion = false
                }
            }
        }
    }

    fun guardarPerfil(nombre: String) {
        if (!guardandoPerfil) {
            guardandoPerfil = true
            coroutineScope.launch {
                try {
                    UsuarioRepository.actualizarPerfil(nombre.trim())
                    mostrarDialogoEditar = false
                    snackbarHostState.showSnackbar(getString(Res.string.perfil_actualizado))
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    snackbarHostState.showSnackbar(error.message ?: getString(Res.string.perfil_error_actualizar))
                } finally {
                    guardandoPerfil = false
                }
            }
        }
    }

    fun cerrarSesion(onCerrarSesionClick: () -> Unit) {
        if (!cerrandoSesion) {
            cerrandoSesion = true
            coroutineScope.launch {
                try {
                    PerfilCacheSesion.limpiar()
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
}

@Composable
internal fun rememberPerfilState(): PerfilState {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // 1. Observación reactiva centralizada del usuario desde Firestore (sin mutaciones en composición)
    val usuarioBDState = UsuarioRepository.usuarioActualFlow.collectAsState()
    val idUsuarioActual = UsuarioRepository.obtenerIdUsuarioActual()

    val state = remember(idUsuarioActual, coroutineScope, snackbarHostState, usuarioBDState) {
        val usuarioInicial = usuarioBDState.value
        val favoritosEnCache = intentarResolverFavoritosEnCache(
            uid = idUsuarioActual,
            favoritosIds = usuarioInicial?.favoritos
        )
        PerfilState(
            coroutineScope = coroutineScope,
            snackbarHostState = snackbarHostState,
            usuarioBDState = usuarioBDState,
            estaCargandoInicial = usuarioInicial == null,
            visitasIniciales = UsuarioRepository.visitasEnCache(idUsuarioActual) ?: 0,
            favoritosIniciales = favoritosEnCache?.first.orEmpty(),
            favoritosSincronizadosInicial = favoritosEnCache?.second == true
        )
    }

    val usuarioBD = state.usuarioBD

    LaunchedEffect(idUsuarioActual) {
        if (state.usuarioBD == null) {
            state.estaCargando = true
        }
        state.errorPerfil = null
        try {
            if (idUsuarioActual == null) {
                state.errorPerfil = getString(Res.string.perfil_error_sin_sesion)
            } else {
                if (!UsuarioRepository.perfilYaAsegurado(idUsuarioActual)) {
                    UsuarioRepository.asegurarPerfil(
                        email = AutenticacionRepository.usuarioActual?.email.orEmpty(),
                        nombre = AutenticacionRepository.usuarioActual?.displayName.orEmpty()
                    )
                }
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

    // Carga y mapeo reactivo de favoritos usando caché en memoria primero para 0 parpadeos
    LaunchedEffect(idUsuarioActual, usuarioBD?.favoritos) {
        val favoritosIds = usuarioBD?.favoritos ?: run {
            state.listaFavoritosDetallados = emptyList()
            state.favoritosSincronizados = false
            return@LaunchedEffect
        }
        if (favoritosIds.isEmpty()) {
            state.listaFavoritosDetallados = emptyList()
            state.favoritosSincronizados = true
            state.cargandoFavoritos = false
            PerfilCacheSesion.guardar(idUsuarioActual, emptyList(), emptyList(), true)
            return@LaunchedEffect
        }

        // Resolución síncrona instantánea si los catálogos ya viven en memoria
        intentarResolverFavoritosEnCache(idUsuarioActual, favoritosIds)?.let { (resueltos, sinc) ->
            state.listaFavoritosDetallados = resueltos
            state.favoritosSincronizados = sinc
            state.cargandoFavoritos = false
            if (ComercioRepository.categoriasEnCache != null) {
                return@LaunchedEffect
            }
        }

        if (!state.favoritosSincronizados) {
            state.cargandoFavoritos = true
        }
        try {
            var lugaresOk = true
            var comerciosOk = true
            val (todosLugares, todosComercios, categoriasMap) = coroutineScope {
                val tareaLugares = async {
                    try {
                        LugaresRepository.obtenerTodos().associateBy { it.id.trim() }
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Exception) {
                        lugaresOk = false
                        emptyMap()
                    }
                }
                val tareaComercios = async {
                    try {
                        ComercioRepository.obtenerPublicados().associateBy { it.id.trim() }
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Exception) {
                        comerciosOk = false
                        emptyMap()
                    }
                }
                val tareaCategorias = async {
                    try {
                        ComercioRepository.obtenerCategorias().associate { it.id.trim() to it.etiqueta }
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Exception) {
                        emptyMap()
                    }
                }
                Triple(tareaLugares.await(), tareaComercios.await(), tareaCategorias.await())
            }

            val resueltos = resolverFavoritosDesdeCatalogos(
                favoritosIds = favoritosIds,
                todosLugares = todosLugares,
                todosComercios = todosComercios,
                categoriasMap = categoriasMap
            )
            if (lugaresOk || comerciosOk) {
                val sincronizados = lugaresOk && comerciosOk
                state.listaFavoritosDetallados = resueltos
                state.favoritosSincronizados = sincronizados
                if (sincronizados) {
                    PerfilCacheSesion.guardar(idUsuarioActual, favoritosIds, resueltos, true)
                }
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            if (state.mostrarVentanaFavoritos) {
                val detalle = error.message?.takeIf { it.isNotBlank() }
                val mensaje = if (detalle != null) {
                    getString(Res.string.perfil_error_cargar_favoritos, detalle)
                } else {
                    getString(Res.string.perfil_error_cargar_favoritos_generico)
                }
                state.snackbarHostState.showSnackbar(mensaje)
            }
        } finally {
            state.cargandoFavoritos = false
        }
    }

    return state
}
