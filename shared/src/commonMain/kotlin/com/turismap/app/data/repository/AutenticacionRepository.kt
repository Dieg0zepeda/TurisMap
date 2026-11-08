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

    suspend fun iniciarSesion(email: String, contrasena: String) {
        val resultado = auth.signInWithEmailAndPassword(email.trim(), contrasena)
        val usuario = resultado.user
            ?: throw IllegalStateException("Firebase no devolvió el usuario autenticado.")
        try {
            UsuarioRepository.asegurarPerfil(
                email = usuario.email ?: email.trim(),
                nombre = usuario.displayName.orEmpty(),
                fotoUrl = usuario.photoURL
            )
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            auth.signOut()
            throw error
        }
    }

    suspend fun registrar(
        nombre: String,
        email: String,
        contrasena: String,
        intereses: List<String>
    ) {
        val resultado = auth.createUserWithEmailAndPassword(email.trim(), contrasena)
        val usuario = resultado.user
            ?: throw IllegalStateException("Firebase no devolvió el usuario creado.")
        try {
            usuario.updateProfile(displayName = nombre.trim())
            UsuarioRepository.asegurarPerfil(
                email = usuario.email ?: email.trim(),
                nombre = nombre.trim(),
                intereses = intereses,
                fotoUrl = usuario.photoURL
            )
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            auth.signOut()
            throw error
        }
    }

    /**
     * Autenticación con Google usando el idToken obtenido del dispositivo.
     * Guarda la foto de perfil oficial y asegura el rol 'viajero'.
     */
    suspend fun iniciarSesionConGoogle(idToken: String) {
        val credencial = GoogleAuthProvider.credential(idToken = idToken, accessToken = null)
        val resultado = auth.signInWithCredential(credencial)
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
            throw error
        }
    }

    suspend fun enviarRestablecimientoContrasena(email: String) {
        auth.sendPasswordResetEmail(email.trim())
    }

    suspend fun cerrarSesion() {
        auth.signOut()
    }
}