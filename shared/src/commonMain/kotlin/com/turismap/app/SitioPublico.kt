package com.turismap.app

data class SitioPublico(
    val id: String,
    val nombre: String,
    val municipio: String,
    val descripcion: String,
    val latitud: Double,
    val longitud: Double,
    val categoria: String,
    val icono: String = "🏛️"
)

val SITIOS_PUBLICOS_RUTA = listOf(
    SitioPublico(
        id = "ataco_parque",
        nombre = "Parque Central de Ataco",
        municipio = "Concepción de Ataco",
        descripcion = "Plaza colonial rodeada de telares, cafeterías y faroles tradicionales.",
        latitud = 13.8698,
        longitud = -89.8488,
        categoria = "Plaza Pública",
        icono = "🌳"
    ),
    SitioPublico(
        id = "juayua_iglesia",
        nombre = "Plaza e Iglesia Santa Lucía",
        municipio = "Juayúa",
        descripcion = "Corazón histórico de la ciudad y sede del festival gastronómico.",
        latitud = 13.8415,
        longitud = -89.7505,
        categoria = "Cultura",
        icono = "⛪"
    ),
    SitioPublico(
        id = "apaneca_mirador",
        nombre = "Mirador de la Cruz",
        municipio = "Apaneca",
        descripcion = "Punto panorámico con vista amplia a la cordillera cafetalera.",
        latitud = 13.8595,
        longitud = -89.8025,
        categoria = "Mirador",
        icono = "🌄"
    ),
    SitioPublico(
        id = "nahuizalco_mercado",
        nombre = "Mercado Nocturno",
        municipio = "Nahuizalco",
        descripcion = "Mercado tradicional nocturno iluminado con velas artesanales.",
        latitud = 13.7778,
        longitud = -89.7369,
        categoria = "Tradición",
        icono = "🕯️"
    )
)