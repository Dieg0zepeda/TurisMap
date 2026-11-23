package com.turismap.app.screens.perfil

import com.turismap.app.data.models.CategoriaComercio
import com.turismap.app.data.models.Lugar
import com.turismap.app.data.repository.Comercio
import com.turismap.app.data.repository.ComercioRepository
import com.turismap.app.data.repository.LugaresRepository
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.resources.StringResource

internal enum class CategoriaFavorito {
    TODOS,
    DESTINOS,
    COMERCIOS
}

internal data class ItemFavoritoUi(
    val id: String,
    val idFavoritoOriginal: String,
    val nombre: String,
    val categoria: String = "",
    val categoriaRes: StringResource? = null,
    val imagenUrl: String,
    val calificacion: Double?,
    val esComercio: Boolean
)

/**
 * Caché en memoria de favoritos resueltos, aislada por UID para limpiar limpiamente al cerrar sesión
 * o cambiar de cuenta y evitar parpadeos del contador al entrar en Perfil.
 */
internal object PerfilCacheSesion {
    private data class EntradaFavoritos(
        val uid: String,
        val favoritosIds: List<String>,
        val resueltos: List<ItemFavoritoUi>,
        val sincronizados: Boolean
    )

    private val favoritosFlow = MutableStateFlow<EntradaFavoritos?>(null)

    fun obtener(uid: String?, favoritosIds: List<String>?): Pair<List<ItemFavoritoUi>, Boolean>? {
        val uidLimpio = uid?.trim()?.takeIf(String::isNotEmpty) ?: return null
        if (favoritosIds == null) return null
        val entrada = favoritosFlow.value ?: return null
        return if (entrada.uid == uidLimpio && entrada.favoritosIds == favoritosIds) {
            entrada.resueltos to entrada.sincronizados
        } else {
            null
        }
    }

    fun guardar(
        uid: String?,
        favoritosIds: List<String>,
        resueltos: List<ItemFavoritoUi>,
        sincronizados: Boolean
    ) {
        val uidLimpio = uid?.trim()?.takeIf(String::isNotEmpty) ?: return
        favoritosFlow.value = EntradaFavoritos(
            uid = uidLimpio,
            favoritosIds = favoritosIds.toList(),
            resueltos = resueltos.toList(),
            sincronizados = sincronizados
        )
    }

    fun limpiar() {
        favoritosFlow.value = null
    }
}

internal fun resolverFavoritosDesdeCatalogos(
    favoritosIds: List<String>,
    todosLugares: Map<String, Lugar>,
    todosComercios: Map<String, Comercio>,
    categoriasMap: Map<String, String>
): List<ItemFavoritoUi> {
    val clavesVistas = mutableSetOf<Pair<Boolean, String>>()
    return favoritosIds.mapNotNull { idOriginal ->
        val idLimpio = idOriginal.trim()
        val lugar = todosLugares[idLimpio]
        if (lugar != null) {
            if (!clavesVistas.add(false to lugar.id)) return@mapNotNull null
            ItemFavoritoUi(
                id = lugar.id,
                idFavoritoOriginal = idOriginal,
                nombre = lugar.nombre,
                categoriaRes = lugar.categoriaResuelta.etiquetaRes,
                imagenUrl = lugar.imagenPortadaUrl,
                calificacion = lugar.calificacion.takeIf { it > 0.0 },
                esComercio = false
            )
        } else {
            val idComercioSinPrefijo = idLimpio.removePrefix("comercio:")
            val comercio = todosComercios[idComercioSinPrefijo] ?: todosComercios[idLimpio]
            if (comercio != null) {
                if (!clavesVistas.add(true to comercio.id)) return@mapNotNull null
                val etiquetaCategoria = categoriasMap[comercio.categoria.trim()]
                    ?: comercio.categoria
                ItemFavoritoUi(
                    id = comercio.id,
                    idFavoritoOriginal = idOriginal,
                    nombre = comercio.nombre,
                    categoria = etiquetaCategoria,
                    imagenUrl = comercio.imagenUrls.firstOrNull().orEmpty(),
                    calificacion = comercio.calificacionPromedio?.takeIf { it > 0.0 },
                    esComercio = true
                )
            } else {
                null
            }
        }
    }
}

internal fun intentarResolverFavoritosEnCache(
    uid: String?,
    favoritosIds: List<String>?
): Pair<List<ItemFavoritoUi>, Boolean>? {
    if (favoritosIds == null) return null
    if (favoritosIds.isEmpty()) return emptyList<ItemFavoritoUi>() to true

    PerfilCacheSesion.obtener(uid, favoritosIds)?.let { return it }

    val lugaresCache = LugaresRepository.lugaresEnCache ?: return null
    val lugaresMap = lugaresCache.associateBy { it.id.trim() }
    val todosSonLugares = favoritosIds.all { lugaresMap.containsKey(it.trim()) }
    val comerciosCache = ComercioRepository.publicadosEnCache

    if (!todosSonLugares && comerciosCache == null) return null

    val comerciosMap = comerciosCache.orEmpty().associateBy { it.id.trim() }
    val categoriasMap = ComercioRepository.categoriasEnCache
        .orEmpty()
        .associate { categoria: CategoriaComercio -> categoria.id.trim() to categoria.etiqueta }

    val resueltos = resolverFavoritosDesdeCatalogos(
        favoritosIds = favoritosIds,
        todosLugares = lugaresMap,
        todosComercios = comerciosMap,
        categoriasMap = categoriasMap
    )
    PerfilCacheSesion.guardar(
        uid = uid,
        favoritosIds = favoritosIds,
        resueltos = resueltos,
        sincronizados = true
    )
    return resueltos to true
}
