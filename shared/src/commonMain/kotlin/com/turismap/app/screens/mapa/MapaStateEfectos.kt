package com.turismap.app.screens.mapa

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import com.turismap.app.MapaCamaraCache
import com.turismap.app.data.repository.AtractivosPublicosRepository
import com.turismap.app.data.repository.ComercioRepository
import com.turismap.app.data.repository.EstadoPublicados
import com.turismap.app.data.repository.LugaresRepository
import com.turismap.app.data.repository.UsuarioRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.jetbrains.compose.resources.getString
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.mapa_error_cargar_atractivos
import turismap.shared.generated.resources.mapa_error_cargar_comercios
import turismap.shared.generated.resources.mapa_error_cargar_destinos
import turismap.shared.generated.resources.mapa_sin_datos_publicados

@Composable
internal fun rememberMapaState(
    latInicial: Double?,
    lngInicial: Double?,
    zoomInicial: Float?
): MapaState {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val uidActual = UsuarioRepository.obtenerIdUsuarioActual()
    val textoBusquedaState = rememberSaveable { mutableStateOf("") }
    val filtroNombreState = rememberSaveable { mutableStateOf(FiltroMarcadoresMapa.TODOS.name) }
    val categoriaAtractivoNombreState = rememberSaveable { mutableStateOf<String?>(null) }

    val state = remember(uidActual) {
        val lugaresCache = LugaresRepository.lugaresEnCache
        val comerciosCache = ComercioRepository.publicadosEnCache
        val atractivosCache = AtractivosPublicosRepository.atractivosEnCache
        val categoriasCache = ComercioRepository.categoriasEnCache
            ?.associate { it.id.trim() to it.etiqueta.trim() }
            .orEmpty()
        MapaState(
            latInicial = latInicial,
            lngInicial = lngInicial,
            zoomInicial = zoomInicial,
            uidActual = uidActual,
            lugaresIniciales = lugaresCache,
            comerciosIniciales = comerciosCache,
            atractivosIniciales = atractivosCache,
            categoriasIniciales = categoriasCache,
            camaraGuardada = MapaCamaraCache.obtener(uidActual),
            textoBusquedaState = textoBusquedaState,
            filtroNombreState = filtroNombreState,
            categoriaAtractivoNombreState = categoriaAtractivoNombreState,
            scope = coroutineScope,
            snackbarHostState = snackbarHostState
        )
    }

    LaunchedEffect(latInicial, lngInicial, zoomInicial) {
        if (latInicial != null && lngInicial != null) {
            state.camaraLatObjetivo = latInicial
            state.camaraLngObjetivo = lngInicial
            state.camaraZoomObjetivo = zoomInicial
        }
    }

    LaunchedEffect(state.intentoCarga) {
        val esReintentoManual = state.intentoCarga > 0
        if (!esReintentoManual &&
            LugaresRepository.lugaresEnCache != null &&
            ComercioRepository.categoriasEnCache != null
        ) {
            val cacheLugares = LugaresRepository.lugaresEnCache.orEmpty()
            state.lugaresTuristicos = cacheLugares
            state.municipiosMap = cacheLugares.associateBy { it.id.trim() }
            state.categoriasMap = ComercioRepository.categoriasEnCache
                .orEmpty()
                .associate { it.id.trim() to it.etiqueta.trim() }
            state.errorLugares = null
            state.lugaresCargados = true
            return@LaunchedEffect
        }

        val tieneDatosVisibles = state.lugaresTuristicos.isNotEmpty()
        if (!tieneDatosVisibles) {
            state.lugaresCargados = false
        }
        state.errorLugares = null
        try {
            val (lugares, categorias) = coroutineScope {
                val tareaLugares = async {
                    LugaresRepository.obtenerTodos(forzarRecarga = esReintentoManual)
                }
                val tareaCategorias = async {
                    try {
                        ComercioRepository.obtenerCategorias(forzarRecarga = esReintentoManual)
                            .associate { it.id.trim() to it.etiqueta.trim() }
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Exception) {
                        null
                    }
                }
                tareaLugares.await() to tareaCategorias.await()
            }
            state.lugaresTuristicos = lugares
            state.municipiosMap = lugares.associateBy { it.id.trim() }
            categorias?.let { state.categoriasMap = it }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            if (!tieneDatosVisibles) {
                state.errorLugares = error.message ?: getString(Res.string.mapa_error_cargar_destinos)
            }
        } finally {
            state.lugaresCargados = true
        }
    }

    // Carga independiente de atractivos naturales públicos (con fallback seed si Firestore está vacío/sin permisos)
    LaunchedEffect(state.intentoCarga) {
        val esReintentoManual = state.intentoCarga > 0
        if (!esReintentoManual && AtractivosPublicosRepository.atractivosEnCache != null) {
            state.atractivosPublicos = AtractivosPublicosRepository.atractivosEnCache.orEmpty()
            state.errorAtractivos = null
            state.atractivosCargados = true
            return@LaunchedEffect
        }

        val tieneDatosVisibles = state.atractivosPublicos.isNotEmpty()
        if (!tieneDatosVisibles) {
            state.atractivosCargados = false
        }
        state.errorAtractivos = null
        try {
            state.atractivosPublicos = AtractivosPublicosRepository.obtenerActivos(
                forzarRecarga = esReintentoManual
            )
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            if (!tieneDatosVisibles) {
                state.errorAtractivos = error.message ?: getString(Res.string.mapa_error_cargar_atractivos)
            }
        } finally {
            state.atractivosCargados = true
        }
    }

    // Suscripción al StateFlow compartido de comercios publicados (sin recrear listener al volver a Mapa)
    LaunchedEffect(state.intentoCarga) {
        val esReintentoManual = state.intentoCarga > 0
        if (esReintentoManual) {
            ComercioRepository.reintentarObservacionPublicados()
        }
        if (state.todosLosComercios.isEmpty() && ComercioRepository.publicadosEnCache == null) {
            state.comerciosCargados = false
        }
        state.errorComercios = null

        ComercioRepository.publicadosStateFlow.collect { estado ->
            when (estado) {
                is EstadoPublicados.Cargando -> {
                    val cache = ComercioRepository.publicadosEnCache
                    if (cache != null) {
                        state.todosLosComercios = cache
                        state.comerciosCargados = true
                    }
                }
                is EstadoPublicados.Exito -> {
                    state.todosLosComercios = estado.comercios
                    state.errorComercios = null
                    state.comerciosCargados = true
                }
                is EstadoPublicados.Error -> {
                    val cache = estado.comerciosEnCache
                    if (cache != null) {
                        state.todosLosComercios = cache
                        state.errorComercios = null
                        state.comerciosCargados = true
                    } else {
                        try {
                            state.todosLosComercios = ComercioRepository.obtenerPublicados(
                                forzarRecarga = esReintentoManual
                            )
                            state.errorComercios = null
                        } catch (fallbackError: CancellationException) {
                            throw fallbackError
                        } catch (fallbackError: Exception) {
                            if (state.todosLosComercios.isEmpty()) {
                                state.errorComercios = fallbackError.message
                                    ?: estado.causa.message
                                    ?: getString(Res.string.mapa_error_cargar_comercios)
                            }
                        } finally {
                            state.comerciosCargados = true
                        }
                    }
                }
            }
        }
    }

    // Detección y selección automática del municipio si se ingresa con coordenadas específicas
    LaunchedEffect(latInicial, lngInicial, state.lugaresTuristicos) {
        if (latInicial != null && lngInicial != null && state.lugaresTuristicos.isNotEmpty()) {
            val lugarCoincidente = state.lugaresTuristicos.minByOrNull { lugar ->
                val dLat = lugar.ubicacionCoordenadas.latitud - latInicial
                val dLng = lugar.ubicacionCoordenadas.longitud - lngInicial
                (dLat * dLat) + (dLng * dLng)
            }
            if (lugarCoincidente != null) {
                state.marcadorSeleccionado = MarcadorMapa.Destino(lugarCoincidente)
            }
        }
    }

    LaunchedEffect(state.errorCargaDetalle, state.sinDatosPublicados) {
        val mensaje = state.errorCargaDetalle
            ?: if (state.sinDatosPublicados) getString(Res.string.mapa_sin_datos_publicados) else null
        mensaje?.let { state.snackbarHostState.showSnackbar(it) }
    }

    return state
}
