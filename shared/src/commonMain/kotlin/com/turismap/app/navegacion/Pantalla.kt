package com.turismap.app.navegacion

sealed class Pantalla(val ruta: String) {
    object Explorar : Pantalla("explorar")
    object Mapa : Pantalla("mapa")
    object Guia : Pantalla("guia")
    object Perfil : Pantalla("perfil")
    
    object DetalleAtaco : Pantalla("detalle_ataco")
    object DetalleApaneca : Pantalla("detalle_apaneca")
    object DetalleJuayua : Pantalla("detalle_juayua")
    object DetalleSalcoatitan : Pantalla("detalle_salcoatitan")
    object DetalleNahuizalco : Pantalla("detalle_nahuizalco")
    
    object DetalleExperiencia : Pantalla("detalle_experiencia")
    object InicioSesion : Pantalla("inicio_sesion")
}
