package com.turismap.app.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Atractivo(
    val id: String = "",
    val etiqueta: String = "",
    val etiquetaColorHex: Long = 0L,
    val titulo: String = "",
    val descripcion: String = "",
    val accion: String = "",
    val imagenUrl: String = ""
)