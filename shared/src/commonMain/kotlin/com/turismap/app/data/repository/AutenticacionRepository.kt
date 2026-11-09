package com.turismap.app.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.CancellationException

object AutenticacionRepository {
    private val auth by lazy { Firebase.auth }

    val usuarioActual: FirebaseUser?
        get() = auth.currentUser

    internal fun traducirErrorAuth(error: Throwable, porDefecto: String): Exception {
        val msg = error.message?.lowercase().orEmpty()
        val traducido = when {
            msg.contains("invalid-credential") || msg.contains("wrong-password") || msg.contains("user-not-found") || msg.contains("invalid_login_credentials") ->
                "Correo o contraseña incorrectos. Verifica tus datos."
            msg.contains("email-already-in-use") || msg.contains("already in use") ->
                "Este correo electrónico ya está registrado en TurisMap."
            msg.contains("invalid-email") || msg.contains("badly formatted") ->
                "El formato del correo electrónico no es válido."
            msg.contains("weak-password") ->
                "La contraseña es muy débil. Usa al menos 8 caracteres."
            msg.contains("too-many-requests") || msg.contains("blocked") ->
                "Demasiados intentos fallidos. Espera un momento e intenta de nuevo."
            msg.contains("network-request-failed") || msg.contains("network") || msg.contains("unable to resolve host") || msg.contains("unavailable") || msg.contains("deadline-exceeded") ->
                "Sin conexión a internet. Verifica tu red e intenta nuevamente."
            msg.contains("permission-denied") || msg.contains("permission_denied") || msg.contains("insufficient permissions") ->
                "No tienes permisos para realizar esta acción o los datos no cumplen el formato."
            msg.contains("user-disabled") ->
                "Esta cuenta ha sido deshabilitada por un administrador."
            else -> porDefecto
        }
        return IllegalStateException(traducido, error)
    }

    suspend fun iniciarSesion(email: String, contrasena: String) {
        val emailLimpio = email.trim()
        require(emailLimpio.isNotBlank() && contrasena.isNotBlank()) {
            "Ingresa tu correo y contraseña para continuar."
        }

        val resultado = try {
            auth.signInWithEmailAndPassword(emailLimpio, contrasena)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            throw traducirErrorAuth(error, "No se pudo iniciar sesión. Verifica tus credenciales.")
        }

        val usuario = resultado.user
            ?: throw IllegalStateException("No se pudo obtener la sesión del usuario.")
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
            throw traducirErrorAuth(error, "No se pudo cargar el perfil del usuario.")
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
            "El nombre debe tener entre 2 y 120 caracteres."
        }
        require(contrasena.length >= 8) {
            "La contraseña debe tener al menos 8 caracteres."
        }

        val resultado = try {
            auth.createUserWithEmailAndPassword(emailLimpio, contrasena)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            throw traducirErrorAuth(error, "No se pudo crear la cuenta.")
        }

        val usuario = resultado.user
            ?: throw IllegalStateException("No se pudo confirmar el usuario creado.")
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
            auth.signOut()
            throw traducirErrorAuth(error, "No se pudo guardar el perfil del viajero.")
        }
    }

    suspend fun iniciarSesionConGoogle(idToken: String) {
        val credencial = GoogleAuthProvider.credential(idToken = idToken, accessToken = null)
        val resultado = try {
            auth.signInWithCredential(credencial)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            throw traducirErrorAuth(error, "Error al autenticar con Google.")
        }

        val usuario = resultado.user
            ?: throw IllegalStateException("No se pudo obtener el usuario de Google.")

        try {
            UsuarioRepository.asegurarPerfil(
                email = usuario.email.orEmpty(),
                nombre = usuario.displayName.orEmpty().ifBlank { "Viajero TurisMap" },
                fotoUrl = usuario.photoURL
            )
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            auth.signOut()
            throw traducirErrorAuth(error, "No se pudo sincronizar el perfil con Google.")
        }
    }

    suspend fun enviarRestablecimientoContrasena(email: String) {
        val emailLimpio = email.trim()
        require(emailLimpio.isNotBlank()) {
            "Ingresa tu correo electrónico para recuperar la contraseña."
        }
        try {
            auth.sendPasswordResetEmail(emailLimpio)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            throw traducirErrorAuth(error, "No se pudo enviar el correo de recuperación.")
        }
    }

    suspend fun cerrarSesion() {
        auth.signOut()
    }
}