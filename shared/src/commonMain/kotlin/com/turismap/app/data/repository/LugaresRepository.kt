package com.turismap.app.data.repository

import com.turismap.app.data.models.Lugar

// ==========================================
// 2. EL REPOSITORIO (Gestor de Base de Datos)
// ==========================================

// Contrato KMP puro - implementación en Android
expect object LugaresRepository {
    suspend fun obtenerTodos(): List<Lugar>
    suspend fun obtenerPorId(id: String): Lugar?
}