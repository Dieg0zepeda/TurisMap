package com.turismap.app.data.models

enum class CategoriaLugar(val etiqueta: String) {
    TODOS("Todos"),
    CAFE("Cafeterías"),
    MIRADOR("Miradores"),
    ARTESANIA("Artesanías"),
    RESTAURANTE("Restaurantes")
}

data class Coordenada(
    val latitud: Double,
    val longitud: Double
)

data class Lugar(
    val id: String,
    val nombre: String,
    val subtitulo: String,
    val categoria: CategoriaLugar, // Recuperado para el mapa
    val ubicacionCoordenadas: Coordenada, // Recuperado para el mapa
    val ubicacionCorta: String,
    val ubicacionLarga: String,
    val elevacion: String,
    val clima: String,
    val calificacion: Double,
    val cantidadResenas: Int,
    val historia: String,
    val mejorTemporadaTitulo: String,
    val mejorTemporadaDesc: String,
    val atractivos: List<Atractivo>,
    val directorioStats: DirectorioStats
)

data class Atractivo(
    val id: String,
    val etiqueta: String,
    val etiquetaColorHex: Long,
    val titulo: String,
    val descripcion: String,
    val accion: String,
    val imagenUrl: String = ""
)

data class DirectorioStats(
    val tituloGeneral: String,
    val descripcionGeneral: String,
    val stat1Icon: String,
    val stat1Texto: String,
    val stat2Icon: String,
    val stat2Texto: String,
    val stat3Icon: String,
    val stat3Texto: String,
    val stat4Icon: String,
    val stat4Texto: String
)