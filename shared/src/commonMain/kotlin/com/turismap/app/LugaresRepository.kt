package com.turismap.app

object LugaresRepository {
    fun obtenerLugaresAtaco(): List<LugarTuristico> {
        return listOf(
            LugarTuristico(
                id = "1",
                nombre = "Mirador de la Cruz de Ataco",
                categoria = CategoriaLugar.MIRADOR,
                descripcion = "Vista panoramica del pueblo y montanas cafetaleras.",
                ubicacion = Coordenada(13.8715, -89.8458),
                calificacion = 4.8
            ),
            LugarTuristico(
                id = "2",
                nombre = "Cafeteria El Carmen",
                categoria = CategoriaLugar.CAFE,
                descripcion = "Cafe de altura y recorridos sobre el proceso tradicional del cafe.",
                ubicacion = Coordenada(13.8682, -89.8495),
                calificacion = 4.7
            ),
            LugarTuristico(
                id = "3",
                nombre = "Mercado de Artesanias",
                categoria = CategoriaLugar.ARTESANIA,
                descripcion = "Telares de palanca, tallados en madera y artesanias locales.",
                ubicacion = Coordenada(13.8698, -89.8479),
                calificacion = 4.6
            ),
            LugarTuristico(
                id = "4",
                nombre = "Parque Central Fray Rafael Campos",
                categoria = CategoriaLugar.TODOS,
                descripcion = "Corazon cultural rodeado de murales coloridos e iglesia principal.",
                ubicacion = Coordenada(13.8694, -89.8483),
                calificacion = 4.9
            )
        )
    }
}