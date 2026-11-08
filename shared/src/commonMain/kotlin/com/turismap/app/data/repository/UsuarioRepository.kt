package com.turismap.app.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

@Serializable
data class UsuarioBD(
    val email: String = "",
    val rol: String = "viajero",
    val nombre: String = "",
    val telefono: String = "",
    val favoritos: List<String> = emptyList(),
    val intereses: List<String> = emptyList(),
    val fotoPerfil: String = "",
    val fotoPortada: String = ""
)

object UsuarioRepository {
    private val firestore by lazy { Firebase.firestore }

    fun obtenerIdUsuarioActual(): String? = Firebase.auth.currentUser?.uid

    // ==========================================
    // 1. OBSERVADORES EN TIEMPO REAL (FLOW)
    // ==========================================

    /**
     * Emite el perfil actualizado cada vez que cambia el documento en Firestore
     * (por ejemplo, al agregar o quitar favoritos desde cualquier pantalla).
     */
    fun observarUsuarioActual(): Flow<UsuarioBD?> {
        val uid = obtenerIdUsuarioActual() ?: return flowOf(null)
        return firestore.collection("usuarios").document(uid).snapshots.map { snapshot ->
            if (snapshot.exists) snapshot.data(UsuarioBD.serializer()) else null
        }
    }

    fun observarUsuarioPorId(idUsuario: String): Flow<UsuarioBD?> =
        firestore.collection("usuarios").document(idUsuario).snapshots.map { snapshot ->
            if (snapshot.exists) snapshot.data(UsuarioBD.serializer()) else null
        }

    fun observarContadorVisitas(idUsuario: String): Flow<Int> =
        firestore.collection("usuarios").document(idUsuario)
            .collection("historial_rutas")
            .snapshots
            .map { it.documents.size }

    // ==========================================
    // 2. CONSULTAS PUNTUALES
    // ==========================================

    /**
     * Asegura la existencia del documento en Firestore.
     * Si el usuario es nuevo, lo crea con rol "viajero" y su foto de perfil.
     * Si ya existe pero no tenía foto, le asigna la foto traída de Google.
     */
    suspend fun asegurarPerfil(
        email: String,
        nombre: String = "",
        intereses: List<String> = emptyList(),
        fotoUrl: String? = null
    ) {
        val usuario = Firebase.auth.currentUser
            ?: throw IllegalStateException("No hay una sesión autenticada.")
        val referencia = firestore.collection("usuarios").document(usuario.uid)
        val documento = referencia.get()

        if (!documento.exists) {
            referencia.set(
                UsuarioBD.serializer(),
                UsuarioBD(
                    email = email.trim(),
                    rol = "viajero",
                    nombre = nombre.trim(),
                    intereses = intereses,
                    fotoPerfil = fotoUrl.orEmpty()
                )
            )
        } else {
            val usuarioBD = try {
                documento.data(UsuarioBD.serializer())
            } catch (_: Exception) {
                null
            }

            if (usuarioBD != null && usuarioBD.fotoPerfil.isBlank() && !fotoUrl.isNullOrBlank()) {
                referencia.update("fotoPerfil" to fotoUrl.trim())
            }
        }
    }

    suspend fun obtenerUsuarioPorId(idUsuario: String): UsuarioBD? {
        val documento = firestore.collection("usuarios").document(idUsuario).get()
        return if (documento.exists) documento.data(UsuarioBD.serializer()) else null
    }

    // ==========================================
    // 3. MUTACIONES DE PERFIL Y FOTOS
    // ==========================================

    suspend fun actualizarPerfil(nombre: String, telefono: String = "") {
        val usuario = Firebase.auth.currentUser
            ?: throw IllegalStateException("No hay una sesión autenticada.")

        val docRef = firestore.collection("usuarios").document(usuario.uid)
        docRef.update("nombre" to nombre.trim())
        if (telefono.isNotBlank()) {
            docRef.update("telefono" to telefono.trim())
        }
        usuario.updateProfile(displayName = nombre.trim())
    }

    suspend fun actualizarFoto(perfil: Boolean, url: String) {
        val uid = obtenerIdUsuarioActual()
            ?: throw IllegalStateException("Inicia sesión para actualizar tus fotos.")

        val urlLimpia = url.trim()
        val esRutaValida = urlLimpia.isBlank() ||
                urlLimpia.startsWith("https://", ignoreCase = true) ||
                urlLimpia.startsWith("http://", ignoreCase = true) ||
                urlLimpia.startsWith("content://", ignoreCase = true) ||
                urlLimpia.startsWith("file://", ignoreCase = true)

        require(esRutaValida) {
            "Formato de ruta de imagen no válido."
        }

        val campo = if (perfil) "fotoPerfil" else "fotoPortada"
        firestore.collection("usuarios").document(uid).update(campo to urlLimpia)
    }

    // ==========================================
    // 4. FAVORITOS Y RUTAS
    // ==========================================

    suspend fun cambiarFavorito(lugarId: String, guardar: Boolean) {
        val uid = obtenerIdUsuarioActual()
            ?: throw IllegalStateException("Inicia sesión para guardar lugares favoritos.")
        val referencia = firestore.collection("usuarios").document(uid)
        val documento = referencia.get()

        if (!documento.exists) {
            val user = Firebase.auth.currentUser
            asegurarPerfil(
                email = user?.email.orEmpty(),
                nombre = user?.displayName.orEmpty(),
                fotoUrl = user?.photoURL
            )
        }

        val operacion = if (guardar) {
            FieldValue.arrayUnion(lugarId)
        } else {
            FieldValue.arrayRemove(lugarId)
        }
        referencia.update("favoritos" to operacion)
    }

    suspend fun registrarVisita(lugarId: String, tipoInteraccion: String = "visita") {
        val uid = obtenerIdUsuarioActual()
            ?: throw IllegalStateException("Inicia sesión para guardar el historial de visitas.")
        firestore.collection("usuarios").document(uid)
            .collection("historial_rutas")
            .add(
                mapOf(
                    "lugarId" to lugarId,
                    "fechaVisita" to FieldValue.serverTimestamp,
                    "tipoInteraccion" to tipoInteraccion
                )
            )
    }

    suspend fun contarVisitas(idUsuario: String): Int =
        firestore.collection("usuarios").document(idUsuario)
            .collection("historial_rutas")
            .get()
            .documents
            .size

    suspend fun obtenerHistorialVisitas(idUsuario: String): List<String> = try {
        firestore.collection("usuarios").document(idUsuario)
            .collection("historial_rutas")
            .get()
            .documents
            .mapNotNull { doc ->
                try {
                    doc.get<String>("lugarId")
                } catch (_: Exception) {
                    null
                }
            }
            .distinct()
    } catch (_: Exception) {
        emptyList()
    }
}