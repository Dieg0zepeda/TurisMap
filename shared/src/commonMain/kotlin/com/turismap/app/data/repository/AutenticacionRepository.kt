package com.turismap.app.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
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
            UsuarioRepository.asegurarPerfil(usuario.email ?: email.trim(), usuario.displayName.orEmpty())
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
                intereses = intereses
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
