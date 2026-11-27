package com.turismap.app.screens.detalleexperiencia

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.UriHandler
import com.turismap.app.data.models.Lugar
import com.turismap.app.data.repository.AutenticacionRepository
import com.turismap.app.data.repository.Comercio
import com.turismap.app.data.repository.ComercioRepository
import com.turismap.app.data.repository.EstadoPublicados
import com.turismap.app.data.repository.LugaresRepository
import com.turismap.app.data.repository.Resena
import com.turismap.app.data.repository.UsuarioRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.detalle_exp_error_abrir_enlace
import turismap.shared.generated.resources.detalle_exp_error_cargar
import turismap.shared.generated.resources.detalle_exp_error_cargar_resenas
import turismap.shared.generated.resources.detalle_exp_error_guardar_resena
import turismap.shared.generated.resources.detalle_exp_error_no_disponible
import turismap.shared.generated.resources.guia_error_actualizar_favorito

private sealed class ResultadoRefrescoComercio {
    data class Disponible(val comercio: Comercio) : ResultadoRefrescoComercio()
    data object NoDisponible : ResultadoRefrescoComercio()
    data class ErrorRed(val excepcion: Exception) : ResultadoRefrescoComercio()
}

private fun buscarComercioEnCache(comercioId: String): Comercio? {
    val idLimpio = comercioId.trim()
    val directo = ComercioRepository.obtenerEnCachePorId(idLimpio)
    val desdeFlujo = (ComercioRepository.publicadosStateFlow.value as? EstadoPublicados.Exito)
        ?.comercios
        ?.firstOrNull { it.id.trim() == idLimpio }
    return (directo ?: desdeFlujo)?.takeIf { it.estado == "publicado" }
}

private fun ordenarResenasPorFecha(resenas: List<Resena>): List<Resena> =
    resenas.sortedWith(
        compareByDescending<Resena> { (it.actualizadoEn ?: it.creadoEn)?.seconds ?: Long.MIN_VALUE }
            .thenByDescending { (it.actualizadoEn ?: it.creadoEn)?.nanoseconds ?: Int.MIN_VALUE }
    )

