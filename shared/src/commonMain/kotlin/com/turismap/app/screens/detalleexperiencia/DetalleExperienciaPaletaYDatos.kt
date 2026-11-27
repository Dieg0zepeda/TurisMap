package com.turismap.app.screens.detalleexperiencia

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.turismap.app.data.CoordenadasRuta
import com.turismap.app.data.models.Coordenada
import com.turismap.app.data.models.Lugar
import com.turismap.app.data.repository.Comercio
import com.turismap.app.data.repository.Resena
import com.turismap.app.data.repository.esCoordenadaValida
import com.turismap.app.screens.mapa.codificarParametroMaps
import com.turismap.app.theme.SuperficieBarraInferiorAdaptable
import com.turismap.app.theme.TerracotaAcento
import com.turismap.app.theme.VerdeTextoAdaptable
import org.jetbrains.compose.resources.StringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.detalle_exp_error_calificacion_invalida

internal const val MAX_COMENTARIO_RESENA = 1000
private const val MIN_DIGITOS_TELEFONO = 8

internal data class DetalleExperienciaPaleta(
    val esOscuro: Boolean,
    val fondoPantalla: Color,
    val superficieTarjeta: Color,
    val superficieContenedor: Color,
    val superficieBarraInferior: Color,
    val borde: Color,
    val textoPrincipal: Color,
    val textoSecundario: Color,
    val primario: Color,
    val sobrePrimario: Color,
    val contenedorPrimario: Color,
    val sobreContenedorPrimario: Color,
    val acentoVerdeTexto: Color,
    val terracota: Color,
    val ambarFondo: Color,
    val ambarFranjaFondo: Color,
    val ambarTexto: Color,
    val ambarAcento: Color,
    val ambarBorde: Color
)

@Composable
internal fun rememberDetalleExperienciaPaleta(): DetalleExperienciaPaleta {
    val esOscuro = isSystemInDarkTheme()
    val scheme = MaterialTheme.colorScheme
    val superficieBarra = SuperficieBarraInferiorAdaptable
    val acentoVerde = VerdeTextoAdaptable

    return remember(esOscuro, scheme, superficieBarra, acentoVerde) {
        DetalleExperienciaPaleta(
            esOscuro = esOscuro,
            fondoPantalla = scheme.background,
            superficieTarjeta = scheme.surfaceContainerLow,
            superficieContenedor = scheme.surfaceContainer,
            superficieBarraInferior = superficieBarra,
            borde = scheme.outlineVariant,
            textoPrincipal = scheme.onSurface,
            textoSecundario = scheme.onSurfaceVariant,
            primario = scheme.primary,
            sobrePrimario = scheme.onPrimary,
            contenedorPrimario = scheme.primaryContainer,
            sobreContenedorPrimario = scheme.onPrimaryContainer,
            acentoVerdeTexto = acentoVerde,
            terracota = TerracotaAcento,
            ambarFondo = if (esOscuro) Color(0xFF2D2412) else Color(0xFFFEF3C7),
            ambarFranjaFondo = if (esOscuro) Color(0xFF1E1A11) else Color(0xFFFFFBEB),
            ambarTexto = if (esOscuro) Color(0xFFF3D689) else Color(0xFF92400E),
            ambarAcento = if (esOscuro) Color(0xFFE9C46A) else Color(0xFFD97706),
            ambarBorde = if (esOscuro) Color(0xFFE9C46A).copy(alpha = 0.34f) else Color(0xFFD97706).copy(alpha = 0.30f)
        )
    }
}

