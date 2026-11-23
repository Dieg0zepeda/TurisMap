package com.turismap.app.screens.detalleexperiencia

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.turismap.app.data.repository.AutenticacionRepository
import com.turismap.app.data.repository.Comercio
import com.turismap.app.data.repository.ComercioRepository
import com.turismap.app.data.repository.Resena
import com.turismap.app.data.repository.UsuarioRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.detalle_exp_error_calificacion_invalida
import turismap.shared.generated.resources.detalle_exp_error_cargar
import turismap.shared.generated.resources.detalle_exp_error_guardar_resena
import turismap.shared.generated.resources.detalle_exp_error_no_disponible
import turismap.shared.generated.resources.guia_error_actualizar_favorito

internal data class DetalleExperienciaPaleta(
    val esOscuro: Boolean,
    val verdeTurisMap: Color = Color(0xFF10B981),
    val verdeOscuroTurisMap: Color = Color(0xFF047857),
    val doradoCorona: Color = Color(0xFFF59E0B),
    val colorFondoPantalla: Color,
    val colorTextoPrincipal: Color,
    val colorTextoSecundario: Color,
    val colorBordeSuave: Color,
    val colorBarraInferior: Color,
    val colorFondoInput: Color
)

@Composable
internal fun rememberDetalleExperienciaPaleta(esOscuro: Boolean): DetalleExperienciaPaleta =
    remember(esOscuro) {
        DetalleExperienciaPaleta(
            esOscuro = esOscuro,
            colorFondoPantalla = if (esOscuro) Color(0xFF0B0F17) else Color(0xFFF8FAFC),
            colorTextoPrincipal = if (esOscuro) Color(0xFFF8FAFC) else Color(0xFF0F172A),
            colorTextoSecundario = if (esOscuro) Color(0xFF94A3B8) else Color(0xFF64748B),
            colorBordeSuave = if (esOscuro) Color.White.copy(alpha = 0.08f) else Color(0xFFE2E8F0),
            colorBarraInferior = if (esOscuro) Color(0xFF0F172A) else Color.White,
            colorFondoInput = if (esOscuro) Color(0xFF1E293B) else Color(0xFFF1F5F9)
        )
    }

internal data class DetalleExperienciaDatosUi(
    val comercio: Comercio,
    val esPropietario: Boolean,
    val esPremium: Boolean,
    val esSocio: Boolean,
    val esBasico: Boolean,
    val fotos: List<String>,
    val telefonoLimpio: String,
    val telefonoUrl: String,
    val whatsappUrl: String,
    val mapsUrl: String,
    val sitioWebUrl: String?,
    val precioTexto: String,
    val promedioNumericoTexto: String?,
    val totalResenas: Int
) {
    companion object {
        fun desde(
            datos: Comercio,
            resenas: List<Resena>,
            usuarioActualUid: String?
        ): DetalleExperienciaDatosUi {
            val esPropietario = datos.propietarioUid == usuarioActualUid

            val esPremium = datos.planInt == 3
            val esSocio = datos.planInt == 2
            val esBasico = datos.planInt == 1

            // Límite de fotos: 8 para Premium, 4 para Socio, 2 para Básico
            val fotosReales = datos.imagenUrls.filter {
                it.startsWith("http://") || it.startsWith("https://")
            }
            val fotos = fotosReales.take(datos.maxFotosPermitidas)

            val telefonoLimpio = datos.telefono.filter { it.isDigit() }
            val telefonoUrl = "tel:${datos.telefono.filter { it.isDigit() || it == '+' }}"
            val whatsappUrl = if (telefonoLimpio.startsWith("503")) {
                "https://wa.me/$telefonoLimpio"
            } else {
                "https://wa.me/503$telefonoLimpio"
            }
            val direccionConsulta = datos.direccion.ifBlank { "${datos.nombre}, ${datos.lugarId}" }
            val mapsUrl = "https://www.google.com/maps/search/?api=1&query=" +
                direccionConsulta.replace(" ", "+")
            val sitioWebUrl = datos.sitioWeb.takeIf { it.isNotBlank() }?.let { web ->
                if (web.startsWith("http")) web else "https://$web"
            }

            val precioTexto = if (datos.precioDesde != null && datos.precioDesde > 0) {
                val pStr = datos.precioDesde.toString()
                if (pStr.contains(".")) {
                    val partes = pStr.split(".")
                    "$${partes[0]}.${partes[1].padEnd(2, '0').take(2)}"
                } else {
                    "$$pStr.00"
                }
            } else {
                "$3.00"
            }

            val totalResenasReal = if (resenas.isNotEmpty()) resenas.size else (datos.cantidadResenas ?: 0)
            val promedioReal = if (resenas.isNotEmpty()) {
                resenas.map { it.calificacion }.average()
            } else {
                datos.calificacionPromedio?.takeIf { it > 0.0 } ?: 0.0
            }

            val promedioNumericoTexto = if (promedioReal > 0.0) {
                val red = kotlin.math.round(promedioReal * 10) / 10
                "$red"
            } else {
                null
            }

            return DetalleExperienciaDatosUi(
                comercio = datos,
                esPropietario = esPropietario,
                esPremium = esPremium,
                esSocio = esSocio,
                esBasico = esBasico,
                fotos = fotos,
                telefonoLimpio = telefonoLimpio,
                telefonoUrl = telefonoUrl,
                whatsappUrl = whatsappUrl,
                mapsUrl = mapsUrl,
                sitioWebUrl = sitioWebUrl,
                precioTexto = precioTexto,
                promedioNumericoTexto = promedioNumericoTexto,
                totalResenas = totalResenasReal
            )
        }
    }
}

