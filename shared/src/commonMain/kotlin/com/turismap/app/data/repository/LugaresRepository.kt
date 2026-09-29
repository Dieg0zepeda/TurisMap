package com.turismap.app.data.repository

import com.turismap.app.data.models.Atractivo
import com.turismap.app.data.models.CategoriaLugar
import com.turismap.app.data.models.Coordenada
import com.turismap.app.data.models.DirectorioStats
import com.turismap.app.data.models.Lugar

object LugaresRepository {
    fun obtenerTodos(): List<Lugar> {
        return listOf(
            Lugar(
                id = "apaneca",
                nombre = "Apaneca",
                subtitulo = "La cumbre más fresca de la cordillera y senderos de niebla",
                categoria = CategoriaLugar.TODOS,
                ubicacionCoordenadas = Coordenada(13.8569, -89.7997), // Coordenadas aproximadas
                ubicacionCorta = "Ruta de las Flores · CA-8 Km 87",
                ubicacionLarga = "Ahuachapán Sur · El Salvador",
                elevacion = "1,470 msnm",
                clima = "16°C · Clima de Montaña",
                calificacion = 4.9,
                cantidadResenas = 0,
                historia = "El municipio más alto de la Ruta de las Flores, cuna de vientos frescos que acarician lagunas en cráteres extintos y fincas de café de estirpe mundial entre bosques de cipreses.",
                mejorTemporadaTitulo = "MEJOR TEMPORADA",
                mejorTemporadaDesc = "Noviembre a Marzo · Clima despejado y vientos alisios",
                atractivos = listOf(
                    Atractivo("1", "Aventura & Ocio", 0xFF1B4934, "Laberinto de Cipreses", "El laberinto natural más grande de Centroamérica en Finca Albania.", "Ver atracción →"),
                    Atractivo("2", "Senderismo", 0xFF1B4934, "Laguna Verde", "Cráter volcánico rodeado de senderos ecológicos y leyendas locales.", "Ver recorrido →")
                ),
                directorioStats = DirectorioStats(
                    "DIRECTORIO LOCAL ACTIVO", "Explora 32 alojamientos de montaña, cafeterías de café bourbon y centros de aventura certificados.",
                    "LocalCafe", "11 Cafés de\nAltura", "Home", "9 Glampings &\nCabañas",
                    "Restaurant", "7 Restaurantes", "DirectionsWalk", "5 Operadores"
                )
            ),
            Lugar(
                id = "ataco",
                nombre = "Concepción de Ataco",
                subtitulo = "Cuna de telares artesanales y café de altura",
                categoria = CategoriaLugar.TODOS,
                ubicacionCoordenadas = Coordenada(13.8697, -89.8486), // Coordenadas aproximadas
                ubicacionCorta = "Ruta de las Flores · CA-8 Km 92",
                ubicacionLarga = "Ahuachapán Sur · El Salvador",
                elevacion = "1,260 msnm",
                clima = "19°C · Fresco",
                calificacion = 4.9,
                cantidadResenas = 1240,
                historia = "Famoso por sus calles empedradas adornadas con murales vibrantes y la calidez de sus telares de palanca tradicionales.",
                mejorTemporadaTitulo = "MEJOR TEMPORADA",
                mejorTemporadaDesc = "Octubre a Febrero · Clima de montaña y cosecha",
                atractivos = listOf(
                    Atractivo("3", "Arte Colonial", 0xFFC65A42, "Murales & Empedrado", "Fachadas llenas de folclor popular y pasajes fotogénicos.", "Ver recorrido →"),
                    Atractivo("4", "Naturaleza", 0xFF1B4934, "Fincas & Miradores", "Cata de café de especialidad y vistas a la serranía.", "Ver fincas →")
                ),
                directorioStats = DirectorioStats(
                    "DIRECTORIO COMUNITARIO", "Explora 38 alojamientos con encanto, cafeterías y talleres verificados.",
                    "LocalCafe", "14 Cafés\nEspeciales", "Home", "12 Hoteles\nBoutique",
                    "Restaurant", "8 Restaurantes\nTípicos", "Brush", "4 Talleres\nVivos"
                )
            )
        )
    }

    fun obtenerPorId(id: String): Lugar? {
        return obtenerTodos().find { it.id == id }
    }
}