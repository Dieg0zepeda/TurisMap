package com.turismap.app.data.repository

import com.turismap.app.data.models.Lugar

// Implementación temporal vacía para que iOS compile sin errores por ahora
actual object LugaresRepository {
    actual suspend fun obtenerTodos(): List<Lugar> = emptyList()
    actual suspend fun obtenerPorId(id: String): Lugar? = null
}