package com.turismap.app.data.repository

import com.turismap.app.components.getFirebaseFile
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.storage.storage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.getString
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.perfil_error_sin_sesion
import turismap.shared.generated.resources.repo_auth_error_cargar_perfil
import turismap.shared.generated.resources.repo_auth_error_nombre_longitud
import turismap.shared.generated.resources.repo_error_ruta_imagen_invalida
import turismap.shared.generated.resources.repo_error_sesion_actualizar_fotos
import turismap.shared.generated.resources.repo_error_sesion_favoritos
import turismap.shared.generated.resources.repo_error_sesion_historial_visitas
import turismap.shared.generated.resources.repo_usuario_error_actualizar_favoritos
import turismap.shared.generated.resources.repo_usuario_error_actualizar_perfil
import turismap.shared.generated.resources.repo_usuario_error_guardar_imagen
import turismap.shared.generated.resources.repo_usuario_error_registrar_visita

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
    private val storage by lazy { Firebase.storage }
    private val scopeObservacion by lazy {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    fun obtenerIdUsuarioActual(): String? = Firebase.auth.currentUser?.uid

    // ==========================================
    // 1. OBSERVADORES EN TIEMPO REAL (FLOW)
    // ==========================================

    /**
     * Flujo único centralizado ligado al estado de sesión de Firebase Auth.
     * Evita crear múltiples listeners de Firestore en recomposiciones o pantallas concurrentes.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val usuarioActualFlow: StateFlow<UsuarioBD?> by lazy {
        Firebase.auth.authStateChanged
            .onStart { emit(Firebase.auth.currentUser) }
            .map { it?.uid }
            .distinctUntilChanged()
            .flatMapLatest { uid ->
                if (uid.isNullOrBlank()) {
                    flowOf(null)
                } else {
                    observarUsuarioPorId(uid)
                }
            }
            .catch { emit(null) }
            .stateIn(
                scope = scopeObservacion,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
                initialValue = null
            )
    }

    /**
     * Devuelve el flujo compartido del usuario autenticado actual.
     */
    fun observarUsuarioActual(): StateFlow<UsuarioBD?> = usuarioActualFlow

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
            ?: throw IllegalStateException(getString(Res.string.perfil_error_sin_sesion))
        val referencia = firestore.collection("usuarios").document(usuario.uid)
        val documento = referencia.get()

        if (!documento.exists) {
            val fotoInicial = fotoUrl?.trim()
                ?.takeIf(::esUrlFotoHttpsValida)
                .orEmpty()
            // Esquema cerrado permitido por firestore.rules (sin enviar campos no autorizados como telefono)
            referencia.set(
                mapOf(
                    "email" to email.trim(),
                    "rol" to "viajero",
                    "nombre" to nombre.trim().take(120),
                    "favoritos" to emptyList<String>(),
                    "intereses" to intereses,
                    "fotoPerfil" to fotoInicial,
                    "fotoPortada" to ""
                )
            )
        } else {
            val usuarioBD = try {
                documento.data(UsuarioBD.serializer())
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                null
            }

            val fotoLimpia = fotoUrl?.trim()
            if (usuarioBD != null && usuarioBD.fotoPerfil.isBlank() && !fotoLimpia.isNullOrBlank() && esUrlFotoHttpsValida(fotoLimpia)) {
                referencia.update("fotoPerfil" to fotoLimpia)
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
            throw AutenticacionRepository.traducirErrorAuth(error, Res.string.repo_auth_error_cargar_perfil)
        }
    }

    // ==========================================
    // 3. MUTACIONES DE PERFIL Y FOTOS
    // ==========================================

    @Suppress("UNUSED_PARAMETER")
    suspend fun actualizarPerfil(nombre: String, telefono: String = "") {
        val usuario = Firebase.auth.currentUser
            ?: throw IllegalStateException(getString(Res.string.perfil_error_sin_sesion))

        val nombreLimpio = nombre.trim()
        require(nombreLimpio.length in 2..120) {
            getString(Res.string.repo_auth_error_nombre_longitud)
        }

        try {
            val docRef = firestore.collection("usuarios").document(usuario.uid)
            docRef.update("nombre" to nombreLimpio)
            usuario.updateProfile(displayName = nombreLimpio)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            throw AutenticacionRepository.traducirErrorAuth(error, Res.string.repo_usuario_error_actualizar_perfil)
        }
    }

    internal fun esUrlFotoHttpsValida(url: String): Boolean {
        val urlLimpia = url.trim()
        if (urlLimpia.isBlank()) return false
        if (urlLimpia.any(Char::isWhitespace)) return false
        if (urlLimpia.startsWith("file://", ignoreCase = true)) return false
        if (urlLimpia.startsWith("content://", ignoreCase = true)) return false
        return urlLimpia.startsWith("https://", ignoreCase = true)
    }

    suspend fun subirYActualizarFoto(perfil: Boolean, uriLocal: String): String {
        val uid = obtenerIdUsuarioActual()
            ?: throw IllegalStateException(getString(Res.string.repo_error_sesion_actualizar_fotos))

        val uriLimpia = uriLocal.trim()
        require(uriLimpia.isNotBlank()) {
            getString(Res.string.repo_error_ruta_imagen_invalida)
        }

        val archivo = getFirebaseFile(uriLimpia)
            ?: throw IllegalArgumentException(getString(Res.string.repo_error_ruta_imagen_invalida))

        val nombreArchivo = if (perfil) "perfil.jpg" else "portada.jpg"
        val referencia = storage.reference.child("usuarios/$uid/$nombreArchivo")

        val urlDescarga = try {
            referencia.putFile(archivo)
            referencia.getDownloadUrl()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            throw AutenticacionRepository.traducirErrorAuth(error, Res.string.repo_usuario_error_guardar_imagen)
        }

        actualizarFoto(perfil = perfil, url = urlDescarga)
        return urlDescarga
    }

    suspend fun actualizarFoto(perfil: Boolean, url: String) {
        val uid = obtenerIdUsuarioActual()
            ?: throw IllegalStateException(getString(Res.string.repo_error_sesion_actualizar_fotos))

        val urlLimpia = url.trim()
        require(esUrlFotoHttpsValida(urlLimpia)) {
            getString(Res.string.repo_error_ruta_imagen_invalida)
        }

        val campo = if (perfil) "fotoPerfil" else "fotoPortada"
        try {
            firestore.collection("usuarios").document(uid).update(campo to urlLimpia)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            throw AutenticacionRepository.traducirErrorAuth(error, Res.string.repo_usuario_error_guardar_imagen)
        }
    }

    // ==========================================
    // 4. FAVORITOS Y RUTAS
    // ==========================================

    suspend fun cambiarFavorito(lugarId: String, guardar: Boolean) {
        val uid = obtenerIdUsuarioActual()
            ?: throw IllegalStateException(getString(Res.string.repo_error_sesion_favoritos))
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
            throw AutenticacionRepository.traducirErrorAuth(error, Res.string.repo_usuario_error_actualizar_favoritos)
        }
    }

    suspend fun registrarVisita(lugarId: String, tipoInteraccion: String = "visita") {
        val uid = obtenerIdUsuarioActual()
            ?: throw IllegalStateException(getString(Res.string.repo_error_sesion_historial_visitas))
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
            throw AutenticacionRepository.traducirErrorAuth(error, Res.string.repo_usuario_error_registrar_visita)
        }
    }

    suspend fun contarVisitas(idUsuario: String): Int = try {
        firestore.collection("usuarios").document(idUsuario)
            .collection("historial_rutas")
            .get()
            .documents
            .size
    } catch (error: CancellationException) {
        throw error
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
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    null
                }
            }
            .distinct()
    } catch (error: CancellationException) {
        throw error
    } catch (_: Exception) {
        emptyList()
    }
}