internal data class DetalleExperienciaDatosUi(
    val comercio: Comercio,
    val categoriaEtiqueta: String,
    val municipioEtiqueta: String,
    val horarioLimpio: String,
    val esPropietario: Boolean,
    val esPremium: Boolean,
    val esSocio: Boolean,
    val esBasico: Boolean,
    val fotos: List<String>,
    val telefonoLimpio: String,
    val telefonoUrl: String?,
    val whatsappUrl: String?,
    val mapsUrl: String?,
    val sitioWebUrl: String?,
    val coordenadaMapa: Coordenada?,
    val zoomMapa: Float,
    val precioTexto: String?,
    val promedioNumericoTexto: String?,
    val totalResenas: Int
) {
    val mostrarWhatsapp: Boolean
        get() = (esSocio || esPremium) && whatsappUrl != null

    val mostrarLlamar: Boolean
        get() = telefonoUrl != null

    val mostrarSitioWeb: Boolean
        get() = (esSocio || esPremium) && sitioWebUrl != null

    val tieneAccionesRapidas: Boolean
        get() = mostrarWhatsapp || mostrarLlamar || mostrarSitioWeb

    companion object {
        fun desde(
            datos: Comercio,
            resenas: List<Resena>,
            usuarioActualUid: String?,
            categoriasMap: Map<String, String> = emptyMap(),
            municipiosMap: Map<String, Lugar> = emptyMap()
        ): DetalleExperienciaDatosUi {
            val esPropietario = !usuarioActualUid.isNullOrBlank() &&
                datos.propietarioUid.isNotBlank() &&
                datos.propietarioUid == usuarioActualUid

            val categoriaEtiqueta = categoriasMap[datos.categoria.trim()]
                ?.takeIf(String::isNotBlank)
                ?: datos.categoria.trim()

            val lugarMunicipio = resolverLugarMunicipio(datos, municipiosMap)
            val municipioEtiqueta = lugarMunicipio?.nombre?.trim()?.takeIf(String::isNotEmpty)
                ?: datos.municipio.trim().ifBlank { datos.lugarId.trim() }

            val esPremium = datos.planInt == 3
            val esSocio = datos.planInt == 2
            val esBasico = datos.planInt == 1

            // Límite de fotos según plan: 8 Premium, 4 Socio, 2 Básico (solo HTTPS sin espacios)
            val fotos = datos.imagenUrls
                .map(String::trim)
                .filter(::esUrlFotoHttpsSegura)
                .take(datos.maxFotosPermitidas)

            val (telefonoLimpio, telefonoUrl, whatsappUrl) = resolverEnlacesTelefono(datos.telefono)

            val coordenadaPropia = datos.coordenadaPropiaValida()
            val coordenadaLugar = lugarMunicipio?.ubicacionCoordenadas?.takeIf(::esCoordenadaValida)
            val fallbackMunicipio = CoordenadasRuta.buscar(municipioEtiqueta)
                ?: CoordenadasRuta.buscar(datos.lugarId)
                ?: CoordenadasRuta.buscar(datos.municipio)
                ?: CoordenadasRuta.buscar(datos.direccion)

            val coordenadaMapa = coordenadaPropia
                ?: coordenadaLugar
                ?: fallbackMunicipio?.let { Coordenada(it.latitud, it.longitud) }
            val zoomMapa = when {
                coordenadaPropia != null -> 16.5f
                coordenadaLugar != null -> 15.0f
                else -> fallbackMunicipio?.zoom ?: 15.0f
            }

            val consultaMaps = if (coordenadaPropia != null) {
                "${coordenadaPropia.latitud},${coordenadaPropia.longitud}"
            } else {
                datos.direccion.trim().ifBlank {
                    listOf(datos.nombre, municipioEtiqueta)
                        .map(String::trim)
                        .filter(String::isNotEmpty)
                        .joinToString(", ")
                }
            }
            val mapsUrl = consultaMaps.takeIf(String::isNotBlank)?.let { consulta ->
                "https://www.google.com/maps/search/?api=1&query=" + codificarParametroMaps(consulta)
            }
            val sitioWebUrl = validarSitioWebSeguro(datos.sitioWeb)
            val precioTexto = formatearPrecioSeguro(datos.precioDesde)

            val resenasValidas = resenas.filter { it.calificacion in 1..5 }
            val totalResenasReal = if (resenasValidas.isNotEmpty()) {
                resenasValidas.size
            } else {
                (datos.cantidadResenas ?: 0).coerceAtLeast(0)
            }
            val promedioReal = if (resenasValidas.isNotEmpty()) {
                resenasValidas.map { it.calificacion }.average()
            } else {
                datos.calificacionPromedio?.takeIf { it.isFinite() && it > 0.0 } ?: 0.0
            }
            val promedioNumericoTexto = if (promedioReal > 0.0) {
                val red = kotlin.math.round(promedioReal * 10.0) / 10.0
                "$red"
            } else {
                null
            }

            return DetalleExperienciaDatosUi(
                comercio = datos,
                categoriaEtiqueta = categoriaEtiqueta,
                municipioEtiqueta = municipioEtiqueta,
                horarioLimpio = datos.horario.trim(),
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
                coordenadaMapa = coordenadaMapa,
                zoomMapa = zoomMapa,
                precioTexto = precioTexto,
                promedioNumericoTexto = promedioNumericoTexto,
                totalResenas = totalResenasReal
            )
        }
    }
}

