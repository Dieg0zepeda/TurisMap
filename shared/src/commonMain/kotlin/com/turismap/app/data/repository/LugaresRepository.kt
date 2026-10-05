package com.turismap.app.data.repository

import com.turismap.app.data.models.Lugar

// Contrato KMP puro, sin importaciones de Firebase ni dependencias nativas
expect object LugaresRepository {
    suspend fun obtenerTodos(): List<Lugar>
    suspend fun obtenerPorId(id: String): Lugar?
}