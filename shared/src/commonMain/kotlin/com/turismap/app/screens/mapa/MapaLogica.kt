package com.turismap.app.screens.mapa

import com.turismap.app.data.CoordenadasRuta
import com.turismap.app.data.models.Coordenada
import com.turismap.app.data.models.Lugar
import com.turismap.app.data.repository.Comercio
import com.turismap.app.data.repository.esCoordenadaValida
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin

private const val PRECISION_COLISION_COORDENADAS = 20_000.0 // ~5.5 m
private const val RADIO_BASE_ANILLO_GRADOS = 0.00055 // ~61 m
private const val INCREMENTO_RADIO_ANILLO_GRADOS = 0.00050 // ~55 m por anillo adicional
private const val CAPACIDAD_BASE_ANILLO = 8

internal fun normalizarTextoMapa(texto: String): String {
    val limpio = texto.trim().lowercase()
    if (limpio.isEmpty()) return ""

    val builder = StringBuilder(limpio.length)
    var espacioPendiente = false
    for (caracter in limpio) {
        if (caracter.isWhitespace()) {
            if (builder.isNotEmpty()) {
                espacioPendiente = true
            }
            continue
        }
        if (espacioPendiente) {
            builder.append(' ')
            espacioPendiente = false
        }
        val normalizado = when (caracter) {
            'á', 'à', 'ä', 'â' -> 'a'
            'é', 'è', 'ë', 'ê' -> 'e'
            'í', 'ì', 'ï', 'î' -> 'i'
            'ó', 'ò', 'ö', 'ô' -> 'o'
            'ú', 'ù', 'ü', 'û' -> 'u'
            'ñ' -> 'n'
            else -> caracter
        }
        builder.append(normalizado)
    }
    return builder.toString()
}

internal fun coincideBusquedaMapa(consulta: String, vararg campos: String): Boolean {
    val consultaNorm = normalizarTextoMapa(consulta)
    if (consultaNorm.isEmpty()) return true

    val textoCombinado = campos
        .asSequence()
        .map(::normalizarTextoMapa)
        .filter(String::isNotEmpty)
        .joinToString(" ")
    if (textoCombinado.isEmpty()) return false
    if (textoCombinado.contains(consultaNorm)) return true

    val tokens = consultaNorm.split(' ').filter(String::isNotEmpty)
    return tokens.isNotEmpty() && tokens.all { token -> textoCombinado.contains(token) }
}

internal fun relevanciaBusquedaMapa(
    consulta: String,
    nombre: String,
    vararg camposSecundarios: String
): Int {
    val consultaNorm = normalizarTextoMapa(consulta)
    if (consultaNorm.isEmpty()) return 0

    val nombreNorm = normalizarTextoMapa(nombre)
    if (nombreNorm.startsWith(consultaNorm)) return 0
    if (nombreNorm.contains(consultaNorm)) return 1

    val tokens = consultaNorm.split(' ').filter(String::isNotEmpty)
    if (tokens.isNotEmpty() && tokens.all { token -> nombreNorm.contains(token) }) return 2

    val camposCombinados = listOf(nombreNorm) + camposSecundarios.map(::normalizarTextoMapa)
    val textoTotal = camposCombinados.filter(String::isNotEmpty).joinToString(" ")
    if (textoTotal.contains(consultaNorm)) return 3
    if (tokens.isNotEmpty() && tokens.all { token -> textoTotal.contains(token) }) return 4

    return Int.MAX_VALUE
}

internal fun esMarcadorComercio(lugar: Lugar): Boolean =
    lugar.id.startsWith("comercio:")

internal fun buscarMunicipioDeComercio(
    comercio: Comercio,
    municipiosMap: Map<String, Lugar>
): Lugar? {
    val clavesCandidatas = listOf(comercio.lugarId, comercio.municipio)
        .map(String::trim)
        .filter(String::isNotEmpty)
        .distinct()
    if (clavesCandidatas.isEmpty()) return null

    clavesCandidatas.forEach { claveExacta ->
        municipiosMap[claveExacta]?.let { return it }
    }

    for (clave in clavesCandidatas) {
        val claveNormalizada = normalizarTextoMapa(clave)
        municipiosMap.values.firstOrNull { lugar ->
            val idNorm = normalizarTextoMapa(lugar.id)
            val nombreNorm = normalizarTextoMapa(lugar.nombre)
            (idNorm.isNotEmpty() && (idNorm == claveNormalizada || claveNormalizada.contains(idNorm))) ||
                (nombreNorm.isNotEmpty() && (nombreNorm == claveNormalizada || nombreNorm.contains(claveNormalizada)))
        }?.let { return it }
    }

    return null
}

internal fun resolverCoordenadasComercio(
    comercio: Comercio,
    municipiosMap: Map<String, Lugar>
): Coordenada {
    comercio.coordenadaPropiaValida()?.let { return it }

    val municipio = buscarMunicipioDeComercio(comercio, municipiosMap)
    val coordenadaMunicipio = municipio?.ubicacionCoordenadas
    if (coordenadaMunicipio != null && esCoordenadaValida(coordenadaMunicipio)) {
        return coordenadaMunicipio
    }

    val ref = normalizarTextoMapa(
        listOf(municipio?.nombre.orEmpty(), comercio.municipio, comercio.lugarId, comercio.direccion)
            .joinToString(" ")
    )
    return CoordenadasRuta.buscar(ref)
        ?.let { Coordenada(latitud = it.latitud, longitud = it.longitud) }
        ?: Coordenada(0.0, 0.0)
}

