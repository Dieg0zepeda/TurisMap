package com.turismap.app.data.models

import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.categoria_lugar_artesanias
import turismap.shared.generated.resources.categoria_lugar_cafeterias
import turismap.shared.generated.resources.categoria_lugar_miradores
import turismap.shared.generated.resources.categoria_lugar_restaurantes
import turismap.shared.generated.resources.categoria_lugar_todos

@Serializable
enum class CategoriaLugar {
    TODOS,
    CAFE,
    MIRADOR,
    ARTESANIA,
    RESTAURANTE;

    val etiquetaRes: StringResource
        get() = when (this) {
            TODOS -> Res.string.categoria_lugar_todos
            CAFE -> Res.string.categoria_lugar_cafeterias
            MIRADOR -> Res.string.categoria_lugar_miradores
            ARTESANIA -> Res.string.categoria_lugar_artesanias
            RESTAURANTE -> Res.string.categoria_lugar_restaurantes
        }
}