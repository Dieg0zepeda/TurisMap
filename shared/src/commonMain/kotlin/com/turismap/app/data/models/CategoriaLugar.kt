package com.turismap.app.data.models

import kotlinx.serialization.Serializable

@Serializable
enum class CategoriaLugar(val etiqueta: String) {
    TODOS("Todos"),
    CAFE("Cafeterías"),
    MIRADOR("Miradores"),
    ARTESANIA("Artesanías"),
    RESTAURANTE("Restaurantes")
}