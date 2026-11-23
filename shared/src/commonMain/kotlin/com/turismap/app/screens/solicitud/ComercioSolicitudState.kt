package com.turismap.app.screens.solicitud

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.turismap.app.data.models.CategoriaComercio
import com.turismap.app.data.models.Coordenada
import com.turismap.app.data.models.Lugar
import com.turismap.app.data.repository.ComercioRepository
import com.turismap.app.data.repository.LugaresRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.comercio_dia_domingo
import turismap.shared.generated.resources.comercio_dia_jueves
import turismap.shared.generated.resources.comercio_dia_lunes
import turismap.shared.generated.resources.comercio_dia_martes
import turismap.shared.generated.resources.comercio_dia_miercoles
import turismap.shared.generated.resources.comercio_dia_sabado
import turismap.shared.generated.resources.comercio_dia_viernes
import turismap.shared.generated.resources.comercio_error_cargar_categorias
import turismap.shared.generated.resources.comercio_error_cargar_plan
import turismap.shared.generated.resources.comercio_error_cargar_ubicaciones
import turismap.shared.generated.resources.comercio_error_enviar_solicitud
import turismap.shared.generated.resources.comercio_error_permisos_coleccion
import turismap.shared.generated.resources.comercio_error_plan_no_disponible
import turismap.shared.generated.resources.comercio_error_seleccionar_ubicacion
import turismap.shared.generated.resources.comercio_error_seleccionar_ubicacion_exacta
import turismap.shared.generated.resources.comercio_error_sin_categorias
import turismap.shared.generated.resources.comercio_error_sin_ubicaciones
import turismap.shared.generated.resources.comercio_error_sitio_web_https

internal data class HorarioDia(
    val apertura: String = "",
    val cierre: String = ""
)

internal enum class DiaSemanaSolicitud(
    val nombrePersistencia: String
) {
    LUNES("Lunes"),
    MARTES("Martes"),
    MIERCOLES("Miércoles"),
    JUEVES("Jueves"),
    VIERNES("Viernes"),
    SABADO("Sábado"),
    DOMINGO("Domingo");

    val etiquetaRes: StringResource
        get() = when (this) {
            LUNES -> Res.string.comercio_dia_lunes
            MARTES -> Res.string.comercio_dia_martes
            MIERCOLES -> Res.string.comercio_dia_miercoles
            JUEVES -> Res.string.comercio_dia_jueves
            VIERNES -> Res.string.comercio_dia_viernes
            SABADO -> Res.string.comercio_dia_sabado
            DOMINGO -> Res.string.comercio_dia_domingo
        }
}

internal class ComercioSolicitudState(
    nombreCompletoInicial: String,
    emailInicial: String,
    val planId: String,
    private val scope: CoroutineScope
) {
    private val planIdNormalizado = planId.trim().lowercase()
    val esSocio: Boolean = planIdNormalizado.contains("socio") || planIdNormalizado.contains("premium")
    val esPremium: Boolean = planIdNormalizado.contains("premium")
    val email: String = emailInicial.trim()

    var lugares by mutableStateOf<List<Lugar>>(emptyList())
    var cargandoLugares by mutableStateOf(true)
    var lugarSeleccionado by mutableStateOf<Lugar?>(null)
    var coordenadaSeleccionada by mutableStateOf<Coordenada?>(null)
    var seleccionandoUbicacion by mutableStateOf(false)

    var nombreCompleto by mutableStateOf(nombreCompletoInicial.take(120))
    var nombreNegocio by mutableStateOf("")
    var categoria by mutableStateOf("")
    var categoriaId by mutableStateOf("")
    var descripcion by mutableStateOf("")
    var direccion by mutableStateOf("")
    var telefono by mutableStateOf("")
    var sitioWeb by mutableStateOf("")
    var horario by mutableStateOf("")
    var promocionEspecial by mutableStateOf("")

    var lugaresError by mutableStateOf<String?>(null)
    var errorEnvio by mutableStateOf<String?>(null)
    var enviando by mutableStateOf(false)
    var selectorMunicipioAbierto by mutableStateOf(false)
    var selectorCategoriaAbierto by mutableStateOf(false)
    var categorias by mutableStateOf<List<CategoriaComercio>>(emptyList())
    var cargandoCategorias by mutableStateOf(true)
    var categoriasError by mutableStateOf<String?>(null)
    var planNombre by mutableStateOf("")
    var planPrecio by mutableStateOf<String?>(null)
    var cargandoPlan by mutableStateOf(true)
    var planError by mutableStateOf<String?>(null)
    var intentoCarga by mutableIntStateOf(0)

    var mostrarHorarioDetallado by mutableStateOf(false)
    var diasSeleccionados by mutableStateOf<Set<DiaSemanaSolicitud>>(emptySet())
    var horariosPorDia by mutableStateOf<Map<DiaSemanaSolicitud, HorarioDia>>(emptyMap())
    var solicitudEnviada by mutableStateOf(false)

    val digitosTelefono: Int
        get() = telefono.count { it.isDigit() }

    val telefonoInvalido: Boolean
        get() = telefono.isNotBlank() && digitosTelefono < 8

    val sitioWebInvalido: Boolean
        get() = esSocio && sitioWeb.isNotBlank() && !sitioWeb.trim().startsWith("https://", ignoreCase = true)

    val puedeEnviar: Boolean
        get() = !enviando &&
            !cargandoLugares &&
            lugaresError == null &&
            !cargandoCategorias &&
            categoriasError == null &&
            !cargandoPlan &&
            planError == null &&
            lugarSeleccionado != null &&
            nombreCompleto.isNotBlank() &&
            email.isNotBlank() &&
            nombreNegocio.isNotBlank() &&
            categoriaId.isNotBlank() &&
            coordenadaSeleccionada != null &&
            descripcion.isNotBlank() &&
            direccion.isNotBlank() &&
            digitosTelefono >= 8 &&
            !sitioWebInvalido &&
            !solicitudEnviada

    fun reintentarCarga() {
        intentoCarga++
    }

    fun aplicarHorarioPorDias() {
        val diasOrdenados = DiaSemanaSolicitud.entries.filter { it in diasSeleccionados }
        horario = diasOrdenados.joinToString(", ") { dia ->
            val horarioDia = horariosPorDia[dia] ?: HorarioDia()
            val abre = horarioDia.apertura.trim().ifBlank { "8:00 AM" }
            val cierra = horarioDia.cierre.trim().ifBlank { "5:00 PM" }
            "${dia.nombrePersistencia}: $abre a $cierra"
        }
        mostrarHorarioDetallado = false
    }

    fun enviarSolicitud() {
        val webLimpia = sitioWeb.trim()
        if (esSocio && webLimpia.isNotBlank() && !webLimpia.startsWith("https://", ignoreCase = true)) {
            scope.launch {
                errorEnvio = getString(Res.string.comercio_error_sitio_web_https)
            }
            return
        }

        enviando = true
        errorEnvio = null
        scope.launch {
            try {
                val lugar = lugarSeleccionado
                    ?: throw IllegalStateException(getString(Res.string.comercio_error_seleccionar_ubicacion))

                val descripcionFinal = if (esPremium && promocionEspecial.isNotBlank()) {
                    "${descripcion.trim()}\n\n[PROMO TURISMAP]: ${promocionEspecial.trim()}"
                } else {
                    descripcion.trim()
                }.take(2000)

                ComercioRepository.enviarSolicitudComercio(
                    nombreCompleto = nombreCompleto.trim().take(120),
                    email = email,
                    municipio = lugar.nombre.trim().take(100),
                    nombreNegocio = nombreNegocio.trim().take(120),
                    categoria = categoriaId,
                    descripcion = descripcionFinal,
                    direccion = direccion.trim().take(300),
                    telefono = telefono.trim().take(40),
                    sitioWeb = if (esSocio) webLimpia else "",
                    horario = if (esSocio) horario.trim() else "",
                    planId = planId,
                    coordenadas = coordenadaSeleccionada
                        ?: throw IllegalStateException(
                            getString(Res.string.comercio_error_seleccionar_ubicacion_exacta)
                        )
                )
                solicitudEnviada = true
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                errorEnvio = error.message ?: getString(Res.string.comercio_error_enviar_solicitud)
            } finally {
                enviando = false
            }
        }
    }
}

