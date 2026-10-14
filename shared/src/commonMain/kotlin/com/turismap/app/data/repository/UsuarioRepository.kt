package com.turismap.app.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.firestore
import kotlinx.serialization.Serializable

@Serializable
data class UsuarioBD(
    val email: String = "",
    val rol: String = "viajero",
    val nombre: String = "",
    val favoritos: List<String> = emptyList(),
    val intereses: List<String> = emptyList(),
    val fotoPerfil: String = "",
    val fotoPortada: String = ""
)

object UsuarioRepository {
    private val firestore by lazy { Firebase.firestore }

    fun obtenerIdUsuarioActual(): String? = Firebase.auth.currentUser?.uid

    suspend fun asegurarPerfil(email: String, nombre: String = "", intereses: List<String> = emptyList()) {
        val usuario = Firebase.auth.currentUser
            ?: throw IllegalStateException("No hay una sesión autenticada.")
        val referencia = firestore.collection("usuarios").document(usuario.uid)
        val documento = referencia.get()

        if (!documento.exists) {
            referencia.set(
                UsuarioBD.serializer(),
                UsuarioBD(
                    email = email,
                    nombre = nombre,
                    intereses = intereses
                )
            )
        }
    }

    suspend fun obtenerUsuarioPorId(idUsuario: String): UsuarioBD? {
        val documento = firestore.collection("usuarios").document(idUsuario).get()
        return if (documento.exists) documento.data(UsuarioBD.serializer()) else null
    }

    suspend fun actualizarPerfil(nombre: String) {
        val usuario = Firebase.auth.currentUser
            ?: throw IllegalStateException("No hay una sesión autenticada.")
        firestore.collection("usuarios").document(usuario.uid).update("nombre" to nombre)
        usuario.updateProfile(displayName = nombre)
    }

    suspend fun cambiarFavorito(lugarId: String, guardar: Boolean) {
        val uid = obtenerIdUsuarioActual()
            ?: throw IllegalStateException("Inicia sesión para guardar lugares favoritos.")
        val referencia = firestore.collection("usuarios").document(uid)
        val documento = referencia.get()

        if (!documento.exists) {
            asegurarPerfil(Firebase.auth.currentUser?.email.orEmpty())
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
}
