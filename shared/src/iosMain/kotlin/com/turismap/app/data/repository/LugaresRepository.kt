package com.turismap.app.data.repository

import com.turismap.app.data.models.Lugar
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.CancellationException

private val firestore by lazy { Firebase.firestore }

actual object LugaresRepository {
    actual suspend fun obtenerTodos(): List<Lugar> {
        try {
            return firestore.collection("lugares").get().documents.map { document ->
                document.data<Lugar>().copy(id = document.id)
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            throw IllegalStateException("No se pudieron cargar los lugares desde Firestore.", error)
        }
    }

    actual suspend fun obtenerPorId(id: String): Lugar? {
        try {
            val document = firestore.collection("lugares").document(id).get()
            return if (document.exists) document.data<Lugar>().copy(id = document.id) else null
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            throw IllegalStateException("No se pudo cargar el lugar '$id' desde Firestore.", error)
        }
    }
}