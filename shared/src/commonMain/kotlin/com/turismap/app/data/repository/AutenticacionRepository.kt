package com.turismap.app.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.repo_auth_error_cargar_perfil
import turismap.shared.generated.resources.repo_auth_error_contrasena_debil
import turismap.shared.generated.resources.repo_auth_error_contrasena_longitud
import turismap.shared.generated.resources.repo_auth_error_correo_en_uso
import turismap.shared.generated.resources.repo_auth_error_correo_invalido
import turismap.shared.generated.resources.repo_auth_error_correo_recuperacion
import turismap.shared.generated.resources.repo_auth_error_crear_cuenta
import turismap.shared.generated.resources.repo_auth_error_credenciales_invalidas
import turismap.shared.generated.resources.repo_auth_error_credenciales_vacias
import turismap.shared.generated.resources.repo_auth_error_cuenta_deshabilitada
import turismap.shared.generated.resources.repo_auth_error_demasiados_intentos
import turismap.shared.generated.resources.repo_auth_error_enviar_recuperacion
import turismap.shared.generated.resources.repo_auth_error_google
import turismap.shared.generated.resources.repo_auth_error_guardar_perfil
import turismap.shared.generated.resources.repo_auth_error_iniciar_sesion
import turismap.shared.generated.resources.repo_auth_error_nombre_longitud
import turismap.shared.generated.resources.repo_auth_error_permisos
import turismap.shared.generated.resources.repo_auth_error_sin_conexion
import turismap.shared.generated.resources.repo_auth_error_sincronizar_google
import turismap.shared.generated.resources.repo_error_auth_usuario_creado_no_devuelto
import turismap.shared.generated.resources.repo_error_auth_usuario_google
import turismap.shared.generated.resources.repo_error_auth_usuario_no_devuelto
import turismap.shared.generated.resources.repo_nombre_viajero_predeterminado

object AutenticacionRepository {
    private val auth by lazy { Firebase.auth }

    val usuarioActual: FirebaseUser?
        get() = auth.currentUser

    internal suspend fun traducirErrorAuth(
        error: Throwable,
        porDefectoRes: StringResource
    ): Exception {
        val msg = generateSequence(error as Throwable?) { it.cause }
            .mapNotNull(Throwable::message)
            .joinToString(" ")
            .lowercase()
        val recurso = when {
            msg.contains("invalid-credential") ||
                msg.contains("wrong-password") ||
                msg.contains("user-not-found") ||
                msg.contains("invalid_login_credentials") ->
                Res.string.repo_auth_error_credenciales_invalidas
            msg.contains("email-already-in-use") || msg.contains("already in use") ->
                Res.string.repo_auth_error_correo_en_uso
            msg.contains("invalid-email") || msg.contains("badly formatted") ->
                Res.string.repo_auth_error_correo_invalido
            msg.contains("weak-password") ->
                Res.string.repo_auth_error_contrasena_debil
            msg.contains("too-many-requests") || msg.contains("blocked") ->
                Res.string.repo_auth_error_demasiados_intentos
            msg.contains("network-request-failed") ||
                msg.contains("network") ||
                msg.contains("unable to resolve host") ||
                msg.contains("unavailable") ||
                msg.contains("deadline-exceeded") ->
                Res.string.repo_auth_error_sin_conexion
            msg.contains("permission-denied") ||
                msg.contains("permission_denied") ||
                msg.contains("insufficient permissions") ->
                Res.string.repo_auth_error_permisos
            msg.contains("user-disabled") ->
                Res.string.repo_auth_error_cuenta_deshabilitada
            else -> porDefectoRes
        }
        return IllegalStateException(getString(recurso), error)
    }

    suspend fun iniciarSesion(email: String, contrasena: String) {
        val emailLimpio = email.trim()
        require(emailLimpio.isNotBlank() && contrasena.isNotBlank()) {
            getString(Res.string.repo_auth_error_credenciales_vacias)
        }

        val resultado = try {
            auth.signInWithEmailAndPassword(emailLimpio, contrasena)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            throw traducirErrorAuth(error, Res.string.repo_auth_error_iniciar_sesion)
        }

        val usuario = resultado.user
            ?: throw IllegalStateException(getString(Res.string.repo_error_auth_usuario_no_devuelto))
        try {
            UsuarioRepository.asegurarPerfil(
                email = usuario.email ?: emailLimpio,
                nombre = usuario.displayName.orEmpty(),
                fotoUrl = usuario.photoURL
            )
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            auth.signOut()
            throw traducirErrorAuth(error, Res.string.repo_auth_error_cargar_perfil)
        }
    }

    suspend fun registrar(
        nombre: String,
        email: String,
        contrasena: String,
        intereses: List<String>
    ) {
        val nombreLimpio = nombre.trim()
        val emailLimpio = email.trim()

        require(nombreLimpio.length in 2..120) {
            getString(Res.string.repo_auth_error_nombre_longitud)
        }
        require(contrasena.length >= 8) {
            getString(Res.string.repo_auth_error_contrasena_longitud)
        }

        val resultado = try {
            auth.createUserWithEmailAndPassword(emailLimpio, contrasena)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            throw traducirErrorAuth(error, Res.string.repo_auth_error_crear_cuenta)
        }

        val usuario = resultado.user
            ?: throw IllegalStateException(getString(Res.string.repo_error_auth_usuario_creado_no_devuelto))
        try {
            usuario.updateProfile(displayName = nombreLimpio)
            UsuarioRepository.asegurarPerfil(
                email = usuario.email ?: emailLimpio,
                nombre = nombreLimpio,
                intereses = intereses,
                fotoUrl = usuario.photoURL
            )
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            try {
                usuario.delete()
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                // Si falla el borrado (p. ej. sin red), continuamos cerrando sesión
            }
            try {
                auth.signOut()
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                // Evitar enmascarar el error original de registro
            }
            throw traducirErrorAuth(error, Res.string.repo_auth_error_guardar_perfil)
        }
    }

    /**
     * Autenticación con Google usando el idToken obtenido del dispositivo.
     * Guarda la foto de perfil oficial y asegura el rol 'viajero'.
     */
    suspend fun iniciarSesionConGoogle(idToken: String) {
        val credencial = GoogleAuthProvider.credential(idToken = idToken, accessToken = null)
        val resultado = try {
            auth.signInWithCredential(credencial)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            throw traducirErrorAuth(error, Res.string.repo_auth_error_google)
        }

        val usuario = resultado.user
            ?: throw IllegalStateException(getString(Res.string.repo_error_auth_usuario_google))

        try {
            val nombrePredeterminado = getString(Res.string.repo_nombre_viajero_predeterminado)
            UsuarioRepository.asegurarPerfil(
                email = usuario.email.orEmpty(),
                nombre = usuario.displayName.orEmpty().ifBlank { nombrePredeterminado },
                fotoUrl = usuario.photoURL
            )
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            auth.signOut()
            throw traducirErrorAuth(error, Res.string.repo_auth_error_sincronizar_google)
        }
    }

    suspend fun enviarRestablecimientoContrasena(email: String) {
        val emailLimpio = email.trim()
        require(emailLimpio.isNotBlank()) {
            getString(Res.string.repo_auth_error_correo_recuperacion)
        }
        try {
            auth.sendPasswordResetEmail(emailLimpio)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            throw traducirErrorAuth(error, Res.string.repo_auth_error_enviar_recuperacion)
        }
    }

    suspend fun cerrarSesion() {
        UsuarioRepository.limpiarCacheUsuario()
        auth.signOut()
    }
}