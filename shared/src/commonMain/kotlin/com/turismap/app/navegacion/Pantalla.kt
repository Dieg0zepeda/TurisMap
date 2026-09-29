package com.turismap.app.navegacion

sealed class Pantalla(val ruta: String) {
    object Explorar : Pantalla("explorar")
    object Mapa : Pantalla("mapa")
    object Guia : Pantalla("guia")
    object Perfil : Pantalla("perfil")

    // RUTA DINÁMICA
    object DetalleLugar : Pantalla("detalle_lugar/{lugarId}") {
        fun crearRuta(lugarId: String) = "detalle_lugar/$lugarId"
    }

    object DetalleExperiencia : Pantalla("detalle_experiencia")
    object InicioSesion : Pantalla("inicio_sesion")
}