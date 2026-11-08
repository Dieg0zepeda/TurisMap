package com.turismap.app.data

data class CoordenadaMunicipio(
    val latitud: Double,
    val longitud: Double,
    val zoom: Float = 15.0f
)

object CoordenadasRuta {
    private val coordenadas = mapOf(
        "nahuizalco" to CoordenadaMunicipio(13.7766, -89.7364),
        "salcoatitan" to CoordenadaMunicipio(13.8294, -89.7547),
        "juayua" to CoordenadaMunicipio(13.8417, -89.7456),
        "apaneca" to CoordenadaMunicipio(13.8569, -89.8031),
        "ataco" to CoordenadaMunicipio(13.8703, -89.8483),
        "concepcion_de_ataco" to CoordenadaMunicipio(13.8703, -89.8483),
        "ahuachapan" to CoordenadaMunicipio(13.9214, -89.8450),
        "el_refugio" to CoordenadaMunicipio(13.9744, -89.7214),
        "tacuba" to CoordenadaMunicipio(13.9000, -89.9333)
    )

    fun obtener(idMunicipio: String): CoordenadaMunicipio {
        val clave = idMunicipio.trim().lowercase().replace(" ", "_")
        return coordenadas[clave]
            ?: coordenadas.entries.firstOrNull { clave.contains(it.key) }?.value
            ?: CoordenadaMunicipio(13.8569, -89.8031) // Apaneca por defecto (centro de la cordillera)
    }
}