private fun resolverLugarMunicipio(
    datos: Comercio,
    municipiosMap: Map<String, Lugar>
): Lugar? {
    if (municipiosMap.isEmpty()) return null
    val idLimpio = datos.lugarId.trim()
    if (idLimpio.isNotEmpty()) {
        municipiosMap[idLimpio]?.let { return it }
    }
    val municipioLimpio = datos.municipio.trim()
    val candidatas = listOf(idLimpio, municipioLimpio).filter(String::isNotEmpty)
    for (clave in candidatas) {
        municipiosMap.values.firstOrNull { lugar ->
            lugar.id.trim().equals(clave, ignoreCase = true) ||
                lugar.nombre.trim().equals(clave, ignoreCase = true)
        }?.let { return it }
    }
    return null
}

private fun esUrlFotoHttpsSegura(url: String): Boolean =
    url.startsWith("https://", ignoreCase = true) &&
        !url.any { it.isWhitespace() || it.isISOControl() }

private fun resolverEnlacesTelefono(telefonoRaw: String): Triple<String, String?, String?> {
    val limpio = telefonoRaw.trim()
    val digitos = limpio.filter(Char::isDigit)
    if (digitos.length < MIN_DIGITOS_TELEFONO) {
        return Triple(digitos, null, null)
    }
    val tieneMasInicial = limpio.startsWith("+")
    val telefonoUrl = if (tieneMasInicial) "tel:+$digitos" else "tel:$digitos"
    val digitosWhatsapp = when {
        tieneMasInicial -> digitos
        digitos.startsWith("00") && digitos.length > 10 -> digitos.drop(2)
        digitos.startsWith("503") && digitos.length >= 11 -> digitos
        digitos.length == MIN_DIGITOS_TELEFONO -> "503$digitos"
        else -> digitos
    }
    return Triple(digitos, telefonoUrl, "https://wa.me/$digitosWhatsapp")
}

private fun formatearPrecioSeguro(precioDesde: Double?): String? {
    val valor = precioDesde?.takeIf { it.isFinite() && it > 0.0 } ?: return null
    val totalCentavos = kotlin.math.round(valor * 100.0).toLong()
    val enteros = totalCentavos / 100L
    val centavos = (totalCentavos % 100L).toString().padStart(2, '0')
    return "$$enteros.$centavos"
}

internal fun validarSitioWebSeguro(sitioWeb: String): String? {
    val webLimpia = sitioWeb.trim()
    if (!webLimpia.startsWith("https://", ignoreCase = true)) return null
    if (webLimpia.any { it.isWhitespace() || it.isISOControl() }) return null
    val sinEsquema = webLimpia.drop(8)
    val host = sinEsquema
        .substringBefore('/')
        .substringBefore('?')
        .substringBefore('#')
        .substringBefore(':')
    val etiquetas = host.split('.')
    if (etiquetas.size < 2 || etiquetas.any(String::isBlank)) return null
    return "https://$sinEsquema"
}

internal fun validarCalificacionResena(calificacion: Int): StringResource? =
    if (calificacion !in 1..5) Res.string.detalle_exp_error_calificacion_invalida else null

internal fun sanitizarComentarioResena(comentario: String): String =
    comentario.take(MAX_COMENTARIO_RESENA)