internal fun validarCalificacionResena(calificacion: Int): StringResource? =
    if (calificacion !in 1..5) Res.string.detalle_exp_error_calificacion_invalida else null

internal fun sanitizarComentarioResena(comentario: String): String = comentario.take(1000)

internal class DetalleExperienciaState(
    private val comercioId: String,
    private val scope: CoroutineScope
) {
    var comercio by mutableStateOf<Comercio?>(null)
    var resenas by mutableStateOf<List<Resena>>(emptyList())
    var cargando by mutableStateOf(true)
    var error by mutableStateOf<String?>(null)
    var intento by mutableIntStateOf(0)
    var calificacion by mutableIntStateOf(0)
    var comentario by mutableStateOf("")
    var enviando by mutableStateOf(false)
    var mensaje by mutableStateOf<String?>(null)
    var mostrarModalMenu by mutableStateOf(false)

    val tieneUsuarioAutenticado: Boolean
        get() = AutenticacionRepository.usuarioActual != null

    val usuarioActualUid: String?
        get() = AutenticacionRepository.usuarioActual?.uid

    fun reintentar() {
        intento++
    }

    fun actualizarComentario(nuevoComentario: String) {
        comentario = sanitizarComentarioResena(nuevoComentario)
    }

    fun toggleFavorito(esFavoritoActual: Boolean) {
        scope.launch {
            try {
                UsuarioRepository.cambiarFavorito(comercioId, !esFavoritoActual)
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (e: Exception) {
                mensaje = e.message ?: getString(Res.string.guia_error_actualizar_favorito)
            }
        }
    }

    fun publicarResena(datosId: String) {
        val errorValidacion = validarCalificacionResena(calificacion)
        if (errorValidacion != null) {
            scope.launch {
                mensaje = getString(errorValidacion)
            }
        } else {
            enviando = true
            mensaje = null
            scope.launch {
                try {
                    ComercioRepository.enviarResena(datosId, calificacion, comentario)
                    intento++
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
}

@Composable
internal fun rememberDetalleExperienciaState(comercioId: String): Pair<DetalleExperienciaState, Boolean> {
    val scope = rememberCoroutineScope()
    val state = remember(comercioId) { DetalleExperienciaState(comercioId, scope) }

    val usuarioActual by UsuarioRepository.observarUsuarioActual().collectAsState(initial = null)
    val esFavorito = remember(usuarioActual?.favoritos, comercioId) {
        usuarioActual?.favoritos?.any { it == comercioId || it == "comercio:$comercioId" } == true
    }

    LaunchedEffect(comercioId, state.intento) {
        state.cargando = true
        state.error = null
        try {
            state.comercio = ComercioRepository.obtenerPorId(comercioId)
                ?.takeIf { it.estado == "publicado" }
                ?: throw IllegalStateException(getString(Res.string.detalle_exp_error_no_disponible))
            state.resenas = ComercioRepository.obtenerResenas(comercioId)
            val propia = state.resenas.firstOrNull {
                it.usuarioUid == AutenticacionRepository.usuarioActual?.uid
            }
            state.calificacion = propia?.calificacion ?: 0
            state.comentario = propia?.comentario.orEmpty()
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (excepcion: Exception) {
            state.error = excepcion.message ?: getString(Res.string.detalle_exp_error_cargar)
        } finally {
            state.cargando = false
        }
    }

    return state to esFavorito
}
