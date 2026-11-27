package com.turismap.app.data.models

import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.atractivo_categoria_cerro
import turismap.shared.generated.resources.atractivo_categoria_cerros
import turismap.shared.generated.resources.atractivo_categoria_laguna
import turismap.shared.generated.resources.atractivo_categoria_lagunas
import turismap.shared.generated.resources.atractivo_categoria_mirador
import turismap.shared.generated.resources.atractivo_categoria_miradores
import turismap.shared.generated.resources.atractivo_categoria_montana
import turismap.shared.generated.resources.atractivo_categoria_montanas
import turismap.shared.generated.resources.atractivo_categoria_rio
import turismap.shared.generated.resources.atractivo_categoria_rios

@Serializable
enum class CategoriaAtractivoPublico(val claveFirestore: String) {
    MONTANA("montana"),
    MIRADOR("mirador"),
    LAGUNA("laguna"),
    RIO("rio"),
    CERRO("cerro");

    val etiquetaRes: StringResource
        get() = when (this) {
            MONTANA -> Res.string.atractivo_categoria_montana
            MIRADOR -> Res.string.atractivo_categoria_mirador
            LAGUNA -> Res.string.atractivo_categoria_laguna
            RIO -> Res.string.atractivo_categoria_rio
            CERRO -> Res.string.atractivo_categoria_cerro
        }

    val etiquetaPluralRes: StringResource
        get() = when (this) {
            MONTANA -> Res.string.atractivo_categoria_montanas
            MIRADOR -> Res.string.atractivo_categoria_miradores
            LAGUNA -> Res.string.atractivo_categoria_lagunas
            RIO -> Res.string.atractivo_categoria_rios
            CERRO -> Res.string.atractivo_categoria_cerros
        }

    companion object {
        private fun normalizar(texto: String): String {
            val limpio = texto.trim().lowercase()
            if (limpio.isEmpty()) return ""
            val sb = StringBuilder(limpio.length)
            for (caracter in limpio) {
                sb.append(
                    when (caracter) {
                        'á', 'à', 'ä', 'â' -> 'a'
                        'é', 'è', 'ë', 'ê' -> 'e'
                        'í', 'ì', 'ï', 'î' -> 'i'
                        'ó', 'ò', 'ö', 'ô' -> 'o'
                        'ú', 'ù', 'ü', 'û' -> 'u'
                        'ñ' -> 'n'
                        else -> caracter
                    }
                )
            }
            return sb.toString()
        }

        fun desdeTextoOrNull(valor: String?): CategoriaAtractivoPublico? {
            val normalizado = normalizar(valor.orEmpty())
            if (normalizado.isEmpty()) return null

            entries.firstOrNull {
                it.claveFirestore == normalizado || it.name.equals(normalizado, ignoreCase = true)
            }?.let { return it }

            return when {
                normalizado.contains("montan") ||
                    normalizado.contains("mountain") ||
                    normalizado.contains("cordillera") ||
                    normalizado.contains("volcan") -> MONTANA

                normalizado.contains("mirador") ||
                    normalizado.contains("viewpoint") -> MIRADOR

                normalizado.contains("laguna") ||
                    normalizado.contains("lagoon") ||
                    normalizado.contains("lago") ||
                    normalizado.contains("lake") -> LAGUNA

                normalizado == "rio" ||
                    normalizado == "rios" ||
                    normalizado.startsWith("rio ") ||
                    normalizado.contains(" rio") ||
                    normalizado.contains("river") ||
                    normalizado.contains("cascada") ||
                    normalizado.contains("chorro") ||
                    normalizado.contains("waterfall") -> RIO

                normalizado.contains("cerro") ||
                    normalizado.contains("hill") ||
                    normalizado.contains("cumbre") -> CERRO

                else -> null
            }
        }

        fun desdeTexto(valor: String?): CategoriaAtractivoPublico =
            desdeTextoOrNull(valor) ?: MIRADOR
    }
}

@Serializable
data class AtractivoPublico(
    val id: String = "",
    val nombre: String = "",
    val categoria: String = CategoriaAtractivoPublico.MIRADOR.claveFirestore,
    val descripcion: String = "",
    val imagenes: List<String> = emptyList(),
    val coordenadas: Coordenada = Coordenada(),
    val municipio: String = "",
    val lugarId: String = "",
    val activo: Boolean = true
) {
    val categoriaResuelta: CategoriaAtractivoPublico
        get() = CategoriaAtractivoPublico.desdeTexto(categoria)

    val imagenPrincipal: String
        get() = imagenes.firstOrNull { it.isNotBlank() }.orEmpty()

    val tieneCoordenadasValidas: Boolean
        get() = coordenadas.latitud.isFinite() &&
            coordenadas.longitud.isFinite() &&
            coordenadas.latitud in -90.0..90.0 &&
            coordenadas.longitud in -180.0..180.0 &&
            coordenadas.latitud != 0.0 &&
            coordenadas.longitud != 0.0
}
