package com.turismap.app.data.repository

import com.turismap.app.data.models.CategoriaComercio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex

/**
 * Caché en memoria compartida por sesión para catálogos públicos de comercios y categorías.
 * Es segura para KMP (sin APIs de JVM/Android) y evita recargas redundantes al cambiar de pestaña.
 */
internal object ComercioCacheSesion {
    val mutexPublicados = Mutex()
    val mutexDestacados = Mutex()
    val mutexCategorias = Mutex()

    private val publicadosFlow = MutableStateFlow<List<Comercio>?>(null)
    private val publicadosPorIdFlow = MutableStateFlow<Map<String, Comercio>>(emptyMap())
    private val destacadosFlow = MutableStateFlow<List<Comercio>?>(null)
    private val categoriasFlow = MutableStateFlow<List<CategoriaComercio>?>(null)

    fun obtenerPublicados(): List<Comercio>? = publicadosFlow.value

    fun obtenerPorId(id: String): Comercio? = publicadosPorIdFlow.value[id.trim()]

    fun obtenerDestacados(): List<Comercio>? =
        destacadosFlow.value ?: publicadosFlow.value?.let(ComercioRepository::filtrarExperienciasDestacadas)

    fun obtenerCategorias(): List<CategoriaComercio>? = categoriasFlow.value

    fun guardarPublicados(comercios: List<Comercio>) {
        val copia = comercios.toList()
        publicadosFlow.value = copia
        publicadosPorIdFlow.value = copia.associateBy { it.id.trim() }
        destacadosFlow.value = ComercioRepository.filtrarExperienciasDestacadas(copia)
    }

    fun guardarDestacados(destacados: List<Comercio>) {
        val copia = destacados.toList()
        destacadosFlow.value = copia
        if (copia.isNotEmpty()) {
            publicadosPorIdFlow.value = publicadosPorIdFlow.value + copia.associateBy { it.id.trim() }
        }
    }

    fun guardarComercio(comercio: Comercio) {
        val clave = comercio.id.trim()
        if (clave.isEmpty()) return
        publicadosPorIdFlow.value = publicadosPorIdFlow.value + (clave to comercio)
    }

    fun guardarCategorias(categorias: List<CategoriaComercio>) {
        categoriasFlow.value = categorias.toList()
    }

    fun limpiar() {
        publicadosFlow.value = null
        publicadosPorIdFlow.value = emptyMap()
        destacadosFlow.value = null
        categoriasFlow.value = null
    }
}
