package com.turismap.app.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import org.jetbrains.compose.resources.getString
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.repo_error_auth_usuario_creado_no_devuelto
import turismap.shared.generated.resources.repo_error_auth_usuario_google
import turismap.shared.generated.resources.repo_error_auth_usuario_no_devuelto
import turismap.shared.generated.resources.repo_nombre_viajero_predeterminado

object AutenticacionRepository {
    private val auth by lazy { Firebase.auth }

    val usuarioActual: FirebaseUser?
        get() = auth.currentUser

    suspend fun iniciarSesion(email: String, contrasena: String) {
        val resultado = auth.signInWithEmailAndPassword(email.trim(), contrasena)
        val usuario = resultado.user
            ?: throw IllegalStateException(getString(Res.string.repo_error_auth_usuario_no_devuelto))
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
            ?: throw IllegalStateException(getString(Res.string.repo_error_auth_usuario_creado_no_devuelto))
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