package com.turismap.app.screens.mapa

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterHdr
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.Waves
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.turismap.app.data.models.AtractivoPublico as AtractivoPublicoModelo
import com.turismap.app.data.models.CategoriaAtractivoPublico
import com.turismap.app.data.models.Coordenada
import com.turismap.app.data.models.Lugar
import com.turismap.app.data.repository.Comercio as ComercioModelo

sealed interface MarcadorMapa {
    val idUnico: String
    val titulo: String
    val subtitulo: String
    val coordenadas: Coordenada

    fun conCoordenadas(nuevasCoordenadas: Coordenada): MarcadorMapa

    data class Destino(
        val lugar: Lugar,
        override val coordenadas: Coordenada = lugar.ubicacionCoordenadas
    ) : MarcadorMapa {
        override val idUnico: String
            get() = "destino:${lugar.id}"
        override val titulo: String
            get() = lugar.nombre
        override val subtitulo: String
            get() = lugar.subtitulo

        override fun conCoordenadas(nuevasCoordenadas: Coordenada): Destino =
            copy(coordenadas = nuevasCoordenadas)
    }

    data class Comercio(
        val comercio: ComercioModelo,
        val etiquetaCategoria: String,
        val nombreMunicipio: String,
        override val coordenadas: Coordenada
    ) : MarcadorMapa {
        override val idUnico: String
            get() = "comercio:${comercio.id}"
        override val titulo: String
            get() = comercio.nombre
        override val subtitulo: String
            get() = etiquetaCategoria

        override fun conCoordenadas(nuevasCoordenadas: Coordenada): Comercio =
            copy(coordenadas = nuevasCoordenadas)
    }

    data class AtractivoPublico(
        val atractivo: AtractivoPublicoModelo,
        override val coordenadas: Coordenada = atractivo.coordenadas
    ) : MarcadorMapa {
        val categoria: CategoriaAtractivoPublico
            get() = atractivo.categoriaResuelta
        override val idUnico: String
            get() = "atractivo:${atractivo.id}"
        override val titulo: String
            get() = atractivo.nombre
        override val subtitulo: String
            get() = atractivo.municipio

        override fun conCoordenadas(nuevasCoordenadas: Coordenada): AtractivoPublico =
            copy(coordenadas = nuevasCoordenadas)
    }
}

internal fun colorCategoriaAtractivo(categoria: CategoriaAtractivoPublico): Color =
    when (categoria) {
        CategoriaAtractivoPublico.MONTANA -> Color(0xFF2D6A4F)
        CategoriaAtractivoPublico.MIRADOR -> Color(0xFFC88A1E)
        CategoriaAtractivoPublico.LAGUNA -> Color(0xFF0E7490)
        CategoriaAtractivoPublico.RIO -> Color(0xFF0284C7)
        CategoriaAtractivoPublico.CERRO -> Color(0xFF9A5B25)
    }

internal fun iconoCategoriaAtractivo(categoria: CategoriaAtractivoPublico): ImageVector =
    when (categoria) {
        CategoriaAtractivoPublico.MONTANA -> Icons.Default.Terrain
        CategoriaAtractivoPublico.MIRADOR -> Icons.Default.Visibility
        CategoriaAtractivoPublico.LAGUNA -> Icons.Default.Water
        CategoriaAtractivoPublico.RIO -> Icons.Default.Waves
        CategoriaAtractivoPublico.CERRO -> Icons.Default.FilterHdr
    }