@Composable
internal fun rememberComercioSolicitudState(
    nombreCompletoInicial: String,
    emailInicial: String,
    planId: String
): ComercioSolicitudState {
    val scope = rememberCoroutineScope()
    val state = remember(planId) {
        ComercioSolicitudState(
            nombreCompletoInicial = nombreCompletoInicial,
            emailInicial = emailInicial,
            planId = planId,
            scope = scope
        )
    }

    LaunchedEffect(planId, state.intentoCarga) {
        state.cargandoLugares = true
        state.cargandoCategorias = true
        state.cargandoPlan = true
        state.lugaresError = null
        state.categoriasError = null
        state.planError = null
        state.planNombre = ""
        state.planPrecio = null

        try {
            state.lugares = LugaresRepository.obtenerTodos()
            state.lugaresError = if (state.lugares.isEmpty()) {
                getString(Res.string.comercio_error_sin_ubicaciones)
            } else {
                null
            }
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (error: Exception) {
            state.lugaresError = mensajeErrorCarga(
                error = error,
                coleccion = "lugares",
                mensajePredeterminadoRes = Res.string.comercio_error_cargar_ubicaciones
            )
        } finally {
            state.cargandoLugares = false
        }

        try {
            state.categorias = ComercioRepository.obtenerCategorias()
            state.categoriasError = if (state.categorias.isEmpty()) {
                getString(Res.string.comercio_error_sin_categorias)
            } else {
                null
            }
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (error: Exception) {
            state.categoriasError = mensajeErrorCarga(
                error = error,
                coleccion = "categorias_comercio",
                mensajePredeterminadoRes = Res.string.comercio_error_cargar_categorias
            )
        } finally {
            state.cargandoCategorias = false
        }

        try {
            val plan = ComercioRepository.obtenerPlanDisponible(planId)
            state.planNombre = plan?.nombre.orEmpty()
            state.planPrecio = plan?.precioFormateado()
            state.planError = if (plan == null) getString(Res.string.comercio_error_plan_no_disponible) else null
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (error: Exception) {
            state.planError = mensajeErrorCarga(
                error = error,
                coleccion = "planes_comercio",
                mensajePredeterminadoRes = Res.string.comercio_error_cargar_plan
            )
        } finally {
            state.cargandoPlan = false
        }
    }

    return state
}

private suspend fun mensajeErrorCarga(
    error: Exception,
    coleccion: String,
    mensajePredeterminadoRes: StringResource
): String {
    val detalle = generateSequence(error as Throwable?) { it.cause }
        .mapNotNull(Throwable::message)
        .joinToString(" ")
    return if (
        detalle.contains("PERMISSION_DENIED", ignoreCase = true) ||
        detalle.contains("missing or insufficient permissions", ignoreCase = true)
    ) {
        getString(Res.string.comercio_error_permisos_coleccion, coleccion)
    } else {
        error.message?.takeIf(String::isNotBlank) ?: getString(mensajePredeterminadoRes)
    }
}
