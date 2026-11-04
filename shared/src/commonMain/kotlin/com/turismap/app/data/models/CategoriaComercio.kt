package com.turismap.app.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class CategoriaComercio(
    @SerialName("id") val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val orden: Int = 0,
    val activo: Boolean = true
) {
    val etiqueta: String
        get() = nombre.ifBlank { descripcion.ifBlank { id } }
}
