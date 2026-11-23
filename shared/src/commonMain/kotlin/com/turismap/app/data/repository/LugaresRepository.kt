package com.turismap.app.data.repository

import com.turismap.app.data.models.Lugar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex

/**
 * Caché en memoria compartida de sesión para la colección pública `lugares`.
 */
internal object LugaresCache {
    val mutex = Mutex()
    private val lugaresFlow = MutableStateFlow<List<Lugar>?>(null)
    private val porIdFlow = MutableStateFlow<Map<String, Lugar>>(emptyMap())

    fun obtenerLista(): List<Lugar>? = lugaresFlow.value

    fun obtenerPorId(id: String): Lugar? = porIdFlow.value[id.trim()]

    fun guardarLista(nuevosLugares: List<Lugar>) {
        val copia = nuevosLugares.toList()
        lugaresFlow.value = copia
        porIdFlow.value = copia.associateBy { it.id.trim() }
    }

    fun guardarLugar(lugar: Lugar) {
        val clave = lugar.id.trim()
        if (clave.isEmpty()) return
        porIdFlow.value = porIdFlow.value + (clave to lugar)
        val listaActual = lugaresFlow.value ?: return
        val indice = listaActual.indexOfFirst { it.id.trim() == clave }
        lugaresFlow.value = if (indice >= 0) {
            listaActual.toMutableList().apply { this[indice] = lugar }
        } else {
            listaActual + lugar
        }
    }

    fun limpiar() {
        lugaresFlow.value = null
        porIdFlow.value = emptyMap()
    }
}

// Contrato KMP puro - implementaciones en androidMain e iosMain
expect object LugaresRepository {
    val lugaresEnCache: List<Lugar>?
    fun obtenerEnCachePorId(id: String): Lugar?
    suspend fun obtenerTodos(forzarRecarga: Boolean = false): List<Lugar>
    suspend fun obtenerPorId(id: String, forzarRecarga: Boolean = false): Lugar?
    fun limpiarCache()
}