internal class DetalleExperienciaState(
    private val comercioId: String,
    comercioInicial: Comercio?,
    categoriasIniciales: Map<String, String>,
    municipiosIniciales: Map<String, Lugar>,
    private val scope: CoroutineScope,
    val snackbarHostState: SnackbarHostState
) {
    var comercio by mutableStateOf(comercioInicial)
    var resenas by mutableStateOf<List<Resena>>(emptyList())
    var cargandoResenas by mutableStateOf(true)
    var errorResenas by mutableStateOf<String?>(null)
    var categoriasMap by mutableStateOf(categoriasIniciales)
    var municipiosMap by mutableStateOf(municipiosIniciales)
    var favoritosEnProceso by mutableStateOf<Set<String>>(emptySet())
    var cargando by mutableStateOf(comercioInicial == null)
    var error by mutableStateOf<String?>(null)
    var intento by mutableIntStateOf(0)
    var calificacion by mutableIntStateOf(0)
    var comentario by mutableStateOf("")
    var tieneResenaPropia by mutableStateOf(false)
    var usuarioEditoResena by mutableStateOf(false)
    var enviando by mutableStateOf(false)
    var mensaje by mutableStateOf<String?>(null)
    var resenaGuardadaExito by mutableStateOf(false)

    val tieneUsuarioAutenticado: Boolean
        get() = AutenticacionRepository.usuarioActual != null

    val usuarioActualUid: String?
        get() = AutenticacionRepository.usuarioActual?.uid

    fun mostrarAviso(recurso: StringResource) {
        scope.launch {
            snackbarHostState.showSnackbar(getString(recurso))
        }
    }

    fun abrirUriSeguro(
        uriHandler: UriHandler,
        uri: String?,
        errorFallback: StringResource = Res.string.detalle_exp_error_abrir_enlace
    ) {
        val urlLimpia = uri?.trim().orEmpty()
        if (urlLimpia.isBlank()) {
            mostrarAviso(errorFallback)
            return
        }
        try {
            uriHandler.openUri(urlLimpia)
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (_: Exception) {
            mostrarAviso(errorFallback)
        }
    }

    fun reintentar() {
        intento++
    }

    fun reintentarResenas() {
        if (cargandoResenas) return
        scope.launch {
            cargarResenas()
        }
    }

    internal suspend fun cargarResenas() {
        cargandoResenas = true
        errorResenas = null
        try {
            val cargadas = ordenarResenasPorFecha(ComercioRepository.obtenerResenas(comercioId))
            aplicarResenasCargadas(cargadas)
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (ex: Exception) {
            errorResenas = ex.message ?: getString(Res.string.detalle_exp_error_cargar_resenas)
        } finally {
            cargandoResenas = false
        }
    }

    internal fun aplicarResenasCargadas(resenasCargadas: List<Resena>) {
        resenas = resenasCargadas
        val uidActual = AutenticacionRepository.usuarioActual?.uid
        val propia = if (!uidActual.isNullOrBlank()) {
            resenasCargadas.firstOrNull { it.usuarioUid == uidActual }
        } else {
            null
        }
        tieneResenaPropia = propia != null
        if (!usuarioEditoResena) {
            calificacion = propia?.calificacion ?: 0
            comentario = propia?.comentario.orEmpty()
        }
    }

    fun actualizarCalificacion(nuevaCalificacion: Int) {
        calificacion = nuevaCalificacion
        usuarioEditoResena = true
        mensaje = null
        resenaGuardadaExito = false
    }

    fun actualizarComentario(nuevoComentario: String) {
        comentario = sanitizarComentarioResena(nuevoComentario)
        usuarioEditoResena = true
        mensaje = null
        resenaGuardadaExito = false
    }

    fun toggleFavorito(esFavoritoActual: Boolean) {
        if (comercioId in favoritosEnProceso) return
        favoritosEnProceso = favoritosEnProceso + comercioId
        scope.launch {
            try {
                UsuarioRepository.cambiarFavorito(
                    lugarId = comercioId,
                    guardar = !esFavoritoActual,
                    esComercio = true
                )
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(
                    e.message ?: getString(Res.string.guia_error_actualizar_favorito)
                )
            } finally {
                favoritosEnProceso = favoritosEnProceso - comercioId
            }
        }
    }

    fun publicarResena(datosId: String) {
        val uidActual = usuarioActualUid
        val propietarioUid = comercio?.propietarioUid.orEmpty()
        if (enviando || uidActual.isNullOrBlank() || (propietarioUid.isNotBlank() && propietarioUid == uidActual)) {
            return
        }

        val errorValidacion = validarCalificacionResena(calificacion)
        if (errorValidacion != null) {
            resenaGuardadaExito = false
            scope.launch {
                mensaje = getString(errorValidacion)
            }
            return
        }

        enviando = true
        mensaje = null
        resenaGuardadaExito = false
        scope.launch {
            try {
                ComercioRepository.enviarResena(datosId, calificacion, comentario.trim())
                val actualizadas = ordenarResenasPorFecha(ComercioRepository.obtenerResenas(datosId))
                usuarioEditoResena = false
                errorResenas = null
                aplicarResenasCargadas(actualizadas)
                resenaGuardadaExito = true
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (ex: Exception) {
                mensaje = ex.message ?: getString(Res.string.detalle_exp_error_guardar_resena)
            } finally {
                enviando = false
            }
        }
    }
}

@Composable
internal fun rememberDetalleExperienciaState(comercioId: String): Pair<DetalleExperienciaState, Boolean> {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val state = remember(comercioId, scope, snackbarHostState) {
        val comercioInicial = buscarComercioEnCache(comercioId)
        val categoriasIniciales = ComercioRepository.categoriasEnCache
            ?.associate { it.id.trim() to it.etiqueta.trim() }
            .orEmpty()
        val municipiosIniciales = LugaresRepository.lugaresEnCache
            ?.associateBy { it.id.trim() }
            .orEmpty()
        DetalleExperienciaState(
            comercioId = comercioId,
            comercioInicial = comercioInicial,
            categoriasIniciales = categoriasIniciales,
            municipiosIniciales = municipiosIniciales,
            scope = scope,
            snackbarHostState = snackbarHostState
        )
    }

    val usuarioActual by UsuarioRepository.usuarioActualFlow.collectAsState()
    val esFavorito = remember(usuarioActual?.favoritos, comercioId) {
        usuarioActual?.favoritos?.any { it == comercioId || it == "comercio:$comercioId" } == true
    }

    LaunchedEffect(comercioId, state.intento) {
        val teniaComercio = state.comercio != null
        val esReintento = state.intento > 0
        if (!teniaComercio) {
            state.cargando = true
        }
        state.cargandoResenas = true
        state.error = null
        state.errorResenas = null
        try {
            coroutineScope {
                val tareaComercio = async {
                    try {
                        val remoto = ComercioRepository.obtenerPorId(comercioId)
                        if (remoto != null && remoto.estado == "publicado") {
                            ResultadoRefrescoComercio.Disponible(remoto)
                        } else {
                            ResultadoRefrescoComercio.NoDisponible
                        }
                    } catch (cancelacion: CancellationException) {
                        throw cancelacion
                    } catch (ex: Exception) {
                        ResultadoRefrescoComercio.ErrorRed(ex)
                    }
                }
                val tareaCategorias = async {
                    if (state.categoriasMap.isNotEmpty() && !esReintento) return@async null
                    try {
                        ComercioRepository.obtenerCategorias()
                            .associate { it.id.trim() to it.etiqueta.trim() }
                    } catch (cancelacion: CancellationException) {
                        throw cancelacion
                    } catch (_: Exception) {
                        null
                    }
                }
                val tareaMunicipios = async {
                    if (state.municipiosMap.isNotEmpty() && !esReintento) return@async null
                    try {
                        LugaresRepository.obtenerTodos().associateBy { it.id.trim() }
                    } catch (cancelacion: CancellationException) {
                        throw cancelacion
                    } catch (_: Exception) {
                        null
                    }
                }
                val tareaResenas = async {
                    try {
                        Result.success(ordenarResenasPorFecha(ComercioRepository.obtenerResenas(comercioId)))
                    } catch (cancelacion: CancellationException) {
                        throw cancelacion
                    } catch (ex: Exception) {
                        Result.failure(ex)
                    }
                }

                when (val resultadoComercio = tareaComercio.await()) {
                    is ResultadoRefrescoComercio.Disponible -> {
                        state.comercio = resultadoComercio.comercio
                    }
                    ResultadoRefrescoComercio.NoDisponible -> {
                        state.comercio = null
                        state.error = getString(Res.string.detalle_exp_error_no_disponible)
                    }
                    is ResultadoRefrescoComercio.ErrorRed -> {
                        if (!teniaComercio) {
                            state.error = resultadoComercio.excepcion.message
                                ?: getString(Res.string.detalle_exp_error_cargar)
                        }
                    }
                }

                tareaCategorias.await()?.let { state.categoriasMap = it }
                tareaMunicipios.await()?.let { state.municipiosMap = it }

                val resultadoResenas = tareaResenas.await()
                resultadoResenas.onSuccess { resenasCargadas ->
                    state.aplicarResenasCargadas(resenasCargadas)
                }.onFailure { errorCarga ->
                    state.errorResenas = errorCarga.message
                        ?: getString(Res.string.detalle_exp_error_cargar_resenas)
                }
            }
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } finally {
            state.cargando = false
            state.cargandoResenas = false
        }
    }

    return state to esFavorito
}
