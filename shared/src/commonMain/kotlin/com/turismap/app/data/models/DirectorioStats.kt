package com.turismap.app.data.models

import kotlinx.serialization.Serializable

@Serializable
data class DirectorioStats(
    val tituloGeneral: String = "",
    val descripcionGeneral: String = "",
    val stat1Icon: String = "",
    val stat1Texto: String = "",
    val stat2Icon: String = "",
    val stat2Texto: String = "",
    val stat3Icon: String = "",
    val stat3Texto: String = "",
    val stat4Icon: String = "",
    val stat4Texto: String = ""
)