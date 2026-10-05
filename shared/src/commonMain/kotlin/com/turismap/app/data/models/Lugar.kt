package com.turismap.app.data.models

data class Lugar(
    val id: String = "",
    val nombre: String = "",
    val subtitulo: String = "",
    val categoria: CategoriaLugar = CategoriaLugar.TODOS,
    val ubicacionCoordenadas: Coordenada = Coordenada(),
    val ubicacionCorta: String = "",
    val ubicacionLarga: String = "",
    val elevacion: String = "",
    val clima: String = "",
    val calificacion: Double = 0.0,
    val cantidadResenas: Int = 0,
    val historia: String = "",
    val mejorTemporadaTitulo: String = "",
    val mejorTemporadaDesc: String = "",
    val atractivos: List<Atractivo> = emptyList(),
    val directorioStats: DirectorioStats = DirectorioStats()
)