private fun desplazarEnAnillo(
    base: Coordenada,
    indiceEnGrupo: Int,
    totalEnGrupo: Int
): Coordenada {
    var restante = indiceEnGrupo
    var anillo = 0
    var capacidadAnillo = CAPACIDAD_BASE_ANILLO
    var procesadosAntes = 0

    while (restante >= capacidadAnillo) {
        restante -= capacidadAnillo
        procesadosAntes += capacidadAnillo
        anillo++
        capacidadAnillo = CAPACIDAD_BASE_ANILLO + anillo * 4
    }

    val cantidadEnEsteAnillo = minOf(capacidadAnillo, totalEnGrupo - procesadosAntes).coerceAtLeast(1)
    val radio = RADIO_BASE_ANILLO_GRADOS + anillo * INCREMENTO_RADIO_ANILLO_GRADOS
    val desfaseAngular = (PI / 4.0) + (anillo * (PI / cantidadEnEsteAnillo))
    val angulo = desfaseAngular + (2.0 * PI * restante / cantidadEnEsteAnillo)
    val factorCosLat = cos(base.latitud * PI / 180.0).coerceAtLeast(0.2)

    val nuevaLat = (base.latitud + radio * sin(angulo)).coerceIn(-90.0, 90.0)
    val nuevaLng = (base.longitud + (radio * cos(angulo)) / factorCosLat).coerceIn(-180.0, 180.0)
    return Coordenada(latitud = nuevaLat, longitud = nuevaLng)
}

internal fun separarCoordenadasSuperpuestas(lugares: List<Lugar>): List<Lugar> {
    if (lugares.size <= 1) return lugares

    val gruposPorPosicion = lugares.withIndex().groupBy { (_, lugar) ->
        Pair(
            round(lugar.ubicacionCoordenadas.latitud * PRECISION_COLISION_COORDENADAS).toLong(),
            round(lugar.ubicacionCoordenadas.longitud * PRECISION_COLISION_COORDENADAS).toLong()
        )
    }

    val resultado = lugares.toMutableList()

    gruposPorPosicion.values.forEach { grupo ->
        if (grupo.size <= 1) return@forEach

        val destinosOrdenados = grupo.filterNot { esMarcadorComercio(it.value) }.sortedBy { it.value.id }
        val comerciosOrdenados = grupo.filter { esMarcadorComercio(it.value) }.sortedBy { it.value.id }

        if (destinosOrdenados.isNotEmpty()) {
            // El destino/municipio principal permanece fijo en su coordenada original
            val centroAncla = destinosOrdenados.first().value.ubicacionCoordenadas
            val elementosADistribuir = comerciosOrdenados + destinosOrdenados.drop(1)
            elementosADistribuir.forEachIndexed { posicion, item ->
                val coordenadaSeparada = desplazarEnAnillo(
                    base = centroAncla,
                    indiceEnGrupo = posicion,
                    totalEnGrupo = elementosADistribuir.size
                )
                resultado[item.index] = item.value.copy(ubicacionCoordenadas = coordenadaSeparada)
            }
        } else if (comerciosOrdenados.size > 1) {
            val centroBase = comerciosOrdenados.first().value.ubicacionCoordenadas
            comerciosOrdenados.forEachIndexed { posicion, item ->
                val coordenadaSeparada = desplazarEnAnillo(
                    base = centroBase,
                    indiceEnGrupo = posicion,
                    totalEnGrupo = comerciosOrdenados.size
                )
                resultado[item.index] = item.value.copy(ubicacionCoordenadas = coordenadaSeparada)
            }
        }
    }

    return resultado
}

internal fun codificarParametroMaps(valor: String): String =
    valor.encodeToByteArray().joinToString("") { byte ->
        val caracter = byte.toInt() and 0xFF
        if (caracter in 'a'.code..'z'.code ||
            caracter in 'A'.code..'Z'.code ||
            caracter in '0'.code..'9'.code ||
            caracter == '-'.code || caracter == '_'.code ||
            caracter == '.'.code || caracter == '~'.code
        ) {
            caracter.toChar().toString()
        } else {
            "%" + caracter.toString(16).uppercase().padStart(2, '0')
        }
    }

internal fun urlGoogleMaps(comercio: Comercio?, lugar: Lugar, municipio: String?): String {
    val coordenadaValida = comercio?.coordenadaPropiaValida()
        ?: lugar.ubicacionCoordenadas.takeIf(::esCoordenadaValida)
    if (coordenadaValida != null) {
        return "https://www.google.com/maps/dir/?api=1&destination=${coordenadaValida.latitud},${coordenadaValida.longitud}"
    }

    val destino = if (comercio == null) {
        listOf(lugar.nombre, lugar.ubicacionCorta, "El Salvador")
    } else {
        listOf(comercio.nombre, comercio.direccion, municipio, "El Salvador")
    }.filterNotNull().filter(String::isNotBlank).joinToString(", ")

    return "https://www.google.com/maps/dir/?api=1&destination=${codificarParametroMaps(destino)}"
}
