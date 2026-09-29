package com.turismap.app

enum class CategoriaLugar(val etiqueta: String) {
    TODOS("Todos"),
    CAFE("Cafeterias"),
    MIRADOR("Miradores"),
    ARTESANIA("Artesanias"),
    RESTAURANTE("Restaurantes")
}

data class Coordenada(
    val latitud: Double,
    val longitud: Double
)

data class LugarTuristico(
    val id: String,
    val nombre: String,
    val categoria: CategoriaLugar,
    val descripcion: String,
    val ubicacion: Coordenada,
    val calificacion: Double,
    val imagenUrl: String = ""
)