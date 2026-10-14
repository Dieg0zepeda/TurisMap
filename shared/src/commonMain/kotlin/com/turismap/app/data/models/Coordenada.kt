package com.turismap.app.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Coordenada(
    val latitud: Double = 0.0,
    val longitud: Double = 0.0
)