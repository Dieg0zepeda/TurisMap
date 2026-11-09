package com.turismap.app.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
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

    fun observarUsuarioActual(): Flow<UsuarioBD?> {
        val uid = obtenerIdUsuarioActual() ?: return flowOf(null)
        return firestore.collection("usuarios").document(uid).snapshots
            .map { snapshot ->
                if (snapshot.exists) snapshot.data(UsuarioBD.serializer()) else null
            }
            .catch { emit(null) }
    }

    fun observarUsuarioPorId(idUsuario: String): Flow<UsuarioBD?> =
        firestore.collection("usuarios").document(idUsuario).snapshots
            .map { snapshot ->
                if (snapshot.exists) snapshot.data(UsuarioBD.serializer()) else null
            }
            .catch { emit(null) }

    fun observarContadorVisitas(idUsuario: String): Flow<Int> =
        firestore.collection("usuarios").document(idUsuario)
            .collection("historial_rutas")
            .snapshots
            .map { it.documents.size }
            .catch { emit(0) }

    // ==========================================
    // 2. CONSULTAS PUNTUALES
    // ==========================================

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
                    nombre = nombre.trim().take(120),
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
        return try {
            val documento = firestore.collection("usuarios").document(idUsuario).get()
            if (documento.exists) documento.data(UsuarioBD.serializer()) else null
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            throw AutenticacionRepository.traducirErrorAuth(error, "No se pudo cargar el perfil del usuario.")
        }
    }

    // ==========================================
    // 3. MUTACIONES DE PERFIL Y FOTOS
    // ==========================================

    suspend fun actualizarPerfil(nombre: String, telefono: String = "") {
        val usuario = Firebase.auth.currentUser
            ?: throw IllegalStateException("No hay una sesión autenticada.")

        val nombreLimpio = nombre.trim()
        val telefonoLimpio = telefono.trim()

        require(nombreLimpio.length in 2..120) {
            "El nombre debe tener entre 2 y 120 caracteres."
        }

        if (telefonoLimpio.isNotBlank()) {
            val digitos = telefonoLimpio.count { it.isDigit() }
            require(digitos >= 8 && telefonoLimpio.length <= 40) {
                "Ingresa un teléfono válido de al menos 8 dígitos."
            }
        }

        try {
            val docRef = firestore.collection("usuarios").document(usuario.uid)
            docRef.update(
                "nombre" to nombreLimpio,
                "telefono" to telefonoLimpio
            )
            usuario.updateProfile(displayName = nombreLimpio)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            throw AutenticacionRepository.traducirErrorAuth(error, "No se pudo actualizar el perfil.")
        }
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
        try {
            firestore.collection("usuarios").document(uid).update(campo to urlLimpia)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            throw AutenticacionRepository.traducirErrorAuth(error, "No se pudo guardar la imagen.")
        }
    }

    // ==========================================
    // 4. FAVORITOS Y RUTAS
    // ==========================================

    suspend fun cambiarFavorito(lugarId: String, guardar: Boolean) {
        val uid = obtenerIdUsuarioActual()
            ?: throw IllegalStateException("Inicia sesión para guardar lugares favoritos.")
        try {
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
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            throw AutenticacionRepository.traducirErrorAuth(error, "No se pudo actualizar tus favoritos.")
        }
    }

    suspend fun registrarVisita(lugarId: String, tipoInteraccion: String = "visita") {
        val uid = obtenerIdUsuarioActual()
            ?: throw IllegalStateException("Inicia sesión para guardar el historial de visitas.")
        try {
            firestore.collection("usuarios").document(uid)
                .collection("historial_rutas")
                .add(
                    mapOf(
                        "lugarId" to lugarId,
                        "fechaVisita" to FieldValue.serverTimestamp,
                        "tipoInteraccion" to tipoInteraccion
                    )
                )
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            throw AutenticacionRepository.traducirErrorAuth(error, "No se pudo registrar la visita.")
        }
    }

    suspend fun contarVisitas(idUsuario: String): Int = try {
        firestore.collection("usuarios").document(idUsuario)
            .collection("historial_rutas")
            .get()
            .documents
            .size
    } catch (_: Exception) {
        0
    }

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